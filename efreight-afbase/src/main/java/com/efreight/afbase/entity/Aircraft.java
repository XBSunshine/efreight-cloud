package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AF 基础信息 飞机类型码表
 * </p>
 *
 * @author xiaobo
 * @since 2020-04-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_aircraft")
public class Aircraft implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 飞机类型ID
     */
    @TableId(value = "aircraft_id", type = IdType.AUTO)
    private Integer aircraftId;

    /**
     * 飞机类型
     */
    private String aircraftType;

    /**
     * 飞机类型英文名称
     */
    private String aircraftTypeNameEn;

    /**
     * 飞机动力类型
     */
    private String powerType;

    /**
     * 飞机类型 宽体/窄体
     */
    private String aircraftTypePc;

    /**
     * 满载航距（英里）
     */
    private String fullLoadRangeMiles;

    /**
     * 巡航速率（英里）
     */
    private String cruiseSpeedMiles;
    
    private Integer creatorId;
    
    private String creatorName;
    
    private LocalDateTime createTime;
    
    private Integer editorId;
    
    private String editorName;
    
    private LocalDateTime editTime;


}
