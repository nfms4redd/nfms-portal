package org.fao.unredd.report;

import it.geosolutions.unredd.stats.model.config.StatisticConfiguration;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.opengis.ows11.impl.DomainMetadataTypeImpl;
import net.opengis.wps10.OutputDescriptionType;
import net.opengis.wps10.ProcessDescriptionType;
import net.opengis.wps10.ProcessDescriptionsType;

import org.apache.log4j.Logger;

import org.fao.unredd.wps.WPSCall;
import org.fao.unredd.wps.WPSResult;

import org.geotools.data.wps.WPSFactory;
import org.geotools.data.wps.WebProcessingService;
import org.geotools.data.wps.request.DescribeProcessRequest;
import org.geotools.data.wps.response.DescribeProcessResponse;
import org.geotools.ows.ServiceException;

import com.vividsolutions.jts.geom.Geometry;

public class OnlineStatsProcessor {
	private static Logger logger = Logger.getLogger(OnlineStatsProcessor.class);

	private final String capabilitiesURL;
	private final String ProcessName;
	private final String ROIInputName;
	private final String statConfInputName;
	private final String outputName;
	private final int maxConcurrency;
	
	private WPSFactory processFactory; 
	private List<WPSCall> wpsCalls;
	
	public OnlineStatsProcessor(Geometry ROI, Properties config) throws ReportException {
		if (config == null) {
			config = new Properties();
		}
	
		capabilitiesURL = config.getProperty("stats.capabilities", "http://localhost/stg_geoserver/ows?service=WPS&request=GetCapabilities");
		ProcessName = config.getProperty("stats.process.name", "gs:OnlineStatsWPS");
		ROIInputName = config.getProperty("stats.process.input.name.roi", "geometry");
		statConfInputName = config.getProperty("stats.process.input.name.statconf", "statConf");
		outputName = config.getProperty("stats.output.name", "result");
		maxConcurrency = Integer.parseInt(config.getProperty("stats.concurrency", "2"));
		
		processFactory = createWPSFactory();
		wpsCalls = new ArrayList<WPSCall>();
	}

	public OnlineStatsProcessor(Geometry ROI) throws ReportException {
		this(ROI, null);
	}
	
	public void addJob(String date, Geometry ROI, StatisticConfiguration statconf) {
		// TODO Eventually add restrictions to ROI geometries
		org.geotools.process.Process process = processFactory.create();
		
		Map<String, Object> inputs = new TreeMap<String, Object>();
		inputs.put(ROIInputName,      ROI);
		inputs.put(statConfInputName, statconf);
		
		wpsCalls.add(new WPSCall(date, process, inputs));
	}
	
	// Run calls, parse outputs
	public Map<String, double[][]> processAll() throws ReportException {
		// Check if there's something to run
		if (wpsCalls.size() == 0) {
			throw new ReportException(ReportException.Code.NO_STATS_TO_RUN);
		}
		
		// Call the processes, wait for results
		List<WPSResult> wpsResults = callWPSProcesses(wpsCalls);
		
		// Parse WPS outputs & build response
		Map<String, double[][]> statsData = new TreeMap<String, double[][]>();
		for(WPSResult wpsResult : wpsResults) {
			try {
				String date = wpsResult.getId();
				double[][] data = csv2array((String)wpsResult.getOutputs().get(outputName));
				if (data == null) {
					logger.error("Error parsing WPS output. Make sure it is a numeric matrix in CSV format");
					throw new ReportException(ReportException.Code.WPS_EXECUTION_EXCEPTION);
				}
				statsData.put(date, data);
			} catch (ClassCastException e) {
				logger.error("Expected a String as WPS output '"+ outputName +"'", e);
				throw new ReportException(ReportException.Code.WPS_EXECUTION_EXCEPTION, e);
			} catch (NumberFormatException e) {
				logger.error("Error parsing WPS content. Make sure it is a numeric matrix in CSV format", e);
				throw new ReportException(ReportException.Code.WPS_EXECUTION_EXCEPTION, e);
			} catch (NullPointerException e) {
				logger.error("Error parsing WPS output. Make sure it is a numeric matrix in CSV format");
				throw new ReportException(ReportException.Code.WPS_EXECUTION_EXCEPTION);				
			}
		}
		
		return statsData;
	}
	
	// Connect to WPS service, check process description
	private WPSFactory createWPSFactory() throws ReportException {
		
		try {
			URL capabilitiesUrl = new URL(capabilitiesURL);
			WebProcessingService wps = new WebProcessingService(capabilitiesUrl);
			
			// WPSCapabilitiesType capabilities = wps.getCapabilities();
			// ProcessOfferingsType processOfferings = capabilities.getProcessOfferings();
			// EList processes = processOfferings.getProcess();
			
			DescribeProcessRequest descRequest = wps.createDescribeProcessRequest();
			descRequest.setIdentifier(ProcessName);
			DescribeProcessResponse descResponse = wps.issueRequest(descRequest);
			ProcessDescriptionsType processDesc = descResponse.getProcessDesc();
			ProcessDescriptionType pdt = (ProcessDescriptionType) processDesc.getProcessDescription().get(0);
			
			// Workaround for literals with no explicit data type -- should default to String
			// TODO document better and patch Geotools code
			OutputDescriptionType odt = (OutputDescriptionType)pdt.getProcessOutputs().getOutput().get(0);
			if (odt.getLiteralOutput().getDataType() == null) {
				class X extends DomainMetadataTypeImpl {
					X() { reference = "String"; }
				}
				odt.getLiteralOutput().setDataType(new X());				
			}
			
			WPSFactory factory = new WPSFactory(pdt, capabilitiesUrl);
			return factory;
		} catch (MalformedURLException e) {
			logger.error("Malformed WPS Capabilities URL '" + capabilitiesURL + "'", e);
			throw new ReportException(ReportException.Code.WPS_SERVICE_NO_ACCESS, e);
		} catch (ServiceException e) {
			logger.error("WPS error while accessing service description", e);
			throw new ReportException(ReportException.Code.WPS_EXECUTION_EXCEPTION, e);
		} catch (IOException e) {
			logger.error("Network access error while accessing WPS service description at " + capabilitiesURL, e);
			throw new ReportException(ReportException.Code.WPS_SERVICE_NO_ACCESS, e);
		}
	}	
	
	private List<WPSResult> callWPSProcesses(List<WPSCall> wpsCalls) throws ReportException {
		List<WPSResult> wpsResults = new ArrayList<WPSResult>();
		
		ExecutorService svc = Executors.newFixedThreadPool(maxConcurrency);
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
				throw new ReportException(ReportException.Code.WPS_EXECUTION_EXCEPTION, e.getCause());
			}
		}

		svc.shutdown();	
		logger.debug("All WPS processes successfully finished.");
		return wpsResults;
	}
	
	private double[][] csv2array(String str) {
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
