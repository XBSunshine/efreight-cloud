package com.efreight.afbase.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RequestUtil {
    /**
     * post数据到eFreightHttpEngine上
     * @param requestXml
     * @return
     */
    public static String PosteFreightHttpEngine(String url, String requestXml){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        try {
            MultiValueMap<String, String> params= new LinkedMultiValueMap<>();
            params.add("serviceXml", requestXml);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url,params,String.class);
            if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                throw new RuntimeException("第三方接口调用异常：" + responseEntity.getBody());
            }
            return responseEntity.getBody();
        }catch (Exception ex){
            ex.printStackTrace();
            throw ex;
        }

    }
}
