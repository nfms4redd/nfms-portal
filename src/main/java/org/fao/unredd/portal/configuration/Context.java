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
