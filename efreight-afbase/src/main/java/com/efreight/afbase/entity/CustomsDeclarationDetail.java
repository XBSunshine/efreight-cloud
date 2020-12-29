package com.efreight.afbase.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.List;

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
@TableName("af_customs_declaration_detail")
public class CustomsDeclarationDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报关单明细ID
     */
    @TableId(value = "customs_declaration_detail_id", type = IdType.AUTO)
    private Integer customsDeclarationDetailId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 报关单ID
     */
    private Integer customsDeclarationId;

    /**
     * 项号
     */
    private String itemNumber;

    /**
     * 商品编号
     */
    private String productCode;

    /**
     * 商品名称
     */
    private String productName;

    @TableField(exist = false)
    private List<TariffDetails> products;

    /**
     * 规格型号
     */
    private String specificationModel;

    /**
     * 成交数量
     */
    @TableField(value = "quantity_1",strategy = FieldStrategy.IGNORED)
    private BigDecimal quantity1;
    @TableField(exist = false)
    private String quantity1Str;

    /**
     * 成交单位
     */
    @TableField("unit_1")
    private String unit1;

    @TableField(exist = false)
    private List<TariffUnit> unit1s;

    /**
     * 法定数量1
     */
    @TableField(value ="quantity_2",strategy = FieldStrategy.IGNORED)
    private BigDecimal quantity2;
    @TableField(exist = false)
    private String quantity2Str;

    /**
     * 法定单位1
     */
    @TableField("unit_2")
    private String unit2;

    @TableField(exist = false)
    private List<TariffUnit> unit2s;

    /**
     * 法定数量2
     */
    @TableField(value ="quantity_3",strategy = FieldStrategy.IGNORED)
    private BigDecimal quantity3;
    @TableField(exist = false)
    private String quantity3Str;

    /**
     * 法定单位2
     */
    @TableField("unit_3")
    private String unit3;

    @TableField(exist = false)
    private List<TariffUnit> unit3s;

    /**
     * 币制
     */
    private String currency;

    /**
     * 单价
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal declPrice;
    @TableField(exist = false)
    private String declPriceStr;

    /**
     * 总价
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal declTotal;
    @TableField(exist = false)
    private String declTotalStr;

    /**
     * 原产国
     */
    private String countryOrigin;
    @TableField(exist = false)
    private String countryOriginName;

    @TableField(exist = false)
    private List<Airport> countryOrigins;

    /**
     * 目的国
     */
    private String countryDestination;
    @TableField(exist = false)
    private String countryDestinationName;

    @TableField(exist = false)
    private List<Airport> countryDestinations;

    /**
     * 境内目的地
     */
    private String districtCode;
    @TableField(exist = false)
    private String districtCodeName;

    @TableField(exist = false)
    private List<PrmCategoryEciq> districtCodes;

    /**
     * 征免性质
     */
    private String cutMode;
    @TableField(exist = false)
    private String cutModeName;
}
