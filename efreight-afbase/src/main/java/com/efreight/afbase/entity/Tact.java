package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_tact")
public class Tact implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 价格id
     */
    @TableId(value = "tact_id", type = IdType.AUTO)
    private Integer tactId;

    /**
     * 航司代码
     */
    private String carrierCode;
    /**
     * 始发港
     */
    private String departureStation;

    /**
     * 目的港
     */
    private String arrivalStation;
    /**
     * 开始时间
     */
    private LocalDateTime beginDate;

    /**
     * 结束时间
     */
    private LocalDateTime endDate;


    private String tactM;
    private String tactN;
    @TableField("tact_45")
    private String tact45;
    @TableField("tact_100")
    private String tact100;
    @TableField("tact_300")
    private String tact300;
    @TableField("tact_500")
    private String tact500;
    @TableField("tact_700")
    private String tact700;
    @TableField("tact_1000")
    private String tact1000;
    @TableField("tact_2000")
    private String tact2000;
    @TableField("tact_3000")
    private String tact3000;
    @TableField("tact_5000")
    private String tact5000;

    private transient String carrierPrefix;
    private transient String departureStationName;
    private transient String arrivalStationName;
    private transient String createTimeBegin;
	private transient String createTimeEnd;

    private Integer creatorId;
    private String creatorName;
    private LocalDateTime createTime;
    private Integer editorId;
    private String editorName;
    private LocalDateTime editTime;
    private transient Boolean isTrue;
    
    private String tactRemark;
    @TableField(exist = false)
    private String appid;
    @TableField(exist = false)
    private String flightDate;
    @TableField(exist = false)
    private String dataSource;
    private Integer orgId;
    @TableField(exist = false)
    private Integer dataSourceDef;

    private transient String awbNumberPrefix;//主单号3字码
    @TableField(exist = false)
    private String columnStrs;


}
