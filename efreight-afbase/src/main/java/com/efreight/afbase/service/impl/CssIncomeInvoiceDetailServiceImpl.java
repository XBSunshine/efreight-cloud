package com.efreight.afbase.service.impl;

import com.efreight.afbase.entity.CssDebitNote;
import com.efreight.afbase.entity.CssDebitNoteCurrency;
import com.efreight.afbase.entity.CssIncomeInvoice;
import com.efreight.afbase.entity.CssIncomeInvoiceDetail;
import com.efreight.afbase.entity.CssIncomeInvoiceDetailWriteoff;
import com.efreight.afbase.entity.DebitNote;
import com.efreight.afbase.entity.Statement;
import com.efreight.afbase.entity.StatementCurrency;
import com.efreight.afbase.dao.CssDebitNoteCurrencyMapper;
import com.efreight.afbase.dao.CssIncomeInvoiceDetailMapper;
import com.efreight.afbase.dao.CssIncomeInvoiceDetailWriteoffMapper;
import com.efreight.afbase.dao.CssIncomeInvoiceMapper;
import com.efreight.afbase.dao.DebitNoteMapper;
import com.efreight.afbase.dao.StatementCurrencyMapper;
import com.efreight.afbase.dao.StatementMapper;
import com.efreight.afbase.service.CssIncomeInvoiceDetailService;
import com.efreight.afbase.service.CssIncomeInvoiceDetailWriteoffService;
import com.efreight.afbase.service.DebitNoteService;
import com.efreight.afbase.service.StatementService;
import com.efreight.common.security.util.SecurityUtils;

import lombok.AllArgsConstructor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * CSS 应收：发票明细表 服务实现类
 * </p>
 *
 * @author cwd
 * @since 2020-12-28
 */
