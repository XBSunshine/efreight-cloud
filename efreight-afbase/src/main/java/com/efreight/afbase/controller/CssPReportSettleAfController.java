package com.efreight.afbase.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import com.efreight.afbase.entity.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.procedure.CssPReportSettleAfProcedure;
import com.efreight.afbase.service.CssPReportSettleAfService;
import com.efreight.afbase.utils.FieldValUtils;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.security.util.MessageInfo;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/cssPReportSettleAf")
public class CssPReportSettleAfController {

    private final CssPReportSettleAfService service;

    /**
     * 分页查询信息
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @PostMapping(value = "/page")
    public MessageInfo getListPage(Page page, CssPReportSettleAfProcedure bean) {
        bean.setReportType("settle");
        HashMap map = service.getListPage(page, bean);

        return MessageInfo.ok(map);
    }

    /**
     * 销售结算报表分页查询信息
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @PostMapping(value = "/saleSettle")
    public MessageInfo getSaleListPage(Page page, CssPReportSettleAfProcedure bean) {
        bean.setReportType("sale");
        HashMap map = service.getListPage(page, bean);

        return MessageInfo.ok(map);
    }

    /**
     * 航线结算报表分页查询信息
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @PostMapping(value = "/airlineSettle")
    public MessageInfo getAirlineListPage(Page page, CssPReportSettleAfProcedure bean) {
        bean.setReportType("airline");
        HashMap map = service.getListPage(page, bean);

        return MessageInfo.ok(map);
    }

    /**
     * 导出Excel
     *
     * @param
     * @param response
     * @param bean
     * @throws IOException
     */

