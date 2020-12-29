package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_coop_project")
public class CoopProject implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 仓库id
     */
    @TableId(value = "project_id", type = IdType.AUTO)
    private Integer projectId;
    /**
     * 签约公司id
     */
    private Integer orgId;
    /**
     * 业务范畴
     */
    private String businessScope;
    /**
     * 服务产品
     */
    private String businessProduct;
    /**
     * 助记码
     */
    private String projectCode;
    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 合作伙伴ID/公司
     */
    private Integer coopId;
    private String coopName;

    /**
     * 运输条款
     */
    private String transitClause;

    /**
     * 付款方式
     */
    private String paymentMethod;
    /**
     * 业务性质
     */
    private String businessType;
    /**
     * 发票类型
     */
    private String invoiceType;
    /**
     * 销项税率
     */
    private Double vatOutput;
    /**
     * 结算币种
     */
    private String currencyCode;
    /**
     * 接提货要求
     */
    private String warehouseNote;
    /**
     * 制单要求
     */
    private String awbNote;
    /**
     * 是否海外项目
     */
    private Integer isOverseas;
    /**
     * 是否公司项目
     */
    private Integer isHeadquarters;
    /**
     * 责任客服ID
     */
    private Integer servicerId;
    /**
     * 责任客服名字
     */
    private String servicerName;
    /**
     * 责任销售ID
     */
    private Integer salesId;
    /**
     * 责任销售名字
     */
    private String salesName;
    /**
     * 销售经理ID
     */
    private Integer salesManagerId;
    /**
     * 销售经理名字
     */
    private String salesManagerName;
    /**
     * 是否停用
     */
    private Boolean isStop;
    /**
     * 停用人ID
     */
    private Integer stopUserId;
    /**
     * 停用人
     */
    private String stopUserName;
    /**
     * 停用原因
     */
    private String stopReason;
    /**
     * 创建人ID
     */
    private Integer creatorId;
    /**
     * 创建人
     */
    private String creatorName;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改人ID
     */
    private Integer editorId;
    /**
     * 修改人
     */
    private String editorName;
    /**
     * 修改时间
     */
    private Date editTime;
    /**
     * 锁定人ID
     */
    private Integer lockUserId;
    /**
     * 锁定人
     */
    private String lockUserName;
    /**
     * 锁定时间
     */
    private Date lockTime;
    /**
     * 锁定原因
     */
    private String lockReason;
    /**
     * 解锁人ID
     */
    private Integer openUserId;
    /**
     * 解锁人
     */
    private String openUserName;
    /**
     * 解锁时间
     */
    private Date openTime;
    /**
     * 解锁原因
     */
    private String openReason;
    /**
     * 解锁截止日期
     */
    private String openLimit;
    
    /**
     * 是否锁定
     */
    private Boolean isLock;


}
