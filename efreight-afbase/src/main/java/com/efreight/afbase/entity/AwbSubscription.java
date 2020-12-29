package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * AF 运单号 我的订阅
 * </p>
 *
 * @author xiaobo
 * @since 2020-06-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_awb_subscription")
public class AwbSubscription implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 运单订阅ID
     */
    @TableId(value = "awb_subscription_id", type = IdType.AUTO)
    private Integer awbSubscriptionId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 订阅用户ID
     */
    private Integer userId;

    /**
     * 订阅运单号
     */
    private String awbNumber;

    /**
     * 分单号
     */
    private String hawbNumber;

    /**
     * 业务域
     */
    private String businessScope;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 订阅来源：货物追踪、订单轨迹、送货地图
     */
    private String subscriptionFrom;

    /**
     * 订阅来源地址
     */
    private String subscriptionFromUrl;

    /**
     * 创建者IP
     */
    private String createIp;

    /**
     * 是否展示 true是，false否
     */
    @TableField(value = "is_display")
    private Boolean isDisplay;





}
