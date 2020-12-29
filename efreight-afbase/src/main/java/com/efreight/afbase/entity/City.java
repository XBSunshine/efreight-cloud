package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@TableName("af_city")
public class City implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @TableId(value = "city_id", type = IdType.AUTO)
    private Integer cityId;
    /**
     * 城市代码
     */
    private String cityCode;

    /**
     * 国家代码
     */
    private String nationCode;

    /**
     * 城市名称
     */
    private String cityNameCn;

    /**
     * 城市英文名
     */
    private String cityNameEn;

    /**
     * 城市电话区号
     */
    private String cityTelCode;

    /**
     * 时区
     */
    private String timeZone;

    /**
     * 经度
     */
    private String apLongitude;

    /**
     * 纬度
     */
    private String apLatitude;

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
    private LocalDateTime createTime;

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
    private LocalDateTime editTime;

    @TableField(exist = false)
    private String nationNameCn;

    @TableField(exist = false)
    private String nationNameEn;


}
