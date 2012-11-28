package org.fao.unredd.portal.stats;

public class RealTimeStatsException extends Exception {

	enum Code {
		/**
		 * StatsDef depend on many layers, and we haven't implemented this case.
		 */
		MANY_TIME_LAYER_STATS,
		/**
		 * One of the WPS processes has been interrupted.
		 */
		WPS_PROCESS_INTERRUPTED,
		/**
		 * One of the WPS processes has been interrupted.
		 */		
		WPS_EXECUTION_EXCEPTION,
		/**
		 * There are no stats to run. Probable cause is no layerUpdates associated to the statsDef.
		 */			
		NO_STATS_TO_RUN
	}

	private static final long serialVersionUID = 1L;

	private Code code;

	public RealTimeStatsException(Code code) {
		super();
		this.code = code;
	}
	
	public RealTimeStatsException(Code code, String message) {
		super(message);
		this.code = code;
	}

	public RealTimeStatsException(Code code, Throwable cause) {
		super(cause);
		this.code = code;
	}

	public RealTimeStatsException(Code code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}
	
	public Code getCode() {
		return code;
	}

}
