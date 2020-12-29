package com.efreight.afbase.entity.view;

import lombok.Data;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * @author lc
 * @date 2020/12/4 10:54
 */
@Data
public class OrderTrackQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    public OrderTrackQuery(String awbNumber, String businessScope, Integer orgId) {
        this.awbNumber = awbNumber;
        this.businessScope = businessScope;
        this.orgId = orgId;
    }

    /**
     * 主单号
     */
    private String awbNumber;
    /**
     * 分单号
     */
    private String hawbNumber;

    /**
     * 业务域
     */
    private String businessScope;

    /**
     * 企业ID
     */
    private Integer orgId;

    public void validate(){
        Assert.hasLength(awbNumber, "主单号不能为空");
        Assert.hasLength(businessScope, "业务类型不能为空");
        Assert.notNull(orgId, "企业ID不能为空");
    }
}
