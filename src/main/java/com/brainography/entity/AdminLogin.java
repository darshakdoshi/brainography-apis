package com.brainography.entity;
/**
 * @author A SARANG KUMAR TAK
 * @since 08/14/2020
 **/
public class AdminLogin {

    private String uName;
    private String uPwd;

    public AdminLogin() {
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getuPwd() {
        return uPwd;
    }

    public void setuPwd(String uPwd) {
        this.uPwd = uPwd;
    }
}
