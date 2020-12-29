package com.efreight.hrs.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
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
@TableName("hrs_blacklist")
public class Blacklist implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "blacklist_id", type = IdType.AUTO)
    private Integer blacklistId;

    private String fromApp;

    private String userEmail;

    private String phoneNumber;

    private String userName;

    private String userEname;

    private String idType;

    private String idNumber;

    private LocalDateTime blacklistDate;

    private String blacklistReason;

    private Integer creatorId;

    private LocalDateTime createTime;

    private String orgId;

    private Boolean blacklistStatus;


}
