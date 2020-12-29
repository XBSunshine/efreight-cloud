package com.efreight.afbase.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.efreight.afbase.entity.CssPReportSupplierProcurement;
import com.efreight.afbase.entity.CssPReportSupplierProcurementDetail;
import com.efreight.afbase.service.CssPReportSupplierProcurementService;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FieldValUtils;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
@RequestMapping("/cssPReportSupplierProcurement")
@AllArgsConstructor
@Slf4j
public class CssPReportSupplierProcurementController {

    private final CssPReportSupplierProcurementService cssPReportSupplierProcurementService;

    /**
     * 获取供应商采购分析列表
     *
     * @param cssPReportSupplierProcurement
     * @return
     */
    @GetMapping
    public MessageInfo getList(CssPReportSupplierProcurement cssPReportSupplierProcurement) {
        try {
            List<CssPReportSupplierProcurement> list = cssPReportSupplierProcurementService.getList(cssPReportSupplierProcurement);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 获取供应商采购分析详情
     *
     * @param cssPReportSupplierProcurementDetail
     * @return
     */
    @GetMapping("/detail")
    public MessageInfo getList(CssPReportSupplierProcurementDetail cssPReportSupplierProcurementDetail) {
        try {
            List<CssPReportSupplierProcurementDetail> list = cssPReportSupplierProcurementService.viewDetail(cssPReportSupplierProcurementDetail);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
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
    public void exportExcel(HttpServletResponse response, CssPReportSupplierProcurement bean) throws IOException {
        List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
        String[] colunmStrs = null;
        String[] headers = null;

        if (!StringUtils.isEmpty(bean.getColumnStrs())) {
            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(bean.getColumnStrs());
            int num = jsonArr.size();
            headers = new String[num];
            colunmStrs = new String[num];
            //生成表头跟字段
            if (jsonArr != null && jsonArr.size() > 0) {
                for (int i = 0; i < jsonArr.size(); i++) {
                    JSONObject job = jsonArr.getJSONObject(i);
                    headers[i] = job.getString("label");
                    colunmStrs[i] = job.getString("prop");
                }
            }
        }
        //结果集
        List<CssPReportSupplierProcurement> list = cssPReportSupplierProcurementService.getList(bean);

        if (list != null && list.size() > 0) {
            for (CssPReportSupplierProcurement cssPReportSupplierProcurement : list) {
                LinkedHashMap mapLink = new LinkedHashMap();
                for (int j = 0; j < colunmStrs.length; j++) {
                    if ("orderCountRatio".equals(colunmStrs[j]) || "chargeWeightRatio".equals(colunmStrs[j]) || "costFunctionalAmountRatio".equals(colunmStrs[j])) {
                        if (StrUtil.isBlank(FieldValUtils.getFieldValueByFieldName(colunmStrs[j], cssPReportSupplierProcurement))) {
                            mapLink.put(colunmStrs[j], "0.00%");
                        } else {
                            mapLink.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], cssPReportSupplierProcurement) + "%");
                        }
                    } else if ("orderCount".equals(colunmStrs[j]) || "chargeWeight".equals(colunmStrs[j]) || "gross_profit_count".equals(colunmStrs[j]) || "costFunctionalAmount".equals(colunmStrs[j])
                            || "unitCostFunctionalAmount".equals(colunmStrs[j]) || "yearOrderCount".equals(colunmStrs[j]) || "yearChargeWeight".equals(colunmStrs[j]) || "yearCostFunctionalAmount".equals(colunmStrs[j])) {
                        mapLink.put(colunmStrs[j], StrUtil.isNotBlank(FieldValUtils.getFieldValueByFieldName(colunmStrs[j], cssPReportSupplierProcurement)) ? FormatUtils.formatWithQWFNoBit(new BigDecimal(FieldValUtils.getFieldValueByFieldName(colunmStrs[j], cssPReportSupplierProcurement))) : "");
                    } else {
                        mapLink.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], cssPReportSupplierProcurement));
                    }

                }
                listExcel.add(mapLink);
            }
            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");
        }
    }

    /**
     * 导出ExcelDetail
     *
     * @param
     * @param response
     * @param bean
     * @throws IOException
     */

    @PostMapping(value = "/exportExcelDetail")
    public void exportExcelDetail(HttpServletResponse response, CssPReportSupplierProcurementDetail bean) throws IOException {
        List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
        String[] colunmStrs = null;
        String[] headers = null;

        if (!StringUtils.isEmpty(bean.getColumnStrs())) {
            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(bean.getColumnStrs());
            int num = jsonArr.size();
            int numP = num;
            if (bean.getShowConstituteFlag()) {
                numP = numP + 24;
            }
            headers = new String[numP];
            colunmStrs = new String[numP];
            //生成表头跟字段
            if (jsonArr != null && jsonArr.size() > 0) {
                for (int i = 0; i < jsonArr.size(); i++) {
                    JSONObject job = jsonArr.getJSONObject(i);
                    headers[i] = job.getString("label");
                    colunmStrs[i] = job.getString("prop");
                }
            }
            if (bean.getShowConstituteFlag()) {
                //毛利构成 表头
                headers[num] = "干线收入";
                colunmStrs[num] = "mainRoutingIncome";
                headers[num + 1] = "干线成本";
                colunmStrs[num + 1] = "mainRoutingCost";
                headers[num + 2] = "干线毛利";
                colunmStrs[num + 2] = "mainRouting";
                headers[num + 3] = "支线收入";
                colunmStrs[num + 3] = "feederIncome";
                headers[num + 4] = "支线成本";
                colunmStrs[num + 4] = "feederCost";
                headers[num + 5] = "支线毛利";
                colunmStrs[num + 5] = "feeder";
                headers[num + 6] = "操作收入";
                colunmStrs[num + 6] = "operationIncome";
                headers[num + 7] = "操作成本";
                colunmStrs[num + 7] = "operationCost";
                headers[num + 8] = "操作毛利";
                colunmStrs[num + 8] = "operation";
                headers[num + 9] = "包装收入";
                colunmStrs[num + 9] = "packagingIncome";
                headers[num + 10] = "包装成本";
                colunmStrs[num + 10] = "packagingCost";
                headers[num + 11] = "包装毛利";
                colunmStrs[num + 11] = "packaging";
                headers[num + 12] = "仓储收入";
                colunmStrs[num + 12] = "storageIncome";
                headers[num + 13] = "仓储成本";
                colunmStrs[num + 13] = "storageCost";
                headers[num + 14] = "仓储毛利";
                colunmStrs[num + 14] = "storage";
                headers[num + 15] = "快递收入";
                colunmStrs[num + 15] = "postageIncome";
                headers[num + 16] = "快递成本";
                colunmStrs[num + 16] = "postageCost";
                headers[num + 17] = "快递毛利";
                colunmStrs[num + 17] = "postage";
                headers[num + 18] = "关检收入";
                colunmStrs[num + 18] = "clearanceIncome";
                headers[num + 19] = "关检成本";
                colunmStrs[num + 19] = "clearanceCost";
                headers[num + 20] = "关检毛利";
                colunmStrs[num + 20] = "clearance";
                headers[num + 21] = "数据收入";
                colunmStrs[num + 21] = "exchangeIncome";
                headers[num + 22] = "数据成本";
                colunmStrs[num + 22] = "exchangeCost";
                headers[num + 23] = "数据毛利";
                colunmStrs[num + 23] = "exchange";
            }
        }
        //结果集
        List<CssPReportSupplierProcurementDetail> list = cssPReportSupplierProcurementService.viewDetail(bean);
        if (list != null && list.size() > 0) {
            for (CssPReportSupplierProcurementDetail cssPReportSupplierProcurementDetail : list) {
                LinkedHashMap mapLink = new LinkedHashMap();
                for (int j = 0; j < colunmStrs.length; j++) {
                    if ("grossProfitRatio".equals(colunmStrs[j])) {
                        if (StrUtil.isNotBlank(FieldValUtils.getFieldValueByFieldName(colunmStrs[j], cssPReportSupplierProcurementDetail))) {
                            mapLink.put("grossProfitRatio", FieldValUtils.getFieldValueByFieldName(colunmStrs[j], cssPReportSupplierProcurementDetail) + "%");
                        } else {
                            mapLink.put("grossProfitRatio", "0.00%");
                        }
                    } else if ("salesName".equals(colunmStrs[j]) || "servicerName".equals(colunmStrs[j])) {
                        mapLink.put(colunmStrs[j], StrUtil.isNotBlank(FieldValUtils.getFieldValueByFieldName(colunmStrs[j], cssPReportSupplierProcurementDetail)) ? FieldValUtils.getFieldValueByFieldName(colunmStrs[j], cssPReportSupplierProcurementDetail).split(" ")[0] : "");
                    } else if ("unitGrossProfit".equals(colunmStrs[j]) || "unitCostAmount".equals(colunmStrs[j])) {
                        if (StrUtil.isNotBlank(FieldValUtils.getFieldValueByFieldName(colunmStrs[j], cssPReportSupplierProcurementDetail))) {
                            mapLink.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], cssPReportSupplierProcurementDetail));
                        } else {
                            mapLink.put(colunmStrs[j], "0.00");
                        }
                    } else if ("incomeFunctionalAmount".equals(colunmStrs[j]) || "costFunctionalAmount".equals(colunmStrs[j]) || "grossProfit".equals(colunmStrs[j]) || "functionalAmountWriteoff".equals(colunmStrs[j]) || "functionalAmountNoWriteoff".equals(colunmStrs[j])) {
                        mapLink.put(colunmStrs[j], StrUtil.isNotBlank(FieldValUtils.getFieldValueByFieldName(colunmStrs[j], cssPReportSupplierProcurementDetail)) ? FormatUtils.formatWithQWF(new BigDecimal(FieldValUtils.getFieldValueByFieldName(colunmStrs[j], cssPReportSupplierProcurementDetail)), 2) : "");
                    } else {
                        mapLink.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], cssPReportSupplierProcurementDetail));
                    }

                }
                listExcel.add(mapLink);
            }
            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");
        }
    }
}
