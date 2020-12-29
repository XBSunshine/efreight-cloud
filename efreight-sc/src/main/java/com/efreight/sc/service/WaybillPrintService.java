package com.efreight.sc.service;

import com.efreight.sc.entity.WaybillPrint;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * CS 订单管理 海运制单 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-08-17
 */
public interface WaybillPrintService extends IService<WaybillPrint> {

    List<Map<String, String>> getList(Integer orderId);

    WaybillPrint view(String orderIdOrMblNumber, String flag);

    void insert(WaybillPrint waybillPrint);

    void modify(WaybillPrint waybillPrint);

    void print(String type, Integer waybillPrintId);
}
