package org.fao.unredd.report;

public class ReportException extends Exception {

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
		GEOSTORE_ERROR
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
