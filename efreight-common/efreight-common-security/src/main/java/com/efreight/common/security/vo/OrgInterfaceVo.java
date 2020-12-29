package com.efreight.common.security.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OrgInterfaceVo {

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
