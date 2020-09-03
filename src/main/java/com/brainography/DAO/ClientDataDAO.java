package com.brainography.DAO;


import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.brainography.AwsS3.S3repo;
import com.brainography.Constant;
import com.brainography.entity.Client;
import com.brainography.entity.FingerPrint;
import com.brainography.entity.InstantDealerDetails;
import com.brainography.entity.ReportStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author A SARANG KUMAR TAK
 * @since 07/19/2020
 **/

public class ClientDataDAO extends BaseDAO {
    private S3repo s3repo = new S3repo();

    public ClientDataDAO(Connection con) {
        super(con);
    }

    public int[] saveFingerThumbnails(String clientId, int sequence_client_id, List<FingerPrint> fprint, LambdaLogger log) throws Exception {
        int[] result = null;
        int i = 0;
        PreparedStatement pst = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);
            int fno = 0;
            String sql = "insert into client_data(sequence_client_id,dealer_client_id,hand,finger_name,finger_number, left_thumbnail,center_thumbnail,right_thumbnail) values(?,?,?,?,?,?,?,?)";
            pst = con.prepareStatement(sql);
            log.log("thumbnail array length : " + fprint.size());
            for (int j = 0; j < fprint.size(); ++j) {
                FingerPrint obj = fprint.get(j);
                pst.setInt(++i, sequence_client_id);
                pst.setString(++i, obj.getDealer_client_id());
                pst.setString(++i, obj.getHand());
                pst.setString(++i, obj.getFingername());
                pst.setInt(++i, ++fno);
                s3repo.createFolder(Constant.S3_CLIENT_BUCKET_NAME, clientId + "/" + obj.getHand() + "/" + obj.getFingername());

                String[] leftblock = obj.getLeftThumbnail().split(";");
                String leftcontentType = leftblock[0].split(":")[1];
                String leftrealData = leftblock[1].split(",")[1];
                String leftextension = leftcontentType.split("/")[1];

                byte[] leftdata = DatatypeConverter.parseBase64Binary(leftrealData);
                InputStream leftis = new ByteArrayInputStream(leftdata);
                ObjectMetadata leftmetadata = new ObjectMetadata();
                leftmetadata.setContentLength(leftdata.length);
                leftmetadata.setContentType(leftcontentType);
                leftmetadata.setCacheControl(Constant.CACHE_CONTROL);
                String leftfilename = clientId + "/" + obj.getHand() + "/" + obj.getFingername() + "/" + "leftthumbnail" + "." + leftextension;
                s3repo.uploadDocuments(Constant.S3_CLIENT_BUCKET_NAME, leftfilename, leftis, leftmetadata);
                pst.setString(++i, Constant.S3_CLIENT_BASE_PATH + leftfilename);

                String[] centerblock = obj.getCenterThumbnail().split(";");
                String centercontentType = centerblock[0].split(":")[1];
                String centerrealData = centerblock[1].split(",")[1];
                String centerextension = centercontentType.split("/")[1];

                byte[] centerdata = DatatypeConverter.parseBase64Binary(centerrealData);
                InputStream centeris = new ByteArrayInputStream(centerdata);
                ObjectMetadata centermetadata = new ObjectMetadata();
                centermetadata.setContentLength(centerdata.length);
                centermetadata.setContentType(centercontentType);
                centermetadata.setCacheControl(Constant.CACHE_CONTROL);
                String centerfilename = clientId + "/" + obj.getHand() + "/" + obj.getFingername() + "/" + "centerthumbnail" + "." + centerextension;
                s3repo.uploadDocuments(Constant.S3_CLIENT_BUCKET_NAME, centerfilename, centeris, centermetadata);
                pst.setString(++i, Constant.S3_CLIENT_BASE_PATH + centerfilename);

                String[] rightblock = obj.getRightThumbnail().split(";");
                String rightcontentType = rightblock[0].split(":")[1];
                String rightrealData = rightblock[1].split(",")[1];
                String rightextension = rightcontentType.split("/")[1];

                byte[] rightdata = DatatypeConverter.parseBase64Binary(rightrealData);
                InputStream rightis = new ByteArrayInputStream(rightdata);
                ObjectMetadata rightmetadata = new ObjectMetadata();
                rightmetadata.setContentLength(rightdata.length);
                rightmetadata.setContentType(rightcontentType);
                rightmetadata.setCacheControl(Constant.CACHE_CONTROL);
                String rightfilename = clientId + "/" + obj.getHand() + "/" + obj.getFingername() + "/" + "rightthumbnail" + "." + rightextension;
                s3repo.uploadDocuments(Constant.S3_CLIENT_BUCKET_NAME, rightfilename, rightis, rightmetadata);
                pst.setString(++i, Constant.S3_CLIENT_BASE_PATH + rightfilename);
                log.log("batchNo : " + j);
                if (fno == 5) fno = 0;
                i = 0;
                pst.addBatch();
            }
            log.log("executingBatch");
            result = pst.executeBatch();
            log.log("Batchsavedintodb");
            con.setAutoCommit(true);
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
            }
            if (pst != null)
                pst.close();
        }
        return result;
    }


    public int saveFingerImages(List<FingerPrint> listOfFingers, LambdaLogger log) throws Exception {
        int i = 0;
        int saveImageAndDate = 0;
        PreparedStatement pst = null;
        Connection con = getConnection();
        try {
            con.setAutoCommit(false);
            String sql = "update client_data set left_image = ?, center_image = ?, right_image = ? where dealer_client_id = ? and hand = ? and finger_name = ?";
            String sql2 = "update client set upload_date = ? , upload_status = 'completed' where dealer_client_id = ?";
            pst = con.prepareStatement(sql);
            log.log("Image array length : " + listOfFingers.size());
            String dealer_client_id = listOfFingers.get(0).getDealer_client_id();
            for (int j = 0; j < listOfFingers.size(); ++j) {
                FingerPrint obj = listOfFingers.get(j);
                s3repo.createFolder(Constant.S3_CLIENT_BUCKET_NAME, obj.getUserId() + "/" + obj.getHand() + "/" + obj.getFingername());

                String[] leftblock = obj.getLeftImage().split(";");
                String leftcontentType = leftblock[0].split(":")[1];
                String leftrealData = leftblock[1].split(",")[1];
                String leftextension = leftcontentType.split("/")[1];

                byte[] leftdata = DatatypeConverter.parseBase64Binary(leftrealData);
                InputStream leftis = new ByteArrayInputStream(leftdata);
                ObjectMetadata leftmetadata = new ObjectMetadata();
                leftmetadata.setContentLength(leftdata.length);
                leftmetadata.setContentType(leftcontentType);
                leftmetadata.setCacheControl(Constant.CACHE_CONTROL);
                String leftfilename = obj.getUserId() + "/" + obj.getHand() + "/" + obj.getFingername() + "/" + "leftimg" + "." + leftextension;
                s3repo.uploadDocuments(Constant.S3_CLIENT_BUCKET_NAME, leftfilename, leftis, leftmetadata);
                pst.setString(++i, Constant.S3_CLIENT_BASE_PATH + leftfilename);

                String[] centerblock = obj.getCenterImage().split(";");
                String centercontentType = centerblock[0].split(":")[1];
                String centerrealData = centerblock[1].split(",")[1];
                String centerextension = centercontentType.split("/")[1];

                byte[] centerdata = DatatypeConverter.parseBase64Binary(centerrealData);
                InputStream centeris = new ByteArrayInputStream(centerdata);
                ObjectMetadata centermetadata = new ObjectMetadata();
                centermetadata.setContentLength(centerdata.length);
                centermetadata.setContentType(centercontentType);
                centermetadata.setCacheControl(Constant.CACHE_CONTROL);
                String centerfilename = obj.getUserId() + "/" + obj.getHand() + "/" + obj.getFingername() + "/" + "centerimg" + "." + centerextension;
                s3repo.uploadDocuments(Constant.S3_CLIENT_BUCKET_NAME, centerfilename, centeris, centermetadata);
                pst.setString(++i, Constant.S3_CLIENT_BASE_PATH + centerfilename);

                String[] rightblock = obj.getRightImage().split(";");
                String rightcontentType = rightblock[0].split(":")[1];
                String rightrealData = rightblock[1].split(",")[1];
                String rightextension = rightcontentType.split("/")[1];

                byte[] rightdata = DatatypeConverter.parseBase64Binary(rightrealData);
                InputStream rightis = new ByteArrayInputStream(rightdata);
                ObjectMetadata rightmetadata = new ObjectMetadata();
                rightmetadata.setContentLength(rightdata.length);
                rightmetadata.setContentType(rightcontentType);
                rightmetadata.setCacheControl(Constant.CACHE_CONTROL);
                String rightfilename = obj.getUserId() + "/" + obj.getHand() + "/" + obj.getFingername() + "/" + "rightimg" + "." + rightextension;
                s3repo.uploadDocuments(Constant.S3_CLIENT_BUCKET_NAME, rightfilename, rightis, rightmetadata);
                pst.setString(++i, Constant.S3_CLIENT_BASE_PATH + rightfilename);

                pst.setString(++i, obj.getDealer_client_id());
                pst.setString(++i, obj.getHand());
                pst.setString(++i, obj.getFingername());
                log.log("batchNo : " + j);
                i = 0;
                pst.addBatch();
            }
            log.log("executingBatch");
            pst.executeBatch();
            log.log("Batchsavedintodb");
            con.setAutoCommit(true);
            pst = con.prepareStatement(sql2);
            pst.setString(++i, new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
            pst.setString(++i, dealer_client_id);
            saveImageAndDate = pst.executeUpdate();
        } finally {
            if (con != null) con.setAutoCommit(true);
            if (pst != null) pst.close();
        }
        return saveImageAndDate;
    }

    public int decrementCreditByOne(String latestdealerClientId, LambdaLogger log) throws SQLException {
        PreparedStatement pst = null;
        int i = 0;
        int result = 0;
        try {
            con = getConnection();
            String sql = "update dealer_credit set credit = credit - 1 where latest_dealer_client_id = ?";
            pst = con.prepareStatement(sql);
            pst.setString(++i, latestdealerClientId);
            log.log("subracting credit by 1");
            result = pst.executeUpdate();
            log.log("subracted credit by 1");
        } finally {
            if (pst != null)
                pst.close();
        }
        return result;
    }

    public int dealerCreditHistory(int sequence_dealer_id, String dealer_client_id, String client_id, int sequence_client_id, LambdaLogger log) throws Exception {
        int result = 0;
        int i = 0;
        PreparedStatement pst = null;
        try {
            con = getConnection();
            String sql = "insert into dealer_credit_history(sequence_dealer_id, dealer_client_id, sequence_client_id, client_id) values(?,?,?,?)";
            pst = con.prepareStatement(sql);
            pst.setInt(++i, sequence_dealer_id);
            pst.setString(++i, dealer_client_id);
            pst.setInt(++i, sequence_client_id);
            pst.setString(++i, client_id);
            log.log("Dealer History table update start");
            result = pst.executeUpdate();
            log.log("Dealer table updated");
        } finally {
            if (pst != null) pst.close();
        }
        return result;
    }

    public int updateLatestDealerClientId(String latest_dealer_client_id, int sequence_dealer_id, LambdaLogger log) throws Exception {
        int result = 0;
        PreparedStatement pst = null;
        int i = 0;
        try {
            con = getConnection();
            String sql = "update dealer_credit set latest_dealer_client_id = ? where sequence_dealer_id = ?";
            pst = con.prepareStatement(sql);
            pst.setString(++i, latest_dealer_client_id);
            pst.setInt(++i, sequence_dealer_id);
            log.log("updating dealer credit table");
            result = pst.executeUpdate();
            log.log("updated dealer_credit table");
        } finally {
            if (pst != null) pst.close();
        }
        return result;
    }

    public Map<String, Object> getReportStatus(JsonNode ids, LambdaLogger log) throws Exception {
        Map<String, Object> result = new HashMap<>();
        PreparedStatement pst = null;
        ResultSet rst = null;
        int i = 0;

        List<ReportStatus> res = new ArrayList<>();
        try {
            con = getConnection();
            String sql = "select * from client_report";
            pst = con.prepareStatement(sql);
            rst = pst.executeQuery();
            Map<String, String> idsfromDb = new LinkedHashMap<>();
            while (rst.next()) {
                idsfromDb.put(rst.getString("dealer_client_id"), rst.getString("report_status"));
           }
           if(!idsfromDb.isEmpty() && idsfromDb != null) {
               Iterator<JsonNode> itr = ids.iterator();
               while(itr.hasNext()) {
                   JsonNode element = itr.next();
                   if(element != null) {
                       String idVal = element.get("dealer_client_id").asText();
                       if(idsfromDb.containsKey(idVal)) {
                           ReportStatus reportStatus = new ReportStatus();
                           reportStatus.setDealer_client_id(idVal);
                           reportStatus.setStatus(idsfromDb.get(idVal));
                           res.add(reportStatus);
                       }
                   }
               }
               result.put("reportStatus", res);
           }
        } finally {
            if (pst != null) pst.close();
            if (rst != null) rst.close();
        }
        return result;
    }

    public Map<String, Object> getReportFile(String dealerClientId, LambdaLogger log) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        PreparedStatement pst = null;
        ResultSet rst = null;
        String filePath = null;
        int i = 0;
        try {
            con = getConnection();
            String sql = "select report_status, report_file_path from client_report where dealer_client_id = ?";
            String sql2 = "update client set download_date = ?, download_status = 'completed' where dealer_client_id = ?";
            pst = con.prepareStatement(sql);
            pst.setString(++i, dealerClientId);
            rst = pst.executeQuery();
            while (rst.next()) {
                if (Constant.COMPLETED.equalsIgnoreCase(rst.getString("report_status"))) {
                    filePath = rst.getString("report_file_path");
                    if(!Strings.isNullOrEmpty(filePath)) {
                        String[] key = filePath.split("/");
                        S3ObjectInputStream s3ObjectInputStream = s3repo.getObject(new GetObjectRequest(Constant.S3_CLIENT_BUCKET_NAME, key[3] + "/" + key[4]));
                        if (s3ObjectInputStream != null) {
                            byte[] bytes = IOUtils.toByteArray(s3ObjectInputStream);
                            String base64Encoded = Base64.getEncoder().encodeToString(bytes);
                            result.put("reportFile", base64Encoded);
                            result.put("fileName", key[4]);
                        }
                    } else {
                        result.put("Warning", "File path not found, check for report upload before download");
                    }
                } else {
                    result.put("reportStatus", "Report not completed");
                    break;
                }
            }
            if(!Strings.isNullOrEmpty(filePath)) {
                pst = con.prepareStatement(sql2);
                pst.setString(i, new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
                pst.setString(++i, dealerClientId);
                pst.executeUpdate();
            }
        } finally {
            if (pst != null) pst.close();
            if (rst != null) rst.close();
        }
        return result;
    }

    public int updateArchive(String dealerClientId, LambdaLogger log) throws Exception {
        PreparedStatement pst = null;
        int i = 0;
        int update = 0;
        try {
            con = getConnection();
            String sql = "update client set is_archive = 'Yes' where dealer_client_id = ?";
            pst = con.prepareStatement(sql);
            pst.setString(++i, dealerClientId);
            update = pst.executeUpdate();
        } finally {
            if (pst != null) pst.close();
        }
        return update;
    }

    public Map<String, Object> syncDetails(String username, LambdaLogger log) throws Exception {
        Map<String, Object> result = new HashMap<>();
        PreparedStatement pst = null;
        ResultSet rst = null;
        int i = 0;
        int sequence_dealer_id = 0;
        InstantDealerDetails instantDealerDetails = new InstantDealerDetails();
        List<Client> listClients = new ArrayList<Client>();
        try {
            con = getConnection();
            String sqld = "select dealer.sequence_dealer_id, dealer.username, dealer.password, dealer.code, dealer.status, dealer_credit.credit from dealer join dealer_credit on dealer_credit.sequence_dealer_id = (select sequence_dealer_id from dealer where username = ?)";
            String sqlc = "select * from client where sequence_dealer_id = ?";
            StringBuilder output = new StringBuilder();
            pst = con.prepareStatement(sqld);
            pst.setString(++i, username);
            rst = pst.executeQuery();
            log.log("getting dealer details");
            while (rst.next()) {
                sequence_dealer_id = rst.getInt(rst.getInt("dealer.sequence_dealer_id"));
                log.log("sequence_dealer_id " + sequence_dealer_id);
                instantDealerDetails.setDealerId(rst.getString("dealer.username"));
                instantDealerDetails.setPwd(rst.getString("dealer.password"));
                instantDealerDetails.setDealerCode(rst.getString("dealer.code"));
                instantDealerDetails.setAvailCredit(rst.getInt("dealer_credit.credit"));
                instantDealerDetails.setDealerStatus(rst.getString("dealer.status"));
                result.put("Dealer", instantDealerDetails);
            }
            log.log("got dealer details");
            pst = con.prepareStatement(sqlc);
            pst.setInt(i, sequence_dealer_id);
            rst = pst.executeQuery();
            log.log("getting client details");
            while (rst.next()) {
                Client client = new Client();
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
                client.setPhoto(!Strings.isNullOrEmpty(rst.getString("photo")) ? getS3Object(rst.getString("photo").split("/")) : null);
                client.setUsercode(rst.getString("user_code"));
                client.setDealerId(rst.getString("dealer_username"));
                client.setClientnumber(rst.getString("client_number"));
                client.setParentname(rst.getString("parent_name"));
                client.setIsArchive(rst.getString("is_archive"));
                listClients.add(client);
            }
            log.log("got client details");
            result.put("clients", listClients);
        } finally {
            if (pst != null) pst.close();
            if (rst != null) rst.close();
        }
        return result;
    }

    private String getS3Object(String[] key) throws Exception {
        String base64Encoded = null;
        S3ObjectInputStream s3ObjectInputStream = s3repo.getObject(new GetObjectRequest(Constant.S3_CLIENT_BUCKET_NAME,key[3] + "/" + key[4] + "/" + key[5]));
        if (s3ObjectInputStream != null) {
            byte[] bytes = IOUtils.toByteArray(s3ObjectInputStream);
            base64Encoded = Base64.getEncoder().encodeToString(bytes);
        }
        return base64Encoded;
    }
}




