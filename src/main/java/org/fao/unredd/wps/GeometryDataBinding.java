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

import org.n52.wps.io.data.IComplexData;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Handles a {@link Geometry} so it can be used in WPS client.
 * 
 * @author Oscar Fonts
 */
public class GeometryDataBinding implements IComplexData {

	private static final long serialVersionUID = 1L;
	
	protected transient Geometry geometry;
	
	public GeometryDataBinding(Geometry payload) {
		this.geometry = payload;
	}

	@Override
	public Class<Geometry> getSupportedClass() {
		return Geometry.class;
	}
	
	@Override
	public Geometry getPayload() {
		return geometry;
	}
}
