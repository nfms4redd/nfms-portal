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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;

/**
 * Runs a Groovy script.
 * 
 * Wraps exceptions into application specific {@link ReportException}s.
 * 
 * @author Oscar Fonts
 */
public class ScriptRunner {

	private static Logger logger = Logger.getLogger(ScriptRunner.class);
	
	ScriptEngine engine = null;
	String fileName = null;
	
	/**
	 * Loads a groovy script and checks its validity.
	 * 
	 * @param location local path to the Groovy script file to be run.
	 * @throws ReportException Location doesn't exist or is not recognized as a valid Groovy script.
	 */
	public ScriptRunner(File location) throws ReportException {
		try {
			if(!location.exists()) {
				throw new FileNotFoundException();
			}
			fileName = location.getName();
			ScriptEngineManager mgr = new ScriptEngineManager();
	        engine = mgr.getEngineByName("groovy");
			engine.eval(new FileReader(location));
		} catch (FileNotFoundException e) {
			logger.error("Script file not found '" + location.toString() + "': " + e.getMessage());
			throw new ReportException(ReportException.Code.GROOVY_SCRIPT_NOT_FOUND);
		} catch (ScriptException e) {
			logger.error("Script '" + fileName + "' loading failed: " + e.getMessage());
			e.printStackTrace();
			throw new ReportException(ReportException.Code.GROOVY_SCRIPT_RUN_ERROR);
		}
	}
	
	/**
	 * Call a specific function in the groovy script.
	 * 
	 * @param name The function name.
	 * @param arguments As many arguments as the function needs.
	 * @return the script function return.
	 * 
	 * @throws ReportException The function does not exist, or the script run failed.
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
