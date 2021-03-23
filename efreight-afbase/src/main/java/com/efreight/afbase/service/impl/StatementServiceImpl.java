package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.dao.*;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.entity.procedure.SettleStatement;
import com.efreight.afbase.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.utils.FieldValUtils;
import com.efreight.afbase.utils.LoginUtils;
import com.efreight.afbase.utils.PDFUtils;
import com.efreight.common.core.feign.RemoteServiceToHRS;
import com.efreight.common.core.jms.MailSendService;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.core.utils.JxlsUtils;
import com.efreight.common.core.utils.PoiUtils;
import com.efreight.common.remoteVo.OrgTemplateConfig;
import com.efreight.common.security.util.SecurityUtils;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import com.itextpdf.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jxls.util.Util;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * CSS 应收：清单 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2019-12-24
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatementServiceImpl extends ServiceImpl<StatementMapper, Statement> implements StatementService {

    private final AfOrderService afOrderService;
    private final ScOrderService scOrderService;
    private final TcOrderService tcOrderService;
    private final LcOrderService lcOrderService;
    private final IoOrderService ioOrderService;
    private final DebitNoteService debitNoteService;
    private final CssDebitNoteService cssDebitNoteService;
    private final StatementCurrencyService statementCurrencyService;
    private final StatementCurrencyMapper statementMapper;
    private final LogService logService;
    private final ScLogService scLogService;
    private final TcLogService tcLogService;
    private final LcLogService lcLogService;
    private final IoLogService ioLogService;
    private final CssIncomeWriteoffService cssIncomeWriteoffService;
    private final MailSendService mailSendService;
    private final DebitNoteMapper debitNoteMapper;
    private final SettleStatementMapper settleStatementMapper;
    private final RemoteServiceToHRS remoteServiceToHRS;
    private final AfOrderMapper afOrderMapper;
    private final CssIncomeInvoiceDetailWriteoffService cssIncomeInvoiceDetailWriteoffService;
    private final CssIncomeInvoiceDetailService cssIncomeInvoiceDetailService;
    private final CssIncomeInvoiceMapper cssIncomeInvoiceMapper;
    private final CssIncomeInvoiceDetailMapper detailMapper;

    @Override
    public IPage getPage2(Page page, Statement statement) {
        statement.setOrgId(SecurityUtils.getUser().getOrgId());
        if(!statement.getInvoiceDateStart().isEmpty()||!statement.getInvoiceDateEnd().isEmpty()
        		||!statement.getInvoiceNum().isEmpty()||!statement.getInvoiceTitle().isEmpty()) {
        	statement.setInvoiceQuery(1);
        }
        IPage<Statement> iPage = baseMapper.getPage(page, statement);
        iPage.getRecords().stream().forEach(record -> {
        	if(record.getWriteoffComplete()!=null&&record.getWriteoffComplete() == 1) {
        		record.setStatementStatus("核销完毕");
        	}else if(record.getWriteoffComplete()!=null&&record.getWriteoffComplete() == 0) {
        		record.setStatementStatus("部分核销");
        	}else {
				if(record.getInvoiceStatus()!=null&&record.getInvoiceStatus()==1&&record.getWriteoffComplete()==null) {
					record.setStatementStatus("开票完毕");
				}else if(record.getInvoiceStatus()!=null&&record.getInvoiceStatus()==0&&record.getWriteoffComplete()==null) {
					record.setStatementStatus("部分开票");
				}else if(record.getInvoiceStatus()!=null&&record.getInvoiceStatus()==-1) {
					record.setStatementStatus("待开票");
				}else {
					record.setStatementStatus("已制清单");
				}
          }
            //
//            LambdaQueryWrapper<CssIncomeWriteoff> cssIncomeWriteoffLambdaQueryWrapper = Wrappers.<CssIncomeWriteoff>lambdaQuery();
//            cssIncomeWriteoffLambdaQueryWrapper.eq(CssIncomeWriteoff::getStatementId, record.getStatementId()).eq(CssIncomeWriteoff::getOrgId, SecurityUtils.getUser().getOrgId());
//            List<CssIncomeWriteoff> cssIncomeWriteoffList = cssIncomeWriteoffService.list(cssIncomeWriteoffLambdaQueryWrapper);
//            StringBuffer writeoffNumbuffer = new StringBuffer("");
//            for (int i = 0; i < cssIncomeWriteoffList.size(); i++) {
//                CssIncomeWriteoff cssIncomeWriteoff = cssIncomeWriteoffList.get(i);
//                writeoffNumbuffer.append(cssIncomeWriteoff.getIncomeWriteoffId());
//                writeoffNumbuffer.append(" ");
//                writeoffNumbuffer.append(cssIncomeWriteoff.getWriteoffNum());
//                writeoffNumbuffer.append("  ");
//            }
//            record.setWriteoffNum(writeoffNumbuffer.toString());
        	//账单核销号
        	LambdaQueryWrapper<CssIncomeInvoiceDetailWriteoff> cssIncomeInvoiceDetailWriteoffWrapper = Wrappers.<CssIncomeInvoiceDetailWriteoff>lambdaQuery();
        	cssIncomeInvoiceDetailWriteoffWrapper.eq(CssIncomeInvoiceDetailWriteoff::getStatementId, record.getStatementId()).eq(CssIncomeInvoiceDetailWriteoff::getOrgId, SecurityUtils.getUser().getOrgId());
            List<CssIncomeInvoiceDetailWriteoff> listCssIncomeInvoiceDetailWriteoff = cssIncomeInvoiceDetailWriteoffService.list(cssIncomeInvoiceDetailWriteoffWrapper);
            StringBuffer writeoffNumbuffer = new StringBuffer("");
            if(listCssIncomeInvoiceDetailWriteoff!=null&&listCssIncomeInvoiceDetailWriteoff.size()>0) {
            	for (int i = 0; i < listCssIncomeInvoiceDetailWriteoff.size(); i++) {
            		CssIncomeInvoiceDetailWriteoff p = listCssIncomeInvoiceDetailWriteoff.get(i);
            		writeoffNumbuffer.append(p.getInvoiceDetailWriteoffId());
            		writeoffNumbuffer.append(" ");
            		writeoffNumbuffer.append(p.getWriteoffNum());
            		writeoffNumbuffer.append("  ");
            	}
            }
            record.setWriteoffNum(writeoffNumbuffer.toString());
            DateTimeFormatter formatters = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            //发票号
            LambdaQueryWrapper<CssIncomeInvoiceDetail> cssIncomeInvoiceDetailWrapper = Wrappers.<CssIncomeInvoiceDetail>lambdaQuery();
            cssIncomeInvoiceDetailWrapper.eq(CssIncomeInvoiceDetail::getStatementId, record.getStatementId()).eq(CssIncomeInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId());
            List<CssIncomeInvoiceDetail> listCssIncomeInvoiceDetail = cssIncomeInvoiceDetailService.list(cssIncomeInvoiceDetailWrapper);
            StringBuffer invoiceNumbuffer = new StringBuffer("");
            if(listCssIncomeInvoiceDetail!=null&&listCssIncomeInvoiceDetail.size()>0) {
            	for (int i = 0; i < listCssIncomeInvoiceDetail.size(); i++) {
            		CssIncomeInvoiceDetail k = listCssIncomeInvoiceDetail.get(i);
            		invoiceNumbuffer.append(k.getInvoiceDetailId());
            		invoiceNumbuffer.append("#");
            		if(k.getInvoiceNum()!=null&&!"".equals(k.getInvoiceNum())){
                    	invoiceNumbuffer.append(k.getInvoiceNum()+" "+"("+formatters.format(k.getInvoiceDate())+")");
                    }else {
                    	invoiceNumbuffer.append(k.getInvoiceNum());
                    }
            		invoiceNumbuffer.append("&");
            	}
            }
            record.setInvoiceNum(invoiceNumbuffer.toString());
            //账单金额实现多币种显示
            LambdaQueryWrapper<StatementCurrency> statementCurrencyWrapper = Wrappers.<StatementCurrency>lambdaQuery();
            statementCurrencyWrapper.eq(StatementCurrency::getStatementId, record.getStatementId()).eq(StatementCurrency::getOrgId, SecurityUtils.getUser().getOrgId());
            List<StatementCurrency> currencyList = statementCurrencyService.list(statementCurrencyWrapper);
            StringBuffer buffer3 = new StringBuffer("");
            StringBuffer buffer = new StringBuffer();
            StringBuffer buffer2 = new StringBuffer();
            currencyList.stream().forEach(currency -> {
            	if(buffer3.toString().isEmpty()) {
            		buffer3.append(currency.getCurrency());
            	}else {
            		if(!buffer3.toString().contains(currency.getCurrency())) {
            			buffer3.append(",").append(currency.getCurrency());
            		}
            	}
                buffer.append(new DecimalFormat("###,##0.00").format(currency.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP))).append(" (").append(currency.getCurrency()).append(")  ");
                if (currency.getAmountWriteoff() != null) {
                    buffer2.append(new DecimalFormat("###,##0.00").format(currency.getAmountWriteoff().setScale(2, BigDecimal.ROUND_HALF_UP))).append(" (").append(currency.getCurrency()).append(")  ");
                }
            });

            record.setCurrencyAmount(buffer.toString());
            record.setCurrencyAmount2(buffer2.toString());
            record.setCurrencyStr(buffer3.toString());

            //根据清单id:statement_id查询订单的责任客服
            List<Integer> servicerIdList = new ArrayList<>();
            if ("AE".equals(record.getBusinessScope()) || "AI".equals(record.getBusinessScope())) {
                servicerIdList = baseMapper.getServicerIdListByStatementId(record.getStatementId());
            } else if (record.getBusinessScope().startsWith("S")) {
                servicerIdList = baseMapper.getServicerIdListByStatementId1(record.getStatementId());
            } else if (record.getBusinessScope().startsWith("T")) {
                servicerIdList = baseMapper.getServicerIdListByStatementIdTC(record.getStatementId());
            } else if (record.getBusinessScope().startsWith("L")) {
                servicerIdList = baseMapper.getServicerIdListByStatementIdLC(record.getStatementId());
            } else if (record.getBusinessScope().startsWith("I")) {
                servicerIdList = baseMapper.getServicerIdListByStatementIdIO(record.getStatementId());
            }
            record.setServicerIdList(servicerIdList);

            //根据清单id:statement_id查询订单的责任销售
            List<Integer> salesIdList = new ArrayList<>();
            if ("AE".equals(record.getBusinessScope()) || "AI".equals(record.getBusinessScope())) {
                salesIdList = baseMapper.getSalesIdListByStatementId(record.getStatementId());
            } else if (record.getBusinessScope().startsWith("S")) {
                salesIdList = baseMapper.getSalesIdListByStatementId1(record.getStatementId());
            } else if (record.getBusinessScope().startsWith("T")) {
                salesIdList = baseMapper.getSalesIdListByStatementIdTC(record.getStatementId());
            } else if (record.getBusinessScope().startsWith("L")) {
                salesIdList = baseMapper.getSalesIdListByStatementIdLC(record.getStatementId());
            } else if (record.getBusinessScope().startsWith("I")) {
                salesIdList = baseMapper.getSalesIdListByStatementIdIO(record.getStatementId());
            }
            record.setSalesIdList(salesIdList);
        });

        return iPage;
    }

    public List<Statement> getTatol(Statement statement) {
        statement.setOrgId(SecurityUtils.getUser().getOrgId());
        if(!statement.getInvoiceDateStart().isEmpty()||!statement.getInvoiceDateEnd().isEmpty()
        		||!statement.getInvoiceNum().isEmpty()||!statement.getInvoiceTitle().isEmpty()) {
        	statement.setInvoiceQuery(1);
        }
        List<Statement> list = baseMapper.getPageList(statement);
        List<Statement> listTotal = new ArrayList<Statement>();
        Statement ment = new Statement();
        HashMap<String, BigDecimal> currencyAmount = new HashMap<>();//清单金额（原币）
        HashMap<String, BigDecimal> currencyAmount2 = new HashMap<>();//已核销金额（原币）
        BigDecimal functionalAmount = new BigDecimal(0);
        BigDecimal functionalAmountWriteoff = new BigDecimal(0);
        BigDecimal functionalAmountNoWriteoff = new BigDecimal(0);
        for (int i = 0; i < list.size(); i++) {
            Statement record = list.get(i);
            functionalAmount = functionalAmount.add(record.getFunctionalAmount());
            if (record.getFunctionalAmountWriteoff() != null) {
                functionalAmountWriteoff = functionalAmountWriteoff.add(record.getFunctionalAmountWriteoff());
            }
            //账单金额实现多币种显示
            LambdaQueryWrapper<StatementCurrency> statementCurrencyWrapper = Wrappers.<StatementCurrency>lambdaQuery();
            statementCurrencyWrapper.eq(StatementCurrency::getStatementId, record.getStatementId()).eq(StatementCurrency::getOrgId, SecurityUtils.getUser().getOrgId());
            List<StatementCurrency> currencyList = statementCurrencyService.list(statementCurrencyWrapper);
            for (StatementCurrency currency : currencyList) {
                if (currencyAmount.containsKey(currency.getCurrency())) {
                    currencyAmount.put(currency.getCurrency(), currencyAmount.get(currency.getCurrency()).add(currency.getAmount()));
                } else {
                    currencyAmount.put(currency.getCurrency(), currency.getAmount());
                }
                if (currency.getAmountWriteoff() != null) {
                    if (currencyAmount2.containsKey(currency.getCurrency())) {
                        currencyAmount2.put(currency.getCurrency(), currencyAmount2.get(currency.getCurrency()).add(currency.getAmountWriteoff()));
                    } else {
                        currencyAmount2.put(currency.getCurrency(), currency.getAmountWriteoff());
                    }
                }
            }

        }
        StringBuffer buffer = new StringBuffer();
        StringBuffer buffer2 = new StringBuffer();
        for (Map.Entry<String, BigDecimal> entry : currencyAmount.entrySet()) {
            buffer.append(new DecimalFormat("###,##0.00").format(entry.getValue().setScale(2, BigDecimal.ROUND_HALF_UP))).append(" (").append(entry.getKey()).append(")  ");
        }
        if (currencyAmount2 != null && !currencyAmount2.isEmpty()) {
            for (Map.Entry<String, BigDecimal> entry : currencyAmount2.entrySet()) {
                buffer2.append(new DecimalFormat("###,##0.00").format(entry.getValue().setScale(2, BigDecimal.ROUND_HALF_UP))).append(" (").append(entry.getKey()).append(")  ");
            }
        }
        ment.setStatementNum("合计");
        ment.setCurrencyAmount(buffer.toString());
        ment.setCurrencyAmount2(buffer2.toString());
        ment.setFunctionalAmountWriteoff(functionalAmountWriteoff);
        ment.setFunctionalAmount(functionalAmount);
        listTotal.add(ment);
        return listTotal;
    }

    @Override
    public IPage getPage(Page page, Statement statement) {
        //查询订单信息
        LambdaQueryWrapper<AfOrder> orderWrapper = Wrappers.<AfOrder>lambdaQuery();
        orderWrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId());
        if (StrUtil.isNotBlank(statement.getAwbNumber())) {
            orderWrapper.like(AfOrder::getAwbNumber, "%" + statement.getAwbNumber() + "%");
        }
        if (StrUtil.isNotBlank(statement.getOrderCode())) {
            orderWrapper.like(AfOrder::getOrderCode, "%" + statement.getOrderCode() + "%");
        }
        if (StrUtil.isNotBlank(statement.getCustomerNumber())) {
            orderWrapper.like(AfOrder::getCustomerNumber, "%" + statement.getCustomerNumber() + "%");
        }

        List<Integer> orderList = afOrderService.list(orderWrapper).stream().map(AfOrder::getOrderId).collect(Collectors.toList());
        if (orderList.size() == 0) {
            page.setRecords(new ArrayList());
            return page;
        }

        //查询账单信息
        LambdaQueryWrapper<DebitNote> debitWrapper = Wrappers.<DebitNote>lambdaQuery();
        debitWrapper.eq(DebitNote::getOrgId, SecurityUtils.getUser().getOrgId());
        if (StrUtil.isNotBlank(statement.getDebitNoteNum())) {
            debitWrapper.like(DebitNote::getDebitNoteNum, "%" + statement.getDebitNoteNum() + "%");
        }
        debitWrapper.in(DebitNote::getOrderId, orderList);
        List<Integer> statementIdList = debitNoteService.list(debitWrapper).stream().map(DebitNote::getStatementId).distinct().collect(Collectors.toList());
        if (statementIdList.size() == 0) {
            page.setRecords(new ArrayList());
            return page;
        }
        //查询清单信息

        LambdaQueryWrapper<Statement> wrapper = Wrappers.<Statement>lambdaQuery();
        wrapper.eq(Statement::getOrgId, SecurityUtils.getUser().getOrgId());
        if (StrUtil.isNotBlank(statement.getBusinessScope())) {
            wrapper.eq(Statement::getBusinessScope, statement.getBusinessScope());
        }

        if (StrUtil.isNotBlank(statement.getStatementNum())) {
            wrapper.like(Statement::getStatementNum, "%" + statement.getStatementNum() + "%");
        }

        if (StrUtil.isNotBlank(statement.getCustomerName())) {
            wrapper.like(Statement::getCustomerName, "%" + statement.getCustomerName() + "%");
        }

        if (statement.getStatementDateStart() != null) {
            wrapper.ge(Statement::getStatementDate, statement.getStatementDateStart());
        }
        if (statement.getStatementDateEnd() != null) {
            wrapper.le(Statement::getStatementDate, statement.getStatementDateEnd());
        }

        //状态预留
        if (statement.getStatementStatus().equals("发票开具")) {
            wrapper.isNotNull(Statement::getInvoiceId);
        } else if (statement.getStatementStatus().equals("制作清单")) {
            wrapper.isNull(Statement::getInvoiceId);
        }
        wrapper.in(Statement::getStatementId, statementIdList);
        IPage<Statement> iPage = baseMapper.selectPage(page, wrapper);
        iPage.getRecords().stream().forEach(record -> {
            if (record.getInvoiceId() == null) {
                record.setStatementStatus("制作清单");
            } else {
                record.setStatementStatus("发票开具");
            }

            //账单金额实现多币种显示
            LambdaQueryWrapper<StatementCurrency> statementCurrencyWrapper = Wrappers.<StatementCurrency>lambdaQuery();
            statementCurrencyWrapper.eq(StatementCurrency::getStatementId, record.getStatementId()).eq(StatementCurrency::getOrgId, SecurityUtils.getUser().getOrgId());
            List<StatementCurrency> currencyList = statementCurrencyService.list(statementCurrencyWrapper);
            StringBuffer buffer = new StringBuffer();
            currencyList.stream().forEach(currency -> {
                buffer.append(new DecimalFormat("###,###.00").format(currency.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP))).append(" (").append(currency.getCurrency()).append(")  ");
            });

            record.setCurrencyAmount(buffer.toString());
        });

        return iPage;
    }

    private String getOrderCode(String businessScope) {
        Date dt = new Date();

        String year = String.format("%ty", dt);

        String mon = String.format("%tm", dt);

        String day = String.format("%td", dt);
        return businessScope + "-ST-" + year + mon + day;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doSave(Statement bean) {
        String businessScope = bean.getBusinessScope();
        List<CssDebitNote> isOKList = baseMapper.isDebitNote(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteIds());
        if (isOKList.size() > 0) {
            throw new RuntimeException("已经制做过清单");
        }
        for (int i = 0; i < bean.getBillList().size(); i++) {
            CssDebitNote bill = bean.getBillList().get(i);
            CssDebitNote bill2 = cssDebitNoteService.getById(bill.getDebitNoteId());
            if (bill2 == null) {
                throw new RuntimeException("账单不存在");
            }
            if (!bill.getRowUuid().equals(bill2.getRowUuid())) {
                throw new RuntimeException("账单不是最新数据，请刷新页面再操作");
            }
        }
        for (int i = 0; i < bean.getBillList().size(); i++) {
            CssDebitNote bill = bean.getBillList().get(i);
            bill.setRowUuid(UUID.randomUUID().toString());
            cssDebitNoteService.updateById(bill);
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        bean.setStatementDate(formatter.format(new Date()));
        String code = getOrderCode(businessScope);
        List<Statement> codeList = baseMapper.selectCode(SecurityUtils.getUser().getOrgId(), code);

//        if (codeList.size() == 0) {
//            bean.setStatementNum(code + "0001");
//        } else if (codeList.size() < 9999) {
//            bean.setStatementNum(code + String.format("%04d", codeList.size() + 1));
//        } else {
//            throw new RuntimeException("每天最多可以创建9999个清单");
//        }

        if (codeList.size() == 0) {
            bean.setStatementNum(code + "0001");
        } else {
            if ((code + "9999").equals(codeList.get(0).getStatementNum())) {
                throw new RuntimeException("每天最多可以创建9999个" + businessScope + "清单");
            } else {
                String str = codeList.get(0).getStatementNum();
                str = str.substring(str.length() - 4);
                bean.setStatementNum(code + String.format("%04d", Integer.parseInt(str) + 1));
            }

        }
        bean.setRowUuid(UUID.randomUUID().toString());
        bean.setCreateTime(LocalDateTime.now());
        bean.setCreatorId(SecurityUtils.getUser().getId());
        bean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        baseMapper.insert(bean);

        bean.setDebitNoteIds("'" + bean.getDebitNoteIds().replaceAll(",", "','") + "'");
        baseMapper.updateDebitNote(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteIds(), bean.getStatementId(),bean.getOrgBankConfigId(), UUID.randomUUID().toString());

        //
        for (int i = 0; i < bean.getCurrencyList().size(); i++) {
            StatementCurrency debitBean = bean.getCurrencyList().get(i);
            debitBean.setStatementId(bean.getStatementId());
            debitBean.setOrgId(SecurityUtils.getUser().getOrgId());
            statementMapper.insert(debitBean);
            //汇率同步
            if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
                baseMapper.updateIncome2(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteIds(), debitBean.getCurrency(), debitBean.getExchangeRate());
            } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
                baseMapper.updateIncomeSE2(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteIds(), debitBean.getCurrency(), debitBean.getExchangeRate());
            } else if (bean.getBusinessScope().startsWith("T")) {
                baseMapper.updateIncomeTE2(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteIds(), debitBean.getCurrency(), debitBean.getExchangeRate());
            } else if (bean.getBusinessScope().startsWith("L")) {
                baseMapper.updateIncomeLC2(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteIds(), debitBean.getCurrency(), debitBean.getExchangeRate());
            } else if ("IO".equals(bean.getBusinessScope())) {
                baseMapper.updateIncomeIO2(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteIds(), debitBean.getCurrency(), debitBean.getExchangeRate());
            }
            baseMapper.updateIncome3(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteIds(), debitBean.getCurrency(), debitBean.getExchangeRate());
        }
        //汇率同步
        List<CssDebitNote> list = baseMapper.getNoteList(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteIds());
        for (int i = 0; i < list.size(); i++) {
            CssDebitNote note = list.get(i);
            baseMapper.updateIncome4(SecurityUtils.getUser().getOrgId(), note.getDebitNoteId(), note.getFunctionalAmount());
        }
        Set orderIds = new HashSet();
        //修改订单费用状态
        for (int i = 0; i < bean.getBillList().size(); i++) {
            CssDebitNote bill = bean.getBillList().get(i);
            orderIds.add(bill.getOrderId());
//            if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
//                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
//                afOrderMapper.generateTempTableA(SecurityUtils.getUser().getOrgId(), bill.getOrderUuid());
//                //根据临时表更新订单状态
//                afOrderMapper.updateOrderStatusByTempTableA();
//
//            } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
//                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
//                afOrderMapper.generateTempTableS(SecurityUtils.getUser().getOrgId(), bill.getOrderUuid());
//                //根据临时表更新订单状态
//                afOrderMapper.updateOrderStatusByTempTableS();
//            } else if (bean.getBusinessScope().startsWith("T")) {
//                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
//                afOrderMapper.generateTempTableT(SecurityUtils.getUser().getOrgId(), bill.getOrderUuid());
//                //根据临时表更新订单状态
//                afOrderMapper.updateOrderStatusByTempTableT();
//            } else if (bean.getBusinessScope().startsWith("L")) {
//                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
//                afOrderMapper.generateTempTableL(SecurityUtils.getUser().getOrgId(), bill.getOrderUuid());
//                //根据临时表更新订单状态
//                afOrderMapper.updateOrderStatusByTempTableL();
//            } else if (bean.getBusinessScope().equals("IO")) {
//                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
//                afOrderMapper.generateTempTableIO(SecurityUtils.getUser().getOrgId(), bill.getOrderUuid());
//                //根据临时表更新订单状态
//                afOrderMapper.updateOrderStatusByTempTableIO();
//            }
        }
        
        Iterator it = orderIds.iterator();
		while (it.hasNext()) {
			//更新订单应收状态：（order. income_status）
    		List<Map> listMap = detailMapper.getOrderIncomeStatus(SecurityUtils.getUser().getOrgId(), it.next().toString(),bean.getBusinessScope());
    		if(listMap!=null&&listMap.size()>0) {
    			for(Map map:listMap) {
    				detailMapper.updateOrderIncomeStatus(Integer.valueOf(map.get("org_id").toString()),Integer.valueOf(map.get("order_id").toString()),map.get("income_status").toString(),UUID.randomUUID().toString(),bean.getBusinessScope());
    			}
    		}
		}
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doUpdate(Statement bean) {
        Statement statement = getById(bean.getStatementId());
        if (statement == null) {
            throw new RuntimeException("清单不存在");
        }
        if (!bean.getRowUuid().equals(statement.getRowUuid())) {
            throw new RuntimeException("清单不是最新数据，请刷新页面再操作");
        }
        LambdaQueryWrapper<CssIncomeInvoice> cssIncomeInvoiceWrapper = Wrappers.<CssIncomeInvoice>lambdaQuery();
        cssIncomeInvoiceWrapper.eq(CssIncomeInvoice::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssIncomeInvoice::getStatementId, bean.getStatementId()).last("limit 1");
        CssIncomeInvoice incomeInvoice = cssIncomeInvoiceMapper.selectOne(cssIncomeInvoiceWrapper);
        if (incomeInvoice != null) {
            throw new RuntimeException(statement.getStatementNum()+"清单 已申请开票 或 已开票 ，不能修改。");
        }
        bean.setRowUuid(UUID.randomUUID().toString());
        bean.setEditTime(LocalDateTime.now());
        bean.setEditorId(SecurityUtils.getUser().getId());
        bean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        bean.setOrgId(SecurityUtils.getUser().getOrgId());

        UpdateWrapper<Statement> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("statement_id", bean.getStatementId());
        baseMapper.update(bean, updateWrapper);

        //根据statement_id查询账单信息
        LambdaQueryWrapper<DebitNote> debitNoteLambdaQueryWrapper = Wrappers.<DebitNote>lambdaQuery();
        debitNoteLambdaQueryWrapper.eq(DebitNote::getStatementId, bean.getStatementId()).eq(DebitNote::getOrgId, SecurityUtils.getUser().getOrgId());
        List<DebitNote> debitNotes = debitNoteService.list(debitNoteLambdaQueryWrapper);

        baseMapper.updateDebitNote2(SecurityUtils.getUser().getOrgId(), bean.getStatementId());

        baseMapper.delete2(SecurityUtils.getUser().getOrgId(), bean.getStatementId());

        bean.setDebitNoteIds("'" + bean.getDebitNoteIds().replaceAll(",", "','") + "'");
        baseMapper.updateDebitNote(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteIds(), bean.getStatementId(),bean.getOrgBankConfigId(), UUID.randomUUID().toString());

        //
        for (int i = 0; i < bean.getCurrencyList().size(); i++) {
            StatementCurrency debitBean = bean.getCurrencyList().get(i);
            debitBean.setStatementId(bean.getStatementId());
            debitBean.setOrgId(SecurityUtils.getUser().getOrgId());
            statementMapper.insert(debitBean);
            //汇率同步
            if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
                baseMapper.updateIncome2(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteIds(), debitBean.getCurrency(), debitBean.getExchangeRate());
            } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
                baseMapper.updateIncomeSE2(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteIds(), debitBean.getCurrency(), debitBean.getExchangeRate());
            } else if (bean.getBusinessScope().startsWith("T")) {
                baseMapper.updateIncomeTE2(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteIds(), debitBean.getCurrency(), debitBean.getExchangeRate());
            } else if (bean.getBusinessScope().startsWith("L")) {
                baseMapper.updateIncomeLC2(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteIds(), debitBean.getCurrency(), debitBean.getExchangeRate());
            } else if ("IO".equals(bean.getBusinessScope())) {
                baseMapper.updateIncomeIO2(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteIds(), debitBean.getCurrency(), debitBean.getExchangeRate());
            }
            baseMapper.updateIncome3(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteIds(), debitBean.getCurrency(), debitBean.getExchangeRate());
        }
        //汇率同步
        List<CssDebitNote> list = baseMapper.getNoteList(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteIds());
        for (int i = 0; i < list.size(); i++) {
            CssDebitNote note = list.get(i);
            baseMapper.updateIncome4(SecurityUtils.getUser().getOrgId(), note.getDebitNoteId(), note.getFunctionalAmount());
        }
        Set orderIds = new HashSet();
        //修改订单费用状态
        for (int i = 0; i < debitNotes.size(); i++) {
            DebitNote bill = debitNotes.get(i);
            orderIds.add(bill.getOrderId());
//            if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
//                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
//                afOrderMapper.generateTempTableA(SecurityUtils.getUser().getOrgId(), bill.getOrderUuid());
//                //根据临时表更新订单状态
//                afOrderMapper.updateOrderStatusByTempTableA();
//
//            } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
//                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
//                afOrderMapper.generateTempTableS(SecurityUtils.getUser().getOrgId(), bill.getOrderUuid());
//                //根据临时表更新订单状态
//                afOrderMapper.updateOrderStatusByTempTableS();
//            } else if (bean.getBusinessScope().startsWith("T")) {
//                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
//                afOrderMapper.generateTempTableT(SecurityUtils.getUser().getOrgId(), bill.getOrderUuid());
//                //根据临时表更新订单状态
//                afOrderMapper.updateOrderStatusByTempTableT();
//            } else if (bean.getBusinessScope().startsWith("L")) {
//                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
//                afOrderMapper.generateTempTableL(SecurityUtils.getUser().getOrgId(), bill.getOrderUuid());
//                //根据临时表更新订单状态
//                afOrderMapper.updateOrderStatusByTempTableL();
//            } else if (bean.getBusinessScope().equals("IO")) {
//                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
//                afOrderMapper.generateTempTableIO(SecurityUtils.getUser().getOrgId(), bill.getOrderUuid());
//                //根据临时表更新订单状态
//                afOrderMapper.updateOrderStatusByTempTableIO();
//            }
        }
        Iterator it = orderIds.iterator();
		while (it.hasNext()) {
			//更新订单应收状态：（order. income_status）
    		List<Map> listMap = detailMapper.getOrderIncomeStatus(SecurityUtils.getUser().getOrgId(), it.next().toString(),bean.getBusinessScope());
    		if(listMap!=null&&listMap.size()>0) {
    			for(Map map:listMap) {
    				detailMapper.updateOrderIncomeStatus(Integer.valueOf(map.get("org_id").toString()),Integer.valueOf(map.get("order_id").toString()),map.get("income_status").toString(),UUID.randomUUID().toString(),bean.getBusinessScope());
    			}
    		}
		}
        return true;
    }

    @Override
    public String print(String statementIds, String lang, String businessScope, Boolean isTrue) {
        if (StrUtil.isBlank(statementIds)) {
            throw new RuntimeException("请选择清单");
        }
        LambdaQueryWrapper<Statement> wrapper = Wrappers.<Statement>lambdaQuery();
        wrapper.eq(Statement::getOrgId, SecurityUtils.getUser().getOrgId());
        wrapper.in(Statement::getStatementId, Arrays.asList(statementIds.split(",")));
        List<Statement> statementList = baseMapper.selectList(wrapper);
        //查询清单下的所有账单
        String debitNoteIds = "";
        if (isTrue == true) {
            debitNoteIds = baseMapper.getDebitNoteIds(statementIds, businessScope, SecurityUtils.getUser().getOrgId());
        }
        return printStatementForMany(statementList, lang, businessScope, debitNoteIds);
    }

    public String print2(String statementIds, String lang, String businessScope, Boolean isTrue) {

        LambdaQueryWrapper<Statement> wrapper = Wrappers.<Statement>lambdaQuery();
        wrapper.eq(Statement::getOrgId, SecurityUtils.getUser().getOrgId());
        wrapper.in(Statement::getStatementId, Arrays.asList(statementIds.split(",")));
        List<Statement> statementList = baseMapper.selectList(wrapper);
        //查询清单下的所有账单
        String debitNoteIds = "";
        if (isTrue == true) {
            debitNoteIds = baseMapper.getDebitNoteIds(statementIds, businessScope, SecurityUtils.getUser().getOrgId());
        }
        return printStatementForMany(statementList, lang, businessScope, debitNoteIds);
    }

    @Override
    public void printExcel(String statementId, String lang, String businessScope, Boolean isTrue) {
        Statement statement = getById(statementId);
        if (statement == null) {
            throw new RuntimeException("清单不存在");
        }
        HashMap<String, Object> context = new HashMap<>();

        //整理清单数据
        List<List<StatementPrint>> statementListResult = baseMapper.printStatement(SecurityUtils.getUser().getOrgId(), businessScope, statementId, lang, SecurityUtils.getUser().getId());
        String imageLogoPath = "";
        String imageSealPath = "";
        if (statementListResult != null && statementListResult.size() == 2) {
            StatementPrint tableHeader = statementListResult.get(0).get(0);
            tableHeader.setCreateTime(LocalDateTime.now());
            tableHeader.setCreateTimeStr(tableHeader.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
            tableHeader.setCreatorName(SecurityUtils.getUser().getUserCname());
            tableHeader.setCreatorEname(SecurityUtils.getUser().getUserEname());
            List<StatementPrint> tableBody = statementListResult.get(1);
            DecimalFormat decimalFormat2 = new DecimalFormat("#,###,###,###.##########");
            HashMap<String, Integer> serialMap = new HashMap<>();
            if (tableBody != null && tableBody.size() > 0) {
                tableBody.stream().forEach(statementPrint -> {
                    String str = statementPrint.getChargeableWeight();
                    if (StrUtil.isNotBlank(str)) {
                        if (str.contains(",")) {
                            str = str.replaceAll(",", "");
                        }
                        str = decimalFormat2.format(Double.valueOf(str));
                        statementPrint.setChargeableWeight(str);
                    }
                    if (serialMap.get("serial") == null) {
                        serialMap.put("serial", 1);
                    } else {
                        serialMap.put("serial", serialMap.get("serial") + 1);
                    }
                    statementPrint.setSerial(serialMap.get("serial").toString());
                });
            }

            context.put("tableHeader", tableHeader);
            context.put("tableBody", tableBody);

            //图标处理
            if (StrUtil.isNotBlank(tableHeader.getOrgLogo())) {
                imageLogoPath = PDFUtils.filePath + "/PDFtemplate/temp/debitnote/" + UUID.randomUUID().toString() + "/" + tableHeader.getOrgLogo().substring(tableHeader.getOrgLogo().lastIndexOf("/") + 1, tableHeader.getOrgLogo().length());
                JxlsUtils.downloadFile(tableHeader.getOrgLogo(), imageLogoPath);
                try {
                    byte[] imageBytes = Util.toByteArray(new FileInputStream(imageLogoPath));
                    context.put("Input27", imageBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (StrUtil.isNotBlank(tableHeader.getOrgSeal())) {
                imageSealPath = PDFUtils.filePath + "/PDFtemplate/temp/debitnote/" + UUID.randomUUID().toString() + "/" + tableHeader.getOrgSeal().substring(tableHeader.getOrgSeal().lastIndexOf("/") + 1, tableHeader.getOrgSeal().length());
                JxlsUtils.downloadFile(tableHeader.getOrgSeal(), imageSealPath);
                try {
                    byte[] imageBytes = Util.toByteArray(new FileInputStream(imageSealPath));
                    context.put("Input26", imageBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            throw new RuntimeException("清单不存在或清单数据有误,无法导出");
        }

        //查询清单下的所有账单
        String debitNoteIds = "";
        if (isTrue == true) {

            debitNoteIds = baseMapper.getDebitNoteIds(statementId, businessScope, SecurityUtils.getUser().getOrgId());

            String modelType = "";
            if ("C".equals(lang)) {
                modelType = "CH";
            } else if ("E".equals(lang)) {
                modelType = "EN";
            }
            List<List<Map<String, String>>> list = new ArrayList<List<Map<String, String>>>();
            for (int i = 0; i < debitNoteIds.split(",").length; i++) {
                List<List<Map<String, String>>> listResult = debitNoteMapper.printManyNew(SecurityUtils.getUser().getOrgId(), businessScope, debitNoteIds.split(",")[i], modelType, SecurityUtils.getUser().getId(), null);
                if (listResult != null && listResult.get(0) != null && listResult.get(0).size() > 0) {
                    if (list.size() > 0) {
                        list.get(0).addAll(listResult.get(0));
                        list.get(1).addAll(listResult.get(1));
                    } else {
                        list = listResult;
                    }
                }
            }
            List<Map<String, String>> listDebit = new ArrayList<Map<String, String>>();
            Map<String, List<Map<String, String>>> mapIncome = new HashMap<String, List<Map<String, String>>>();
            if (list != null && list.size() > 1 && list.get(0) != null && list.get(0).size() > 0) {
                listDebit = list.get(0);
            }
            //分组清洗数据
            if (list != null && list.size() > 1 && list.get(1) != null && list.get(1).size() > 0) {
                mapIncome = list.get(1).stream()
                        .collect(Collectors.groupingBy(item -> item.get("debit_note_id").toString()));
            }
            //整理账单数据
            DecimalFormat decimalFormat2 = new DecimalFormat("#,###,###,###.##########");
            ArrayList<Map<String, String>> debitnoteList = new ArrayList<>();
            for (int j = 0; j < listDebit.size(); j++) {
                Map<String, String> item = listDebit.get(j);
                //得到文件的保存目录
                if (mapIncome.get(item.get("debit_note_id").toString()) != null && mapIncome.get(item.get("debit_note_id").toString()).size() > 0) {
                    int index = 1;
                    for (int i = 0; i < mapIncome.get(item.get("debit_note_id").toString()).size(); i++) {
                        Map<String, String> itemIn = mapIncome.get(item.get("debit_note_id").toString()).get(i);
                        int indexM = 1;
                        if (index <= 9) {
                            indexM = 1;
                        } else if (9 < index && index <= 19) {
                            indexM = 2;
                        } else if (19 < index && index <= 29) {
                            indexM = 3;
                        } else if (29 < index && index <= 39) {
                            indexM = 4;
                        }
                        String input1_3 = "";
                        if (StrUtil.isNotBlank(itemIn.get("Input1_3"))) {
                            input1_3 = decimalFormat2.format(Double.valueOf(itemIn.get("Input1_3")));
                        }
                        if (index >= 10) {
                            String indexStr = String.valueOf(index).substring(String.valueOf(index).length() - 1, String.valueOf(index).length());
                            item.put("Input" + indexM + indexStr + "1", itemIn.get("Input1_1").toString());
                            item.put("Input" + indexM + indexStr + "2", itemIn.get("Input1_2").toString());
                            item.put("Input" + indexM + indexStr + "3", input1_3);
                            item.put("Input" + indexM + indexStr + "4", itemIn.get("Input1_4").toString());
                            item.put("Input" + indexM + indexStr + "5", itemIn.get("Input1_5").toString());
                            item.put("Input" + indexM + indexStr + "6", itemIn.get("Input1_6").toString());
                            item.put("Input" + indexM + indexStr + "7", itemIn.get("Input1_7").toString());
                            item.put("Input" + indexM + indexStr + "8", itemIn.get("Input1_8").toString());
                            item.put("Input" + indexM + indexStr + "9", itemIn.get("Input1_9").toString());
                        } else {
                            item.put("Input" + indexM + index + "1", itemIn.get("Input1_1").toString());
                            item.put("Input" + indexM + index + "2", itemIn.get("Input1_2").toString());
                            item.put("Input" + indexM + index + "3", input1_3);
                            item.put("Input" + indexM + index + "4", itemIn.get("Input1_4").toString());
                            item.put("Input" + indexM + index + "5", itemIn.get("Input1_5").toString());
                            item.put("Input" + indexM + index + "6", itemIn.get("Input1_6").toString());
                            item.put("Input" + indexM + index + "7", itemIn.get("Input1_7").toString());
                            item.put("Input" + indexM + index + "8", itemIn.get("Input1_8").toString());
                            item.put("Input" + indexM + index + "9", itemIn.get("Input1_9").toString());
                        }

                        index++;
                    }
                }

                debitnoteList.add(item);
            }
            context.put("data", debitnoteList);
        } else {
            context.put("data", new ArrayList());
        }
        String path = PDFUtils.filePath + "/PDFtemplate/temp/statement/" + new Date().getTime() + "/STATEMENT_temp_" + statementListResult.get(0).get(0).getStatementNum() + ".xlsx";
        String lastPath = PDFUtils.filePath + "/PDFtemplate/temp/statement/" + new Date().getTime() + "/STATEMENT_" + statementListResult.get(0).get(0).getStatementNum() + ".xlsx";

        JxlsUtils.exportExcelToFile(getExcelTemplate(businessScope, lang), path, context);
        PoiUtils.multiplySheetForStatement(lastPath, path, StrUtil.isNotBlank(debitNoteIds) ? debitNoteIds.split(",").length : 0, imageLogoPath, imageSealPath);
        JxlsUtils.responseExcel(lastPath);
    }

    private String getExcelTemplate(String businessScope, String lang) {
        String templatePath = PDFUtils.filePath + "/PDFtemplate/temp/statement/excelTemplate/EF_STATEMENT_" + LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli() + ".xlsx";
        OrgTemplateConfig orgTemplateConfig = remoteServiceToHRS.getOrgTemlateByOrgId(SecurityUtils.getUser().getOrgId()).getData();
        if ("C".equals(lang)) {
            if ("AE".equals(businessScope)) {
                if (orgTemplateConfig == null || StrUtil.isBlank(orgTemplateConfig.getStatementTemplateAeExcelCn())) {
                    OrgTemplateConfig superTemplateConfig = remoteServiceToHRS.getOrgTemlateByOrgId(1).getData();
                    if (superTemplateConfig == null || StrUtil.isBlank(superTemplateConfig.getStatementTemplateAeExcelCn())) {
                        templatePath = PDFUtils.filePath + "/PDFtemplate/EF_STATEMENT.xlsx";
                    } else {
                        JxlsUtils.downloadFile(superTemplateConfig.getStatementTemplateAeExcelCn(), templatePath);
                    }
                } else {
                    JxlsUtils.downloadFile(orgTemplateConfig.getStatementTemplateAeExcelCn(), templatePath);
                }
            } else if ("AI".equals(businessScope)) {
                if (orgTemplateConfig == null || StrUtil.isBlank(orgTemplateConfig.getStatementTemplateAiExcelCn())) {
                    OrgTemplateConfig superTemplateConfig = remoteServiceToHRS.getOrgTemlateByOrgId(1).getData();
                    if (superTemplateConfig == null || StrUtil.isBlank(superTemplateConfig.getStatementTemplateAiExcelCn())) {
                        templatePath = PDFUtils.filePath + "/PDFtemplate/EF_STATEMENT.xlsx";
                    } else {
                        JxlsUtils.downloadFile(superTemplateConfig.getStatementTemplateAiExcelCn(), templatePath);
                    }
                } else {
                    JxlsUtils.downloadFile(orgTemplateConfig.getStatementTemplateAiExcelCn(), templatePath);
                }
            } else if ("SE".equals(businessScope)) {
                if (orgTemplateConfig == null || StrUtil.isBlank(orgTemplateConfig.getStatementTemplateSeExcelCn())) {
                    OrgTemplateConfig superTemplateConfig = remoteServiceToHRS.getOrgTemlateByOrgId(1).getData();
                    if (superTemplateConfig == null || StrUtil.isBlank(superTemplateConfig.getStatementTemplateSeExcelCn())) {
                        templatePath = PDFUtils.filePath + "/PDFtemplate/EF_STATEMENT.xlsx";
                    } else {
                        JxlsUtils.downloadFile(superTemplateConfig.getStatementTemplateSeExcelCn(), templatePath);
                    }
                } else {
                    JxlsUtils.downloadFile(orgTemplateConfig.getStatementTemplateSeExcelCn(), templatePath);
                }
            } else if ("SI".equals(businessScope)) {
                if (orgTemplateConfig == null || StrUtil.isBlank(orgTemplateConfig.getStatementTemplateSiExcelCn())) {
                    OrgTemplateConfig superTemplateConfig = remoteServiceToHRS.getOrgTemlateByOrgId(1).getData();
                    if (superTemplateConfig == null || StrUtil.isBlank(superTemplateConfig.getStatementTemplateSiExcelCn())) {
                        templatePath = PDFUtils.filePath + "/PDFtemplate/EF_STATEMENT.xlsx";
                    } else {
                        JxlsUtils.downloadFile(superTemplateConfig.getStatementTemplateSiExcelCn(), templatePath);
                    }
                } else {
                    JxlsUtils.downloadFile(orgTemplateConfig.getStatementTemplateSiExcelCn(), templatePath);
                }
            } else if ("TE".equals(businessScope)) {
                if (orgTemplateConfig == null || StrUtil.isBlank(orgTemplateConfig.getStatementTemplateTeExcelCn())) {
                    OrgTemplateConfig superTemplateConfig = remoteServiceToHRS.getOrgTemlateByOrgId(1).getData();
                    if (superTemplateConfig == null || StrUtil.isBlank(superTemplateConfig.getStatementTemplateTeExcelCn())) {
                        templatePath = PDFUtils.filePath + "/PDFtemplate/EF_STATEMENT.xlsx";
                    } else {
                        JxlsUtils.downloadFile(superTemplateConfig.getStatementTemplateTeExcelCn(), templatePath);
                    }
                } else {
                    JxlsUtils.downloadFile(orgTemplateConfig.getStatementTemplateTeExcelCn(), templatePath);
                }
            }else if ("TI".equals(businessScope)) {
                if (orgTemplateConfig == null || StrUtil.isBlank(orgTemplateConfig.getStatementTemplateTiExcelCn())) {
                    OrgTemplateConfig superTemplateConfig = remoteServiceToHRS.getOrgTemlateByOrgId(1).getData();
                    if (superTemplateConfig == null || StrUtil.isBlank(superTemplateConfig.getStatementTemplateTiExcelCn())) {
                        templatePath = PDFUtils.filePath + "/PDFtemplate/EF_STATEMENT.xlsx";
                    } else {
                        JxlsUtils.downloadFile(superTemplateConfig.getStatementTemplateTiExcelCn(), templatePath);
                    }
                } else {
                    JxlsUtils.downloadFile(orgTemplateConfig.getStatementTemplateTiExcelCn(), templatePath);
                }
            } else if ("LC".equals(businessScope)) {
                if (orgTemplateConfig == null || StrUtil.isBlank(orgTemplateConfig.getStatementTemplateLcExcelCn())) {
                    OrgTemplateConfig superTemplateConfig = remoteServiceToHRS.getOrgTemlateByOrgId(1).getData();
                    if (superTemplateConfig == null || StrUtil.isBlank(superTemplateConfig.getStatementTemplateLcExcelCn())) {
                        templatePath = PDFUtils.filePath + "/PDFtemplate/EF_STATEMENT.xlsx";
                    } else {
                        JxlsUtils.downloadFile(superTemplateConfig.getStatementTemplateLcExcelCn(), templatePath);
                    }
                } else {
                    JxlsUtils.downloadFile(orgTemplateConfig.getStatementTemplateLcExcelCn(), templatePath);
                }
            } else if ("IO".equals(businessScope)) {
                if (orgTemplateConfig == null || StrUtil.isBlank(orgTemplateConfig.getStatementTemplateIoExcelCn())) {
                    OrgTemplateConfig superTemplateConfig = remoteServiceToHRS.getOrgTemlateByOrgId(1).getData();
                    if (superTemplateConfig == null || StrUtil.isBlank(superTemplateConfig.getStatementTemplateLcExcelCn())) {
                        templatePath = PDFUtils.filePath + "/PDFtemplate/EF_STATEMENT.xlsx";
                    } else {
                        JxlsUtils.downloadFile(superTemplateConfig.getStatementTemplateIoExcelCn(), templatePath);
                    }
                } else {
                    JxlsUtils.downloadFile(orgTemplateConfig.getStatementTemplateIoExcelCn(), templatePath);
                }
            }
        } else if ("E".equals(lang)) {
            if ("AE".equals(businessScope)) {
                if (orgTemplateConfig == null || StrUtil.isBlank(orgTemplateConfig.getStatementTemplateAeExcelEn())) {
                    OrgTemplateConfig superTemplateConfig = remoteServiceToHRS.getOrgTemlateByOrgId(1).getData();
                    if (superTemplateConfig == null || StrUtil.isBlank(superTemplateConfig.getStatementTemplateAeExcelEn())) {
                        templatePath = PDFUtils.filePath + "/PDFtemplate/EF_STATEMENT_EN.xlsx";
                    } else {
                        JxlsUtils.downloadFile(superTemplateConfig.getStatementTemplateAeExcelEn(), templatePath);
                    }
                } else {
                    JxlsUtils.downloadFile(orgTemplateConfig.getStatementTemplateAeExcelEn(), templatePath);
                }
            } else if ("AI".equals(businessScope)) {
                if (orgTemplateConfig == null || StrUtil.isBlank(orgTemplateConfig.getStatementTemplateAiExcelEn())) {
                    OrgTemplateConfig superTemplateConfig = remoteServiceToHRS.getOrgTemlateByOrgId(1).getData();
                    if (superTemplateConfig == null || StrUtil.isBlank(superTemplateConfig.getStatementTemplateAiExcelEn())) {
                        templatePath = PDFUtils.filePath + "/PDFtemplate/EF_STATEMENT_EN.xlsx";
                    } else {
                        JxlsUtils.downloadFile(superTemplateConfig.getStatementTemplateAiExcelEn(), templatePath);
                    }
                } else {
                    JxlsUtils.downloadFile(orgTemplateConfig.getStatementTemplateAiExcelEn(), templatePath);
                }
            } else if ("SE".equals(businessScope)) {
                if (orgTemplateConfig == null || StrUtil.isBlank(orgTemplateConfig.getStatementTemplateSeExcelEn())) {
                    OrgTemplateConfig superTemplateConfig = remoteServiceToHRS.getOrgTemlateByOrgId(1).getData();
                    if (superTemplateConfig == null || StrUtil.isBlank(superTemplateConfig.getStatementTemplateSeExcelEn())) {
                        templatePath = PDFUtils.filePath + "/PDFtemplate/EF_STATEMENT_EN.xlsx";
                    } else {
                        JxlsUtils.downloadFile(superTemplateConfig.getStatementTemplateSeExcelEn(), templatePath);
                    }
                } else {
                    JxlsUtils.downloadFile(orgTemplateConfig.getStatementTemplateSeExcelEn(), templatePath);
                }
            } else if ("SI".equals(businessScope)) {
                if (orgTemplateConfig == null || StrUtil.isBlank(orgTemplateConfig.getStatementTemplateSiExcelEn())) {
                    OrgTemplateConfig superTemplateConfig = remoteServiceToHRS.getOrgTemlateByOrgId(1).getData();
                    if (superTemplateConfig == null || StrUtil.isBlank(superTemplateConfig.getStatementTemplateSiExcelEn())) {
                        templatePath = PDFUtils.filePath + "/PDFtemplate/EF_STATEMENT_EN.xlsx";
                    } else {
                        JxlsUtils.downloadFile(superTemplateConfig.getStatementTemplateSiExcelEn(), templatePath);
                    }
                } else {
                    JxlsUtils.downloadFile(orgTemplateConfig.getStatementTemplateSiExcelEn(), templatePath);
                }
            } else if ("TE".equals(businessScope)) {
                if (orgTemplateConfig == null || StrUtil.isBlank(orgTemplateConfig.getStatementTemplateTeExcelEn())) {
                    OrgTemplateConfig superTemplateConfig = remoteServiceToHRS.getOrgTemlateByOrgId(1).getData();
                    if (superTemplateConfig == null || StrUtil.isBlank(superTemplateConfig.getStatementTemplateTeExcelEn())) {
                        templatePath = PDFUtils.filePath + "/PDFtemplate/EF_STATEMENT_EN.xlsx";
                    } else {
                        JxlsUtils.downloadFile(superTemplateConfig.getStatementTemplateTeExcelEn(), templatePath);
                    }
                } else {
                    JxlsUtils.downloadFile(orgTemplateConfig.getStatementTemplateTeExcelEn(), templatePath);
                }
            }else if ("TI".equals(businessScope)) {
                if (orgTemplateConfig == null || StrUtil.isBlank(orgTemplateConfig.getStatementTemplateTiExcelEn())) {
                    OrgTemplateConfig superTemplateConfig = remoteServiceToHRS.getOrgTemlateByOrgId(1).getData();
                    if (superTemplateConfig == null || StrUtil.isBlank(superTemplateConfig.getStatementTemplateTiExcelEn())) {
                        templatePath = PDFUtils.filePath + "/PDFtemplate/EF_STATEMENT_EN.xlsx";
                    } else {
                        JxlsUtils.downloadFile(superTemplateConfig.getStatementTemplateTiExcelEn(), templatePath);
                    }
                } else {
                    JxlsUtils.downloadFile(orgTemplateConfig.getStatementTemplateTiExcelEn(), templatePath);
                }
            } else if ("LC".equals(businessScope)) {
                if (orgTemplateConfig == null || StrUtil.isBlank(orgTemplateConfig.getStatementTemplateLcExcelEn())) {
                    OrgTemplateConfig superTemplateConfig = remoteServiceToHRS.getOrgTemlateByOrgId(1).getData();
                    if (superTemplateConfig == null || StrUtil.isBlank(superTemplateConfig.getStatementTemplateLcExcelEn())) {
                        templatePath = PDFUtils.filePath + "/PDFtemplate/EF_STATEMENT_EN.xlsx";
                    } else {
                        JxlsUtils.downloadFile(superTemplateConfig.getStatementTemplateLcExcelEn(), templatePath);
                    }
                } else {
                    JxlsUtils.downloadFile(orgTemplateConfig.getStatementTemplateLcExcelEn(), templatePath);
                }
            } else if ("IO".equals(businessScope)) {
                if (orgTemplateConfig == null || StrUtil.isBlank(orgTemplateConfig.getStatementTemplateIoExcelEn())) {
                    OrgTemplateConfig superTemplateConfig = remoteServiceToHRS.getOrgTemlateByOrgId(1).getData();
                    if (superTemplateConfig == null || StrUtil.isBlank(superTemplateConfig.getStatementTemplateIoExcelEn())) {
                        templatePath = PDFUtils.filePath + "/PDFtemplate/EF_STATEMENT_EN.xlsx";
                    } else {
                        JxlsUtils.downloadFile(superTemplateConfig.getStatementTemplateIoExcelEn(), templatePath);
                    }
                } else {
                    JxlsUtils.downloadFile(orgTemplateConfig.getStatementTemplateIoExcelEn(), templatePath);
                }
            }
        }

        return templatePath;
    }

    @Override
    @SneakyThrows
    public void exportSettleStatementExcel(String statementId, String lang, String businessScope) {
        if (getById(statementId) == null) {
            throw new RuntimeException("清单不存在，导出失败");
        }
        LambdaQueryWrapper<DebitNote> debitNoteWrapper = Wrappers.<DebitNote>lambdaQuery();
        debitNoteWrapper.eq(DebitNote::getStatementId, statementId).eq(DebitNote::getOrgId, SecurityUtils.getUser().getOrgId());
        if (debitNoteService.list(debitNoteWrapper).size() == 0) {
            throw new RuntimeException("清单异常：找不见账单，导出失败");
        }
        List<List<SettleStatement>> lists = settleStatementMapper.querySettleStatement(SecurityUtils.getUser().getOrgId(), businessScope, statementId, lang);
        if (lists.size() != 3) {
            throw new RuntimeException("清单异常：找不见费用，导出失败");
        }
        //整理所有账单费用明细去重
//        HashMap<Integer, String> serviceMap = new HashMap<>();
        HashMap<String, String> serviceMap = new HashMap<>();
        lists.get(2).stream().forEach(service -> {
            if (serviceMap.get(service.getServiceId() + service.getIncomeCurrency()) == null) {
                serviceMap.put(service.getServiceId() + service.getIncomeCurrency(), service.getServiceName() + "(" + service.getIncomeCurrency() + ")");
            }
        });
//        List<Integer> serviceIds = lists.get(2).stream().map(SettleStatement::getServiceId).distinct().collect(Collectors.toList());
        List<String> serviceIds = lists.get(2).stream().map(a -> a.getServiceId() + "_" + a.getIncomeCurrency()).distinct().collect(Collectors.toList());

        //账单币种明细
        List<SettleStatement> listcurren = settleStatementMapper.queryCurrencyList(SecurityUtils.getUser().getOrgId(), businessScope, Integer.valueOf(statementId));
        HashMap<String, BigDecimal> mapCurrency = new HashMap<>();
        HashMap<String, String> mapCurrencyTwo = new HashMap<>();
        listcurren.stream().forEach(o -> {
            if (mapCurrency.containsKey(o.getIncomeCurrency())) {
                mapCurrency.put(o.getIncomeCurrency(), mapCurrency.get(o.getIncomeCurrency()).add(o.getFunctionalAmount()));
            } else {
                mapCurrency.put(o.getIncomeCurrency(), o.getFunctionalAmount());
            }
        });
        //序列号
        HashMap<String, Integer> serialMap = new HashMap<>();
        serialMap.put("serial", 0);
        lists.get(1).stream().forEach(debiteNote -> {
            serialMap.put("serial", serialMap.get("serial") + 1);
            debiteNote.setSerial(serialMap.get("serial").toString());
            ArrayList<SettleStatement> services = new ArrayList<>();
            serviceIds.stream().forEach(serviceId -> {
                String sID = serviceId.split("_")[0];
                String incomeCurrency = serviceId.split("_")[1];
                List<SettleStatement> tempServices = lists.get(2).stream().filter(service -> service.getDebitNoteId().intValue() == debiteNote.getDebitNoteId().intValue() && service.getServiceId().intValue() == Integer.valueOf(sID).intValue() && incomeCurrency.equals(service.getIncomeCurrency())).collect(Collectors.toList());
                SettleStatement income = new SettleStatement();
                income.setServiceName(serviceMap.get(sID + incomeCurrency));
                income.setServiceId(Integer.valueOf(sID));
                income.setIncomeCurrency(incomeCurrency);
                if (tempServices.size() == 0) {
                    income.setFunctionalAmount(null);
                } else if (tempServices.size() == 1) {
                    income.setFunctionalAmount(tempServices.get(0).getFunctionalAmount());
                    income.setFunctionalAmountStr(tempServices.get(0).getFunctionalAmountStr());
                } else {
                    HashMap<String, BigDecimal> sum = new HashMap<>();
                    sum.put("sum", BigDecimal.ZERO);
                    tempServices.stream().forEach(tempService -> {
                        sum.put("sum", sum.get("sum").add(tempService.getFunctionalAmount()));
                    });
                    income.setFunctionalAmount(sum.get("sum"));
                    income.setFunctionalAmountStr(FormatUtils.formatWithQWF(sum.get("sum"), 2));
                }
                services.add(income);
            });
            if (listcurren != null && listcurren.size() > 0) {
                List<SettleStatement> listOne = new ArrayList<SettleStatement>();
                List<SettleStatement> listTwo = listcurren.stream().filter(o -> o.getDebitNoteId().intValue() == debiteNote.getDebitNoteId().intValue()).collect(Collectors.toList());

                for (String key : mapCurrency.keySet()) {
                    SettleStatement s = new SettleStatement();
                    s.setIncomeCurrency("合计(" + key + ")");
                    stename:
                    for (SettleStatement ste : listTwo) {
                        if (ste.getIncomeCurrency().equals(key)) {
                            s.setFunctionalAmountStr(FormatUtils.formatWithQWF(ste.getFunctionalAmount(), 2));
                            s.setFunctionalAmount(ste.getFunctionalAmount());
                            if (!mapCurrencyTwo.containsKey(key)) {
                                mapCurrencyTwo.put(key, key);
                            }
                            break stename;
                        }
                    }
                    listOne.add(s);
                }
                debiteNote.setCurrencyList(listOne);
            }
            debiteNote.setServiceList(services);
        });
        //计算合计
        SettleStatement sumSettleStatement = new SettleStatement();
        ArrayList<SettleStatement> sumService = new ArrayList<>();

        serviceIds.stream().forEach(serviceId -> {
            String sID = serviceId.split("_")[0];
            String incomeCurrency = serviceId.split("_")[1];
            SettleStatement sumServiceEachServiceName = new SettleStatement();
            lists.get(1).stream().forEach(debitNote -> {
                BigDecimal functionalAmount = debitNote.getServiceList().stream().filter(service -> service.getServiceId().intValue() == Integer.valueOf(sID).intValue() && incomeCurrency.equals(service.getIncomeCurrency())).collect(Collectors.toList()).get(0).getFunctionalAmount();
                if (functionalAmount != null) {
                    if (sumServiceEachServiceName.getFunctionalAmount() == null) {
                        sumServiceEachServiceName.setFunctionalAmount(functionalAmount);
                    } else {
                        sumServiceEachServiceName.setFunctionalAmount(sumServiceEachServiceName.getFunctionalAmount().add(functionalAmount));
                    }
                }
            });
            sumServiceEachServiceName.setFunctionalAmountStr(FormatUtils.formatWithQWF(sumServiceEachServiceName.getFunctionalAmount(), 2));
            sumService.add(sumServiceEachServiceName);
        });
        sumSettleStatement.setServiceList(sumService);
        //币种合计
        List<SettleStatement> listThree = new ArrayList<SettleStatement>();
        StringBuffer sb = new StringBuffer();
        for (String key : mapCurrency.keySet()) {
            SettleStatement s = new SettleStatement();
            s.setIncomeCurrency("合计(" + key + ")");
            s.setFunctionalAmount(mapCurrency.get(key));
            s.setFunctionalAmountStr((FormatUtils.formatWithQWF(mapCurrency.get(key), 2)));
            listThree.add(s);
            sb.append("(").append(key).append(")").append(s.getFunctionalAmountStr()).append("、");
        }
        sumSettleStatement.setCurrencyList(listThree);
        sumSettleStatement.setSerial("合计：");

        HashMap<String, Integer> pieceSumMap = new HashMap<>();
        pieceSumMap.put("piecesSum", 0);
        HashMap<String, BigDecimal> chargeWeightSumMap = new HashMap<>();
        chargeWeightSumMap.put("chargeWeightSum", BigDecimal.ZERO);
        HashMap<String, BigDecimal> weightSumMap = new HashMap<>();
        weightSumMap.put("weightSum", BigDecimal.ZERO);
        HashMap<String, BigDecimal> volumeSumMap = new HashMap<>();
        volumeSumMap.put("volumeSum", BigDecimal.ZERO);
        HashMap<String, BigDecimal> amountSumMap = new HashMap<>();
        amountSumMap.put("amountSum", BigDecimal.ZERO);
        lists.get(1).stream().forEach(debitNote -> {
            if (debitNote.getPieces() != null) {
                pieceSumMap.put("piecesSum", pieceSumMap.get("piecesSum") + debitNote.getPieces());
            }
            if (debitNote.getChargeableWeight() != null) {
                chargeWeightSumMap.put("chargeWeightSum", chargeWeightSumMap.get("chargeWeightSum").add(debitNote.getChargeableWeight()));
            }
            if (debitNote.getWeight() != null) {
                weightSumMap.put("weightSum", weightSumMap.get("weightSum").add(debitNote.getWeight()));
            }
            if (debitNote.getVolume() != null) {
                volumeSumMap.put("volumeSum", volumeSumMap.get("volumeSum").add(debitNote.getVolume()));
            }
            if (debitNote.getFunctionalAmount() != null) {
                amountSumMap.put("amountSum", amountSumMap.get("amountSum").add(debitNote.getFunctionalAmount()));
            }
        });
        sumSettleStatement.setPieces(pieceSumMap.get("piecesSum"));
        sumSettleStatement.setChargeableWeight(chargeWeightSumMap.get("chargeWeightSum"));
        sumSettleStatement.setWeight(weightSumMap.get("weightSum"));
        sumSettleStatement.setVolume(volumeSumMap.get("volumeSum"));
        sumSettleStatement.setFunctionalAmountSum(FormatUtils.formatWithQWF(amountSumMap.get("amountSum"), 2));

        lists.get(1).add(sumSettleStatement);

        ArrayList<String> serviceNames = new ArrayList<>();
//        lists.get(2).stream().map(SettleStatement::getServiceId).distinct().forEach(serviceId -> {
//            serviceNames.add(serviceMap.get(serviceId));
//        });
        serviceIds.forEach(serviceId -> {
            String sID = serviceId.split("_")[0];
            String incomeCurrency = serviceId.split("_")[1];
            serviceNames.add(serviceMap.get(sID + incomeCurrency));
        });
        ArrayList<String> incomeCurrencys = new ArrayList<>();
//        listcurren.stream().map(SettleStatement::getIncomeCurrency).distinct().forEach(incomeCurrency -> {
//        	incomeCurrencys.add("合计("+incomeCurrency+")");
//        });
        for (String key : mapCurrencyTwo.keySet()) {
            incomeCurrencys.add("合计(" + key + ")");
        }


        HashMap<String, Object> context = new HashMap<>();
        if (StrUtil.isNotBlank(lists.get(0).get(0).getOrgLogo())) {
            String imagePath = PDFUtils.filePath + "/PDFtemplate/temp/settlestatement/" + UUID.randomUUID().toString() + "/" + lists.get(0).get(0).getOrgLogo().substring(lists.get(0).get(0).getOrgLogo().lastIndexOf("/") + 1, lists.get(0).get(0).getOrgLogo().length());
            JxlsUtils.downloadFile(lists.get(0).get(0).getOrgLogo(), imagePath);
            try {
                byte[] imageBytes = Util.toByteArray(new FileInputStream(imagePath));
                context.put("orgLogo", imageBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (StrUtil.isNotBlank(lists.get(0).get(0).getOrgSeal())) {
            String imagePath = PDFUtils.filePath + "/PDFtemplate/temp/settlestatement/" + UUID.randomUUID().toString() + "/" + lists.get(0).get(0).getOrgSeal().substring(lists.get(0).get(0).getOrgSeal().lastIndexOf("/") + 1, lists.get(0).get(0).getOrgSeal().length());
            JxlsUtils.downloadFile(lists.get(0).get(0).getOrgSeal(), imagePath);
            try {
                byte[] imageBytes = Util.toByteArray(new FileInputStream(imagePath));
                context.put("orgSeal", imageBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //当前制单时间
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
        context.put("currowTime", sdf.format(new Date()));
        context.put("currowUserName", SecurityUtils.getUser().getUserCname());
        context.put("statement", lists.get(0).get(0));
        context.put("debitnoteList", lists.get(1));
        context.put("serviceNames", serviceNames);
        context.put("incomeCurrencys", incomeCurrencys);
        context.put("currencyAmountStr", sb.toString().substring(0, sb.toString().length() - 1));
        JxlsUtils.exportExcelWithLocalModel(PDFUtils.filePath + "/PDFtemplate/EF_STATEMENT_LIST.xlsx", context);
    }


    public String sendStatementExcel(String statementId, String lang, String businessScope) {
        if (getById(statementId) == null) {
            throw new RuntimeException("清单不存在，发送失败");
        }
        LambdaQueryWrapper<DebitNote> debitNoteWrapper = Wrappers.<DebitNote>lambdaQuery();
        debitNoteWrapper.eq(DebitNote::getStatementId, statementId).eq(DebitNote::getOrgId, SecurityUtils.getUser().getOrgId());
        if (debitNoteService.list(debitNoteWrapper).size() == 0) {
            throw new RuntimeException("清单异常：找不见账单，发送失败");
        }
        List<List<SettleStatement>> lists = settleStatementMapper.querySettleStatement(SecurityUtils.getUser().getOrgId(), businessScope, statementId, lang);
        if (lists.size() != 3) {
            throw new RuntimeException("清单异常：找不见费用，发送失败");
        }
        //整理所有账单费用明细去重
//      HashMap<Integer, String> serviceMap = new HashMap<>();
        HashMap<String, String> serviceMap = new HashMap<>();
        lists.get(2).stream().forEach(service -> {
            if (serviceMap.get(service.getServiceId() + service.getIncomeCurrency()) == null) {
                serviceMap.put(service.getServiceId() + service.getIncomeCurrency(), service.getServiceName() + "(" + service.getIncomeCurrency() + ")");
            }
        });
//      List<Integer> serviceIds = lists.get(2).stream().map(SettleStatement::getServiceId).distinct().collect(Collectors.toList());
        List<String> serviceIds = lists.get(2).stream().map(a -> a.getServiceId() + "_" + a.getIncomeCurrency()).distinct().collect(Collectors.toList());

        //账单币种明细
        List<SettleStatement> listcurren = settleStatementMapper.queryCurrencyList(SecurityUtils.getUser().getOrgId(), businessScope, Integer.valueOf(statementId));
        HashMap<String, BigDecimal> mapCurrency = new HashMap<>();
        HashMap<String, String> mapCurrencyTwo = new HashMap<>();
        listcurren.stream().forEach(o -> {
            if (mapCurrency.containsKey(o.getIncomeCurrency())) {
                mapCurrency.put(o.getIncomeCurrency(), mapCurrency.get(o.getIncomeCurrency()).add(o.getFunctionalAmount()));
            } else {
                mapCurrency.put(o.getIncomeCurrency(), o.getFunctionalAmount());
            }
        });
        //序列号
        HashMap<String, Integer> serialMap = new HashMap<>();
        serialMap.put("serial", 0);
        lists.get(1).stream().forEach(debiteNote -> {
            serialMap.put("serial", serialMap.get("serial") + 1);
            debiteNote.setSerial(serialMap.get("serial").toString());
            ArrayList<SettleStatement> services = new ArrayList<>();
            serviceIds.stream().forEach(serviceId -> {
                String sID = serviceId.split("_")[0];
                String incomeCurrency = serviceId.split("_")[1];
                List<SettleStatement> tempServices = lists.get(2).stream().filter(service -> service.getDebitNoteId().intValue() == debiteNote.getDebitNoteId().intValue() && service.getServiceId().intValue() == Integer.valueOf(sID).intValue() && incomeCurrency.equals(service.getIncomeCurrency())).collect(Collectors.toList());
                SettleStatement income = new SettleStatement();
                income.setServiceName(serviceMap.get(sID + incomeCurrency));
                income.setServiceId(Integer.valueOf(sID));
                income.setIncomeCurrency(incomeCurrency);
                if (tempServices.size() == 0) {
                    income.setFunctionalAmount(null);
                } else if (tempServices.size() == 1) {
                    income.setFunctionalAmount(tempServices.get(0).getFunctionalAmount());
                    income.setFunctionalAmountStr(tempServices.get(0).getFunctionalAmountStr());
                } else {
                    HashMap<String, BigDecimal> sum = new HashMap<>();
                    sum.put("sum", BigDecimal.ZERO);
                    tempServices.stream().forEach(tempService -> {
                        sum.put("sum", sum.get("sum").add(tempService.getFunctionalAmount()));
                    });
                    income.setFunctionalAmount(sum.get("sum"));
                    income.setFunctionalAmountStr(FormatUtils.formatWithQWF(sum.get("sum"), 2));
                }
                services.add(income);
            });
            if (listcurren != null && listcurren.size() > 0) {
                List<SettleStatement> listOne = new ArrayList<SettleStatement>();
                List<SettleStatement> listTwo = listcurren.stream().filter(o -> o.getDebitNoteId().intValue() == debiteNote.getDebitNoteId().intValue()).collect(Collectors.toList());

                for (String key : mapCurrency.keySet()) {
                    SettleStatement s = new SettleStatement();
                    s.setIncomeCurrency("合计(" + key + ")");
                    stename:
                    for (SettleStatement ste : listTwo) {
                        if (ste.getIncomeCurrency().equals(key)) {
                            s.setFunctionalAmountStr(FormatUtils.formatWithQWF(ste.getFunctionalAmount(), 2));
                            s.setFunctionalAmount(ste.getFunctionalAmount());
                            if (!mapCurrencyTwo.containsKey(key)) {
                                mapCurrencyTwo.put(key, key);
                            }
                            break stename;
                        }
                    }
                    listOne.add(s);
                }
                debiteNote.setCurrencyList(listOne);
            }
            debiteNote.setServiceList(services);
        });
        //计算合计
        SettleStatement sumSettleStatement = new SettleStatement();
        ArrayList<SettleStatement> sumService = new ArrayList<>();

        serviceIds.stream().forEach(serviceId -> {
            String sID = serviceId.split("_")[0];
            String incomeCurrency = serviceId.split("_")[1];
            SettleStatement sumServiceEachServiceName = new SettleStatement();
            lists.get(1).stream().forEach(debitNote -> {
                BigDecimal functionalAmount = debitNote.getServiceList().stream().filter(service -> service.getServiceId().intValue() == Integer.valueOf(sID).intValue() && incomeCurrency.equals(service.getIncomeCurrency())).collect(Collectors.toList()).get(0).getFunctionalAmount();
                if (functionalAmount != null) {
                    if (sumServiceEachServiceName.getFunctionalAmount() == null) {
                        sumServiceEachServiceName.setFunctionalAmount(functionalAmount);
                    } else {
                        sumServiceEachServiceName.setFunctionalAmount(sumServiceEachServiceName.getFunctionalAmount().add(functionalAmount));
                    }
                }
            });
            sumServiceEachServiceName.setFunctionalAmountStr(FormatUtils.formatWithQWF(sumServiceEachServiceName.getFunctionalAmount(), 2));
            sumService.add(sumServiceEachServiceName);
        });
        sumSettleStatement.setServiceList(sumService);
        //币种合计
        List<SettleStatement> listThree = new ArrayList<SettleStatement>();
        StringBuffer sb = new StringBuffer();
        for (String key : mapCurrency.keySet()) {
            SettleStatement s = new SettleStatement();
            s.setIncomeCurrency("合计(" + key + ")");
            s.setFunctionalAmount(mapCurrency.get(key));
            s.setFunctionalAmountStr((FormatUtils.formatWithQWF(mapCurrency.get(key), 2)));
            listThree.add(s);
            sb.append("(").append(key).append(")").append(s.getFunctionalAmountStr()).append("、");
        }
        sumSettleStatement.setCurrencyList(listThree);
        sumSettleStatement.setSerial("合计：");

        HashMap<String, Integer> pieceSumMap = new HashMap<>();
        pieceSumMap.put("piecesSum", 0);
        HashMap<String, BigDecimal> chargeWeightSumMap = new HashMap<>();
        chargeWeightSumMap.put("chargeWeightSum", BigDecimal.ZERO);
        HashMap<String, BigDecimal> weightSumMap = new HashMap<>();
        weightSumMap.put("weightSum", BigDecimal.ZERO);
        HashMap<String, BigDecimal> volumeSumMap = new HashMap<>();
        volumeSumMap.put("volumeSum", BigDecimal.ZERO);
        HashMap<String, BigDecimal> amountSumMap = new HashMap<>();
        amountSumMap.put("amountSum", BigDecimal.ZERO);
        lists.get(1).stream().forEach(debitNote -> {
            if (debitNote.getPieces() != null) {
                pieceSumMap.put("piecesSum", pieceSumMap.get("piecesSum") + debitNote.getPieces());
            }
            if (debitNote.getChargeableWeight() != null) {
                chargeWeightSumMap.put("chargeWeightSum", chargeWeightSumMap.get("chargeWeightSum").add(debitNote.getChargeableWeight()));
            }
            if (debitNote.getWeight() != null) {
                weightSumMap.put("weightSum", weightSumMap.get("weightSum").add(debitNote.getWeight()));
            }
            if (debitNote.getVolume() != null) {
                volumeSumMap.put("volumeSum", volumeSumMap.get("volumeSum").add(debitNote.getVolume()));
            }
            if (debitNote.getFunctionalAmount() != null) {
                amountSumMap.put("amountSum", amountSumMap.get("amountSum").add(debitNote.getFunctionalAmount()));
            }
        });
        sumSettleStatement.setPieces(pieceSumMap.get("piecesSum"));
        sumSettleStatement.setChargeableWeight(chargeWeightSumMap.get("chargeWeightSum"));
        sumSettleStatement.setWeight(weightSumMap.get("weightSum"));
        sumSettleStatement.setVolume(volumeSumMap.get("volumeSum"));
        sumSettleStatement.setFunctionalAmountSum(FormatUtils.formatWithQWF(amountSumMap.get("amountSum"), 2));

        lists.get(1).add(sumSettleStatement);

        ArrayList<String> serviceNames = new ArrayList<>();
//      lists.get(2).stream().map(SettleStatement::getServiceId).distinct().forEach(serviceId -> {
//          serviceNames.add(serviceMap.get(serviceId));
//      });
        serviceIds.forEach(serviceId -> {
            String sID = serviceId.split("_")[0];
            String incomeCurrency = serviceId.split("_")[1];
            serviceNames.add(serviceMap.get(sID + incomeCurrency));
        });
        ArrayList<String> incomeCurrencys = new ArrayList<>();
//      listcurren.stream().map(SettleStatement::getIncomeCurrency).distinct().forEach(incomeCurrency -> {
//      	incomeCurrencys.add("合计("+incomeCurrency+")");
//      });
        for (String key : mapCurrencyTwo.keySet()) {
            incomeCurrencys.add("合计(" + key + ")");
        }


        HashMap<String, Object> context = new HashMap<>();
        if (StrUtil.isNotBlank(lists.get(0).get(0).getOrgLogo())) {
            String imagePath = PDFUtils.filePath + "/PDFtemplate/temp/settlestatement/" + UUID.randomUUID().toString() + "/" + lists.get(0).get(0).getOrgLogo().substring(lists.get(0).get(0).getOrgLogo().lastIndexOf("/") + 1, lists.get(0).get(0).getOrgLogo().length());
            JxlsUtils.downloadFile(lists.get(0).get(0).getOrgLogo(), imagePath);
            try {
                byte[] imageBytes = Util.toByteArray(new FileInputStream(imagePath));
                context.put("orgLogo", imageBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (StrUtil.isNotBlank(lists.get(0).get(0).getOrgSeal())) {
            String imagePath = PDFUtils.filePath + "/PDFtemplate/temp/settlestatement/" + UUID.randomUUID().toString() + "/" + lists.get(0).get(0).getOrgSeal().substring(lists.get(0).get(0).getOrgSeal().lastIndexOf("/") + 1, lists.get(0).get(0).getOrgSeal().length());
            JxlsUtils.downloadFile(lists.get(0).get(0).getOrgSeal(), imagePath);
            try {
                byte[] imageBytes = Util.toByteArray(new FileInputStream(imagePath));
                context.put("orgSeal", imageBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //当前制单时间
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
        context.put("currowTime", sdf.format(new Date()));
        context.put("currowUserName", SecurityUtils.getUser().getUserCname());
        context.put("statement", lists.get(0).get(0));
        context.put("debitnoteList", lists.get(1));
        context.put("serviceNames", serviceNames);
        context.put("incomeCurrencys", incomeCurrencys);
        context.put("currencyAmountStr", sb.toString().substring(0, sb.toString().length() - 1));
        String lastFilePath = PDFUtils.filePath + "/PDFtemplate/temp/statement/excel/STATEMENT_" + statementId + "_" + UUID.randomUUID().toString() + "_" + new Date().getTime() + ".xlsx";
        JxlsUtils.exportExcelToFile(PDFUtils.filePath + "/PDFtemplate/EF_STATEMENT_LIST.xlsx", lastFilePath, context);
        return lastFilePath.replace(PDFUtils.filePath, "");
    }

    @Override
    public boolean send(ListSend debitNoteSendEntity) {
        if (StrUtil.isBlank(debitNoteSendEntity.getReceiver())) {
            throw new RuntimeException("收件人不能为空");
        }
        if (StrUtil.isBlank(debitNoteSendEntity.getStatementId())) {
            throw new RuntimeException("清单号不能为空");
        }
        if (StrUtil.isBlank(debitNoteSendEntity.getTemplateType())) {
            throw new RuntimeException("模板类型不能为空");
        }
        String ccUser = "";
        if (StrUtil.isNotBlank(debitNoteSendEntity.getCcUser())) {
            ccUser = debitNoteSendEntity.getCcUser();
        }
        String path = null;
        if ("S".equals(debitNoteSendEntity.getTemplateType())) {
            path = this.sendStatementExcel(debitNoteSendEntity.getStatementId(), "C", "AE");
        } else {
            path = this.print2(debitNoteSendEntity.getStatementId(), debitNoteSendEntity.getTemplateType(), debitNoteSendEntity.getBusinessScope(), debitNoteSendEntity.getIsTrue());
        }
        String filePath = PDFUtils.filePath + path;
        ArrayList<Map<String, String>> fileList = new ArrayList<>();
        HashMap<String, String> fileMap = new HashMap<>();
        fileMap.put("name", filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length()));
        fileMap.put("path", filePath);
        fileMap.put("flag", "local");
        fileList.add(fileMap);

        String content = debitNoteSendEntity.getContent();
        StringBuilder builder = new StringBuilder();
        builder.append(content.replaceAll("\n", "<br />"));
        mailSendService.sendAttachmentsMailNew(true, debitNoteSendEntity.getReceiver().split(","), ccUser.split(","), null, debitNoteSendEntity.getSubject(), builder.toString(), fileList, null);
        return true;
    }

    @SneakyThrows
    public String printStatementForMany(List<Statement> list, String lang, String businessScope, String
            debitNoteIds) {
        ArrayList<String> filePathList = new ArrayList<>();
        list.stream().forEach(statement -> {
            filePathList.add(printStatement(statement, lang, false, businessScope));
        });

        //打印明细选择是，拼接账单明细PDF
        if (!StrUtil.isEmpty(debitNoteIds)) {
            List<String> detailPathList = debitNoteService.printManyNewForStatementPrint(lang, debitNoteIds, businessScope);
            if (detailPathList != null && detailPathList.size() > 0) {
                filePathList.addAll(detailPathList);
            }
        }

        //拼接多个PDF文件
        String lastFilePath = PDFUtils.filePath + "/PDFtemplate/temp/statement/print/STATEMENT_" + new Date().getTime() + ".pdf";
        if (list.size() == 1) {
            lastFilePath = PDFUtils.filePath + "/PDFtemplate/temp/statement/print/STATEMENT_" + list.get(0).getStatementNum() + "_" + new Date().getTime() + ".pdf";
        }
        if ("E".equals(lang)) {
            PDFUtils.loadAllPDFForFile(filePathList, lastFilePath, PDFUtils.PAGE_EN);
        } else if ("C".equals(lang)) {
            PDFUtils.loadAllPDFForFile(filePathList, lastFilePath, PDFUtils.PAGE_CH);
        }
        return lastFilePath.replace(PDFUtils.filePath, "");
    }

    @SneakyThrows
    public String printStatement(Statement statement, String lang, Boolean replace, String businessScope) {
        String savePath = PDFUtils.filePath + "/PDFtemplate/temp/statement";
        String name = "statement_" + statement.getStatementNum();
        // 得到文件保存的名称
        String saveFilename = PDFUtils.makeFileName(name + ".pdf");
        // 得到文件的保存目录
        String newPDFPath = PDFUtils.makePath(saveFilename, savePath) + "/" + saveFilename;

        List<List<StatementPrint>> listResult = baseMapper.printStatement(SecurityUtils.getUser().getOrgId(), businessScope, statement.getStatementId().toString(), lang, SecurityUtils.getUser().getId());
        if (listResult != null && listResult.size() == 2) {
            StatementPrint tableHeader = listResult.get(0).get(0);
            //StatementPrint tableHeader = baseMapper.queryStatementPrintHeaderInfoByStatementId(statement.getStatementId(), SecurityUtils.getUser().getOrgId());
            tableHeader.setCreateTime(LocalDateTime.now());
            tableHeader.setCreatorName(SecurityUtils.getUser().getUserCname());
            tableHeader.setCreatorEname(SecurityUtils.getUser().getUserEname());
            //List<StatementPrint> tableBody = baseMapper.queryDebitNoteListByStatementId(statement.getStatementId(), SecurityUtils.getUser().getOrgId(),businessScope);
            List<StatementPrint> tableBody = listResult.get(1);


            // pdf写入数据与下载
            createStatementPDF(tableHeader, tableBody, newPDFPath, lang, businessScope);
        }
        // 打印预览
        if (replace) {
            return newPDFPath.replace(PDFUtils.filePath, "");
        } else {
            return newPDFPath;
        }
    }


    /**
     * 生成清单PDF文件中的内容
     *
     * @param tableHeader
     * @param tableBody
     * @param fileName
     * @return
     */
    @SneakyThrows
    private File createStatementPDF(StatementPrint tableHeader, List<StatementPrint> tableBody, String
            fileName, String lang, String businessScope) {
        File file = new File(fileName);
        FileOutputStream out = null;
        Document document = new Document(PageSize.A4);
        try {
            //实例化文档对象
            out = new FileOutputStream(file);
            //创建写入器
            PdfWriter writer = PdfWriter.getInstance(document, out);
            /* 添加页码 */
            writer.setPageEvent(new PdfPageEventHelper());
            // 打开文档准备写入内容
            document.open();

            // 加载报表
            loadStatementTable(document, tableHeader, tableBody, lang, businessScope);
            // 关闭文档
            document.close();
            System.out.println("PDF文件生成成功，PDF文件名：" + file.getAbsolutePath());
            log.info("PDF文件生成成功，PDF文件名：" + file.getAbsolutePath());
        } catch (DocumentException e) {
            log.error("PDF文件" + file.getAbsolutePath() + "生成失败！--DocumentException:" + e);
            throw new RuntimeException("PDF文件" + file.getAbsolutePath() + "生成失败！--DocumentException:" + e);
        } catch (IOException ee) {
            log.error("PDF文件" + file.getAbsolutePath() + "生成失败！--IOException:" + ee);
            throw new RuntimeException("PDF文件" + file.getAbsolutePath() + "生成失败！--IOException:" + ee);
        } finally {
            if (out != null) {
                try {
                    // 关闭输出文件流
                    out.close();
                } catch (IOException e1) {
                    log.error("关闭输出文件流失败！--IOException:" + e1);
                    throw new RuntimeException("关闭输出文件流失败！--IOException:" + e1);
                }
            }
        }
        return file;
    }

    @SneakyThrows
    private void loadStatementTable(Document document, StatementPrint
            tableHeader, List<StatementPrint> tableBody, String lang, String businessScope) {
        BaseFont bfChinese = BaseFont.createFont(PDFUtils.simhei, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        if ("AE".equals(businessScope)) {
            // 正文的字体
            Font contentFont = new Font(bfChinese, 13, Font.BOLD);
            Font contentFontDetail = new Font(bfChinese, 8, Font.NORMAL);
            // 标题的字体
            Font titleFont = new Font(bfChinese, 15, Font.BOLD);

            // 创建表格
            int colNums = 7; // 创建一个4列的表格
            PdfPTable table = new PdfPTable(colNums);
            table.setSpacingBefore(10f);

            float[] widths = {0.1f, 0.15f, 0.15f, 0.2f, 0.15f, 0.1f, 0.15f}; // percentage
            table.setWidths(widths); // 设置列宽度
            // 边框属性
            table.getDefaultCell().setPadding(2); // space between content and
            table.getDefaultCell().setSpaceCharRatio(2);
            table.getDefaultCell().setBorderWidth(0f);
            // 表格宽度
            table.setWidthPercentage(105);
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.getDefaultCell().setMinimumHeight(20f);

            PdfPCell cell;

            //Logo
            if (StrUtil.isNotBlank(tableHeader.getOrgLogo())) {
                Image image = Image.getInstance(tableHeader.getOrgLogo());
                image.setAlignment(Image.ALIGN_CENTER);
                image.scaleToFit(120, 80);
                cell = new PdfPCell(image);
            } else {
                cell = new PdfPCell();
            }
            cell.setFixedHeight(40f);
            cell.setColspan(2);
            cell.setBorderWidth(0f);
            table.addCell(cell);

            //签约公司
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase(tableHeader.getOrgEname(), contentFont));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase(tableHeader.getOrgName(), contentFont));
            }
            cell.setFixedHeight(40f);
            cell.setColspan(3);
            cell.setBorderWidth(0f);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase("", contentFontDetail));
            cell.setFixedHeight(40f);
            cell.setBorderWidth(0f);
            cell.setColspan(2);
            table.addCell(cell);

            //STATEMENT
            cell = new PdfPCell(new Phrase("STATEMENT", titleFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setFixedHeight(30f);
            cell.setBorderWidth(0f);
            cell.setColspan(7);
            table.addCell(cell);

            //TO
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase("TO", contentFontDetail));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase("客户名称    ：", contentFontDetail));
            }
            cell.setFixedHeight(20f);
            cell.setColspan(3);
            cell.setBorderWidth(0f);
            table.addCell(cell);

            //地址
            if ("E".equals(lang)) {
                table.addCell(new Phrase("ADDRESS", contentFontDetail));
            } else if ("C".equals(lang)) {
                table.addCell(new Phrase("联系地址        ：", contentFontDetail));
            }


            cell = new PdfPCell(new Phrase(tableHeader.getCoopAddress(), contentFontDetail));
            cell.setFixedHeight(20f);
            cell.setColspan(3);
            cell.setRowspan(2);
            cell.setBorderWidth(0f);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);

            //customer
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase(tableHeader.getCustomerEname(), contentFontDetail));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase(tableHeader.getCustomerName(), contentFontDetail));
            }
            cell.setColspan(3);
            cell.setRowspan(4);
            cell.setBorderWidth(0f);
            table.addCell(cell);

            //站位空
            table.addCell(new Phrase(""));

            //电话
            if ("E".equals(lang)) {
                table.addCell(new Phrase("TEL", contentFontDetail));
            } else if ("C".equals(lang)) {
                table.addCell(new Phrase("联系电话        ：", contentFontDetail));
            }


            cell = new PdfPCell(new Phrase(tableHeader.getPhoneNumber(), contentFontDetail));
            cell.setFixedHeight(20f);
            cell.setColspan(3);
            cell.setBorderWidth(0f);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);


            //清单编号
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase("STATEMENT", contentFontDetail));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase("对账清单        ：", contentFontDetail));
            }
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase(tableHeader.getStatementNum(), contentFontDetail));
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setColspan(3);
            table.addCell(cell);

            //清单日期
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase("DATE", contentFontDetail));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase("清单日期        ：", contentFontDetail));
            }
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase(tableHeader.getStatementDate() == null ? "" : tableHeader.getStatementDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), contentFontDetail));
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setColspan(3);
            table.addCell(cell);

            //一条线
            Paragraph line = new Paragraph();
            line.add(new Chunk(new LineSeparator()));
            cell = new PdfPCell(line);
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setColspan(7);
            table.addCell(cell);

            //账单明细
            // 添加表头信息
            if ("C".equals(lang)) {
                table.addCell(new Phrase("序号", contentFontDetail));
                table.addCell(new Phrase("账单号", contentFontDetail));
                table.addCell(new Phrase("运单号", contentFontDetail));
                table.addCell(new Phrase("客户单号", contentFontDetail));
                table.addCell(new Phrase("航班信息", contentFontDetail));
                cell = new PdfPCell(new Phrase("计费重量", contentFontDetail));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBorderWidth(0f);
                table.addCell(cell);
                cell = new PdfPCell(new Phrase("账单金额", contentFontDetail));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBorderWidth(0f);
                table.addCell(cell);
            } else if ("E".equals(lang)) {
                table.addCell(new Phrase("SN", contentFontDetail));
                table.addCell(new Phrase("Debitnote", contentFontDetail));
                table.addCell(new Phrase("MAWB", contentFontDetail));
                table.addCell(new Phrase("Customer Order", contentFontDetail));
                table.addCell(new Phrase("Flight", contentFontDetail));
                cell = new PdfPCell(new Phrase("C.W.", contentFontDetail));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBorderWidth(0f);
                table.addCell(cell);
                cell = new PdfPCell(new Phrase("Total Price", contentFontDetail));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBorderWidth(0f);
                table.addCell(cell);
            }

            DecimalFormat decimalFormat2 = new DecimalFormat("#,###,###,###.##########");
            for (int i = 0; i < tableBody.size(); i++) {
                StatementPrint statementPrint = tableBody.get(i);
                // 添加表格内容
                table.addCell(new Phrase((i + 1) + "", contentFontDetail));
                table.addCell(new Phrase(statementPrint.getDebitNoteNum(), contentFontDetail));
                table.addCell(new Phrase(statementPrint.getAwbNumber(), contentFontDetail));
                table.addCell(new Phrase(statementPrint.getCustomerNumber(), contentFontDetail));
                table.addCell(new Phrase(statementPrint.getFlightInfo(), contentFontDetail));
                String str = statementPrint.getChargeableWeight();
                if (StrUtil.isNotBlank(str)) {
                    if (str.contains(",")) {
                        str = str.replaceAll(",", "");
                    }
                    str = decimalFormat2.format(Double.valueOf(str));
                }
                cell = new PdfPCell(new Phrase(str, contentFontDetail));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setBorderWidth(0f);
                table.addCell(cell);
                PdfPCell cell1 = new PdfPCell(new Phrase(statementPrint.getCurrencyAmount(), contentFontDetail));
                cell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell1.setBorderWidth(0f);
                table.addCell(cell1);
            }

            //站位空一行
            cell = new PdfPCell(new Phrase("", contentFontDetail));
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setColspan(7);
            table.addCell(cell);

            //清单金额
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase("Amount" + tableHeader.getCurrencyAmount(), contentFontDetail));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase("清单金额    ：" + tableHeader.getCurrencyAmount(), contentFontDetail));
            }
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setColspan(7);
            table.addCell(cell);

