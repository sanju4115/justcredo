package com.credolabs.justcredo.easyparcel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sanjaykumar on 09/03/18.
 */

public class Trip implements Serializable {
    private String from;
    private ArrayList<String> ppIds;
    private String time;
    private String to;
    private String key;

    public Trip(String from, ArrayList<String> ppids, String time, String to) {
        this.from = from;
        this.ppIds = ppids;
        this.time = time;
        this.to = to;
    }

    public Trip() {
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public ArrayList<String> getPpids() {
        return ppIds;
    }

    public void setPpids(ArrayList<String> ppids) {
        this.ppIds = ppids;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
