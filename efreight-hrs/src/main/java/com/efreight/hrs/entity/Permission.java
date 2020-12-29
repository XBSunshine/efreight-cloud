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
@TableName("hrs_permission")
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "permission_id", type = IdType.AUTO)
    private Integer permissionId;

    private String permissionCode;

    private String permissionName;
    
    private String helpDocumentName;
    private String helpDocumentUrl;

    /**
     * 前段链接
     */
    private String path;

    /**
     * vue链接
     */
    private String url;

    private String permission;

    private String icon;

    private Integer parentId;

    private String parentIds;

    private Integer sort;

    /**
     * 1:启用 0:停用
     */
    private Integer status;

    private String permissionType;

    private Integer creatorId;

    private LocalDateTime createTime;

    private LocalDateTime stopDate;

    private String appCode;
    
    private String disabled;

    private String adminDefault;

}
