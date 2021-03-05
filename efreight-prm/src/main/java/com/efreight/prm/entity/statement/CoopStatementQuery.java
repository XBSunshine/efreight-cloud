package com.efreight.prm.entity.statement;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 应收账龄-查询参数
 * @author lc
 * @date 2021/1/29 13:38
 */
@Data
public class CoopStatementQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer orgId;

    /**
     * 是否超期 1是，0否
     */
    private Integer overdue;

    /**
     * 超额 1是  0否
     */
    private Integer excess;
    /**
     * 客户中文全称名称
     */
    private String coopName;

    // 客户负责人
    private String customerResponsible;

    /**
     * 超期区间
     */
    private List<Integer>overdueInterval;

    private Integer intervalAmount1;
    private Integer intervalAmount2;
    private Integer intervalAmount3;
    private Integer intervalAmount4;
    private Integer intervalAmount5;

    /**
     * 导出的列
     */
    private String columnStr;

}
