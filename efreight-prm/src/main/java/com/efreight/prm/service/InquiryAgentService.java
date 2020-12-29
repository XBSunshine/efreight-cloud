package com.efreight.prm.service;

import com.efreight.prm.entity.InquiryAgent;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * PRM 询盘代理 服务类
 * </p>
 *
 * @author qipm
 * @since 2020-05-18
 */
public interface InquiryAgentService {

    Map<String, Object> queryList(Integer currentPage, Integer pageSize, Map<String, Object> paramMap);

    Map<String, Object> getInquiryAgentList(Integer currentPage, Integer pageSize, Map<String, Object> paramMap);

    int doSave(InquiryAgent bean);

    InquiryAgent queryById(Map<String, Object> paramMap);

    void doEdit(InquiryAgent bean);

    Map<String, Object> selectCarrierCode(Map<String, Object> paramMap);

    Map<String, Object> selectAirport(Map<String, Object> paramMap);

    Map<String, Object> selectNation(Map<String, Object> paramMap);

    Map<String, Object> selectContacts(Map<String, Object> paramMap);

    List<InquiryAgent> exportExcel(InquiryAgent bean);
}
