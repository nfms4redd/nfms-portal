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
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.fao.test.FunctionalTestSuite;
import org.geotools.xml.Parser;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.n52.wps.client.WPSClientException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Tests the {@link WPSProcess} class.
 * 
 * Needs a local GeoServer 2.2 instance with the WPS extension installed.
 * 
 * @author Oscar Fonts
 */
@Category(FunctionalTestSuite.class)
public class TestWPSProcess extends TestCase {
	
	static final String WPS_URL = "http://localhost:8080/geoserver/wps";

	static final String BUFFER_PROCESS_ID = "JTS:buffer";
	static final String BUFFER_INPUT_GEOMETRY_NAME = "geom";
	static final String BUFFER_INPUT_GEOMETRY_VALUE = "POINT(0 0)";
	static final String BUFFER_INPUT_DISTANCE_NAME = "distance";
	static final String BUFFER_INPUT_DISTANCE_VALUE = "0.01";
	static final String BUFFER_EXPECTED_RESPONSE_VALUE = "POLYGON ((0.01 0, 0.0098078528040323 -0.0019509032201613, 0.0092387953251129 -0.0038268343236509, 0.0083146961230255 -0.005555702330196, 0.0070710678118655 -0.0070710678118655, 0.005555702330196 -0.0083146961230255, 0.0038268343236509 -0.0092387953251129, 0.0019509032201613 -0.0098078528040323, 0 -0.01, -0.0019509032201613 -0.0098078528040323, -0.0038268343236509 -0.0092387953251129, -0.005555702330196 -0.0083146961230255, -0.0070710678118655 -0.0070710678118655, -0.0083146961230255 -0.005555702330196, -0.0092387953251129 -0.0038268343236509, -0.0098078528040323 -0.0019509032201613, -0.01 0, -0.0098078528040323 0.0019509032201613, -0.0092387953251129 0.0038268343236509, -0.0083146961230254 0.005555702330196, -0.0070710678118655 0.0070710678118655, -0.005555702330196 0.0083146961230255, -0.0038268343236509 0.0092387953251129, -0.0019509032201613 0.0098078528040323, 0 0.01, 0.0019509032201613 0.0098078528040323, 0.0038268343236509 0.0092387953251129, 0.005555702330196 0.0083146961230254, 0.0070710678118655 0.0070710678118655, 0.0083146961230255 0.005555702330196, 0.0092387953251129 0.0038268343236509, 0.0098078528040323 0.0019509032201612, 0.01 0))";

	/**
	 * Calls the remote process JTS:buffer with test data and checks result.
	 * 
	 * @throws ParseException if the input test geometry has no valid WKT syntax.
	 * @throws WPSClientException if process execution failed.
	 */
	@Test
	public void testBuffer() throws Exception {
		WKTReader wktReader = new WKTReader();

		// Instantiate process; implies sending a describeProcess to the WPS service
		WPSProcess process;
		try {
			process = new WPSProcess(WPS_URL, BUFFER_PROCESS_ID);
		} catch (WPSClientException e) {
			System.out.println(this.getClass().getName() + " testBuffer not run:");
			System.out.println(e.getMessage());
			fail();
			return;
		}
		
		// Fill input parameters
		Map<String, Object> inputs = new TreeMap<String, Object>();
		inputs.put(BUFFER_INPUT_GEOMETRY_NAME, wktReader.read(BUFFER_INPUT_GEOMETRY_VALUE));
		inputs.put(BUFFER_INPUT_DISTANCE_NAME, BUFFER_INPUT_DISTANCE_VALUE);

		// Execute
		String response = process.execute(inputs, String.class);
		System.out.print(response);
		
		// Parse
		Parser par = new Parser(new org.geotools.gml3.GMLConfiguration());
		InputStream is = new ByteArrayInputStream(response.getBytes());
		Geometry geom = (Geometry)par.parse(is);

		// Compare results
		assertEquals(BUFFER_EXPECTED_RESPONSE_VALUE, geom.toString());
	}
}
