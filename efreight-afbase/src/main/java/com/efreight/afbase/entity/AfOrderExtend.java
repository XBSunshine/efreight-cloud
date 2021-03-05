package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author bxs
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_order_extend")
public class AfOrderExtend implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer orderId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 托盘材质
     */
    private String palletMaterial;

    /**
     * 特货包装
     */
    private String specialPackage;

    /**
     * 温度要求
     */
    private String celsiusRequire;

    /**
     * 温度计
     */
    private Integer thermometer;

    /**
     * 是否有温度要求
     */
    private Boolean isCelsiusRequire;

}
