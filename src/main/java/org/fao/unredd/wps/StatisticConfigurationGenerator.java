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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.generator.AbstractGenerator;

/**
 * Serializes a {@link StatisticConfiguration} as XML,
 * so it can be included in a WPS execute request.
 * 
 * Internally uses a JAXB {@Marshaller}.
 * 
 * @author Oscar Fonts
 */
public class StatisticConfigurationGenerator extends AbstractGenerator {

	public StatisticConfigurationGenerator() {
		super();
		supportedIDataTypes.add(StatisticConfigurationDataBinding.class);
	}
	
	/**
	 * Generates a data stream with an XML representation of
	 * {@link StatisticConfiguration}.
	 */
	@Override
	public InputStream generateStream(IData data, String mimeType, String schema)
			throws IOException {
		
		StatisticConfiguration statConf = (StatisticConfiguration)data.getPayload();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		try {
			JAXBContext context = JAXBContext.newInstance(StatisticConfiguration.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.marshal(statConf, out);
		} catch (JAXBException e) {
			throw new IOException("StatsConf invalid XML contents", e);
		}

		return new ByteArrayInputStream(out.toByteArray());
	}

}