//            cell = new PdfPCell(new Phrase(tableHeader.getCurrencyAmount(), contentFontDetail));
//            cell.setColspan(5);
//            cell.setBorderWidth(0f);
//            table.addCell(cell);

            //清单金额(本币)
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase("", contentFontDetail));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase("清单本币金额：" + tableHeader.getFunctionalAmount(), contentFontDetail));
            }
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setColspan(3);
            table.addCell(cell);
//            if ("E".equals(lang)) {
//                cell = new PdfPCell(new Phrase("", contentFontDetail));
//            } else if ("C".equals(lang)) {
//                cell = new PdfPCell(new Phrase(tableHeader.getFunctionalAmount(), contentFontDetail));
//            }
//            cell.setBorderWidth(0f);
//            table.addCell(cell);

            //清单金额(本币大写)
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase("", contentFontDetail));
                cell.setFixedHeight(20f);
                cell.setBorderWidth(0f);
                cell.setColspan(4);
                table.addCell(cell);
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase("本币金额（大写）：" + tableHeader.getFunctionalAmountConvertBig(), contentFontDetail));
                cell.setFixedHeight(20f);
                cell.setBorderWidth(0f);
                cell.setColspan(4);
                table.addCell(cell);

//                cell = new PdfPCell(new Phrase(tableHeader.getFunctionalAmountConvertBig(), contentFontDetail));
//                cell.setBorderWidth(0f);
//                cell.setColspan(2);
//                table.addCell(cell);
            }

            //一条直线
            cell = new PdfPCell(line);
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setColspan(7);
            table.addCell(cell);

            //备注
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase("REMARKS", contentFontDetail));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase("清单备注    ：", contentFontDetail));
            }
            cell.setBorderWidth(0f);
            cell.setColspan(2);
            cell.setRowspan(6);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase(tableHeader.getStatementRemark(), contentFontDetail));
            cell.setColspan(5);
            cell.setRowspan(6);
            cell.setBorderWidth(0f);
            table.addCell(cell);

            //一条直线
            cell = new PdfPCell(line);
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setColspan(7);
            table.addCell(cell);

            //请收到此清单后签字确认回传，未提出异议者视为已确认，请按规定日期付款
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase("PLEASE SIGN AND RETURN ONE COPY OF THIS D/N AFTER RECEIPT OF THE D/N NO OBJECTIONS ARE RAISED IS CONSIDERED CONFIRMED,PLEASE PAY ACCORDING TO THE DATE SPECIFIED.", contentFontDetail));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase("收到此清单后，请签字确认并回传。未提出异议者视为已确认，请按规定日期付款，谢谢！", contentFontDetail));
            }
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setColspan(7);
            table.addCell(cell);

            //一条直线
            cell = new PdfPCell(line);
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setColspan(7);
            table.addCell(cell);

            //银行账户信息BANK INFO
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase("BANK INFO", contentFontDetail));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase("银行信息    ：", contentFontDetail));
            }
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setColspan(4);
            table.addCell(cell);

            //签字&盖章：SIGNATURE
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase("SIGNATURE", contentFontDetail));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase("签字&盖章：", contentFontDetail));
            }
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setRowspan(4);
            cell.setBorderWidthBottom(1f);
            table.addCell(cell);

            //图片seal
            if (StrUtil.isNotBlank(tableHeader.getOrgSeal())) {
                Image imageSeal = Image.getInstance(tableHeader.getOrgSeal());
                imageSeal.setAlignment(Image.ALIGN_CENTER);
                imageSeal.scaleToFit(100f, 100f);
                cell = new PdfPCell(imageSeal);
            } else {
                cell = new PdfPCell();
            }
            cell.setColspan(2);
            cell.setRowspan(4);
            cell.setBorderWidth(0f);
            cell.setBorderWidthBottom(1f);
            table.addCell(cell);

            //银行账户名称：翌飞锐特电子商务（北京）有限公司
            cell = new PdfPCell(new Phrase(tableHeader.getBankInfo(), contentFontDetail));
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setColspan(4);
            cell.setRowspan(3);
            table.addCell(cell);


            //站位空一行
            cell = new PdfPCell(new Phrase("", contentFontDetail));
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setColspan(7);
            table.addCell(cell);

            //站位空四列
            cell = new PdfPCell(new Phrase("", contentFontDetail));
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setColspan(4);
            table.addCell(cell);


            //制单人
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase("Printer", contentFontDetail));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase("打印人   ：", contentFontDetail));
            }
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            table.addCell(cell);

            //站位空一列
            cell = new PdfPCell(new Phrase("", contentFontDetail));
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            table.addCell(cell);

            //制单时间
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase("Date of Printing", contentFontDetail));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase("打印时间：", contentFontDetail));
            }
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            table.addCell(cell);

            //站位空四列
            cell = new PdfPCell(new Phrase("", contentFontDetail));
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setColspan(4);
            table.addCell(cell);


            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase(tableHeader.getCreatorEname(), contentFontDetail));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase(tableHeader.getCreatorName(), contentFontDetail));
            }
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setBorderWidthBottom(1f);
            table.addCell(cell);

            //站位空一列
            cell = new PdfPCell(new Phrase("", contentFontDetail));
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase(tableHeader.getCreateTime() == null ? "" : tableHeader.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), contentFontDetail));
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setBorderWidthBottom(1f);
            table.addCell(cell);

            document.add(table);
        } else {
            // 正文的字体
            Font contentFont = new Font(bfChinese, 13, Font.NORMAL);
            Font contentFontDetail = new Font(bfChinese, 11, Font.NORMAL);
            // 标题的字体
            Font titleFont = new Font(bfChinese, 15, Font.BOLD);


            // 创建表格
            int colNums = 4; // 创建一个4列的表格
            PdfPTable table = new PdfPTable(colNums);// 发票抬头
            table.setSpacingBefore(10f);

            float[] widths = {0.2f, 0.3f, 0.2f, 0.3f}; // percentage
            table.setWidths(widths); // 设置列宽度
            // 边框属性
            table.getDefaultCell().setBorderColor(new BaseColor(0, 0, 0)); // 边框颜色
            table.getDefaultCell().setPadding(2); // space between content and
            table.getDefaultCell().setSpaceCharRatio(2);
            table.getDefaultCell().setBorderWidth(0.5f);
            // 表格宽度
            table.setWidthPercentage(104);
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.getDefaultCell().setMinimumHeight(20f);
            // contentFont.setColor(0,0,0);

            //表头第一行第一列
            PdfPCell cell;
            cell = new PdfPCell(new Phrase("STATEMENT", titleFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setFixedHeight(24f);
            cell.setColspan(4);
            table.addCell(cell);
            //表头第二行第一列
            cell = new PdfPCell(new Phrase("FROM：", contentFont));
            cell.setFixedHeight(20f);
            cell.setColspan(2);
            table.addCell(cell);

            //表头第二行第二列
            cell = new PdfPCell(new Phrase("TO：", contentFont));
            cell.setFixedHeight(20f);
            cell.setColspan(2);
            table.addCell(cell);

            //表头第三行第一列
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase(tableHeader.getOrgEname(), contentFont));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase(tableHeader.getOrgName(), contentFont));
            }
            cell.setFixedHeight(45f);
            cell.setColspan(2);
            table.addCell(cell);

            //表头第三行第二列
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase(tableHeader.getCustomerEname(), contentFont));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase(tableHeader.getCustomerName(), contentFont));
            }
            cell.setFixedHeight(45f);
            cell.setColspan(2);
            table.addCell(cell);

            //表头第四行第一列
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase("STATEMENT NO", contentFont));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase("清单编号", contentFont));
            }
            cell.setFixedHeight(20f);
            table.addCell(cell);

            //表头第四行第二列
            cell = new PdfPCell(new Phrase(tableHeader.getStatementNum(), contentFont));
            table.addCell(cell);

            //表头第四行第三列
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase("STATEMENT DATE", contentFont));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase("清单日期", contentFont));
            }
            cell.setFixedHeight(20f);
            table.addCell(cell);

            //表头第四行第四列

            cell = new PdfPCell(new Phrase(tableHeader.getStatementDate() == null ? "" : tableHeader.getStatementDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), contentFont));
            table.addCell(cell);

            //表头第五行第一列
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase("Amount of local currency", contentFont));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase("清单金额(本币)", contentFont));
            }
            cell.setFixedHeight(20f);
            table.addCell(cell);

            //表头第五行第二列
            cell = new PdfPCell(new Phrase(tableHeader.getFunctionalAmount(), contentFont));
            table.addCell(cell);

            //表头第五行第三列
            cell = new PdfPCell(new Phrase("", contentFont));
            cell.setFixedHeight(20f);
            cell.setColspan(2);
            table.addCell(cell);

            //表头第六行第一列
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase("Amount", contentFont));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase("清单金额", contentFont));
            }
            cell.setFixedHeight(20f);
            table.addCell(cell);

            //表头第六行第二列
            cell = new PdfPCell(new Phrase(tableHeader.getCurrencyAmount(), contentFont));
            cell.setColspan(3);
            table.addCell(cell);

            //表头第七行第一列
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase("Printer", contentFont));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase("打印人", contentFont));
            }
            cell.setFixedHeight(20f);
            table.addCell(cell);

            //表头第七行第二列
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase(tableHeader.getCreatorEname(), contentFont));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase(tableHeader.getCreatorName(), contentFont));
            }
            table.addCell(cell);

            //表头第七行第三列
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase("Printing Date", contentFont));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase("打印时间", contentFont));
            }
            cell.setFixedHeight(20f);
            table.addCell(cell);

            //表头第七行第四列
            cell = new PdfPCell(new Phrase(tableHeader.getCreateTime() == null ? "" : tableHeader.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), contentFont));
            table.addCell(cell);

            //表头第八行第四列
            if ("E".equals(lang)) {
                cell = new PdfPCell(new Phrase("Remarks", contentFont));
            } else if ("C".equals(lang)) {
                cell = new PdfPCell(new Phrase("备注", contentFont));
            }
            cell.setFixedHeight(20f);
            table.addCell(cell);

            //表头第八行第二列
            cell = new PdfPCell(new Phrase(tableHeader.getStatementRemark(), contentFont));
            cell.setColspan(3);
            table.addCell(cell);

            document.add(table);

            int colNumsDetail = 6; // 创建一个5列的表格
            PdfPTable tableDetail = new PdfPTable(colNumsDetail);// 开票明细
            float[] detailWidths = {0.06f, 0.20f, 0.20f, 0.20f, 0.14f, 0.20f}; // percentage
            tableDetail.setWidths(detailWidths); // 设置列宽度
            tableDetail.setSpacingBefore(30f);
            // 边框属性
            tableDetail.getDefaultCell().setBorderColor(new BaseColor(0, 0, 0)); // 边框颜色
            tableDetail.getDefaultCell().setPadding(2); // space between content and border
            tableDetail.getDefaultCell().setSpaceCharRatio(2);
            tableDetail.getDefaultCell().setBorderWidth(0.5f); // Sets the borderwidth of the table.

            // 表格宽度
            tableDetail.setWidthPercentage(104);
            tableDetail.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            tableDetail.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableDetail.getDefaultCell().setMinimumHeight(20f);

            // 添加表头信息
            if ("AE".equals(businessScope) || "SE".equals(businessScope) || "TE".equals(businessScope)) {
                if ("C".equals(lang)) {
                    tableDetail.addCell(new Phrase("序号", contentFontDetail));
                    if ("TE".equals(businessScope)) {
                        tableDetail.addCell(new Phrase("运单号", contentFontDetail));
                    } else {
                        tableDetail.addCell(new Phrase("主单号", contentFontDetail));
                    }
                    tableDetail.addCell(new Phrase("客户单号", contentFontDetail));
                    tableDetail.addCell(new Phrase("账单号", contentFontDetail));
                    tableDetail.addCell(new Phrase("开航日期", contentFontDetail));
                    tableDetail.addCell(new Phrase("金额", contentFontDetail));
                } else if ("E".equals(lang)) {
                    tableDetail.addCell(new Phrase("No", contentFontDetail));
                    if ("TE".equals(businessScope)) {
                        tableDetail.addCell(new Phrase("RWB No", contentFontDetail));
                    } else {
                        tableDetail.addCell(new Phrase("Mawb No", contentFontDetail));
                    }
                    tableDetail.addCell(new Phrase("Customer No", contentFontDetail));
                    tableDetail.addCell(new Phrase("Debit note", contentFontDetail));
                    tableDetail.addCell(new Phrase("Sailing date", contentFontDetail));
                    tableDetail.addCell(new Phrase("Amount", contentFontDetail));
                }
            } else {
                if ("C".equals(lang)) {
                    tableDetail.addCell(new Phrase("序号", contentFontDetail));
                    if("TI".equals(businessScope)) {
                    	tableDetail.addCell(new Phrase("运单号", contentFontDetail));
                    }else {
                    	tableDetail.addCell(new Phrase("主单号", contentFontDetail));
                    }
                    tableDetail.addCell(new Phrase("客户单号", contentFontDetail));
                    tableDetail.addCell(new Phrase("账单号", contentFontDetail));
                    tableDetail.addCell(new Phrase("到港日期", contentFontDetail));
                    tableDetail.addCell(new Phrase("金额", contentFontDetail));
                } else if ("E".equals(lang)) {
                    tableDetail.addCell(new Phrase("No", contentFontDetail));
                    if("TI".equals(businessScope)) {
                    	tableDetail.addCell(new Phrase("RWB No", contentFontDetail));
                    }else {
                    	tableDetail.addCell(new Phrase("Mawb No", contentFontDetail));
                    }
                    tableDetail.addCell(new Phrase("Customer No", contentFontDetail));
                    tableDetail.addCell(new Phrase("Debit note", contentFontDetail));
                    tableDetail.addCell(new Phrase("Arrival date", contentFontDetail));
                    tableDetail.addCell(new Phrase("Amount", contentFontDetail));
                }
            }


            for (int i = 0; i < tableBody.size(); i++) {
                StatementPrint statementPrint = tableBody.get(i);
                // 添加表格内容
                tableDetail.addCell(new Phrase((i + 1) + "", contentFontDetail));
                tableDetail.addCell(new Phrase(statementPrint.getAwbNumber(), contentFontDetail));
                tableDetail.addCell(new Phrase(statementPrint.getCustomerNumber(), contentFontDetail));
                tableDetail.addCell(new Phrase(statementPrint.getDebitNoteNum(), contentFontDetail));
                tableDetail.addCell(new Phrase(statementPrint.getFlightDate() == null ? "" : statementPrint.getFlightDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), contentFontDetail));
                //tableDetail.addCell(new Phrase(statementPrint.getCurrencyAmount(), contentFont));
                PdfPCell cell1;
                cell1 = new PdfPCell(new Phrase(statementPrint.getCurrencyAmount(), contentFontDetail));
                cell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tableDetail.addCell(cell1);
            }

            document.add(tableDetail);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer statementId, String businessScope, String statementNum) {
        //条件判断
        Statement statement = getById(statementId);
        if (statement== null) {
            throw new RuntimeException("清单不是最新数据，请刷新页面再操作");
        }
        LambdaQueryWrapper<CssIncomeInvoice> cssIncomeInvoiceWrapper = Wrappers.<CssIncomeInvoice>lambdaQuery();
        cssIncomeInvoiceWrapper.eq(CssIncomeInvoice::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssIncomeInvoice::getStatementId, statementId).last("limit 1");
        CssIncomeInvoice incomeInvoice = cssIncomeInvoiceMapper.selectOne(cssIncomeInvoiceWrapper);
        if (incomeInvoice != null) {
            throw new RuntimeException(statement.getStatementNum()+"清单 已申请开票 或 已开票 ，不能删除。");
        }
        //删除清单
        this.removeById(statementId);
        //删除清单币种
        LambdaQueryWrapper<StatementCurrency> statementCurrencyLambdaQueryWrapper = Wrappers.<StatementCurrency>lambdaQuery();
        statementCurrencyLambdaQueryWrapper.eq(StatementCurrency::getOrgId, SecurityUtils.getUser().getOrgId()).eq(StatementCurrency::getStatementId, statementId);
        statementCurrencyService.remove(statementCurrencyLambdaQueryWrapper);
        //清空账单对应的清单id
        LambdaQueryWrapper<DebitNote> debitNoteLambdaQueryWrapper = Wrappers.<DebitNote>lambdaQuery();
        debitNoteLambdaQueryWrapper.eq(DebitNote::getStatementId, statementId).eq(DebitNote::getOrgId, SecurityUtils.getUser().getOrgId());
        List<DebitNote> debitNotes = debitNoteService.list(debitNoteLambdaQueryWrapper);
//        HashMap<Integer, String> map = new HashMap<>();
        ArrayList<Map<String, String>> orderList = new ArrayList<Map<String, String>>();
        debitNotes.stream().forEach(debitNote -> {
            debitNote.setStatementId(null);
            String orderUUID = "";
            HashMap<String, String> map = new HashMap<String, String>();
//            if (map.get(debitNote.getOrderId()) == null) {
            if ("AE".equals(businessScope) || "AI".equals(businessScope)) {
                LambdaQueryWrapper<AfOrder> afOrderLambdaQueryWrapper = Wrappers.<AfOrder>lambdaQuery();
                afOrderLambdaQueryWrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfOrder::getOrderId, debitNote.getOrderId());
                AfOrder order = afOrderService.getOne(afOrderLambdaQueryWrapper);
//                    String value = order.getOrderCode() + "," + order.getOrderUuid() + "," + (StrUtil.isBlank(order.getAwbNumber()) ? " " : order.getAwbNumber()) + "," + (StrUtil.isBlank(order.getAwbUuid()) ? " " : order.getAwbUuid());
//                    map.put(debitNote.getOrderId(), value);
                orderUUID = order.getOrderUuid();
                map.put("orderCode", order.getOrderCode());
                map.put("orderUUID", order.getOrderUuid());

            } else if (businessScope.startsWith("S")) {
                LambdaQueryWrapper<ScOrder> afOrderLambdaQueryWrapper = Wrappers.<ScOrder>lambdaQuery();
                afOrderLambdaQueryWrapper.eq(ScOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(ScOrder::getOrderId, debitNote.getOrderId());
                ScOrder order = scOrderService.getOne(afOrderLambdaQueryWrapper);
//	                String value = order.getOrderCode() + "," + order.getOrderUuid() ;
//	                map.put(debitNote.getOrderId(), value);
                orderUUID = order.getOrderUuid();
                map.put("orderCode", order.getOrderCode());
                map.put("orderUUID", order.getOrderUuid());
            } else if (businessScope.startsWith("T")) {
                LambdaQueryWrapper<TcOrder> tcOrderLambdaQueryWrapper = Wrappers.<TcOrder>lambdaQuery();
                tcOrderLambdaQueryWrapper.eq(TcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(TcOrder::getOrderId, debitNote.getOrderId());
                TcOrder order = tcOrderService.getOne(tcOrderLambdaQueryWrapper);
                orderUUID = order.getOrderUuid();
                map.put("orderCode", order.getOrderCode());
                map.put("orderUUID", order.getOrderUuid());
            } else if (businessScope.startsWith("L")) {
                LambdaQueryWrapper<LcOrder> lcOrderLambdaQueryWrapper = Wrappers.<LcOrder>lambdaQuery();
                lcOrderLambdaQueryWrapper.eq(LcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(LcOrder::getOrderId, debitNote.getOrderId());
                LcOrder order = lcOrderService.getOne(lcOrderLambdaQueryWrapper);
                orderUUID = order.getOrderUuid();
                map.put("orderCode", order.getOrderCode());
                map.put("orderUUID", order.getOrderUuid());
            } else if ("IO".equals(businessScope)) {
                LambdaQueryWrapper<IoOrder> ioOrderLambdaQueryWrapper = Wrappers.<IoOrder>lambdaQuery();
                ioOrderLambdaQueryWrapper.eq(IoOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(IoOrder::getOrderId, debitNote.getOrderId());
                IoOrder order = ioOrderService.getOne(ioOrderLambdaQueryWrapper);
                orderUUID = order.getOrderUuid();
                map.put("orderCode", order.getOrderCode());
                map.put("orderUUID", order.getOrderUuid());
            }
//            }
            int isHave = 0;

            for (int i = 0; i < orderList.size(); i++) {
                if (orderUUID.equals(orderList.get(i).get("orderUUID"))) {
                    isHave = 1;
                    break;
                }
            }
            if (isHave == 0) {
                orderList.add(map);
            }
        });
        debitNoteService.updateBatchById(debitNotes);
        Set orderIds = new HashSet();
        //修改订单费用状态
        for (int i = 0; i < debitNotes.size(); i++) {
            DebitNote bill = debitNotes.get(i);
            orderIds.add(bill.getOrderId());
//            if ("AE".equals(businessScope) || "AI".equals(businessScope)) {
//                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
//                afOrderMapper.generateTempTableA(SecurityUtils.getUser().getOrgId(), bill.getOrderUuid());
//                //根据临时表更新订单状态
//                afOrderMapper.updateOrderStatusByTempTableA();
//
//            } else if ("SE".equals(businessScope) || "SI".equals(businessScope)) {
//                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
//                afOrderMapper.generateTempTableS(SecurityUtils.getUser().getOrgId(), bill.getOrderUuid());
//                //根据临时表更新订单状态
//                afOrderMapper.updateOrderStatusByTempTableS();
//            } else if (businessScope.startsWith("T")) {
//                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
//                afOrderMapper.generateTempTableT(SecurityUtils.getUser().getOrgId(), bill.getOrderUuid());
//                //根据临时表更新订单状态
//                afOrderMapper.updateOrderStatusByTempTableT();
//            } else if (businessScope.startsWith("L")) {
//                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
//                afOrderMapper.generateTempTableL(SecurityUtils.getUser().getOrgId(), bill.getOrderUuid());
//                //根据临时表更新订单状态
//                afOrderMapper.updateOrderStatusByTempTableL();
//            } else if (businessScope.equals("IO")) {
//                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
//                afOrderMapper.generateTempTableIO(SecurityUtils.getUser().getOrgId(), bill.getOrderUuid());
//                //根据临时表更新订单状态
//                afOrderMapper.updateOrderStatusByTempTableIO();
//            }
        }
        
        Iterator it = orderIds.iterator();
		while (it.hasNext()) {
			//更新订单应收状态：（order. income_status）
    		List<Map> listMap = detailMapper.getOrderIncomeStatus(SecurityUtils.getUser().getOrgId(), it.next().toString(),businessScope);
    		if(listMap!=null&&listMap.size()>0) {
    			for(Map map:listMap) {
    				detailMapper.updateOrderIncomeStatus(Integer.valueOf(map.get("org_id").toString()),Integer.valueOf(map.get("order_id").toString()),map.get("income_status").toString(),UUID.randomUUID().toString(),businessScope);
    			}
    		}
		}
        //添加日志
        for (int i = 0; i < orderList.size(); i++) {
            LogBean logBean = new LogBean();
            logBean.setBusinessScope(businessScope);
            logBean.setPageName("清单");
            logBean.setPageFunction("删除清单");
            logBean.setLogRemark("清单号：" + statementNum);

            logBean.setOrderNumber(orderList.get(i).get("orderCode"));
            logBean.setOrderUuid(orderList.get(i).get("orderUUID"));

            if ("AE".equals(businessScope) || "AI".equals(businessScope)) {
                //日志
                logService.saveLog(logBean);
            } else if ("SE".equals(businessScope) || "SI".equals(businessScope)) {
                //日志
                ScLog logBean2 = new ScLog();
                BeanUtils.copyProperties(logBean, logBean2);
                scLogService.saveLog(logBean2);
            } else if (businessScope.startsWith("T")) {
                TcLog logBeanTC = new TcLog();
                BeanUtils.copyProperties(logBean, logBeanTC);
                logBeanTC.setCreatorId(SecurityUtils.getUser().getId());
                logBeanTC.setCreatorName(SecurityUtils.getUser().buildOptName());
                logBeanTC.setCreatTime(LocalDateTime.now());
                logBeanTC.setOrgId(SecurityUtils.getUser().getOrgId());
                tcLogService.save(logBeanTC);
            } else if (businessScope.startsWith("L")) {
                LcLog logBeanLC = new LcLog();
                BeanUtils.copyProperties(logBean, logBeanLC);
                logBeanLC.setCreatorId(SecurityUtils.getUser().getId());
                logBeanLC.setCreatorName(SecurityUtils.getUser().buildOptName());
                logBeanLC.setCreatTime(LocalDateTime.now());
                logBeanLC.setOrgId(SecurityUtils.getUser().getOrgId());
                lcLogService.save(logBeanLC);
            } else if ("IO".equals(businessScope)) {
                IoLog logBeanIO = new IoLog();
                BeanUtils.copyProperties(logBean, logBeanIO);
                logBeanIO.setCreatorId(SecurityUtils.getUser().getId());
                logBeanIO.setCreatorName(SecurityUtils.getUser().buildOptName());
                logBeanIO.setCreatTime(LocalDateTime.now());
                logBeanIO.setOrgId(SecurityUtils.getUser().getOrgId());
                ioLogService.save(logBeanIO);
            }
        }
    }

    @Override
    public Statement checkCssStatement(Integer statementId) {
        LambdaQueryWrapper<Statement> StatementWrapper = Wrappers.<Statement>lambdaQuery();
        StatementWrapper.eq(Statement::getStatementId, statementId);
        Statement statement = baseMapper.selectOne(StatementWrapper);
        if(statement!=null) {
        	//查询invoiceId
        	LambdaQueryWrapper<CssIncomeInvoice> cssIncomeInvoiceWrapper = Wrappers.<CssIncomeInvoice>lambdaQuery();
        	cssIncomeInvoiceWrapper.eq(CssIncomeInvoice::getStatementId, statementId);
        	CssIncomeInvoice  c = cssIncomeInvoiceMapper.selectOne(cssIncomeInvoiceWrapper);
        	if(c!=null) {
        		statement.setInvoiceId(c.getInvoiceId());
        	}
        }
        return statement;
    }


    public void exportExcelList(Statement bean) {
        List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
        String[] colunmStrs = null;
        String[] headers = null;
        if (!StringUtils.isEmpty(bean.getColumnStrs())) {
            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(bean.getColumnStrs());
            int num = jsonArr.size();
            headers = new String[num];
            colunmStrs = new String[num];
//	    		 headers[0]= "账单编号";
//	    		 colunmStrs[0]="debitNoteNum";
            //生成表头跟字段
            if (jsonArr != null && jsonArr.size() > 0) {
                int numStr = 0;
                for (int i = 0; i < jsonArr.size(); i++) {
                    JSONObject job = jsonArr.getJSONObject(i);
                    headers[numStr] = job.getString("label");
                    colunmStrs[numStr] = job.getString("prop");
                    numStr++;
                }
            }
        } else {
            //默认数据
            headers = new String[]{"清单号", "清单状态", "清单日期", "收款客户", "清单金额（原币）", "已核销金额（原币）", "清单金额（本币）",
                    "已核销金额（本币）", "未核销金额（本币）", "核销单号", "清单备注","发票号码","开票申请备注", "开票申请人","开票申请时间","制单人",
                    "清单制作时间"};
            colunmStrs = new String[]{"statementNum", "statementStatus", "statementDate", "customerName",
                    "currencyAmount", "currencyAmount2", "functionalAmount", "functionalAmountWriteoff", "functionalAmountNoWriteoff",
                    "writeoffNum", "statementRemark","invoiceNum","applyRemark", "invoiceCreatorName","invoiceCreateTime","creatorName",
                    "createTime"};
        }
        //查询
        Page page = new Page();
        page.setCurrent(1);
        page.setSize(1000000);
        IPage result = this.getPage2(page, bean);
        List<Statement> totalInfo = this.getTatol(bean);
        if (totalInfo != null && totalInfo.size() > 0) {
            LinkedHashMap mapTwo = new LinkedHashMap();
            Statement totalInfoOne = totalInfo.get(0);
            totalInfoOne.setStatementNum("合计");
            result.getRecords().add(totalInfoOne);
        }
        if (result != null && result.getRecords() != null && result.getRecords().size() > 0) {
            DecimalFormat decimalFormat2 = new DecimalFormat("#,###,###,###.##########");
            List<Statement> listA = result.getRecords();
            for (Statement excel2 : listA) {
                if (excel2 != null && !"".equals(excel2.getCurrencyAmount())) {
                    excel2.setCurrencyAmount(excel2.getCurrencyAmount().replaceAll("  ", String.valueOf((char) 10) + ""));
                }
                if (excel2 != null && !"".equals(excel2.getCurrencyAmount2())) {
                    excel2.setCurrencyAmount2(excel2.getCurrencyAmount2().replaceAll("  ", String.valueOf((char) 10) + ""));
                }
                LinkedHashMap mapTwo = new LinkedHashMap();
                for (int j = 0; j < colunmStrs.length; j++) {
                    if ("functionalAmountNoWriteoff".equals(colunmStrs[j])) {
                        if (excel2.getFunctionalAmount() != null) {
                            BigDecimal noWriteoff = excel2.getFunctionalAmountWriteoff() != null ? excel2.getFunctionalAmountWriteoff() : BigDecimal.ZERO;
                            mapTwo.put("functionalAmountNoWriteoff", decimalFormat2.format(excel2.getFunctionalAmount().subtract(noWriteoff)));
                        } else {
                            mapTwo.put("functionalAmountNoWriteoff", "");
                        }
                    } else if ("creatorName".equals(colunmStrs[j])) {
                        if (excel2.getCreatorName() != null) {
                            mapTwo.put("creatorName", excel2.getCreatorName().split(" ")[0]);
                        } else {
                            mapTwo.put("creatorName", "");
                        }
                    } else if ("functionalAmount".equals(colunmStrs[j])) {
                        if (excel2.getFunctionalAmount() != null) {
                            mapTwo.put("functionalAmount", decimalFormat2.format(excel2.getFunctionalAmount()));
                        } else {
                            mapTwo.put("functionalAmount", "");
                        }

                    } else if ("functionalAmountWriteoff".equals(colunmStrs[j])) {
                        if (excel2.getFunctionalAmountWriteoff() != null) {
                            mapTwo.put("functionalAmountWriteoff", decimalFormat2.format(excel2.getFunctionalAmountWriteoff()));
                        } else {
                            mapTwo.put("functionalAmountWriteoff", "");
                        }

                    } else if ("createTime".equals(colunmStrs[j])) {
                        if (excel2.getCreateTime() != null) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            mapTwo.put("createTime", formatter.format(excel2.getCreateTime()));
                        } else {
                            mapTwo.put("createTime", "");
                        }
                    } else if ("writeoffNum".equals(colunmStrs[j])) {
                        StringBuffer sb = new StringBuffer();
                        if (excel2.getWriteoffNum() != null && !"".equals(excel2.getWriteoffNum())) {
                            if (excel2.getWriteoffNum().contains("  ")) {
                                String[] array = excel2.getWriteoffNum().split("  ");
                                for (int k = 0; k < array.length; k++) {
                                    sb.append(array[k].split(" ")[1]).append("\n");
                                }
                                mapTwo.put("writeoffNum", sb.toString());
                            } else {
                                mapTwo.put("writeoffNum", excel2.getWriteoffNum().split(" ")[1]);
                            }
                        } else {
                            mapTwo.put("writeoffNum", "");
                        }

                    }else if("invoiceNum".equals(colunmStrs[j])) {
                    	StringBuffer sb = new StringBuffer();
                        if (excel2.getInvoiceNum() != null && !"".equals(excel2.getInvoiceNum())) {
                            if (excel2.getInvoiceNum().contains("&")) {
                                String[] array = excel2.getInvoiceNum().split("&");
                                for (int k = 0; k < array.length; k++) {
                                    sb.append(array[k].split("#")[1]).append("\n");
                                }
                                mapTwo.put("invoiceNum", sb.toString());
                            } else {
                                mapTwo.put("invoiceNum", excel2.getInvoiceNum().split("#")[1]);
                            }
                        } else {
                            mapTwo.put("invoiceNum", "");
                        }
                    }else if ("invoiceCreateTime".equals(colunmStrs[j])) {
                        if (excel2.getInvoiceCreateTime() != null) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            mapTwo.put("invoiceCreateTime", formatter.format(excel2.getInvoiceCreateTime()));
                        } else {
                            mapTwo.put("invoiceCreateTime", "");
                        }
                    }else if ("invoiceCreatorName".equals(colunmStrs[j])) {
                        if (excel2.getInvoiceCreatorName() != null) {
                            mapTwo.put("invoiceCreatorName", excel2.getInvoiceCreatorName().split(" ")[0]);
                        } else {
                            mapTwo.put("invoiceCreatorName", "");
                        }
                    } else {
                        mapTwo.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], excel2));
                    }

                }
                listExcel.add(mapTwo);
            }
            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(LoginUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");
        }
    }
}
