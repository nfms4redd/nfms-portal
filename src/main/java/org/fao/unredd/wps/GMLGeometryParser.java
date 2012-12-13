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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.xml.Parser;
import org.n52.wps.io.datahandler.parser.AbstractParser;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Builds a JTS {@link Geometry} from GML2 or GML3 fragments.
 * Used to read geometries in WPS responses.
 * 
 * Internally uses XML {@link Parser} from GeoTools xsd.
 * 
 * @author Oscar Fonts
 */
public class GMLGeometryParser extends AbstractParser {

	Parser par2 = null;
	Parser par3 = null;
	
	public GMLGeometryParser() {
		super();
		supportedIDataTypes.add(GeometryDataBinding.class);
	}

	/**
	 * Generates a {@link Geometry} from a GML stream.
	 * 
	 * By default it will parse stream as GML3, unless mimeType or schema
	 * parameters indicate explicitly to use GML2 parsing.
	 */
	@Override
	public GeometryDataBinding parse(InputStream input, String mimeType, String schema) {
		Geometry geom = null;
		try {
			if ((mimeType != null && mimeType.contains("gml/2")) || (schema != null && schema.contains("gml/2"))) {
				geom = (Geometry)getGML2Parser().parse(input);
			} else {
				geom = (Geometry)getGML3Parser().parse(input);
			}
			return new GeometryDataBinding(geom);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("GML not recognized as a Geometry", e);
		} catch (ParserConfigurationException e) {
			throw new IllegalArgumentException("Error while configuring GML parser", e);
		} catch(SAXException e) {
			throw new IllegalArgumentException("Error while parsing GML", e);
		} catch(IOException e) {
			throw new IllegalArgumentException("Error transfering GML", e);
		}
	}

	Parser getGML2Parser() {
		if (par2 == null) {
			par2 = new Parser(new org.geotools.gml2.GMLConfiguration());
		}
		return par2;
	}
	
	Parser getGML3Parser() {
		if (par3 == null) {
			par3 = new Parser(new org.geotools.gml3.GMLConfiguration());
		}
		return par3;
	}
	
}
