package com.efreight.afbase.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AF 基础信息 服务类别：收付模板设定
 * </p>
 *
 * @author qipm
 * @since 2020-06-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_service_template")
public class AfServiceTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "template_id", type = IdType.AUTO)
    private Integer templateId;
    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 模板代码：TC001
     */
    private String templateCode;
    private String templateName;

    /**
     * 模板类型：1 应收   0 应付
     */
    private Integer templateType;

    /**
     * 口岸（出口 始发港  、进口 目的港）
     */
    private String portCode;

    /**
     * 模板备注
     */
    private String templateRemark;

    /**
     * 收款客户、付款对象ID
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer customerId;

    /**
     * 收费\付费：服务ID
     */
    private Integer serviceId;

    /**
     * 收费\付费：标准
     */
    private String serviceChargeStandard;

    /**
     * 收费\付费：币种
     */
    private String serviceCurrency;

    /**
     * 收费\付费：单价
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal serviceUnitPrice;

    /**
     * 创建人ID
     */
    private Integer creatorId;

    /**
     * 创建人
     */
    private String creatorName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

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
    private transient String createTimeBegin;
    private transient String createTimeEnd;
    private transient List<AfServiceTemplate> addTemplate;
    private transient List<AfServiceTemplate> deleteTemplate;
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal  serviceAmountMin;
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal serviceAmountMax;
    private Integer serviceAmountDigits;
    private String serviceAmountCarry;

    @TableField(exist = false)
    private String columnStrs;

}
