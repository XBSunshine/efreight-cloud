package com.efreight.prm.entity.budget;

import lombok.Data;

import java.io.Serializable;

/**
 * 预算服务
 * @author lc
 * @date 2021/2/25 14:37
 */
@Data
public class BudgetServiceBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务ID
     */
    private Integer serviceId;

    /**
     * 服务名称
     */
    private String serviceName;

}
