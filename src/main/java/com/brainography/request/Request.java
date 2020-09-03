package com.brainography.request;
/**
 * @author A SARANG KUMAR TAK
 * @since 07/19/2020
 **/

public class Request {

    private String body;
    private String resource;
    private String path;

    public Request(){}

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
