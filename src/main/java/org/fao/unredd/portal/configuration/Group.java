package org.fao.unredd.portal.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sgiaccio
 */
public class Group {
    private String label;
    private List items = new ArrayList(); // item can both be a nested group or a context

    public List getItems() {
        return items;
    }

    public void setItems(ArrayList items) {
        this.items = items;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
}