@Service
@AllArgsConstructor
public class CssIncomeInvoiceDetailServiceImpl extends ServiceImpl<CssIncomeInvoiceDetailMapper, CssIncomeInvoiceDetail> implements CssIncomeInvoiceDetailService {
    private final CssIncomeInvoiceMapper cssIncomeInvoiceMapper;
    private final DebitNoteMapper debitNoteMapper;
    private final StatementMapper statementMapper;
    private final CssDebitNoteCurrencyMapper cssDebitNoteCurrencyMapper;
    private final StatementCurrencyMapper statementCurrencyMapper;
    private final CssIncomeInvoiceDetailWriteoffMapper cssIncomeInvoiceDetailWriteoffMapper;
    private final CssIncomeInvoiceDetailWriteoffService cssIncomeInvoiceDetailWriteoffService;
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean doSave(CssIncomeInvoiceDetail bean) {
		String rowUuid = UUID.randomUUID().toString();
		//校验开票申请表rowid是否变化
		CssIncomeInvoice cssIncomeInvoice = cssIncomeInvoiceMapper.selectById(bean.getInvoiceId());
		if(cssIncomeInvoice==null) {
			throw new RuntimeException("发票申请数据有变更");
		}
		if(!bean.getRowUuid().equals(cssIncomeInvoice.getRowUuid())) {
			//row  有变化
			throw new RuntimeException("发票申请数据有变更");
		}
		if(cssIncomeInvoice.getDebitNoteId()!=null) {
			CssDebitNoteCurrency cdc = cssDebitNoteCurrencyMapper.queryCssDebitNoteCurrency(SecurityUtils.getUser().getOrgId(),cssIncomeInvoice.getDebitNoteId(),bean.getCurrency());
			if(cdc.getAmountInvoice()!=null&&cdc.getAmount().compareTo(cdc.getAmountInvoice())==0) {
				throw new RuntimeException("该币种已开票完毕，请勿重复开票");
			}
		}else {
			StatementCurrency sc = statementCurrencyMapper.queryStatementCurrencyCurrency(SecurityUtils.getUser().getOrgId(),cssIncomeInvoice.getStatementId(),bean.getCurrency());
			if(sc.getAmountInvoice()!=null&&sc.getAmount().compareTo(sc.getAmountInvoice())==0) {
				throw new RuntimeException("该币种已开票完毕，请勿重复开票");
			}
		}
		//插入开票明细表
		bean.setDebitNoteId(cssIncomeInvoice.getDebitNoteId());
		bean.setStatementId(cssIncomeInvoice.getStatementId());
		bean.setBusinessScope(cssIncomeInvoice.getBusinessScope());
		bean.setInvoiceId(cssIncomeInvoice.getInvoiceId());
		bean.setRowUuid(rowUuid);
		bean.setCreatorId(SecurityUtils.getUser().getId());
		bean.setCreateTime(LocalDateTime.now());
		bean.setCreatorName(SecurityUtils.getUser().buildOptName());
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		baseMapper.insert(bean);
		//更新逻辑
		this.updateAll(bean);
		//是否自动核销
		if(bean.getIsAutoWriteoff()!=null&&bean.getIsAutoWriteoff()==2) {
			this.writeoff(bean);
		}
		return true;
	}
	
	
	/**
	 * 收入对账：开票自动核销
	 * @param bean
	 * @return
	 */
	public boolean writeoff(CssIncomeInvoiceDetail bean) {
		CssIncomeInvoiceDetailWriteoff detailWriteoff = new CssIncomeInvoiceDetailWriteoff();
		//插入：开票核销表（css_income_invoice_detail_writeoff）；
		//生成核销号
        String code = getCode(bean.getBusinessScope());
        List<CssIncomeInvoiceDetailWriteoff> codeList = cssIncomeInvoiceDetailWriteoffMapper.selectCode(SecurityUtils.getUser().getOrgId(), code);
        if (codeList.size() == 0) {
    	  detailWriteoff.setWriteoffNum(code + "0001");
        } else {
          if ((code + "9999").equals(codeList.get(0).getWriteoffNum())) {
              throw new RuntimeException("每天最多可以核销9999个" + bean.getBusinessScope() + "核销单");
          } else {
              String str = codeList.get(0).getWriteoffNum();
              str = str.substring(str.length() - 4);
              detailWriteoff.setWriteoffNum(code + String.format("%04d", Integer.parseInt(str) + 1));
          }

        }
        detailWriteoff.setCustomerName(bean.getCustomerName());
		detailWriteoff.setCustomerId(bean.getCustomerId());
		detailWriteoff.setInvoiceNum(bean.getInvoiceNum());
		detailWriteoff.setFinancialAccountName(bean.getFinancialAccountName());
		detailWriteoff.setFinancialAccountCode(bean.getFinancialAccountCode());
		detailWriteoff.setFinancialAccountType(bean.getFinancialAccountType());
		detailWriteoff.setCurrency(bean.getCurrency());
		detailWriteoff.setInvoiceDetailId(bean.getInvoiceDetailId());
		detailWriteoff.setAmount(bean.getAmount());
		detailWriteoff.setAmountWriteoff(bean.getAmount());
		detailWriteoff.setWriteoffDate(bean.getWriteoffDate());
		detailWriteoff.setWriteoffRemark(bean.getWriteoffRemark());
        detailWriteoff.setOrgId(SecurityUtils.getUser().getOrgId());
        detailWriteoff.setBusinessScope(bean.getBusinessScope());
        detailWriteoff.setDebitNoteId(bean.getDebitNoteId());
        detailWriteoff.setStatementId(bean.getStatementId());
        detailWriteoff.setInvoiceId(bean.getInvoiceId());
        detailWriteoff.setCreateTime(LocalDateTime.now());
        detailWriteoff.setCreatorId(SecurityUtils.getUser().getId());
        detailWriteoff.setCreatorName(SecurityUtils.getUser().buildOptName());
        detailWriteoff.setRowUuid(UUID.randomUUID().toString());
        cssIncomeInvoiceDetailWriteoffMapper.insert(detailWriteoff);
        //更新
        cssIncomeInvoiceDetailWriteoffService.updateAll(detailWriteoff);
        return true;
	}
	
