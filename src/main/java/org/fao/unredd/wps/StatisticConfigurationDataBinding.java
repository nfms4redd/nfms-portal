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

import org.n52.wps.io.data.IData;

/**
 * Handles a {@link StatisticConfiguration} so it can be used in WPS client.
 * 
 * @author Oscar Fonts
 */
public class StatisticConfigurationDataBinding implements IData {

	private static final long serialVersionUID = 1L;

	protected transient StatisticConfiguration statConf;
	
	public StatisticConfigurationDataBinding(StatisticConfiguration payload) {
		this.statConf = payload;
	}
	
	@Override
	public Object getPayload() {
		return statConf;
	}

	@Override
	public Class<StatisticConfiguration> getSupportedClass() {
		return StatisticConfiguration.class;
	}

}
