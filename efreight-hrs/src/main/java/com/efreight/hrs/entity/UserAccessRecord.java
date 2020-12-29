package com.efreight.hrs.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author lc
 * @date 2020/5/12 14:52
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("hrs_user_access_record")
public class UserAccessRecord {
    private static final long serialVersionUID = 1L;
    /**
     * 数据iD
     */
    private Integer recordId;
    /**
     * 企业ID
     */
    private Integer orgId;
    /**
     * 用户ID
     */
    private Integer userId;
    /**
     * 页面ID
     */
    private Integer permissionId;
    /**
     * 页面名称
     */
    private String permissionName;
    /**
     * 访问次数
     */
    private Integer recordsNumber;
    /**
     * 访问地址
     */
    private String path;
    /**
     * 数据创建时间
     */
    private LocalDateTime createTime;
    /**
     * 数据修改时间
     */
    private LocalDateTime editTime;

    /**
     * 最近访问时间
     */
    @TableField(exist = false)
    private String accessTime;
}
