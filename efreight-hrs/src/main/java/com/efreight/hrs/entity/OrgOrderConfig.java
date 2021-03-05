package com.efreight.hrs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 企业订单配置信息
 * @author lc
 * @date 2020/7/28 15:48
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("hrs_org_order_config")
public class OrgOrderConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 数据ID
     */
    @TableId(value = "config_id", type = IdType.AUTO)
    private Integer configId;
    /**
     * 签约公司ID
     */
    private Integer orgId;
    /**
     * 业务范畴
     */
    private String businessScope;
    /**
     *订单设置：财务锁账可见， 1 是，0 否
     */
    private Boolean financeLockView;
    /**
     *服务产品
     */
    private String businessProduct;
    /**
     *货物类型
     */
    private String goodsType;
    /**
     * 电池情况
     */
    private String batteryType;
    /**
     * 海关编码
     */
    private String customsStatusCode;
    /**
     * 装箱方式
     */
    private String  containerMethod;
    /**
     *货物流向
     */
    private String cargoFlowType;
    /**
     * 运输方式
     */
    private String shippingMethod;
    /**
     * 提单类型
     */
    private String billingType;
    /**
     * IATA Code
     */
    private String iataCode;

    /**
     * 交运文件：航协铜牌代码
     */
    private String cataCertifiedSalesAgents;
    //是否支持 航线签单： 1是  ，0否；
    private Integer rountingSign;
    //航线签单支持的服务产品
    private String rountingSignBusinessProduct;
    //预配舱单是否暂存（AE）,1：是，0否，默认否
    private  Boolean mft2201Save;

}
