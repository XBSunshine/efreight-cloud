package com.efreight.afbase.service.impl;

import com.efreight.afbase.entity.CssDebitNoteCurrency;
import com.efreight.afbase.entity.CssIncomeFiles;
import com.efreight.afbase.entity.CssIncomeInvoice;
import com.efreight.afbase.entity.CssIncomeInvoiceDetail;
import com.efreight.afbase.entity.CssIncomeInvoiceDetailWriteoff;
import com.efreight.afbase.entity.DebitNote;
import com.efreight.afbase.entity.Statement;
import com.efreight.afbase.entity.StatementCurrency;
import com.efreight.afbase.dao.CssCostWriteoffMapper;
import com.efreight.afbase.dao.CssDebitNoteCurrencyMapper;
import com.efreight.afbase.dao.CssIncomeFilesMapper;
import com.efreight.afbase.dao.CssIncomeInvoiceDetailMapper;
import com.efreight.afbase.dao.CssIncomeInvoiceMapper;
import com.efreight.afbase.dao.DebitNoteMapper;
import com.efreight.afbase.dao.StatementCurrencyMapper;
import com.efreight.afbase.service.AfCostService;
import com.efreight.afbase.service.AfOrderService;
import com.efreight.afbase.service.CssIncomeInvoiceService;
import com.efreight.afbase.service.CssPaymentDetailService;
import com.efreight.afbase.service.DebitNoteService;
import com.efreight.afbase.service.IoCostService;
import com.efreight.afbase.service.IoOrderService;
import com.efreight.afbase.service.LcCostService;
import com.efreight.afbase.service.LcOrderService;
import com.efreight.afbase.service.ScCostService;
import com.efreight.afbase.service.ScOrderService;
import com.efreight.afbase.service.ServiceService;
import com.efreight.afbase.service.StatementService;
import com.efreight.afbase.service.TcCostService;
import com.efreight.afbase.service.TcOrderService;
import com.efreight.afbase.utils.FieldValUtils;
import com.efreight.afbase.utils.LoginUtils;
import com.efreight.common.core.feign.RemoteCoopService;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.security.util.SecurityUtils;


import lombok.AllArgsConstructor;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * CSS 应收：发票申请表 服务实现类
 * </p>
 *
 * @author cwd
 * @since 2020-12-28
 */
