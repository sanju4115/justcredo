package com.credolabs.justcredo.model;

/**
 * Created by Sanjay kumar on 3/30/2017.
 */

public class HistorySearchedModel {
    String mainText;
    String secText;

    public HistorySearchedModel(String mainText, String secText) {
        this.mainText = mainText;
        this.secText = secText;
    }

    public HistorySearchedModel() {
    }

    public String getMainText() {
        return mainText;
    }

    public void setMainText(String mainText) {
        this.mainText = mainText;
    }

    public String getSecText() {
        return secText;
    }

    public void setSecText(String secText) {
        this.secText = secText;
    }
}
