package org.fao.unredd.portal.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import it.geosolutions.geostore.core.model.Resource;
import it.geosolutions.geostore.services.rest.GeoStoreClient;
import it.geosolutions.unredd.geostore.UNREDDGeostoreManager;
import it.geosolutions.unredd.geostore.model.UNREDDChartScript;
import it.geosolutions.unredd.geostore.model.UNREDDLayer;
import it.geosolutions.unredd.geostore.model.UNREDDLayerUpdate;
import it.geosolutions.unredd.geostore.model.UNREDDStatsDef;

public class RealTimeStats {
	
	private static Logger logger = Logger.getLogger(RealTimeStats.class);

    GeoStoreClient client = null;
    UNREDDGeostoreManager geostore = null;
        
	public RealTimeStats(GeoStoreClient client, UNREDDGeostoreManager geostore) {
		this.client = client;
		this.geostore = geostore;
	}

	public String run(String wktROI, long chartScriptId) throws RealTimeStatsException {
		
		// TODO: Check wktROI syntax and validity
		
		// Get the needed atomic WPS stats calls 
		List<WPSCall> wpsCalls = buildWPSCalls(wktROI, chartScriptId);
		
		// Check if there's something to run
		if (wpsCalls.size() == 0) {
			throw new RealTimeStatsException(RealTimeStatsException.Code.NO_STATS_TO_RUN);
		}

		// Launch processes in parallel
		List<WPSResult> wpsResults = dispatchWPSCalls(wpsCalls);
		
		// Just show something
		for (WPSResult wpsResult : wpsResults) {
			logger.debug("WPS Results: " + wpsResult.getStatsData());
		}
		
		// TODO run groovy script
		
		
		// BEGIN harcoded dumb response (TODO: Delete this entire block)
    	String chartURL = "http://demo1.geo-solutions.it/diss_geostore/rest/misc/category/name/ChartData/resource/name/drc_forest_area_charts_7_en/data?name=Demo%20Custom%20Stats";
    	
    	logger.debug("  Result URL: " + chartURL);
    	logger.info("RealTimeStats finished");
    	
    	return "{ \n"+
		  "   \"success\": true, \n"+
		  "   \"response_type\": \"result_embedded\", \n"+
		  "   \"link\": { \n"+
		  "      \"type\": \"text/html\", \n"+
		  "      \"href\": \""+ chartURL +"\" \n"+
		  "   } \n"+
		  "}";
		// END harcoded dumb response
	}
	
	List<WPSCall> buildWPSCalls(String wktROI, long chartScriptId) throws RealTimeStatsException {
		List<WPSCall> wpsCalls = new ArrayList<WPSCall>();
		
		// Log input params
		logger.info("Starting RealTimeStats");
		logger.debug("  ChartScriptID: " + String.valueOf(chartScriptId));
		logger.debug("  WKT ROI: " + wktROI);
		
		// Get ChartScript
		UNREDDChartScript chartScript = new UNREDDChartScript(client.getResource(chartScriptId));
		logger.info("> Chart Script: " + chartScript.getId());

		// Get StatsDefs
		List<Resource> statsDefs = geostore.searchStatsDefsByChartScript(chartScript);
		logger.info(" >> Stats Defs: " + statsDefs.toString());
		
		// For each StatsDef
		for (Resource statsRes : statsDefs) {
			// Get Collection of Layers
			UNREDDStatsDef statsDef = new UNREDDStatsDef(statsRes); 
			List<Resource> layers = geostore.searchLayersByStatsDef(statsDef);
			logger.info("  >>> Layers: " + layers.toString());
			
			if (layers.size() > 1) {
				throw new RealTimeStatsException(RealTimeStatsException.Code.MANY_TIME_LAYER_STATS);
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
				logger.info("   >>>> LayerUpdates: " + layerUpdates.toString());
				
				// For each Layer Update
				for (Resource layerUpdate : layerUpdates) {
					// Construct a new WPSCall
					logger.info("    >>>>> WPSCall for chart " + chartScript.getName() + ": Stats " + statsDef.getName() + ", Layer " + layer.getName() + ", and Update " + layerUpdate.getName());
					wpsCalls.add(new WPSCall(statsDef, new UNREDDLayerUpdate(layerUpdate)));
				}
			}
		}
		return wpsCalls;
	}
	
	List<WPSResult> dispatchWPSCalls(List<WPSCall> wpsCalls) throws RealTimeStatsException {
		List<WPSResult> wpsResults = new ArrayList<WPSResult>();
		
		// TODO parametrize number of simultaneous threads
		ExecutorService svc = Executors.newFixedThreadPool(2);
		
		ExecutorCompletionService<WPSResult> compSvc = new ExecutorCompletionService<WPSResult>(svc);
		
		List<Future<WPSResult>> pendingResults = new ArrayList<Future<WPSResult>>();
		for(WPSCall wpsCall : wpsCalls) {
			Future<WPSResult> future = compSvc.submit(wpsCall);
			pendingResults.add(future);
		}
		logger.debug("Submitted all " + pendingResults.size() + " WPS requests.");

		while (pendingResults.size() > 0) {
			try {
				Future<WPSResult> future = compSvc.take();
				WPSResult wpsResult = future.get();
				pendingResults.remove(future);
				wpsResults.add(wpsResult);
				logger.debug(wpsResults.size() + " processes finished, " + pendingResults.size() + " still pending.");
			} catch (InterruptedException e) {
				logger.warn("One WPS process execution was cancelled.", e);
			} catch (ExecutionException e) {
				logger.warn("One WPS process execution failed. Pending processes will be cancelled.", e.getCause());
				// Cancel all other pending processes.
				for (Future<WPSResult> noFuture : pendingResults) {
	                noFuture.cancel(true);
	            }
				// Close the thread pool
				svc.shutdown();
				// Re-throw cause
				throw new RealTimeStatsException(RealTimeStatsException.Code.WPS_EXECUTION_EXCEPTION, e.getCause());
			}
		}

		svc.shutdown();	
		logger.debug("All WPS processes successfully finished.");
		return wpsResults;
	}
	
	public static void main(String[] args) {
		GeoStoreClient client = new GeoStoreClient();
		client.setGeostoreRestUrl("http://demo1.geo-solutions.it/stg_geostore/rest");
		client.setUsername("admin");
		client.setPassword("Unr3dd");
		UNREDDGeostoreManager geostore = new UNREDDGeostoreManager(client);
		
		RealTimeStats stats = new RealTimeStats(client, geostore);
		try {
			System.out.println(stats.run("",26));
		} catch (RealTimeStatsException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
}
