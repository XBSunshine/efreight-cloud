package com.efreight.afbase.controller;


import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.entity.shipping.ShippingBillData;
import com.efreight.afbase.entity.view.OrderDeliveryNoticeCheck;
import com.efreight.afbase.entity.view.OrderTrack;
import com.efreight.afbase.entity.view.OrderTrackShare;
import com.efreight.afbase.service.AfAwbRouteTrackManifestService;
import com.efreight.afbase.service.AfOrderService;
import com.efreight.afbase.service.AfShipperLetterService;
import com.efreight.afbase.utils.FieldValUtils;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.remoteVo.OrderForVL;
import com.efreight.common.security.constant.CommonConstants;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/aforder")
@Slf4j
public class AfOrderController {
    private final AfOrderService service;
    private final AfShipperLetterService letterService;
    private final AfAwbRouteTrackManifestService awbRouteTrackManifestService;

    /**
     * 分页查询信息
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    public MessageInfo getListPage(Page page, AfOrder bean) {
        return MessageInfo.ok(service.getListPage(page, bean));
    }

    @GetMapping("/getTatol")
    public MessageInfo getTatol(AfOrder bean) {
        return MessageInfo.ok(service.getTatol(bean));
    }

    /**
     * 选择客户
     *
     * @return list
     */
    @GetMapping(value = "/selectCoop")
    public MessageInfo selectCoop(Page page, VPrmCoop bean) {
        //运单来源
        return MessageInfo.ok(service.selectCoop(page, bean));
    }

    /**
     * 选择AI客户
     *
     * @return list
     */
    @GetMapping(value = "/selectAICoop")
    public MessageInfo selectAICoop(Page page, VPrmCoop bean) {
        //运单来源
        return MessageInfo.ok(service.selectAICoop(page, bean));
    }

    /**
     * 选择prm客户
     *
     * @return list
     */
    @GetMapping(value = "/selectPrmCoop")
    public MessageInfo selectPrmCoop(Page page, VPrmCoop bean) {
        //运单来源
        return MessageInfo.ok(service.selectPrmCoop(page, bean));
    }

