package com.efreight.afbase.entity.view;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lc
 * @date 2020/7/31 16:30
 */
@Data
public class CarrierSearch implements Serializable {
    /**
     * 航司二字码
     */
    private String carrierCode;
    /**
     * 航司三字码
     */
    private String carrierPrefix;
    /**
     * 航司中文名称
     */
    private String carrierNameCn;
}
