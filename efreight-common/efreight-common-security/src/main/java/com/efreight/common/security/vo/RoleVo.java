package com.efreight.common.security.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
/**
 * 
 * @author zhanghw
 *
 */
@Data
public class RoleVo implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	private Integer roleId;

	private String roleName;

	private Integer creatorId;

	private LocalDateTime createTime;

	private Integer editorId;

	private LocalDateTime editTime;

	private Integer orgId;

	private Boolean roleStatus;
}
