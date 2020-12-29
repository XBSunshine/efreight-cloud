package com.efreight.afbase.service;

import com.efreight.afbase.entity.OrderInquiryQuotation;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * AF 询价：报价单明细 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-05-28
 */
public interface OrderInquiryQuotationService extends IService<OrderInquiryQuotation> {

    void saveInquiryQuotation(List<OrderInquiryQuotation> orderInquiryQuotations);

    void modify(List<OrderInquiryQuotation> orderInquiryQuotations);
}
