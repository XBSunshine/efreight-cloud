package com.efreight.ws.common.util;

import com.alibaba.fastjson.JSON;
import com.efreight.ws.common.pojo.WSException;
import org.springframework.util.StringUtils;

import java.util.Map;

public class ValidUtil {

    public static void valid(Object value, String message){
        if(null == value){
            throw WSException.argEx(message);
        }
        if(!StringUtils.hasLength(value.toString())){
            throw WSException.argEx(message);
        }
    }

    public static void valid(Object bean, Map<String, String> validField){
        String json = JSON.toJSONString(bean);
        Map<String, Object> map = JSON.parseObject(json, Map.class);

        validField.keySet().stream().forEach((key)->{
            Object value =  map.get(key);
            valid(value, validField.get(key));
        });
    }
}
