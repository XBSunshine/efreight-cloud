package com.efreight.afbase.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AF 询价：报价单明细
 * </p>
 *
 * @author xiaobo
 * @since 2020-05-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_order_Inquiry_quotation")
public class OrderInquiryQuotation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报价单ID
     */
    @TableId(value = "order_inquiry_quotation_id", type = IdType.AUTO)
    private Integer orderInquiryQuotationId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 询价订单ID
     */
    private Integer orderInquiryId;

    /**
     * 报价公司
     */
    private String quotationCompanyName;

    /**
     * 报价联系人信息
     */
    private String quotationContacts;

    /**
     * 报价有效期（截止日期)
     */
    private LocalDateTime quotationEndDate;

    /**
     * 航司代码
     */
    private String carrierCode;

    /**
     * 始发港
     */
    private String departureStation;

    /**
     * 中转港
     */
    private String transitStation;

    /**
     * ETD
     */
    private LocalDate expectDeparture;

    /**
     * 分批信息
     */
    private String batchRemark;

    /**
     * 报价：单价
     */
    private BigDecimal quotationAmount;

    /**
     * 报价：总价
     */
    private BigDecimal quotationUnitprice;

    @TableField(exist = false)
    private BigDecimal price;

    @TableField(exist = false)
    private String priceType;

    /**
     * 重量等级
     */
    private String weightClass;

    /**
     * 密度等级
     */
    private String densityClass;

    /**
     * 报价备注
     */
    private String quotationRemark;

    /**
     * 是否有效：默认1
     */
    private Boolean isValid;

    /**
     * 是否选中报价方案
     */
    private Boolean quotationSelected;

    /**
     * 创建人ID
     */
    private Integer creatorId;

    /**
     * 创建人
     */
    private String creatorName;

    /**
     * 创建人时间
     */
    private LocalDateTime creatTime;

    /**
     * 修改人ID
     */
    private Integer editorId;

    /**
     * 修改人
     */
    private String editorName;

    /**
     * 修改时间
     */
    private LocalDateTime editTime;

    @TableField(exist = false)
    private String rowUuid;

}
