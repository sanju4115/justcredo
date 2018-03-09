package com.credolabs.justcredo.easyparcel;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by sanjaykumar on 09/03/18.
 */

public class ParcelDetails implements Serializable {

    private String toAddress;
    private String FromAddress;
    private Long time;
    private String uid;
    private String status;

    public ParcelDetails() {
    }

    public ParcelDetails(String toAddress,String fromAddress, Long time, String uid, String status) {
        this.toAddress = toAddress;
        FromAddress = fromAddress;
        this.time = time;
        this.uid = uid;
        this.status = status;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getFromAddress() {
        return FromAddress;
    }

    public void setFromAddress(String fromAddress) {
        FromAddress = fromAddress;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
