package com.brainography.entity;

public class ReportStatus {

    private String dealer_client_id;
    private String status;

    public ReportStatus() {}

    public String getDealer_client_id() {
        return dealer_client_id;
    }

    public void setDealer_client_id(String dealer_client_id) {
        this.dealer_client_id = dealer_client_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
