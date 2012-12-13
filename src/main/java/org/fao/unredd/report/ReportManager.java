/*
 * nfms4redd Portal Interface - http://nfms4redd.org/
 *
 * (C) 2012, FAO Forestry Department (http://www.fao.org/forestry/)
 *
 * This application is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package org.fao.unredd.report;

import java.io.File;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import it.geosolutions.geostore.core.model.Resource;
import it.geosolutions.geostore.services.rest.GeoStoreClient;
import it.geosolutions.unredd.geostore.UNREDDGeostoreManager;
import it.geosolutions.unredd.geostore.model.UNREDDChartScript;
import it.geosolutions.unredd.geostore.model.UNREDDLayer;
import it.geosolutions.unredd.geostore.model.UNREDDLayerUpdate;
import it.geosolutions.unredd.geostore.model.UNREDDStatsDef;
import it.geosolutions.unredd.geostore.model.UNREDDLayer.Attributes;
import it.geosolutions.unredd.geostore.utils.NameUtils;
import it.geosolutions.unredd.stats.model.config.StatisticConfiguration;

public class ReportManager {
	
	private static Logger logger = Logger.getLogger(ReportManager.class);

    UNREDDGeostoreManager geostore = null;
    Properties config;
       
	public ReportManager(UNREDDGeostoreManager geostore, Properties config) {
		this.geostore = geostore;
		this.config = config;
	}

    public ReportManager(UNREDDGeostoreManager geostore) {
		this(geostore, null);
	}
	
	public URL get(String wktROI, long chartScriptId) throws ReportException {
		// Get ROI Geometry
		Geometry ROI;
		try {
			WKTReader wktReader = new WKTReader();
			ROI = wktReader.read(wktROI);
		} catch (ParseException e) {
			logger.warn("Error Parsing WKT string " + wktROI, e);
			throw new ReportException(ReportException.Code.INVALID_WKT_ROI, e.getCause());
		}
		
		if (ROI == null) {
			logger.warn("ROI is missing" + wktROI);
			throw new ReportException(ReportException.Code.INVALID_WKT_ROI);			
		}
		
		// Get the stats 
		Map<String, double[][]> statsData = runStats(ROI, chartScriptId);
		
		// Get the report (chart)
		String report = runChartScript(chartScriptId, statsData);
		logger.info("============ REPORT =============");
		logger.info(report);
		logger.info("============ END REPORT =============");
		
		// TODO Put report contents into a new CustomReport category in GeoStore. Think about needed attributes.
		String resourceURL = "http://demo1.geo-solutions.it/diss_geostore/rest/misc/category/name/ChartData/resource/name/drc_forest_area_charts_7_en/data?name=Demo%20Custom%20Stats";

    	logger.debug("Report finished. Result URL: " + resourceURL);
		
		try {
			return new URL(resourceURL);
		} catch (MalformedURLException e) {
			throw new ReportException(ReportException.Code.GEOSTORE_ERROR, e);
		}
	}
	
	String runChartScript(long chartScriptId, Map<String, double[][]> statsDataMap) throws ReportException {
		UNREDDChartScript chartScript = new UNREDDChartScript(geostore.getClient().getResource(chartScriptId));
		String scriptPath = chartScript.getAttribute(UNREDDChartScript.Attributes.SCRIPTPATH);
		
		ScriptRunner script = new ScriptRunner(new File(scriptPath));
		String result = script.callFunction("htmlChart", statsDataMap).toString();

		logger.debug("Chart script returned " + result);
		return result;
	}

	Map<String, double[][]> runStats(Geometry ROI, long chartScriptId) throws ReportException {
		OnlineStatsProcessor stats = new OnlineStatsProcessor(ROI, config);
		
		// Log input params
		logger.debug("Starting RealTimeStats");
		logger.debug("  ChartScriptID: " + String.valueOf(chartScriptId));
		logger.debug("  WKT ROI: " + ROI);
		
		// Get ChartScript
		UNREDDChartScript chartScript = new UNREDDChartScript(geostore.getClient().getResource(chartScriptId));
		logger.debug("> Chart Script: " + chartScript.getId());

		// Get StatsDefs
		List<Resource> statsDefs = geostore.searchStatsDefsByChartScript(chartScript);
		logger.debug(" >> Stats Defs: " + statsDefs.toString());
		
		// For each StatsDef
		for (Resource statsRes : statsDefs) {
			// Get Collection of Layers
			UNREDDStatsDef statsDef = new UNREDDStatsDef(statsRes); 
			List<Resource> layers = geostore.searchLayersByStatsDef(statsDef);
			logger.debug("  >>> Layers: " + layers.toString());
			
			if (layers.size() > 1) {
				throw new ReportException(ReportException.Code.MANY_TIME_LAYER_STATS);
				// In case of more than one time-dependent layer, the solution could be as in the Portal time slider:
				//   1. Merge all the timestamps into one common vector.
				//   2. For each time instant in the vector, get the most recent update in each layer.
				//      If there is not such update for all layers (v.gr. in one layer all updates are
				//      future respect the considered instant), do nothing.
				//   3. Calculate stats based on that combination.							
			}
			
			// For each Layer
			for (Resource layerRes : layers) {
				// Get all dependent Layer Updates
				UNREDDLayer layer = new UNREDDLayer(layerRes);
				List<Resource> layerUpdates = geostore.searchLayerUpdatesByLayerName(layer.getName());
				logger.debug("   >>>> LayerUpdates: " + layerUpdates.toString());
				
				// For each Layer Update
				for (Resource layerUpdateRes : layerUpdates) {
					// Construct a new WPSCall
					UNREDDLayerUpdate layerUpdate = new UNREDDLayerUpdate(layerUpdateRes);
					StatisticConfiguration statconf = buildStatisticConfiguration(statsDef, layer, layerUpdate);
					String date = layerUpdate.getDateAsString();
					
					stats.addJob(date, ROI, statconf);
					logger.debug("    >>>>> WPSCall for chart " + chartScript.getName() + ": Stats " + statsDef.getName() + ", Layer " + layer.getName() + ", and Update " + layerUpdate.getName());
				}
			}
		}

		// Finally, run the stats calculation
		return stats.processAll();
	}
	
	StatisticConfiguration buildStatisticConfiguration(UNREDDStatsDef statsDef, UNREDDLayer layer, UNREDDLayerUpdate layerUpdate) throws ReportException {
		// Get info from GeoStore objects
		String layerName = layer.getName();
		String year = layerUpdate.getAttribute(UNREDDLayerUpdate.Attributes.YEAR);
		String month = layerUpdate.getAttribute(UNREDDLayerUpdate.Attributes.MONTH);
		String day = layerUpdate.getAttribute(UNREDDLayerUpdate.Attributes.DAY);
        String rasterPath = layer.getAttribute(Attributes.MOSAICPATH);
		String statDefXML = geostore.getClient().getData(statsDef.getId());
		
        // Build the location to layerUpdate file, and replace token in stats XML
        String rasterFile = NameUtils.buildTifFileName(layerName, year, month, day);
        String rasterFullPath = new File(rasterPath, rasterFile).getAbsolutePath();
        statDefXML.replace("{FILEPATH}", rasterFullPath);
		
		try {
			JAXBContext context = JAXBContext.newInstance(StatisticConfiguration.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			StatisticConfiguration statConf = (StatisticConfiguration) unmarshaller.unmarshal(new StringReader(statDefXML));
			return statConf;
		} catch (JAXBException e) {
			logger.error("StatsConf invalid XML contents", e);
			throw new ReportException(ReportException.Code.INVALID_STATSDEF_XML, e);
		}
	}

	public static void main(String[] args) {
		GeoStoreClient client = new GeoStoreClient();
		client.setGeostoreRestUrl("http://demo1.geo-solutions.it/stg_geostore/rest");
		client.setUsername("admin");
		client.setPassword("Unr3dd");
		UNREDDGeostoreManager geostore = new UNREDDGeostoreManager(client);
		
		ReportManager report = new ReportManager(geostore);
		try {
			System.out.println(report.get("POLYGON((21.073607809505024 -2.6035981542402555, 24.105834371884185 -1.4617579288653582, 24.765014059357643 -3.3496552215281192, 21.996459371968403 -4.3580986905562, 21.073607809505024 -2.6035981542402555))", 26));
		} catch (ReportException e) {
			System.out.println(e.getMessage());
		}
	}
}
