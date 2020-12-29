package com.efreight.sc.entity.view;

import com.efreight.sc.entity.LcTruck;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author lc
 * @date 2020/8/12 11:40
 */
@Data
public class LcTruckExcel implements Serializable {
    /**
     * 车牌号
     */
    private String truckNumber;

    /**
     * 车辆：长度
     */
    private Integer length;

    /**
     * 车辆：吨位
     */
    private BigDecimal ton;

    /**
     * 车辆：限重
     */
    private BigDecimal weightLimit;

    /**
     * 车辆：最大体积
     */
    private BigDecimal volumeLimit;

    /**
     * 司机姓名
     */
    private String driverName;

    /**
     * 司机电话
     */
    private String driverTel;


    /**
     * 操作人
     */
    private String operator;

    /**
     * 操作时间
     */
    private String operateTime;

    public static LcTruckExcel build(LcTruck item) {
        if(null == item){return null;}
        LcTruckExcel lcTruckExcel = new LcTruckExcel();
        lcTruckExcel.setTruckNumber(item.getTruckNumber());
        lcTruckExcel.setLength(item.getLength());
        lcTruckExcel.setTon(item.getTon());
        lcTruckExcel.setWeightLimit(item.getWeightLimit());
        lcTruckExcel.setVolumeLimit(item.getVolumeLimit());
        lcTruckExcel.setDriverName(item.getDriverName());
        lcTruckExcel.setDriverTel(item.getDriverTel());
        lcTruckExcel.setOperateTime(item.getOperateTime());
        lcTruckExcel.setOperator(item.getOperator());
        return lcTruckExcel;
    }
}
