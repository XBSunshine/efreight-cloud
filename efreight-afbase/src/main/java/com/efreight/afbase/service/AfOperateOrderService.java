package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.entity.shipping.ShippingBillData;
import com.efreight.afbase.entity.view.OrderDeliveryNotice;
import com.efreight.afbase.entity.view.OrderDeliveryNoticeCheck;
import com.efreight.afbase.entity.view.OrderTrack;
import com.efreight.afbase.entity.view.OrderTrackShare;
import com.efreight.common.remoteVo.OrderForVL;
import com.itextpdf.text.DocumentException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface AfOperateOrderService extends IService<AfOperateOrder> {

    IPage<AfOperateOrder> getListPage(Page page, AfOperateOrder bean);
    List<AfOperateOrder> getTatol(AfOperateOrder bean);
    List<AEOperateOrder> exportAeExcel(AfOperateOrder bean);
    IPage<VPrmCoop> selectCoop(Page page, VPrmCoop bean);
    IPage<VPrmCoop> selectPrmCoop(Page page, VPrmCoop bean);
    AfOperateOrder doSave(AfOperateOrder bean);

    Boolean doUpdate(AfOperateOrder bean);

    Boolean selectOrderStatus(String node_name,String order_uuid);
    AfOperateOrder getOrderById(Integer orderId,Integer letterId);

    Boolean doUninstall(AfOperateOrder bean);
    List<Integer> getOrderStatus(AfOperateOrder bean);
    Boolean doFinish(AfOperateOrder bean);
    Boolean doCancel(AfOperateOrder bean);
    Boolean doStop(AfOperateOrder bean);

    Boolean printOrderLetter(Integer orgId,String orderUuid,String userId);

    String printOrderLetter1(String orderUuid, Integer orgId, String userId) throws IOException, DocumentException;

    String awbSubmit(String orderUuid, Integer orgId) throws IOException, DocumentException;

    void forceStop(String reason, String orderUuid,String businessScope);

    AfOperateOrder queryOrderByOrderUuid(String orderUuid);

    Boolean getShippingData(String apiType);

    String getAwbPrintId(String awbUuid);

    String getFlightNumber(String awbUuid);

    /**
     * 根据订单UUID查询 订单轨迹信息
     * @param orderUUID 订单UUID
     * @return
     */
    OrderTrack getOrderTrack(String orderUUID);

    /**
     * 根据订单UUID查询数据
     * @param orderUUID
     * @return
     */
    AfOperateOrder getOrderByUUID(String orderUUID);

    /**
     * 使用邮件方式分享订单轨迹
     * @param orderTrackShare 订单轨迹分享信息
     */
    void orderTrackShareWithEmail(OrderTrackShare orderTrackShare) throws Exception;

    /**
     * 主舱单制作
     * @param orgId 企业ID
     * @param orderUUID
     * @return
     */
    ShippingBillData getMasterShippingBill(Integer orgId, String orderUUID) throws Exception;
    ShippingBillData getShippersData(String hasMwb, String orderUUID, String letterIds) throws Exception;

    String print(OrderLetters orderLetters, boolean flag);

    /**
     * 运单舱单制作
     * @param orgId 企业ID
     * @param orderUUID
     * @return
     */
    ShippingBillData getWaybillData(Integer orgId, String orderUUID) throws Exception;


    /**
     * 标签制作
     * @param orgId
     * @param orderUUID
     * @return
     * @throws Exception
     */
    ShippingBillData getTagMakeData(Integer orgId, String orderUUID) throws Exception;

    /**
     * 首页订单统计信息
     * @param orgId 企业ID
     * @return
     */
    List<Map<String, Object>> homeStatistics(Integer orgId);
    List<Map<String, Object>> selectCompany(Integer orgId);

    String getOrderCostStatusForAF(Integer orderId);
    String getOrderCostStatusForSC(Integer orderId);
    String getOrderCostStatusForLC(Integer orderId);
    String getOrderCostStatusForIO(Integer orderId);
    String getOrderCostStatusForTC(Integer orderId);
    void updateOrderCostStatusForSC(Integer orderId);

    String printBusinessCalculationBill(String businessScope, Integer orderId,Boolean ifReplace);

    /**
     * 查询订单送货通知信息
     * @param orderUuid 订单UUID
     * @param flag
     * @return
     */
    OrderDeliveryNotice getOrderDeliveryNotice(String orderUuid, String flag);

    /**
     *查询是否可以进行送货通知的跳转
     * @param orderUUID 订单UUID
     * @param flag
     * @return
     */
    OrderDeliveryNoticeCheck checkOrderDeliveryNotice(String orderUUID, String flag);

    IPage<VPrmCoop> getCoopList(Page page, VPrmCoop bean);
    IPage<VPrmCoop> getCoopListNew(Page page, VPrmCoop bean);

    void updateOrderCostStatusForTC(Integer orderId);

    List<OrderForVL> getOrderListForVL(OrderForVL orderForVL);

    String getMasterShippingBillCheck(String type, String hasMwb, String orderUUID, String letterIds);

    void airCargoManifestPrint(Integer orderId);
    void exitCard(AfOperateOrder bean);
    void doPrintGoodsName(AfOperateOrder bean);

    Boolean shippingSendCheckHasSend(String orderUUID);

    Boolean insertLogAfterSendShipper(LogBean logbean);

    //	List<Map<String, Object>> getShipperByLetterId(Integer orderId,Integer letterId);
    List<Map<String,Object>> getOpreationLookList(AfOrder bean);

    IPage<Map<String, Object>> getOperaLookListPage(Page page, AfOrder bean);

    Boolean saveShippers(AfOperateOrder bean);

    Map<String, Object> sendShippersData(String hasMwb, String orderUUID, String letterIds) throws Exception;

    Map<String,Object> deleteShipper(String orderUUID, String letterId);

    /**
     * 保存追踪信息
     * @param awbNumber 主单号
     * @param hawNumber 分单号
     * @param businessScope 业务域
     */
    void saveRouteInfo(String awbNumber, String hawNumber, String businessScope);

    /**
     * 根据主单号和分单号 查询订单轨迹信息:
     * 1，判断是否存在与主单号和订单号相对应的订单信息，如果不存在，只查询舱单信息和轨迹信息，如果存储则返回OrderTrack对象
     * 2，当分单号为空里，可能会存储多条订单信息，任意取一条
     * @param awbNumber 主单号
     * @param hawbNumber 分单号
     * @param businessScope 业务类型 @see CommonConstant.BUSINESS_SCOPE
     * @return
     */
    OrderTrack getOrderTrack(String awbNumber, String hawbNumber, String businessScope);

    Map<String,Object> getAiOrderById(Integer orderId);

    /**
     * 货物追踪 检查主单
     * @param awbNumber
     * @return
     */
    Map<String, Integer> checkCargoTrackingQuery(String awbNumber);

    /**
     * 货物追踪
     * 1，验证是否已经达到用量，如果达到则不查询
     * 2，查询轨迹信息
     * @param awbNumber 主单号
     * @param hawbNumber 分单号
     * @param businessScope 业务域
     * @return
     */
    OrderTrack cargoTracking(String awbNumber, String hawbNumber, String businessScope);
    Boolean insertAfLog(LogBean logbean);
}