    /**
     * 不同业务范畴查询客户信息
     *
     * @param page
     * @param bean
     * @return
     */
    @GetMapping("/getCoopList")
    public MessageInfo getCoopList(Page page, VPrmCoop bean) {
        try {
            IPage<VPrmCoop> result = service.getCoopList(page, bean);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 供应商不区分 类型
     *
     * @param page
     * @param bean
     * @return
     */
    @GetMapping("/getCoopListNew")
    public MessageInfo getCoopListNew(Page page, VPrmCoop bean) {
        try {
            IPage<VPrmCoop> result = service.getCoopListNew(page, bean);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 添加
     *
     * @param bean 实体
     * @return success/false
     */
    @PreAuthorize("@pms.hasPermission('af-order-add')")
    @PostMapping(value = "/doSave")
    public MessageInfo doSave(@Valid @RequestBody AfOrder bean) {
//		List<Airport> list=airportService.isHaved(bean.getApCode(),"CN");
//		if (list.size()==0) {
//			return MessageInfo.failed("机场代码："+bean.getApCode()+"不存在");
//		}
        try {
            return MessageInfo.ok(service.doSave(bean));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 添加
     *
     * @param bean 实体
     * @return success/false
     */
    @PostMapping(value = "/doSaveAI")
    public MessageInfo doSaveAI(@Valid @RequestBody AfOrder bean) {
        try {
            return MessageInfo.ok(service.doSaveAI(bean));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 修改
     *
     * @param bean 实体
     * @return success/false
     */
    @PreAuthorize("@pms.hasPermission('af-order-edit')")
    @PostMapping(value = "/doUpdate")
    public MessageInfo doUpdate(@Valid @RequestBody AfOrder bean) {
        try {
            return MessageInfo.ok(service.doUpdate(bean));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    @PostMapping(value = "/doUpdateAI")
    public MessageInfo doUpdateAI(@Valid @RequestBody AfOrder bean) {
        try {
            return MessageInfo.ok(service.doUpdateAI(bean));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping(value = {"/view/{id}/{letterId}", "/view/{id}"})
    public MessageInfo<AfOrder> getOrderById(@PathVariable Integer id, @PathVariable(value = "letterId", required = false) Integer letterId) {
        return MessageInfo.ok(service.getOrderById(id, letterId));
    }

    @GetMapping(value = "/selectOrderStatus")
    public MessageInfo selectOrderStatus(String node_name, String order_uuid) {
        return MessageInfo.ok(service.selectOrderStatus(node_name, order_uuid));
    }

    @PreAuthorize("@pms.hasPermission('af-order-uninstall')")
    @PostMapping(value = "/doUninstall")
    public MessageInfo doUninstall(@RequestBody AfOrder bean) {
        return MessageInfo.ok(service.doUninstall(bean));
    }

    //	@PreAuthorize("@pms.hasPermission('af-order-stop')")
    @PreAuthorize("@pms.hasPermission('af-order-uninstall')")
    @PostMapping(value = "/doStop")
    public MessageInfo doStop(@RequestBody AfOrder bean) {
        return MessageInfo.ok(service.doStop(bean));
    }

    @GetMapping("/getOrderMatch/{awbUuid}")
    public MessageInfo getOrderById(@PathVariable String awbUuid) {
        return MessageInfo.ok(service.getOrderMatch(awbUuid));
    }

    @PreAuthorize("@pms.hasPermission('af-order-match')")
    @PostMapping(value = "/doOrderMatch")
    public MessageInfo doOrderMatch(@RequestBody AfOrderMatch bean) {
        return MessageInfo.ok(service.doOrderMatch(bean));
    }

    //收入完成
    @PostMapping(value = "/doIncome")
    public MessageInfo doIncome(@RequestBody AfOrder bean) {
        return MessageInfo.ok(service.doIncome(bean));
    }

    //成本完成
    @PostMapping(value = "/doCost")
    public MessageInfo doCost(@RequestBody AfOrder bean) {
        return MessageInfo.ok(service.doCost(bean));
    }

    //业务完成
    @PostMapping(value = "/doFinish")
    public MessageInfo doFinish(@RequestBody AfOrder bean) {
        return MessageInfo.ok(service.doFinish(bean));
    }

    //取消业务完成
    @PostMapping(value = "/doCancel")
    public MessageInfo doCancel(@RequestBody AfOrder bean) {
        return MessageInfo.ok(service.doCancel(bean));
    }

    //获取订单状态
    @PostMapping(value = "/getOrderStatus")
    public MessageInfo getOrderStatus(@RequestBody AfOrder bean) {
        return MessageInfo.ok(service.getOrderStatus(bean));
    }

    //订单是否有账单
    @PostMapping(value = "/getOrderIncomeStatus")
    public MessageInfo getOrderIncomeStatus(@RequestBody AfOrder bean) {
        return MessageInfo.ok(service.getOrderIncomeStatus(bean));
    }

    /**
     * 打印托书
     *
     * @param
     * @return
     */
//	@PreAuthorize("@pms.hasPermission('af-order-printletter')")
    @PostMapping("/printOrderLetter")
    public MessageInfo printOrderLetter(HttpServletRequest request) {
        return MessageInfo.ok(service.printOrderLetter(Integer.parseInt(request.getParameter("orgId")), request.getParameter("orderUuid"), request.getParameter("userId")));
    }

    @PostMapping("/printOrderLetter1/{orderUuid}/{userId}")
    public MessageInfo printOrderLetter1(@PathVariable String orderUuid, @PathVariable String userId) {
        try {
            String url = service.printOrderLetter1(orderUuid, SecurityUtils.getUser().getOrgId(), userId);
            return MessageInfo.ok(url);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 运单确认件
     *
     * @param orderUuid
     * @return
     */
    @PreAuthorize("@pms.hasPermission('af-order-awbsubmit')")
    @PostMapping("/awbSubmit/{orderUuid}")
    public MessageInfo awbSubmit(@PathVariable String orderUuid) {
        try {
            String url = service.awbSubmit(orderUuid, SecurityUtils.getUser().getOrgId());
            return MessageInfo.ok(url);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 强制关闭
     *
     * @param param
     * @return
     */
    @PreAuthorize("@pms.hasPermission('af-order-forcestop')")
    @PutMapping("/forceStop")
    public MessageInfo forceStop(@RequestBody Map<String, String> param) {
        try {
            service.forceStop(param.get("reason"), param.get("orderUuid"), param.get("businessScope"));
            return MessageInfo.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 通过OrderUuid查询订单详情
     *
     * @param orderUuid
     * @return
     */
    @GetMapping("/queryOrderByOrderUuid/{orderUuid}")
    public MessageInfo queryOrderByOrderUuid(@PathVariable("orderUuid") String orderUuid) {
        try {
            AfOrder afOrder = service.queryOrderByOrderUuid(orderUuid);
            return MessageInfo.ok(afOrder);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping(value = "/getShippingData/{apiType}")
    public MessageInfo getShippingData(@PathVariable("apiType") String apiType) {
        return MessageInfo.ok(service.getShippingData(apiType));
    }

    @PostMapping("/getAwbPrintId/{awbUuid}")
    public MessageInfo getAwbPrintId(@PathVariable String awbUuid) {
        try {
            String awbPrintId = service.getAwbPrintId(awbUuid);
            return MessageInfo.ok(awbPrintId);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    @PostMapping("/getFlightNumber/{awbUuid}")
    public MessageInfo getFlightNumber(@PathVariable String awbUuid) {
        try {
            String awbPrintId = service.getFlightNumber(awbUuid);
            return MessageInfo.ok(awbPrintId);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping(value = "/orderTrack/{orderUUID}")
    public MessageInfo getOrderTrack(@PathVariable("orderUUID") String orderUUID) {
        try {
            OrderTrack orderTrack = service.getOrderTrack(orderUUID);
            return MessageInfo.ok(orderTrack);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    @PostMapping(value = "/orderTrackShare")
    public MessageInfo orderTrackShare(@RequestBody OrderTrackShare orderTrackShare) {
        try {
            EUserDetails user = SecurityUtils.getUser();
            orderTrackShare.setOperator(user.getUserCname());
            orderTrackShare.setOperatorEmail(user.getUserEmail());
            orderTrackShare.setOperatorPhone(user.getPhoneNumber());

            service.orderTrackShareWithEmail(orderTrackShare);
            return MessageInfo.ok();
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 舱单制作
     *
     * @return
     */
    @GetMapping("shippingBillData/{orderUUID}")
    public MessageInfo shippingBillData(@PathVariable("orderUUID") String orderUUID) {
        try {
            ShippingBillData shippingBillData = service.getMasterShippingBill(SecurityUtils.getUser().getOrgId(), orderUUID);
            return MessageInfo.ok(shippingBillData);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 新版发送舱单 (单条/多条)
     *
     * @return
     * @author limr 20200904
     */
    @PostMapping(value = {"sendShippersData/{hasMwb}/{orderUUID}", "sendShippersData/{hasMwb}/{orderUUID}/{letterIds}"})
//    @RequestMapping(value = "/getShippersData", method = RequestMethod.POST)
    public MessageInfo sendShippersData(
            @PathVariable("hasMwb") String hasMwb,
            @PathVariable("orderUUID") String orderUUID,
            @PathVariable(value = "letterIds", required = false) String letterIds) {
        try {
            Map<String, Object> sendCallbackData = service.sendShippersData(hasMwb, orderUUID, letterIds);
            return MessageInfo.ok(sendCallbackData);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 发送舱单校验必填项
     *
     * @return
     */
    @GetMapping(value = {"shippingBillDataCheck/{type}/{hasMwb}/{orderUUID}/{letterIds}", "shippingBillDataCheck/{type}/{hasMwb}/{orderUUID}"})
    public MessageInfo shippingBillDataCheck(
            @PathVariable("type") String type,
            @PathVariable("hasMwb") String hasMwb,
            @PathVariable("orderUUID") String orderUUID,
            @PathVariable(value = "letterIds", required = false) String letterIds) {
        try {
            String shippingBillData = service.getMasterShippingBillCheck(type, hasMwb, orderUUID, letterIds);
            return MessageInfo.ok(shippingBillData);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 发送舱单 校验是否已发送
     *
     * @return
     * @date 20200827
     * @author limr
     */
    @GetMapping("shippingSendCheckHasSend/{orderUUID}")
    public MessageInfo shippingSendCheckHasSend(@PathVariable("orderUUID") String orderUUID) {
        try {
//            MessageInfo.ok(service.shippingSendCheckStatus(orderUUID));
//            String shippingBillData = service.shippingSendCheckStatus(SecurityUtils.getUser().getOrgId(), type, orderUUID);
            return MessageInfo.ok(service.shippingSendCheckHasSend(orderUUID));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 发送舱单 成功后添加日志
     *
     * @return
     * @date 20200827
     * @author limr
     */
    @PostMapping("insertLogAfterSendShipper")
    public MessageInfo insertLogAfterSendShipper(LogBean logbean) {
        try {
            return MessageInfo.ok(service.insertLogAfterSendShipper(logbean));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 运单制作
     *
     * @return
     */
    @GetMapping("waybillData/{orderUUID}")
    public MessageInfo waybillData(@PathVariable("orderUUID") String orderUUID) {
        try {
            ShippingBillData waybillData = service.getWaybillData(SecurityUtils.getUser().getOrgId(), orderUUID);
            return MessageInfo.ok(waybillData);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }


    /**
     * 标签制作
     *
     * @return
     */
    @GetMapping("tagMake/{orderUUID}")
    public MessageInfo tagMake(@PathVariable("orderUUID") String orderUUID) {
        try {
            ShippingBillData waybillData = service.getTagMakeData(SecurityUtils.getUser().getOrgId(), orderUUID);
            return MessageInfo.ok(waybillData);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public MessageInfo statistics() {
        try {
            List<Map<String, Object>> data = service.homeStatistics(SecurityUtils.getUser().getOrgId());
            return MessageInfo.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping("/selectCompany")
    public MessageInfo selectCompany() {
        try {
            List<Map<String, Object>> data = service.selectCompany(SecurityUtils.getUser().getOrgId());
            return MessageInfo.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 导出Excel
     *
     * @param response
     * @param bean
     * @throws IOException
     */
    @PostMapping("/exportExcel")
    public void exportExcel(HttpServletResponse response, @ModelAttribute("bean") AfOrder bean) throws IOException {
        //自定义字段
        if (!StringUtils.isEmpty(bean.getColumnStrs())) {
            List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();

            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(bean.getColumnStrs());
            String[] headers = new String[jsonArr.size()];
            String[] colunmStrs = new String[jsonArr.size()];

            //生成表头跟字段
            if (jsonArr != null && jsonArr.size() > 0) {
                for (int i = 0; i < jsonArr.size(); i++) {
                    JSONObject job = jsonArr.getJSONObject(i);
                    headers[i] = job.getString("label");
                    colunmStrs[i] = job.getString("prop");
                }
            }

            //遍历结果集 区相应字段封装 linkedHashMap后存储list发往工具类
            if ("AE".equals(bean.getBusinessScope())) {
                List<AEOrder> list = service.exportAeExcel(bean);
                if (list != null && list.size() > 0) {
                    for (AEOrder order : list) {
                        LinkedHashMap map = new LinkedHashMap();
                        for (int j = 0; j < colunmStrs.length; j++) {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], order));
                        }
                        listExcel.add(map);
                    }
                }
            } else {
                List<AIOrder> list = service.exportAiExcel(bean);
                if (list != null && list.size() > 0) {
                    for (AIOrder order : list) {
                        LinkedHashMap map = new LinkedHashMap();
                        for (int j = 0; j < colunmStrs.length; j++) {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], order));
                        }
                        listExcel.add(map);
                    }
                }
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");

        } else {
            //默认查询导出
            if ("AE".equals(bean.getBusinessScope())) {
                List<AEOrder> list = service.exportAeExcel(bean);
                //导出日志数据
                ExportExcel<AEOrder> ex = new ExportExcel<AEOrder>();
                String[] headers = {"主单号", "订单号", "操作节点", "收入完成", "成本完成", "预报件数", "预报毛重", "预报体积", "预报计重", "预报密度",
                        "实际件数", "实际毛重", "实际体积", "实际计重", "实际密度", "预报尺寸", "实际尺寸", "航班号",
                        "航班日期", "始发港", "目的港", "航线", "货源地", "客户代码", "客户名称", "供应商代码", "运单来源", "提货服务", "库内操作", "外场服务"
                        , "报关服务", "目的港清关", "目的港派送", "客户单号", "服务产品", "分单数", "货站", "库房"
                        , "中文品名", "货物类型", "电池情况", "责任销售", "责任客服", "责任操作", "订单备注"};
                ex.exportExcel(response, "导出EXCEL", headers, list, "Export");
            } else {
                List<AIOrder> list = service.exportAiExcel(bean);
                //导出日志数据
                ExportExcel<AIOrder> ex = new ExportExcel<AIOrder>();
                String[] headers = {"运单号", "订单号", "操作节点", "收入完成", "成本完成", "客户名称", "航班号", "到港日期"
                        , "始发港", "目的港", "件数", "毛重", "体积", "计重", "外库调单", "库内操作", "报关服务", "派送服务", "客户单号"
                        , "货物流向", "流向备注", "货物类型", "中文品名", "破损记录", "责任销售", "责任客服", "责任操作", "订单备注"};
                ex.exportExcel(response, "导出EXCEL", headers, list, "Export");
            }
        }
    }

    /**
     * 打印业务核算
     *
     * @param businessScope
     * @param orderId
     * @return
     */
    @GetMapping("/printBusinessCalculationBill/{businessScope}/{orderId}")
    public MessageInfo printBusinessCalculationBill(@PathVariable("businessScope") String businessScope, @PathVariable("orderId") Integer orderId) {
        try {
            String path = service.printBusinessCalculationBill(businessScope, orderId, true);
            return MessageInfo.ok(path);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping("checkOrderDeliveryNotice/{orderUuid}/{flag}")
    public MessageInfo checkOrderDeliveryNotice(@PathVariable("orderUuid") String orderUuid, @PathVariable("flag") String flag) {
        try {
            OrderDeliveryNoticeCheck result = service.checkOrderDeliveryNotice(orderUuid, flag);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * SC服务派车单编辑调用-获取订单列表
     *
     * @param orderForVL
     * @return
     */
    @PostMapping("/getOrderListForVL")
    public MessageInfo<List<OrderForVL>> getOrderListForVL(@RequestBody OrderForVL orderForVL) {
        try {
            List<OrderForVL> list = service.getOrderListForVL(orderForVL);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 货物舱单打印
     *
     * @param orderId
     */
    @PostMapping("/airCargoManifestPrint/{orderId}")
    public void airCargoManifestPrint(@PathVariable("orderId") Integer orderId) {
        try {
            service.airCargoManifestPrint(orderId);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }


    /**
     * 操作看板列表
     *
     * @param bean
     * @return
     * @author limr 200200901
     */
    @GetMapping("/getOpreationLookList")
    public MessageInfo getOpreationLookList(AfOrder bean) {
        return MessageInfo.ok(service.getOpreationLookList(bean));
    }

    @GetMapping("/operaLookPage")
    public MessageInfo getOperaLookListPage(Page page, AfOrder bean) {
        return MessageInfo.ok(service.getOperaLookListPage(page, bean));
    }

    /**
     * 编辑仓单保存
     *
     * @param bean 实体
     * @return success/false
     */
    @PostMapping(value = "/saveShippers")
    public MessageInfo saveShippers(@Valid @RequestBody AfOrder bean) {
        try {
            return MessageInfo.ok(service.saveShippers(bean));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * ai编辑仓单保存 20200923
     *
     * @param bean 实体
     * @return success/false
     */
    @PostMapping(value = "/ai/saveShippers")
    public MessageInfo saveAiShippers(@Valid @RequestBody AfShipperLetter bean) {
        try {
            return MessageInfo.ok(letterService.saveAiShippers(bean));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 删除舱单
     *
     * @param orderUUID
     * @param letterId
     * @return
     */
    @PostMapping(value = {"/deleteShipper/{orderUUID}", "/deleteShipper/{orderUUID}/{letterId}"})
    public MessageInfo deleteShipper(
            @PathVariable("orderUUID") String orderUUID,
            @PathVariable(value = "letterId", required = false) String letterId) {
        try {
            return MessageInfo.ok(service.deleteShipper(orderUUID, letterId));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping("getOperationLookTrackList")
    public MessageInfo getOperationLookTrackList(
            @RequestParam("awbNumber") String awbNumber,
            @RequestParam(name = "hawbNumber", required = false) String hawbNumber) {
        try {
            return MessageInfo.ok(awbRouteTrackManifestService.operationLookList(awbNumber, hawbNumber, CommonConstants.BUSINESS_SCOPE.AE));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 导出Excel
     *
     * @param response
     * @param bean
     * @throws IOException
     */
    @PostMapping("/operationLook/exportExcel")
    public void OperationLookExportExcel(HttpServletResponse response, @ModelAttribute("bean") AfOrder bean) {
        try {
            service.exportOperationLookExcel(bean);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    @GetMapping(value = {"/ai/view/{orderId}"})
    public MessageInfo<Map<String, Object>> getAiOrderById(@PathVariable Integer orderId) {
        return MessageInfo.ok(service.getAiOrderById(orderId));
    }

    /**
     * 货物追踪(信息查询
     * @param businessScope 业务域
     * @param awbNumber  主单号 不能为空
     * @param hawbNumber 分单号 可以为空
     * @return
     */
    @GetMapping("cargoTracking")
    public MessageInfo cargoTracking(@RequestParam("businessScope") String businessScope,
                                     @RequestParam("awbNumber") String awbNumber,
                                     @RequestParam(name = "hawbNumber", required = false) String hawbNumber) {
        try{
            OrderTrack orderTrack = this.service.cargoTracking(awbNumber, hawbNumber, businessScope);
            return MessageInfo.ok(orderTrack);
        }catch (Exception e){
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping("checkCargoTrackingQuery/{awbNumber}")
    public MessageInfo checkCargoTrackingQuery(@PathVariable String awbNumber) {
        Map<String, Integer> result = this.service.checkCargoTrackingQuery(awbNumber);
        return MessageInfo.ok(result);
    }
}

