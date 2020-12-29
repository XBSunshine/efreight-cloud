package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.efreight.common.core.utils.FormatUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_awb_number")
public class AwbNumber implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主运单号ID
     */
    @TableId(value = "awb_id", type = IdType.AUTO)
    private Integer awbId;

    /**
     * 主运单号UUID
     */
    private String awbUuid;
    /**
     * 签约公司ID
     */
    private Integer orgId;
    /**
     * 主运单号
     */
    private String awbNumber;
    /**
     * 主单状态
     */
    private String awbStatus;
    /**
     * 始发港
     */
    private String departureStation;
    /**
     * 运单所属
     */
    private String awbFromType;
    /**
     * 运单来源ID
     */
    private String awbFromId;
    /**
     * 运单来源名称
     */
    private String awbFromName;
    /**
     * 预定代理ID
     */
    private Integer reservedCoopId;
    /**
     * 预定代理名称
     */
    private String reservedCoopName;
    /**
     * 预订人ID
     */
    private Integer reservedUserId;
    /**
     * 预订人
     */
    private String reservedUser;
    /**
     * 预订时间
     */
    private Date reservedTime;
    /**
     * 锁定人ID
     */
    private Integer lockerId;
    /**
     *锁定人
     */
    private String lockerName;
    /**
     * 锁定时间
     */
    private Date lockTime;
    /**
     * 创建人ID
     */
    private Integer creatorId;
    /**
     *创建人
     */
    private String creatorName;
    /**
     * 创建时间
     */
    private Date creatTime;
    
    private transient String awbIds;
    private transient String awb3;
    private transient String awb8;
    private transient Integer awbcount;
    
    private transient String orgIdV;
    private transient String coopIdV;
    private transient String coopTypeV;
    private transient String coopCodeV;
    private transient String coopMnemonicV;
    private transient String coopNameV;
    private transient String shortNameV;
    private transient String value;


    public String getCreatorName() {
        return FormatUtils.formatCreator(this.creatorName);
    }
}
