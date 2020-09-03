package com.brainography.services;


import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.brainography.Constant;
import com.brainography.DAO.ClientDataDAO;
import com.brainography.DAO.UserDAO;
import com.brainography.Utils;
import com.brainography.entity.Client;
import com.brainography.entity.DealerLogin;
import com.brainography.entity.FingerList;
import com.brainography.request.Request;
import com.brainography.response.FinalResponse;
import com.brainography.response.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.gson.Gson;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author A SARANG KUMAR TAK
 * @since 07/19/2020
 **/

public class Service {

    Gson gson = new Gson();

    public Service() {}

    public FinalResponse getResponse(Request request, LambdaLogger log) {
        String resource = request.getResource();
        if (!Strings.isNullOrEmpty(resource)) {
            switch (resource) {
                case ("/dealerlogin"):
                    return dealerLogin(request, log);
                case ("/getcreditbydealerid"):
                    return getCreditByDealerId(request, log);
                case ("/addclient"):
                    return addClient(request, log);
                case ("/addfingerprints"):
                    return addFingerPrints(request, log);
                case ("/getreportstatus"):
                    return getReportStatus(request, log);
                case ("/downloadreport"):
                    return downloadReport(request, log);
                case ("/sync"):
                    return sync(request, log);
                default:
                    return Utils.buildErrorResponse();
            }
        }
        return null;
    }

    public FinalResponse dealerLogin(Request request, LambdaLogger log) {
        Connection con = null;
        UserDAO userDAO = new UserDAO(con);
        Response response = new Response();
        try {
            DealerLogin d = gson.fromJson(request.getBody(), DealerLogin.class);
            if (userDAO.isValidDealer(d.getDealerId(), d.getPwd())) {
                response.setStatus(Constant.SUCCESS);
                Map<String, Object> res = userDAO.getDealerCodeByID(d);
                response.setDataMap(res);
            } else {
                response.setStatus(Constant.FAILED);
            }
        } catch (Exception e) {
            response.setStatus(Constant.FAILED);
            e.printStackTrace();
        } finally {
               userDAO.closeConnection();
        }
        return Utils.buildResponse(gson, response);
    }

    public FinalResponse getCreditByDealerId(Request request, LambdaLogger log) {
        Connection con = null;
        UserDAO userDAO = new UserDAO(con);
        Response response = new Response();
        try {
            DealerLogin dealerLogin = gson.fromJson(request.getBody(), DealerLogin.class);
            Map<String, Object> res = userDAO.getDealerCreditById(dealerLogin.getDealerId(), log);
            response.setDataMap(res);
            response.setStatus(Constant.SUCCESS);
        } catch (Exception e) {
            response.setStatus(Constant.FAILED);
            e.printStackTrace();
        } finally {
               userDAO.closeConnection();
        }
        return Utils.buildResponse(gson, response);
    }

    public FinalResponse addClient(Request request, LambdaLogger log) {
        Connection con = null;
        UserDAO userDAO = new UserDAO(con);
        ClientDataDAO clientDataDAO = new ClientDataDAO(con);
        Response response = new Response();
        log.log("client class got de-serialized");
        try {
            Client client = gson.fromJson(request.getBody(), Client.class);
            int sequence_client_id = userDAO.addClient(client, log);
            int sequence_dealer_id = userDAO.updateSequenceDealerId(client.getDealerId(), sequence_client_id, log);
            if (sequence_client_id > 0 && sequence_dealer_id > 0) {
                Map<String, Object> res = new HashMap<>();
                int[] thumbnails = clientDataDAO.saveFingerThumbnails(client.getClientId(), sequence_client_id, client.getThumbnails(), log);
                int insert_into_history = clientDataDAO.dealerCreditHistory(sequence_dealer_id, client.getDealer_client_id(), client.getClientId(), sequence_client_id, log);
                int update_latest_dealer = clientDataDAO.updateLatestDealerClientId(client.getDealer_client_id(), sequence_dealer_id, log);
                if (insert_into_history > 0 && update_latest_dealer > 0 && thumbnails.length > 0) {
                    res.put("userId", client.getClientId());
                    response.setStatus(Constant.SUCCESS);
                    response.setDataMap(res);
                } else {
                    response.setStatus(Constant.FAILED);
                    log.log("No history table, thumbnail, latest dealer updates");
                }
            } else {
                response.setStatus(Constant.FAILED);
                log.log("No sequence_client_id generated");
            }
        } catch (Exception e) {
            response.setStatus(Constant.FAILED);
            e.printStackTrace();
        } finally {
               userDAO.closeConnection();
               clientDataDAO.closeConnection();
        }
        return Utils.buildResponse(gson, response);
    }

