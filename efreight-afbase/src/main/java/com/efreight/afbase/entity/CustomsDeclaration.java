package com.efreight.afbase.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AF 报关单
 * </p>
 *
 * @author xiaobo
 * @since 2020-12-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_customs_declaration")
public class CustomsDeclaration implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报关单ID
     */
    @TableId(value = "customs_declaration_id", type = IdType.AUTO)
    private Integer customsDeclarationId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    private Integer warehouseId;
    @TableField(exist = false)
    private String warehouseNameCn;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 主运单号
     */
    private String awbNumber;

    /**
     * 分运单号
     */
    private String hawbNumber;

    /**
     * 预录入编号
     */
    private String customsNumberPreEntry;

    /**
     * 海关编号
     */
    private String customsNumber;

    /**
     * 发货人代码（境内、境外）
     */
    private String shipperCode;

    /**
     * 发货人名称（境内、境外）
     */
    private String shipperName;

    /**
     * 收货人代码（境内、境外）
     */
    private String consigneeCode;

    /**
     * 收货人名称（境内、境外）
     */
    private String consigneeName;

    /**
     * 关别（出境、入境 ）
     */
    private String ciqQreaCode;

    @TableField(exist = false)
    private String ciqQreaName;

    /**
     * 出口日期/进口日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate exportImportDate;

    /**
     * 申报日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate declareDate;

    /**
     * 备案号
     */
    private String manualNumber;

    /**
     * 运输方式
     */
    private String transportMode;
    @TableField(exist = false)
    private String transportModeName;

    /**
     * 运输工具
     */
    private String transportName;

    /**
     * 生产销售单位代码/消费使用单位代码
     */
    private String salesConsumptionCode;

    /**
     * 生产销售单位名称/消费使用单位名称
     */
    private String salesConsumptionName;

    /**
     * 监管方式
     */
    private String tradeMode;
    @TableField(exist = false)
    private String tradeModeName;

    /**
     * 征免性质
     */
    private String cutMode;
    @TableField(exist = false)
    private String cutModeName;

    /**
     * 许可证号
     */
    private String licenseNumber;

    /**
     * 合同协议号
     */
    private String contractNumber;

    private String portDepartureArrival;
    @TableField(exist = false)
    private String portDepartureArrivalName;

    /**
     * 贸易国别
     */
    private String countryTrade;
    @TableField(exist = false)
    private String countryTradeName;

    /**
     * 启运国/运抵国
     */
    private String countryDepartureArrival;
    @TableField(exist = false)
    private String countryDepartureArrivalName;

    /**
     * 指运港/经停港
     */
    @TableField("country_departure_arrival_1")
    private String countryDepartureArrival1;
    @TableField(exist = false)
    private String countryDepartureArrival1Name;

    /**
     * 离境口岸/入境口岸
     */
    private String exportImportPort;
    @TableField(exist = false)
    private String exportImportPortName;

    /**
     * 包装种类
     */
    private String packageType;
    @TableField(exist = false)
    private String packageTypeName;

    /**
     * 件数
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer pieces;
    @TableField(exist = false)
    private String piecesStr;

    /**
     * 毛重
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal grossWeight;
    @TableField(exist = false)
    private String grossWeightStr;

    /**
     * 净重
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal netWeight;
    @TableField(exist = false)
    private String netWeightStr;

    /**
     * 成交方式
     */
    private String transMode;
    @TableField(exist = false)
    private String transModeName;

    /**
     * 币制
     */
    private String feeCurrency;

    /**
     * 运费
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal feeRate;
    @TableField(exist = false)
    private String feeRateStr;

    /**
     * 保费
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal insurRate;
    @TableField(exist = false)
    private String insurRateStr;

    /**
     * 杂费
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal otherRate;
    @TableField(exist = false)
    private String otherRateStr;

    /**
     * 随附单证代码（分号分隔）
     */
    private String edocCode;
    @TableField(exist = false)
    private String edocCodeName;

    /**
     * 唛码及备注
     */
    private String marksNotes;

    /**
     * 特殊关系确认
     */
    @TableField("spec_flag_1")
    private Boolean specFlag1;
    @TableField(exist = false)
    private String specFlag1Str;

    /**
     * 价格影响确认
     */
    @TableField("spec_flag_2")
    private Boolean specFlag2;
    @TableField(exist = false)
    private String specFlag2Str;

    /**
     * 支持特权使用费确认
     */
    @TableField("spec_flag_3")
    private Boolean specFlag3;
    @TableField(exist = false)
    private String specFlag3Str;

    /**
     * 自提自缴
     */
    @TableField("spec_flag_4")
    private Boolean specFlag4;
    @TableField(exist = false)
    private String specFlag4Str;

    /**
     * 预留
     */
    @TableField("spec_flag_5")
    private Boolean specFlag5;

    /**
     * 报关员
     */
    private String customsDeclarer;

    /**
     * 报关员证编号
     */
    private String customsDeclarerCertificateNumber;

    /**
     * 报关员电话
     */
    private String customsDeclarerPhoneNumber;

    /**
     * 申报单位代码
     */
    private String customsAgentCode;

    /**
     * 申报单位名称
     */
    private String customsAgentName;


    /**
     * 创建人
     */
    private Integer creatorId;

    private String creatorName;

    @TableField(exist = false)
    private LocalDateTime createTimeStart;
    @TableField(exist = false)
    private LocalDateTime createTimeEnd;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    private Integer editorId;

    private String editorName;

    /**
     * 修改时间
     */
    private LocalDateTime editTime;

    @TableField(exist = false)
    private List<CustomsDeclarationDetail> detailList;


}
