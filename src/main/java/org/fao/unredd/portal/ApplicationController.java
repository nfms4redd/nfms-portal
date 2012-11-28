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
package org.fao.unredd.portal;

import it.geosolutions.geostore.core.model.Resource;
import it.geosolutions.geostore.services.rest.GeoStoreClient;
import it.geosolutions.unredd.geostore.UNREDDGeostoreManager;
import it.geosolutions.unredd.geostore.model.UNREDDCategories;
import it.geosolutions.unredd.geostore.model.UNREDDLayerUpdate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;

import org.apache.log4j.Logger;
import org.fao.unredd.portal.stats.RealTimeStats;
import org.fao.unredd.portal.stats.RealTimeStatsException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ApplicationController {
    
	private static Logger logger = Logger.getLogger(ApplicationController.class);
	
    @Autowired
    GeoStoreClient client;
    
    @Autowired
	org.fao.unredd.portal.Config config;
        
    @Autowired
    net.tanesha.recaptcha.ReCaptchaImpl reCaptcha;
    
    UNREDDGeostoreManager geostore = null;

    /**
     * A collection of the possible AJAX responses.
     * 
     * Will associate a text message, an id, and an HTTP status
     * code to each response.
     * 
     * Includes method to serialize responses in JSON format
     * with "success", "id" and "message" properties, so the
     * client can easily handle them and render messages to the user.
     * 
     * @author Oscar Fonts
     */
	enum AjaxResponses {
		/*                    ID  HTTP  Message               */
		FEEDBACK_OK           (1, 200, "ajax_feedback_ok"),
		READ_ERROR            (2, 500, "ajax_read_error"),
		SYNTAX_ERROR          (3, 400, "ajax_syntax_error"),
		STORING_ERROR         (4, 500, "ajax_storing_error"),
		UNAUTHORIZED          (5, 401, "ajax_invalid_recaptcha"),
		MANY_TIME_LAYER_STATS (6, 500, "ajax_many_time_layer_stats");
		
		
		private int id, status;
		private String message;
		
		/**
		 * Constructor from enum values
		 * 
		 * @param id Response id
		 * @param status HTTP Status Code for the response
		 * @param message Response message text
		 */
		AjaxResponses(int id, int status, String message) {
			this.id = id;
			this.status = status;
			this.message = message;
		}
		
	    /**
	     * Format AJAX response body in JSON syntax, with a "success" flag,
	     * a message "id", and a "message" string.
	     */
	    String getJson() {
			Map<String, Object> contents = new HashMap<String, Object>();
			
			contents.put("success", status == 200);
			contents.put("id", id);
			contents.put("message", message);
			
			new Config();
			JSONObject json = new JSONObject();
			json.putAll(contents);
			return json.toString();
		}
	    
	    int getStatus() {
	    	return this.status;
	    }
    }  
	
	@RequestMapping(value="/index.do", method=RequestMethod.GET)
    public ModelAndView index(Model model) {
        ModelAndView mv = new ModelAndView();
        model.addAttribute("captchaHtml", reCaptcha.createRecaptchaHtml(null, null));
        mv.setViewName("index");
        return mv;
    }
    
    @RequestMapping("/messages.json")
    public ModelAndView locale() {
    	return new ModelAndView("messages", "messages", config.getMessages());
    }
       
    @RequestMapping("/static/**")
    public void getFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	// Get path to file
    	String fileName = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        
    	// Verify file exists
    	File file = new File(config.getDir()+"/static/"+fileName);
    	if (!file.isFile()) {
    		response.sendError(HttpServletResponse.SC_NOT_FOUND);
    		return;
    	}
		
    	// Manage cache headers: Last-Modified and If-Modified-Since
    	long ifModifiedSince = request.getDateHeader("If-Modified-Since");
    	long lastModified = file.lastModified();
    	if (ifModifiedSince >= (lastModified / 1000 * 1000)) {
    		response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    		return;
    	}
    	response.setDateHeader("Last-Modified", lastModified);
    	
    	// Set content type
    	FileNameMap fileNameMap = URLConnection.getFileNameMap();
		String type = fileNameMap.getContentTypeFor(fileName);
    	response.setContentType(type);
    	
    	// Send contents
    	try {
        	InputStream is = new FileInputStream(file);
            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
		} catch (IOException e) {
			logger.error("Error reading file", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
    }

	@RequestMapping(value="/feedback", method = RequestMethod.POST)
	public void feedback(HttpServletRequest request, HttpServletResponse response) throws IOException {	
		// Get posted attributes
		@SuppressWarnings("unchecked")
		Map<String, String> attributes = flattenParamValues(request.getParameterMap());
		
		// Check reCAPTCHA
		boolean authorized = this.checkRecaptcha(
			request.getRemoteAddr(),
			attributes.get("recaptcha_challenge"),
			attributes.get("recaptcha_response")
		);
		if (!authorized) {
			response.sendError(AjaxResponses.UNAUTHORIZED.status, AjaxResponses.UNAUTHORIZED.getJson());
			return;
		}
		
		// Get posted data (body)
		StringBuffer body = new StringBuffer();
		String line = null;
		BufferedReader reader;
		try {
			reader = request.getReader();
			while ((line = reader.readLine()) != null) {
				body.append(line);
			}
		} catch (IOException e) { // Error reading response body.
			logger.error(e);
			response.sendError(AjaxResponses.READ_ERROR.status, AjaxResponses.READ_ERROR.getJson());
		}
		
		// Validate posted JSON data syntax
		String data = body.toString();		
		try {
			 // Test syntax: Convert to JSON and back to String.
			data = JSONSerializer.toJSON(data).toString(2) ;
		} catch (JSONException e) { // Couldn't parse response body as JSON.
			logger.warn(e); 
			response.sendError(AjaxResponses.SYNTAX_ERROR.status, AjaxResponses.SYNTAX_ERROR.getJson());
		}
		
		// Insert Feedback data into GeoStore
		try {
			getGeostore().insertFeedback(attributes, data);
			response.getWriter().write(AjaxResponses.FEEDBACK_OK.getJson()); // Correct!
		} catch (Exception e) { // GeoStore error.
			logger.error(e);
			response.sendError(AjaxResponses.STORING_ERROR.status, AjaxResponses.STORING_ERROR.getJson());
		}
	}
	
	@RequestMapping("/charts.json")
	public void charts(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	response.setContentType("application/json;charset=UTF-8");
    	try {
			response.getWriter().print(getCharts());
            response.flushBuffer();
		} catch (Exception e) {
			logger.error(e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}		
	}
	
	@RequestMapping(value="/stats.json", method = RequestMethod.POST)
	public void stats(HttpServletRequest request, HttpServletResponse response) throws IOException, JAXBException {
		// Get posted attributes
		@SuppressWarnings("unchecked")
		Map<String, String> attributes = flattenParamValues(request.getParameterMap());
		
		// Get Chart Script Resource from ChartScriptId parameter
		long chartScriptId = Long.getLong(attributes.get("ChartScriptId"));
		
		// Get posted data (body)
		StringBuffer body = new StringBuffer();
		String line = null;
		BufferedReader reader;
		try {
			reader = request.getReader();
			while ((line = reader.readLine()) != null) {
				body.append(line);
			}
		} catch (IOException e) { // Error reading response body.
			logger.error(e);
			response.sendError(AjaxResponses.READ_ERROR.status, AjaxResponses.READ_ERROR.getJson());
		}
		String wktROI = body.toString();
		
		// Calculate Statistics
    	RealTimeStats stats = new RealTimeStats(client, getGeostore());
    	
		String result;
		try {
			result = stats.run(wktROI, chartScriptId);
		} catch (RealTimeStatsException e) {
			// TODO Return a HTTP 500 response, plus localized message failure wrapped in a json syntax structure.
			result = e.getMessage();
			e.printStackTrace();
			response.sendError(AjaxResponses.UNAUTHORIZED.status, AjaxResponses.UNAUTHORIZED.getJson());
		}
		
		// Flush response
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().print(result);
        response.flushBuffer();
	}
    
	@RequestMapping("/layers.json")
    public void layers(HttpServletResponse response) throws IOException {
    	
    	response.setContentType("application/json;charset=UTF-8");
    	try {
			response.getWriter().print(setLayerTimes());
            response.flushBuffer();
		} catch (IOException e) {
			logger.error("Error reading file", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
    }
	
	private String getCharts() throws UnsupportedEncodingException, JAXBException {
		Map<Long, String> resp = new HashMap<Long, String>();
		List<Resource> charts = getGeostore().getUNREDDResources(UNREDDCategories.CHARTSCRIPT);
		for(Resource chart : charts) {
			resp.put(chart.getId(), chart.getName());
		}
		JSONObject json = new JSONObject();
		json.putAll(resp);
		return json.toString();		
	}
    
    private String setLayerTimes() {
    	String jsonLayers = config.getLayers();
		Pattern patt = Pattern.compile("\\$\\{time\\.([\\w.]*)\\}");
		Matcher m = patt.matcher(jsonLayers);
		StringBuffer sb = new StringBuffer(jsonLayers.length());
		while (m.find()) { // Found time-dependant layer in json file
			String layerName = m.group(1);
			try {
				m.appendReplacement(sb, getLayerTimesFromGeostore(layerName));
			} catch (Exception e) {
				m.appendReplacement(sb, "");
				logger.error("Error getting layer times from GeoStore.");
			}
		}
		m.appendTail(sb);
		return sb.toString();
    }
    
    private String getLayerTimesFromGeostore(String layerName) throws JAXBException, UnsupportedEncodingException {
        StringBuilder timeString = new StringBuilder();
        List<Resource> layerUpdates = getGeostore().searchLayerUpdatesByLayerName(layerName);
        if (layerUpdates.size() == 0) {
        	logger.warn("Requested times for \"" + layerName  + "\", but no corresponding LayerUpdates found in GeoStore.");
        }
        Iterator<Resource> iterator = layerUpdates.iterator();
        while (iterator.hasNext()) {
            UNREDDLayerUpdate unreddLayerUpdate = new UNREDDLayerUpdate(iterator.next());
            String year  = unreddLayerUpdate.getAttribute(UNREDDLayerUpdate.Attributes.YEAR);
            String month = unreddLayerUpdate.getAttribute(UNREDDLayerUpdate.Attributes.MONTH);
            String day = unreddLayerUpdate.getAttribute(UNREDDLayerUpdate.Attributes.DAY);
            
            // build time string
            timeString.append(year);
            
            if (month != null) {
            	timeString.append("-");
                if (month.length() == 1) timeString.append("0");
                timeString.append(month);
                if (day != null) {
                	timeString.append("-");
                    if (day.length() == 1) timeString.append("0");
                    timeString.append(day);
                }
            }           
            if (iterator.hasNext()) {
                timeString.append(",");
            }
        }
        return timeString.toString();
    }
    
	private static Map<String, String> flattenParamValues(Map<String, String[]> oldMap) {
		Map<String, String> newMap = new HashMap<String, String>();
		for (Map.Entry<String, String[]> entry : oldMap.entrySet()) {
			String[] value = entry.getValue();
			if (value !=null && value.length > 0) {
				newMap.put(entry.getKey(), value[0]);
			}
		}
		return newMap;
	}
    
	private UNREDDGeostoreManager getGeostore() {
		if (geostore == null) {
			try {
				geostore = new UNREDDGeostoreManager(client);
			} catch (Exception ex) {
	        	logger.error("Error connecting to GeoStore", ex);
	        }
		}
		return geostore;
	}
    
    private boolean checkRecaptcha(String address, String challenge, String response) {
        return reCaptcha.checkAnswer(address, challenge, response).isValid();
    }

}
