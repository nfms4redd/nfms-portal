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
