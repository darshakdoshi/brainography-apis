package com.brainography;


import com.brainography.response.FinalResponse;
import com.brainography.response.Response;
import com.google.gson.Gson;

/**
 * @author A SARANG KUMAR TAK
 * @since 07/19/2020
 **/

public class Utils {

    public static FinalResponse buildResponse(Gson gson, Response response){
         FinalResponse fresponse = new FinalResponse();
         fresponse.setBody(gson.toJson(response));
         fresponse.setStatusCode(200);
         return fresponse;
    }

    public static FinalResponse buildErrorResponse() {
        FinalResponse fresponse = new FinalResponse();
        fresponse.setBody("Wrong resource name");
        fresponse.setStatusCode(500);
        return fresponse;
    }

}
