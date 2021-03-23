package com.efreight.afbase.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AF 订单管理 出口订单 签单表
 * </p>
 *
 * @author qipm
 * @since 2020-11-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_rounting_sign")
public class RountingSign implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
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
     * 签单状态：1已签单，0未签订
     */
    private Integer signState;

    /**
     * MSR 单价
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal msrUnitprice;

    /**
     * 收费重量
     */
    private Double incomeWeight;

    /**
     * MSR 金额（本币）
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal msrFunctionalAmount;

    /**
     * 航线负责人：名称
     */
    private String routingPersonName;

    /**
     * 航线负责人ID
     */
    private Integer routingPersonId;
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal cuUnitprice;

    private BigDecimal costWeight;
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal cuFunctionalAmount;
    
    /**
     * rowid， insert updat 更新rowid
     */
    private String rowUuid;

    private Integer editorId;

    private String editorName;

    private LocalDateTime editTime;
    
    @TableField(exist = false)
    private String awbNumber;
    @TableField(exist = false)
    private String awbFromName;
    @TableField(exist = false)
    private Integer awbFromId;
    @TableField(exist = false)
    private String businessProduct;
    @TableField(exist = false)
    private String departureStation;
    @TableField(exist = false)
    private String arrivalStation;
    @TableField(exist = false)
    private String expectFlight;
    @TableField(exist = false)
    private String transitStation;
    @TableField(exist = false)
    private LocalDate expectDeparture;
    @TableField(exist = false)
    private String transitStation2;
    @TableField(exist = false)
    private Integer planPieces;
    @TableField(exist = false)
    private Integer confirmPieces;
    @TableField(exist = false)
    private BigDecimal planWeight;
    @TableField(exist = false)
    private BigDecimal confirmWeight;
    @TableField(exist = false)
    private BigDecimal planDensity;
    @TableField(exist = false)
    private BigDecimal confirmDensity;
    @TableField(exist = false)
    private BigDecimal planChargeWeight;
    @TableField(exist = false)
    private BigDecimal confirmChargeWeight;
    @TableField(exist = false)
    private BigDecimal costUnitPrice;
    @TableField(exist = false)
    private BigDecimal costQuantity;
    @TableField(exist = false)
    private BigDecimal costAmount;
    @TableField(exist = false)
    private String msrPriceType;
    @TableField(exist = false)
    private String costPriceType;
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal msrAmount;
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal cuAmount;
    

}
