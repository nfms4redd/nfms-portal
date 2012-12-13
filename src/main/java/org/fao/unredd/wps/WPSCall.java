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
package org.fao.unredd.wps;

import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.n52.wps.client.WPSClientException;

/**
 * Executes a WPS process using the {@link Callable} interface.
 * Useful to run multiple calls in parallel in a multithreaded
 * environment.
 * 
 * Process response is wrapped in a {@link WPSResult} object.
 * 
 * @author Oscar Fonts
 */
public class WPSCall implements Callable<WPSResult> {

	private static Logger logger = Logger.getLogger(WPSCall.class);
	
	final String id;
	final Map<String, Object> inputs;
	final WPSProcess process;
	final Class<?> resultType;
	
	/**
	 * Every call is composed of the following parameters:
	 * 
	 * @param id Identificator, can be any string. The {@link WPSResult} from this call will be identified by this same string.
	 * @param process The WPS process to run.
	 * @param inputs A collection of process inputs.
	 * @param resultType The desired result class. The response will be parsed to an instance of this class, contained in the {@link WPSResult}.
	 * 
	 * @see WPSResult#getId()
	 * @see WPSResult#getOutput()
	 */
	public <T> WPSCall(String id, WPSProcess process, Map<String, Object> inputs, Class<T> resultType) {
		this.id = id;
		this.process = process;
		this.inputs = inputs;
		this.resultType = resultType;
	}
	
	public String getId() {
		return id;
	}

	public Map<String, Object> getInputs() {
		return inputs;
	}

	/**
	 * Actually executes the WPS process.
	 * 
	 * This method is usually called from a thread pool executor in a concurrent environment.
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public WPSResult call() throws WPSClientException {
		Thread curThread = Thread.currentThread();
		logger.debug(curThread.getName() + " Start WPS process " + id);

		Object result = process.execute(inputs, resultType);
		
		logger.debug(curThread.getName() + " End WPS process " + id);
		return new WPSResult(this, result);
	}
}
