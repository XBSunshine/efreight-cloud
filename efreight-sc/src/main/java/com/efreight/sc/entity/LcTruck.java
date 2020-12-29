package com.efreight.sc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * LC  车辆管理
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("lc_truck")
public class LcTruck implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 车辆ID
     */
    @TableId(value = "truck_id", type = IdType.AUTO)
    private Integer truckId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

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
     * 是否有效：1是 0否
     */
    private Boolean isValid;

    /**
     * 创建人
     */
    private Integer creatorId;

    private String creatorName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    private Integer editorId;

    private String editorName;

    /**
     * 修改时间
     */
    private LocalDateTime editTime;

    /**
     * 操作人
     */
    @TableField(exist = false)
    private String operator;

    /**
     * 操作时间
     */
    @TableField(exist = false)
    private String operateTime;
}
