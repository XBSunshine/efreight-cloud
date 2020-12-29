package com.efreight.ws.common.pojo;

import lombok.Data;

@Data
public class WSException extends RuntimeException{
    /**
     * 成功：200
     * 失败：500
     * 异常状态码(6位)(2前两位模块标识，后四位业务状态标识
     *  通用状态码以10开始
     *  10 0000  认证相关错误
     *  10 0001  没有权限标识
     *  10 0002  没有访问权限
     *  10 1000  程序通用错误
     *
     *  AF模块以20开头
     *  20 1000 订单相关
     *  20 1001 ~ 20 1020  订单-创建服务
     *  20 1021 ~ 20 1030  订单-出重服务
     *  20 1031 ~ 20 1050  订单-编辑订单服务
     *  20 1051 ~ 20 1060  订单-编辑出重服务
     *
     */
    private int code;
    private String message;

    public WSException(){}

    public WSException(int code, String message){
        super(message);
        this.code = code;
        this.message = message;
    }

    public static WSException argEx(String message){
        WSException wsException = new WSException();
        wsException.setCode(101001);
        wsException.setMessage(message);
        return wsException;
    }

    public static WSException noAuthor(){
        WSException wsException = new WSException();
        wsException.setCode(100000);
        wsException.setMessage("Unauthorized");
        return wsException;
    }

    public static WSException noDefinePermission(){
        WSException wsException = new WSException();
        wsException.setCode(100001);
        wsException.setMessage("No Define Permission");
        return wsException;
    }

    public static WSException noPermission(){
        WSException wsException = new WSException();
        wsException.setCode(100002);
        wsException.setMessage("No Permission");
        return wsException;
    }

    public static WSException multiAuth(){
        WSException wsException = new WSException();
        wsException.setCode(100003);
        wsException.setMessage("Multiple Authorization Information");
        return wsException;
    }
}
