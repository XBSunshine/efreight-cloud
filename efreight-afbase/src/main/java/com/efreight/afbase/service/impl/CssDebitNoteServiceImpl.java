package com.efreight.afbase.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.*;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.service.*;
import com.efreight.common.core.feign.RemoteServiceToSC;
import com.efreight.common.remoteVo.IoIncome;
import com.efreight.common.remoteVo.LcIncome;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 清单 服务实现类
 * </p>
 *
 * @author qipm
 * @since 2019-11-06
 */
@Service
@AllArgsConstructor
public class CssDebitNoteServiceImpl extends ServiceImpl<CssDebitNoteMapper, CssDebitNote> implements CssDebitNoteService {
    private final LogService logService;
    private final ScLogService scLogService;
    private final AfOrderMapper afOrderMapper;
    private final CssDebitNoteCurrencyMapper debitMapper;
    private final AfIncomeMapper afIncomeMapper;
    private final ScIncomeMapper scIncomeMapper;
    private final TcIncomeMapper tcIncomeMapper;
    private final TcLogService tcLogService;
    private final RemoteServiceToSC remoteServiceToSC;
    private final LcLogService lcLogService;
    private final IoLogService ioLogService;
    private final StatementMapper statementMapper;
    private final CssIncomeInvoiceDetailMapper detailMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doEditInvoiceRemark(DebitNote bean) {
        CssDebitNote dbDebitNote = baseMapper.selectById(bean.getDebitNoteId());
        Optional.ofNullable(dbDebitNote).ifPresent((item)->{
            if(item.getWriteoffComplete() !=null && item.getWriteoffComplete() == 1){
                throw new IllegalStateException("您好，该账单已经完全核销，不能修改发票信息。");
            }
        });

        baseMapper.doEditInvoiceRemark(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteId(), bean.getInvoiceRemark(), bean.getInvoiceTitle(), bean.getInvoiceNum(), bean.getInvoiceDate());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doEditInvoiceRemark2(DebitNote bean) {
        Statement statement = statementMapper.selectById(bean.getStatementId());
        Optional.ofNullable(statement).ifPresent((item)->{
            if(item.getWriteoffComplete() != null && item.getWriteoffComplete() == 1){
                throw new IllegalStateException("您好，该清单已经完全核销，不能修改发票信息。");
            }
        });
        baseMapper.doEditInvoiceRemark2(SecurityUtils.getUser().getOrgId(), bean.getStatementId(), bean.getInvoiceRemark(), bean.getInvoiceTitle(), bean.getInvoiceNum(), bean.getInvoiceDate());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doSave(CssDebitNote bean) {
        String incomeIdsStr = bean.getIncomeIds();
        String businessScope = bean.getBusinessScope();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        bean.setDebitNoteDate(formatter.format(new Date()));
        String code = getOrderCode(businessScope);
        List<CssDebitNote> codeList = baseMapper.selectCode(SecurityUtils.getUser().getOrgId(), code);

//		if (codeList.size() == 0) {
//			bean.setDebitNoteNum(code + "0001");
//		} else if (codeList.size() < 9999) {
//			bean.setDebitNoteNum(code + String.format("%04d", codeList.size() + 1));
//		} else {
//			throw new RuntimeException("每天最多可以创建9999个账单");
//		}
        if (codeList.size() == 0) {
            bean.setDebitNoteNum(code + "0001");
        } else {
            if ((code + "9999").equals(codeList.get(0).getDebitNoteNum())) {
                throw new RuntimeException("每天最多可以创建9999个" + businessScope + "账单");
            } else {
                String str = codeList.get(0).getDebitNoteNum();
                str = str.substring(str.length() - 4);
                bean.setDebitNoteNum(code + String.format("%04d", Integer.parseInt(str) + 1));
            }

        }
        bean.setRowUuid(UUID.randomUUID().toString());
        bean.setCreateTime(LocalDateTime.now());
        bean.setCreatorId(SecurityUtils.getUser().getId());
        bean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        baseMapper.insert(bean);
        //日志
        AfOrder orderBean = null;
        if (bean.getBusinessScope().startsWith("A")) {
            orderBean = afOrderMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        } else if (bean.getBusinessScope().startsWith("S")) {
            orderBean = afOrderMapper.getSEOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        } else if (bean.getBusinessScope().startsWith("T")) {
            orderBean = afOrderMapper.getTCOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        }else if(bean.getBusinessScope().startsWith("LC")){
        	orderBean = afOrderMapper.getLCOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        }else if(bean.getBusinessScope().startsWith("IO")) {
        	orderBean = afOrderMapper.getIOOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        }
        LogBean logBean = new LogBean();
        logBean.setPageName("费用录入");
        logBean.setPageFunction("制作账单");

        logBean.setLogRemark("账单号：" + bean.getDebitNoteNum());
        logBean.setBusinessScope(businessScope);
        logBean.setOrderNumber(orderBean == null ? "" : orderBean.getOrderCode());
        logBean.setOrderId(orderBean == null ? null : orderBean.getOrderId());
        logBean.setOrderUuid(orderBean == null ? "" : orderBean.getOrderUuid());
        bean.setIncomeIds("'" + bean.getIncomeIds().replaceAll(",", "','") + "'");

//		List<Integer> incodmeStatusList=afOrderMapper.getOrderIncomeStatus(SecurityUtils.getUser().getOrgId(),bean.getOrderUuid());
        if ("AE".equals(businessScope) || "AI".equals(businessScope)) {
            //rowuuid
            String incomeIds[] = incomeIdsStr.split(",");
            String incomeRowUuids[] = bean.getIncomeRowUuids().split(",");
            for (int i = 0; i < incomeIds.length; i++) {
                AfIncome bean2 = afIncomeMapper.selectById(incomeIds[i]);
                if (!incomeRowUuids[i].equals(bean2.getRowUuid())) {
                    throw new RuntimeException("账单下收入明细不是最新数据，请刷新页面重新操作");
                }
            }
            //修改订单费用状态
//            afOrderMapper.updateOrderIncomeStatus3(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), "已制账单", UUID.randomUUID().toString());

            //日志
            logService.saveLog(logBean);
            baseMapper.updateIncome(SecurityUtils.getUser().getOrgId(), bean.getIncomeIds(), bean.getDebitNoteId(), bean.getDebitNoteDate(), UUID.randomUUID().toString());
        } else if ("SE".equals(businessScope) || "SI".equals(businessScope)) {
            //rowuuid
            String incomeIds[] = incomeIdsStr.split(",");
            String incomeRowUuids[] = bean.getIncomeRowUuids().split(",");
            for (int i = 0; i < incomeIds.length; i++) {
                ScIncome bean2 = scIncomeMapper.selectById(incomeIds[i]);
                if (!incomeRowUuids[i].equals(bean2.getRowUuid())) {
                    throw new RuntimeException("账单下收入明细不是最新数据，请刷新页面重新操作");
                }
            }
            //修改订单费用状态
//            afOrderMapper.updateOrderIncomeStatusSE3(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), "已制账单", UUID.randomUUID().toString());
            //日志
            ScLog logBean2 = new ScLog();
            BeanUtils.copyProperties(logBean, logBean2);
            scLogService.saveLog(logBean2);
            baseMapper.updateIncomeSE(SecurityUtils.getUser().getOrgId(), bean.getIncomeIds(), bean.getDebitNoteId(), bean.getDebitNoteDate(), UUID.randomUUID().toString());
        } else if (businessScope.startsWith("T")) {
            String incomeIds[] = incomeIdsStr.split(",");
            String incomeRowUuids[] = bean.getIncomeRowUuids().split(",");
            for (int i = 0; i < incomeIds.length; i++) {
                TcIncome bean2 = tcIncomeMapper.selectById(incomeIds[i]);
                if (!incomeRowUuids[i].equals(bean2.getRowUuid())) {
                    throw new RuntimeException("账单下收入明细不是最新数据，请刷新页面重新操作");
                }
            }
            //修改订单费用状态
//            afOrderMapper.updateOrderIncomeStatusTC3(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), "已制账单", UUID.randomUUID().toString());
            //日志
            TcLog logBean2 = new TcLog();
            BeanUtils.copyProperties(logBean, logBean2);
            logBean2.setCreatorId(SecurityUtils.getUser().getId());
            logBean2.setCreatorName(SecurityUtils.getUser().buildOptName());
            logBean2.setCreatTime(LocalDateTime.now());
            logBean2.setOrgId(SecurityUtils.getUser().getOrgId());
            tcLogService.save(logBean2);
            baseMapper.updateIncomeTC(SecurityUtils.getUser().getOrgId(), bean.getIncomeIds(), bean.getDebitNoteId(), bean.getDebitNoteDate(), UUID.randomUUID().toString());
        } else if ("LC".equals(businessScope)) {
            String incomeIds[] = incomeIdsStr.split(",");
            String incomeRowUuids[] = bean.getIncomeRowUuids().split(",");
            for (int i = 0; i < incomeIds.length; i++) {
                LcIncome bean2 = remoteServiceToSC.viewLcIncome(Integer.valueOf(incomeIds[i])).getData();
                if (!incomeRowUuids[i].equals(bean2.getRowUuid())) {
                    throw new RuntimeException("账单下收入明细不是最新数据，请刷新页面重新操作");
                }
            }
            //修改订单费用状态
//            afOrderMapper.updateOrderIncomeStatusLC(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), "已制账单", UUID.randomUUID().toString());
            //日志
            LcLog logBean2 = new LcLog();
            BeanUtils.copyProperties(logBean, logBean2);
            logBean2.setCreatorId(SecurityUtils.getUser().getId());
            logBean2.setCreatorName(SecurityUtils.getUser().buildOptName());
            logBean2.setCreatTime(LocalDateTime.now());
            logBean2.setOrgId(SecurityUtils.getUser().getOrgId());
            lcLogService.save(logBean2);
            baseMapper.updateIncomeLC(SecurityUtils.getUser().getOrgId(), bean.getIncomeIds(), bean.getDebitNoteId(), bean.getDebitNoteDate(), UUID.randomUUID().toString());
        } else if ("IO".equals(businessScope)) {
            String incomeIds[] = incomeIdsStr.split(",");
            String incomeRowUuids[] = bean.getIncomeRowUuids().split(",");
            for (int i = 0; i < incomeIds.length; i++) {
                IoIncome bean2 = remoteServiceToSC.viewIoIncome(Integer.valueOf(incomeIds[i])).getData();
                if (!incomeRowUuids[i].equals(bean2.getRowUuid())) {
                    throw new RuntimeException("账单下收入明细不是最新数据，请刷新页面重新操作");
                }
            }
            //修改订单费用状态
//            afOrderMapper.updateOrderIncomeStatusIO(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), "已制账单", UUID.randomUUID().toString());
            //日志
            IoLog logBean2 = new IoLog();
            BeanUtils.copyProperties(logBean, logBean2);
            logBean2.setCreatorId(SecurityUtils.getUser().getId());
            logBean2.setCreatorName(SecurityUtils.getUser().buildOptName());
            logBean2.setCreatTime(LocalDateTime.now());
            logBean2.setOrgId(SecurityUtils.getUser().getOrgId());
            ioLogService.save(logBean2);
            baseMapper.updateIncomeIO(SecurityUtils.getUser().getOrgId(), bean.getIncomeIds(), bean.getDebitNoteId(), bean.getDebitNoteDate(), UUID.randomUUID().toString());
        }
        //
        for (int i = 0; i < bean.getDebitCurrencyList().size(); i++) {
            CssDebitNoteCurrency debitBean = bean.getDebitCurrencyList().get(i);
            debitBean.setDebitNoteId(bean.getDebitNoteId());
            debitBean.setOrgId(SecurityUtils.getUser().getOrgId());
            debitMapper.insert(debitBean);

            //汇率同步
            if ("AE".equals(businessScope) || "AI".equals(businessScope)) {
                baseMapper.updateIncome2(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteId(), debitBean.getCurrency(), debitBean.getExchangeRate());
            } else if ("SE".equals(businessScope) || "SI".equals(businessScope)) {
                baseMapper.updateIncomeSE2(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteId(), debitBean.getCurrency(), debitBean.getExchangeRate());
            } else if (businessScope.startsWith("T")) {
                baseMapper.updateIncomeTC2(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteId(), debitBean.getCurrency(), debitBean.getExchangeRate());
            } else if ("LC".equals(businessScope)) {
                baseMapper.updateIncomeLC2(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteId(), debitBean.getCurrency(), debitBean.getExchangeRate());
            } else if ("IO".equals(businessScope)) {
                baseMapper.updateIncomeIO2(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteId(), debitBean.getCurrency(), debitBean.getExchangeRate());
            }
        }

        //日志
//        logMapper.updateLog(SecurityUtils.getUser().getOrgId(),bean.getOrderUuid(),"财务锁账",SecurityUtils.getUser().getId(),
//        		SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail(),LocalDateTime.now());
//		baseMapper.updateOder(SecurityUtils.getUser().getOrgId(),bean.getOrderId());
        
      //更新订单应收状态：（order. income_status）
		List<Map> listMap = detailMapper.getOrderIncomeStatus(SecurityUtils.getUser().getOrgId(), orderBean.getOrderId().toString(),businessScope);
		if(listMap!=null&&listMap.size()>0) {
			for(Map map:listMap) {
				detailMapper.updateOrderIncomeStatus(Integer.valueOf(map.get("org_id").toString()),Integer.valueOf(map.get("order_id").toString()),map.get("income_status").toString(),UUID.randomUUID().toString(),businessScope);
			}
		}
        
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doUpdate(CssDebitNote bean) {
        String incomeIdsStr = bean.getIncomeIds();
        CssDebitNote order = getById(bean.getDebitNoteId());
        if (order == null) {
            throw new RuntimeException("账单不存在");
        }
        if (!bean.getRowUuid().equals(order.getRowUuid())) {
            throw new RuntimeException("账单不是最新数据，请刷新页面再操作");
        }
        String businessScope = bean.getBusinessScope();
        bean.setRowUuid(UUID.randomUUID().toString());
        bean.setEditTime(LocalDateTime.now());
        bean.setEditorId(SecurityUtils.getUser().getId());
        bean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        baseMapper.updateById(bean);
        //日志
//        AfOrder orderBean = afOrderMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        AfOrder orderBean = null;
        if ("AE".equals(businessScope) || "AI".equals(businessScope)) {
            orderBean = afOrderMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        } else if ("SE".equals(businessScope) || "SI".equals(businessScope)) {
            orderBean = afOrderMapper.getSEOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        } else if (businessScope.startsWith("T")) {
            orderBean = afOrderMapper.getTCOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        } else if ("LC".equals(businessScope)) {
            orderBean = afOrderMapper.getLCOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        } else if ("IO".equals(businessScope)) {
            orderBean = afOrderMapper.getIOOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        }
        LogBean logBean = new LogBean();
        logBean.setPageName("费用录入");
        logBean.setPageFunction("制作账单");

        logBean.setLogRemark("账单号：" + bean.getDebitNoteNum() + "加入新费用");
        logBean.setBusinessScope(businessScope);
        logBean.setOrderNumber(orderBean.getOrderCode());
        logBean.setOrderId(orderBean.getOrderId());
        logBean.setOrderUuid(orderBean.getOrderUuid());
        bean.setIncomeIds("'" + bean.getIncomeIds().replaceAll(",", "','") + "'");
        if ("AE".equals(businessScope) || "AI".equals(businessScope)) {
            //rowuuid
            String incomeIds[] = incomeIdsStr.split(",");
            String incomeRowUuids[] = bean.getIncomeRowUuids().split(",");
            for (int i = 0; i < incomeIds.length; i++) {
                AfIncome bean2 = afIncomeMapper.selectById(incomeIds[i]);
                if (!incomeRowUuids[i].equals(bean2.getRowUuid())) {
                    throw new RuntimeException("账单下收入明细不是最新数据，请刷新页面重新操作");
                }
            }
            //日志
            logService.saveLog(logBean);
            baseMapper.updateIncome(SecurityUtils.getUser().getOrgId(), bean.getIncomeIds(), bean.getDebitNoteId(), bean.getDebitNoteDate(), UUID.randomUUID().toString());
        } else if ("SE".equals(businessScope) || "SI".equals(businessScope)) {
            //rowuuid
            String incomeIds[] = incomeIdsStr.split(",");
            String incomeRowUuids[] = bean.getIncomeRowUuids().split(",");
            for (int i = 0; i < incomeIds.length; i++) {
                ScIncome bean2 = scIncomeMapper.selectById(incomeIds[i]);
                if (!incomeRowUuids[i].equals(bean2.getRowUuid())) {
                    throw new RuntimeException("账单下收入明细不是最新数据，请刷新页面重新操作");
                }
            }
            //日志
            ScLog logBean2 = new ScLog();
            BeanUtils.copyProperties(logBean, logBean2);
            scLogService.saveLog(logBean2);
            baseMapper.updateIncomeSE(SecurityUtils.getUser().getOrgId(), bean.getIncomeIds(), bean.getDebitNoteId(), bean.getDebitNoteDate(), UUID.randomUUID().toString());
        } else if (businessScope.startsWith("T")) {
            //rowuuid
            String incomeIds[] = incomeIdsStr.split(",");
            String incomeRowUuids[] = bean.getIncomeRowUuids().split(",");
            for (int i = 0; i < incomeIds.length; i++) {
                TcIncome bean2 = tcIncomeMapper.selectById(incomeIds[i]);
                if (!incomeRowUuids[i].equals(bean2.getRowUuid())) {
                    throw new RuntimeException("账单下收入明细不是最新数据，请刷新页面重新操作");
                }
            }
            //日志
            TcLog logBean2 = new TcLog();
            BeanUtils.copyProperties(logBean, logBean2);
            logBean2.setCreatorId(SecurityUtils.getUser().getId());
            logBean2.setCreatorName(SecurityUtils.getUser().buildOptName());
            logBean2.setCreatTime(LocalDateTime.now());
            logBean2.setOrgId(SecurityUtils.getUser().getOrgId());
            tcLogService.save(logBean2);
            baseMapper.updateIncomeTC(SecurityUtils.getUser().getOrgId(), bean.getIncomeIds(), bean.getDebitNoteId(), bean.getDebitNoteDate(), UUID.randomUUID().toString());
        } else if ("LC".equals(businessScope)) {
            String incomeIds[] = incomeIdsStr.split(",");
            String incomeRowUuids[] = bean.getIncomeRowUuids().split(",");
            for (int i = 0; i < incomeIds.length; i++) {
                LcIncome bean2 = remoteServiceToSC.viewLcIncome(Integer.valueOf(incomeIds[i])).getData();
                if (!incomeRowUuids[i].equals(bean2.getRowUuid())) {
                    throw new RuntimeException("账单下收入明细不是最新数据，请刷新页面重新操作");
                }
            }
            //日志
            LcLog logBean2 = new LcLog();
            BeanUtils.copyProperties(logBean, logBean2);
            logBean2.setCreatorId(SecurityUtils.getUser().getId());
            logBean2.setCreatorName(SecurityUtils.getUser().buildOptName());
            logBean2.setCreatTime(LocalDateTime.now());
            logBean2.setOrgId(SecurityUtils.getUser().getOrgId());
            lcLogService.save(logBean2);
            baseMapper.updateIncomeLC(SecurityUtils.getUser().getOrgId(), bean.getIncomeIds(), bean.getDebitNoteId(), bean.getDebitNoteDate(), UUID.randomUUID().toString());
        } else if ("IO".equals(businessScope)) {
            String incomeIds[] = incomeIdsStr.split(",");
            String incomeRowUuids[] = bean.getIncomeRowUuids().split(",");
            for (int i = 0; i < incomeIds.length; i++) {
                IoIncome bean2 = remoteServiceToSC.viewIoIncome(Integer.valueOf(incomeIds[i])).getData();
                if (!incomeRowUuids[i].equals(bean2.getRowUuid())) {
                    throw new RuntimeException("账单下收入明细不是最新数据，请刷新页面重新操作");
                }
            }
            //日志
            IoLog logBean2 = new IoLog();
            BeanUtils.copyProperties(logBean, logBean2);
            logBean2.setCreatorId(SecurityUtils.getUser().getId());
            logBean2.setCreatorName(SecurityUtils.getUser().buildOptName());
            logBean2.setCreatTime(LocalDateTime.now());
            logBean2.setOrgId(SecurityUtils.getUser().getOrgId());
            ioLogService.save(logBean2);
            baseMapper.updateIncomeIO(SecurityUtils.getUser().getOrgId(), bean.getIncomeIds(), bean.getDebitNoteId(), bean.getDebitNoteDate(), UUID.randomUUID().toString());
        }

        //先删除再增加
        debitMapper.deleteByDebitId(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteId());
        for (int i = 0; i < bean.getDebitCurrencyList().size(); i++) {
            CssDebitNoteCurrency debitBean = bean.getDebitCurrencyList().get(i);
            debitBean.setDebitNoteId(bean.getDebitNoteId());
            debitBean.setOrgId(SecurityUtils.getUser().getOrgId());
            debitMapper.insert(debitBean);

            //汇率同步
            if ("AE".equals(businessScope) || "AI".equals(businessScope)) {
                baseMapper.updateIncome2(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteId(), debitBean.getCurrency(), debitBean.getExchangeRate());
            } else if ("SE".equals(businessScope) || "SI".equals(businessScope)) {
                baseMapper.updateIncomeSE2(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteId(), debitBean.getCurrency(), debitBean.getExchangeRate());
            } else if ("TE".equals(businessScope) || "TI".equals(businessScope)) {
                baseMapper.updateIncomeTC2(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteId(), debitBean.getCurrency(), debitBean.getExchangeRate());
            } else if ("LC".equals(businessScope)) {
                baseMapper.updateIncomeLC2(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteId(), debitBean.getCurrency(), debitBean.getExchangeRate());
            } else if ("IO".equals(businessScope)) {
                baseMapper.updateIncomeIO2(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteId(), debitBean.getCurrency(), debitBean.getExchangeRate());
            }
        }
        //日志
//		logMapper.updateLog(SecurityUtils.getUser().getOrgId(),bean.getOrderUuid(),"财务锁账",SecurityUtils.getUser().getId(),
//				SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail(),LocalDateTime.now());
//		baseMapper.updateOder(SecurityUtils.getUser().getOrgId(),bean.getOrderId());
        //更新订单应收状态：（order. income_status）
		List<Map> listMap = detailMapper.getOrderIncomeStatus(SecurityUtils.getUser().getOrgId(), orderBean.getOrderId().toString(),businessScope);
		if(listMap!=null&&listMap.size()>0) {
			for(Map map:listMap) {
				detailMapper.updateOrderIncomeStatus(Integer.valueOf(map.get("org_id").toString()),Integer.valueOf(map.get("order_id").toString()),map.get("income_status").toString(),UUID.randomUUID().toString(),businessScope);
			}
		}
        return true;
    }

    @Override
    public List<CssDebitNote> queryHavedBill(CssDebitNote bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        return baseMapper.queryHavedBill(bean);
    }

    @Override
    public List<AfIncome> queryHavedBillDetail(AfIncome bean) {
        String businessScope = bean.getBusinessScope();
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        List<AfIncome> list = new ArrayList<AfIncome>();
        if ("AE".equals(businessScope) || "AI".equals(businessScope)) {
            list = baseMapper.queryHavedBillDetail(bean);
        } else if ("SE".equals(businessScope) || "SI".equals(businessScope)) {
            list = baseMapper.queryHavedSEBillDetail(bean);
        } else if (businessScope.startsWith("T")) {
            list = baseMapper.queryHavedTCBillDetail(bean);
        } else if (businessScope.startsWith("L")) {
            list = baseMapper.queryHavedLCBillDetail(bean);
        } else if (businessScope.equals("IO")) {
            list = baseMapper.queryHavedIOBillDetail(bean);
        }
        return list;
    }

    private String getOrderCode(String code) {
        Date dt = new Date();

        String year = String.format("%ty", dt);

        String mon = String.format("%tm", dt);

        String day = String.format("%td", dt);
        return code + "-DN-" + year + mon + day;
    }
}
