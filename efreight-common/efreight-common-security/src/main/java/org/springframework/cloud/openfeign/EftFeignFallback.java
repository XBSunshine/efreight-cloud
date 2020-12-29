package org.springframework.cloud.openfeign;

import cn.hutool.core.util.StrUtil;

import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SpringContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author zhanghw
 */
@Slf4j
@AllArgsConstructor
public class EftFeignFallback<T> implements MethodInterceptor {
	private final Class<T> targetType;
	private final String targetName;
	private final Throwable cause;

	@Nullable
	@Override
	@SneakyThrows
	public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) {
		Class<?> returnType = method.getReturnType();
		if (MessageInfo.class != returnType) {
			return null;
		}
		FeignException exception = (FeignException) cause;

		byte[] content = exception.content();

		String str = StrUtil.str(content, StandardCharsets.UTF_8);

		log.error("PigxFeignFallback:[{}.{}] serviceId:[{}] message:[{}]", targetType.getName(), method.getName(), targetName, str);
		ObjectMapper objectMapper = SpringContextHolder.getBean(ObjectMapper.class);
		return objectMapper.readValue(str, MessageInfo.class);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		EftFeignFallback<?> that = (EftFeignFallback<?>) o;
		return targetType.equals(that.targetType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(targetType);
	}
}
