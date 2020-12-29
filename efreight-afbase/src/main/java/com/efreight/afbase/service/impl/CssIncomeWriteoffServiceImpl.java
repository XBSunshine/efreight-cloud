package com.efreight.afbase.service.impl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.efreight.afbase.entity.*;
import com.efreight.afbase.service.*;
import lombok.AllArgsConstructor;
import cn.hutool.core.util.StrUtil;

import com.efreight.afbase.dao.AfOrderMapper;
import com.efreight.afbase.dao.CssIncomeWriteoffDetailMapper;
import com.efreight.afbase.dao.CssIncomeWriteoffMapper;
import com.efreight.afbase.utils.FieldValUtils;
import com.efreight.afbase.utils.LoginUtils;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.security.util.SecurityUtils;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * CSS 应收：核销 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-03
 */
@Service
@AllArgsConstructor
public class CssIncomeWriteoffServiceImpl extends ServiceImpl<CssIncomeWriteoffMapper, CssIncomeWriteoff> implements CssIncomeWriteoffService {

    private final CssIncomeWriteoffDetailMapper cssIncomeWriteoffDetailMapper;
    private final CssDebitNoteCurrencyService cssDebitNoteCurrencyService;
    private final StatementCurrencyService statementCurrencyService;
    private final AfOrderMapper afOrderMapper;
    private final CssDebitNoteService cssDebitNoteService;
    private final CssIncomeWriteoffStatementDetailService cssIncomeWriteoffStatementDetailService;
    private final CssIncomeWriteoffDetailService cssIncomeWriteoffDetailService;

    @Override
    public IPage getPage2(Page page, CssIncomeWriteoff bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        IPage<CssIncomeWriteoff> iPage = baseMapper.getListPage(page, bean);
        HashMap<String, List<CssIncomeWriteoff>> map = new HashMap<>();
        iPage.getRecords().stream().forEach(one -> {
            StringBuffer buffer2 = new StringBuffer();
            buffer2.append(new DecimalFormat("###,##0.00").format(one.getAmountWriteoff().setScale(2, BigDecimal.ROUND_HALF_UP)))
                    .append(" (").append(one.getCurrency()).append(")  ");
            one.setCurrencyAmount2(buffer2.toString());
            //账单金额实现多币种显示
            if (one.getDebitNoteId() != null) {
                LambdaQueryWrapper<CssDebitNoteCurrency> cssDebitCurrencyWrapper = Wrappers.<CssDebitNoteCurrency>lambdaQuery();
                cssDebitCurrencyWrapper.eq(CssDebitNoteCurrency::getDebitNoteId, one.getDebitNoteId()).eq(CssDebitNoteCurrency::getOrgId, SecurityUtils.getUser().getOrgId());
                List<CssDebitNoteCurrency> currencyList = cssDebitNoteCurrencyService.list(cssDebitCurrencyWrapper);
                StringBuffer buffer = new StringBuffer();
                currencyList.stream().forEach(currency -> {
                    buffer.append(new DecimalFormat("###,##0.00").format(currency.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP))).append(" (").append(currency.getCurrency()).append(")  ");
                });

                one.setCurrencyAmount(buffer.toString());
            } else {
                LambdaQueryWrapper<StatementCurrency> statementCurrencyMapper = Wrappers.<StatementCurrency>lambdaQuery();
                statementCurrencyMapper.eq(StatementCurrency::getStatementId, one.getStatementId()).eq(StatementCurrency::getOrgId, SecurityUtils.getUser().getOrgId());
                List<StatementCurrency> currencyList = statementCurrencyService.list(statementCurrencyMapper);
                StringBuffer buffer = new StringBuffer();
                currencyList.stream().forEach(currency -> {
                    buffer.append(new DecimalFormat("###,##0.00").format(currency.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP))).append(" (").append(currency.getCurrency()).append(")  ");
                });

                one.setCurrencyAmount(buffer.toString());
            }


        });

        //合计
//        List<CssIncomeWriteoff> list = baseMapper.getTatol(bean);
//        
//        List<CssIncomeWriteoff> list2 = baseMapper.getTatol2(bean);
//        
//        
//        CssIncomeWriteoff order = new CssIncomeWriteoff();
//
//        BigDecimal functionalAmountWriteoff = new BigDecimal(0);
//        StringBuffer buffer = new StringBuffer();
//      
//        for (int i = 0; i < list.size(); i++) {
//        	CssIncomeWriteoff afOrder = list.get(i);
//            if (afOrder.getFunctionalAmountWriteoff() != null) {
//            	functionalAmountWriteoff = functionalAmountWriteoff.add(afOrder.getFunctionalAmountWriteoff());
//            }
//        }
//        for (int i = 0; i < list2.size(); i++) {
//        	buffer.append(new DecimalFormat("###,###.00").format(
//        			list2.get(i).getAmountWriteoff().setScale(2, BigDecimal.ROUND_HALF_UP)))
//        			.append(" (").append(list2.get(i).getCurrency()).append(")  ");
//        }
//        if (iPage.getTotal()>0) {
//        	order.setWriteoffNum("合计");
//            order.setCurrencyAmount("  ");
//            order.setCurrencyAmount2(buffer.toString());
//            order.setFunctionalAmountWriteoff(functionalAmountWriteoff);
//            
//            iPage.getRecords().add(order);
//		}
//        
        return iPage;
    }

    @Override
    public List<CssIncomeWriteoff> getTatol(CssIncomeWriteoff bean) {

        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        List<CssIncomeWriteoff> list = baseMapper.getTatol(bean);

        List<CssIncomeWriteoff> list2 = baseMapper.getTatol2(bean);


        List<CssIncomeWriteoff> tatolList = new ArrayList<CssIncomeWriteoff>();
        CssIncomeWriteoff order = new CssIncomeWriteoff();

        BigDecimal functionalAmountWriteoff = new BigDecimal(0);
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < list.size(); i++) {
            CssIncomeWriteoff afOrder = list.get(i);
            if (afOrder.getFunctionalAmountWriteoff() != null) {
                functionalAmountWriteoff = functionalAmountWriteoff.add(afOrder.getFunctionalAmountWriteoff());

            }
        }
        for (int i = 0; i < list2.size(); i++) {
            buffer.append(new DecimalFormat("###,##0.00").format(
                    list2.get(i).getAmountWriteoff().setScale(2, BigDecimal.ROUND_HALF_UP)))
                    .append(" (").append(list2.get(i).getCurrency()).append(")  ");
        }

        order.setWriteoffNum("合计");
        order.setCurrencyAmount("  ");
        order.setCurrencyAmount2(buffer.toString());
        order.setFunctionalAmountWriteoff(functionalAmountWriteoff);

        tatolList.add(order);
        return tatolList;
    }

    @Override
    public IPage getPage(Page page, CssIncomeWriteoff bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        IPage<CssIncomeWriteoff> iPage = baseMapper.getListPage(page, bean);
        HashMap<String, List<CssIncomeWriteoff>> map = new HashMap<>();
        iPage.getRecords().stream().forEach(one -> {
            //是否核销
//            LambdaQueryWrapper<CssIncomeWriteoff> cssIncomeWriteoffLambdaQueryWrapper = Wrappers.<CssIncomeWriteoff>lambdaQuery();
//            cssIncomeWriteoffLambdaQueryWrapper.eq(CssIncomeWriteoff::getDebitNoteId, one.getDebitNoteId()).eq(CssIncomeWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).last("limit 1");
//            CssIncomeWriteoff cssIncomeWriteoff = cssIncomeWriteoffService.getOne(cssIncomeWriteoffLambdaQueryWrapper);
//            if (cssIncomeWriteoff == null) {
//                one.setIfWriteoff(false);
//            } else {
//                one.setIfWriteoff(true);
//            }
//
//
//            if (one.getInvoiceId() != null) {
//                one.setDebitNoteStatus("发票开具");
//            } else {
//                if (one.getStatementId() == null) {
//                    one.setDebitNoteStatus("制作账单");
//                } else {
//                    one.setDebitNoteStatus("制作清单");
//                }
//            }
            //账单金额实现多币种显示
            if (one.getDebitNoteId() != null) {
                LambdaQueryWrapper<CssDebitNoteCurrency> cssDebitCurrencyWrapper = Wrappers.<CssDebitNoteCurrency>lambdaQuery();
                cssDebitCurrencyWrapper.eq(CssDebitNoteCurrency::getDebitNoteId, one.getDebitNoteId()).eq(CssDebitNoteCurrency::getOrgId, SecurityUtils.getUser().getOrgId());
                List<CssDebitNoteCurrency> currencyList = cssDebitNoteCurrencyService.list(cssDebitCurrencyWrapper);
                StringBuffer buffer = new StringBuffer();
                currencyList.stream().forEach(currency -> {
                    buffer.append(new DecimalFormat("###,###.00").format(currency.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP))).append(" (").append(currency.getCurrency()).append(")  ");
                });

                one.setCurrencyAmount(buffer.toString());
            } else {
                LambdaQueryWrapper<StatementCurrency> statementCurrencyMapper = Wrappers.<StatementCurrency>lambdaQuery();
                statementCurrencyMapper.eq(StatementCurrency::getStatementId, one.getStatementId()).eq(StatementCurrency::getOrgId, SecurityUtils.getUser().getOrgId());
                List<StatementCurrency> currencyList = statementCurrencyService.list(statementCurrencyMapper);
                StringBuffer buffer = new StringBuffer();
                currencyList.stream().forEach(currency -> {
                    buffer.append(new DecimalFormat("###,###.00").format(currency.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP))).append(" (").append(currency.getCurrency()).append(")  ");
                });

                one.setCurrencyAmount(buffer.toString());
            }


            //封装树结构
            String currency = "";
            if (StrUtil.isNotBlank(one.getCurrency())) {
                currency = one.getCurrency();
            }
            String amountTaxRateStr = "";
//            if (one.getAmountTaxRate() != null) {
//                amountTaxRateStr = one.getAmountTaxRate().toString();
//            }
            if (one.getIncomeWriteoffId() != null) {
                amountTaxRateStr = one.getIncomeWriteoffId().toString();
            }

//            String key = one.getCustomerId() + "$%$" + amountTaxRateStr;
            String key = one.getCustomerId() + "$%$";
            if (map.get(key) == null) {
                ArrayList<CssIncomeWriteoff> debitNotes = new ArrayList<>();
                debitNotes.add(one);
                map.put(key, debitNotes);
            } else {
                map.get(key).add(one);
            }
        });
        List<CssIncomeWriteoffTree> result = new ArrayList<>();
        for (Map.Entry<String, List<CssIncomeWriteoff>> entry : map.entrySet()) {
            CssIncomeWriteoffTree debitNoteTree = new CssIncomeWriteoffTree();
            debitNoteTree.setIncomeWriteoffId("A" + entry.getValue().get(0).getIncomeWriteoffId());
//            debitNoteTree.setAmountTaxRate(entry.getValue().get(0).getAmountTaxRate());
            debitNoteTree.setCustomerName(entry.getValue().get(0).getCustomerName());
            debitNoteTree.setCurrency(entry.getValue().get(0).getCurrency());
            debitNoteTree.setChildren(entry.getValue());
            result.add(debitNoteTree);
        }