@Service
@AllArgsConstructor
public class CssIncomeInvoiceServiceImpl extends ServiceImpl<CssIncomeInvoiceMapper, CssIncomeInvoice> implements CssIncomeInvoiceService {
      private final DebitNoteService debitNoteService;
      private final StatementService statementService;
      private final CssDebitNoteCurrencyMapper cssDebitNoteCurrencyMapper;
      private final CssIncomeInvoiceDetailMapper cssIncomeInvoiceDetailMapper;
      private final StatementCurrencyMapper statementCurrencyMapper;
      private final DebitNoteMapper debitNoteMapper;
      private final CssIncomeInvoiceDetailMapper detailMapper;
      private final CssIncomeFilesMapper filesMapper;
      
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean doSave(CssIncomeInvoice bean) {
		String rowUuid = UUID.randomUUID().toString();
		String orderIds = null;
		String bs = null;
		if("debitNote".equals(bean.getType())) {
			//账单
			bean.setStatementId(null);
			//查询账单或者清单ID
			DebitNote dn = debitNoteService.getById(bean.getDebitNoteId());
			if(dn==null) {
				throw new RuntimeException("账单不存在");
			}
			orderIds = dn.getOrderId().toString();
			bs = dn.getBusinessScope();
			if(dn.getStatementId()!=null) {
				throw new RuntimeException("您好，账单号"+dn.getDebitNoteNum()+" 已做开票申请 或 已开票");
			}
			LambdaQueryWrapper<CssIncomeInvoice> cssIncomeInvoiceWrapper = Wrappers.<CssIncomeInvoice>lambdaQuery();
			cssIncomeInvoiceWrapper.eq(CssIncomeInvoice::getDebitNoteId, dn.getDebitNoteId()).eq(CssIncomeInvoice::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssIncomeInvoice::getBusinessScope,dn.getBusinessScope());
			List<CssIncomeInvoice> list = baseMapper.selectList(cssIncomeInvoiceWrapper);
			if(list!=null&&list.size()>0) {
				throw new RuntimeException("您好，账单号"+dn.getDebitNoteNum()+" 已做开票申请 或 已开票");
			}
			if(!dn.getRowUuid().equals(bean.getCheckRowUuid())) {
				//rowUuid 不一致  
				throw new RuntimeException("账单数据有变更");
			}
			
			dn.setRowUuid(rowUuid);
			bean.setBusinessScope(dn.getBusinessScope());
			bean.setCustomerId(Integer.valueOf(dn.getCustomerId()).intValue());
			bean.setCustomerName(dn.getCustomerName());
			debitNoteService.updateById(dn);
		}else {
			//清单
			bean.setDebitNoteId(null);
			Statement st = statementService.getById(bean.getStatementId());
			if(st==null) {
				throw new RuntimeException("账单不存在");
			}
			bs = st.getBusinessScope();
			List<DebitNote> listDebit = debitNoteMapper.queryDebitNoteListByWhere(SecurityUtils.getUser().getOrgId(),bean.getStatementId());
			for(DebitNote dn:listDebit) {
				if(orderIds!=null) {
					orderIds = orderIds+","+dn.getOrderId().toString();
				}else {
					orderIds = dn.getOrderId().toString();
				}
			}
			
			LambdaQueryWrapper<CssIncomeInvoice> cssIncomeInvoiceWrapper = Wrappers.<CssIncomeInvoice>lambdaQuery();
			cssIncomeInvoiceWrapper.eq(CssIncomeInvoice::getStatementId, bean.getStatementId()).eq(CssIncomeInvoice::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssIncomeInvoice::getBusinessScope,st.getBusinessScope());
			List<CssIncomeInvoice> list = baseMapper.selectList(cssIncomeInvoiceWrapper);
			if(list!=null&&list.size()>0) {
				throw new RuntimeException("您好，清单号"+st.getStatementNum()+" 已做开票申请 或 已开票");
			}
			if(!st.getRowUuid().equals(bean.getCheckRowUuid())) {
				//rowUuid 不一致  
				throw new RuntimeException("清单数据有变更");
			}
			st.setRowUuid(rowUuid);
			bean.setBusinessScope(st.getBusinessScope());
			bean.setCustomerId(Integer.valueOf(st.getCustomerId()).intValue());
			bean.setCustomerName(st.getCustomerName());
			statementService.updateById(st);
		}
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		bean.setInvoiceStatus(-1);
		bean.setCreatorId(SecurityUtils.getUser().getId());
		bean.setCreateTime(LocalDateTime.now());
		bean.setCreatorName(SecurityUtils.getUser().buildOptName());
		bean.setRowUuid(rowUuid);
		baseMapper.insert(bean);
		//更新订单应收状态：（order. income_status）
		List<Map> listMap = detailMapper.getOrderIncomeStatus(SecurityUtils.getUser().getOrgId(), orderIds,bs);
		if(listMap!=null&&listMap.size()>0) {
			for(Map map:listMap) {
				detailMapper.updateOrderIncomeStatus(Integer.valueOf(map.get("org_id").toString()),Integer.valueOf(map.get("order_id").toString()),map.get("income_status").toString(),UUID.randomUUID().toString(),bs);
			}
		}
		return true;
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean cancelDNInvoice(Integer debitNoteId,String rowUuid) {
		//根据账单ID 查询
		DebitNote dn = debitNoteService.getById(debitNoteId);
		if(dn==null) {
			throw new RuntimeException("该账单不存在");
		}
		if(dn!=null&&dn.getStatementId()!=null) {
			throw new RuntimeException("该账单已做清单");
		}
		if(!rowUuid.equals(dn.getRowUuid())) {
			throw new RuntimeException("账单数据有变更");
		}
		//根据账单ID 查询 发票申请信息
		LambdaQueryWrapper<CssIncomeInvoice> cssIncomeInvoiceWrapper = Wrappers.<CssIncomeInvoice>lambdaQuery();
		cssIncomeInvoiceWrapper.eq(CssIncomeInvoice::getDebitNoteId, dn.getDebitNoteId()).eq(CssIncomeInvoice::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssIncomeInvoice::getBusinessScope,dn.getBusinessScope());
		CssIncomeInvoice bean = baseMapper.selectOne(cssIncomeInvoiceWrapper);
		if(bean==null) {
			throw new RuntimeException("账单发票申请数据有变更");
		}
		if(bean.getInvoiceStatus()!=-1) {
			throw new RuntimeException("您好，未开发票的账单 才可 撤销开票申请。");
		}
		if(bean!=null) {
			String r = UUID.randomUUID().toString();
			baseMapper.deleteById(bean.getInvoiceId());
			dn.setRowUuid(r);
			debitNoteService.updateById(dn);
		}
		//更新订单应收状态：（order. income_status）
		List<Map> listMap = detailMapper.getOrderIncomeStatus(SecurityUtils.getUser().getOrgId(), dn.getOrderId().toString(),dn.getBusinessScope());
		if(listMap!=null&&listMap.size()>0) {
			for(Map map:listMap) {
				detailMapper.updateOrderIncomeStatus(Integer.valueOf(map.get("org_id").toString()),Integer.valueOf(map.get("order_id").toString()),map.get("income_status").toString(),UUID.randomUUID().toString(),dn.getBusinessScope());
			}
		}
		return true;
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean cancelSTInvoice(Integer statementId,String rowUuid) {
		//根据清单ID 查询
		Statement st = statementService.getById(statementId);
		if(st==null) {
			throw new RuntimeException("该清单不存在");
		}
		if(!rowUuid.equals(st.getRowUuid())) {
			throw new RuntimeException("清单数据有变更");
		}
		//根据账单ID 查询 发票申请信息
		LambdaQueryWrapper<CssIncomeInvoice> cssIncomeInvoiceWrapper = Wrappers.<CssIncomeInvoice>lambdaQuery();
		cssIncomeInvoiceWrapper.eq(CssIncomeInvoice::getStatementId, st.getStatementId()).eq(CssIncomeInvoice::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssIncomeInvoice::getBusinessScope,st.getBusinessScope());
		CssIncomeInvoice bean = baseMapper.selectOne(cssIncomeInvoiceWrapper);
		if(bean==null) {
			throw new RuntimeException("清单发票申请数据有变更,请刷新数据后操作");
		}
		if(bean.getInvoiceStatus()!=-1) {
			throw new RuntimeException("您好，未开发票的清单 才可 撤销开票申请。");
		}
		if(bean!=null) {
			String r = UUID.randomUUID().toString();
			baseMapper.deleteById(bean.getInvoiceId());
			st.setRowUuid(r);
			statementService.updateById(st);
		}
		String orderIds = null;
		String bs = st.getBusinessScope();
		List<DebitNote> listDebit = debitNoteMapper.queryDebitNoteListByWhere(SecurityUtils.getUser().getOrgId(),statementId);
		for(DebitNote dn:listDebit) {
			if(orderIds!=null) {
				orderIds = orderIds+","+dn.getOrderId().toString();
			}else {
				orderIds = dn.getOrderId().toString();
			}
		}
		//更新订单应收状态：（order. income_status）
		List<Map> listMap = detailMapper.getOrderIncomeStatus(SecurityUtils.getUser().getOrgId(), orderIds,bs);
		if(listMap!=null&&listMap.size()>0) {
			for(Map map:listMap) {
				detailMapper.updateOrderIncomeStatus(Integer.valueOf(map.get("org_id").toString()),Integer.valueOf(map.get("order_id").toString()),map.get("income_status").toString(),UUID.randomUUID().toString(),bs);
			}
		}
		return true;
	}


	@Override
	public IPage getPage(Page page, CssIncomeInvoice bean) {
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		IPage<CssIncomeInvoice> iPage = baseMapper.getPage(page, bean);
		iPage.getRecords().stream().forEach(record -> {
			//账单/清单金额
			StringBuffer buffer = new StringBuffer("");
			StringBuffer buffer2 = new StringBuffer("");
			if(record.getDebitNoteId()!=null) {
				//账单金额实现多币种显示
	            LambdaQueryWrapper<CssDebitNoteCurrency> cssDebitCurrencyWrapper = Wrappers.<CssDebitNoteCurrency>lambdaQuery();
	            cssDebitCurrencyWrapper.eq(CssDebitNoteCurrency::getDebitNoteId, record.getDebitNoteId()).eq(CssDebitNoteCurrency::getOrgId, SecurityUtils.getUser().getOrgId());
	            List<CssDebitNoteCurrency> currencyList = cssDebitNoteCurrencyMapper.selectList(cssDebitCurrencyWrapper);
	            if(currencyList!=null&&currencyList.size()>0) {
	            currencyList.stream().forEach(currency -> {
	            	buffer.append(new DecimalFormat("###,###,###.00").format(currency.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));
	            	buffer.append(" (").append(currency.getCurrency()).append(")  ");
	            	if(currency.getCurrency().equals(record.getCurrency())) {
	            		buffer2.append(new DecimalFormat("###,###,###.00").format(currency.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP))).append(" (").append(currency.getCurrency()).append(")  ");
	            	}
	            });
	          }
			}else {
			    LambdaQueryWrapper<StatementCurrency> statementCurrencyWrapper = Wrappers.<StatementCurrency>lambdaQuery();
	            statementCurrencyWrapper.eq(StatementCurrency::getStatementId, record.getStatementId()).eq(StatementCurrency::getOrgId, SecurityUtils.getUser().getOrgId());
	            List<StatementCurrency> currencyList = statementCurrencyMapper.selectList(statementCurrencyWrapper);
	            if(currencyList!=null&&currencyList.size()>0) {
	            	currencyList.stream().forEach(currency -> {
	            		buffer.append(new DecimalFormat("###,###,###.00").format(currency.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));
	            		buffer.append(" (").append(currency.getCurrency()).append(")  ");
	            		if(currency.getCurrency().equals(record.getCurrency())) {
	            			buffer2.append(new DecimalFormat("###,###,###.00").format(currency.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP))).append(" (").append(currency.getCurrency()).append(")  ");
	            		}
	            	});
	            }
			}
			if(record.getWriteoffComplete()!=null&&record.getWriteoffComplete()==1) {
				record.setInvoiceStatusStr("核销完毕");
			}else if(record.getWriteoffComplete()!=null&&record.getWriteoffComplete()==0) {
				record.setInvoiceStatusStr("部分核销");
			}else if(record.getInvoiceStatus()==0&&record.getBusinessWriteoffComplete()==null) {
				record.setInvoiceStatusStr("部分开票");
			}else if(record.getInvoiceStatus()==1&&record.getBusinessWriteoffComplete()==null) {
				record.setInvoiceStatusStr("开票完毕");
			}else {
				//invoice_status = -1 
				record.setInvoiceStatusStr("待开票");
			}
			//待开票 显示全部
			if(record.getInvoiceStatus()==-1) {
				record.setBusniessAmount(buffer.toString());
			}else {
				record.setBusniessAmount(buffer2.toString());
			}
			
			//附件
			if(record.getInvoiceDetailId()!=null) {
				StringBuffer sb = new StringBuffer();
				LambdaQueryWrapper<CssIncomeFiles> cssIncomeFilesWrapper = new LambdaQueryWrapper<CssIncomeFiles>();
				cssIncomeFilesWrapper.eq(CssIncomeFiles::getInvoiceDetailId, record.getInvoiceDetailId());
				cssIncomeFilesWrapper.isNull(CssIncomeFiles::getInvoiceDetailWriteoffId);
				List<CssIncomeFiles> list = filesMapper.selectList(cssIncomeFilesWrapper);
				if(list!=null&&list.size()>0) {
					for(CssIncomeFiles files:list) {
						sb.append(files.getFileUrl());
						sb.append(" ");
						sb.append(files.getFileName());
						sb.append("  ");
					}
					record.setFiles(sb.toString());
				}
			}
		});
		return iPage;
	}


