package com.efreight.common.security.util;

import com.efreight.common.security.constant.CommonConstants;
import lombok.*;

import java.io.Serializable;

/**
 * 
 * @author zhanghw
 *
 * @param <T>
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MessageInfo<T> implements Serializable {

	private static final long serialVersionUID = 7162156649464891381L;
	@Getter
	@Setter
	private int code;
	@Getter
	@Setter
	private String messageInfo;
	@Getter
	@Setter
	private T data;

	public static <T> MessageInfo<T> ok() {
		return restResult(null, CommonConstants.SUCCESS, null);
	}

	public static <T> MessageInfo<T> ok(T data) {
		return restResult(data, CommonConstants.SUCCESS, null);
	}

	public static <T> MessageInfo<T> ok(T data, String msg) {
		return restResult(data, CommonConstants.SUCCESS, msg);
	}

	public static <T> MessageInfo<T> failed() {
		return restResult(null, CommonConstants.FAIL, null);
	}

	public static <T> MessageInfo<T> failed(String msg) {
		return restResult(null, CommonConstants.FAIL, msg);
	}

	public static <T> MessageInfo<T> failed(T data) {
		return restResult(data, CommonConstants.FAIL, null);
	}

	public static <T> MessageInfo<T> failed(T data, String msg) {
		return restResult(data, CommonConstants.FAIL, msg);
	}

	public static <T> MessageInfo<T> restResult(T data, int code, String msg) {
		MessageInfo<T> apiResult = new MessageInfo<>();
		apiResult.setCode(code);
		apiResult.setData(data);
		apiResult.setMessageInfo(msg);
		return apiResult;
	}
}
