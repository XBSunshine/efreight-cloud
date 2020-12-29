package com.efreight.afbase.entity.cargo.track;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author lc
 * @date 2020/12/3 14:27
 */
@Data
public class CargoRoute implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主单号
     */
    private String awbNumber;
    /**
     * 分单号
     */
    private String hawbNumber;
    private String flightStatusCode;
    private String flightStatusName;
    /**
     * 航班号
     */
    private String flightNum;
    /**
     * 事件时间
     */
    private LocalDateTime eventTime;
    /**
     * 备注
     */
    private String remark;
    /**
     * 件数
     */
    private String quantity;
    /**
     * 毛重
     */
    private String grossWeight;
    /**
     * 排序码
     */
    private String sourceSyscode;
}
