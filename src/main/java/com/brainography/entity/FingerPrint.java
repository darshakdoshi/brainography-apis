package com.brainography.entity;

/**
 * @author A SARANG KUMAR TAK
 * @since 07/19/2020
 **/

public class FingerPrint {

    private String userId;
    private String dealer_client_id;
    private String hand;
    private String fingername;
    private String leftImage;
    private String centerImage;
    private String rightImage;
    private String leftThumbnail;
    private String centerThumbnail;
    private String rightThumbnail;

    public FingerPrint(){}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDealer_client_id() {
        return dealer_client_id;
    }

    public void setDealer_client_id(String dealer_client_id) {
        this.dealer_client_id = dealer_client_id;
    }

    public String getHand() {
        return hand;
    }

    public void setHand(String hand) {
        this.hand = hand;
    }

    public String getFingername() {
        return fingername;
    }

    public void setFingername(String fingername) {
        this.fingername = fingername;
    }

    public String getLeftImage() {
        return leftImage;
    }

    public void setLeftImage(String leftImage) {
        this.leftImage = leftImage;
    }

    public String getCenterImage() {
        return centerImage;
    }

    public void setCenterImage(String centerImage) {
        this.centerImage = centerImage;
    }

    public String getRightImage() {
        return rightImage;
    }

    public void setRightImage(String rightImage) {
        this.rightImage = rightImage;
    }

    public String getLeftThumbnail() {
        return leftThumbnail;
    }

    public void setLeftThumbnail(String leftThumbnail) {
        this.leftThumbnail = leftThumbnail;
    }

    public String getCenterThumbnail() {
        return centerThumbnail;
    }

    public void setCenterThumbnail(String centerThumbnail) {
        this.centerThumbnail = centerThumbnail;
    }

    public String getRightThumbnail() {
        return rightThumbnail;
    }

    public void setRightThumbnail(String rightThumbnail) {
        this.rightThumbnail = rightThumbnail;
    }
}
