package com.efreight.ws.common.config;

import com.efreight.ws.afbase.ws.impl.AEOrderWSImpl;
import com.efreight.ws.common.interceptor.AuthInInterceptor;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.ws.Endpoint;


@Configuration
public class CXFConfig {

    @Bean
    public Endpoint endpoint(SpringBus springBus, AuthInInterceptor authInterceptor) {
        EndpointImpl endpoint = new EndpointImpl(springBus, new AEOrderWSImpl());
        endpoint.publish("/v1.0/af");
        //授权拦截器
        endpoint.getInInterceptors().add(authInterceptor);

        //日志输出拦截器
        endpoint.getInInterceptors().add(new LoggingInInterceptor());
        endpoint.getOutInterceptors().add(new LoggingOutInterceptor());

        return endpoint;
    }

    @Bean
    public SpringBus cxf() {
        return new SpringBus();
    }
}
