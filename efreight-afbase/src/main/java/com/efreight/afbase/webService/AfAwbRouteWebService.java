package com.efreight.afbase.webService;

//import java.util.List;

import javax.jws.WebMethod;
//import javax.jws.WebParam;
//import javax.jws.WebResult;
import javax.jws.WebService;

//import com.efreight.afbase.entity.AfAwbRoute;


/**
 * WebService接口 用于对接运单追踪 
 * @author caiwd
 * @创建日期 2020/2/27
 */
@WebService(name = "AfAwbRouteWebService", 
targetNamespace = "http://WebService.afbase.efreight.com") 
public interface AfAwbRouteWebService {
	@WebMethod
    //@WebResult(name = "AfAwbRoute",targetNamespace = "")
	//@WebParam(name = "AwbNumber") 
	public String queryAfAwbRoute(String xml);
	
	@WebMethod
	public String trackReceipt(String xml);

}
