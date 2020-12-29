package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.OrderInquiry;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * AF 询价单 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-05-26
 */
public interface OrderInquiryService extends IService<OrderInquiry> {

    IPage getPage(Page page, OrderInquiry orderInquiry);

    OrderInquiry view(Integer orderInquiryId);

    OrderInquiry view(String orderInquiryUuid);

    Integer insert(OrderInquiry orderInquiry);

    void modify(OrderInquiry orderInquiry);

    void stop(Integer orderInquiryId);

    List<Map<String, String>> getInquryAgentContactList(String inquryAgentIds);

    void sendQrcode(Map<String, Object> param);

    List<OrderInquiry> exportExcel(OrderInquiry bean);
    Map getInquryAgentDepList(String dep);

    void exportInquiryQuotationExcel(Integer orderInquiryId);

    void createFourYCWhenInquiry();

    Boolean getFourYCWhenInquiry();
}
