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
package org.fao.unredd.portal.configuration;

import java.util.ArrayList;
import java.util.List;
import org.fao.unredd.portal.configuration.layer.Layer;

/**
 *
 * @author sgiaccio
 */

public class Context {
    private boolean active;
    private String infoFile;
    private List<Layer> layers = new ArrayList<Layer>();

    private String inlineImageUrl;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getInfoFile() {
        return infoFile;
    }

    public void setInfoFile(String infoFile) {
        this.infoFile = infoFile;
    }

    public String getInlineImageUrl() {
        return inlineImageUrl;
    }

    public void setInlineImageUrl(String inlineImageUrl) {
        this.inlineImageUrl = inlineImageUrl;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public void setLayers(ArrayList<Layer> layers) {
        this.layers = layers;
    }
}
