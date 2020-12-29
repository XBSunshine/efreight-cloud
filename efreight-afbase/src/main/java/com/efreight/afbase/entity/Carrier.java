package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_carrier")
public class Carrier implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "carrier_id", type = IdType.AUTO)
    private Integer carrierId;
    /**
     * 航司二字代码
     */
    private String carrierCode;

    /**
     * 航司名称
     */
    private String carrierNameCn;

    /**
     * 航司英文名称
     */
    private String carrierNameEn;

    @TableField(exist = false)
    private String carrierName;

    /**
     * 航司三字码
     */
    private String carrierPrefix;

    /**
     * 航司制单要求
     */
    private String carrierAwbRequirement;

    /**
     * 航司主单模板_格打
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private String carrierMawbModFormat;

    /**
     * 航司主单模板_套打
     */
    @TableField(strategy=FieldStrategy.IGNORED)
    private String carrierMawbModOver;

    /**
     * 航司分单模板_格打
     */
    @TableField(strategy=FieldStrategy.IGNORED)
    private String carrierHawbModFormat;

    /**
     * 航司分单模板_套打
     */
    @TableField(strategy=FieldStrategy.IGNORED)
    private String carrierHawbModOver;

    /**
     * 航司主单模板_格打(excel)
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private String carrierMawbModFormatExcel;

    /**
     * 航司主单模板_套打(excel)
     */
    @TableField(strategy=FieldStrategy.IGNORED)
    private String carrierMawbModOverExcel;

    /**
     * 航司分单模板_格打(excel)
     */
    @TableField(strategy=FieldStrategy.IGNORED)
    private String carrierHawbModFormatExcel;

    /**
     * 航司分单模板_套打(excel)
     */
    @TableField(strategy=FieldStrategy.IGNORED)
    private String carrierHawbModOverExcel;
    /**
     * 创建人ID
     */
    private Integer creatorId;

    /**
     * 创建人名称
     */
    private String creatorName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改人ID
     */
    private Integer editorId;

    /**
     * 修改人名称
     */
    private String editorName;

    /**
     * 修改人时间
     */
    private Date editTime;

    /**
     * 航司二字代码,用于后台判断是否更改过此值
     */
    @TableField(exist = false)
    private String carrierCode1;

    /**
     * 航司三字码,用于后台判断是否更改过此值
     */
    @TableField(exist = false)
    private String carrierPrefix1;


}
