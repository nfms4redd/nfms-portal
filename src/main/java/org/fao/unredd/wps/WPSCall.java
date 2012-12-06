package org.fao.unredd.wps;

import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

/**
 * Executes a WPS process using the {@link Callable} interface,
 * so processes can be run in parallel in a multithreaded environment.
 * 
 * Process outcome is wrapped in a {@link WPSResult} object.
 * 
 * @author Oscar Fonts
 */
public class WPSCall implements Callable<WPSResult> {

	private static Logger logger = Logger.getLogger(WPSCall.class);
	
	String id;
	org.geotools.process.Process process;
	Map<String, Object> inputs;
	
	
	public WPSCall(String id, org.geotools.process.Process process, Map<String, Object> inputs) {
		this.id = id;
		this.process = process;
		this.inputs = inputs;
	}
	
	public String getId() {
		return id;
	}

	public Map<String, Object> getInputs() {
		return inputs;
	}

	@Override
	public WPSResult call() {
		Thread curThread = Thread.currentThread();
		logger.debug(curThread.getName() + " Start WPS process " + id);

		// TODO handle process status
		Map<String, Object> outputs = process.execute(inputs, null);
		
		logger.debug(curThread.getName() + " End WPS process " + id);
		return new WPSResult(this, outputs);
	}
}
