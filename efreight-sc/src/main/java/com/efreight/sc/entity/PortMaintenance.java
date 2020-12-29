package com.efreight.sc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * CS 海运港口表
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sc_port_maintenance")
public class PortMaintenance implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "port_id", type = IdType.AUTO)
    private Integer portId;

    /**
     * 港口代码
     */
    private String portCode;

    /**
     * 港口中文名称
     */
    private String portNameCn;

    /**
     * 港口英文名称
     */
    private String portNameEn;

    /**
     * 国家中文名称
     */
    private String countryNameCn;

    /**
     * 是否启用  0 未启用 ，  1 启用 。
     */
    private Boolean isValid;

    /**
     * 航线
     */
    private String routingName;

    /**
     * 创建者ID
     */
    private Integer creatorId;

    /**
     * 创建者
     */
    private String creatorName;

    /**
     * 数据创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改者ID
     */
    private Integer editorId;

    /**
     * 修改者
     */
    private String editorName;

    /**
     * 修改时间
     */
    private LocalDateTime editTime;

    /**
     * 国家代码
     */
    private String countryCode;
    /**
     * 国家英文名
     */
    private String countryNameEn;
    /**
     * 城市代码
     */
    private String cityCode;
    /**
     * 城市英文名称
     */
    private String cityNameEn;
    /**
     * 城市中文名称
     */
    private String cityNameCn;
    /**
     * 经度
     */
    private String portLongitude;
    /**
     * 纬度
     */
    private String portLatitude;
    /**
     * 时区
     */
    private String timeZone;

}
