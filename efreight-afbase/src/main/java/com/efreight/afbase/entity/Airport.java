package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_airport")
public class Airport implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ap_id", type = IdType.AUTO)
    private Integer apId;
    /**
     * 机场代码
     */
    private String apCode;

    /**
     * 机场名称
     */
    private String apNameCn;

    /**
     * 机场英文名称
     */
    private String apNameEn;

    /**
     * 城市代码
     */
    private String cityCode;
    private String cityNameEn;
    private String cityNameCn;

    /**
     * 国家代码
     */
    private String nationCode;
    private String nationNameEn;
    private String nationNameCn;
    /**
     * 航线分区
     */
    private String routingGroupName;
    private String routingName;
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
     * 是否生效
     */
    private Boolean apStatus;

    /**
     * 创建信息
     */
    private Integer creatorId;
    private String creatorName;
    private LocalDateTime createTime;

    /**
     * 修改信息
     */
    private Integer editorId;
    private String editorName;
    private LocalDateTime editTime;

    @TableField(strategy = FieldStrategy.IGNORED)
    private String nationCodeThree;
    @TableField(strategy = FieldStrategy.IGNORED)
    private String nationCodeNumber;
    @TableField(strategy = FieldStrategy.IGNORED)
    private String nationCodeCoo;
}
