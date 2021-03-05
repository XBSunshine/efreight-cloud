package com.efreight.afbase.service;

import com.efreight.afbase.entity.AfOrder;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * AF 延伸服务 成本 服务类
 * </p>
 *
 * @author qipm
 * @since 2019-10-12
 */
public interface SendService extends IService<AfOrder> {


	Map<String, Object> doEsdDecleare(String hasMwb, String orderUUID, String letterIds);
	Map<String, Object> doEsdDecleareFHL(String orderUUID, String letterIds);
	Map<String, Object> doEawbPreDelete(String AWBNumber, String orderUUID);
	Map<String, Object> doEawbPreDeleteAwb(String AWBNumber, String orderCode);
	Map<String, Object> doEawbPreDeleteFHL(String AWBNumber, String HWBNumber, String orderCode);
	Map<String, Object> doEAWB_WH(String hasMwb, String orderUUID, String letterIds);
	Map<String, Object> doOneDecleare_ForSend(String hasMwb, String orderUUID, String letterIds);
	Map<String, Object> doMft2201_Decleare(String hasMwb, String orderUUID, String letterIds);
	Map<String, Object> doEAWB_AMS(String hasMwb, String orderUUID, String letterIds);
	Map<String, Object> doEAWB_TB(String hasMwb, String orderUUID, String letterIds);
	Map<String, Object> doSendGoodsName(Integer orderId, String orderUUID,String awbNumber);
    Map<String, Object> doMft2201_Delete(String awbNumber,String hawbNumber, String orderCode,String contactTel, String deleteReason);
    Map<String, Object> doSendVEEntry(Integer entryOrderId);
    Map<String, Object> doVEEntryConfirm(Integer entryOrderId);
    Map<String, Object> doSendVEAppoint(Integer entryOrderId);
    Map<String, Object> doVEEntryQuery(Integer entryOrderId);
}
