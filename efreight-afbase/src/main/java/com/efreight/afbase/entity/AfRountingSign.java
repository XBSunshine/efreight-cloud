package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_rounting_sign")
public class AfRountingSign implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "rounting_sign_id", type = IdType.AUTO)
    private Integer rountingSignId;
    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 签单状态
     */
    private Integer signState;

    /**
     * MSR 单价
     */
    private BigDecimal msrUnitprice;

    /**
     * 收费重量
     */
    private Double incomeWeight;

    /**
     * MSR 金额（本币）
     */
    @TableField("msr_functional_amount")
    private BigDecimal msrAmountWriteoff;

    /**
     * 航线负责人：名称
     */
    private String routingPersonName;

    /**
     * 航线负责人ID
     */
    private Integer routingPersonId;

    /**
     * rowUuid
     */
    private String rowUuid;

    private Integer editorId;

    private String editorName;

    private Date edit_time;

}
