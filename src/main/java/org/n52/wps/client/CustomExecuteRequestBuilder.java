package org.n52.wps.client;

import java.io.IOException;
import java.io.InputStream;

import net.opengis.wps.x100.ComplexDataType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.OutputDefinitionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.n52.wps.client.ExecuteRequestBuilder;
import org.n52.wps.client.StaticDataHandlerRepository;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.data.IData;

public class CustomExecuteRequestBuilder extends ExecuteRequestBuilder {
	
	public CustomExecuteRequestBuilder(ProcessDescriptionType processDesc) {
		super(processDesc);
	}

	private static Logger LOGGER = Logger.getLogger(CustomExecuteRequestBuilder.class);
	
	public boolean setRawData(String schema, String encoding, String mimeType) {
		if (processDesc.getProcessOutputs().getOutputArray().length != 1) {
			return false;
		}
		OutputDefinitionType output = execute.getExecute().addNewResponseForm().addNewRawDataOutput();
		output.setIdentifier(processDesc.getProcessOutputs().getOutputArray(0).getIdentifier());
		
		if (schema != null) {
			output.setSchema(schema);
		}
		if (mimeType != null) {
			output.setMimeType(mimeType);
		}
		if (encoding != null) {
			output.setEncoding(encoding);
		}
		return true;
	}
	
	
	public void addComplexData(String parameterID, IData value, String schema, String encoding, String mimeType) throws WPSClientException {
		GeneratorFactory fac = StaticDataHandlerRepository.getGeneratorFactory();
		InputDescriptionType inputDesc = getParameterDescription(parameterID);
		if (inputDesc == null) {
			throw new IllegalArgumentException("inputDesription is null for: " + parameterID);
		}
		if (inputDesc.getComplexData() == null) {
			throw new IllegalArgumentException("inputDescription is not of type ComplexData: " + parameterID);			
		}
		
			
		LOGGER.debug("Looking for matching Generator ..." + 
				" schema: " + schema +
				" mimeType: " + mimeType +
				" encoding: " + encoding);
		
		IGenerator generator = fac.getGenerator(schema, mimeType, encoding, value.getClass());
		
		if (generator == null) {
			// generator is still null
			throw new IllegalArgumentException("Could not find an appropriate generator for parameter: " + parameterID);
		}
		
		
		InputStream stream = null;  
				
			InputType input = execute.getExecute().getDataInputs().addNewInput();
			input.addNewIdentifier().setStringValue(inputDesc.getIdentifier().getStringValue());
			// encoding is UTF-8 (or nothing and we default to UTF-8)
			// everything that goes to this condition should be inline xml data
		try {
			
			if (encoding == null || encoding.equals("") || encoding.equalsIgnoreCase("CDATA") || encoding.equalsIgnoreCase(IOHandler.DEFAULT_ENCODING)){
					stream = generator.generateStream(value, mimeType, schema);
					
			}else if(encoding.equalsIgnoreCase("base64")){
					stream = generator.generateBase64Stream(value, mimeType, schema);
			}else{
				throw new WPSClientException("Encoding not supported");
			}
					ComplexDataType data = input.addNewData().addNewComplexData();
					
					if (encoding.equalsIgnoreCase("CDATA")) {
						XmlString xs = XmlString.Factory.newInstance();
						xs.setStringValue(IOUtils.toString(stream));
						data.set(xs);
					} else {
						data.set(XmlObject.Factory.parse(stream));
					}
					if (schema != null) {
						data.setSchema(schema);
					}
					if (mimeType != null) {
						data.setMimeType(mimeType);
					}
					if (encoding != null) {
						data.setEncoding(encoding);
					}
			}catch(XmlException e) {
					throw new IllegalArgumentException("error inserting node into execute request", e);
			} catch (IOException e) {
					throw new IllegalArgumentException("error reading generator output", e);
			}
			
	}
	
	private InputDescriptionType getParameterDescription(String id) {
		InputDescriptionType[] inputDescs = processDesc.getDataInputs().getInputArray();
		for (InputDescriptionType inputDesc : inputDescs) {
			if(inputDesc.getIdentifier().getStringValue().equals(id))
			{
				return inputDesc;
			}
		}
		return null;
	}
	
}
