package com.efreight.common.core.aspect;

import com.efreight.common.core.annotation.ResponseResult;
import com.efreight.common.core.constant.Constant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Component
@Aspect
public class ResponseResultAspect {
    @SneakyThrows
    @Around("@annotation(responseResult)||@within(responseResult)")
    public Object setIfResponseResult(ProceedingJoinPoint point, ResponseResult responseResult) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        requestAttributes.getRequest().setAttribute(Constant.RESPONSE_RESULT_ANN.getValue(), Constant.RESPONSE_RESULT_ANN.getIsExist());
        return point.proceed();
    }
}
