package com.brainography.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author A SARANG KUMAR TAK
 * @since 07/19/2020
 **/

public class BaseDAO {

  Connection con = null;

  public BaseDAO(Connection con) {}

    protected Connection getConnection() {
        try {
            if(con == null || con.isClosed()) {
             String url = "jdbc:mysql://brainographydb.cse1mvnkc219.us-west-2.rds.amazonaws.com:3306/brainodb";
             String uname = "braino";
             String pass = "brainography";
             con= DriverManager.getConnection(url,uname,pass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }

    public void closeConnection() {
        try {
            if(con !=null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
