package com.efreight.prm.entity.budget;

import lombok.Data;

import java.io.Serializable;

/**
 * 预算分析列表实体
 * @author lc
 * @date 2021/2/25 11:35
 */
@Data
public class BudgetListBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 业务区域
     */
    private String zoneName;
    /**
     * 业务区域代码
     */
    private String zoneCode;
    /**
     * 服务ID
     */
    private Integer serviceId;
    /**
     * 服务名
     */
    private String serviceName;
    /**
     * 服务Code
     */
    private String serviceCode;
    /**
     * 实际（老业务）
     */
    private String oldActuralCharge;
    /**
     * 实际(新业务)
     */
    private String newActuralCharge;
    /**
     * 合计
     */
    private String totalActuralCharge;
    /**
     * 预算(老业务)
     */
    private String oldBudget;
    /**
     * 预算(新业务)
     */
    private String newBudget;
    /**
     * 总预算
     */
    private String totalBudget;
    /**
     * 完成率(老业务)
     */
    private String oldFillRate;
    /**
     * 完成率(新业务)
     */
    private String newFillRate;
    /**
     * 合计完成率
     */
    private String totalFillRate;
    /**
     * 同期实际
     */
    private String samePeriod;
    /**
     * 增长率
     */
    private String growthRate;

}
