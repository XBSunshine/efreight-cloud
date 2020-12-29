package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AF 关税税则
 * </p>
 *
 * @author qipm
 * @since 2020-05-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_tariff_details")
public class TariffDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "tariff_id", type = IdType.AUTO)
    private Integer tariffId;
    /**
     * 税则编码
     */
    private String productCode;

    /**
     * 税则名称
     */
    private String productName;

    /**
     * 出口税率
     */
    private String exportTariff;
    /**
     * 出口退税率
     */
    private String exportRebateRate;
    /**
     * 出口暂定税率
     */
    private String temporaryExportTaxRate;
    

    /**
     * 增值税率
     */
    private String vatRates;
    /**
     * 最惠国进口税率
     */
    private String preferentialImportTariff;
    /**
     * 进口暂定税率
     */
    private String temporaryImportTaxRate;
    /**
     * 进口普通税率
     */
    private String importTariff;

    /**
     * 消费税率
     */
    private String consumptionTax;
    /**
     * 法定第一单位
     */
    private String legalFirstUnit;
    private String legalFirstUnitCode;

    /**
     * 法定第二单位
     */
    private String secondUnit;
    private String secondUnitCode;
    /**
     * 监管条件代码缩写
     */
    private String regulatoryCondition;
    /**
     * 监管条件详细信息
     */
    private String regulatoryConditionRemark;
    /**
     * 检验检疫类别代码缩写
     */
    private String testCategory;
    /**
     * 检验检疫类别详细信息
     */
    private String testCategoryRemark;
    /**
     * 申报要素
     */
    private String elements;


}
