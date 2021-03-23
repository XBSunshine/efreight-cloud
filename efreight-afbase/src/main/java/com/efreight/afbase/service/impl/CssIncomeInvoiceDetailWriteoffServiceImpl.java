package com.efreight.afbase.service.impl;

import com.efreight.afbase.entity.AfIncome;
import com.efreight.afbase.entity.CssDebitNoteCurrency;
import com.efreight.afbase.entity.CssIncomeFiles;
import com.efreight.afbase.entity.CssIncomeInvoice;
import com.efreight.afbase.entity.CssIncomeInvoiceDetail;
import com.efreight.afbase.entity.CssIncomeInvoiceDetailWriteoff;
import com.efreight.afbase.entity.DebitNote;
import com.efreight.afbase.entity.Statement;
import com.efreight.afbase.entity.StatementCurrency;
import com.efreight.afbase.dao.CssDebitNoteCurrencyMapper;
import com.efreight.afbase.dao.CssIncomeFilesMapper;
import com.efreight.afbase.dao.CssIncomeInvoiceDetailMapper;
import com.efreight.afbase.dao.CssIncomeInvoiceDetailWriteoffMapper;
import com.efreight.afbase.dao.CssIncomeInvoiceMapper;
import com.efreight.afbase.dao.DebitNoteMapper;
import com.efreight.afbase.dao.StatementCurrencyMapper;
import com.efreight.afbase.dao.StatementMapper;
import com.efreight.afbase.service.CssIncomeInvoiceDetailWriteoffService;
import com.efreight.afbase.utils.FieldValUtils;
import com.efreight.afbase.utils.LoginUtils;
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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * CSS 应收：发票明细 核销表 服务实现类
 * </p>
 *
 * @author cwd
 * @since 2020-12-28
 */
@Service
@AllArgsConstructor
public class CssIncomeInvoiceDetailWriteoffServiceImpl extends ServiceImpl<CssIncomeInvoiceDetailWriteoffMapper, CssIncomeInvoiceDetailWriteoff> implements CssIncomeInvoiceDetailWriteoffService {

	private final CssIncomeInvoiceDetailMapper detailMapper;
	private final CssIncomeInvoiceMapper invoiceMapper;
	private final CssDebitNoteCurrencyMapper cssDebitNoteCurrencyMapper;
	private final DebitNoteMapper debitNoteMapper;
    private final StatementMapper statementMapper;
    private final StatementCurrencyMapper statementCurrencyMapper;
    private final CssIncomeFilesMapper filesMapper;
	
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean doSave(CssIncomeInvoiceDetailWriteoff bean) {
		//校验：发票申请表 rowid 是否变化
		CssIncomeInvoiceDetail detail = detailMapper.selectById(bean.getInvoiceDetailId());
		if(detail==null) {
			throw new RuntimeException("开票数据异常");
		}
		CssIncomeInvoice invoice = invoiceMapper.selectById(detail.getInvoiceId());
		if(invoice==null) {
			throw new RuntimeException("发票申请数据异常");
		}
		if(!bean.getInvoiceRowUuid().equals(invoice.getRowUuid())) {
			throw new RuntimeException("发票申请数据有变更");
		}
		
		BigDecimal noWf = detail.getAmount().subtract(detail.getAmountWriteoff()!=null?detail.getAmountWriteoff():BigDecimal.ZERO);
		//校验核销金额是否大于 未核销金额
		if(bean.getAmountWriteoff().compareTo(noWf)>0) {
			throw new RuntimeException("本次核销金额不能大于未核销金额");
		}
		//插入：开票核销表（css_income_invoice_detail_writeoff）；
		  //生成核销号
        String code = getCode(detail.getBusinessScope());
        List<CssIncomeInvoiceDetailWriteoff> codeList = baseMapper.selectCode(SecurityUtils.getUser().getOrgId(), code);
        if (codeList.size() == 0) {
            bean.setWriteoffNum(code + "0001");
        } else {
            if ((code + "9999").equals(codeList.get(0).getWriteoffNum())) {
                throw new RuntimeException("每天最多可以核销9999个" + bean.getBusinessScope() + "核销单");
            } else {
                String str = codeList.get(0).getWriteoffNum();
                str = str.substring(str.length() - 4);
                bean.setWriteoffNum(code + String.format("%04d", Integer.parseInt(str) + 1));
            }

        }
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        bean.setBusinessScope(detail.getBusinessScope());
        bean.setDebitNoteId(invoice.getDebitNoteId());
        bean.setStatementId(invoice.getStatementId());
        bean.setInvoiceId(invoice.getInvoiceId());
        bean.setCreateTime(LocalDateTime.now());
        bean.setCreatorId(SecurityUtils.getUser().getId());
        bean.setCreatorName(SecurityUtils.getUser().buildOptName());
        bean.setRowUuid(UUID.randomUUID().toString());
        baseMapper.insert(bean);
        //更新操作
        this.updateAll(bean);
		return true;
	}
	
