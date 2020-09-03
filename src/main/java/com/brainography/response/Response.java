package com.brainography.response;

import java.util.Map;

/**
 * @author A SARANG KUMAR TAK
 * @since 07/19/2020
 **/

public class Response {

    private String status;
    private Map<String, Object> dataMap;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }
}
