package com.efreight.sc.entity.view;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * VIEW
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_V_prm_coop")
public class AfVPrmCoop implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 合作伙伴ID
     */
    private Integer coopId;

    /**
     * 合作伙伴类型
     */
    private String coopType;

    /**
     * 合作伙伴代码
     */
    private String coopCode;

    /**
     * 助记码
     */
    private String coopMnemonic;

    /**
     * 合作伙伴中文全称
     */
    private String coopName;

    /**
     * 合作伙伴中文简称
     */
    private String shortName;

    @TableField("business_scope_AE")
    private String businessScopeAe;

    @TableField("business_scope_AI")
    private String businessScopeAi;

    @TableField("business_scope_SI")
    private String businessScopeSi;

    @TableField("business_scope_SE")
    private String businessScopeSe;

    @TableField("business_scope_TE")
    private String businessScopeTE;

    @TableField("business_scope_TI")
    private String businessScopeTI;

    @TableField("business_scope_LC")
    private String businessScopeLC;

    @TableField("business_scope_VL")
    private String businessScopeVL;

    @TableField(exist = false)
    private String businessScope;
}
