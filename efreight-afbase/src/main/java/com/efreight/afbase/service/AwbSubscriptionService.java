package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.AwbSubscription;
import com.efreight.afbase.entity.view.Subscribe;
import com.efreight.afbase.entity.view.SubscribeVO;
import com.efreight.common.security.vo.OrgServiceMealConfigVo;

import java.util.List;

/**
 * <p>
 * AF 运单号 我的订阅 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-06-29
 */
public interface AwbSubscriptionService extends IService<AwbSubscription> {

    List<AwbSubscription> getList(String businessScope);

    Boolean getRoute(String awbNumber);

    void deleteAwbSubscription(Integer awbSubscriptionId);

    /**
     * 货物追踪订阅
     * @param subscribe 数据Bean
     * @return 返回是否首次订阅 true:是， false:否
     */
    boolean cargoTrackingSubscribe(Subscribe subscribe);

    /**
     * 企业附加服务信息
     * @param orgId 企业ID
     * @param serviceType 服务类型
     * @return
     */
    OrgServiceMealConfigVo orgAdditionalService(Integer orgId, String serviceType);

    /**
     * 检查是否可以进行货物追踪的查询
     * @param subscribe
     */
    void checkOrgAdditionalService(Subscribe subscribe);

    /**
     * 更新企业附加服容量
     * @param orgId
     * @param serviceType
     */
    void updateAdditionalServiceRemaining(Integer orgId, String serviceType);

    /**
     * 分页查询订阅明细
     * @param page
     * @param date 日期 格式为：yyyy-MM
     * @return
     */
    IPage<SubscribeVO> pageSubscribe(Page page, Integer orgId, String date);


    /**
     * 导出数据的查询
     * @param orgId
     * @param date
     * @return
     */
    List<SubscribeVO> exportSubscribe(Integer orgId, String date);
}
