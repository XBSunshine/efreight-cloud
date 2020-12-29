package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

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
@TableName("hrs_org_api_config")
public class OrgInterface implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "org_api_config_id", type = IdType.AUTO)
    private Integer orgApiConfigId;

    private Integer orgId;

    private String apiType;

    private String appid;

    private String authToken;

    private String platform;

    private String function;

    private Integer enable;

    private String urlAuth;

    private String urlPost;

    private String apiRemark;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;
    
    private Integer editorId;
    
    private String editorName;

    private LocalDateTime editTime;

}
