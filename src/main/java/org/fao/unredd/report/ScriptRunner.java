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
