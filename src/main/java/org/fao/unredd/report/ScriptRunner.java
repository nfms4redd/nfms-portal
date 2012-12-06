/**
 * 
 */
package org.fao.unredd.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;

/**
 * @author oscar
 *
 */
public class ScriptRunner {

	private static Logger logger = Logger.getLogger(ScriptRunner.class);
	
	ScriptEngine engine = null;
	String fileName = null;
	
	/**
	 * 
	 * @throws ReportException 
	 */
	public ScriptRunner(File location) throws ReportException {
		try {
			fileName = location.getName();
			ScriptEngineManager mgr = new ScriptEngineManager();
	        engine = mgr.getEngineByName("groovy");
			engine.eval(new FileReader(location));
		} catch (FileNotFoundException e) {
			logger.error("Script file not found '" + fileName + "': " + e.getMessage());
			throw new ReportException(ReportException.Code.GROOVY_SCRIPT_NOT_FOUND);
		} catch (ScriptException e) {
			logger.error("Script '" + fileName + "' loading failed: " + e.getMessage());
			e.printStackTrace();
			throw new ReportException(ReportException.Code.GROOVY_SCRIPT_RUN_ERROR);
		}
	}
	
	/**
	 * 
	 * @throws ReportException 
	 */
	public Object callFunction(String name, Object... arguments) throws ReportException {
		try {
			Invocable inv = (Invocable) engine;
			return inv.invokeFunction(name, arguments);
		} catch (ScriptException e) {
			logger.error("Script run failed when executing '" + name + "' function: " + e.getMessage());
			e.printStackTrace();
			throw new ReportException(ReportException.Code.GROOVY_SCRIPT_RUN_ERROR);
		} catch (NoSuchMethodException e) {
			logger.error("No '" + name + "' function found in Groovy script.");
			throw new ReportException(ReportException.Code.GROOVY_SCRIPT_NO_FUNCTION);
		}
	}

}
