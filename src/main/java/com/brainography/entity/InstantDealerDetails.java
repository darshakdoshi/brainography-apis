package com.brainography.entity;

public class InstantDealerDetails {

    private String dealerId;
    private String pwd;
    private String dealerCode;
    private int availCredit;
    private String dealerStatus;

    public InstantDealerDetails() {
    }

    public String getDealerId() {
        return dealerId;
    }

    public void setDealerId(String dealerId) {
        this.dealerId = dealerId;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getDealerCode() {
        return dealerCode;
    }

    public void setDealerCode(String dealerCode) {
        this.dealerCode = dealerCode;
    }

    public int getAvailCredit() {
        return availCredit;
    }

    public void setAvailCredit(int availCredit) {
        this.availCredit = availCredit;
    }

    public String getDealerStatus() {
        return dealerStatus;
    }

    public void setDealerStatus(String dealerStatus) {
        this.dealerStatus = dealerStatus;
    }
}
