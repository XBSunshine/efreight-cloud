package com.efreight.hrs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("hrs_user_page_set")
public class UserPageSet implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "page_set_id", type = IdType.AUTO)
    private Integer pageSetId;

    private Integer orgId;

    private Integer userId;

    private String pageName;

    private String label;

    private String prop;

    private String align;

    private Integer fieldNo;

    private Integer width;

    private Boolean sortable;

    private LocalDateTime createTime;

    private LocalDateTime editTime;

    @TableField(exist = false)
    private Integer index;

}
