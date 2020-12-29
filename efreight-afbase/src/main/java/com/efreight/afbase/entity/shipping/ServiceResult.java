package com.efreight.afbase.entity.shipping;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

//舱单接口返回数据
@Data
@XmlRootElement(name = "ServiceResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceResult implements Serializable {

    @XmlElement(name = "ResultCode")
    private Integer code;

    @XmlElement(name = "ResultContent")
    private String content;

    @XmlElement(name = "ResultData")
    private ServiceResultData data;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    static class ServiceResultData{
        @XmlElement(name = "ServiceEntitySyscode")
        private String serviceEntitySyscode;
    }
}
