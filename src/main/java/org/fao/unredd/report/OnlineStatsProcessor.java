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

import it.geosolutions.unredd.stats.model.config.StatisticConfiguration;

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

import org.apache.log4j.Logger;

import org.fao.unredd.wps.WPSCall;
import org.fao.unredd.wps.WPSProcess;
import org.fao.unredd.wps.WPSResult;
import org.n52.wps.client.WPSClientException;

import com.vividsolutions.jts.geom.Geometry;

public class OnlineStatsProcessor {
	private static Logger logger = Logger.getLogger(OnlineStatsProcessor.class);

	private final String baseURL;
	private final String processName;
	private final String ROIInputName;
	private final String statConfInputName;
	private final String outputName;
	private final int maxConcurrency;

	private WPSProcess process;
	private List<WPSCall> wpsCalls;
	
	public OnlineStatsProcessor(Geometry ROI, Properties config) throws ReportException {
		if (config == null) {
			config = new Properties();
		}
	
		baseURL = config.getProperty("stats.url", "http://localhost/stg_geoserver/ows");
		processName = config.getProperty("stats.processname", "gs:OnlineStatsWPS");
		ROIInputName = config.getProperty("stats.inputname.roi", "geometry");
		statConfInputName = config.getProperty("stats.inputname.statconf", "statConf");
		outputName = config.getProperty("stats.outputname", "result");
		maxConcurrency = Integer.parseInt(config.getProperty("stats.concurrency", "2"));
		
		try {
			process = new WPSProcess(baseURL, processName);
			wpsCalls = new ArrayList<WPSCall>();
		} catch (WPSClientException e) {
			logger.error(e.getMessage(), e);
			throw new ReportException(ReportException.Code.WPS_SERVICE_NO_ACCESS, e);
		}
	}

	public OnlineStatsProcessor(Geometry ROI) throws ReportException {
		this(ROI, null);
	}
	
	public void addJob(String date, Geometry ROI, StatisticConfiguration statconf) {
		// TODO Eventually add restrictions to ROI geometries
		
		Map<String, Object> inputs = new TreeMap<String, Object>();
		inputs.put(ROIInputName,      ROI);
		inputs.put(statConfInputName, statconf);
		
		wpsCalls.add(new WPSCall(date, process, inputs, String.class));
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
				double[][] data = csv2array((String)wpsResult.getOutput());
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
