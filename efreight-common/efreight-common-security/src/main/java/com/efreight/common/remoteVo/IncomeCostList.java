package com.efreight.common.remoteVo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

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
public class IncomeCostList<income,cost> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<income> incomeList;
    private List<cost> costList;
    
    private List<income> incomeDeleteList;
    private List<cost> costDeleteList;
    /**
     * 业务范畴
     */
    private String businessScope;
    private String orderUuid;
    private Integer orderId;
    private Boolean incomeRecorded;
    private Boolean costRecorded;

    private String incomeStatus;
    private String costStatus;
}
