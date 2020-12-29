package com.efreight.common.security.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lc
 * @date 2020/12/3 15:02
 */
@Data
public class UserBaseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer userId;
    private Integer orgId;
}
