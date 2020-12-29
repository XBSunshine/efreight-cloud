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
public class TactInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 价格id
     */
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
    private String beginDateStr;
    /**
     * 结束时间
     */
    private String endDateStr;
    private String tactM;
    private String tactN;
    private String tact45;
    private String tact100;
    private String tact300;
    private String tact500;
    private String tact700;
    private String tact1000;
    private String tact2000;
    private String tact3000;
    private String tact5000;
    private String tactRemark;



}
