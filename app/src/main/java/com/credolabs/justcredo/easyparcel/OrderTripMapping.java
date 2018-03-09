package com.credolabs.justcredo.easyparcel;

import java.io.Serializable;

/**
 * Created by sanjaykumar on 09/03/18.
 */

public class OrderTripMapping implements Serializable {
    private String tripId;
    private String orderId;

    public OrderTripMapping() {
    }

    public OrderTripMapping(String tripId, String orderId) {
        this.tripId = tripId;
        this.orderId = orderId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
