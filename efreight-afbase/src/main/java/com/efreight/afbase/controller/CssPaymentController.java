package com.efreight.afbase.controller;


import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.service.CssPaymentService;
import com.efreight.common.core.annotation.ResponseResult;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FieldValUtils;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * <p>
 * CSS 成本对账单 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-05
 */
@RestController
@RequestMapping("/cssPayment")
@AllArgsConstructor
@Slf4j
public class CssPaymentController {

    private final CssPaymentService cssPaymentService;


    /**
     * 分页查询列表
     *
     * @param page
     * @param cssPayment
     * @return
     */
    @GetMapping
    public MessageInfo page(Page page, CssPayment cssPayment) {
        try {
            IPage result = cssPaymentService.getPage(page, cssPayment);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 新增
     *
     * @param cssPayment
     * @return
     */
    @PostMapping
    public MessageInfo save(@RequestBody CssPayment cssPayment) {
        try {
            if ("AE".equals(cssPayment.getBusinessScope()) || "AI".equals(cssPayment.getBusinessScope())) {
                cssPaymentService.insert(cssPayment);
            }
            if ("SE".equals(cssPayment.getBusinessScope()) || "SI".equals(cssPayment.getBusinessScope())) {
                cssPaymentService.insertSc(cssPayment);
            }
            if (cssPayment.getBusinessScope().startsWith("T")) {
                cssPaymentService.insertTc(cssPayment);
            }
            if ("LC".equals(cssPayment.getBusinessScope())) {
                cssPaymentService.insertLc(cssPayment);
            }
            if ("IO".equals(cssPayment.getBusinessScope())) {
                cssPaymentService.insertIO(cssPayment);
            }
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 编辑
     *
     * @param cssPayment
     * @return
     */
    @PutMapping
    public MessageInfo edit(@RequestBody CssPayment cssPayment) {
        try {
            cssPaymentService.modify(cssPayment);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 删除
     *
     * @param paymentId
     * @return
     */
    @DeleteMapping("/{paymentId}/{rowUuid}")
    public MessageInfo delete(@PathVariable("paymentId") Integer paymentId, @PathVariable("rowUuid") String rowUuid) {
        try {
            cssPaymentService.delete(paymentId, rowUuid);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查看详情
     *
     * @param paymentId
     * @return
     */
    @GetMapping("/{paymentId}")
    public MessageInfo view(@PathVariable("paymentId") Integer paymentId) {
        try {
            CssPayment cssPayment = cssPaymentService.view(paymentId);
            return MessageInfo.ok(cssPayment);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 自动匹配
     *
     * @param afCost
     * @return
     */
    @GetMapping("/automatch")
    public MessageInfo automatch(AfCost afCost) {
        try {
            List<AfCost> result = cssPaymentService.getAutomatchCostList(afCost);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查询对账明细列表(对账单核销时用)
     *
     * @param paymentId
     * @return
     */
    @GetMapping("/paymentDetail/{paymentId}")
    public MessageInfo getPaymentDetailByPaymentId(@PathVariable("paymentId") Integer paymentId) {
        try {
            List<CssCostWriteoffDetail> result = cssPaymentService.getPaymentDetailByPaymentId(paymentId);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 导出Excel
     *
     * @param
     * @return
     */
    @PostMapping("/exportExcel")
    public void exportExcel(CssPayment cssPayment) {
        try {
            cssPaymentService.exportExcel(cssPayment.getPaymentId());
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    /**
     * 导出Excel
     *
     * @param response
     * @param bean
     * @throws IOException
     */
    @PostMapping("/exportPaymentExcel")
    public void exportPaymentExcel(HttpServletResponse response, @ModelAttribute("bean") CssPayment bean) throws IOException {
        //自定义字段
        List<CssPayment> list = cssPaymentService.exportPaymentExcel(bean);
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
                for (CssPayment cssPayment : list) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        if ("createTime".equals(colunmStrs[j])) {
                            if (cssPayment.getCreateTime() != null) {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                map.put("createTime", formatter.format(cssPayment.getCreateTime()));
                            } else {
                                map.put("createTime", "");
                            }
                        } else if ("invoiceTime".equals(colunmStrs[j])) {
                            if (cssPayment.getInvoiceTime() != null) {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                map.put("invoiceTime", formatter.format(cssPayment.getInvoiceTime()));
                            } else {
                                map.put("invoiceTime", "");
                            }
                        } else if ("creatorName".equals(colunmStrs[j]) || "invoiceCreatorName".equals(colunmStrs[j])) {
                            if (!StringUtils.isEmpty(FieldValUtils.getFieldValueByFieldName(colunmStrs[j], cssPayment))) {
                                map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], cssPayment).split(" ")[0]);
                            } else {
                                map.put(colunmStrs[j], "");
                            }
                        } else {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], cssPayment));
                        }
                    }
                    listExcel.add(map);
                }
                listExcel.get(list.size() - 1).put(colunmStrs[0], "合计：");
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");

        }
    }

    /**
     * 发票备注
     *
     * @param
     * @param
     * @return
     */
    @PostMapping(value = "/invoiceRemark")
    public MessageInfo invoiceRemark(@Valid @RequestBody CssPayment bean) {
        try {
            cssPaymentService.invoiceRemark(bean);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 批量账单上传Excel
     *
     * @param currency
     * @param serviceIds
     * @param file
     * @return
     */
    @PostMapping("/readExcel")
    public MessageInfo readExcel(@RequestParam("businessScope") String businessScope, @RequestParam("customerId") Integer customerId, @RequestParam("currency") String currency, @RequestParam("serviceIds") String serviceIds, @RequestParam("file") MultipartFile file) {
        try {
            List<PaymentBatchDetail> list = cssPaymentService.readExcel(businessScope, customerId, currency, serviceIds, file);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 导出批量账单明细
     *
     * @param paymentBatchDetail
     */
    @PostMapping("/exportBatchDetail")
    public void exportBatchDetail(@RequestBody PaymentBatchDetail paymentBatchDetail) {
        try {
            cssPaymentService.exportBatchDetail(paymentBatchDetail);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 批量对账模板下载
     */
    @PostMapping("/downloadModel")
    public void downloadModel() {
        try {
            cssPaymentService.downloadModel();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 批量账单生成
     *
     * @param paymentBatchDetail
     */
    @PostMapping("/batch")
    public MessageInfo saveBatch(@RequestBody PaymentBatchDetail paymentBatchDetail) {
        try {
            cssPaymentService.savePaymentBatch(paymentBatchDetail);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 账单层面核销
     *
     * @param cssCostInvoiceDetail
     */
    @PostMapping("/writeoff")
    @ResponseResult
    public void writeoff(@RequestBody CssCostInvoiceDetail cssCostInvoiceDetail) {
        cssPaymentService.writeoff(cssCostInvoiceDetail);
    }

    /**
     * 校验账单是否满足直接核销条件
     *
     * @param paymentId
     * @param rowUuid
     */
    @GetMapping("/checkIfCanWriteoff/{paymentId}/{rowUuid}")
    @ResponseResult
    public void checkIfCanWriteoff(@PathVariable("paymentId") Integer paymentId, @PathVariable("rowUuid") String rowUuid) {
        cssPaymentService.checkIfCanWriteoff(paymentId, rowUuid);
    }
}

