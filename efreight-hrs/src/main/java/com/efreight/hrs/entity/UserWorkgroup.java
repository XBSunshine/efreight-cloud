package com.efreight.hrs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.efreight.hrs.pojo.org.workgroup.UserListBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * HRS 用户：工作组
 * @author lc
 * @date 2020/10/15 13:54
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("hrs_user_workgroup")
public class UserWorkgroup implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 数据ID
     */
    @TableId(value = "workgroup_id", type = IdType.AUTO)
    private Integer workgroupId;
    /**
     * 签约公司ID
     */
    private Integer orgId;
    /**
     * 业务范畴
     */
    private String businessScope;
    /**
     * 工作组名
     */
    private String workgroupName;
    /**
     * 工作组备注
     */
    private String workgroupRemark;
    /**
     * 创建者ID
     */
    private Integer creatorId;
    /**
     * 创建者名
     */
    private String  creatorName;
    /**
     * 数据创建时间
     */
    private LocalDateTime createTime;
    /**
     * 编辑者iD
     */
    private Integer editorId;
    /**
     * 编辑者名
     */
    private String editorName;
    /**
     * 编辑时间
     */
    private LocalDateTime editTime;

    /**
     * 用户个数
     */
    @TableField(exist = false)
    private Integer userCount;

    /**
     * 组中成员
     */
    @TableField(exist = false)
    private List<UserListBean> userList;


}
