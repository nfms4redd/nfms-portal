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
package org.fao.unredd.wps;

import it.geosolutions.unredd.stats.model.config.StatisticConfiguration;

import java.io.IOException;
import java.util.Map;

import net.opengis.ows.x11.ExceptionReportDocument;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.geotools.feature.FeatureCollection;

import org.n52.wps.client.ExecuteRequestBuilder;
import org.n52.wps.client.ExecuteResponseAnalyser;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;

import com.vividsolutions.jts.geom.Geometry;

/**
 * WPS process executor.
 * 
 * @author Oscar Fonts
 */
public class WPSProcess {
	
	private static Logger logger = Logger.getLogger(WPSProcess.class);
	
	static final String UTF8_ENCODING = "UTF-8";
	static final String XML_MIME_TYPE = "text/xml";
	static final String GML3_MIME_TYPE = "text/xml; subtype=gml/3.1.1";
	static final String GML3_FEATURE_SCHEMA = "http://schemas.opengis.net/gml/3.1.1/base/feature.xsd";
	static final String GML3_GEOMETRY_SCHEMA = "http://schemas.opengis.net/gml/3.1.1/base/geometryBasic2d.xsd";
	
	protected final String baseURL;
	protected final WPSClientSession client;
	protected final ProcessDescriptionType process;
	
	/**
	 * Bound to a specific process in a specific WPS service,
	 * and perform a describeProcess on the service.
	 * 
	 * @param baseURL The WPS base URL (that is, without parameters: *not* the capabilities URL)
	 * @param pocessName The process identifier.
	 * @throws WPSClientException Error getting process description from the WPS service.
	 */
	public WPSProcess(String baseURL, String pocessName) throws WPSClientException {
		this.baseURL = baseURL;
		client = WPSClientSession.getInstance();
		try {
			process =  client.getProcessDescription(baseURL, pocessName);
		} catch (IOException e) {
			throw new WPSClientException("Error getting process description", e);
		}
	}
	
	/**
	 * Execute the process.
	 * 
	 * Inputs will be interpreted as:
	 * <ul>
	 * <li>Literal Strings
	 * <li>StatisticConfiguration
	 * <li>JTS Geometry
	 * <li>FeatureCollection
	 * <li>A FeatureCollection as reference (input is a String with an URL)
	 * </ul>
	 * 
	 * A single output is expected.
	 * 
	 * This method is meant to be thread safe.
	 * 
	 * @param inputs Input map. {@link AbstractGenerator Generators} for each input class have to be registered in <i>wps_config.xml</i> file.
	 * @param responseType The class for the output. {@link AbstractParser Parsers} for the output class has to be registered in <i>wps_config.xml</i> file.
	 * @return The output. An instance of the former class.
	 * @throws WPSClientException Something went wrong during the WPS execution, either locally or remotely.
	 * 
	 * @see <a href="http://52north.org/communities/geoprocessing/wps/architecture.html">52N WPS Architecture</a>
	 */
	@SuppressWarnings("unchecked")
	public <T> T execute(Map<String, Object> inputs, Class<T> responseType) throws WPSClientException {
		ExecuteRequestBuilder executeBuilder = new ExecuteRequestBuilder(process);

		for (InputDescriptionType input : process.getDataInputs().getInputArray()) {
			String inputName = input.getIdentifier().getStringValue();
			Object inputValue = inputs.get(inputName);
			
			// Value missing, but mandatory
			if (inputValue == null && input.getMinOccurs().intValue() > 0) {
				throw new WPSClientException("Property not set, but mandatory: " + inputName);
			}
			
			// Handle different input types
			if (input.getLiteralData() != null && inputValue != null) {
				
				// String literal
				executeBuilder.addLiteralData(inputName, inputValue.toString());
			} else if (input.getComplexData() != null) {
				// Complex data
				if (inputValue instanceof StatisticConfiguration) {
					// StatisticConfiguration
					IData data = new StatisticConfigurationDataBinding((StatisticConfiguration) inputValue);
					executeBuilder.addComplexData(inputName, data,
							null, UTF8_ENCODING, XML_MIME_TYPE);
				} else if (inputValue instanceof Geometry) {
					// Geometry
					IData data = new GeometryDataBinding((Geometry) inputValue);
					executeBuilder.addComplexData(inputName, data,
							GML3_GEOMETRY_SCHEMA, UTF8_ENCODING, GML3_MIME_TYPE);
				} else if (inputValue instanceof FeatureCollection) {
					// FeatureCollection
					@SuppressWarnings("rawtypes")
					IData data = new GTVectorDataBinding((FeatureCollection) inputValue);
					executeBuilder.addComplexData(inputName, data,
							GML3_FEATURE_SCHEMA, UTF8_ENCODING, GML3_MIME_TYPE);
				} else if (inputValue instanceof String) {
					// FeatureCollection as reference
					executeBuilder.addComplexDataReference(inputName, (String) inputValue,
							GML3_FEATURE_SCHEMA, UTF8_ENCODING, GML3_MIME_TYPE);
				} else {
					String message = "Input value of type " + inputValue.getClass().getSimpleName() + " is not supported by WPS Client, and won't be added to process execute request.";
					logger.error(message);
				}
			}
		}
		
		// Construct response type & response data binding
		Class<?> responseBinding = Object.class;
		if (responseType.isAssignableFrom(Geometry.class)) {
			executeBuilder.setRawData(null, UTF8_ENCODING, GML3_MIME_TYPE);
			responseBinding = GeometryDataBinding.class;
		} else if (responseType.isAssignableFrom(String.class)) {
			executeBuilder.setRawData(null, UTF8_ENCODING, XML_MIME_TYPE);
		}
		
		// Send the execute request
		ExecuteDocument execute = executeBuilder.getExecute();
		execute.getExecute().setService("WPS");
		Object responseObject = client.execute(baseURL, execute);

		IData data;
		if (responseObject instanceof ExceptionReportDocument) {
			ExceptionReportDocument response = (ExceptionReportDocument) responseObject;
			String message = StringEscapeUtils.unescapeXml(response.getExceptionReport().getExceptionArray(0).getExceptionTextArray(0));
			logger.error(message);
			throw new WPSClientException(message);
		} else if (responseObject instanceof ExecuteResponseDocument) {
			// Parse document response
			ExecuteResponseDocument response = (ExecuteResponseDocument) responseObject;
			if (response.getExecuteResponse().getStatus().isSetProcessFailed()) {
				String message = StringEscapeUtils.unescapeXml(response.getExecuteResponse().getStatus().getProcessFailed().getExceptionReport().getExceptionArray(0).getExceptionTextArray(0));
				logger.error(message);
				throw new WPSClientException(message);
			}
			ExecuteResponseAnalyser analyser = new ExecuteResponseAnalyser(execute, response, process);
			data = analyser.getComplexDataByIndex(0, responseBinding);
		} else {
			// Parse raw data response
			String outputId = process.getProcessOutputs().getOutputArray(0).getIdentifier().getStringValue();
			ExecuteResponseAnalyser analyser = new ExecuteResponseAnalyser(execute, responseObject, process);
			data = analyser.getComplexData(outputId, responseBinding);
		}
		
		return (T)data.getPayload();
	}
}