	@Override
	public boolean updateAll(CssIncomeInvoiceDetailWriteoff bean) {
		
		String rowUuid = UUID.randomUUID().toString();
		//更新 对应的开票明细表 核销金额
		CssIncomeInvoiceDetail detail = detailMapper.selectById(bean.getInvoiceDetailId());
		LambdaQueryWrapper<CssIncomeInvoiceDetailWriteoff> cssIncomeInvoiceDetailWriteoffWrapper = Wrappers.<CssIncomeInvoiceDetailWriteoff>lambdaQuery();
		cssIncomeInvoiceDetailWriteoffWrapper.eq(CssIncomeInvoiceDetailWriteoff::getInvoiceDetailId, bean.getInvoiceDetailId()).eq(CssIncomeInvoiceDetailWriteoff::getOrgId, SecurityUtils.getUser().getOrgId());
		List<CssIncomeInvoiceDetailWriteoff> listOne = baseMapper.selectList(cssIncomeInvoiceDetailWriteoffWrapper);
		if(listOne!=null&&listOne.size()>0) {
			detail.setAmountWriteoff(null);
			for(CssIncomeInvoiceDetailWriteoff dw:listOne) {
				if(detail.getAmountWriteoff()!=null) {
					detail.setAmountWriteoff(detail.getAmountWriteoff().add(dw.getAmountWriteoff()));
				}else {
					detail.setAmountWriteoff(dw.getAmountWriteoff());
				}
			}
		}else {
			detail.setAmountWriteoff(null);
		}
		if(detail.getAmountWriteoff()!=null&&detail.getAmount().compareTo(detail.getAmountWriteoff())==0) {
			detail.setWriteoffComplete(1);
		}else if(listOne!=null&&listOne.size()>0) {
			detail.setWriteoffComplete(0);
		}else {
			detail.setWriteoffComplete(null);
		}
		detail.setRowUuid(rowUuid);
		detailMapper.updateById(detail);
		//更新 开票申请表rowUuid
		CssIncomeInvoice invoice = invoiceMapper.selectById(detail.getInvoiceId());
		invoice.setRowUuid(rowUuid);
		invoiceMapper.updateById(invoice);
		//计算当前账单或者清单所核销对应币种的钱数
		BigDecimal amount = null;
		BigDecimal amountIncome = null;
		LambdaQueryWrapper<CssIncomeInvoiceDetailWriteoff> wOne = Wrappers.<CssIncomeInvoiceDetailWriteoff>lambdaQuery();
		wOne.eq(CssIncomeInvoiceDetailWriteoff::getInvoiceId, bean.getInvoiceId()).eq(CssIncomeInvoiceDetailWriteoff::getOrgId, SecurityUtils.getUser().getOrgId());
		wOne.eq(CssIncomeInvoiceDetailWriteoff::getCurrency, bean.getCurrency());
		List<CssIncomeInvoiceDetailWriteoff> listTwo = baseMapper.selectList(wOne);
		if(listTwo!=null&&listTwo.size()>0) {
			for(CssIncomeInvoiceDetailWriteoff cd:listTwo) {
				if(amount!=null) {
					amount = amount.add(cd.getAmountWriteoff());
				}else {
					amount = cd.getAmountWriteoff();
				}
			}
			amountIncome = amount;
		}
		List<DebitNote> listDebitNotes = new ArrayList<DebitNote>();
		if(invoice.getDebitNoteId()!=null) {
			//账单
			DebitNote d = debitNoteMapper.selectById(invoice.getDebitNoteId());
			listDebitNotes.add(d);
		}else {
			//清单所在账单
			listDebitNotes = debitNoteMapper.queryDebitNoteListByStatement(SecurityUtils.getUser().getOrgId(), invoice.getStatementId(),bean.getCurrency(),bean.getBusinessScope());
		}
		
		String orderId = null;
		
		if(listDebitNotes!=null&&listDebitNotes.size()>0) {
			BigDecimal amountCurr = amount;
			BigDecimal amountIncomeOne = amountIncome;
			for(DebitNote dn:listDebitNotes) {
				if(orderId!=null) {
					orderId = orderId+","+dn.getOrderId().toString();
				}else {
					orderId = dn.getOrderId().toString();
				}
				//账单币种表 对应币种的 已核销金额 = NULL
				baseMapper.updateDebitNoteAmountWriteoff(SecurityUtils.getUser().getOrgId(),dn.getDebitNoteId(),bean.getCurrency());
				//根据 账单 对应订单的开航日期（进口按照 到港日期）升序 、金额 升序
				List<CssDebitNoteCurrency> list = cssDebitNoteCurrencyMapper.queryCssDebitNoteCurrencyOrder(SecurityUtils.getUser().getOrgId(),dn.getDebitNoteId(),bean.getCurrency(),bean.getBusinessScope());
				if(list!=null&&list.size()>0&&amount!=null) {
					a:for(CssDebitNoteCurrency cdnc:list) {
						if(amountCurr.compareTo(BigDecimal.ZERO)==0) {
							if(amountCurr.compareTo(amount)==0) {
								//如果核销金额是0  并且与初始核销数相等 则全部核销
								cdnc.setAmountWriteoff(cdnc.getAmount());
							}else {
								//不相等认为核销完毕 打破循环
								break a;
							}
						}else if(amountCurr.compareTo(BigDecimal.ZERO)<0) {
							cdnc.setAmountWriteoff(cdnc.getAmount());
						}else {
							BigDecimal amountCurrTwo = amountCurr;
							amountCurr = amountCurr.subtract(cdnc.getAmount());
							if(amountCurr.compareTo(BigDecimal.ZERO)>0) {
								cdnc.setAmountWriteoff(cdnc.getAmount());
								
							}else if(amountCurr.compareTo(BigDecimal.ZERO)<=0) {
								cdnc.setAmountWriteoff(amountCurrTwo);
								//核销金额 消耗完毕后归0 
								amountCurr = BigDecimal.ZERO;
								cssDebitNoteCurrencyMapper.updateById(cdnc);
								break a;
							}
						} 
						cssDebitNoteCurrencyMapper.updateById(cdnc);
					}
					
				}
				
				//更新账单表
				 List<CssDebitNoteCurrency> listAll = cssDebitNoteCurrencyMapper.queryBill2(SecurityUtils.getUser().getOrgId(),dn.getDebitNoteId().toString());
				 boolean flagStatus = true;
				 boolean flagStatusNUll = true;
				 BigDecimal amountWriteoff = null;//本币核销金额
				 if(listAll!=null&&listAll.size()>0) {
					for(CssDebitNoteCurrency cdc:listAll) {
						if(cdc.getAmountWriteoff()!=null) {
							flagStatusNUll = false;
							if(amountWriteoff!=null) {
								amountWriteoff = amountWriteoff.add(cdc.getAmountWriteoff().multiply(cdc.getExchangeRate()));
							}else {
								amountWriteoff = cdc.getAmountWriteoff().multiply(cdc.getExchangeRate());
							}
							if(cdc.getAmountWriteoff()!=null&&cdc.getAmount().compareTo(cdc.getAmountWriteoff())>0) {
								flagStatus = false;
							}
						}else {
							flagStatus = false;
						}
					}
					if(flagStatusNUll) {
						dn.setWriteoffComplete(null);
					}else if(flagStatus){
						dn.setWriteoffComplete(1);
					}else {
						dn.setWriteoffComplete(0);
					}
					dn.setFunctionalAmountWriteoff(amountWriteoff);
					dn.setRowUuid(rowUuid);
					debitNoteMapper.updateById(dn);
				}
				 
				 
			   //更新 income表
			   //先将income income_amount_writeoff 置为NULL
			   baseMapper.updateIncomeAmountWriteoff(SecurityUtils.getUser().getOrgId(),dn.getDebitNoteId(),bean.getCurrency(),bean.getBusinessScope());
			   //查询对应账单币种的income
			   List<AfIncome> listIncome = baseMapper.queryIncomeAmountWriteoff(SecurityUtils.getUser().getOrgId(),dn.getDebitNoteId(),bean.getCurrency(),bean.getBusinessScope());
			   if(listIncome!=null&&listIncome.size()>0&&amountIncome!=null) {
				   for(AfIncome income:listIncome) {
					   if(amountIncome.compareTo(BigDecimal.ZERO)==0) {
							if(amountIncome.compareTo(amountIncomeOne)==0) {
								//如果核销金额是0  并且与初始核销数相等 则全部核销
								baseMapper.updateIncomeAmountWriteoffTwo(income.getIncomeId(),income.getIncomeAmount(),rowUuid,bean.getBusinessScope());
							}else {
								//不相等认为核销完毕 打破循环
								break;
							}
					   }else if(amountIncome.compareTo(BigDecimal.ZERO)<0) {
						   baseMapper.updateIncomeAmountWriteoffTwo(income.getIncomeId(),income.getIncomeAmount(),rowUuid,bean.getBusinessScope());
					   }else {
						   BigDecimal amountIncomeTwo = amountIncome;
						   amountIncome = amountIncome.subtract(income.getIncomeAmount());
						   if(amountIncome.compareTo(BigDecimal.ZERO)>0) {
							   baseMapper.updateIncomeAmountWriteoffTwo(income.getIncomeId(),income.getIncomeAmount(),rowUuid,bean.getBusinessScope());
						   }else if(amountIncome.compareTo(BigDecimal.ZERO)<=0){
							   amountIncome = BigDecimal.ZERO;
							   baseMapper.updateIncomeAmountWriteoffTwo(income.getIncomeId(),amountIncomeTwo,rowUuid,bean.getBusinessScope());
							   break;
						   }
					   }
				   }
			   }
			}
		}
		
		//如果是清单 更新清单币种表  清单表 
		if(bean.getStatementId()!=null) {
			Statement s = statementMapper.selectById(bean.getStatementId());
			//清单币种表 对应币种的 已核销金额 = NULL
			baseMapper.updateStatementAmountWriteoff(SecurityUtils.getUser().getOrgId(),bean.getStatementId(),bean.getCurrency());
			StatementCurrency sc = statementCurrencyMapper.queryStatementCurrencyCurrency(SecurityUtils.getUser().getOrgId(),bean.getStatementId(),bean.getCurrency());
			if(sc!=null&&amount!=null) {
				if(amount.compareTo(BigDecimal.ZERO)<0) {
					sc.setAmountWriteoff(sc.getAmount());
				}else if(amount.compareTo(sc.getAmount())>0){
					sc.setAmountWriteoff(sc.getAmount());
					amount = amount.subtract(sc.getAmount());
				}else if(amount.compareTo(sc.getAmount())==0) {
					sc.setAmountWriteoff(sc.getAmount());
					amount = BigDecimal.ZERO;
				}else {
					sc.setAmountWriteoff(amount);
					amount = BigDecimal.ZERO;
				}
				sc.setFunctionalAmountWriteoff(sc.getAmountWriteoff().multiply(sc.getExchangeRate()));
				statementCurrencyMapper.updateById(sc);
			}
			
			
			//更新清单表
			 List<StatementCurrency> listAll = statementCurrencyMapper.queryBillCurrency(SecurityUtils.getUser().getOrgId(),bean.getStatementId());
			 boolean flagStatus = true;
			 boolean flagStatusNUll = true;
			 BigDecimal amountWriteoff = null;//本币核销金额
			 if(listAll!=null&&listAll.size()>0) {
				for(StatementCurrency stc:listAll) {
					if(stc.getAmountWriteoff()!=null) {
						flagStatusNUll = false;
						if(amountWriteoff!=null) {
							amountWriteoff = amountWriteoff.add(stc.getAmountWriteoff().multiply(stc.getExchangeRate()));
						}else {
							amountWriteoff = stc.getAmountWriteoff().multiply(stc.getExchangeRate());
						}
						if(stc.getAmount().compareTo(stc.getAmountWriteoff())>0) {
							flagStatus = false;
						}
					}else {
						flagStatus = false;
					}
				}
				if(flagStatusNUll) {
					s.setWriteoffComplete(null);
				}else if(flagStatus){
					s.setWriteoffComplete(1);
				}else {
					s.setWriteoffComplete(0);
				}
				s.setFunctionalAmountWriteoff(amountWriteoff);
				s.setRowUuid(rowUuid);
				statementMapper.updateById(s);
			}
		}
		
		
	   //更新订单应收状态：（order. income_status）
	   List<Map> listMap = detailMapper.getOrderIncomeStatus(SecurityUtils.getUser().getOrgId(), orderId,bean.getBusinessScope());
	   if(listMap!=null&&listMap.size()>0) {
			for(Map map:listMap) {
				detailMapper.updateOrderIncomeStatus(Integer.valueOf(map.get("org_id").toString()),Integer.valueOf(map.get("order_id").toString()),map.get("income_status").toString(),rowUuid,bean.getBusinessScope());
			}
	    }
		
	   return true;
	}
	
	
	
