package com.brainography.entity;

import java.util.List;

/**
 * @author A SARANG KUMAR TAK
 * @since 07/19/2020
 **/

public class FingerList {

   private List<FingerPrint> listOfFingers;
   private String dealerId; //username

    public String getDealerId() {
        return dealerId;
    }

    public void setDealerId(String dealerId) {
        this.dealerId = dealerId;
    }

    public List<FingerPrint> getListOfFingers() {
        return listOfFingers;
    }

    public void setListOfFingers(List<FingerPrint> listOfFingers) {
        this.listOfFingers = listOfFingers;
    }
}
