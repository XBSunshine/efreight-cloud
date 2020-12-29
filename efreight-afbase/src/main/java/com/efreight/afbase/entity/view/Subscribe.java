package com.efreight.afbase.entity.view;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lc
 * @date 2020/12/3 15:43
 */
@Data
public class Subscribe implements Serializable {

    private static final long serialVersionUID = 1L;

    public Subscribe(){

    }

    public Subscribe(Integer userId, Integer orgId) {
        this.userId = userId;
        this.orgId = orgId;
    }

    private Integer userId;
    private Integer orgId;
    private String awbNumber;
    private String hawbNumber;
    private String businessScope;
    /**
     * 数据创建IP
     */
    private String createIp;

}
