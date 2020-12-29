package com.efreight.afbase.entity.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 第三方舱单数据
 */
@Data
public class ShippingBillData implements Serializable {
    @JsonProperty("auth_token")
    private String authToken;
    private String platform;
    private String function;
    private String data;
    private String url;
    private Integer type =1;


}
