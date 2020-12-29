package com.efreight.afbase.entity.route;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author lc
 * @date 2020/9/16 14:05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_awb_route_hawb")
public class AfAwbRouteHawb {
    /**
     * 数据ID
     */
    private Integer awbRouteHawbId;
    /**
     * 主单追踪表ID
     */
    private Integer awbRouteId;
    /**
     * 业务范畴
     */
    private String businessScope;
    /**
     * 主运单号
     */
    private String awbNumber;
    /**
     * 分单号
     */
    private String hawbNumber;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 是否追踪 0未开始 1已跟踪
     */
    private Integer isTrack;
    /**
     * 跟踪时间
     */
    private LocalDateTime trackTime;
}
