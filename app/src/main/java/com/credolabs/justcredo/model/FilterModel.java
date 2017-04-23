package com.credolabs.justcredo.model;

import java.io.Serializable;

/**
 * Created by Sanjay kumar on 4/22/2017.
 */

public class FilterModel implements Serializable {

    private String name = null;
    private boolean selected = false;

    public FilterModel(String name, boolean selected) {
        this.name = name;
        this.selected = selected;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
