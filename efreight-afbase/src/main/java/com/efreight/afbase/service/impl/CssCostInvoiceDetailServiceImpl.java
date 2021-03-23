package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.dao.*;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.utils.LoginUtils;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FieldValUtils;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * <p>
 * CSS 应付：发票明细表 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2021-01-04
 */
@Service
@AllArgsConstructor
public class CssCostInvoiceDetailServiceImpl extends ServiceImpl<CssCostInvoiceDetailMapper, CssCostInvoiceDetail> implements CssCostInvoiceDetailService {

    private final CssCostInvoiceMapper cssCostInvoiceMapper;

    private final CssPaymentMapper cssPaymentMapper;

    private final CssCostInvoiceDetailWriteoffService cssCostInvoiceDetailWriteoffService;

    private final AfOrderService afOrderService;

    private final ScOrderService scOrderService;

    private final TcOrderService tcOrderService;

    private final LcOrderService lcOrderService;

    private final IoOrderService ioOrderService;

    private final CssPaymentDetailService cssPaymentDetailService;

    private final CssCostFilesMapper cssCostFilesMapper;

    @Override
    public IPage<CssCostInvoiceDetail> getPage(Page page, CssCostInvoiceDetail cssCostInvoiceDetail) {
        cssCostInvoiceDetail.setOrgId(SecurityUtils.getUser().getOrgId());
        IPage<CssCostInvoiceDetail> result = baseMapper.getPage(page, cssCostInvoiceDetail);
        fixResult(result.getRecords());
        return result;
    }

