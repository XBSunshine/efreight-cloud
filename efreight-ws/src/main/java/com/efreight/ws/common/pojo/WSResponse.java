package com.efreight.ws.common.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class WSResponse implements Serializable {
    private int code;
    private String message;
    private int businessCode;

    public static WSResponse ok(){
        WSResponse wsResponse = new WSResponse();
        wsResponse.setCode(200);
        wsResponse.setMessage("Success");
        return wsResponse;
    }

    public static WSResponse failed(String message){
        WSResponse wsResponse = new WSResponse();
        wsResponse.setCode(200);
        wsResponse.setMessage("Success");
        return wsResponse;
    }
}
