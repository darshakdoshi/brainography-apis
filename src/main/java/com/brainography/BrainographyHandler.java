package com.brainography;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.brainography.request.Request;
import com.brainography.response.FinalResponse;
import com.brainography.services.Service;

/**
 * @author A SARANG KUMAR TAK
 * @since 07/19/2020
 **/

public class BrainographyHandler implements RequestHandler<Request, FinalResponse> {

    @Override
    public FinalResponse handleRequest(Request input, Context context) {
        LambdaLogger log = context.getLogger();
        return new Service().getResponse(input, log);
    }

}
