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

/**
 * Contains the WPS execution results, plus a reference to the original inputs and {@link WPSCall} id.
 * 
 * @author Oscar Fonts
 */
public class WPSResult {

	final WPSCall call;
	final Object output;
	
	/**
	 * Creates a WPSResult, indicating the {@link WPSCall} and the execute output.
	 */
	public WPSResult(WPSCall call, Object output) {
		this.call = call;
		this.output = output;
	}
	
	/**
	 * @return The result identificator, equaling the {@link WPSCall} identificator whose execution generated this result.
	 *
	 * @see WPSCall#WPSCall(String, WPSProcess, Map, Class)
	 */
	public String getId() {
		return call.getId();
	}

	/**
	 * @return The original inputs whose execution is this result.
	 */
	public Map<String, Object> getInputs() {
		return call.getInputs();
	}
	
	/**
	 * @return The actual output. The return Class will be the indicated in the {@link WPSCall}.
	 * 
	 * @see WPSCall#WPSCall(String, WPSProcess, Map, Class)
	 */
	public Object getOutput() {
		return this.output;
	}
}
