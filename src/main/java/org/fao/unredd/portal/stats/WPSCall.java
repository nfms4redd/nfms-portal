package org.fao.unredd.portal.stats;

import it.geosolutions.unredd.geostore.model.UNREDDLayerUpdate;
import it.geosolutions.unredd.geostore.model.UNREDDStatsDef;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import net.opengis.wps10.ProcessDescriptionType;
import net.opengis.wps10.ProcessDescriptionsType;

import org.apache.log4j.Logger;
import org.geotools.data.wps.WPSFactory;
import org.geotools.data.wps.WebProcessingService;
import org.geotools.data.wps.request.DescribeProcessRequest;
import org.geotools.data.wps.response.DescribeProcessResponse;
import org.geotools.ows.ServiceException;

/**
 * @author Oscar Fonts
 */
public class WPSCall implements Callable<WPSResult> {

	private static Logger logger = Logger.getLogger(RealTimeStats.class);
	
	UNREDDStatsDef statsDef;
	UNREDDLayerUpdate layerUpdate;
	
	public WPSCall(UNREDDStatsDef statsDef, UNREDDLayerUpdate layerUpdate) {
		this.statsDef = statsDef;
		this.layerUpdate = layerUpdate;
	}

	@Override
	public WPSResult call() throws RealTimeStatsException {
		Thread curThread = Thread.currentThread();
		logger.debug(curThread.getName() + " Starting WPS " + layerUpdate.getName());

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			logger.warn(curThread.getName() + " Interrupted WPS: " + e.getMessage());
		}

		//executeWPS();

		WPSResult result = new WPSResult(statsDef, layerUpdate);
	    double[][] fakeData = string2array("0;2.7054510603632188E12;2.877681315625E7\n" +
    	    "1;1.4063107478184375E11;1.5722685498765625E11\n "+
    	    "2;4.545089675834375E10;8.87870496021875E9\n" +
    	    "3;5.530039613040625E10;3.51526227321E11\n" +
    	    "4;6.034334891771875E10;9.3727609985E10\n" +
    	    "5;7.5594055541125E10;9.731188803175E10\n" +
    	    "6;2.3177936950590625E11;2.563238154174375E11\n" +
    	    "7;1.010258168434375E10;9.51275621125E8\n" +
    	    "8;2.989867189859375E10;9.8721240096625E10\n" +
    	    "9;1.471711473171875E10;4.5632077002375E10\n"+
    	    "10;7.697938152725E10;4.2798827305971875E11\n" +
    	    "11;2.0855088763E10;4.356016757334375E10");
		result.setStatsData(fakeData);
		
		logger.debug(curThread.getName() + " Ending WPS " + layerUpdate.getName());		
		return result;
	}
	
	
	String executeWPS() throws ServiceException, IOException {
		// TODO: Externalize WPS endpoint
		URL url = new URL("http://localhost:8080/geoserver/ows?service=WPS&request=GetCapabilities");
		WebProcessingService wps = new WebProcessingService(url);
		
		//WPSCapabilitiesType capabilities = wps.getCapabilities();
		
		//ProcessOfferingsType processOfferings = capabilities.getProcessOfferings();
		//EList processes = processOfferings.getProcess();
		
		DescribeProcessRequest descRequest = wps.createDescribeProcessRequest();
		descRequest.setIdentifier("py:ZonalStats");
		// TODO handle serviceException here
		DescribeProcessResponse descResponse = wps.issueRequest(descRequest);
		ProcessDescriptionsType processDesc = descResponse.getProcessDesc();
		ProcessDescriptionType pdt = (ProcessDescriptionType) processDesc.getProcessDescription().get(0);
		WPSFactory wpsfactory = new WPSFactory(pdt, url);

		org.geotools.process.Process process = wpsfactory.create();
		Map<String, Object> map = new TreeMap<String, Object>();
		map.put("input_a", null);
		
		// TODO handle execution responses (in case something goes wrong...)
		// http://docs.geotools.org/stable/userguide/unsupported/wps.html
		Map<String, Object> results = process.execute(map, null);
		String result = (String) results.get("result");
		return result;	
	}
	
	double[][] string2array(String str) {
		double[][] ret = null;
		String rows[] = str.split("[\\r?\\n]+");
		for (int i = 0; i < rows.length ; i++) {
			String cols[] = rows[i].split(";");
			if (ret == null) {
				ret = new double[rows.length][cols.length];
			}
			for (int j = 0; j < cols.length; j++) {
				ret[i][j] = Double.parseDouble(cols[j]);
			}
		}
		return ret;
	}

}