    @PostMapping(value = "/exportExcel")
    public void exportExcel(HttpServletResponse response, CssPReportSettleAfProcedure bean) throws IOException {

        List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
        String[] colunmStrs = null;
        String[] headers = null;
        if (!StringUtils.isEmpty(bean.getColumnStrs())) {
            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(bean.getColumnStrs());
            int num = jsonArr.size() + 1;
            if (bean.getShowConstituteFlag() == true) {
                num += 23;
            }
            headers = new String[num];
            colunmStrs = new String[num];
            headers[0] = "序号";
            colunmStrs[0] = "num";

            //生成表头跟字段
            if (jsonArr != null && jsonArr.size() > 0) {
                int numStr = 1;
                for (int i = 0; i < jsonArr.size(); i++) {
                    JSONObject job = jsonArr.getJSONObject(i);
                    if ("constitute".equals(job.getString("prop"))) {
                        continue;
                    }
                    headers[numStr] = job.getString("label");
                    colunmStrs[numStr] = job.getString("prop");

                    if ("order_status".equals(job.getString("prop"))) {
                        colunmStrs[numStr] = "orderStatus";
                    }
                    if ("income_status".equals(job.getString("prop"))) {
                        colunmStrs[numStr] = "incomeStatus";
                    }
                    if ("cost_status".equals(job.getString("prop"))) {
                        colunmStrs[numStr] = "costStatus";
                    }
                    numStr++;
                }
            }
            if (bean.getShowConstituteFlag() == true) {
                headers[headers.length - 24] = "干线收入";
                colunmStrs[colunmStrs.length - 24] = "mainRoutingIncome";
                headers[headers.length - 23] = "干线成本";
                colunmStrs[colunmStrs.length - 23] = "mainRoutingCost";
                headers[headers.length - 22] = "干线毛利";
                colunmStrs[colunmStrs.length - 22] = "mainRouting";
                headers[headers.length - 21] = "支线收入";
                colunmStrs[colunmStrs.length - 21] = "feederIncome";
                headers[headers.length - 20] = "支线成本";
                colunmStrs[colunmStrs.length - 20] = "feederCost";
                headers[headers.length - 19] = "支线毛利";
                colunmStrs[colunmStrs.length - 19] = "feeder";
                headers[headers.length - 18] = "操作收入";
                colunmStrs[colunmStrs.length - 18] = "operationIncome";
                headers[headers.length - 17] = "操作成本";
                colunmStrs[colunmStrs.length - 17] = "operationCost";
                headers[headers.length - 16] = "操作毛利";
                colunmStrs[colunmStrs.length - 16] = "operation";
                headers[headers.length - 15] = "包装收入";
                colunmStrs[colunmStrs.length - 15] = "packagingIncome";
                headers[headers.length - 14] = "包装成本";
                colunmStrs[colunmStrs.length - 14] = "packagingCost";
                headers[headers.length - 13] = "包装毛利";
                colunmStrs[colunmStrs.length - 13] = "packaging";
                headers[headers.length - 12] = "仓储收入";
                colunmStrs[colunmStrs.length - 12] = "storageIncome";
                headers[headers.length - 11] = "仓储成本";
                colunmStrs[colunmStrs.length - 11] = "storageCost";
                headers[headers.length - 10] = "仓储毛利";
                colunmStrs[colunmStrs.length - 10] = "storage";
                headers[headers.length - 9] = "快递收入";
                colunmStrs[colunmStrs.length - 9] = "postageIncome";
                headers[headers.length - 8] = "快递成本";
                colunmStrs[colunmStrs.length - 8] = "postageCost";
                headers[headers.length - 7] = "快递毛利";
                colunmStrs[colunmStrs.length - 7] = "postage";
                headers[headers.length - 6] = "关检收入";
                colunmStrs[colunmStrs.length - 6] = "clearanceIncome";
                headers[headers.length - 5] = "关检成本";
                colunmStrs[colunmStrs.length - 5] = "clearanceCost";
                headers[headers.length - 4] = "关检毛利";
                colunmStrs[colunmStrs.length - 4] = "clearance";
                headers[headers.length - 3] = "数据收入";
                colunmStrs[colunmStrs.length - 3] = "exchangeIncome";
                headers[headers.length - 2] = "数据成本";
                colunmStrs[colunmStrs.length - 2] = "exchangeCost";
                headers[headers.length - 1] = "数据毛利";
                colunmStrs[colunmStrs.length - 1] = "exchange";
            }
        }
        
      if("TE".equals(bean.getBusinessScope()) || "TI".equals(bean.getBusinessScope()) || "LC".equals(bean.getBusinessScope())|| "IO".equals(bean.getBusinessScope())) {
        	List<CssPReportSettleExcel> list = service.getListForExcelNew(bean);
        	 //自定义字段拼装结果数据
            if (!StringUtils.isEmpty(bean.getColumnStrs())) {
                if (list != null && list.size() > 0) {
                    list.get(list.size() - 1).setBusinessScope("");
                    int i = 1;
                    for (CssPReportSettleExcel excel : list) {
                        LinkedHashMap map = new LinkedHashMap();
                        for (int j = 0; j < colunmStrs.length; j++) {
                            if (j == 0) {
                                if (i == list.size()) {
                                    map.put(colunmStrs[j], "合计:");
                                } else {
                                    map.put(colunmStrs[j], i + "");
                                }
                            } else {
                                map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], excel));
                            }
                        }
                        listExcel.add(map);
                        i++;
                    }
                }
                ExcelExportUtils u = new ExcelExportUtils();
                u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");
            }
        	
        }else if ("AE".equals(bean.getBusinessScope())) {
            //自定义字段拼装结果数据
            List<CssPReportSettleAfExcel> list = service.getListForExcel(bean);
            if (!StringUtils.isEmpty(bean.getColumnStrs())) {
                if (list != null && list.size() > 0) {
                    list.get(list.size() - 1).setBusinessScope("");
                    int i = 1;
                    for (CssPReportSettleAfExcel excel : list) {
                        LinkedHashMap map = new LinkedHashMap();
                        for (int j = 0; j < colunmStrs.length; j++) {
                            if (j == 0) {
                                if (i == list.size()) {
                                    map.put(colunmStrs[j], "合计:");
                                } else {
                                    map.put(colunmStrs[j], i + "");
                                }
                            } else {
                                map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], excel));
                            }
                        }
                        listExcel.add(map);
                        i++;
                    }
                }
                ExcelExportUtils u = new ExcelExportUtils();
                u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");
            } else {
                if (StrUtil.isBlank(bean.getFinancialDateStart()) && StrUtil.isBlank(bean.getFinancialDateEnd())) {

                    if(bean.getShowConstituteFlag() == true){
                        List<CssPReportSettleAfExcelTwo> listTwo = list.stream().map(a -> {
                            CssPReportSettleAfExcelTwo b = new CssPReportSettleAfExcelTwo();
                            BeanUtils.copyProperties(a, b);
                            return b;
                        }).collect(Collectors.toList());
                        headers = new String[]{"业务范畴", "订单号", "客户代码", "客户名称", "主单号", "客户单号", "订单状态", "应收情况", "应付情况", "供应商代码", "运单来源", "始发港", "目的港", "中转港1", "中转港2","货源地"
                                , "航班号", "航线", "开航日期", "服务产品", "责任销售", "销售部门", "责任客服", "工作组", "责任航线", "货物类型", "件数", "毛重", "体积", "计重", "应收金额 (本币)", "应付金额（本币）", "毛利（本币）", "单公斤毛利", "干线单公斤毛利", "干线收入", "干线成本", "干线毛利", "支线收入", "支线成本", "支线毛利", "操作收入", "操作成本", "操作毛利", "包装收入", "包装成本", "包装毛利", "仓储收入", "仓储成本", "仓储毛利", "快递收入", "快递成本", "快递毛利", "关检收入", "关检成本", "关检毛利", "数据收入", "数据成本", "数据毛利"};
                        ExportExcel<CssPReportSettleAfExcelTwo> ex = new ExportExcel<CssPReportSettleAfExcelTwo>();
                        ex.exportExcel(response, "导出EXCEL", headers, listTwo, "结算报表Export");
                    }else{
                        List<CssPReportSettleAfExcelTwoForConstitute> listTwo = list.stream().map(a -> {
                            CssPReportSettleAfExcelTwoForConstitute b = new CssPReportSettleAfExcelTwoForConstitute();
                            BeanUtils.copyProperties(a, b);
                            return b;
                        }).collect(Collectors.toList());
                        headers = new String[]{"业务范畴", "订单号", "客户代码", "客户名称", "主单号", "客户单号", "订单状态", "应收情况", "应付情况", "供应商代码", "运单来源", "始发港", "目的港", "中转港1", "中转港2","货源地"
                                , "航班号", "航线", "开航日期", "服务产品", "责任销售", "销售部门", "责任客服", "工作组", "责任航线", "货物类型", "件数", "毛重", "体积", "计重", "应收金额 (本币)", "应付金额（本币）", "毛利（本币）", "单公斤毛利", "干线单公斤毛利"};
                        ExportExcel<CssPReportSettleAfExcelTwoForConstitute> ex = new ExportExcel<CssPReportSettleAfExcelTwoForConstitute>();
                        ex.exportExcel(response, "导出EXCEL", headers, listTwo, "结算报表Export");
                    }

                } else {
                    if(bean.getShowConstituteFlag() == true){
                        headers = new String[]{"业务范畴", "财务日期", "订单号", "客户代码", "客户名称", "主单号", "客户单号", "订单状态", "应收情况", "应付情况", "供应商代码", "运单来源", "始发港", "目的港", "中转港1", "中转港2","货源地"
                                , "航班号", "航线", "开航日期", "服务产品", "责任销售", "销售部门", "责任客服", "工作组", "责任航线", "货物类型", "件数", "毛重", "体积", "计重", "应收金额 (本币)", "应付金额（本币）", "毛利（本币）", "单公斤毛利", "干线单公斤毛利", "干线收入", "干线成本", "干线毛利", "支线收入", "支线成本", "支线毛利", "操作收入", "操作成本", "操作毛利", "包装收入", "包装成本", "包装毛利", "仓储收入", "仓储成本", "仓储毛利", "快递收入", "快递成本", "快递毛利", "关检收入", "关检成本", "关检毛利", "数据收入", "数据成本", "数据毛利"};
                        ExportExcel<CssPReportSettleAfExcel> ex = new ExportExcel<CssPReportSettleAfExcel>();
                        ex.exportExcel(response, "导出EXCEL", headers, list, "结算报表Export");
                    }else{
                        List<CssPReportSettleAfExcelForConstitute> listTwo = list.stream().map(a -> {
                            CssPReportSettleAfExcelForConstitute b = new CssPReportSettleAfExcelForConstitute();
                            BeanUtils.copyProperties(a, b);
                            return b;
                        }).collect(Collectors.toList());
                        headers = new String[]{"业务范畴", "财务日期", "订单号", "客户代码", "客户名称", "主单号", "客户单号", "订单状态", "应收情况", "应付情况", "供应商代码", "运单来源", "始发港", "目的港", "中转港1", "中转港2","货源地"
                                , "航班号", "航线", "开航日期", "服务产品", "责任销售", "销售部门", "责任客服", "工作组", "责任航线", "货物类型", "件数", "毛重", "体积", "计重", "应收金额 (本币)", "应付金额（本币）", "毛利（本币）", "单公斤毛利", "干线单公斤毛利"};
                        ExportExcel<CssPReportSettleAfExcelForConstitute> ex = new ExportExcel<CssPReportSettleAfExcelForConstitute>();
                        ex.exportExcel(response, "导出EXCEL", headers, listTwo, "结算报表Export");
                    }
                }
            }
        } else if ("AI".equals(bean.getBusinessScope())) {
            List<CssPReportSettleAfExcelAI> list = service.getListForExcelAI(bean);
            //自定义字段拼装结果数据
            if (!StringUtils.isEmpty(bean.getColumnStrs())) {
                if (list != null && list.size() > 0) {
                    list.get(list.size() - 1).setBusinessScope("");
                    int i = 1;
                    for (CssPReportSettleAfExcelAI excel : list) {
                        LinkedHashMap map = new LinkedHashMap();
                        for (int j = 0; j < colunmStrs.length; j++) {
                            if (j == 0) {
                                if (i == list.size()) {
                                    map.put(colunmStrs[j], "合计:");
                                } else {
                                    map.put(colunmStrs[j], i + "");
                                }
                            } else {
                                map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], excel));
                            }
                        }
                        listExcel.add(map);
                        i++;
                    }
                }
                ExcelExportUtils u = new ExcelExportUtils();
                u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");
            } else {
                if (StrUtil.isBlank(bean.getFinancialDateStart()) && StrUtil.isBlank(bean.getFinancialDateEnd())) {
                    if(bean.getShowConstituteFlag() == true){
                        List<CssPReportSettleAfExcelTwoAI> listTwo = list.stream().map(a -> {
                            CssPReportSettleAfExcelTwoAI b = new CssPReportSettleAfExcelTwoAI();
                            BeanUtils.copyProperties(a, b);
                            return b;
                        }).collect(Collectors.toList());
                        headers = new String[]{"业务范畴", "订单号", "客户代码", "客户名称", "主单号", "客户单号", "订单状态", "应收情况", "应付情况", "始发港", "目的港"
                                , "航班号", "航线", "到港日期", "责任销售", "销售部门", "责任客服", "工作组", "货物类型", "件数", "毛重", "体积", "计重", "应收金额 (本币)", "应付金额（本币）", "毛利（本币）", "单公斤毛利", "干线单公斤毛利", "干线收入", "干线成本", "干线毛利", "支线收入", "支线成本", "支线毛利", "操作收入", "操作成本", "操作毛利", "包装收入", "包装成本", "包装毛利", "仓储收入", "仓储成本", "仓储毛利", "快递收入", "快递成本", "快递毛利", "关检收入", "关检成本", "关检毛利", "数据收入", "数据成本", "数据毛利"};
                        ExportExcel<CssPReportSettleAfExcelTwoAI> ex = new ExportExcel<CssPReportSettleAfExcelTwoAI>();
                        ex.exportExcel(response, "导出EXCEL", headers, listTwo, "结算报表Export");
                    }else{
                        List<CssPReportSettleAfExcelTwoAIForConstitute> listTwo = list.stream().map(a -> {
                            CssPReportSettleAfExcelTwoAIForConstitute b = new CssPReportSettleAfExcelTwoAIForConstitute();
                            BeanUtils.copyProperties(a, b);
                            return b;
                        }).collect(Collectors.toList());
                        headers = new String[]{"业务范畴", "订单号", "客户代码", "客户名称", "主单号", "客户单号", "订单状态", "应收情况", "应付情况", "始发港", "目的港"
                                , "航班号", "航线", "到港日期", "责任销售", "销售部门", "责任客服", "工作组", "货物类型", "件数", "毛重", "体积", "计重", "应收金额 (本币)", "应付金额（本币）", "毛利（本币）", "单公斤毛利", "干线单公斤毛利"};
                        ExportExcel<CssPReportSettleAfExcelTwoAIForConstitute> ex = new ExportExcel<CssPReportSettleAfExcelTwoAIForConstitute>();
                        ex.exportExcel(response, "导出EXCEL", headers, listTwo, "结算报表Export");
                    }
                } else {
                    if(bean.getShowConstituteFlag() == true){
                        headers = new String[]{"业务范畴", "财务日期", "订单号", "客户代码", "客户名称", "主单号", "客户单号", "订单状态", "应收情况", "应付情况", "始发港", "目的港"
                                , "航班号", "航线", "到港日期", "责任销售", "销售部门", "责任客服", "工作组", "货物类型", "件数", "毛重", "体积", "计重", "应收金额 (本币)", "应付金额（本币）", "毛利（本币）", "单公斤毛利", "干线单公斤毛利", "干线收入", "干线成本", "干线毛利", "支线收入", "支线成本", "支线毛利", "操作收入", "操作成本", "操作毛利", "包装收入", "包装成本", "包装毛利", "仓储收入", "仓储成本", "仓储毛利", "快递收入", "快递成本", "快递毛利", "关检收入", "关检成本", "关检毛利", "数据收入", "数据成本", "数据毛利"};
                        ExportExcel<CssPReportSettleAfExcelAI> ex = new ExportExcel<CssPReportSettleAfExcelAI>();
                        ex.exportExcel(response, "导出EXCEL", headers, list, "结算报表Export");
                    }else{
                        List<CssPReportSettleAfExcelAIForConstitute> listTwo = list.stream().map(a -> {
                            CssPReportSettleAfExcelAIForConstitute b = new CssPReportSettleAfExcelAIForConstitute();
                            BeanUtils.copyProperties(a, b);
                            return b;
                        }).collect(Collectors.toList());
                        headers = new String[]{"业务范畴", "财务日期", "订单号", "客户代码", "客户名称", "主单号", "客户单号", "订单状态", "应收情况", "应付情况", "始发港", "目的港"
                                , "航班号", "航线", "到港日期", "责任销售", "销售部门", "责任客服", "工作组", "货物类型", "件数", "毛重", "体积", "计重", "应收金额 (本币)", "应付金额（本币）", "毛利（本币）", "单公斤毛利", "干线单公斤毛利"};
                        ExportExcel<CssPReportSettleAfExcelAIForConstitute> ex = new ExportExcel<CssPReportSettleAfExcelAIForConstitute>();
                        ex.exportExcel(response, "导出EXCEL", headers, listTwo, "结算报表Export");
                    }
                }
            }
        } else if ("SE".equals(bean.getBusinessScope())) {
            List<CssPReportSettleAfExcelSE> list = service.getListForExcelSE(bean);

            //自定义字段拼装结果数据
            if (!StringUtils.isEmpty(bean.getColumnStrs())) {
                if (list != null && list.size() > 0) {
                    list.get(list.size() - 1).setBusinessScope("");
                    int i = 1;
                    for (CssPReportSettleAfExcelSE excel : list) {
                        LinkedHashMap map = new LinkedHashMap();
                        for (int j = 0; j < colunmStrs.length; j++) {
                            if (j == 0) {
                                if (i == list.size()) {
                                    map.put(colunmStrs[j], "合计:");
                                } else {
                                    map.put(colunmStrs[j], i + "");
                                }
                            } else {
                                map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], excel));
                            }
                        }
                        listExcel.add(map);
                        i++;
                    }
                }
                ExcelExportUtils u = new ExcelExportUtils();
                u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");
            } else {
                if (StrUtil.isBlank(bean.getFinancialDateStart()) && StrUtil.isBlank(bean.getFinancialDateEnd())) {
                    if(bean.getShowConstituteFlag() == true){
                        List<CssPReportSettleAfExcelTwoSE> listTwo = list.stream().map(a -> {
                            CssPReportSettleAfExcelTwoSE b = new CssPReportSettleAfExcelTwoSE();
                            BeanUtils.copyProperties(a, b);
                            return b;
                        }).collect(Collectors.toList());
                        headers = new String[]{"业务范畴", "订单号", "客户代码", "客户名称", "主单号", "客户单号", "订单状态", "应收情况", "应付情况", "订舱代理编码", "订舱代理", "始发港", "目的港"
                                , "航次号", "航线", "开航日期", "责任销售", "销售部门", "责任客服", "工作组", "货物类型", "标箱数", "毛重", "体积", "计费吨", "应收金额 (本币)", "应付金额（本币）", "毛利（本币）", "单位毛利", "干线单位毛利", "干线收入", "干线成本", "干线毛利", "支线收入", "支线成本", "支线毛利", "操作收入", "操作成本", "操作毛利", "包装收入", "包装成本", "包装毛利", "仓储收入", "仓储成本", "仓储毛利", "快递收入", "快递成本", "快递毛利", "关检收入", "关检成本", "关检毛利", "数据收入", "数据成本", "数据毛利"};
                        ExportExcel<CssPReportSettleAfExcelTwoSE> ex = new ExportExcel<CssPReportSettleAfExcelTwoSE>();
                        ex.exportExcel(response, "导出EXCEL", headers, listTwo, "结算报表Export");
                    }else{
                        List<CssPReportSettleAfExcelTwoSEForConstitute> listTwo = list.stream().map(a -> {
                            CssPReportSettleAfExcelTwoSEForConstitute b = new CssPReportSettleAfExcelTwoSEForConstitute();
                            BeanUtils.copyProperties(a, b);
                            return b;
                        }).collect(Collectors.toList());
                        headers = new String[]{"业务范畴", "订单号", "客户代码", "客户名称", "主单号", "客户单号", "订单状态", "应收情况", "应付情况", "订舱代理编码", "订舱代理", "始发港", "目的港"
                                , "航次号", "航线", "开航日期", "责任销售", "销售部门", "责任客服", "工作组", "货物类型", "标箱数", "毛重", "体积", "计费吨", "应收金额 (本币)", "应付金额（本币）", "毛利（本币）", "单位毛利", "干线单位毛利"};
                        ExportExcel<CssPReportSettleAfExcelTwoSEForConstitute> ex = new ExportExcel<CssPReportSettleAfExcelTwoSEForConstitute>();
                        ex.exportExcel(response, "导出EXCEL", headers, listTwo, "结算报表Export");
                    }
                } else {
                    if(bean.getShowConstituteFlag() == true){
                        headers = new String[]{"业务范畴", "财务日期", "订单号", "客户代码", "客户名称", "主单号", "客户单号", "订单状态", "应收情况", "应付情况", "订舱代理编码", "订舱代理", "始发港", "目的港"
                                , "航次号", "航线", "开航日期", "责任销售", "销售部门", "责任客服", "工作组", "货物类型", "标箱数", "毛重", "体积", "计重", "应收金额 (本币)", "应付金额（本币）", "毛利（本币）", "单位毛利", "干线单位毛利", "干线收入", "干线成本", "干线毛利", "支线收入", "支线成本", "支线毛利", "操作收入", "操作成本", "操作毛利", "包装收入", "包装成本", "包装毛利", "仓储收入", "仓储成本", "仓储毛利", "快递收入", "快递成本", "快递毛利", "关检收入", "关检成本", "关检毛利", "数据收入", "数据成本", "数据毛利"};
                        ExportExcel<CssPReportSettleAfExcelSE> ex = new ExportExcel<CssPReportSettleAfExcelSE>();
                        ex.exportExcel(response, "导出EXCEL", headers, list, "结算报表Export");
                    }else{
                        List<CssPReportSettleAfExcelSEForConstitute> listTwo = list.stream().map(a -> {
                            CssPReportSettleAfExcelSEForConstitute b = new CssPReportSettleAfExcelSEForConstitute();
                            BeanUtils.copyProperties(a, b);
                            return b;
                        }).collect(Collectors.toList());
                        headers = new String[]{"业务范畴", "财务日期", "订单号", "客户代码", "客户名称", "主单号", "客户单号", "订单状态", "应收情况", "应付情况", "订舱代理编码", "订舱代理", "始发港", "目的港"
                                , "航次号", "航线", "开航日期", "责任销售", "销售部门", "责任客服", "工作组", "货物类型", "标箱数", "毛重", "体积", "计重", "应收金额 (本币)", "应付金额（本币）", "毛利（本币）", "单位毛利", "干线单位毛利"};
                        ExportExcel<CssPReportSettleAfExcelSEForConstitute> ex = new ExportExcel<CssPReportSettleAfExcelSEForConstitute>();
                        ex.exportExcel(response, "导出EXCEL", headers, listTwo, "结算报表Export");
                    }
                }
            }
        } else {
            List<CssPReportSettleAfExcelSI> list = service.getListForExcelSI(bean);
            //自定义字段拼装结果数据
            if (!StringUtils.isEmpty(bean.getColumnStrs())) {
                if (list != null && list.size() > 0) {
                    list.get(list.size() - 1).setBusinessScope("");
                    int i = 1;
                    for (CssPReportSettleAfExcelSI excel : list) {
                        LinkedHashMap map = new LinkedHashMap();
                        for (int j = 0; j < colunmStrs.length; j++) {
                            if (j == 0) {
                                if (i == list.size()) {
                                    map.put(colunmStrs[j], "合计:");
                                } else {
                                    map.put(colunmStrs[j], i + "");
                                }
                            } else {
                                map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], excel));
                            }
                        }
                        listExcel.add(map);
                        i++;
                    }
                }
                ExcelExportUtils u = new ExcelExportUtils();
                u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");
            } else {
                if (StrUtil.isBlank(bean.getFinancialDateStart()) && StrUtil.isBlank(bean.getFinancialDateEnd())) {
                    if(bean.getShowConstituteFlag() == true){
                        List<CssPReportSettleAfExcelTwoSI> listTwo = list.stream().map(a -> {
                            CssPReportSettleAfExcelTwoSI b = new CssPReportSettleAfExcelTwoSI();
                            BeanUtils.copyProperties(a, b);
                            return b;
                        }).collect(Collectors.toList());
                        headers = new String[]{"业务范畴", "订单号", "客户代码", "客户名称", "主单号", "客户单号", "订单状态", "应收情况", "应付情况", "始发港", "目的港"
                                , "航次号", "航线", "到港日期", "责任销售", "销售部门", "责任客服", "工作组", "货物类型", "标箱数", "毛重", "体积", "计费吨", "应收金额 (本币)", "应付金额（本币）", "毛利（本币）", "单位毛利", "干线单位毛利", "干线收入", "干线成本", "干线毛利", "支线收入", "支线成本", "支线毛利", "操作收入", "操作成本", "操作毛利", "包装收入", "包装成本", "包装毛利", "仓储收入", "仓储成本", "仓储毛利", "快递收入", "快递成本", "快递毛利", "关检收入", "关检成本", "关检毛利", "数据收入", "数据成本", "数据毛利"};
                        ExportExcel<CssPReportSettleAfExcelTwoSI> ex = new ExportExcel<CssPReportSettleAfExcelTwoSI>();
                        ex.exportExcel(response, "导出EXCEL", headers, listTwo, "结算报表Export");
                    }else{
                        List<CssPReportSettleAfExcelTwoSIForConstitute> listTwo = list.stream().map(a -> {
                            CssPReportSettleAfExcelTwoSIForConstitute b = new CssPReportSettleAfExcelTwoSIForConstitute();
                            BeanUtils.copyProperties(a, b);
                            return b;
                        }).collect(Collectors.toList());
                        headers = new String[]{"业务范畴", "订单号", "客户代码", "客户名称", "主单号", "客户单号", "订单状态", "应收情况", "应付情况", "始发港", "目的港"
                                , "航次号", "航线", "到港日期", "责任销售", "销售部门", "责任客服", "工作组", "货物类型", "标箱数", "毛重", "体积", "计费吨", "应收金额 (本币)", "应付金额（本币）", "毛利（本币）", "单位毛利", "干线单位毛利"};
                        ExportExcel<CssPReportSettleAfExcelTwoSIForConstitute> ex = new ExportExcel<CssPReportSettleAfExcelTwoSIForConstitute>();
                        ex.exportExcel(response, "导出EXCEL", headers, listTwo, "结算报表Export");
                    }
                } else {
                    if(bean.getShowConstituteFlag() == true){
                        headers = new String[]{"业务范畴", "财务日期", "订单号", "客户代码", "客户名称", "主单号", "客户单号", "订单状态", "应收情况", "应付情况", "始发港", "目的港"
                                , "航次号", "航线", "到港日期", "责任销售", "销售部门", "责任客服", "工作组", "货物类型", "标箱数", "毛重", "体积", "计费吨", "应收金额 (本币)", "应付金额（本币）", "毛利（本币）", "单位毛利", "干线单位毛利", "干线收入", "干线成本", "干线毛利", "支线收入", "支线成本", "支线毛利", "操作收入", "操作成本", "操作毛利", "包装收入", "包装成本", "包装毛利", "仓储收入", "仓储成本", "仓储毛利", "快递收入", "快递成本", "快递毛利", "关检收入", "关检成本", "关检毛利", "数据收入", "数据成本", "数据毛利"};
                        ExportExcel<CssPReportSettleAfExcelSI> ex = new ExportExcel<CssPReportSettleAfExcelSI>();
                        ex.exportExcel(response, "导出EXCEL", headers, list, "结算报表Export");
                    }else{
                        List<CssPReportSettleAfExcelSIForConstitute> listTwo = list.stream().map(a -> {
                            CssPReportSettleAfExcelSIForConstitute b = new CssPReportSettleAfExcelSIForConstitute();
                            BeanUtils.copyProperties(a, b);
                            return b;
                        }).collect(Collectors.toList());
                        headers = new String[]{"业务范畴", "财务日期", "订单号", "客户代码", "客户名称", "主单号", "客户单号", "订单状态", "应收情况", "应付情况", "始发港", "目的港"
                                , "航次号", "航线", "到港日期", "责任销售", "销售部门", "责任客服", "工作组", "货物类型", "标箱数", "毛重", "体积", "计费吨", "应收金额 (本币)", "应付金额（本币）", "毛利（本币）", "单位毛利", "干线单位毛利"};
                        ExportExcel<CssPReportSettleAfExcelSIForConstitute> ex = new ExportExcel<CssPReportSettleAfExcelSIForConstitute>();
                        ex.exportExcel(response, "导出EXCEL", headers, listTwo, "结算报表Export");
                    }
                }
            }
        }


    }
}
