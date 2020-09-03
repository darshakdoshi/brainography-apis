package com.brainography.DAO;


import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.brainography.AwsS3.S3repo;
import com.brainography.Constant;
import com.brainography.entity.Client;
import com.brainography.entity.DealerLogin;
import com.google.common.base.Strings;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * @author A SARANG KUMAR TAK
 * @since 07/19/2020
 **/

public class UserDAO extends BaseDAO {
    private S3repo s3repo = new S3repo();

    public UserDAO(Connection con) {
        super(con);
    }

    public boolean isValidDealer(String username, String pwd) throws Exception {
        boolean result = false;
        PreparedStatement pst = null;
        ResultSet rst = null;
        int index = 0;
        try {
            String sql = "select password from dealer where username = ?";
            con = getConnection();
            pst = con.prepareStatement(sql);
            pst.setString(++index, username);
            rst = pst.executeQuery();
            while (rst.next()) {
                result = rst.getString("password").equals(pwd);
            }

        } finally {
            if (rst != null)
                rst.close();
            if (pst != null)
                pst.close();
        }
        return result;
    }

    public Map<String, Object> getDealerCodeByID(DealerLogin d) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        PreparedStatement pst = null;
        ResultSet rst = null;
        int i = 0;
        try {
            String sql = "select username,code from dealer where username = ?";
            con = getConnection();
            pst = con.prepareStatement(sql);
            pst.setString(++i, d.getDealerId());
            rst = pst.executeQuery();
            while (rst.next()) {
                result.put("dealerId", rst.getString("username"));
                result.put("dealerCode", rst.getString("code"));
            }
        } finally {
            if (rst != null)
                rst.close();
            if (pst != null)
                pst.close();
        }
        return result;
    }

    public Map<String, Object> getDealerCreditById(String username, LambdaLogger log) throws Exception {
        PreparedStatement pst = null;
        ResultSet rst = null;
        int i = 0;
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            String sql = "select dealer.status, dealer_credit.credit from dealer join dealer_credit on dealer.username = ?";
            con = getConnection();
            pst = con.prepareStatement(sql);
            pst.setString(++i, username);
            log.log("Getting credit");
            rst = pst.executeQuery();
            while (rst.next()) {
                result.put("availCredit", rst.getInt("dealer_credit.credit"));
                result.put("dealerStatus", rst.getString("dealer.status"));
                log.log("Got credit");
                break;
            }
        } finally {
            if (rst != null)
                rst.close();
            if (pst != null)
                pst.close();
        }
        return result;
    }

    public int addClient(Client client, LambdaLogger log) throws Exception {
        PreparedStatement pst = null;
        ResultSet rst = null;
        int sequence_client_id = 0;
        try {
            int i = 0;
            StringBuilder sql = new StringBuilder("insert into client (dealer_client_id,name,gender,date_of_birth,handiness,nationality,occupation,contact_number,email,address,city,state,country,zip,captured_by, ");
            sql.append("age,saved_date,part_in_research,religion,cast,sub_cast,birth_place,dealer_username,parent_name)");
            sql.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            String sql2 = "update client set client_id = ? where sequence_client_id = ?";
            String sql3 = "update client set photo = ? where sequence_client_id = ?";
            con = getConnection();

            pst = con.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
            pst.setString(++i, client.getDealer_client_id());
            pst.setString(++i, client.getClientname());
            pst.setString(++i, client.getGender());
            pst.setString(++i, client.getDob());
            pst.setString(++i, client.getHandiness());
            pst.setString(++i, client.getNationality());
            pst.setString(++i, client.getOccupation());
            pst.setString(++i, client.getContact());
            pst.setString(++i, client.getEmail());
            pst.setString(++i, client.getAddress());
            pst.setString(++i, client.getCity());
            pst.setString(++i, client.getState());
            pst.setString(++i, client.getCountry());
            pst.setString(++i, client.getZipcode());
            pst.setString(++i, client.getCapturedby());
            pst.setString(++i, client.getAge());
            pst.setString(++i, client.getSaveddate());
            pst.setString(++i, client.getPartinresearch());
            pst.setString(++i, client.getReligion());
            pst.setString(++i, client.getCast());
            pst.setString(++i, client.getSubcast());
            pst.setString(++i, client.getBirthplace());
            pst.setString(++i, client.getDealerId());
            pst.setString(++i, client.getParentname());
            pst.executeUpdate();
            rst = pst.getGeneratedKeys();
            while (rst.next()) {
                sequence_client_id = rst.getInt(1);
            }
            log.log("sequence_client_id : " + sequence_client_id);

            i = 0;
            pst = con.prepareStatement(sql2);
            final String cal_client_id = "BOG" + String.format("%04d", sequence_client_id);
            log.log("client_id : " + cal_client_id);
            client.setClientId(cal_client_id);
            pst.setString(++i, cal_client_id);
            pst.setInt(++i, sequence_client_id);
            pst.executeUpdate();

            if (!Strings.isNullOrEmpty(client.getClientphoto())) {
                i = 0;
                String filename = uploadPhotoTos3(client, log);
                pst = con.prepareStatement(sql3);
                pst.setString(++i, Constant.S3_CLIENT_BASE_PATH + filename);
                pst.setInt(++i, sequence_client_id);
                pst.executeUpdate();
            }

        } finally {
            if (pst != null) pst.close();
            if (rst != null) rst.close();
        }
        return sequence_client_id;
    }

    private String uploadPhotoTos3(Client client, LambdaLogger log) throws Exception {
        String fileName = "";
        boolean isBucketExist = false;
        isBucketExist = s3repo.isBucketExist(Constant.S3_CLIENT_BUCKET_NAME);
        if (!isBucketExist) {
            s3repo.createBucket(Constant.S3_CLIENT_BUCKET_NAME);
        }
        s3repo.createFolder(Constant.S3_CLIENT_BUCKET_NAME, client.getClientId()); //folder name BOG00009
        s3repo.createFolder(Constant.S3_CLIENT_BUCKET_NAME, client.getClientId() + "/" + "Profile");
        s3repo.createFolder(Constant.S3_CLIENT_BUCKET_NAME, client.getClientId() + "/" + "LeftHand");
        s3repo.createFolder(Constant.S3_CLIENT_BUCKET_NAME, client.getClientId() + "/" + "RightHand");
        String[] block = client.getClientphoto().split(";");
        String contentType = block[0].split(":")[1];
        String realData = block[1].split(",")[1];
        String extension = contentType.split("/")[1];

        byte[] data = DatatypeConverter.parseBase64Binary(realData);
        InputStream is = new ByteArrayInputStream(data);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(data.length);
        metadata.setContentType(contentType);
        metadata.setCacheControl(Constant.CACHE_CONTROL);
        fileName = client.getClientId() + "/" + "Profile" + "/" + "profile" + "." + extension;
        s3repo.uploadDocuments(Constant.S3_CLIENT_BUCKET_NAME, fileName, is, metadata);
        log.log("Photo fileName : " + fileName);

        return fileName;
    }

    public int updateSequenceDealerId(String username, int sequence_client_id, LambdaLogger log) throws Exception {
        PreparedStatement pst = null;
        ResultSet rst = null;
        int sequence_dealer_id = 0;
        int i = 0;
        try {
            con = getConnection();
            String sql1 = "select sequence_dealer_id from dealer where username = ?";
            String sql2 = "update client set sequence_dealer_id = ? where sequence_client_id = ?";
            pst = con.prepareStatement(sql1);
            pst.setString(++i, username);
            rst = pst.executeQuery();
            while (rst.next()) {
                sequence_dealer_id = rst.getInt("sequence_dealer_id");
            }
            pst = con.prepareStatement(sql2);
            pst.setInt(i, sequence_dealer_id);
            pst.setInt(++i, sequence_client_id);
            pst.executeUpdate();
        } finally {
            if(pst!=null) pst.close();
            if(rst!=null) rst.close();
        }
        return sequence_dealer_id;
    }

}


