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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fao.unredd.portal.configuration.layer;

import java.util.HashMap;

/**
 *
 * @author sgiaccio
 */
public class WMSLayer implements Layer
{
    private String baseUrl;
    private String wmsName;
    private String imageFormat;
    private boolean visible;
    private boolean queriable;
    private String legend;
    private HashMap<String,String> wmsParameters;
    private String geobatchLayerName;


    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setGeobatchLayerName(String geobatchLayerName) {
        this.geobatchLayerName = geobatchLayerName;
    }

    public void setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
    }

    public void setLegend(String legend) {
        this.legend = legend;
    }

    public void setQueriable(boolean queriable) {
        this.queriable = queriable;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setWmsName(String wmsName) {
        this.wmsName = wmsName;
    }

    public void setWmsParameters(HashMap<String, String> wmsParameters) {
        this.wmsParameters = wmsParameters;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getGeobatchLayerName() {
        return geobatchLayerName;
    }

    public String getImageFormat() {
        return imageFormat;
    }

    public String getLegend() {
        return legend;
    }

    public boolean isQueriable() {
        return queriable;
    }

    public boolean isVisible() {
        return visible;
    }

    public String getWmsName() {
        return wmsName;
    }

    public HashMap<String, String> getWmsParameters() {
        return wmsParameters;
    }
}
