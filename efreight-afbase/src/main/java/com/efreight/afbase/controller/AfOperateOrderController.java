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
import com.efreight.afbase.service.AfOperateOrderService;
import com.efreight.afbase.utils.FieldValUtils;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.remoteVo.OrderForVL;
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
@RequestMapping("/afoporder")
@Slf4j
public class AfOperateOrderController {
    private final AfOperateOrderService service;

    /**
     * 分页查询信息
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    public MessageInfo getListPage(Page page, AfOperateOrder bean) {
        return MessageInfo.ok(service.getListPage(page, bean));
    }

    @GetMapping("/getTatol")
    public MessageInfo getTatol(AfOperateOrder bean) {
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
     * 添加
     *
     * @param bean 实体
     * @return success/false
     */
    @PreAuthorize("@pms.hasPermission('af-oporder-add')")
    @PostMapping(value = "/doSave")
    public MessageInfo doSave(@Valid @RequestBody AfOperateOrder bean) {
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
     * 修改
     *
     * @param bean 实体
     * @return success/false
     */
    @PreAuthorize("@pms.hasPermission('af-oporder-edit')")
    @PostMapping(value = "/doUpdate")
    public MessageInfo doUpdate(@Valid @RequestBody AfOperateOrder bean) {
        try {
            return MessageInfo.ok(service.doUpdate(bean));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }


    @GetMapping(value={"/view/{id}/{letterId}","/view/{id}"})
    public MessageInfo<AfOperateOrder> getOrderById(@PathVariable Integer id,@PathVariable(value = "letterId",required = false) Integer letterId) {
        return MessageInfo.ok(service.getOrderById(id,letterId));
    }



    @PreAuthorize("@pms.hasPermission('af-oporder-uninstall')")
    @PostMapping(value = "/doUninstall")
    public MessageInfo doUninstall(@RequestBody AfOperateOrder bean) {
        return MessageInfo.ok(service.doUninstall(bean));
    }

    //	@PreAuthorize("@pms.hasPermission('af-order-stop')")
    @PreAuthorize("@pms.hasPermission('af-oporder-uninstall')")
    @PostMapping(value = "/doStop")
    public MessageInfo doStop(@RequestBody AfOperateOrder bean) {
        return MessageInfo.ok(service.doStop(bean));
    }

    //业务完成
    @PostMapping(value = "/doFinish")
    public MessageInfo doFinish(@RequestBody AfOperateOrder bean) {
        return MessageInfo.ok(service.doFinish(bean));
    }

    //取消业务完成
    @PostMapping(value = "/doCancel")
    public MessageInfo doCancel(@RequestBody AfOperateOrder bean) {
        return MessageInfo.ok(service.doCancel(bean));
    }

    //获取订单状态
    @PostMapping(value = "/getOrderStatus")
    public MessageInfo getOrderStatus(@RequestBody AfOperateOrder bean) {
        return MessageInfo.ok(service.getOrderStatus(bean));
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
    @PreAuthorize("@pms.hasPermission('af-oporder-awbsubmit')")
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
    @PreAuthorize("@pms.hasPermission('af-oporder-forcestop')")
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
            AfOperateOrder afOrder = service.queryOrderByOrderUuid(orderUuid);
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
     * 新版发送舱单 获取舱单数据 (单条/多条)
     * @author limr 20200904
     * @return
     */
    @PostMapping(value={"getShippersData/{hasMwb}/{orderUUID}","getShippersData/{hasMwb}/{orderUUID}/{letterIds}"})
//    @RequestMapping(value = "/getShippersData", method = RequestMethod.POST)
    public MessageInfo getShippersData(
            @PathVariable("hasMwb") String hasMwb,
            @PathVariable("orderUUID") String orderUUID,
            @PathVariable(value = "letterIds",required = false) String letterIds) {
        try {
            ShippingBillData shippingBillData = service.getShippersData(hasMwb, orderUUID, letterIds);
            return MessageInfo.ok(shippingBillData);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 舱单制作校验必填项
     *
     * @return
     */
    @GetMapping(value={"shippingBillDataCheck/{type}/{hasMwb}/{orderUUID}/{letterIds}","shippingBillDataCheck/{type}/{hasMwb}/{orderUUID}"})
    public MessageInfo shippingBillDataCheck(
            @PathVariable("type") String type,
            @PathVariable("hasMwb") String hasMwb,
            @PathVariable("orderUUID") String orderUUID,
            @PathVariable(value="letterIds",required = false) String letterIds) {
        try {
            String shippingBillData = service.getMasterShippingBillCheck(type, hasMwb, orderUUID, letterIds);
            return MessageInfo.ok(shippingBillData);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 发送舱单 成功后添加日志
     * @date 20200827
     * @author limr
     * @return
     */
    @PostMapping("insertAfLog")
    public MessageInfo insertAfLog(LogBean logbean) {
        try {
            return MessageInfo.ok(service.insertAfLog(logbean));
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




    /**
     * 导出Excel
     *
     * @param response
     * @param bean
     * @throws IOException
     */
    @PostMapping("/exportExcel")
    public void exportExcel(HttpServletResponse response, @ModelAttribute("bean") AfOperateOrder bean) throws IOException,Exception {
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
                List<AEOperateOrder> list = service.exportAeExcel(bean);
                if (list != null && list.size() > 0) {
                    for (AEOperateOrder order : list) {
                        LinkedHashMap map = new LinkedHashMap();
                        for (int j = 0; j < colunmStrs.length; j++) {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], order));
                        }
                        listExcel.add(map);
                    }
                }
            } else {
                throw new Exception("此订单不是出口业务，调用接口错误！");
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");

        } else {
            if (!"AE".equals(bean.getBusinessScope())) {
                throw new Exception("此订单不是出口业务，调用接口错误！");
            }
            List<AEOperateOrder> list = service.exportAeExcel(bean);
            //导出日志数据
            ExportExcel<AEOperateOrder> ex = new ExportExcel<AEOperateOrder>();
            String[] headers = {"主单号", "订单号", "操作节点", "出口类型", "预报件数", "预报毛重", "预报体积", "预报计重", "预报密度",
                    "实际件数", "实际毛重", "实际体积", "实际计重", "实际密度", "货站件数", "货站毛重","航班号",
                    "航班日期", "始发港", "目的港", "货源地","分单数", "货站", "中文品名", "货物类型", "电池情况", "责任操作", "订单备注"};
            ex.exportExcel(response, "导出EXCEL", headers, list, "Export");
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
    public MessageInfo checkOrderDeliveryNotice(@PathVariable("orderUuid") String orderUuid,@PathVariable("flag")String flag) {
        try {
            OrderDeliveryNoticeCheck result = service.checkOrderDeliveryNotice(orderUuid,flag);
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

    @PostMapping("/exitCard")
    public void exitCard(@RequestBody AfOperateOrder bean) {
    	try {
    		service.exitCard(bean);
    	} catch (Exception e) {
    		log.error(e.getMessage());
    		throw new RuntimeException(e);
    	}
    }
    @PostMapping("/doPrintGoodsName")
    public void doPrintGoodsName(@RequestBody AfOperateOrder bean) {
    	try {
    		service.doPrintGoodsName(bean);
    	} catch (Exception e) {
    		log.error(e.getMessage());
    		throw new RuntimeException(e);
    	}
    }
    /**
     * 编辑舱单显示
     * @author limr 200200901
     * @param letterId
     * @return
     */
//    @GetMapping("/shipperListView/{orderId}/{letterId}")
//    public MessageInfo<List<Map<String, Object>>> getShipperByLetterId(@PathVariable Integer orderId,@PathVariable Integer letterId) {
//        return MessageInfo.ok(service.getShipperByLetterId(orderId,letterId));
//    }


    /**
     * 编辑仓单保存
     *
     * @param bean 实体
     * @return success/false
     */
    @PostMapping(value = "/saveShippers")
    public MessageInfo saveShippers(@Valid @RequestBody AfOperateOrder bean) {
        try {
            return MessageInfo.ok(service.saveShippers(bean));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 可以删除的接口
     */

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
     * 操作看板列表
     * @author limr 200200901
     * @param bean
     * @return
     */
    @GetMapping("/getOpreationLookList")
    public MessageInfo getOpreationLookList(AfOrder bean) {
        return MessageInfo.ok(service.getOpreationLookList(bean));
    }
    @GetMapping("/operaLookPage")
    public MessageInfo getOperaLookListPage(Page page, AfOrder bean) {
        return MessageInfo.ok(service.getOperaLookListPage(page, bean));
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

    @GetMapping(value = "/selectOrderStatus")
    public MessageInfo selectOrderStatus(String node_name, String order_uuid) {
        return MessageInfo.ok(service.selectOrderStatus(node_name, order_uuid));
    }
}
