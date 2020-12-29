package com.efreight.sc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * VL 派車單操作日志
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("vl_log")
public class VlLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Integer logId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 操作页面
     */
    private String pageName;

    /**
     * 操作页面功能
     */
    private String pageFunction;

    /**
     * 日志详细内容
     */
    private String logRemark;

    /**
     * 订单UUID
     */
    private String orderUuid;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 订单号
     */
    private String orderNumber;

    /**
     * 创建人ID
     */
    private Integer creatorId;

    /**
     * 创建人
     */
    private String creatorName;

    /**
     * 创建人时间
     */
    private LocalDateTime creatTime;
    @TableField(exist = false)
    private String logName;
    @TableField(exist = false)
    private String operationTimeStart;
    @TableField(exist = false)
    private String operationTimeEnd;

}
