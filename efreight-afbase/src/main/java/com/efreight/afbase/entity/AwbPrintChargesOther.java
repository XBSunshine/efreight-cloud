package com.efreight.afbase.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AF 操作管理 运单制单 杂费表
 * </p>
 *
 * @author xiaobo
 * @since 2019-10-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_awb_print_charges_other")
public class AwbPrintChargesOther implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 费用ID
     */
    @TableId(value = "awb_charges_id", type = IdType.AUTO)
    private Integer awbChargesId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 运单制单ID
     */
    private Integer awbPrintId;

    /**
     * 费用代码
     */
    private String awbChargesCode;

    /**
     * 费用等级
     */
    private BigDecimal awbChargesRate;

    /**
     * 费用数量
     */
    private BigDecimal awbChargesQuantity;

    /**
     * 费用金额
     */
    private BigDecimal awbChargesAmount;


}
