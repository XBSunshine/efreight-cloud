package com.efreight.prm.entity.budget;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * @author lc
 * @date 2021/2/25 12:36
 */
@Data
public class BudgetQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务ID
     */
    private Integer serviceId;
    /**
     * 区域代码
     */
    private String zoneCode;
    /**
     * 账单起始时间
     */
    private String startDate;
    /**
     * 账单结束时间
     */
    private String endDate;
    /**
     * 销售员ID
     */
    private Set<Integer> saleIds;

    /**
     * 销售员ID(字符串连接)
     * */
    private String saleIdsStr;
}
