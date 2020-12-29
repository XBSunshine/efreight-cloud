package com.efreight.afbase.entity.view;

import lombok.Data;

import java.io.Serializable;

/**
 * 订阅明细
 *
 * @author lc
 * @date 2020/12/10 10:35
 */
@Data
public class SubscribeVO implements Serializable {


    /**
     * 日期
     */
    private String date;

    /**
     * 业务域（字典值）
     */
    private String businessScope;

    /**
     * 主单号
     */
    private String awbNumber;
    /**
     * 分单号
     */
    private String hawbNumber;
    /**
     * 数据创建人
     */
    private String creator;
    /**
     * 数据创建时间
     */
    private String createTime;
    /**
     * 数据创建IP
     */
    private String createIp;
}
