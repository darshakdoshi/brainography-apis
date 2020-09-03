package com.brainography.entity;

public class UploadReport {

    private String clientId;
    private String dealer_client_id;
    private String report;

    public UploadReport() {
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getDealer_client_id() {
        return dealer_client_id;
    }

    public void setDealer_client_id(String dealer_client_id) {
        this.dealer_client_id = dealer_client_id;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }
}
