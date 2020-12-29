package com.efreight.afbase.controller;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.service.OperationPlanService;
import com.efreight.afbase.utils.ExportExcel;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FieldValUtils;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Controller
@RequestMapping("/operationPlan")
@AllArgsConstructor
@Slf4j
public class OperationPlanController {

    private final OperationPlanService operationPlanService;

    @GetMapping
    @ResponseBody
    public MessageInfo getPage(Page page, OperationPlan operationPlan) {
        try {
            IPage result = operationPlanService.getPage(page, operationPlan);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 根据始发港查询库存
     *
     * @param departure
     * @return
     */
    @GetMapping("/findStorehouse")
    @ResponseBody
    public MessageInfo findStorehouse(String departure) {
        try {
            List<Warehouse> result = operationPlanService.findStorehouse(departure);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 根据始发港查询货栈
     *
     * @param departure
     * @return
     */
    @GetMapping("/findWarehouse")
    @ResponseBody
    public MessageInfo findWarehouse(String departure) {
        try {
            List<Warehouse> result = operationPlanService.findWarehouse(departure);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 打印托书
     *
     * @param request
     * @return
     */
    @PostMapping("/printLetters")
    public MessageInfo printLetters(HttpServletRequest request) {
//        try {
        return MessageInfo.ok(operationPlanService.printLetters(Integer.parseInt(request.getParameter("orgId")), request.getParameter("awbUUIds")));
//            return MessageInfo.ok(operationPlanService.printLetters(Integer.parseInt(bean.get("orgId").toString()),bean.get("awbUUIds").toString()));
//            return MessageInfo.ok(operationPlanService.printLetters(orgId,awbUUIds));
//        }catch (Exception e){
//            return MessageInfo.failed(e.getMessage());
//        }
    }

    @GetMapping("/printLetters1/{awbUuid}")
    @ResponseBody
    public MessageInfo printLetters1( @PathVariable("awbUuid") String awbUuid){
        try{
            String url = operationPlanService.printLetters1(SecurityUtils.getUser().getOrgId(),awbUuid);
            return MessageInfo.ok(url);
        }catch (Exception e){
            return MessageInfo.failed(e.getMessage());
        }
    }

    //货站没有维护托书模板,该始发港下只有一个托书模板可用
    @GetMapping("/printLetters2/{awbUuid}")
    @ResponseBody
    public MessageInfo printLetters2( @PathVariable("awbUuid") String awbUuid){
        try{
            String url = operationPlanService.printLetters2(SecurityUtils.getUser().getOrgId(),awbUuid);
            return MessageInfo.ok(url);
        }catch (Exception e){
            return MessageInfo.failed(e.getMessage());
        }
    }

    //货站没有维护托书模板,该始发港下有多个个托书模板可用，使用选择的一个模板
    @GetMapping("/printLetters3")
    @ResponseBody
    public MessageInfo printLetters3(String awbUuid,String shipperTemplateFile){
        try{
            String url = operationPlanService.printLetters3(SecurityUtils.getUser().getOrgId(),awbUuid,shipperTemplateFile);
            return MessageInfo.ok(url);
        }catch (Exception e){
            return MessageInfo.failed(e.getMessage());
        }
    }

    @PostMapping(value = "/checkLetters")
    @ResponseBody
    public MessageInfo checkLetters(@RequestBody Letters bean) {
        try {
        	return MessageInfo.ok(operationPlanService.checkLetters(bean.getAwbUUIds()));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }

    }

    @PostMapping(value = "/isExistExcelTemplate")
    @ResponseBody
    public MessageInfo isExistExcelTemplate(@RequestBody Letters bean) {
        try {
            return MessageInfo.ok(operationPlanService.isExistExcelTemplate(bean.getAwbUUIds()));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }

    }

    @GetMapping(value = "/selectTemplate")
    @ResponseBody
    public MessageInfo selectTemplate(Page page, OperationPlan bean) {
        return MessageInfo.ok(operationPlanService.selectTemplate(page,bean));
    }

    /**
     * 打印标签单个主单 (暂时废弃)
     *
     * @param request
     * @return
     */
    @PostMapping("/printTag")
    public MessageInfo printTag(HttpServletRequest request) {
        try {
            return MessageInfo.ok(operationPlanService.printTag(Integer.parseInt(request.getParameter("orgId")), request.getParameter("printScope"), request.getParameter("orderUuid"),request.getParameter("slIds")));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 打印标签单个主单 返回路径
     * @return
     */
    @GetMapping(value="/printTagNew")
    @ResponseBody
    public MessageInfo printTagNew(OperationPlan bean) {
        try {
            return MessageInfo.ok(operationPlanService.printTagNew(SecurityUtils.getUser().getOrgId(),bean.getPrintScope(), bean.getOrderUuid(),bean.getSlIds(),bean.getPageName() ));
        } catch (Exception e) {
            e.printStackTrace();
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
     * 打印标签多个主单
     * @return
     */
    @GetMapping(value="/printTagMany")
    @ResponseBody
    public MessageInfo printTagMany(OperationPlan bean) {
        try {
            return MessageInfo.ok(operationPlanService.printTagMany(SecurityUtils.getUser().getOrgId(),bean.getOrderUuid(),bean.getPageName()));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

  
    /**
     * 打印操作计划
     *
     * @param response
     * @return
     */
    @RequestMapping(value = "/printPlan", method = RequestMethod.POST)
    public void printPlan(HttpServletRequest request, HttpServletResponse response, String orderIds) throws IOException {


        List<OperationPlanExcel> list = operationPlanService.queryListForExcle(orderIds);


        //导出日志数据
        ExportExcel<OperationPlanExcel> ex = new ExportExcel<OperationPlanExcel>();
        String[] headers = {"序号","客户代码","客户名称", "主运单号", "客户单号", "航班信息", "件/毛/体/尺", "货物情况", "分单信息", "责任客服", "备注"};
        ex.exportExcel(response, "导出EXCEL", headers, list, "Export");
    }

    /**
     * 通过订单查托书
     * @param orderUuid
     * @param type
     * @return
     */
    @GetMapping("/findShipperLetter/{type}/{orderUuid}")
    @ResponseBody
    public MessageInfo getShipperLetterByOrderUuid(@PathVariable("orderUuid") String orderUuid, @PathVariable("type") String type) {
        try {
            List<AfShipperLetter> list = operationPlanService.getShipperLetterByOrderUuid(orderUuid, type);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 交货托书导出
     *
     * @param awbUuid
     * @return
     */
    @PostMapping("/exportExcel/{awbUuid}")
    public void exportExcel(@PathVariable String awbUuid) {
        try {
            operationPlanService.exportExcel(awbUuid);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 导出Excel
     *
     * @param response
     * @param bean
     * @throws IOException
     */
    @PostMapping("/exportOperationPlanExcel")
    public void exportOperationPlanExcel(HttpServletResponse response, @ModelAttribute("bean") OperationPlan bean) throws IOException {
        //自定义字段
        List<OperationPlan> list = operationPlanService.exportOperationPlanExcel(bean);
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
            if (list != null && list.size() > 0) {
                for (OperationPlan operationPlan : list) {
                    //拼接操作要求
                    String operationRemark = "";
                    if(operationPlan.getOperationRemark() != null && !"".equals(operationPlan.getOperationRemark())){
                        operationRemark += "库内操作：" + operationPlan.getOperationRemark() + String.valueOf((char) 10);
                    }
                    if(operationPlan.getOutfieldRemark() != null && !"".equals(operationPlan.getOutfieldRemark())){
                        operationRemark += "库外操作：" + operationPlan.getOutfieldRemark();
                    }
                    operationPlan.setOperationRemark(operationRemark);
                    //设置责任销售和责任客服
                    if(operationPlan.getSalesName() != null && !"".equals(operationPlan.getSalesName())){
                        operationPlan.setSalesName(operationPlan.getSalesName().split(" ")[0]);
                    }
                    if(operationPlan.getServicerName() != null && !"".equals(operationPlan.getServicerName())){
                        operationPlan.setServicerName(operationPlan.getServicerName().split(" ")[0]);
                    }
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], operationPlan));
                    }
                    listExcel.add(map);
                }
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");

        }
    }
}
