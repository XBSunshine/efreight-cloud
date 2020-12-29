package com.efreight.common.security.component;

import com.efreight.common.security.constant.CommonConstants;
import com.efreight.common.security.exception.EftAuth2Exception;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.SneakyThrows;

/**
 * @author zhanghw
 * @date 2018/11/16
 * <p>
 * OAuth2 异常格式化
 */
public class EftAuth2ExceptionSerializer extends StdSerializer<EftAuth2Exception> {
	public EftAuth2ExceptionSerializer() {
		super(EftAuth2Exception.class);
	}

	@Override
	@SneakyThrows
	public void serialize(EftAuth2Exception value, JsonGenerator gen, SerializerProvider provider) {
		gen.writeStartObject();
		gen.writeObjectField("code", CommonConstants.FAIL);
		gen.writeStringField("messagInfo", value.getMessage());
		gen.writeStringField("data", value.getErrorCode());
		gen.writeEndObject();
	}
}