	@Override
	public Map openView(CssIncomeInvoice bean) {
		Map map = new HashMap();
		if(bean.getDebitNoteId()!=null) {
			//查询 币种 表
			List<CssDebitNoteCurrency> listTwo = cssDebitNoteCurrencyMapper.queryBill2(SecurityUtils.getUser().getOrgId(),bean.getDebitNoteId().toString());
			if(listTwo!=null&&listTwo.size()>0) {
				listTwo.stream().forEach(o->{
					if(o.getAmountInvoice()!=null) {
						o.setAmountInvoiceNo(o.getAmount().subtract(o.getAmountInvoice()));
					}else {
						o.setAmountInvoiceNo(o.getAmount());
					}
				});
			}
			map.put("dataTwo",listTwo);
		}else {
			//查询 币种 表
			List<StatementCurrency> listTwo = statementCurrencyMapper.queryBillCurrency(SecurityUtils.getUser().getOrgId(),bean.getStatementId());
			if(listTwo!=null&&listTwo.size()>0) {
				listTwo.stream().forEach(o->{
					if(o.getAmountInvoice()!=null) {
						o.setAmountInvoiceNo(o.getAmount().subtract(o.getAmountInvoice()));
					}else {
						o.setAmountInvoiceNo(o.getAmount());
					}
				});
			}
			map.put("dataTwo",listTwo);
		}
		//查询开票明细
		LambdaQueryWrapper<CssIncomeInvoiceDetail> cssIncomeInvoiceDetailWrapper = Wrappers.<CssIncomeInvoiceDetail>lambdaQuery();
		cssIncomeInvoiceDetailWrapper.eq(CssIncomeInvoiceDetail::getInvoiceId, bean.getInvoiceId()).eq(CssIncomeInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId());
		List<CssIncomeInvoiceDetail> listOne = cssIncomeInvoiceDetailMapper.selectList(cssIncomeInvoiceDetailWrapper);
		map.put("dataOne",listOne);
		return map;
	}


