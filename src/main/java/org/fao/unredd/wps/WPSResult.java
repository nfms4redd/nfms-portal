package org.fao.unredd.wps;

import java.util.Map;

public class WPSResult {

	WPSCall call;
	Map<String, Object> outputs;
	
	public WPSResult(WPSCall call, Map<String, Object> outputs) {
		this.call = call;
		this.outputs = outputs;
	}
	
	public String getId() {
		return call.getId();
	}

	public Map<String, Object> getInputs() {
		return call.getInputs();
	}
	
	public Map<String, Object> getOutputs() {
		return this.outputs;
	}
}
