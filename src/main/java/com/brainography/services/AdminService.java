package com.brainography.services;


import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.brainography.Constant;
import com.brainography.DAO.AdminDAO;
import com.brainography.DAO.ClientDataDAO;
import com.brainography.Utils;
import com.brainography.entity.AdminLogin;
import com.brainography.entity.Client;
import com.brainography.entity.UploadReport;
import com.brainography.request.Request;
import com.brainography.response.FinalResponse;
import com.brainography.response.Response;
import com.google.common.base.Strings;
import com.google.gson.Gson;

import java.sql.Connection;
import java.util.Map;

/**
 * @author A SARANG KUMAR TAK
 * @since 04/08/2020
 **/

public class AdminService {

    Gson gson = new Gson();

    public AdminService() {
    }

    public FinalResponse getResponse(Request request, LambdaLogger log) {
        String resource = request.getResource();
        if (!Strings.isNullOrEmpty(resource)) {
            switch (resource) {
                case ("/adminlogin"):
                    return adminLogin(request, log);
                case ("/pendingclientreports/{limit}"):
                    return pendingClientReports(request, log);
                case ("/completedclientreports/{limit}"):
                    return completedClientReports(request, log);
                case ("/uploadreport"):
                    return uploadReport(request, log);
                case ("/downloadreports"):
                    return downloadReports(request, log);
                case ("/getclientdetails"):
                    return getClientDetails(request, log);
                default:
                    return Utils.buildErrorResponse();
            }
        }
        return null;
    }

    public FinalResponse adminLogin(Request request, LambdaLogger log) {
        Connection con = null;
        Response response = new Response();
        AdminDAO adminDAO = new AdminDAO(con);
        try {
            AdminLogin adminLogin = gson.fromJson(request.getBody(), AdminLogin.class);
            if (adminDAO.isValidAdmin(adminLogin.getuName(), adminLogin.getuPwd()))
                response.setStatus(Constant.SUCCESS);
            else response.setStatus(Constant.FAILED);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(Constant.FAILED);
        } finally {
            if (adminDAO != null) adminDAO.closeConnection();
        }
        return Utils.buildResponse(gson, response);
    }

    public FinalResponse pendingClientReports(Request request, LambdaLogger log) {
        Connection con = null;
        AdminDAO adminDAO = new AdminDAO(con);
        Response response = new Response();
        try {
            String limit = request.getPath().split("/")[2];
            Map<String, Object> res = adminDAO.pendingClientReport(limit);
            if (!res.isEmpty() && res != null) {
                response.setStatus(Constant.SUCCESS);
                response.setDataMap(res);
            } else {
                response.setStatus(Constant.FAILED);
            }
        } catch (Exception e) {
            response.setStatus(Constant.FAILED);
            e.printStackTrace();
        } finally {
            if (adminDAO != null) adminDAO.closeConnection();
        }
        return Utils.buildResponse(gson, response);
    }

    public FinalResponse completedClientReports(Request request, LambdaLogger log) {
        Connection con = null;
        AdminDAO adminDAO = new AdminDAO(con);
        Response response = new Response();
        try {
            String limit = request.getPath().split("/")[2];
            Map<String, Object> res = adminDAO.completedClientReport(limit);
            if (!res.isEmpty() && res != null) {
                response.setStatus(Constant.SUCCESS);
                response.setDataMap(res);
            } else {
                response.setStatus(Constant.FAILED);
            }
        } catch (Exception e) {
            response.setStatus(Constant.FAILED);
            e.printStackTrace();
        } finally {
            if (adminDAO != null) adminDAO.closeConnection();
        }
        return Utils.buildResponse(gson, response);
    }

    public FinalResponse uploadReport(Request request, LambdaLogger log) {
        Connection con = null;
        AdminDAO adminDAO = new AdminDAO(con);
        Response response = new Response();
        try {
            UploadReport uploadReport = gson.fromJson(request.getBody(), UploadReport.class);
            String fileName = adminDAO.uploadReportTos3(uploadReport.getClientId(), uploadReport.getReport());
            if (!Strings.isNullOrEmpty(fileName)) {
                Map<String, Object> res = adminDAO.saveReportPathToDb(uploadReport.getClientId(), uploadReport.getDealer_client_id(), fileName);
                if (res != null && !res.isEmpty()) {
                    response.setStatus(Constant.SUCCESS);
                    response.setDataMap(res);
                } else {
                    response.setStatus(Constant.FAILED);
                }
            }
        } catch (Exception e) {
            response.setStatus(Constant.FAILED);
            e.printStackTrace();

        } finally {
            if (adminDAO != null) adminDAO.closeConnection();
        }
        return Utils.buildResponse(gson, response);
    }

    public FinalResponse downloadReports(Request request, LambdaLogger log) {
        Connection con = null;
        ClientDataDAO clientDataDAO = new ClientDataDAO(con);
        Response response = new Response();
        try {
            Client client = gson.fromJson(request.getBody(), Client.class);
            Map<String, Object> res = clientDataDAO.getReportFile(client.getDealer_client_id(), log);
            if(res != null && !res.isEmpty()) {
               response.setStatus(Constant.SUCCESS);
               response.setDataMap(res);
            } else {
                response.setStatus(Constant.FAILED);
            }
        }
        catch(Exception e) {
            response.setStatus(Constant.FAILED);
            e.printStackTrace();
        }
        finally {
            if(clientDataDAO !=null)
                clientDataDAO.closeConnection();
        }
        return Utils.buildResponse(gson,response);
    }

   public FinalResponse getClientDetails(Request request, LambdaLogger log) {
        Connection con = null;
        AdminDAO adminDAO = new AdminDAO(con);
        Response response = new Response();
        try {
            Client client = gson.fromJson(request.getBody(), Client.class);
            Map<String,Object> res = adminDAO.getClientDetail(client.getClientId(),client.getDealer_client_id(),log);
            if(res != null && !res.isEmpty()) {
                response.setStatus(Constant.SUCCESS);
                response.setDataMap(res);
            } else {
                response.setStatus(Constant.FAILED);
            }
        }
        catch(Exception e) {
            response.setStatus(Constant.FAILED);
            e.printStackTrace();
        } finally {
            if(adminDAO!=null) adminDAO.closeConnection();
        }
        return Utils.buildResponse(gson,response);
   }

}
