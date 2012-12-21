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

/**
 * Handles possible errors during a Report construction.
 * 
 * Error causes are classified using the embedded {@link #Code} list. 
 * 
 * @author Oscar Fonts
 */
public class ReportException extends Exception {

	/**
	 * List of possible Report error causes.
	 */
	public enum Code {
		/**
		 * StatsDef depend on many layers, and we haven't implemented this case.
		 */
		MANY_TIME_LAYER_STATS,
		/**
		 * The WPS service cannot be accessed.
		 */
		WPS_SERVICE_NO_ACCESS,
		/**
		 * One of the WPS processes has been interrupted.
		 */		
		WPS_PROCESS_INTERRUPTED,
		/**
		 * One of the WPS processes has failed.
		 */		
		WPS_EXECUTION_EXCEPTION,
		/**
		 * There are no stats to run. Probable cause is no layerUpdates associated to the statsDef.
		 */			
		NO_STATS_TO_RUN,
		/**
		 * Error parsing the WKT ROI syntax.
		 */			
		INVALID_WKT_ROI,
		/**
		 * Groovy script not found.
		 */			
		GROOVY_SCRIPT_NOT_FOUND,
		/**
		 * Groovy script run error.
		 */			
		GROOVY_SCRIPT_RUN_ERROR,
		/**
		 * Groovy script: no 'execute' function found.
		 */				
		GROOVY_SCRIPT_NO_FUNCTION,
		/**
		 * Error in geostore access.
		 */	
		GEOSTORE_ERROR,
		/**
		 * The statistics definition in GeoStore is not valid.
		 */			
		INVALID_STATSDEF_XML
	}

	private static final long serialVersionUID = 1L;

	private Code code;

	public ReportException(Code code) {
		super();
		this.code = code;
	}
	
	public ReportException(Code code, String message) {
		super(message);
		this.code = code;
	}

	public ReportException(Code code, Throwable cause) {
		super(cause);
		this.code = code;
	}

	public ReportException(Code code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}
	
	public Code getCode() {
		return code;
	}

}