	@Override
	public CssIncomeInvoice invoiceView(Integer invoiceId) {
		return baseMapper.selectById(invoiceId);
	}


	@Override
	public void exportExcelList(CssIncomeInvoice bean) {
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
                int numStr = 0;
                for (int i = 0; i < jsonArr.size(); i++) {
                    JSONObject job = jsonArr.getJSONObject(i);
                    headers[numStr] = job.getString("label");
                    colunmStrs[numStr] = job.getString("prop");
                    numStr++;
                }
            }
        } 
        //查询
        Page page = new Page();
        page.setCurrent(1);
        page.setSize(1000000);
        IPage result = this.getPage(page, bean);
        if (result != null && result.getRecords() != null && result.getRecords().size() > 0) {
            DecimalFormat decimalFormat2 = new DecimalFormat("#,###,###,###.##########");
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            List<CssIncomeInvoice> listA = result.getRecords();
            for (CssIncomeInvoice excel2 : listA) {
                LinkedHashMap mapTwo = new LinkedHashMap();
                for (int j = 0; j < colunmStrs.length; j++) {
                    if ("amount".equals(colunmStrs[j])) {
                        if (excel2.getAmount() != null) {
                            mapTwo.put("amount", decimalFormat2.format(excel2.getAmount())+"("+excel2.getCurrency()+")");
                        } else {
                            mapTwo.put("amount", "");
                        }
                    }else if("amountWriteoff".equals(colunmStrs[j])){
                    	 if (excel2.getAmountWriteoff() != null) {
                             mapTwo.put("amountWriteoff", decimalFormat2.format(excel2.getAmountWriteoff()));
                         } else {
                             mapTwo.put("amountWriteoff", "");
                         }
                    }else if("amountWriteoffNo".equals(colunmStrs[j])){
                   	 if (excel2.getAmountWriteoffNo() != null) {
                         mapTwo.put("amountWriteoffNo", decimalFormat2.format(excel2.getAmountWriteoffNo()));
                     } else {
                         mapTwo.put("amountWriteoffNo", "");
                     }
                    }else if("creatorName".equals(colunmStrs[j])){
	                   	 if (excel2.getCreatorName()!=null&&!excel2.getCreatorName().isEmpty()) {
	                         mapTwo.put("creatorName", excel2.getCreatorName().split(" ")[0]);
	                     } else {
	                         mapTwo.put("creatorName", "");
	                     }
                    }else if("openInvoiceUserName".equals(colunmStrs[j])){
	                   	 if (excel2.getOpenInvoiceUserName()!=null&&!excel2.getOpenInvoiceUserName().isEmpty()) {
	                         mapTwo.put("openInvoiceUserName", excel2.getOpenInvoiceUserName().split(" ")[0]);
	                     } else {
	                         mapTwo.put("openInvoiceUserName", "");
	                     }
                    }else if("createTime".equals(colunmStrs[j])){
	                   	 if (excel2.getCreateTime()!=null) {
	                         mapTwo.put("createTime", df.format(excel2.getCreateTime()));
	                     } else {
	                         mapTwo.put("createTime", "");
	                     }
                    }else if("files".equals(colunmStrs[j])){
	              		 StringBuffer sb = new StringBuffer();
	                     if (excel2.getFiles() != null && !"".equals(excel2.getFiles())) {
	                         if (excel2.getFiles().contains("  ")) {
	                             String[] array = excel2.getFiles().split("  ");
	                             for (int k = 0; k < array.length; k++) {
	                                 sb.append(array[k].split(" ")[0]).append("\n");
	                             }
	                             mapTwo.put("files", sb.toString());
	                         } else {
	                             mapTwo.put("files", excel2.getFiles().split(" ")[0]);
	                         }
	                     } else {
	                         mapTwo.put("files", "");
	                     }
                    }
                    else if("openInvoiceTime".equals(colunmStrs[j])){
	                   	 if (excel2.getOpenInvoiceTime()!=null) {
	                         mapTwo.put("openInvoiceTime", df.format(excel2.getOpenInvoiceTime()));
	                     } else {
	                         mapTwo.put("openInvoiceTime", "");
	                     }
                    }else {
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
