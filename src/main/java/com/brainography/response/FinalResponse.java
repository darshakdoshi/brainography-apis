package com.brainography.response;

import java.util.Map;

/**
 * @author A SARANG KUMAR TAK
 * @since 07/19/2020
 **/

public class FinalResponse {

    private String body;
    private Map<String, String> headers;
    private int statusCode;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