//        return result;
        page.setTotal(iPage.getTotal());
        page.setRecords(result);
        return page;
    }

    @Override
    public List<CssDebitNoteCurrency> queryBillCurrency(Integer debitNoteId) {
        return baseMapper.queryBillCurrency(SecurityUtils.getUser().getOrgId(), debitNoteId);
    }

    @Override
    public List<FinancialAccount> getFinancialAccount(String businessScope, Integer customerId) {
        List<FinancialAccount> list = baseMapper.getFinancialAccount(SecurityUtils.getUser().getOrgId(),businessScope,customerId);
        /*if(list != null && list.size() > 0){
            for (int i = 0; i < list.size(); i++) {
                String manageMode = list.get(i).getManageMode();
                String subsidiaryAccount = list.get(i).getSubsidiaryAccount();
                if("辅助账".equals(manageMode) && "往来单位".equals(subsidiaryAccount)){//如果 manage_mode=辅助账 AND subsidiary_account=往来单位 则 科目代码 取 当前 核销单 收款对象/供应商 对应 coop表的 财务码
                   String financialCode = baseMapper.getFinancialCodeByCoopId(customerId);
                   if(financialCode != null && !"".equals(financialCode)){
                       list.get(i).setFinancialAccountCode(financialCode);
                   }else{
                       list.get(i).setFinancialAccountCode("");
                   }
                }
            }
        }*/
        return list;
    }

    @Override
    public List<StatementCurrency> queryListCurrency(Integer statementId) {
        return baseMapper.queryListCurrency(SecurityUtils.getUser().getOrgId(), statementId);
    }

    private String getCode(String businessScope) {
        Date dt = new Date();

        String year = String.format("%ty", dt);

        String mon = String.format("%tm", dt);

        String day = String.format("%td", dt);
        return businessScope + "-RW-" + year + mon + day;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doBillWriteoff(CssIncomeWriteoff bean) {

        //生成编号
        String code = getCode(bean.getBusinessScope());
        List<CssIncomeWriteoff> codeList = baseMapper.selectCode(SecurityUtils.getUser().getOrgId(), code);

        if (codeList.size() == 0) {
            bean.setWriteoffNum(code + "0001");
        } else {
            if ((code + "9999").equals(codeList.get(0).getWriteoffNum())) {
                throw new RuntimeException("每天最多可以核销9999个" + bean.getBusinessScope() + "账单");
            } else {
                String str = codeList.get(0).getWriteoffNum();
                str = str.substring(str.length() - 4);
                bean.setWriteoffNum(code + String.format("%04d", Integer.parseInt(str) + 1));
            }

        }

        bean.setCreateTime(new Date());
        bean.setCreatorId(SecurityUtils.getUser().getId());
        bean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        if(bean.getFinancialAccountName() != null && !"".equals(bean.getFinancialAccountName())){
            bean.setFinancialAccountName(bean.getFinancialAccountName().substring(0,bean.getFinancialAccountName().lastIndexOf(" ")));
        }

        //核销
        baseMapper.insert(bean);

        BigDecimal total = new BigDecimal(0);
        //核销明细
        for (int i = 0; i < bean.getDebitCurrencyList().size(); i++) {
            CssIncomeWriteoffDetail detailBean = new CssIncomeWriteoffDetail();
            detailBean.setIncomeWriteoffId(bean.getIncomeWriteoffId());
            detailBean.setDebitNoteId(bean.getDebitCurrencyList().get(i).getDebitNoteId());
            detailBean.setCurrency(bean.getDebitCurrencyList().get(i).getCurrency());
            detailBean.setAmountWriteoff(bean.getDebitCurrencyList().get(i).getAmountWriteoff2());
            detailBean.setFunctionalAmountWriteoff(bean.getDebitCurrencyList().get(i).getFunctionalAmountWriteoff2());
            if (detailBean.getAmountWriteoff().compareTo(new BigDecimal(0)) == 0) {
                continue;
            }
            cssIncomeWriteoffDetailMapper.insert(detailBean);
            //
            CssDebitNoteCurrency cssDebitNoteCurrency = baseMapper.getCssDebitNoteCurrency(SecurityUtils.getUser().getOrgId(), bean.getDebitCurrencyList().get(i).getDebitNoteId(), bean.getDebitCurrencyList().get(i).getCurrency());
            baseMapper.updateCssDebitNoteCurrency(SecurityUtils.getUser().getOrgId(),
                    bean.getDebitCurrencyList().get(i).getDebitNoteId(), bean.getDebitCurrencyList().get(i).getCurrency(),
                    cssDebitNoteCurrency.getAmountWriteoff().add(bean.getDebitCurrencyList().get(i).getAmountWriteoff2()),
                    cssDebitNoteCurrency.getFunctionalAmountWriteoff().add(bean.getDebitCurrencyList().get(i).getFunctionalAmountWriteoff2()));
            total = total.add(bean.getDebitCurrencyList().get(i).getFunctionalAmountWriteoff2());
        }
        //
        CssDebitNote cssDebitNote = baseMapper.getCssDebitNote(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteId());
//        baseMapper.updateCssDebitNote(SecurityUtils.getUser().getOrgId(),
//        		bean.getDebitNoteId(),cssDebitNote.getFunctionalAmountWriteoff().add(bean.getAmountWriteoff()));
        if (cssDebitNote == null) {
            throw new RuntimeException("账单不存在");
        }
        if (!bean.getRowUuid().equals(cssDebitNote.getRowUuid())) {
            throw new RuntimeException("账单不是最新数据，请刷新页面再操作");
        }
        baseMapper.updateCssDebitNote(SecurityUtils.getUser().getOrgId(),
                bean.getDebitNoteId(), cssDebitNote.getFunctionalAmountWriteoff().add(total), UUID.randomUUID().toString());
        //修改账单状态
        int flag = 1;
        for (int i = 0; i < bean.getDebitCurrencyList().size(); i++) {
            CssDebitNoteCurrency cssDebitNoteCurrency = baseMapper.getCssDebitNoteCurrency(SecurityUtils.getUser().getOrgId(),
                    bean.getDebitCurrencyList().get(i).getDebitNoteId(), bean.getDebitCurrencyList().get(i).getCurrency());
            if (cssDebitNoteCurrency.getAmount().compareTo(cssDebitNoteCurrency.getAmountWriteoff()) != 0 &&
                    cssDebitNoteCurrency.getFunctionalAmount().compareTo(cssDebitNoteCurrency.getFunctionalAmountWriteoff()) != 0) {
                flag = 0;
            }
        }
//        if (flag==1) {
        baseMapper.updateBillStatus(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteId(), flag);
//		}
        //修改订单费用状态
        String order_uuid = "";
        List<String> uuidList = afOrderMapper.getOrderUUID(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteId());
        if (uuidList.size() > 0) {
            order_uuid = uuidList.get(0);
        }
        /*List<CssDebitNote> billList = afOrderMapper.getOrderBill(SecurityUtils.getUser().getOrgId(), order_uuid);

        String incomeStatus = "核销完毕";
        for (int i = 0; i < billList.size(); i++) {
            CssDebitNote bill = billList.get(i);
            if (bill.getWriteoffComplete() == null || bill.getWriteoffComplete() != 1) {
                incomeStatus = "部分核销";
                break;
            }
        }*/
        if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
            //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
            afOrderMapper.generateTempTableA(SecurityUtils.getUser().getOrgId(), order_uuid);
            //根据临时表更新订单状态
            afOrderMapper.updateOrderStatusByTempTableA();

        } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
            //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
            afOrderMapper.generateTempTableS(SecurityUtils.getUser().getOrgId(), order_uuid);
            //根据临时表更新订单状态
            afOrderMapper.updateOrderStatusByTempTableS();
        } else if (bean.getBusinessScope().startsWith("T")) {
            //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
            afOrderMapper.generateTempTableT(SecurityUtils.getUser().getOrgId(), order_uuid);
            //根据临时表更新订单状态
            afOrderMapper.updateOrderStatusByTempTableT();
        } else if (bean.getBusinessScope().startsWith("L")) {
            //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
            afOrderMapper.generateTempTableL(SecurityUtils.getUser().getOrgId(), order_uuid);
            //根据临时表更新订单状态
            afOrderMapper.updateOrderStatusByTempTableL();
        } else if (bean.getBusinessScope().equals("IO")) {
            //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
            afOrderMapper.generateTempTableIO(SecurityUtils.getUser().getOrgId(), order_uuid);
            //根据临时表更新订单状态
            afOrderMapper.updateOrderStatusByTempTableIO();
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Boolean doListWriteoff(CssIncomeWriteoff bean) {

        //生成编号
        String code = getCode(bean.getBusinessScope());
        List<CssIncomeWriteoff> codeList = baseMapper.selectCode(SecurityUtils.getUser().getOrgId(), code);

        if (codeList.size() == 0) {
            bean.setWriteoffNum(code + "0001");
        } else {
            if ((code + "9999").equals(codeList.get(0).getWriteoffNum())) {
                throw new RuntimeException("每天最多可以核销9999个" + bean.getBusinessScope() + "清单");
            } else {
                String str = codeList.get(0).getWriteoffNum();
                str = str.substring(str.length() - 4);
                bean.setWriteoffNum(code + String.format("%04d", Integer.parseInt(str) + 1));
            }

        }

        bean.setCreateTime(new Date());
        bean.setCreatorId(SecurityUtils.getUser().getId());
        bean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        if(bean.getFinancialAccountName() != null && !"".equals(bean.getFinancialAccountName())){
            bean.setFinancialAccountName(bean.getFinancialAccountName().substring(0,bean.getFinancialAccountName().lastIndexOf(" ")));
        }

        //核销
        baseMapper.insert(bean);

        BigDecimal total = new BigDecimal(0);
        BigDecimal total2 = new BigDecimal(0);
        //核销明细
        for (int i = 0; i < bean.getListCurrencyList().size(); i++) {
            CssIncomeWriteoffDetail detailBean = new CssIncomeWriteoffDetail();
            detailBean.setIncomeWriteoffId(bean.getIncomeWriteoffId());
            detailBean.setStatementId(bean.getListCurrencyList().get(i).getStatementId());
            detailBean.setCurrency(bean.getListCurrencyList().get(i).getCurrency());
            detailBean.setAmountWriteoff(bean.getListCurrencyList().get(i).getAmountWriteoff2());
            detailBean.setFunctionalAmountWriteoff(bean.getListCurrencyList().get(i).getFunctionalAmountWriteoff2());
            if (detailBean.getAmountWriteoff().compareTo(new BigDecimal(0)) == 0) {
                continue;
            }
            cssIncomeWriteoffDetailMapper.insert(detailBean);
            //
            StatementCurrency cssDebitNoteCurrency = baseMapper.getCssStatementCurrency(SecurityUtils.getUser().getOrgId(),
                    bean.getListCurrencyList().get(i).getStatementId(), bean.getListCurrencyList().get(i).getCurrency());
            baseMapper.updateCssStatementCurrency(SecurityUtils.getUser().getOrgId(),
                    bean.getListCurrencyList().get(i).getStatementId(), bean.getListCurrencyList().get(i).getCurrency(),
                    cssDebitNoteCurrency.getAmountWriteoff().add(bean.getListCurrencyList().get(i).getAmountWriteoff2()),
                    cssDebitNoteCurrency.getFunctionalAmountWriteoff().add(bean.getListCurrencyList().get(i).getFunctionalAmountWriteoff2()));
            total = total.add(bean.getListCurrencyList().get(i).getFunctionalAmountWriteoff2());
            total2 = total2.add(bean.getListCurrencyList().get(i).getAmountWriteoff2());
        }
        Statement cssDebitNote = baseMapper.getCssStatement(SecurityUtils.getUser().getOrgId(), bean.getStatementId());
        if (cssDebitNote == null) {
            throw new RuntimeException("清单不存在");
        }
        if (!bean.getRowUuid().equals(cssDebitNote.getRowUuid())) {
            throw new RuntimeException("清单不是最新数据，请刷新页面再操作");
        }
        baseMapper.updateCssStatement(SecurityUtils.getUser().getOrgId(),
                bean.getStatementId(), cssDebitNote.getFunctionalAmountWriteoff().add(total));
        //修改账单状态
        int flag = 1;
        for (int i = 0; i < bean.getListCurrencyList().size(); i++) {
            StatementCurrency cssDebitNoteCurrency = baseMapper.getCssStatementCurrency(SecurityUtils.getUser().getOrgId(),
                    bean.getListCurrencyList().get(i).getStatementId(), bean.getListCurrencyList().get(i).getCurrency());
            if (cssDebitNoteCurrency.getAmount().compareTo(cssDebitNoteCurrency.getAmountWriteoff()) != 0 &&
                    cssDebitNoteCurrency.getFunctionalAmount().compareTo(cssDebitNoteCurrency.getFunctionalAmountWriteoff()) != 0) {
                flag = 0;
            }
        }
        baseMapper.updateListStatus(SecurityUtils.getUser().getOrgId(), bean.getStatementId(), flag);
        //修改账单核销金额
        int totalFlag = 1;
        if (total.compareTo(new BigDecimal(0)) == -1) {
            totalFlag = -1;
        }
        List<CssDebitNote> list = baseMapper.getUpdateList3(SecurityUtils.getUser().getOrgId(), bean.getStatementId(), bean.getBusinessScope());
        list.stream().forEach(writeoffBeforeDebitNote -> {
            LambdaQueryWrapper<CssDebitNoteCurrency> cssDebitNoteCurrencyWrapper = Wrappers.<CssDebitNoteCurrency>lambdaQuery();
            cssDebitNoteCurrencyWrapper.eq(CssDebitNoteCurrency::getDebitNoteId, writeoffBeforeDebitNote.getDebitNoteId()).eq(CssDebitNoteCurrency::getOrgId, SecurityUtils.getUser().getOrgId());
            List<CssDebitNoteCurrency> cssDebitNoteCurrencies = cssDebitNoteCurrencyService.list(cssDebitNoteCurrencyWrapper);
            writeoffBeforeDebitNote.setDebitCurrencyList(cssDebitNoteCurrencies);
        });

        if (flag == 1) {
            baseMapper.updateNote(SecurityUtils.getUser().getOrgId(), bean.getStatementId());
            for (int i = 0; i < list.size(); i++) {
                baseMapper.updateNoteCurrency(SecurityUtils.getUser().getOrgId(), list.get(i).getDebitNoteId());
            }
        } else {
            if (totalFlag == -1) {
                for (int i = 0; i < list.size(); i++) {
                    CssDebitNote note = list.get(i);
                    baseMapper.updateNoteCurrency2(SecurityUtils.getUser().getOrgId(), note.getDebitNoteId(), bean.getCurrency());
                }

            } else {
                for (int i = 0; i < list.size(); i++) {
                    CssDebitNote note = list.get(i);
                    List<CssDebitNoteCurrency> currencyList = baseMapper.getCurrencyList2(SecurityUtils.getUser().getOrgId(), note.getDebitNoteId(), bean.getCurrency());
                    for (int j = 0; j < currencyList.size(); j++) {
                        total = total.subtract(currencyList.get(j).getFunctionalAmount());
                        total2 = total2.subtract(currencyList.get(j).getAmount());
                    }

                    baseMapper.updateNoteCurrency22(SecurityUtils.getUser().getOrgId(), note.getDebitNoteId(), bean.getCurrency());
                }
                for (int i = 0; i < list.size(); i++) {
                    CssDebitNote note = list.get(i);
                    List<CssDebitNoteCurrency> currencyList = baseMapper.getCurrencyList22(SecurityUtils.getUser().getOrgId(), note.getDebitNoteId(), bean.getCurrency());

                    for (int j = 0; j < currencyList.size(); j++) {
                        CssDebitNoteCurrency currency = currencyList.get(j);
                        if (total.compareTo(new BigDecimal(0)) != 0) {
                            if (currency.getFunctionalAmountWriteoffNo().compareTo(total) == 1) {
                                baseMapper.updateNoteCurrency5(SecurityUtils.getUser().getOrgId(), note.getDebitNoteId(), bean.getCurrency(), currency.getAmountWriteoff().add(total2), currency.getFunctionalAmountWriteoff().add(total));
                                total = total.subtract(total);
                                total2 = total2.subtract(total2);
                            } else {
                                baseMapper.updateNoteCurrency5(SecurityUtils.getUser().getOrgId(), note.getDebitNoteId(), bean.getCurrency(), currency.getAmountWriteoff().add(currency.getAmountWriteoffNo()), currency.getFunctionalAmountWriteoff().add(currency.getFunctionalAmountWriteoffNo()));
                                total = total.subtract(currency.getFunctionalAmountWriteoffNo());
                                total2 = total2.subtract(currency.getAmountWriteoffNo());
                            }
                        }

                    }
                }
            }

            for (int i = 0; i < list.size(); i++) {
                CssDebitNote note = list.get(i);
                BigDecimal functional_amount_writeoff = new BigDecimal(0);
                List<CssDebitNoteCurrency> currencyList = baseMapper.getCurrencyList(SecurityUtils.getUser().getOrgId(), note.getDebitNoteId());
                for (int j = 0; j < currencyList.size(); j++) {
                    functional_amount_writeoff = functional_amount_writeoff.add(currencyList.get(j).getFunctionalAmountWriteoff());
                }

                LambdaQueryWrapper<CssDebitNoteCurrency> cssDebitNoteCurrencyWrapper = Wrappers.<CssDebitNoteCurrency>lambdaQuery();
                cssDebitNoteCurrencyWrapper.eq(CssDebitNoteCurrency::getDebitNoteId, note.getDebitNoteId()).eq(CssDebitNoteCurrency::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssDebitNoteCurrency::getCurrency, bean.getCurrency());
                CssDebitNoteCurrency cssDebitNoteCurrencyAfter = cssDebitNoteCurrencyService.getOne(cssDebitNoteCurrencyWrapper);
                if (cssDebitNoteCurrencyAfter == null) {
                    continue;
                }
                CssDebitNoteCurrency cssDebitNoteCurrencyBefore = note.getDebitCurrencyList().stream().filter(cssDebitNoteCurrency -> cssDebitNoteCurrency.getCurrency().equals(bean.getCurrency())).collect(Collectors.toList()).get(0);
                if (cssDebitNoteCurrencyBefore.getFunctionalAmountWriteoff() == null && cssDebitNoteCurrencyAfter.getFunctionalAmountWriteoff() != null) {
                    baseMapper.updateNote3(SecurityUtils.getUser().getOrgId(), note.getDebitNoteId(), functional_amount_writeoff);
                } else if (cssDebitNoteCurrencyBefore.getFunctionalAmountWriteoff() != null && cssDebitNoteCurrencyAfter.getFunctionalAmountWriteoff().compareTo(cssDebitNoteCurrencyBefore.getFunctionalAmountWriteoff()) != 0) {
                    baseMapper.updateNote3(SecurityUtils.getUser().getOrgId(), note.getDebitNoteId(), functional_amount_writeoff);
                }

            }
            LambdaQueryWrapper<CssDebitNote> cssDebitNoteWrapper = Wrappers.<CssDebitNote>lambdaQuery();
            cssDebitNoteWrapper.eq(CssDebitNote::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssDebitNote::getStatementId, bean.getStatementId());
            List<CssDebitNote> billList = cssDebitNoteService.list(cssDebitNoteWrapper);
            for (int i = 0; i < billList.size(); i++) {
                CssDebitNote bill = billList.get(i);
                if (bill.getFunctionalAmountWriteoff() != null && bill.getFunctionalAmountWriteoff().compareTo(bill.getFunctionalAmount()) == 0) {
                    baseMapper.updateNote33(SecurityUtils.getUser().getOrgId(), bill.getDebitNoteId());
                }
            }
        }
        //记录本次发生核销的账单的集合
        List<CssDebitNote> writeoffAfterList = baseMapper.getUpdateList2(SecurityUtils.getUser().getOrgId(), bean.getStatementId());
        list.stream().forEach(writeoffBeforeDebitNote -> {
            CssDebitNote writeoffAfterOne = writeoffAfterList.stream().filter(writeoffAfterDebitNote -> writeoffAfterDebitNote.getDebitNoteId().equals(writeoffBeforeDebitNote.getDebitNoteId())).collect(Collectors.toList()).get(0);
            //记录本次发生核销的账单
            LambdaQueryWrapper<CssDebitNoteCurrency> cssDebitNoteCurrencyWrapper = Wrappers.<CssDebitNoteCurrency>lambdaQuery();
            cssDebitNoteCurrencyWrapper.eq(CssDebitNoteCurrency::getDebitNoteId, writeoffBeforeDebitNote.getDebitNoteId()).eq(CssDebitNoteCurrency::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssDebitNoteCurrency::getCurrency, bean.getCurrency());
            CssDebitNoteCurrency cssDebitNoteCurrencyAfter = cssDebitNoteCurrencyService.getOne(cssDebitNoteCurrencyWrapper);
            if (cssDebitNoteCurrencyAfter != null) {
                CssDebitNoteCurrency cssDebitNoteCurrencyBefore = writeoffBeforeDebitNote.getDebitCurrencyList().stream().filter(cssDebitNoteCurrency -> cssDebitNoteCurrency.getCurrency().equals(bean.getCurrency())).collect(Collectors.toList()).get(0);
                if (cssDebitNoteCurrencyBefore.getFunctionalAmountWriteoff() == null && cssDebitNoteCurrencyAfter.getFunctionalAmountWriteoff() != null) {
                    CssIncomeWriteoffStatementDetail cssIncomeWriteoffStatementDetail = new CssIncomeWriteoffStatementDetail();
                    cssIncomeWriteoffStatementDetail.setAmountWriteoff(cssDebitNoteCurrencyAfter.getAmountWriteoff());
                    cssIncomeWriteoffStatementDetail.setCurrency(bean.getCurrency());
                    cssIncomeWriteoffStatementDetail.setDebitNoteId(writeoffBeforeDebitNote.getDebitNoteId());
                    cssIncomeWriteoffStatementDetail.setFunctionalAmountWriteoff(cssDebitNoteCurrencyAfter.getFunctionalAmountWriteoff());
                    cssIncomeWriteoffStatementDetail.setStatementId(bean.getStatementId());
                    cssIncomeWriteoffStatementDetail.setIncomeWriteoffId(bean.getIncomeWriteoffId());
                    cssIncomeWriteoffStatementDetailService.save(cssIncomeWriteoffStatementDetail);
                } else if (cssDebitNoteCurrencyBefore.getFunctionalAmountWriteoff() != null && cssDebitNoteCurrencyAfter.getFunctionalAmountWriteoff().compareTo(cssDebitNoteCurrencyBefore.getFunctionalAmountWriteoff()) != 0) {
                    CssIncomeWriteoffStatementDetail cssIncomeWriteoffStatementDetail = new CssIncomeWriteoffStatementDetail();
                    cssIncomeWriteoffStatementDetail.setAmountWriteoff(cssDebitNoteCurrencyAfter.getAmountWriteoff().subtract(cssDebitNoteCurrencyBefore.getAmountWriteoff()));
                    cssIncomeWriteoffStatementDetail.setCurrency(bean.getCurrency());
                    cssIncomeWriteoffStatementDetail.setDebitNoteId(writeoffBeforeDebitNote.getDebitNoteId());
                    cssIncomeWriteoffStatementDetail.setFunctionalAmountWriteoff(cssDebitNoteCurrencyAfter.getFunctionalAmountWriteoff().subtract(cssDebitNoteCurrencyBefore.getFunctionalAmountWriteoff()));
                    cssIncomeWriteoffStatementDetail.setStatementId(bean.getStatementId());
                    cssIncomeWriteoffStatementDetail.setIncomeWriteoffId(bean.getIncomeWriteoffId());
                    cssIncomeWriteoffStatementDetailService.save(cssIncomeWriteoffStatementDetail);
                }
            }

        });
        //修改订单费用状态
        String order_uuid = "";
        List<String> uuidList = afOrderMapper.getOrderUUID2(SecurityUtils.getUser().getOrgId(), bean.getStatementId());
        for (int j = 0; j < uuidList.size(); j++) {

            order_uuid = uuidList.get(j);
            /*List<CssDebitNote> billList = afOrderMapper.getOrderBill(SecurityUtils.getUser().getOrgId(), order_uuid);

            String incomeStatus = "核销完毕";
            for (int i = 0; i < billList.size(); i++) {
                CssDebitNote bill = billList.get(i);
                if (bill.getWriteoffComplete() == null || bill.getWriteoffComplete() != 1) {
                    incomeStatus = "部分核销";
                    break;
                }
            }*/
            if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
                afOrderMapper.generateTempTableA(SecurityUtils.getUser().getOrgId(), order_uuid);
                //根据临时表更新订单状态
                afOrderMapper.updateOrderStatusByTempTableA();
            } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
                afOrderMapper.generateTempTableS(SecurityUtils.getUser().getOrgId(), order_uuid);
                //根据临时表更新订单状态
                afOrderMapper.updateOrderStatusByTempTableS();
            } else if (bean.getBusinessScope().startsWith("T")) {
                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
                afOrderMapper.generateTempTableT(SecurityUtils.getUser().getOrgId(), order_uuid);
                //根据临时表更新订单状态
                afOrderMapper.updateOrderStatusByTempTableT();
            } else if (bean.getBusinessScope().startsWith("L")) {
                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
                afOrderMapper.generateTempTableL(SecurityUtils.getUser().getOrgId(), order_uuid);
                //根据临时表更新订单状态
                afOrderMapper.updateOrderStatusByTempTableL();
            }else if (bean.getBusinessScope().equals("IO")) {
                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
                afOrderMapper.generateTempTableIO(SecurityUtils.getUser().getOrgId(), order_uuid);
                //根据临时表更新订单状态
                afOrderMapper.updateOrderStatusByTempTableIO();
            }
        }
        return true;
    }

    @Override
    public CssIncomeWriteoff getVoucherDate(Integer incomeWriteoffId) {
        return baseMapper.selectById(incomeWriteoffId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doDeleteBillWriteoff(CssIncomeWriteoff bean) {

        List<CssIncomeWriteoffDetail> detailList = baseMapper.getDetailList(bean.getIncomeWriteoffId());


        BigDecimal total = new BigDecimal(0);
        //修改账单币种
        for (int i = 0; i < detailList.size(); i++) {
            CssIncomeWriteoffDetail detailBean = detailList.get(i);

            CssDebitNoteCurrency cssDebitNoteCurrency = baseMapper.getCssDebitNoteCurrency(SecurityUtils.getUser().getOrgId(), detailBean.getDebitNoteId(), detailBean.getCurrency());
            baseMapper.updateCssDebitNoteCurrency(SecurityUtils.getUser().getOrgId(),
                    detailBean.getDebitNoteId(), detailBean.getCurrency(),
                    cssDebitNoteCurrency.getAmountWriteoff().subtract(detailBean.getAmountWriteoff()),
                    cssDebitNoteCurrency.getFunctionalAmountWriteoff().subtract(detailBean.getFunctionalAmountWriteoff()));
            total = total.add(detailBean.getFunctionalAmountWriteoff());
            //删除明细
            cssIncomeWriteoffDetailMapper.deleteById(detailBean.getIncomeWriteoffDetailId());
        }
        //修改账单
        CssDebitNote cssDebitNote = baseMapper.getCssDebitNote(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteId());
        baseMapper.updateCssDebitNote(SecurityUtils.getUser().getOrgId(),
                bean.getDebitNoteId(), cssDebitNote.getFunctionalAmountWriteoff().subtract(total), UUID.randomUUID().toString());

        //删除
        baseMapper.deleteById(bean.getIncomeWriteoffId());
        //修改账单状态
        List<CssIncomeWriteoffDetail> detailList2 = baseMapper.getDetailList2(bean.getDebitNoteId());
        if (detailList2.size() > 0) {
            baseMapper.updateBillStatus(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteId(), 0);
        } else {
            baseMapper.updateBillStatus(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteId(), null);
        }

        //修改订单费用状态
        String order_uuid = "";
        List<String> uuidList = afOrderMapper.getOrderUUID(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteId());
        if (uuidList.size() > 0) {
            order_uuid = uuidList.get(0);
        }
        /*List<CssDebitNote> billList = afOrderMapper.getOrderBill(SecurityUtils.getUser().getOrgId(), order_uuid);

        String incomeStatus = "已制账单";
        for (int i = 0; i < billList.size(); i++) {
            CssDebitNote bill = billList.get(i);
            if (bill.getWriteoffComplete() != null) {
                incomeStatus = "部分核销";
                break;
            }
        }*/
        if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
            //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
            afOrderMapper.generateTempTableA(SecurityUtils.getUser().getOrgId(), order_uuid);
            //根据临时表更新订单状态
            afOrderMapper.updateOrderStatusByTempTableA();
        } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
            //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
            afOrderMapper.generateTempTableS(SecurityUtils.getUser().getOrgId(), order_uuid);
            //根据临时表更新订单状态
            afOrderMapper.updateOrderStatusByTempTableS();
        } else if (bean.getBusinessScope().startsWith("T")) {
            //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
            afOrderMapper.generateTempTableT(SecurityUtils.getUser().getOrgId(), order_uuid);
            //根据临时表更新订单状态
            afOrderMapper.updateOrderStatusByTempTableT();
        } else if (bean.getBusinessScope().startsWith("L")) {
            //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
            afOrderMapper.generateTempTableL(SecurityUtils.getUser().getOrgId(), order_uuid);
            //根据临时表更新订单状态
            afOrderMapper.updateOrderStatusByTempTableL();
        }else if (bean.getBusinessScope().equals("IO")) {
            //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
            afOrderMapper.generateTempTableIO(SecurityUtils.getUser().getOrgId(), order_uuid);
            //根据临时表更新订单状态
            afOrderMapper.updateOrderStatusByTempTableIO();
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doDeleteListWriteoff(CssIncomeWriteoff bean) {
        CssIncomeWriteoff cssIncomeWriteoff = getById(bean.getIncomeWriteoffId());
        if (cssIncomeWriteoff == null) {
            throw new RuntimeException("核销单不存在，请刷新页面再试");
        }
        List<CssIncomeWriteoffDetail> detailList = baseMapper.getDetailList(bean.getIncomeWriteoffId());

        BigDecimal total = new BigDecimal(0);
        BigDecimal total2 = new BigDecimal(0);
        //修改账单币种
        for (int i = 0; i < detailList.size(); i++) {
            CssIncomeWriteoffDetail detailBean = detailList.get(i);

            StatementCurrency cssDebitNoteCurrency = baseMapper.getCssStatementCurrency(SecurityUtils.getUser().getOrgId(),
                    detailBean.getStatementId(), detailBean.getCurrency());
            baseMapper.updateCssStatementCurrency(SecurityUtils.getUser().getOrgId(), detailBean.getStatementId(), detailBean.getCurrency(), cssDebitNoteCurrency.getAmountWriteoff().subtract(detailBean.getAmountWriteoff()).compareTo(BigDecimal.ZERO) == 0 ? null : cssDebitNoteCurrency.getAmountWriteoff().subtract(detailBean.getAmountWriteoff()), cssDebitNoteCurrency.getFunctionalAmountWriteoff().subtract(detailBean.getFunctionalAmountWriteoff()).compareTo(BigDecimal.ZERO) == 0 ? null : cssDebitNoteCurrency.getFunctionalAmountWriteoff().subtract(detailBean.getFunctionalAmountWriteoff()));

            total = total.add(detailBean.getFunctionalAmountWriteoff());
            total2 = total2.add(detailBean.getAmountWriteoff());
            //删除明细
            cssIncomeWriteoffDetailMapper.deleteById(detailBean.getIncomeWriteoffDetailId());
        }
        //修改清单
        Statement cssDebitNote = baseMapper.getCssStatement(SecurityUtils.getUser().getOrgId(), bean.getStatementId());
        baseMapper.updateCssStatement(SecurityUtils.getUser().getOrgId(), bean.getStatementId(), cssDebitNote.getFunctionalAmountWriteoff().subtract(total).compareTo(BigDecimal.ZERO) == 0 ? null : cssDebitNote.getFunctionalAmountWriteoff().subtract(total));

        //删除
        baseMapper.deleteById(bean.getIncomeWriteoffId());

        //修改清单状态
        List<CssIncomeWriteoffDetail> detailList2 = baseMapper.getDetailList3(bean.getStatementId());
        if (detailList2.size() > 0) {
            baseMapper.updateListStatus(SecurityUtils.getUser().getOrgId(), bean.getStatementId(), 0);
        } else {
            baseMapper.updateListStatus(SecurityUtils.getUser().getOrgId(), bean.getStatementId(), null);
        }
        //修改账单核销金额
        int totalFlag = 1;
        if (total.compareTo(new BigDecimal(0)) == -1) {
            totalFlag = -1;
        }
        List<CssDebitNote> list = baseMapper.getUpdateList3ForDelete(SecurityUtils.getUser().getOrgId(), bean.getStatementId(), bean.getBusinessScope());
        list.stream().forEach(writeoffBeforeDebitNote -> {
            LambdaQueryWrapper<CssDebitNoteCurrency> cssDebitNoteCurrencyWrapper = Wrappers.<CssDebitNoteCurrency>lambdaQuery();
            cssDebitNoteCurrencyWrapper.eq(CssDebitNoteCurrency::getDebitNoteId, writeoffBeforeDebitNote.getDebitNoteId()).eq(CssDebitNoteCurrency::getOrgId, SecurityUtils.getUser().getOrgId());
            List<CssDebitNoteCurrency> cssDebitNoteCurrencies = cssDebitNoteCurrencyService.list(cssDebitNoteCurrencyWrapper);
            writeoffBeforeDebitNote.setDebitCurrencyList(cssDebitNoteCurrencies);
        });
        if (detailList2.size() == 0) {
            baseMapper.updateNote22(SecurityUtils.getUser().getOrgId(), bean.getStatementId());
            for (int i = 0; i < list.size(); i++) {
                baseMapper.updateNoteCurrency4(SecurityUtils.getUser().getOrgId(), list.get(i).getDebitNoteId());
            }
        } else {
            if (totalFlag == -1) {
                for (int i = 0; i < list.size(); i++) {
                    CssDebitNote note = list.get(i);
                    baseMapper.updateNoteCurrency3(SecurityUtils.getUser().getOrgId(), note.getDebitNoteId(), bean.getCurrency());
                }

            } else {
//                for (int i = 0; i < list.size(); i++) {
//                    CssDebitNote note = list.get(i);
//                    List<CssDebitNoteCurrency> currencyList = baseMapper.getCurrencyList22ForDelete(SecurityUtils.getUser().getOrgId(), note.getDebitNoteId(), bean.getCurrency());
//
//                    for (int j = 0; j < currencyList.size(); j++) {
//                        CssDebitNoteCurrency currency = currencyList.get(j);
//                        if (total.compareTo(new BigDecimal(0)) != 0) {
//                            if (currency.getFunctionalAmountWriteoff().compareTo(total) == 1) {
//                                baseMapper.updateNoteCurrency5(SecurityUtils.getUser().getOrgId(), note.getDebitNoteId(), bean.getCurrency(), currency.getAmountWriteoff().subtract(total2), currency.getFunctionalAmountWriteoff().subtract(total));
//                                total = total.subtract(total);
//                                total2 = total2.subtract(total2);
//                            } else {
//                                baseMapper.updateNoteCurrency5(SecurityUtils.getUser().getOrgId(), note.getDebitNoteId(), bean.getCurrency(), null, null);
//                                total = total.subtract(currency.getFunctionalAmountWriteoff());
//                                total2 = total2.subtract(currency.getAmountWriteoff());
//                            }
//                        }
//
//                    }
//                }
                LambdaQueryWrapper<CssIncomeWriteoffStatementDetail> cssIncomeWriteoffStatementDetailWrapper = Wrappers.<CssIncomeWriteoffStatementDetail>lambdaQuery();
                cssIncomeWriteoffStatementDetailWrapper.eq(CssIncomeWriteoffStatementDetail::getIncomeWriteoffId, bean.getIncomeWriteoffId());
                List<CssIncomeWriteoffStatementDetail> incomeWriteoffStatementDetails = cssIncomeWriteoffStatementDetailService.list(cssIncomeWriteoffStatementDetailWrapper);
                incomeWriteoffStatementDetails.stream().forEach(cssIncomeWriteoffStatementDetail -> {
                    LambdaQueryWrapper<CssDebitNoteCurrency> cssDebitNoteCurrencyWrapper = Wrappers.<CssDebitNoteCurrency>lambdaQuery();
                    cssDebitNoteCurrencyWrapper.eq(CssDebitNoteCurrency::getDebitNoteId, cssIncomeWriteoffStatementDetail.getDebitNoteId()).eq(CssDebitNoteCurrency::getCurrency, cssIncomeWriteoffStatementDetail.getCurrency()).eq(CssDebitNoteCurrency::getOrgId, SecurityUtils.getUser().getOrgId());
                    CssDebitNoteCurrency cssDebitNoteCurrency = cssDebitNoteCurrencyService.getOne(cssDebitNoteCurrencyWrapper);
                    cssDebitNoteCurrency.setAmountWriteoff(cssDebitNoteCurrency.getAmountWriteoff().subtract(cssIncomeWriteoffStatementDetail.getAmountWriteoff()).compareTo(BigDecimal.ZERO) == 0 ? null : cssDebitNoteCurrency.getAmountWriteoff().subtract(cssIncomeWriteoffStatementDetail.getAmountWriteoff()));
                    cssDebitNoteCurrency.setFunctionalAmountWriteoff(cssDebitNoteCurrency.getFunctionalAmountWriteoff().subtract(cssIncomeWriteoffStatementDetail.getFunctionalAmountWriteoff()).compareTo(BigDecimal.ZERO) == 0 ? null : cssDebitNoteCurrency.getFunctionalAmountWriteoff().subtract(cssIncomeWriteoffStatementDetail.getFunctionalAmountWriteoff()));
                    cssDebitNoteCurrencyService.updateById(cssDebitNoteCurrency);
                });

            }
            for (int i = 0; i < list.size(); i++) {
                CssDebitNote note = list.get(i);
                BigDecimal functional_amount_writeoff = new BigDecimal(0);
                List<CssDebitNoteCurrency> currencyList = baseMapper.getCurrencyList(SecurityUtils.getUser().getOrgId(), note.getDebitNoteId());
                for (int j = 0; j < currencyList.size(); j++) {
                    functional_amount_writeoff = functional_amount_writeoff.add(currencyList.get(j).getFunctionalAmountWriteoff());
                }

                LambdaQueryWrapper<CssDebitNoteCurrency> cssDebitNoteCurrencyWrapper = Wrappers.<CssDebitNoteCurrency>lambdaQuery();
                cssDebitNoteCurrencyWrapper.eq(CssDebitNoteCurrency::getDebitNoteId, note.getDebitNoteId()).eq(CssDebitNoteCurrency::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssDebitNoteCurrency::getCurrency, bean.getCurrency());
                CssDebitNoteCurrency cssDebitNoteCurrencyAfter = cssDebitNoteCurrencyService.getOne(cssDebitNoteCurrencyWrapper);
                if (cssDebitNoteCurrencyAfter == null) {
                    continue;
                }
                CssDebitNoteCurrency cssDebitNoteCurrencyBefore = note.getDebitCurrencyList().stream().filter(cssDebitNoteCurrency -> cssDebitNoteCurrency.getCurrency().equals(bean.getCurrency())).collect(Collectors.toList()).get(0);
                if (cssDebitNoteCurrencyBefore.getFunctionalAmountWriteoff() != null && cssDebitNoteCurrencyAfter.getFunctionalAmountWriteoff() == null) {
                    if (functional_amount_writeoff.compareTo(new BigDecimal(0)) != 0) {
                        baseMapper.updateNote3(SecurityUtils.getUser().getOrgId(), note.getDebitNoteId(), functional_amount_writeoff);
                    } else {
                        baseMapper.updateNote4(SecurityUtils.getUser().getOrgId(), note.getDebitNoteId(), null);
                    }
                } else if (cssDebitNoteCurrencyBefore.getFunctionalAmountWriteoff() != null && cssDebitNoteCurrencyAfter.getFunctionalAmountWriteoff().compareTo(cssDebitNoteCurrencyBefore.getFunctionalAmountWriteoff()) != 0) {
                    if (functional_amount_writeoff.compareTo(new BigDecimal(0)) != 0) {
                        baseMapper.updateNote3(SecurityUtils.getUser().getOrgId(), note.getDebitNoteId(), functional_amount_writeoff);
                    } else {
                        baseMapper.updateNote4(SecurityUtils.getUser().getOrgId(), note.getDebitNoteId(), null);
                    }
                }


            }
        }

        //删除清单账单核销记录
        LambdaQueryWrapper<CssIncomeWriteoffStatementDetail> cssIncomeWriteoffStatementDetailWrapper = Wrappers.<CssIncomeWriteoffStatementDetail>lambdaQuery();
        cssIncomeWriteoffStatementDetailWrapper.eq(CssIncomeWriteoffStatementDetail::getIncomeWriteoffId, bean.getIncomeWriteoffId());
        cssIncomeWriteoffStatementDetailService.remove(cssIncomeWriteoffStatementDetailWrapper);

        //修改订单费用状态
        String order_uuid = "";
        List<String> uuidList = afOrderMapper.getOrderUUID2(SecurityUtils.getUser().getOrgId(), bean.getStatementId());
        for (int j = 0; j < uuidList.size(); j++) {

            order_uuid = uuidList.get(j);
            /*List<CssDebitNote> billList = afOrderMapper.getOrderBill(SecurityUtils.getUser().getOrgId(), order_uuid);

            String incomeStatus = "已制账单";
            for (int i = 0; i < billList.size(); i++) {
                CssDebitNote bill = billList.get(i);
                if (bill.getWriteoffComplete() != null) {
                    incomeStatus = "部分核销";
                    break;
                }
            }*/
            if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
                afOrderMapper.generateTempTableA(SecurityUtils.getUser().getOrgId(), order_uuid);
                //根据临时表更新订单状态
                afOrderMapper.updateOrderStatusByTempTableA();
            } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
                afOrderMapper.generateTempTableS(SecurityUtils.getUser().getOrgId(), order_uuid);
                //根据临时表更新订单状态
                afOrderMapper.updateOrderStatusByTempTableS();
            } else if (bean.getBusinessScope().startsWith("T")) {
                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
                afOrderMapper.generateTempTableT(SecurityUtils.getUser().getOrgId(), order_uuid);
                //根据临时表更新订单状态
                afOrderMapper.updateOrderStatusByTempTableT();
            } else if (bean.getBusinessScope().startsWith("L")) {
                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
                afOrderMapper.generateTempTableL(SecurityUtils.getUser().getOrgId(), order_uuid);
                //根据临时表更新订单状态
                afOrderMapper.updateOrderStatusByTempTableL();
            }else if (bean.getBusinessScope().equals("IO")) {
                //生成一张临时表，存放订单的当前状态和核销后的状态，如果两者相同则不存入临时表
                afOrderMapper.generateTempTableIO(SecurityUtils.getUser().getOrgId(), order_uuid);
                //根据临时表更新订单状态
                afOrderMapper.updateOrderStatusByTempTableIO();
            }
        }
        return true;
    }

    @Override
    public List<CssDebitNoteCurrency> queryBillDetail(Integer incomeWriteoffId) {
        return baseMapper.queryBillDetail(SecurityUtils.getUser().getOrgId(), incomeWriteoffId);
    }

    @Override
    public List<StatementCurrency> queryListDetail(Integer incomeWriteoffId) {
        return baseMapper.queryListDetail(SecurityUtils.getUser().getOrgId(), incomeWriteoffId);
    }

    public void exportExcelList(CssIncomeWriteoff bean) {
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
            headers = new String[]{"核销单号", "核销日期", "收款客户", "账单号/清单号", "应收金额（原币）", "本次核销金额（原币）", "应收金额（本币）",
                    "本次核销金额（本币）", "备注", "核销人", "核销时间", "科目代码", "科目名称"};
            colunmStrs = new String[]{"writeoffNum", "writeoffDate", "customerName", "debitNoteNumStatementNum",
                    "currencyAmount", "currencyAmount2", "functionalAmount", "functionalAmountWriteoff", "writeoffRemark",
                    "creatorName", "createTime", "financialAccountCode", "financialAccountName"};
        }
        //查询
        Page page = new Page();
        page.setCurrent(1);
        page.setSize(1000000);
        IPage result = this.getPage2(page, bean);
        List<CssIncomeWriteoff> totalInfo = this.getTatol(bean);
        if (totalInfo != null && totalInfo.size() > 0) {
            LinkedHashMap mapTwo = new LinkedHashMap();
            CssIncomeWriteoff totalInfoOne = totalInfo.get(0);
            totalInfoOne.setWriteoffNum("合计");
            result.getRecords().add(totalInfoOne);
        }
        if (result != null && result.getRecords() != null && result.getRecords().size() > 0) {
            List<CssIncomeWriteoff> listA = result.getRecords();
            for (CssIncomeWriteoff excel2 : listA) {
                //处理核销日期
                if (excel2.getWriteoffDate() != null && !"".equals(excel2.getWriteoffDate())) {
                    excel2.setWriteoffDate(excel2.getWriteoffDate().split(" ")[0]);
                }
                if (excel2 != null && !"".equals(excel2.getCurrencyAmount())) {
                    excel2.setCurrencyAmount(excel2.getCurrencyAmount().replaceAll("  ", String.valueOf((char) 10) + ""));
                }
                if (excel2 != null && !"".equals(excel2.getCurrencyAmount2())) {
                    excel2.setCurrencyAmount2(excel2.getCurrencyAmount2().replaceAll("  ", String.valueOf((char) 10) + ""));
                }
                LinkedHashMap mapTwo = new LinkedHashMap();
                for (int j = 0; j < colunmStrs.length; j++) {
                    if ("creatorName".equals(colunmStrs[j])) {
                        if (excel2.getCreatorName() != null) {
                            mapTwo.put("creatorName", excel2.getCreatorName().split(" ")[0]);
                        } else {
                            mapTwo.put("creatorName", "");
                        }
                    } else if ("createTime".equals(colunmStrs[j])) {
                        if (excel2.getCreateTime() != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            mapTwo.put("createTime", sdf.format(excel2.getCreateTime()));
                        } else {
                            mapTwo.put("createTime", "");
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

    @Override
    public CssIncomeWriteoff getIncomeWriteoffById(Integer id) {
        CssIncomeWriteoff cssIncomeWriteoff = getById(id);
        LambdaQueryWrapper<CssIncomeWriteoffDetail> cssIncomeWriteoffDetailWrapper = Wrappers.<CssIncomeWriteoffDetail>lambdaQuery();
        cssIncomeWriteoffDetailWrapper.eq(CssIncomeWriteoffDetail::getIncomeWriteoffId, id);
        CssIncomeWriteoffDetail incomeWriteoffDetail = cssIncomeWriteoffDetailService.getOne(cssIncomeWriteoffDetailWrapper);
        cssIncomeWriteoff.setCssIncomeWriteoffDetail(incomeWriteoffDetail);
        if (cssIncomeWriteoff.getStatementId() != null) {
            //清单核销
            LambdaQueryWrapper<StatementCurrency> statementCurrencyWrapper = Wrappers.<StatementCurrency>lambdaQuery();
            statementCurrencyWrapper.eq(StatementCurrency::getOrgId, SecurityUtils.getUser().getOrgId()).eq(StatementCurrency::getStatementId, cssIncomeWriteoff.getStatementId()).eq(StatementCurrency::getCurrency, cssIncomeWriteoff.getCurrency());
            List<StatementCurrency> statementCurrencies = statementCurrencyService.list(statementCurrencyWrapper);
            cssIncomeWriteoff.setListCurrencyList(statementCurrencies);
        } else {
            //账单核销
            LambdaQueryWrapper<CssDebitNoteCurrency> cssDebitNoteCurrencyWrapper = Wrappers.<CssDebitNoteCurrency>lambdaQuery();
            cssDebitNoteCurrencyWrapper.eq(CssDebitNoteCurrency::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssDebitNoteCurrency::getDebitNoteId, cssIncomeWriteoff.getDebitNoteId()).eq(CssDebitNoteCurrency::getCurrency, cssIncomeWriteoff.getCurrency());
            List<CssDebitNoteCurrency> cssDebitNoteCurrencies = cssDebitNoteCurrencyService.list(cssDebitNoteCurrencyWrapper);
            cssIncomeWriteoff.setDebitCurrencyList(cssDebitNoteCurrencies);
        }

        return cssIncomeWriteoff;
    }

}
