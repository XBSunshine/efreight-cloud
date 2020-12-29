
package com.efreight.common.security.exception;

import com.efreight.common.security.component.EftAuth2ExceptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

/**
 * @author zhanghw
 * 自定义OAuth2Exception
 */
@JsonSerialize(using = EftAuth2ExceptionSerializer.class)
public class EftAuth2Exception extends OAuth2Exception {
	@Getter
	private String errorCode;

	public EftAuth2Exception(String msg) {
		super(msg);
	}

	public EftAuth2Exception(String msg, String errorCode) {
		super(msg);
		this.errorCode = errorCode;
	}
}
