package com.efreight.afbase.webService.config;

import javax.xml.ws.Endpoint;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.efreight.afbase.webService.AfAwbRouteWebService;



@Configuration
public class WebConfig {
	@Autowired
    private Bus bus;
    @Autowired
    AfAwbRouteWebService afAwbRouteWebService;
    @Bean
    public Endpoint endpoint() {
        EndpointImpl endpoint = new EndpointImpl(bus, afAwbRouteWebService);
        endpoint.publish("/AfAwbRouteWebService");
        return endpoint;
    }

}
