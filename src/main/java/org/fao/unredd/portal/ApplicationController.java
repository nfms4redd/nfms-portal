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
import it.geosolutions.unredd.geostore.model.UNREDDLayer;
import it.geosolutions.unredd.geostore.model.UNREDDLayerUpdate;
import it.geosolutions.unredd.geostore.model.UNREDDStatsDef;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLDecoder;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.NotImplementedException;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ApplicationController {
    
	private static Logger logger = Logger.getLogger(ApplicationController.class);
	
    @Autowired
    GeoStoreClient client;
    
    @Autowired
	org.fao.unredd.portal.Config config;
        
    @Autowired
    net.tanesha.recaptcha.ReCaptchaImpl reCaptcha;

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
		/*              ID  HTTP  Message               */
		FEEDBACK_OK    (1, 200, "ajax_feedback_ok"),
		READ_ERROR     (2, 500, "ajax_read_error"),
		SYNTAX_ERROR   (3, 400, "ajax_syntax_error"),
		STORING_ERROR  (4, 500, "ajax_storing_error"),
		UNAUTHORIZED   (5, 401, "ajax_invalid_recaptcha");
		
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

	@RequestMapping(value = "/proxy")
	public final void proxyAjaxCall(
			@RequestParam(required = true, value = "url") String url,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		// URL needs to be url decoded
		url = URLDecoder.decode(url, "utf-8");

		OutputStreamWriter writer = new OutputStreamWriter(
				response.getOutputStream());
		HttpClient client = new HttpClient();
		try {
			HttpMethod method = null;

			// Split this according to the type of request
			if (request.getMethod().equals("GET")) {
				method = new GetMethod(url);
			} else if (request.getMethod().equals("POST")) {
				method = new PostMethod(url);

				// Set any eventual parameters that came with our original
				// request (POST params, for instance)
				@SuppressWarnings("rawtypes")
				Enumeration paramNames = request.getParameterNames();
				while (paramNames.hasMoreElements()) {
					String paramName = paramNames.nextElement().toString();
					((PostMethod) method).setParameter(paramName,
							request.getParameter(paramName));
				}
			} else {
				throw new NotImplementedException(
						"This proxy only supports GET and POST methods.");
			}

			// Execute the method
			client.executeMethod(method);

			// Set the content type, as it comes from the server
			Header[] headers = method.getResponseHeaders();
			for (Header header : headers) {
				if ("Content-Type".equalsIgnoreCase(header.getName())) {
					response.setContentType(header.getValue());
				}
			}

			// Write the body, flush and close
			writer.write(method.getResponseBodyAsString());
			writer.flush();
			writer.close();
		} catch (HttpException e) {
			writer.write(e.toString());
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			writer.write(e.toString());
			throw e;
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
		UNREDDGeostoreManager geostore = new UNREDDGeostoreManager(client);
		try {
			geostore.insertFeedback(attributes, data);
			response.getWriter().write(AjaxResponses.FEEDBACK_OK.getJson()); // Correct!
		} catch (Exception e) { // GeoStore error.
			logger.error(e);
			response.sendError(AjaxResponses.STORING_ERROR.status, AjaxResponses.STORING_ERROR.getJson());
		}
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
    
    private String setLayerTimes() {
        List<Resource> layers = null;
        UNREDDGeostoreManager manager = null;
    	String jsonLayers = config.getLayers();
        try {
            manager = new UNREDDGeostoreManager(client);
            layers = manager.getLayers();
        } catch (Exception ex) {
        	logger.error("Error connecting to GeoStore", ex);
        }
        
        if (layers != null) {
            for (Resource layer : layers) {
                String wmsTimes;
                try {
                    wmsTimes = getWmsTimeString(manager, layer);
                    jsonLayers = jsonLayers.replaceAll("\\$\\{time\\."+layer.getName()+"\\}", wmsTimes.toString());
                } catch (Exception ex) {
                	logger.error("Error getting time dimension for layer" + layer.getName());
                }
            }
        }
        
        return jsonLayers;
    }
    
    private String getWmsTimeString(UNREDDGeostoreManager manager, Resource layer) throws JAXBException, UnsupportedEncodingException {
        StringBuilder wmsTimes = new StringBuilder();
        List<Resource> layerUpdates = manager.searchLayerUpdatesByLayerName(layer.getName());
        Iterator<Resource> iterator = layerUpdates.iterator();
        while (iterator.hasNext()) {
            UNREDDLayerUpdate unreddLayerUpdate = new UNREDDLayerUpdate(iterator.next());
            String year  = unreddLayerUpdate.getAttribute(UNREDDLayerUpdate.Attributes.YEAR);
            String month = unreddLayerUpdate.getAttribute(UNREDDLayerUpdate.Attributes.MONTH);
            
            // build wms time string manually
            wmsTimes.append(year).append("-");
            
            if (month != null) {
                if (month.length() == 1) wmsTimes.append("0");
                wmsTimes.append(month);
            }
            else wmsTimes.append("01"); // Assign january if data is updated yearly and month is not there

            wmsTimes.append("-01T00:00:00.000Z"); // period is year or month, so the rest of the time string is always the same
            
            if (iterator.hasNext()) {
                wmsTimes.append(",");
            }
        }
        
        return wmsTimes.toString();
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
    
    /*
    public static void main(String[] args) {
        LayersController controller = new LayersController();
        try {
            controller.test();
        } catch (Exception ex) {
            logger.error(null, ex);
        }
    }
    
    protected void test() throws Exception {
        UNREDDGeostoreManager manager = new UNREDDGeostoreManager("http://localhost:9191/geostore/rest", "admin", "admin");
        getStatsJson(manager);
    }
    */
    
    private String getStatsJson(UNREDDGeostoreManager manager) throws Exception
    {
        JSONObject jsonRoot = new JSONObject();
        
        List<Resource> statsDefResources = manager.getStatsDefs();
        for (Resource statsDefResource : statsDefResources)
        {
            JSONObject statsDefJsonObj = new JSONObject();
            
            UNREDDStatsDef unreddStatsDef = new UNREDDStatsDef(statsDefResource);
            List<String> statsDefLayerNames = unreddStatsDef.getReverseAttributes(UNREDDStatsDef.ReverseAttributes.LAYER.getName());
            
            String zonalLayerName = unreddStatsDef.getAttribute(UNREDDStatsDef.Attributes.ZONALLAYER);
            
            JSONObject layersJsonObj = new JSONObject();
            String zonalLayerAttributeId = "no_zonal_attribute_found"; // DEBUG
            for (String layerName : statsDefLayerNames) {
            	Resource layerResource = manager.searchLayer(layerName); // TODO: optimize this
                UNREDDLayer unreddLayer = new UNREDDLayer(layerResource);
                boolean isZonalLayer = zonalLayerName.equals(layerName);
                JSONObject layerJsonObj = getLayerJsonObj(layerName, isZonalLayer);
                if (isZonalLayer) zonalLayerAttributeId = unreddLayer.getAttribute(UNREDDLayer.Attributes.RASTERATTRIBNAME);
                layersJsonObj.element(layerName, layerJsonObj);
            }
            
            statsDefJsonObj.element("label", "this is the statsDef label");
            statsDefJsonObj.element("layers", layersJsonObj);
            
            // build stats url
            StringBuilder urlStringBuilder = new StringBuilder("/misc/category/ChartData/resource/");
            urlStringBuilder.append(statsDefResource.getName());
            urlStringBuilder.append("_%");
            urlStringBuilder.append(zonalLayerAttributeId);
            urlStringBuilder.append("%");
            
            statsDefJsonObj.element("url", urlStringBuilder.toString());
           
            jsonRoot.element(statsDefResource.getName(), statsDefJsonObj);
      }
        
        System.out.println(jsonRoot.toString()); // DEBUG
        return jsonRoot.toString();
    }

    private JSONObject getLayerJsonObj(String layerName, boolean isZonalLayer) throws Exception {
        JSONObject jsonObj = new JSONObject();
        jsonObj.element("wmsName", layerName);
        if (isZonalLayer) {
            jsonObj.element("zonal", isZonalLayer);
        }
                
        return jsonObj;
    }
    
    private boolean checkRecaptcha(String address, String challenge, String response) {
        return reCaptcha.checkAnswer(address, challenge, response).isValid();
    }

}
