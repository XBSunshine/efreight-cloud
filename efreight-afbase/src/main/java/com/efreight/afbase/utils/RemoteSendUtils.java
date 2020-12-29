package com.efreight.afbase.utils;

import com.efreight.common.security.vo.OrgInterfaceVo;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

public class RemoteSendUtils {
    //发送
    public static ResponseEntity<String> sendToThirdUrl(OrgInterfaceVo config, String data) {
        RestTemplate restTemplate = new RestTemplate();
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("data",data);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(config.getUrlPost()+config.getFunction(), body, String.class);
        return responseEntity;
    }

}
