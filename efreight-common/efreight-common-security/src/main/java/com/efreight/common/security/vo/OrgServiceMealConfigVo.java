package com.efreight.common.security.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lc
 * @date 2020/11/18 15:39
 */
@Data
public class OrgServiceMealConfigVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 问题
     */
    private Integer serviceNumberMax;
    /**
     * 使用量
     */
    private Integer serviceNumberUsed;
    /**
     * 剩余量
     */
    private Integer remaining;
}