	/**
	 * 收入对账：开票、删除开票 后更新逻辑
	 * @param bean
	 * @return
	 */
	public boolean updateAll(CssIncomeInvoiceDetail bean) {
		String rowUuid = UUID.randomUUID().toString();
		String orderId = null;
		BigDecimal amount = BigDecimal.ZERO;
		BigDecimal amountInvoice = BigDecimal.ZERO;
		
		//查询开票申请信息
		CssIncomeInvoice cssIncomeInvoice = cssIncomeInvoiceMapper.selectById(bean.getInvoiceId());
		
		//根据开票申请查询对应币种的开票明细信息
		LambdaQueryWrapper<CssIncomeInvoiceDetail> cssIncomeInvoiceDetailWrapper = Wrappers.<CssIncomeInvoiceDetail>lambdaQuery();
		cssIncomeInvoiceDetailWrapper.eq(CssIncomeInvoiceDetail::getInvoiceId, bean.getInvoiceId()).eq(CssIncomeInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId());
		cssIncomeInvoiceDetailWrapper.eq(CssIncomeInvoiceDetail::getCurrency, bean.getCurrency());
		List<CssIncomeInvoiceDetail> listTwo = baseMapper.selectList(cssIncomeInvoiceDetailWrapper);
		
		if(bean.getDebitNoteId()!=null) {
			//更新：清单/账单 表rowid
			DebitNote dn =  debitNoteMapper.selectById(bean.getDebitNoteId());
			orderId = dn.getOrderId().toString();
			dn.setRowUuid(rowUuid);
			debitNoteMapper.updateById(dn);
			//更新：对应账单/清单 币种表，对应币种的已开票金额
			CssDebitNoteCurrency cdc = cssDebitNoteCurrencyMapper.queryCssDebitNoteCurrency(SecurityUtils.getUser().getOrgId(),bean.getDebitNoteId(),bean.getCurrency());
            if(cdc!=null) {
            	if(listTwo!=null&&listTwo.size()>0) {
            		cdc.setAmountInvoice(null);
            		for(CssIncomeInvoiceDetail ciid:listTwo) {
            			if(cdc.getAmountInvoice()!=null) {
            				cdc.setAmountInvoice(cdc.getAmountInvoice().add(ciid.getAmount()));
            			}else {
            				cdc.setAmountInvoice(ciid.getAmount());
            			}
            		}
            	}else {
            		cdc.setAmountInvoice(null);
            	}
            	cssDebitNoteCurrencyMapper.updateById(cdc);
            }
            //更新：开票申请表 的 发票状态 invoice_status 数据准备
            List<CssDebitNoteCurrency> list = cssDebitNoteCurrencyMapper.queryBill2(SecurityUtils.getUser().getOrgId(),bean.getDebitNoteId().toString());
            if(list!=null&&list.size()>0) {
            	for(CssDebitNoteCurrency a:list) {
            		amount = amount.add(a.getAmount());
            		amountInvoice = amountInvoice.add(a.getAmountInvoice()!=null?a.getAmountInvoice():BigDecimal.ZERO);
            	}
            }
		}else {
			//更新：清单/账单 表rowid
			Statement s = statementMapper.selectById(bean.getStatementId());
			s.setRowUuid(rowUuid);
			statementMapper.updateById(s);
			List<DebitNote> listDn = debitNoteMapper.queryDebitNoteListByWhere(SecurityUtils.getUser().getOrgId(),s.getStatementId());
			if(listDn!=null&&listDn.size()>0) {
				for(DebitNote dn:listDn) {
					if(orderId!=null) {
						orderId = orderId+","+dn.getOrderId().toString();
					}else {
						orderId = dn.getOrderId().toString();
					}
				}
			}
			//更新：对应账单/清单 币种表，对应币种的已开票金额
			StatementCurrency sc = statementCurrencyMapper.queryStatementCurrencyCurrency(SecurityUtils.getUser().getOrgId(),bean.getStatementId(),bean.getCurrency());
		    if(sc!=null) {
		    	if(listTwo!=null&&listTwo.size()>0) {
		    		sc.setAmountInvoice(null);
            		for(CssIncomeInvoiceDetail ciid:listTwo) {
            			if(sc.getAmountInvoice()!=null) {
            				sc.setAmountInvoice(sc.getAmountInvoice().add(ciid.getAmount()));
            			}else {
            				sc.setAmountInvoice(ciid.getAmount());
            			}
            		}
            	}else {
            		sc.setAmountInvoice(null);
            	}
		    	statementCurrencyMapper.updateById(sc);
		    }
			
			List<StatementCurrency> list = statementCurrencyMapper.queryBillCurrency(SecurityUtils.getUser().getOrgId(),bean.getStatementId());
			if(list!=null&&list.size()>0) {
				for(StatementCurrency a:list) {
					amount = amount.add(a.getAmount());
            		amountInvoice = amountInvoice.add(a.getAmountInvoice()!=null?a.getAmountInvoice():BigDecimal.ZERO);
				}
			}
		}
		
		//更新：开票申请 表rowid
		cssIncomeInvoice.setRowUuid(rowUuid);
		
		//更新：开票申请表 的 发票状态 invoice_status 
		if(amount.compareTo(amountInvoice)==0) {
			cssIncomeInvoice.setInvoiceStatus(1);
		}else if(amountInvoice.compareTo(BigDecimal.ZERO)!=0) {
			cssIncomeInvoice.setInvoiceStatus(0);
		}else {
			cssIncomeInvoice.setInvoiceStatus(-1);
		}
		cssIncomeInvoiceMapper.updateById(cssIncomeInvoice);
		
		//更新订单应收状态：（order. income_status）
		List<Map> listMap = baseMapper.getOrderIncomeStatus(SecurityUtils.getUser().getOrgId(), orderId,bean.getBusinessScope());
		if(listMap!=null&&listMap.size()>0) {
			for(Map map:listMap) {
				baseMapper.updateOrderIncomeStatus(Integer.valueOf(map.get("org_id").toString()),Integer.valueOf(map.get("order_id").toString()),map.get("income_status").toString(),rowUuid,bean.getBusinessScope());
			}
		}
		 
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteInvoiceDetail(Integer invoiceDetailId,String rowUuid) {
		//开票明细表. writeoff_complete = NULL （即，发票未核销 才可删除）
		CssIncomeInvoiceDetail detail = baseMapper.selectById(invoiceDetailId);
		if(detail==null) {
			throw new RuntimeException("您好,该发票申请单未开票 不允许 删除");
		}
		if(detail.getWriteoffComplete()!=null) {
			throw new RuntimeException("您好，发票号 "+detail.getInvoiceNum()+"，已核销 或 未开票 不允许 删除。");
		}
		//删除时 检查 开票申请表 Rowid 是否改变
		CssIncomeInvoice invoice = cssIncomeInvoiceMapper.selectById(detail.getInvoiceId());
		if(!rowUuid.equals(invoice.getRowUuid())) {
			throw new RuntimeException("开票申请信息有变更 不可删除");
		}
		baseMapper.deleteById(detail);
		//更新逻辑
		this.updateAll(detail);
		return true;
	}

	@Override
	public CssIncomeInvoiceDetail invoiceDetailInfo(Integer invoiceDetailId) {
		return baseMapper.selectById(invoiceDetailId);
	}
	private String getCode(String businessScope) {
        Date dt = new Date();

        String year = String.format("%ty", dt);

        String mon = String.format("%tm", dt);

        String day = String.format("%td", dt);
        return businessScope + "-RW-" + year + mon + day;
    }

}
