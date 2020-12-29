package com.efreight.hrs.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author wangxx
 * @since 2019-07-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("hrs_user_dept")
public class UserDept implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer userId;

    private Integer deptId;

    private String jobPosition;

    private Integer creatorId;

    private LocalDateTime createTime;

    private Integer orgId;

    @TableField("isMain")
    private Boolean isMain;

    @TableField(exist = false)
    private String creatorName;
    @TableField(exist = false)
    private String deptName;
    @TableField(exist = false)
    private String deptCode;
    @TableField(exist = false)
    private String deptStatus;


}
