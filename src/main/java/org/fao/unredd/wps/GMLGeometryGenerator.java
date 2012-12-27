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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.geotools.xml.Encoder;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.generator.AbstractGenerator;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Serializes a JTS {@link Geometry} as GML2 or GML3,
 * so it can be included in a WPS execute request.
 * 
 * Internally uses XML {@link Encoder} from GeoTools xsd.
 * 
 * @author Oscar Fonts
 */
public class GMLGeometryGenerator extends AbstractGenerator {
	
	public GMLGeometryGenerator() {
		super();
		supportedIDataTypes.add(GeometryDataBinding.class);
	}
	
	/**
	 * Generates a data stream with a {@link Geometry} serialized in GML.
	 * 
	 * By default it will generate GML3, except if mimeType or schema
	 * parameters are explicitly set to generate GML2.
	 */
	@Override
	public InputStream generateStream(IData data, String mimeType, String schema)
			throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		if ((mimeType != null && mimeType.contains("gml/2")) || (schema != null && schema.contains("gml/2"))) {
			Encoder enc = new Encoder(new org.geotools.gml2.GMLConfiguration());
			enc.encode((Geometry)data.getPayload(), org.geotools.gml2.GML._Geometry, out);
		} else {
			Encoder enc = new Encoder(new org.geotools.gml3.GMLConfiguration());
			enc.encode((Geometry)data.getPayload(), org.geotools.gml3.GML._Geometry, out);
		}
		return new ByteArrayInputStream(out.toByteArray());
	}
}
