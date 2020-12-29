package com.efreight.hrs.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;

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
@TableName("hrs_log")
public class Log implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "log_id", type = IdType.AUTO)
    private Integer logId;

    private String opLevel;

    private String opType;

    private String opName;

    private String opInfo;

    private Integer creatorId;

    private LocalDateTime createTime;

    private Integer orgId;

    private Integer deptId;

    private transient String creatorName;
	private transient String createTimeBegin;
	private transient String createTimeEnd;
    


}
