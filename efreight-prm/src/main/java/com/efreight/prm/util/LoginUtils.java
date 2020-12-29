package com.efreight.prm.util;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 类名称：DateUtils   
 * 类描述：   
 * 创建人：limr   
 * 创建时间：2016年8月2日 下午5:40:18   
 * 修改人：limr   
 * 修改时间：2016年8月2日 下午5:40:18   
 * 修改备注：   
 * @version    
 *
 */
public class LoginUtils {
	public static final String TAG = "user";

	public static HttpServletRequest getRequest() {
		RequestAttributes ra = RequestContextHolder.getRequestAttributes();
		return ((ServletRequestAttributes)ra).getRequest();
	}
	public static HttpServletResponse getResponse() {
		RequestAttributes ra = RequestContextHolder.getRequestAttributes();
		return ((ServletRequestAttributes)ra).getResponse();
	}
	
	public static HttpSession getSession() {
		return getRequest().getSession();
	}
}
