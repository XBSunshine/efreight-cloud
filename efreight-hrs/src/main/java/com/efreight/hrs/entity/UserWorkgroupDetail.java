package com.efreight.hrs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * HRS 用户：工作组关系
 * @author lc
 * @date 2020/10/15 14:00
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("hrs_user_workgroup_detail")
public class UserWorkgroupDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 数据ID
     */
    @TableId(value = "workgroup_detail_id", type = IdType.AUTO)
    private Integer workgroupDetailId;
    /**
     * 企业ID
     */
    private Integer orgId;
    /**
     * 工作组ID
     */
    private Integer workgroupId;
    /**
     * 用户ID
     */
    private Integer userId;
}
