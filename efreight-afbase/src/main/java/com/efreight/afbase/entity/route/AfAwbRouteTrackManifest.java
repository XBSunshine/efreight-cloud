package com.efreight.afbase.entity.route;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author lc
 * @date 2020/5/6 13:53
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_awb_route_track_manifest")
public class AfAwbRouteTrackManifest {
    /**
     * 数据ID 自动增长
     */
    @TableId(value = "awb_route_track_manifest_id", type = IdType.AUTO)
    private Integer trackManifestId;
    /**
     * 轨迹表ID
     */
    private Integer awbRouteId;
    /**
     *主单号
     */
    private String awbNumber;
    /**
     * 分单号
     */
    private String hawbNumber;
    /**
     *报文：报文获取顺序码（排序用）
     */
    private String sourceSyscode;
    /**
     * 事件时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventTime;
    /**
     *件数
     */
    private String quantity;
    /**
     * 毛重
     */
    private String grossWeight;
    /**
     *备注
     */
    private String remark;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 是否有效
     */
    private Integer isValid;


    /**
     * 舱单进出口标识
     */
    @TableField("I_E_Flag")
    private String flag;

}
