package com.efreight.common.security.vo;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
@Data
public class LogVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer logId;

    private String opLevel;

    private String opType;

    private String opName;

    private String opInfo;

    private Integer creatorId;

    private LocalDateTime createTime;

    private Integer orgId;

    private Integer deptId;


}
