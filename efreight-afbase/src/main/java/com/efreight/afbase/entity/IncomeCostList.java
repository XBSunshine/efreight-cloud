package com.efreight.afbase.entity;

import java.util.List;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AF 延伸服务 应收
 * </p>
 *
 * @author qipm
 * @since 2019-10-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class IncomeCostList implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<AfIncome> incomeList;
    private List<AfCost> costList;
    
    private List<AfIncome> incomeDeleteList;
    private List<AfCost> costDeleteList;
    /**
     * 业务范畴
     */
    private String businessScope;
    private String orderUuid;
    private Integer orderId;
    private Boolean incomeRecorded;
    private Boolean costRecorded;
   



}
