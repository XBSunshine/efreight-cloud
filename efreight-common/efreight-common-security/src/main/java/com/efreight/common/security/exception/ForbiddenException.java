package com.efreight.common.security.exception;

import com.efreight.common.security.component.EftAuth2ExceptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.http.HttpStatus;

/**
 * @author zhanghw
 */
@JsonSerialize(using = EftAuth2ExceptionSerializer.class)
public class ForbiddenException extends EftAuth2Exception {

	public ForbiddenException(String msg, Throwable t) {
		super(msg);
	}

	@Override
	public String getOAuth2ErrorCode() {
		return "access_denied";
	}

	@Override
	public int getHttpErrorCode() {
		return HttpStatus.FORBIDDEN.value();
	}

}