    private void fixResult(List<CssCostInvoiceDetail> records) {
        records.stream().forEach(cssCostInvoiceDetail -> {
            if (cssCostInvoiceDetail.getAmount() != null) {
                cssCostInvoiceDetail.setAmountStr(FormatUtils.formatWithQWF(cssCostInvoiceDetail.getAmount(), 2) + " (" + cssCostInvoiceDetail.getCurrency() + ")");
            } else {
                cssCostInvoiceDetail.setAmount(BigDecimal.ZERO);
                cssCostInvoiceDetail.setAmountStr("");
            }
            if (cssCostInvoiceDetail.getAmountWriteoff() != null) {
                cssCostInvoiceDetail.setAmountWriteoffStr(FormatUtils.formatWithQWF(cssCostInvoiceDetail.getAmountWriteoff(), 2));
            } else {
                cssCostInvoiceDetail.setAmountWriteoffStr("");
                cssCostInvoiceDetail.setAmountWriteoff(BigDecimal.ZERO);
            }
            if (cssCostInvoiceDetail.getAmountNoWriteoff() != null) {
                cssCostInvoiceDetail.setAmountNoWriteoffStr(FormatUtils.formatWithQWF(cssCostInvoiceDetail.getAmountNoWriteoff(), 2));
            } else {
                cssCostInvoiceDetail.setAmountNoWriteoff(BigDecimal.ZERO);
                cssCostInvoiceDetail.setAmountNoWriteoffStr("");
            }
            LambdaQueryWrapper<CssCostFiles> cssCostFilesLambdaQueryWrapper = Wrappers.<CssCostFiles>lambdaQuery();
            cssCostFilesLambdaQueryWrapper.eq(CssCostFiles::getInvoiceDetailId, cssCostInvoiceDetail.getInvoiceDetailId()).eq(CssCostFiles::getOrgId, SecurityUtils.getUser().getOrgId()).isNull(CssCostFiles::getInvoiceDetailWriteoffId);
            cssCostInvoiceDetail.setFilesList(cssCostFilesMapper.selectList(cssCostFilesLambdaQueryWrapper));
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer invoiceDetailId, String rowUuid) {
        //校验
        CssCostInvoiceDetail invoiceDetail = getById(invoiceDetailId);
        if (invoiceDetail == null) {
            throw new RuntimeException("发票不存在，请刷新再操作");
        }
        CssCostInvoice cssCostInvoice = cssCostInvoiceMapper.selectById(invoiceDetail.getInvoiceId());
        if (cssCostInvoice == null) {
            throw new RuntimeException("发票申请不存在，请刷新再操作");
        }
        if (!cssCostInvoice.getRowUuid().equals(rowUuid)) {
            throw new RuntimeException("您好，数据不是最新 请刷新重试。");
        }

        if (invoiceDetail.getWriteoffComplete() != null) {
            throw new RuntimeException("您好，发票号" + invoiceDetail.getInvoiceNum() + "已核销，不允许删除。");
        }
        //删除
        removeById(invoiceDetailId);

        LambdaQueryWrapper<CssCostInvoiceDetail> cssCostInvoiceDetailWrapper = Wrappers.<CssCostInvoiceDetail>lambdaQuery();
        cssCostInvoiceDetailWrapper.eq(CssCostInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoiceDetail::getInvoiceId, invoiceDetail.getInvoiceId());
        List<CssCostInvoiceDetail> list = list(cssCostInvoiceDetailWrapper);
        HashMap<String, BigDecimal> sum = new HashMap<>();
        sum.put("sum", BigDecimal.ZERO);
        list.stream().forEach(cssCostInvoiceDetail -> {
            if (cssCostInvoiceDetail.getAmount() != null) {
                sum.put("sum", sum.get("sum").add(cssCostInvoiceDetail.getAmount()));
            }
        });
        //更新账单
        CssPayment cssPayment = cssPaymentMapper.selectById(invoiceDetail.getPaymentId());
        cssPayment.setRowUuid(UUID.randomUUID().toString());
        cssPayment.setAmountPaymentInvoice(sum.get("sum"));
        cssPaymentMapper.updateById(cssPayment);

        //更新发票申请表
        if (list.size() == 0) {
            cssCostInvoice.setInvoiceStatus(-1);
        } else {
            cssCostInvoice.setInvoiceStatus(0);
        }
        cssCostInvoice.setRowUuid(UUID.randomUUID().toString());
        cssCostInvoiceMapper.updateById(cssCostInvoice);

        //修改订单状态
        LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
        cssPaymentDetailWrapper.eq(CssPaymentDetail::getPaymentId, cssCostInvoice.getPaymentId()).eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId());
        if (cssPayment.getBusinessScope().startsWith("A")) {
            cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(CssPaymentDetail::getOrderId).distinct().forEach(orderId -> {
                AfOrder afOrder = afOrderService.getById(orderId);
                afOrder.setCostStatus(afOrderService.getOrderCostStatusForAF(orderId));
                afOrder.setRowUuid(UUID.randomUUID().toString());
                afOrderService.updateById(afOrder);
            });
        } else if (cssPayment.getBusinessScope().startsWith("S")) {
            cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(CssPaymentDetail::getOrderId).distinct().forEach(orderId -> {
                ScOrder scOrder = scOrderService.getById(orderId);
                scOrder.setCostStatus(afOrderService.getOrderCostStatusForSC(orderId));
                scOrder.setRowUuid(UUID.randomUUID().toString());
                scOrderService.updateById(scOrder);
            });
        } else if (cssPayment.getBusinessScope().startsWith("T")) {
            cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(CssPaymentDetail::getOrderId).distinct().forEach(orderId -> {
                TcOrder tcOrder = tcOrderService.getById(orderId);
                tcOrder.setCostStatus(afOrderService.getOrderCostStatusForTC(orderId));
                tcOrder.setRowUuid(UUID.randomUUID().toString());
                tcOrderService.updateById(tcOrder);
            });
        } else if ("LC".equals(cssPayment.getBusinessScope())) {
            cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(CssPaymentDetail::getOrderId).distinct().forEach(orderId -> {
                LcOrder lcOrder = lcOrderService.getById(orderId);
                lcOrder.setCostStatus(afOrderService.getOrderCostStatusForLC(orderId));
                lcOrder.setRowUuid(UUID.randomUUID().toString());
                lcOrderService.updateById(lcOrder);
            });
        } else if ("IO".equals(cssPayment.getBusinessScope())) {
            cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(CssPaymentDetail::getOrderId).distinct().forEach(orderId -> {
                IoOrder ioOrder = ioOrderService.getById(orderId);
                ioOrder.setCostStatus(afOrderService.getOrderCostStatusForIO(orderId));
                ioOrder.setRowUuid(UUID.randomUUID().toString());
                ioOrderService.updateById(ioOrder);
            });
        }

        //删除附件
        LambdaQueryWrapper<CssCostFiles> wrapper = Wrappers.<CssCostFiles>lambdaQuery();
        wrapper.eq(CssCostFiles::getInvoiceDetailId, invoiceDetailId).eq(CssCostFiles::getOrgId, SecurityUtils.getUser().getOrgId());
        cssCostFilesMapper.delete(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(CssCostInvoiceDetail cssCostInvoiceDetail) {
        //校验
        CssCostInvoice cssCostInvoice = cssCostInvoiceMapper.selectById(cssCostInvoiceDetail.getInvoiceId());
        if (cssCostInvoice == null) {
            throw new RuntimeException("发票申请不存在,请刷新页面再操作");
        }
        if (!cssCostInvoice.getRowUuid().equals(cssCostInvoiceDetail.getRowUuid())) {
            throw new RuntimeException("您好，数据不是最新 请刷新重试。");
        }

        //保存发票
        cssCostInvoiceDetail.setOrgId(SecurityUtils.getUser().getOrgId());
        cssCostInvoiceDetail.setRowUuid(UUID.randomUUID().toString());
        cssCostInvoiceDetail.setCreatorId(SecurityUtils.getUser().getId());
        cssCostInvoiceDetail.setCreateTime(LocalDateTime.now());
        cssCostInvoiceDetail.setCreatorName(SecurityUtils.getUser().buildOptName());
        save(cssCostInvoiceDetail);
        if (cssCostInvoiceDetail.getIfAutoWriteoff()) {
            CssCostInvoiceDetailWriteoff cssCostInvoiceDetailWriteoff = new CssCostInvoiceDetailWriteoff();
            cssCostInvoiceDetailWriteoff.setRowUuid(cssCostInvoice.getRowUuid());
            cssCostInvoiceDetailWriteoff.setInvoiceDetailId(cssCostInvoiceDetail.getInvoiceDetailId());
            cssCostInvoiceDetailWriteoff.setCustomerId(cssCostInvoiceDetail.getCustomerId());
            cssCostInvoiceDetailWriteoff.setCustomerName(cssCostInvoiceDetail.getCustomerName());
            cssCostInvoiceDetailWriteoff.setCurrency(cssCostInvoiceDetail.getCurrency());
            cssCostInvoiceDetailWriteoff.setAmountWriteoff(cssCostInvoiceDetail.getAmount());
            cssCostInvoiceDetailWriteoff.setFinancialAccountCode(cssCostInvoiceDetail.getFinancialAccountCode());
            cssCostInvoiceDetailWriteoff.setFinancialAccountName(cssCostInvoiceDetail.getFinancialAccountName());
            cssCostInvoiceDetailWriteoff.setFinancialAccountType(cssCostInvoiceDetail.getFinancialAccountType());
            cssCostInvoiceDetailWriteoff.setWriteoffDate(cssCostInvoiceDetail.getWriteoffDate());
            cssCostInvoiceDetailWriteoff.setWriteoffRemark(cssCostInvoiceDetail.getWriteoffRemark());
            cssCostInvoiceDetailWriteoffService.insert(cssCostInvoiceDetailWriteoff);
        }

        //更新账单
        LambdaQueryWrapper<CssCostInvoiceDetail> wrapper = Wrappers.<CssCostInvoiceDetail>lambdaQuery();
        wrapper.eq(CssCostInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoiceDetail::getInvoiceId, cssCostInvoiceDetail.getInvoiceId());
        List<CssCostInvoiceDetail> list = list(wrapper);
        HashMap<String, BigDecimal> sum = new HashMap<>();
        sum.put("sum", BigDecimal.ZERO);
        list.stream().forEach(invoiceDetail -> {
            if (invoiceDetail.getAmount() != null) {
                sum.put("sum", sum.get("sum").add(invoiceDetail.getAmount()));
            }
        });
        CssPayment cssPayment = cssPaymentMapper.selectById(cssCostInvoiceDetail.getPaymentId());
        cssPayment.setRowUuid(UUID.randomUUID().toString());
        cssPayment.setAmountPaymentInvoice(sum.get("sum"));
        cssPaymentMapper.updateById(cssPayment);

        //更新申请单
        if (cssPayment.getAmountPayment().equals(cssPayment.getAmountPaymentInvoice())) {
            cssCostInvoice.setInvoiceStatus(1);
        } else {
            cssCostInvoice.setInvoiceStatus(0);
        }
        cssCostInvoice.setRowUuid(UUID.randomUUID().toString());
        cssCostInvoiceMapper.updateById(cssCostInvoice);

        //修改订单状态
        LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
        cssPaymentDetailWrapper.eq(CssPaymentDetail::getPaymentId, cssCostInvoice.getPaymentId()).eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId());
        if (cssPayment.getBusinessScope().startsWith("A")) {
            cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(CssPaymentDetail::getOrderId).distinct().forEach(orderId -> {
                AfOrder afOrder = afOrderService.getById(orderId);
                afOrder.setCostStatus(afOrderService.getOrderCostStatusForAF(orderId));
                afOrder.setRowUuid(UUID.randomUUID().toString());
                afOrderService.updateById(afOrder);
            });
        } else if (cssPayment.getBusinessScope().startsWith("S")) {
            cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(CssPaymentDetail::getOrderId).distinct().forEach(orderId -> {
                ScOrder scOrder = scOrderService.getById(orderId);
                scOrder.setCostStatus(afOrderService.getOrderCostStatusForSC(orderId));
                scOrder.setRowUuid(UUID.randomUUID().toString());
                scOrderService.updateById(scOrder);
            });
        } else if (cssPayment.getBusinessScope().startsWith("T")) {
            cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(CssPaymentDetail::getOrderId).distinct().forEach(orderId -> {
                TcOrder tcOrder = tcOrderService.getById(orderId);
                tcOrder.setCostStatus(afOrderService.getOrderCostStatusForTC(orderId));
                tcOrder.setRowUuid(UUID.randomUUID().toString());
                tcOrderService.updateById(tcOrder);
            });
        } else if ("LC".equals(cssPayment.getBusinessScope())) {
            cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(CssPaymentDetail::getOrderId).distinct().forEach(orderId -> {
                LcOrder lcOrder = lcOrderService.getById(orderId);
                lcOrder.setCostStatus(afOrderService.getOrderCostStatusForLC(orderId));
                lcOrder.setRowUuid(UUID.randomUUID().toString());
                lcOrderService.updateById(lcOrder);
            });
        } else if ("IO".equals(cssPayment.getBusinessScope())) {
            cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(CssPaymentDetail::getOrderId).distinct().forEach(orderId -> {
                IoOrder ioOrder = ioOrderService.getById(orderId);
                ioOrder.setCostStatus(afOrderService.getOrderCostStatusForIO(orderId));
                ioOrder.setRowUuid(UUID.randomUUID().toString());
                ioOrderService.updateById(ioOrder);
            });
        }
    }

    @Override
    public void checkIfWriteoffComplete(Integer invoiceDetailId) {
        CssCostInvoiceDetail invoiceDetail = getById(invoiceDetailId);
        if (invoiceDetail == null) {
            throw new RuntimeException("付款发票不存在，请刷新重试。");
        }
        if (invoiceDetail.getWriteoffComplete() != null && invoiceDetail.getWriteoffComplete().equals(1)) {
            throw new RuntimeException("您好，您选择的发票 已核销完毕。");
        }
    }

    @Override
    public CssCostInvoiceDetail view(Integer invoiceDetailId) {
        CssCostInvoiceDetail invoiceDetail = getById(invoiceDetailId);
        if (invoiceDetail.getAmountWriteoff() == null) {
            invoiceDetail.setAmountWriteoff(BigDecimal.ZERO);
        }
        invoiceDetail.setAmountNoWriteoff(invoiceDetail.getAmount().subtract(invoiceDetail.getAmountWriteoff()));
        CssCostInvoice cssCostInvoice = cssCostInvoiceMapper.selectById(invoiceDetail.getInvoiceId());
        invoiceDetail.setInvoiceRowUuid(cssCostInvoice.getRowUuid());
        return invoiceDetail;
    }

    @Override
    public void exportExcel(CssCostInvoiceDetail cssCostInvoiceDetail) {
        //自定义字段
        Page<CssCostInvoiceDetail> page = new Page<>();
        page.setCurrent(1);
        page.setSize(10000);
        List<CssCostInvoiceDetail> list = this.getPage(page, cssCostInvoiceDetail).getRecords();
        if (!StringUtils.isEmpty(cssCostInvoiceDetail.getColumnStrs())) {
            List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(cssCostInvoiceDetail.getColumnStrs());
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
                for (CssCostInvoiceDetail invoiceDetail : list) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        if ("invoiceName".equals(colunmStrs[j]) || "creatorName".equals(colunmStrs[j])) {
                            if (StrUtil.isNotBlank(FieldValUtils.getFieldValueByFieldName(colunmStrs[j], invoiceDetail))) {
                                map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], invoiceDetail).split(" ")[0]);
                            } else {
                                map.put(colunmStrs[j], "");
                            }
                        } else if ("amountWriteoff".equals(colunmStrs[j]) || "amountNoWriteoff".equals(colunmStrs[j]) || "amount".equals(colunmStrs[j])) {
                            if (StrUtil.isNotBlank(FieldValUtils.getFieldValueByFieldName(colunmStrs[j], invoiceDetail))) {
                                map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j] + "Str", invoiceDetail));
                            } else {
                                map.put(colunmStrs[j], "");
                            }
                        } else if ("createTime".equals(colunmStrs[j])) {
                            if (invoiceDetail.getCreateTime() != null) {
                                map.put(colunmStrs[j], invoiceDetail.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                            } else {
                                map.put(colunmStrs[j], "");
                            }
                        } else if ("invoiceTime".equals(colunmStrs[j])) {
                            if (invoiceDetail.getInvoiceTime() != null) {
                                map.put(colunmStrs[j], invoiceDetail.getInvoiceTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                            } else {
                                map.put(colunmStrs[j], "");
                            }
                        } else if ("filesList".equals(colunmStrs[j])) {
                            if (invoiceDetail.getFilesList() != null && !invoiceDetail.getFilesList().isEmpty()) {
                                StringBuilder stringBuilder = new StringBuilder();
                                invoiceDetail.getFilesList().stream().forEach(item -> {
                                    stringBuilder.append(item.getFileUrl()).append("\n");
                                });
                                map.put(colunmStrs[j], stringBuilder.toString());
                            } else {
                                map.put(colunmStrs[j], "");
                            }
                        } else {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], invoiceDetail));
                        }
                    }
                    listExcel.add(map);
                }
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(LoginUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");

        }
    }
}