    public FinalResponse addFingerPrints(Request request, LambdaLogger log) {
        Connection con = null;
        ClientDataDAO clientDataDAO = new ClientDataDAO(con);
        UserDAO userDAO = new UserDAO(con);
        Response response = new Response();
        try {
            FingerList fingerList = gson.fromJson(request.getBody(), FingerList.class);
            int result = clientDataDAO.saveFingerImages(fingerList.getListOfFingers(), log);
            if (result > 0) {
                int dec = clientDataDAO.decrementCreditByOne(fingerList.getListOfFingers().get(0).getDealer_client_id(), log); //username
                if (dec > 0) {
                    Map<String, Object> res = new HashMap<>();
                    res.put("availCredit", userDAO.getDealerCreditById(fingerList.getDealerId(), log));
                    response.setStatus(Constant.SUCCESS);
                    response.setDataMap(res);
                } else {
                    response.setStatus(Constant.FAILED);
                    log.log("Problem in credit reduction");
                }
            } else {
                response.setStatus(Constant.FAILED);
                log.log("Problem in adding image");
            }
        } catch (Exception e) {
            response.setStatus(Constant.FAILED);
            e.printStackTrace();
        } finally {
               userDAO.closeConnection();
               clientDataDAO.closeConnection();
        }
        return Utils.buildResponse(gson, response);
    }

    public FinalResponse getReportStatus(Request request, LambdaLogger log) {
        Connection con = null;
        ClientDataDAO clientDataDAO = new ClientDataDAO(con);
        Response response = new Response();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode parentNode = mapper.readValue(request.getBody(), JsonNode.class);
            Map<String, Object> res = clientDataDAO.getReportStatus(parentNode.get("dealerClientIds"), log);
            if (res != null && !res.isEmpty()) {
                response.setStatus(Constant.SUCCESS);
                response.setDataMap(res);
            } else {
                response.setStatus(Constant.FAILED);
            }
        } catch (Exception e) {
            response.setStatus(Constant.FAILED);
            e.printStackTrace();
        } finally {
               clientDataDAO.closeConnection();
        }
        return Utils.buildResponse(gson, response);
    }

    public FinalResponse downloadReport(Request request, LambdaLogger log) {
        Connection con = null;
        ClientDataDAO clientDataDAO = new ClientDataDAO(con);
        Response response = new Response();
        try {
            Client client = gson.fromJson(request.getBody(), Client.class);
            Map<String, Object> res = clientDataDAO.getReportFile(client.getDealer_client_id(), log);
            if (res != null && !res.isEmpty()) {
                int updateStatus = clientDataDAO.updateArchive(client.getDealer_client_id(), log);
                if (updateStatus > 0) {
                    response.setStatus(Constant.SUCCESS);
                    response.setDataMap(res);
                }
            } else {
                response.setStatus(Constant.FAILED);
            }
        } catch (Exception e) {
            response.setStatus(Constant.FAILED);
            e.printStackTrace();
        } finally {
               clientDataDAO.closeConnection();
        }
        return Utils.buildResponse(gson, response);
    }

    public FinalResponse sync(Request request, LambdaLogger log) {
        Connection con = null;
        ClientDataDAO clientDataDAO = new ClientDataDAO(con);
        Response response = new Response();
        try {
            DealerLogin dealerLogin = gson.fromJson(request.getBody(), DealerLogin.class);
            Map<String, Object> res = clientDataDAO.syncDetails(dealerLogin.getDealerId(), log);
            if (res != null && !res.isEmpty()) {
                response.setStatus(Constant.SUCCESS);
                response.setDataMap(res);
            } else {
                response.setStatus(Constant.FAILED);
            }
        } catch (Exception e) {
            response.setStatus(Constant.FAILED);
            e.printStackTrace();
        } finally {
               clientDataDAO.closeConnection();
        }
        return Utils.buildResponse(gson, response);
    }


}
