package com.efreight.afbase.entity;

import java.util.List;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AF 单货匹配
 * </p>
 *
 * @author qipm
 * @since 2019-10-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AfOrderMatch implements Serializable {

    private static final long serialVersionUID = 1L;

    //主运单号
    private String awbId;
    private String awbUuid;
    private String awbNumber;
    //运单来源
    private String awbFromName;
    /**
     * 主单付费重量
     */
    private Double awbCostChargeWeight;
    /**
     * 主单成本单价
     */
    private Double awbMsrUnitprice;
    /**
     * 主单成本单价
     */
    private Double awbMsrAmount;
    private Double priceValue3;
    private Double priceValue5;
    
    private List<AfOrder> orders;



}
