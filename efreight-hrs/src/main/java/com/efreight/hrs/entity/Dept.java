package com.efreight.hrs.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("hrs_dept")
public class Dept implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "dept_id", type = IdType.AUTO)
    private Integer deptId;

    private String deptCode;

    private String deptName;

    private String shortName;

    private String fullName;

    @TableField(strategy= FieldStrategy.IGNORED)
    private Integer managerId;

    private Boolean isProfitunit;

    private Boolean isFinalProfitunit;

    private Integer budgetHc;

    private Integer creatorId;

    private LocalDateTime createTime;

    private Integer editorId;

    private LocalDateTime editTime;

    private LocalDateTime stopDate;

    private Integer stopId;

    private Integer orgId;

    private Boolean deptStatus;
    
    private transient Boolean hasChildren = false;
    private transient String managerName;
    private transient Integer actualHc;
    private transient String deptCodeSelect;
    private transient String deptNameOld;
    private transient String shortNameOld;


}
