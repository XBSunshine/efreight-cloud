package com.efreight.common.remoteVo;

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
 * LC 订单操作日志
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class LcLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
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
     * 日志分类
     */
    private String logType;

    /**
     * 操作节点
     */
    private String nodeName;

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
     * 客户项目ID
     */
    private String projectUuid;

    /**
     * 客户项目名称
     */
    private String projectName;

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
     * 主运单号UUID
     */
    private String awbUuid;

    /**
     * 主运单号
     */
    private String awbNumber;

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

    private String logName;
    private String operationTimeStart;
    private String operationTimeEnd;


}