	 private String getCode(String businessScope) {
        Date dt = new Date();

        String year = String.format("%ty", dt);

        String mon = String.format("%tm", dt);

        String day = String.format("%td", dt);
        return businessScope + "-RW-" + year + mon + day;
    }


	@Override
	public IPage getPage(Page page, CssIncomeInvoiceDetailWriteoff bean) {
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		IPage<CssIncomeInvoiceDetailWriteoff> iPage = baseMapper.getPage(page, bean);
		if(iPage!=null&&iPage.getRecords()!=null&&iPage.getRecords().size()>0) {
			iPage.getRecords().stream().forEach(record -> {
				StringBuffer sb = new StringBuffer();
				//附件
				LambdaQueryWrapper<CssIncomeFiles> cssIncomeFilesWrapper = new LambdaQueryWrapper<CssIncomeFiles>();
				cssIncomeFilesWrapper.eq(CssIncomeFiles::getInvoiceDetailWriteoffId, record.getInvoiceDetailWriteoffId());
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
			});
		}
		return iPage;
	}


	@Override
	public void exportExcelList(CssIncomeInvoiceDetailWriteoff bean) {
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
            List<CssIncomeInvoiceDetailWriteoff> listA = result.getRecords();
            for (CssIncomeInvoiceDetailWriteoff excel2 : listA) {
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
                             mapTwo.put("amountWriteoff", decimalFormat2.format(excel2.getAmountWriteoff())+"("+excel2.getCurrency()+")");
                         } else {
                             mapTwo.put("amountWriteoff", "");
                         }
                    }else if("creatorName".equals(colunmStrs[j])){
	                   	 if (excel2.getCreatorName()!=null&&!excel2.getCreatorName().isEmpty()) {
	                         mapTwo.put("creatorName", excel2.getCreatorName().split(" ")[0]);
	                     } else {
	                         mapTwo.put("creatorName", "");
	                     }
                    }else if("createTime".equals(colunmStrs[j])){
	                   	 if (excel2.getCreateTime()!=null) {
	                         mapTwo.put("createTime", df.format(excel2.getCreateTime()));
	                     } else {
	                         mapTwo.put("createTime", "");
	                     }
                    }else if("financialAccountName".equals(colunmStrs[j])){
                      	 if (excel2.getFinancialAccountName()!=null&&!excel2.getFinancialAccountName().isEmpty()) {
                             mapTwo.put("financialAccountName", excel2.getFinancialAccountName().split(" ")[0]);
                         } else {
                             mapTwo.put("financialAccountName", "");
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
                    else {
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
	public CssIncomeInvoiceDetailWriteoff viewInfo(Integer invoiceDetailWriteoffId) {
		return baseMapper.selectById(invoiceDetailWriteoffId);
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteInfo(Integer invoiceDetailWriteoffId) {
		CssIncomeInvoiceDetailWriteoff info = baseMapper.selectById(invoiceDetailWriteoffId);
		if(info==null) {
			throw new RuntimeException("删除失败，数据有变化，请刷新再试");
		}
		baseMapper.deleteById(info);
		//删除核销单对应的附件
		LambdaQueryWrapper<CssIncomeFiles> cssIncomeFilesWrapper = new LambdaQueryWrapper<CssIncomeFiles>();
		cssIncomeFilesWrapper.eq(CssIncomeFiles::getInvoiceDetailWriteoffId, info.getInvoiceDetailWriteoffId());
		List<CssIncomeFiles> list = filesMapper.selectList(cssIncomeFilesWrapper);
		if(list!=null&&list.size()>0) {
			list.stream().forEach(o->{
				filesMapper.deleteById(o);
			});
		}
		//更新
		this.updateAll(info);
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean invoiceAuto(CssIncomeInvoiceDetailWriteoff bean) {
		String rowUuid = UUID.randomUUID().toString();
		String orderId = null;
		//发票申请
		CssIncomeInvoice invoice = new CssIncomeInvoice();
		//发票
		CssIncomeInvoiceDetail invoiceDetail = new CssIncomeInvoiceDetail();
		//发票核销
		CssIncomeInvoiceDetailWriteoff invoiceDetailWriteoff = new CssIncomeInvoiceDetailWriteoff();
		List<DebitNote> listDebitNotes = new ArrayList<DebitNote>();
		if("账单".equals(bean.getTitleName())) {
			DebitNote dn =  debitNoteMapper.selectById(bean.getDebitNoteId());
			//校验
			if(!dn.getRowUuid().equals(bean.getInvoiceRowUuid())) {
				throw new RuntimeException("您好，账单号"+dn.getDebitNoteNum()+" 不是最新数据，请刷新后重试");
			}
			//1是否制作清单
			if(dn.getStatementId()!=null) {
				throw new RuntimeException("您好，账单号"+dn.getDebitNoteNum()+" 已制清单，请到清单页面进行核销。");
			}
			//2是否有开票记录
			LambdaQueryWrapper<CssIncomeInvoice> cssIncomeInvoiceWrapper = Wrappers.<CssIncomeInvoice>lambdaQuery();
			cssIncomeInvoiceWrapper.eq(CssIncomeInvoice::getDebitNoteId, dn.getDebitNoteId()).eq(CssIncomeInvoice::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssIncomeInvoice::getBusinessScope,dn.getBusinessScope());
			List<CssIncomeInvoice> list = invoiceMapper.selectList(cssIncomeInvoiceWrapper);
			if(list!=null&&list.size()>0) {
				throw new RuntimeException("您好，账单号"+dn.getDebitNoteNum()+"已申请开票或已开票，请在发票页面进行核销。");
			}
			invoice.setDebitNoteId(bean.getDebitNoteId());
			invoice.setStatementId(null);
			invoiceDetail.setDebitNoteId(bean.getDebitNoteId());
			invoiceDetail.setStatementId(null);
			invoiceDetailWriteoff.setDebitNoteId(bean.getDebitNoteId());
			invoiceDetailWriteoff.setStatementId(null);
			listDebitNotes.add(dn);
			
		}else {
			//是否有开票记录
			Statement s = statementMapper.selectById(bean.getStatementId());
			if(!s.getRowUuid().equals(bean.getInvoiceRowUuid())) {
				throw new RuntimeException("您好，清单号"+s.getStatementNum()+" 不是最新数据，请刷新后重试");
			}
			LambdaQueryWrapper<CssIncomeInvoice> cssIncomeInvoiceWrapper = Wrappers.<CssIncomeInvoice>lambdaQuery();
			cssIncomeInvoiceWrapper.eq(CssIncomeInvoice::getStatementId, s.getStatementId()).eq(CssIncomeInvoice::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssIncomeInvoice::getBusinessScope,s.getBusinessScope());
			List<CssIncomeInvoice> list = invoiceMapper.selectList(cssIncomeInvoiceWrapper);
			if(list!=null&&list.size()>0) {
				throw new RuntimeException("您好，账单号"+s.getStatementNum()+"已申请开票或已开票，请在发票页面进行核销。");
			}
			invoice.setDebitNoteId(null);
			invoice.setStatementId(bean.getStatementId());
			invoiceDetail.setDebitNoteId(null);
			invoiceDetail.setStatementId(bean.getStatementId());
			invoiceDetailWriteoff.setDebitNoteId(null);
			invoiceDetailWriteoff.setStatementId(bean.getStatementId());
			//清单所在账单
			listDebitNotes = debitNoteMapper.queryDebitNoteListByStatement(SecurityUtils.getUser().getOrgId(), invoice.getStatementId(),bean.getCurrency(),bean.getBusinessScope());
			//清单币种
			StatementCurrency sc = statementCurrencyMapper.queryStatementCurrencyCurrency(SecurityUtils.getUser().getOrgId(),bean.getStatementId(),bean.getCurrency());
			sc.setAmountWriteoff(sc.getAmount());
			sc.setFunctionalAmountWriteoff(sc.getAmountWriteoff().multiply(sc.getExchangeRate()));
			sc.setAmountInvoice(sc.getAmount());
		    statementCurrencyMapper.updateById(sc);
		    //更新：清单
			s.setRowUuid(rowUuid);
			s.setFunctionalAmountWriteoff(sc.getAmountWriteoff().multiply(sc.getExchangeRate()));
            s.setWriteoffComplete(1);
			statementMapper.updateById(s);
		}
		// 账单币种 更新
		if(listDebitNotes!=null&&listDebitNotes.size()>0) {
		   for(DebitNote dn:listDebitNotes) {
				if(orderId!=null) {
					orderId = orderId+","+dn.getOrderId().toString();
				}else {
					orderId = dn.getOrderId().toString();
				}
				//更新：对应账单/清单 币种表，对应币种的已开票金额
				CssDebitNoteCurrency cdc = cssDebitNoteCurrencyMapper.queryCssDebitNoteCurrency(SecurityUtils.getUser().getOrgId(),dn.getDebitNoteId(),bean.getCurrency());
	            if(cdc!=null) {
	            	cdc.setAmountInvoice(cdc.getAmount());
	            	cdc.setAmountWriteoff(cdc.getAmount());
	            	cdc.setFunctionalAmountWriteoff(cdc.getAmountWriteoff().multiply(cdc.getExchangeRate()));
	            	cssDebitNoteCurrencyMapper.updateById(cdc);
	            }
	            //更新账单
	            dn.setFunctionalAmountWriteoff(cdc.getAmountWriteoff().multiply(cdc.getExchangeRate()));
	            dn.setWriteoffComplete(1);
				dn.setRowUuid(rowUuid);
				debitNoteMapper.updateById(dn);
	           //查询对应账单币种的income
			   List<AfIncome> listIncome = baseMapper.queryIncomeAmountWriteoff(SecurityUtils.getUser().getOrgId(),dn.getDebitNoteId(),bean.getCurrency(),bean.getBusinessScope());
			   if(listIncome!=null&&listIncome.size()>0) {
				   for(AfIncome income:listIncome) {
					   baseMapper.updateIncomeAmountWriteoffTwo(income.getIncomeId(),income.getIncomeAmount(),rowUuid,bean.getBusinessScope());
				   }
			   }
			}
	    }
		invoice.setOrgId(SecurityUtils.getUser().getOrgId());
		invoice.setInvoiceStatus(1);
		invoice.setCreatorId(SecurityUtils.getUser().getId());
		invoice.setCreateTime(LocalDateTime.now());
		invoice.setCreatorName(SecurityUtils.getUser().buildOptName());
		invoice.setRowUuid(rowUuid);
		invoice.setBusinessScope(bean.getBusinessScope());
		invoice.setCustomerId(bean.getCustomerId());
		invoice.setCustomerName(bean.getCustomerName());
		invoice.setInvoiceType(bean.getInvoiceType());
		invoice.setInvoiceTitle(bean.getInvoiceTitle());
		invoice.setTaxpayerNum(bean.getTaxpayerNum());
		invoice.setAddress(bean.getAddress());
		invoice.setPhoneNumber(bean.getPhoneNumber());
		invoice.setBankName(bean.getBankName());
		invoice.setBankNumber(bean.getBankNumber());
		invoice.setApplyRemark(bean.getInvoiceRemark());
		invoiceMapper.insert(invoice);
		
		invoiceDetail.setOrgId(SecurityUtils.getUser().getOrgId());
		invoiceDetail.setBusinessScope(bean.getBusinessScope());
		invoiceDetail.setInvoiceId(invoice.getInvoiceId());
		invoiceDetail.setCustomerId(bean.getCustomerId());
		invoiceDetail.setCustomerName(bean.getCustomerName());
		invoiceDetail.setInvoiceNum(bean.getInvoiceNum());
		invoiceDetail.setInvoiceType(bean.getInvoiceType());
		invoiceDetail.setInvoiceTitle(bean.getInvoiceTitle());
		invoiceDetail.setInvoiceDate(bean.getInvoiceDate());
		invoiceDetail.setCurrency(bean.getCurrency());
		invoiceDetail.setAmount(bean.getAmountWriteoff());
		invoiceDetail.setAddress(bean.getAddress());
		invoiceDetail.setPhoneNumber(bean.getPhoneNumber());
		invoiceDetail.setBankName(bean.getBankName());
		invoiceDetail.setBankNumber(bean.getBankNumber());
		invoiceDetail.setInvoiceRemark(bean.getInvoiceRemark());
		invoiceDetail.setCreatorId(SecurityUtils.getUser().getId());
		invoiceDetail.setCreateTime(LocalDateTime.now());
		invoiceDetail.setCreatorName(SecurityUtils.getUser().buildOptName());
		invoiceDetail.setRowUuid(rowUuid);
		invoiceDetail.setWriteoffComplete(1);
		invoiceDetail.setAmountWriteoff(bean.getAmountWriteoff());
		detailMapper.insert(invoiceDetail);
		
		invoiceDetailWriteoff.setOrgId(SecurityUtils.getUser().getOrgId());
		invoiceDetailWriteoff.setBusinessScope(bean.getBusinessScope());
		invoiceDetailWriteoff.setInvoiceId(invoice.getInvoiceId());
		invoiceDetailWriteoff.setInvoiceDetailId(invoiceDetail.getInvoiceDetailId());
		
		//生成核销号
        String code = getCode(invoiceDetailWriteoff.getBusinessScope());
        List<CssIncomeInvoiceDetailWriteoff> codeList = baseMapper.selectCode(SecurityUtils.getUser().getOrgId(), code);
        if (codeList.size() == 0) {
        	invoiceDetailWriteoff.setWriteoffNum(code + "0001");
        } else {
            if ((code + "9999").equals(codeList.get(0).getWriteoffNum())) {
                throw new RuntimeException("每天最多可以核销9999个" + bean.getBusinessScope() + "核销单");
            } else {
                String str = codeList.get(0).getWriteoffNum();
                str = str.substring(str.length() - 4);
                invoiceDetailWriteoff.setWriteoffNum(code + String.format("%04d", Integer.parseInt(str) + 1));
            }
        }
		invoiceDetailWriteoff.setCurrency(bean.getCurrency());
		invoiceDetailWriteoff.setAmountWriteoff(bean.getAmountWriteoff());
		invoiceDetailWriteoff.setWriteoffRemark(bean.getWriteoffRemark());
		invoiceDetailWriteoff.setFinancialAccountName(bean.getFinancialAccountName());
		invoiceDetailWriteoff.setFinancialAccountCode(bean.getFinancialAccountCode());
		invoiceDetailWriteoff.setFinancialAccountType(bean.getFinancialAccountType());
		invoiceDetailWriteoff.setWriteoffDate(bean.getWriteoffDate());
		invoiceDetailWriteoff.setCustomerId(bean.getCustomerId());
		invoiceDetailWriteoff.setCustomerName(bean.getCustomerName());
		invoiceDetailWriteoff.setCreatorId(SecurityUtils.getUser().getId());
		invoiceDetailWriteoff.setCreateTime(LocalDateTime.now());
		invoiceDetailWriteoff.setCreatorName(SecurityUtils.getUser().buildOptName());
		invoiceDetailWriteoff.setRowUuid(rowUuid);
		baseMapper.insert(invoiceDetailWriteoff);
		
		//更新订单应收状态：（order. income_status）
	   List<Map> listMap = detailMapper.getOrderIncomeStatus(SecurityUtils.getUser().getOrgId(), orderId,bean.getBusinessScope());
	   if(listMap!=null&&listMap.size()>0) {
			for(Map map:listMap) {
				detailMapper.updateOrderIncomeStatus(Integer.valueOf(map.get("org_id").toString()),Integer.valueOf(map.get("order_id").toString()),map.get("income_status").toString(),rowUuid,bean.getBusinessScope());
			}
	    }
		return true;
	}

}
