package com.brainography.DAO;


import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.brainography.AwsS3.S3repo;
import com.brainography.Constant;
import com.brainography.entity.Client;
import com.google.common.base.Strings;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author A SARANG KUMAR TAK
 * @since 04/08/2020
 **/

public class AdminDAO extends BaseDAO {

    private S3repo s3repo = new S3repo();

    public AdminDAO(Connection con) {
        super(con);
    }

    public boolean isValidAdmin(String username, String password) throws SQLException {
        boolean result = false;
        PreparedStatement pst = null;
        ResultSet rst = null;
        int i = 0;
        try {
            con = getConnection();
            String sql = "select password from admin where username = ?";
            con = getConnection();
            pst = con.prepareStatement(sql);
            pst.setString(++i, username);
            rst = pst.executeQuery();
            while (rst.next()) {
                result = rst.getString("password").equals(password);
            }
        } finally {
            if (rst != null)
                rst.close();
            if (pst != null)
                pst.close();
        }
        return result;
    }

    public Map<String, Object> pendingClientReport(String limit) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        PreparedStatement pst = null;
        ResultSet rst1 = null;
        ResultSet rst2 = null;
        Client client = new Client();
        int l = 0;
        try {
            l = Integer.parseInt(limit);
            con = getConnection();
            String sql1 = "select dealer_client_id, client_id, name, contact_number, email, saved_date from client";
            String sql2 = "select client_id, report_status from client_report";
            pst = con.prepareStatement(sql2);
            rst2 = pst.executeQuery();
            Map<String, String> status = new LinkedHashMap<>();
            List<Client> clients = new ArrayList<>();
            while (rst2.next()) {
                status.put(rst2.getString("client_id"), rst2.getString("report_status"));
            }
            pst = con.prepareStatement(sql1);
            rst1 = pst.executeQuery();
            int count = 0;
            while (rst1.next()) {
                String clientId = rst1.getString("client_id");
                if (status.containsKey(clientId) && status.get(clientId).equalsIgnoreCase("pending")) {
                    if(count == l){break;}
                    client.setClientId(clientId);
                    client.setDealer_client_id(rst1.getString("dealer_client_id"));
                    client.setClientname(rst1.getString("name"));
                    client.setContact(rst1.getString("contact_number"));
                    client.setEmail(rst1.getString("email"));
                    client.setSaveddate(rst1.getString("saved_date"));
                    client.setReportStatus(status.get(clientId));
                    clients.add(client);
                    client = new Client();
                    ++count;
                }
            }
            result.put("clients", clients);
        } finally {
            if (rst1 != null)
                rst1.close();
            if (rst2 != null)
                rst2.close();
            if (pst != null)
                pst.close();
        }
        return result;
    }

    public Map<String, Object> completedClientReport(String limit) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        PreparedStatement pst = null;
        ResultSet rst1 = null;
        ResultSet rst2 = null;
        Client client = new Client();
        int l  = 0;
        int count = 0;
        try {
            l = Integer.parseInt(limit);
            con = getConnection();
            String sql1 = "select dealer_client_id, client_id, name, contact_number, email, saved_date from client";
            String sql2 = "select client_id, report_status from client_report";
            pst = con.prepareStatement(sql2);
            rst2 = pst.executeQuery();
            Map<String, String> status = new LinkedHashMap<>();
            List<Client> clients = new ArrayList<>();
            while (rst2.next()) {
                status.put(rst2.getString("client_id"), rst2.getString("report_status"));
            }
            pst = con.prepareStatement(sql1);
            rst1 = pst.executeQuery();
            while (rst1.next()) {
                String clientId = rst1.getString("client_id");
                if (status.containsKey(clientId) && status.get(clientId).equalsIgnoreCase("completed")) {
                    if(count == l){break;}
                    client.setClientId(clientId);
                    client.setDealer_client_id(rst1.getString("dealer_client_id"));
                    client.setClientname(rst1.getString("name"));
                    client.setContact(rst1.getString("contact_number"));
                    client.setEmail(rst1.getString("email"));
                    client.setSaveddate(rst1.getString("saved_date"));
                    client.setReportStatus(status.get(clientId));
                    clients.add(client);
                    client = new Client();
                    ++count;
                }

            }
            result.put("clients", clients);
        } finally {
            if (rst1 != null)
                rst1.close();
            if (rst2 != null)
                rst2.close();
            if (pst != null)
                pst.close();
        }
        return result;
    }

    public String uploadReportTos3(String clientId, String base64) throws Exception {
        String fileName = "";
        if (!Strings.isNullOrEmpty(base64)) {
            boolean isBucketExist = false;
            isBucketExist = s3repo.isBucketExist(Constant.S3_CLIENT_BUCKET_NAME);
            if (!isBucketExist) {
                s3repo.createBucket(Constant.S3_CLIENT_BUCKET_NAME);
            }
            String[] block = base64.split(";");
            String contentType = block[0].split(":")[1];
            String realData = block[1].split(",")[1];
            String extension = contentType.split("/")[1];
            byte[] data = DatatypeConverter.parseBase64Binary(realData);
            InputStream is = new ByteArrayInputStream(data);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(data.length);
            metadata.setContentType(contentType);
            metadata.setCacheControl(Constant.CACHE_CONTROL);
            fileName = clientId + "/" + clientId + "." + extension;
            s3repo.uploadDocuments(Constant.S3_CLIENT_BUCKET_NAME, fileName, is, metadata);
        }
        return fileName;
    }

    public Map<String,Object> saveReportPathToDb(String clientId, String dealerClientId, String fileName) throws Exception {
        Map<String, Object> result = new HashMap<>();
        PreparedStatement pst = null;
        int i = 0;
        try {
            con = getConnection();
            String filePath = Constant.S3_CLIENT_BASE_PATH + fileName;
            StringBuilder sql = new StringBuilder("update client_report set report_file_path ='");
            sql.append(filePath+"',report_status = 'completed' where client_id = ? and dealer_client_id = ?");
            pst = con.prepareStatement(sql.toString());
            pst.setString(++i, clientId);
            pst.setString(++i, dealerClientId);
            pst.executeUpdate();
            result.put("clientId", clientId);
            result.put("dealer_client_id", dealerClientId);
            result.put("ReportUploadStatus", Constant.SUCCESS);
        } finally {
           if(pst!=null) pst.close();
        }
        return result;
    }

    public Map<String, Object> getClientDetail(String clientId, String dealerClientId, LambdaLogger log) throws Exception {
        Map<String, Object> result = new HashMap<>();
        PreparedStatement pst = null;
        ResultSet rst = null;
        Client client = new Client();
        int i = 0;
        try {
            con = getConnection();
            String sql = "select * from client where client_id = ? and dealer_client_id = ?";
            pst = con.prepareStatement(sql);
            pst.setString(++i, clientId);
            pst.setString(++i, dealerClientId);
            rst = pst.executeQuery();
            while(rst.next()) {
                client.setClientId(rst.getString("client_id"));
                client.setDealer_client_id(rst.getString("dealer_client_id"));
                client.setClientname(rst.getString("name"));
                client.setGender(rst.getString("gender"));
                client.setDob(rst.getString("date_of_birth"));
                client.setHandiness(rst.getString("handiness"));
                client.setNationality(rst.getString("nationality"));
                client.setOccupation(rst.getString("occupation"));
                client.setContact(rst.getString("contact_number"));
                client.setEmail(rst.getString("email"));
                client.setAddress(rst.getString("address"));
                client.setCity(rst.getString("city"));
                client.setState(rst.getString("state"));
                client.setCountry(rst.getString("country"));
                client.setZipcode(rst.getString("zip"));
                client.setCapturedby(rst.getString("captured_by"));
                client.setUploadstatus(rst.getString("upload_status"));
                client.setAge(rst.getString("age"));
                client.setUploaddate(rst.getString("upload_date"));
                client.setDownloaddate(rst.getString("download_date"));
                client.setDownloadstatus(rst.getString("download_status"));
                client.setSaveddate(rst.getString("saved_date"));
                client.setPartinresearch(rst.getString("part_in_research"));
                client.setReligion(rst.getString("religion"));
                client.setCast(rst.getString("cast"));
                client.setSubcast(rst.getString("sub_cast"));
                client.setBirthplace(rst.getString("birth_place"));
                client.setPhoto(rst.getString("photo"));
                client.setUsercode(rst.getString("user_code"));
                client.setDealerId(rst.getString("dealer_username"));
                client.setClientnumber(rst.getString("client_number"));
                client.setParentname(rst.getString("parent_name"));
                client.setIsArchive(rst.getString("is_archive"));
             }
             result.put("client", client);
        } finally {
             if(pst!=null) pst.close();
             if(rst!=null) rst.close();
        }
        return result;
    }
}


