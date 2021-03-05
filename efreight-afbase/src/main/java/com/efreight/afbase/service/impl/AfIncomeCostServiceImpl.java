package com.efreight.afbase.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.dao.AfCostMapper;
import com.efreight.afbase.dao.AfIncomeCostMapper;
import com.efreight.afbase.dao.AfIncomeMapper;
import com.efreight.afbase.dao.AfOrderMapper;
import com.efreight.afbase.dao.CssIncomeInvoiceDetailMapper;
import com.efreight.afbase.dao.RountingSignMapper;
import com.efreight.afbase.dao.ScCostMapper;
import com.efreight.afbase.dao.ScIncomeMapper;
import com.efreight.afbase.dao.TcCostMapper;
import com.efreight.afbase.dao.TcIncomeMapper;
import com.efreight.afbase.service.*;
import com.efreight.common.core.feign.RemoteCoopService;
import com.efreight.common.core.feign.RemoteServiceToSC;
import com.efreight.common.remoteVo.IoOrder;
import com.efreight.common.remoteVo.LcOrder;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.common.security.vo.CoopVo;

import lombok.AllArgsConstructor;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * AF 延伸服务 应收 服务实现类
 * </p>
 *
 * @author qipm
 * @since 2019-10-12
 */
@Service
@AllArgsConstructor
public class AfIncomeCostServiceImpl extends ServiceImpl<AfIncomeCostMapper, AfIncomeCost> implements AfIncomeCostService {
    private final LogService logService;
    private final ScLogService scLogService;
    private final AfOrderMapper afOrderMapper;
    private final AwbNumberService awbservice;
    private final AfIncomeMapper afIncomeMapper;
    private final AfCostMapper afCostMapper;
    private final ScOrderService scOrderService;
    private final ScIncomeMapper scIncomeMapper;
    private final ScCostMapper scCostMapper;
    private final AfOrderService afOrderService;
    private final TcOrderService tcOrderService;
    private final TcIncomeMapper tcIncomeMapper;
    private final TcLogService tcLogService;
    private final TcCostMapper tcCostMapper;
    private final RemoteServiceToSC remoteServiceToSC;
    private final DebitNoteService debitNoteService;
    private final CssPaymentService cssPaymentService;
    private final CssPaymentDetailService cssPaymentDetailService;
    private final RemoteCoopService remoteCoopService;
    private final RountingSignMapper rountingSignMapper;
    private final CssIncomeInvoiceDetailMapper detailMapper;

    @Override
    public List<AfIncomeCostTree> getListTree(Integer order_id, String businessScope) {
        ArrayList<AfIncomeCostTree> resultTree = new ArrayList<>();
        if ("AE".equals(businessScope) || "AI".equals(businessScope)) {
            //空运
            List<AfIncomeCostTree> list = baseMapper.getIncomeCostLst(SecurityUtils.getUser().getOrgId(), order_id);

            for (int i = 0; i < list.size(); i++) {
                AfIncomeCostTree tree = list.get(i);
                List<AfIncomeCost> children = baseMapper.getChildrenLst2(SecurityUtils.getUser().getOrgId(), tree.getIncomeId());
                tree.setChildren(children);
                resultTree.add(tree);
            }
        } else if ("SE".equals(businessScope) || "SI".equals(businessScope)) {
            //海运
            List<AfIncomeCostTree> list = baseMapper.getIncomeCostLstBySE(SecurityUtils.getUser().getOrgId(), order_id);

            for (int i = 0; i < list.size(); i++) {
                AfIncomeCostTree tree = list.get(i);
                List<AfIncomeCost> children = baseMapper.getChildrenLstBySE2(SecurityUtils.getUser().getOrgId(), tree.getIncomeId());
                tree.setChildren(children);
                resultTree.add(tree);
            }
        }
        //合计
        if (resultTree.size() > 0) {
            AfIncomeCostTree totalTree = new AfIncomeCostTree();
            BigDecimal incomeFunctionalAmount = new BigDecimal(0);
            BigDecimal costFunctionalAmount = new BigDecimal(0);
            BigDecimal profitAmount = new BigDecimal(0);
            BigDecimal noTaxProfitAmount = new BigDecimal(0);
            for (int i = 0; i < resultTree.size(); i++) {
                if (resultTree.get(i).getIncomeFunctionalAmount() != null) {
                    incomeFunctionalAmount = incomeFunctionalAmount.add(resultTree.get(i).getIncomeFunctionalAmount());
                }
                if (resultTree.get(i).getChildren() != null) {
//        			costFunctionalAmount=costFunctionalAmount.add(resultTree.get(i).getCostFunctionalAmount());
                    for (int j = 0; j < resultTree.get(i).getChildren().size(); j++) {
                        if (resultTree.get(i).getChildren().get(j).getCostFunctionalAmount() != null) {
                            costFunctionalAmount = costFunctionalAmount.add(resultTree.get(i).getChildren().get(j).getCostFunctionalAmount());
                        }

                    }
                }
                if (resultTree.get(i).getProfitAmount() != null) {
                    profitAmount = profitAmount.add(resultTree.get(i).getProfitAmount());
                }
                if (resultTree.get(i).getNoTaxProfitAmount() != null) {
                    noTaxProfitAmount = noTaxProfitAmount.add(resultTree.get(i).getNoTaxProfitAmount());
                }

            }
            totalTree.setIncomeFunctionalAmount(incomeFunctionalAmount);
            totalTree.setCostFunctionalAmount(costFunctionalAmount);
            totalTree.setProfitAmount(profitAmount);
            totalTree.setNoTaxProfitAmount(noTaxProfitAmount);
            totalTree.setTreeId("-1");
            totalTree.setServiceName("合计");
            resultTree.add(0, totalTree);
        }


        return resultTree;
    }

    @Override
    public IncomeCostList getIncomeCostList(AfIncome bean) {
        IncomeCostList resultList = new IncomeCostList();
        if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
            //空运
            resultList.setIncomeList(baseMapper.getIncomeList(SecurityUtils.getUser().getOrgId(), bean.getOrderId()));
            resultList.setCostList(baseMapper.getCostList(SecurityUtils.getUser().getOrgId(), bean.getOrderId()));
            if("AE".equals(bean.getBusinessScope())) {
            	//订单信息
    			AfOrder order = afOrderMapper.selectById(bean.getOrderId());
            	boolean flagSign = true;
        		Map map = rountingSignMapper.getOrgConfigForWhere(SecurityUtils.getUser().getOrgId(),"AE");
        		if(map!=null&&map.containsKey("rounting_sign")) {
        			if("false".equals(map.get("rounting_sign").toString())) {
        				//当前签约公司AE订单没有签单设置
        				flagSign = false;
        			}else {
        				//订单符合签单条件
            			if(order!=null&&StringUtils.isNotBlank(order.getBusinessProduct())&&(map.get("rounting_sign_business_product").toString().contains(order.getBusinessProduct()))) {
            				//当前签约公司设置的AE订单签单服务产品不包含当前订单的服务产品
            				flagSign = true;
            			}else {
            				flagSign = false;
            			}
        			}
        			//编辑才处理
//        			if(StringUtils.isNotEmpty(bean.getSignFlag())) {
//        			}
        		}else {
        			flagSign = false;
        		}
            	List<AfCost> costList = new ArrayList<AfCost>();
            	//查询签单费用
            	RountingSign signQuery = new RountingSign();
            	signQuery.setBusinessScope("AE");
            	signQuery.setOrgId(SecurityUtils.getUser().getOrgId());
            	signQuery.setOrderId(bean.getOrderId());
        		RountingSign sign = rountingSignMapper.getRountingSign(signQuery);
        		if(sign!=null&&flagSign&&sign.getSignState()==1) {
        			AfCost a = new AfCost();
        			a.setBusinessScope("AE");
        			a.setServiceId(-1);
        			a.setServiceName("干线 - 空运费");
        			 if(order.getAwbId()!=null) {
    		        	AwbNumber awbN = awbservice.getById(order.getAwbId());
    		        	a.setCustomerId(Integer.valueOf(awbN.getAwbFromId()));
    		        	a.setCustomerName(awbN.getAwbFromName());
        		     }
        			 a.setCostId(-1);
        			 a.setCostQuantity(new BigDecimal(sign.getIncomeWeight()));
        			 a.setCostUnitPrice(sign.getMsrUnitprice());
        			 a.setCostCurrency("CNY");
        			 a.setCostExchangeRate(new BigDecimal(1));
        			 a.setCostAmountNotTax(sign.getMsrFunctionalAmount());
        			 a.setCostAmountTax(BigDecimal.ZERO);
        			 a.setCostAmountTaxRate(new BigDecimal(1));
        			 a.setCostAmount(sign.getMsrFunctionalAmount());
        			 a.setCostFunctionalAmount(sign.getMsrFunctionalAmount());
        			 a.setOrderId(order.getOrderId());
        			 a.setOrderUuid(order.getOrderUuid());
        			 a.setOrgId(order.getOrgId());
        			 costList.add(a);
        		}
        		if(resultList.getCostList()!=null&&resultList.getCostList().size()>0) {
        			for(AfCost afCost:resultList.getCostList()) {
        				if(flagSign&&"干线 - 空运费".equals(afCost.getServiceName())) {
        					//todo
        				}else {
        					costList.add(afCost);
        				}
        			}
        		}
            	 resultList.setCostList(costList);
            }
        
        } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
            //海运
            resultList.setIncomeList(baseMapper.getIncomeListSE(SecurityUtils.getUser().getOrgId(), bean.getOrderId()));
            resultList.setCostList(baseMapper.getCostListSE(SecurityUtils.getUser().getOrgId(), bean.getOrderId()));
        } else if ("LC".equals(bean.getBusinessScope())) {
            //陆运
            resultList.setIncomeList(remoteServiceToSC.listLcIncome(bean.getOrderId()).getData().stream().map(lcIncome -> {
                DebitNote debitNote = debitNoteService.getById(lcIncome.getDebitNoteId());
                AfIncome afIncome = new AfIncome();
                BeanUtils.copyProperties(lcIncome, afIncome);
                if (debitNote != null) {
                    afIncome.setDebitNoteNum(debitNote.getDebitNoteNum());
                }
                return afIncome;
            }).collect(Collectors.toList()));
            resultList.setCostList(remoteServiceToSC.listLcCost(bean.getOrderId()).getData().stream().map(lcCost -> {
                LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                cssPaymentDetailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, lcCost.getCostId());
                List<CssPaymentDetail> cssPaymentDetails = cssPaymentDetailService.list(cssPaymentDetailWrapper);
                StringBuffer paymentNumBuffer = new StringBuffer();
                cssPaymentDetails.stream().forEach(cssPaymentDetail -> {
                    CssPayment cssPayment = cssPaymentService.getById(cssPaymentDetail.getPaymentId());
                    if ("LC".equals(cssPayment.getBusinessScope())) {
                        if (StrUtil.isBlank(paymentNumBuffer.toString())) {
                            paymentNumBuffer.append(cssPayment.getPaymentNum());
                        } else {
                            paymentNumBuffer.append(",").append(cssPayment.getPaymentNum());
                        }
                    }
                });
                AfCost afCost = new AfCost();
                BeanUtils.copyProperties(lcCost, afCost);
                afCost.setPaymentNum(paymentNumBuffer.toString());
                return afCost;
            }).collect(Collectors.toList()));
        } else if ("IO".equals(bean.getBusinessScope())) {
            //陆运
            resultList.setIncomeList(remoteServiceToSC.listIoIncome(bean.getOrderId()).getData().stream().map(ioIncome -> {
                DebitNote debitNote = debitNoteService.getById(ioIncome.getDebitNoteId());
                AfIncome afIncome = new AfIncome();
                BeanUtils.copyProperties(ioIncome, afIncome);
                if (debitNote != null) {
                    afIncome.setDebitNoteNum(debitNote.getDebitNoteNum());
                }
                return afIncome;
            }).collect(Collectors.toList()));
            resultList.setCostList(remoteServiceToSC.listIoCost(bean.getOrderId()).getData().stream().map(ioCost -> {
                LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                cssPaymentDetailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, ioCost.getCostId());
                List<CssPaymentDetail> cssPaymentDetails = cssPaymentDetailService.list(cssPaymentDetailWrapper);
                StringBuffer paymentNumBuffer = new StringBuffer();
                cssPaymentDetails.stream().forEach(cssPaymentDetail -> {
                    CssPayment cssPayment = cssPaymentService.getById(cssPaymentDetail.getPaymentId());
                    if ("IO".equals(cssPayment.getBusinessScope())) {
                        if (StrUtil.isBlank(paymentNumBuffer.toString())) {
                            paymentNumBuffer.append(cssPayment.getPaymentNum());
                        } else {
                            paymentNumBuffer.append(",").append(cssPayment.getPaymentNum());
                        }
                    }
                });
                AfCost afCost = new AfCost();
                BeanUtils.copyProperties(ioCost, afCost);
                afCost.setPaymentNum(paymentNumBuffer.toString());
                return afCost;
            }).collect(Collectors.toList()));
        } else if (bean.getBusinessScope().startsWith("T")) {
            //铁路
            resultList.setIncomeList(baseMapper.getIncomeListTE(SecurityUtils.getUser().getOrgId(), bean.getOrderId()));
            resultList.setCostList(baseMapper.getCostListTE(SecurityUtils.getUser().getOrgId(), bean.getOrderId()));
        }
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doEdit(IncomeCostList bean) {
        List<AfIncome> incomeList = bean.getIncomeList();
        List<AfCost> costList = bean.getCostList();

        List<AfIncome> incomeDeleteList = bean.getIncomeDeleteList();
        List<AfCost> costDeleteList = bean.getCostDeleteList();
        String businessScope = bean.getBusinessScope();
        if ("LC".equals(businessScope)) {
            LcOrder lcOrder = remoteServiceToSC.viewLcOrder(bean.getOrderId()).getData();
            if ("财务锁账".equals(lcOrder.getOrderStatus())) {
                throw new RuntimeException("已经做过财务锁账");
            }
        } else if ("IO".equals(businessScope)) {
            IoOrder ioOrder = remoteServiceToSC.viewIoOrder(bean.getOrderId()).getData();
            if ("财务锁账".equals(ioOrder.getOrderStatus())) {
                throw new RuntimeException("已经做过财务锁账");
            }
        } else {
            AfOrder order = new AfOrder();
            order.setBusinessScope(businessScope);
            order.setOrderUuid(bean.getOrderUuid());
            //财务锁账已做的 ，不能重复做
            List<Integer> list = afOrderService.getOrderStatus(order);
            if (list.size() > 0) {
                throw new RuntimeException("已经做过财务锁账");
            }
        }
        //修改订单费用状态
//        String income_status = "";
//        String cost_status = "";
//        if (incomeList.size() > 0) {
//            income_status = "已录收入";
//        } else {
//            income_status = "未录收入";
//        }
//        if (costList.size() > 0) {
//            cost_status = "已录成本";
//        } else {
//            cost_status = "未录成本";
//        }
        //日志
        LogBean logBean = new LogBean();
        logBean.setPageName("费用录入");
        logBean.setPageFunction("保存费用");

        logBean.setLogRemark("");
        logBean.setBusinessScope(businessScope);

        if ("AE".equals(businessScope) || "AI".equals(businessScope)) {
        	boolean flag = false;
        	if("AE".equals(businessScope)) {
        		//判断当前签约公司是否设置
        		Map map = rountingSignMapper.getOrgConfigForWhere(SecurityUtils.getUser().getOrgId(),"AE");
        		if(map!=null&&map.containsKey("rounting_sign")) {
        			AfOrder orderCheck = afOrderMapper.selectById(bean.getOrderId());
        			if("true".equals(map.get("rounting_sign").toString())) {
        				flag = true;
        			}
        			if(flag&&orderCheck!=null&&!StringUtils.isEmpty(orderCheck.getBusinessProduct())&&(map.get("rounting_sign_business_product").toString().contains(orderCheck.getBusinessProduct()))) {
        				flag = true;
        			}else {
        				flag = false;
        			}
        		}
        	}
            //修改订单费用状态
//            afOrderMapper.updateOrderIncomeStatus(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), income_status, UUID.randomUUID().toString());
//            afOrderMapper.updateOrderCostStatus(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), cost_status, UUID.randomUUID().toString());
            //日志
            AfOrder orderBean = afOrderMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
            logBean.setOrderNumber(orderBean.getOrderCode());
            logBean.setOrderId(orderBean.getOrderId());
            logBean.setOrderUuid(orderBean.getOrderUuid());
            logService.saveLog(logBean);
            //空运应收
//            int flag1 = 0;
//            int flag2 = 0;
            if (bean.getIncomeRecorded() != orderBean.getIncomeRecorded() || bean.getCostRecorded() != orderBean.getCostRecorded()) {
                throw new RuntimeException("订单不是最新数据，请刷新页面再操作");
            }
            for (int i = 0; i < incomeList.size(); i++) {
                AfIncome incomeBean = incomeList.get(i);

                if (incomeBean.getIncomeId() == null || "".equals(incomeBean.getIncomeId())) {
                    //增加
                    incomeBean.setRowUuid(UUID.randomUUID().toString());
                    incomeBean.setCreateTime(LocalDateTime.now());
                    incomeBean.setCreatorId(SecurityUtils.getUser().getId());
                    incomeBean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    incomeBean.setOrgId(SecurityUtils.getUser().getOrgId());
                    afIncomeMapper.insert(incomeBean);
//                    flag1 = 1;
                } else {
                    AfIncome bean2 = afIncomeMapper.selectById(incomeBean.getIncomeId());
                    if (!incomeBean.getRowUuid().equals(bean2.getRowUuid())) {
                        throw new RuntimeException("费用不是最新数据，请刷新页面再操作");
                    }
                    //修改
                    incomeBean.setRowUuid(UUID.randomUUID().toString());
                    incomeBean.setEditTime(LocalDateTime.now());
                    incomeBean.setEditorId(SecurityUtils.getUser().getId());
                    incomeBean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    UpdateWrapper<AfIncome> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("income_id", incomeBean.getIncomeId());
                    updateWrapper.isNull("debit_note_id");
                    updateWrapper.isNull("financial_date");
//					if (incomeBean.getDebitNoteId()==null || "".equals(incomeBean.getDebitNoteId())) {
                    afIncomeMapper.update(incomeBean, updateWrapper);
//					}
                }

            }
            //空运应付
            for (int i = 0; i < costList.size(); i++) {
                AfCost costBean = costList.get(i);
                if("AE".equals(businessScope)&&costBean.getCostId()!=null&&costBean.getCostId()==-1) {
                	continue;
                }
                if(flag&&"AE".equals(businessScope)&&"干线 - 空运费".equals(costBean.getServiceName())) {
                	continue;
                }
                if (costBean.getCostId() == null || "".equals(costBean.getCostId())) {
                    //增加
                    costBean.setRowUuid(UUID.randomUUID().toString());
                    costBean.setCreateTime(LocalDateTime.now());
                    costBean.setCreatorId(SecurityUtils.getUser().getId());
                    costBean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    costBean.setOrgId(SecurityUtils.getUser().getOrgId());
                    afCostMapper.insert(costBean);
//                    flag2 = 1;
                } else {
                    AfCost bean2 = afCostMapper.selectById(costBean.getCostId());
                    if (!costBean.getRowUuid().equals(bean2.getRowUuid())) {
                        throw new RuntimeException("费用不是最新数据，请刷新页面再操作");
                    }
                    //修改
                    costBean.setRowUuid(UUID.randomUUID().toString());
                    costBean.setEditTime(LocalDateTime.now());
                    costBean.setEditorId(SecurityUtils.getUser().getId());
                    costBean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    UpdateWrapper<AfCost> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("cost_id", costBean.getCostId());
                    updateWrapper.isNull("payment_id");
                    updateWrapper.isNull("financial_date");
                    afCostMapper.update(costBean, updateWrapper);
                }

            }
            for (int i = 0; i < incomeDeleteList.size(); i++) {
                baseMapper.deleteIncome(SecurityUtils.getUser().getOrgId(), incomeDeleteList.get(i).getIncomeId());
            }
            for (int i = 0; i < costDeleteList.size(); i++) {
                 if("AE".equals(businessScope)&&costDeleteList.get(i).getCostId()==-1) {
                 	continue;
                 }
                baseMapper.deleteCost(SecurityUtils.getUser().getOrgId(), costDeleteList.get(i).getCostId());
            }

            //修改订单成本状态（订单应收状态在最后面统一修改）
            orderBean.setRowUuid(UUID.randomUUID().toString());
            orderBean.setCostStatus(afOrderService.getOrderCostStatusForAF(bean.getOrderId()));
            afOrderService.updateById(orderBean);

//            if (flag1 == 1) {
//                afOrderMapper.updateOrderIncomeStatus4(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
//            } else if (incomeDeleteList.size() > 0) {
//                List<CssDebitNote> billList = afOrderMapper.getOrderBill(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
//
//                int incomeStatus = 1;
//                for (int i = 0; i < billList.size(); i++) {
//                    CssDebitNote bill = billList.get(i);
//                    if (bill.getWriteoffComplete() == null || bill.getWriteoffComplete() != 1) {
//                        incomeStatus = 0;
//                        break;
//                    }
//                }
//                List<Integer> icomeList = afOrderMapper.getOrderIcome(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
//                List<Integer> icomeList2 = afOrderMapper.getOrderIcome2(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
//                if (icomeList.size() == 0 && icomeList2.size() > 0 && incomeStatus == 1) {
//                    afOrderMapper.updateOrderIncomeStatus5(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
//                }
//            }
//            if (flag2 == 1) {
//                //含有新增成本
//                afOrderMapper.updateOrderCostStatus4(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
//            }else{
//                LambdaQueryWrapper<AfCost> afCostLambdaQueryWrapper = Wrappers.<AfCost>lambdaQuery();
//                afCostLambdaQueryWrapper.eq(AfCost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfCost::getOrderId, bean.getOrderId());
//                List<AfCost> list = afCostMapper.selectList(afCostLambdaQueryWrapper);
//                boolean ifCostCompleteWriteoff = true;
//                try {
//                    if (list.size() > 0) {
//                        list.stream().forEach(afCost -> {
//                            if (afCost.getCostAmountWriteoff() == null || afCost.getCostAmount().compareTo(afCost.getCostAmountWriteoff()) != 0 || afCost.getCostAmount().compareTo(BigDecimal.ZERO) == 0) {
//                                throw new RuntimeException();
//                            }
//                        });
//                    } else {
//                        ifCostCompleteWriteoff = false;
//                    }
//                } catch (Exception e) {
//                    ifCostCompleteWriteoff = false;
//                }
//                if (ifCostCompleteWriteoff) {
//                    AfOrder order = afOrderService.getById(bean.getOrderId());
//                    order.setCostStatus("核销完毕");
//                    order.setRowUuid(UUID.randomUUID().toString());
//                    afOrderService.updateById(order);
//                }
//            }


            //未启用签单
            if(!flag&&"AE".equals(businessScope)) {
            	RountingSign checkSign = new RountingSign();
            	checkSign.setBusinessScope(businessScope);
            	checkSign.setOrgId(SecurityUtils.getUser().getOrgId());
            	checkSign.setOrderId(bean.getOrderId());
            	RountingSign sign = rountingSignMapper.getRountingSign(checkSign);
            	if(sign==null) {
            		RountingSign ss = new RountingSign();
                	ss.setSignState(0);
        	    	ss.setBusinessScope("AE");
        	    	ss.setOrderId(bean.getOrderId());
        	    	ss.setOrgId(SecurityUtils.getUser().getOrgId());
        	    	ss.setRowUuid(UUID.randomUUID().toString());
        	    	//所有
        	    	RountingSign o = rountingSignMapper.getAfCostForWhere(ss);
        	    	if(o.getMsrUnitprice()!=null) {
        	    		ss.setMsrUnitprice(o.getMsrUnitprice());
        	    	}else {
        	    		ss.setMsrUnitprice(BigDecimal.ZERO);
        	    	}
        	    	ss.setMsrFunctionalAmount(ss.getMsrUnitprice());
        	    	ss.setIncomeWeight(1.0);
        	    	rountingSignMapper.insert(ss);
            	}else {
            		sign.setRowUuid(UUID.randomUUID().toString());
        	    	//所有
        	    	RountingSign o = rountingSignMapper.getAfCostForWhere(sign);
        	    	if(o!=null&&o.getMsrUnitprice()!=null) {
        	    		sign.setMsrUnitprice(o.getMsrUnitprice());
        	    	}else {
        	    		sign.setMsrUnitprice(BigDecimal.ZERO);
        	    	}
        	    	sign.setMsrFunctionalAmount(sign.getMsrUnitprice());
        	    	sign.setIncomeWeight(1.0);
        	    	sign.setEditorId(SecurityUtils.getUser().getId());
        			sign.setEditorName(SecurityUtils.getUser().buildOptName());
        			sign.setEditTime(LocalDateTime.now());
        	    	rountingSignMapper.updateById(sign);
            	}
            }
        } else if ("SE".equals(businessScope) || "SI".equals(businessScope)) {
            //修改订单费用状态
//            afOrderMapper.updateOrderIncomeStatusSE(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), income_status, UUID.randomUUID().toString());
//            afOrderMapper.updateOrderCostStatusSE(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), cost_status, UUID.randomUUID().toString());
            //日志
            AfOrder orderBean = afOrderMapper.getSEOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
            logBean.setOrderNumber(orderBean.getOrderCode());
            logBean.setOrderId(orderBean.getOrderId());
            logBean.setOrderUuid(orderBean.getOrderUuid());
            ScLog logBean2 = new ScLog();
            BeanUtils.copyProperties(logBean, logBean2);
            scLogService.saveLog(logBean2);
            //海运应收
//            int flag1 = 0;
//            int flag2 = 0;
            if (bean.getIncomeRecorded() != orderBean.getIncomeRecorded() || bean.getCostRecorded() != orderBean.getCostRecorded()) {
                throw new RuntimeException("订单不是最新数据，请刷新页面再操作");
            }
            for (int i = 0; i < incomeList.size(); i++) {
                AfIncome incomeBean2 = incomeList.get(i);
                ScIncome incomeBean = new ScIncome();
                BeanUtils.copyProperties(incomeBean2, incomeBean);
                if (incomeBean.getIncomeId() == null || "".equals(incomeBean.getIncomeId())) {
                    //增加
                    incomeBean.setRowUuid(UUID.randomUUID().toString());
                    incomeBean.setCreateTime(LocalDateTime.now());
                    incomeBean.setCreatorId(SecurityUtils.getUser().getId());
                    incomeBean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    incomeBean.setOrgId(SecurityUtils.getUser().getOrgId());
                    scIncomeMapper.insert(incomeBean);
//                    flag1 = 1;
                } else {
                    ScIncome bean2 = scIncomeMapper.selectById(incomeBean.getIncomeId());
                    if (!incomeBean.getRowUuid().equals(bean2.getRowUuid())) {
                        throw new RuntimeException("费用不是最新数据，请刷新页面再操作");
                    }
                    //修改
                    incomeBean.setRowUuid(UUID.randomUUID().toString());
                    incomeBean.setEditTime(LocalDateTime.now());
                    incomeBean.setEditorId(SecurityUtils.getUser().getId());
                    incomeBean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    UpdateWrapper<ScIncome> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("income_id", incomeBean.getIncomeId());
                    updateWrapper.isNull("debit_note_id");
                    updateWrapper.isNull("financial_date");
                    scIncomeMapper.update(incomeBean, updateWrapper);
                }

            }
            //海运应付
            for (int i = 0; i < costList.size(); i++) {
                AfCost costBean2 = costList.get(i);
                ScCost costBean = new ScCost();
                BeanUtils.copyProperties(costBean2, costBean);
                if (costBean.getCostId() == null || "".equals(costBean.getCostId())) {
                    //增加
                    costBean.setRowUuid(UUID.randomUUID().toString());
                    costBean.setCreateTime(LocalDateTime.now());
                    costBean.setCreatorId(SecurityUtils.getUser().getId());
                    costBean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    costBean.setOrgId(SecurityUtils.getUser().getOrgId());
                    scCostMapper.insert(costBean);
//                    flag2 = 1;
                } else {
                    ScCost bean2 = scCostMapper.selectById(costBean.getCostId());
                    if (!costBean.getRowUuid().equals(bean2.getRowUuid())) {
                        throw new RuntimeException("费用不是最新数据，请刷新页面再操作");
                    }
                    //修改
                    costBean.setRowUuid(UUID.randomUUID().toString());
                    costBean.setEditTime(LocalDateTime.now());
                    costBean.setEditorId(SecurityUtils.getUser().getId());
                    costBean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    UpdateWrapper<ScCost> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("cost_id", costBean.getCostId());
                    updateWrapper.isNull("payment_id");
                    updateWrapper.isNull("financial_date");
                    scCostMapper.update(costBean, updateWrapper);
                }

            }
            for (int i = 0; i < incomeDeleteList.size(); i++) {
                baseMapper.deleteIncomeSE(SecurityUtils.getUser().getOrgId(), incomeDeleteList.get(i).getIncomeId());
            }
            for (int i = 0; i < costDeleteList.size(); i++) {
                baseMapper.deleteCostSE(SecurityUtils.getUser().getOrgId(), costDeleteList.get(i).getCostId());
            }
//            if (flag1 == 1) {
//                afOrderMapper.updateOrderIncomeStatusSE4(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
//            } else if (incomeDeleteList.size() > 0) {
//                List<CssDebitNote> billList = afOrderMapper.getOrderBill(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
//
//                int incomeStatus = 1;
//                for (int i = 0; i < billList.size(); i++) {
//                    CssDebitNote bill = billList.get(i);
//                    if (bill.getWriteoffComplete() == null || bill.getWriteoffComplete() != 1) {
//                        incomeStatus = 0;
//                        break;
//                    }
//                }
//                List<Integer> icomeList = afOrderMapper.getSEOrderIcome(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
//                List<Integer> icomeList2 = afOrderMapper.getSEOrderIcome2(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
//                if (icomeList.size() == 0 && icomeList2.size() > 0 && incomeStatus == 1) {
//                    afOrderMapper.updateOrderIncomeStatusSE5(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
//                }
//            }
//            if (flag2 == 1) {
//                afOrderMapper.updateOrderCostStatusSE4(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
//            }else{
//                LambdaQueryWrapper<ScCost> scCostLambdaQueryWrapper = Wrappers.<ScCost>lambdaQuery();
//                scCostLambdaQueryWrapper.eq(ScCost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(ScCost::getOrderId, bean.getOrderId());
//                List<ScCost> list = scCostMapper.selectList(scCostLambdaQueryWrapper);
//                boolean ifCostCompleteWriteoff = true;
//                try {
//                    if (list.size() > 0) {
//                        list.stream().forEach(scCost -> {
//                            if (scCost.getCostAmountWriteoff() == null || scCost.getCostAmount().compareTo(scCost.getCostAmountWriteoff()) != 0 || scCost.getCostAmount().compareTo(BigDecimal.ZERO) == 0) {
//                                throw new RuntimeException();
//                            }
//                        });
//                    } else {
//                        ifCostCompleteWriteoff = false;
//                    }
//                } catch (Exception e) {
//                    ifCostCompleteWriteoff = false;
//                }
//                if (ifCostCompleteWriteoff) {
//                    ScOrder order = scOrderService.getById(bean.getOrderId());
//                    order.setCostStatus("核销完毕");
//                    order.setRowUuid(UUID.randomUUID().toString());
//                    scOrderService.updateById(order);
//                }
//            }
            //修改订单成本状态（订单应收状态在最后面统一修改）
            afOrderService.updateOrderCostStatusForSC(bean.getOrderId());
        } else if (bean.getBusinessScope().startsWith("T")) {
            //修改订单费用状态
//            afOrderMapper.updateOrderIncomeStatusTC(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), income_status, UUID.randomUUID().toString());
//            afOrderMapper.updateOrderCostStatusTC(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), cost_status, UUID.randomUUID().toString());
            //日志
            AfOrder orderBean = afOrderMapper.getTCOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
            logBean.setOrderNumber(orderBean.getOrderCode());
            logBean.setOrderId(orderBean.getOrderId());
            logBean.setOrderUuid(orderBean.getOrderUuid());
            TcLog logBean2 = new TcLog();
            BeanUtils.copyProperties(logBean, logBean2);
            logBean2.setCreatorId(SecurityUtils.getUser().getId());
            logBean2.setCreatorName(SecurityUtils.getUser().buildOptName());
            logBean2.setCreatTime(LocalDateTime.now());
            logBean2.setOrgId(SecurityUtils.getUser().getOrgId());
            tcLogService.save(logBean2);
            //海运应收
//            int flag1 = 0;
//            int flag2 = 0;
            if (bean.getIncomeRecorded() != orderBean.getIncomeRecorded() || bean.getCostRecorded() != orderBean.getCostRecorded()) {
                throw new RuntimeException("订单不是最新数据，请刷新页面再操作");
            }
            for (int i = 0; i < incomeList.size(); i++) {
                AfIncome incomeBean2 = incomeList.get(i);
                TcIncome incomeBean = new TcIncome();
                BeanUtils.copyProperties(incomeBean2, incomeBean);
                if (incomeBean.getIncomeId() == null || "".equals(incomeBean.getIncomeId())) {
                    //增加
                    incomeBean.setRowUuid(UUID.randomUUID().toString());
                    incomeBean.setCreateTime(LocalDateTime.now());
                    incomeBean.setCreatorId(SecurityUtils.getUser().getId());
                    incomeBean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    incomeBean.setOrgId(SecurityUtils.getUser().getOrgId());
                    tcIncomeMapper.insert(incomeBean);
//                    flag1 = 1;
                } else {
                    TcIncome bean2 = tcIncomeMapper.selectById(incomeBean.getIncomeId());
                    if (!incomeBean.getRowUuid().equals(bean2.getRowUuid())) {
                        throw new RuntimeException("费用不是最新数据，请刷新页面再操作");
                    }
                    //修改
                    incomeBean.setRowUuid(UUID.randomUUID().toString());
                    incomeBean.setEditTime(LocalDateTime.now());
                    incomeBean.setEditorId(SecurityUtils.getUser().getId());
                    incomeBean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    UpdateWrapper<TcIncome> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("income_id", incomeBean.getIncomeId());
                    updateWrapper.isNull("debit_note_id");
                    updateWrapper.isNull("financial_date");
                    tcIncomeMapper.update(incomeBean, updateWrapper);
                }

            }
            //铁路应付
            for (int i = 0; i < costList.size(); i++) {
                AfCost costBean2 = costList.get(i);
                TcCost costBean = new TcCost();
                BeanUtils.copyProperties(costBean2, costBean);
                if (costBean.getCostId() == null || "".equals(costBean.getCostId())) {
                    //增加
                    costBean.setRowUuid(UUID.randomUUID().toString());
                    costBean.setCreateTime(LocalDateTime.now());
                    costBean.setCreatorId(SecurityUtils.getUser().getId());
                    costBean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    costBean.setOrgId(SecurityUtils.getUser().getOrgId());
                    tcCostMapper.insert(costBean);
//                    flag2 = 1;
                } else {
                    TcCost bean2 = tcCostMapper.selectById(costBean.getCostId());
                    if (!costBean.getRowUuid().equals(bean2.getRowUuid())) {
                        throw new RuntimeException("费用不是最新数据，请刷新页面再操作");
                    }
                    //修改
                    costBean.setRowUuid(UUID.randomUUID().toString());
                    costBean.setEditTime(LocalDateTime.now());
                    costBean.setEditorId(SecurityUtils.getUser().getId());
                    costBean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    UpdateWrapper<TcCost> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("cost_id", costBean.getCostId());
                    updateWrapper.isNull("payment_id");
                    updateWrapper.isNull("financial_date");

                    tcCostMapper.update(costBean, updateWrapper);
                }

            }
            for (int i = 0; i < incomeDeleteList.size(); i++) {
                baseMapper.deleteIncomeTC(SecurityUtils.getUser().getOrgId(), incomeDeleteList.get(i).getIncomeId());
            }
            for (int i = 0; i < costDeleteList.size(); i++) {
                baseMapper.deleteCostTC(SecurityUtils.getUser().getOrgId(), costDeleteList.get(i).getCostId());
            }
//            if (flag1 == 1) {
//                afOrderMapper.updateOrderIncomeStatusTC4(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
//            } else if (incomeDeleteList.size() > 0) {
//                List<CssDebitNote> billList = afOrderMapper.getOrderBill(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
//
//                int incomeStatus = 1;
//                for (int i = 0; i < billList.size(); i++) {
//                    CssDebitNote bill = billList.get(i);
//                    if (bill.getWriteoffComplete() == null || bill.getWriteoffComplete() != 1) {
//                        incomeStatus = 0;
//                        break;
//                    }
//                }
//                List<Integer> icomeList = afOrderMapper.getTCOrderIcome(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
//                List<Integer> icomeList2 = afOrderMapper.getTCOrderIcome2(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
//                if (icomeList.size() == 0 && icomeList2.size() > 0 && incomeStatus == 1) {
//                    afOrderMapper.updateOrderIncomeStatusTC5(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
//                }
//            }
//            if (flag2 == 1) {
//                afOrderMapper.updateOrderCostStatusTC4(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
//            }else{
//                LambdaQueryWrapper<TcCost> tcCostLambdaQueryWrapper = Wrappers.<TcCost>lambdaQuery();
//                tcCostLambdaQueryWrapper.eq(TcCost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(TcCost::getOrderId, bean.getOrderId());
//                List<TcCost> list = tcCostMapper.selectList(tcCostLambdaQueryWrapper);
//                boolean ifCostCompleteWriteoff = true;
//                try {
//                    if (list.size() > 0) {
//                        list.stream().forEach(tcCost -> {
//                            if (tcCost.getCostAmountWriteoff() == null || tcCost.getCostAmount().compareTo(tcCost.getCostAmountWriteoff()) != 0 || tcCost.getCostAmount().compareTo(BigDecimal.ZERO) == 0) {
//                                throw new RuntimeException();
//                            }
//                        });
//                    } else {
//                        ifCostCompleteWriteoff = false;
//                    }
//                } catch (Exception e) {
//                    ifCostCompleteWriteoff = false;
//                }
//                if (ifCostCompleteWriteoff) {
//                    TcOrder order = tcOrderService.getById(bean.getOrderId());
//                    order.setCostStatus("核销完毕");
//                    order.setRowUuid(UUID.randomUUID().toString());
//                    tcOrderService.updateById(order);
//                }
//            }
            //修改订单成本状态（订单应收状态在最后面统一修改）
            afOrderService.updateOrderCostStatusForTC(bean.getOrderId());
        } else if ("LC".equals(businessScope)) {
            //修改LC订单费用
            com.efreight.common.remoteVo.IncomeCostList incomeCostList = new com.efreight.common.remoteVo.IncomeCostList();
            BeanUtils.copyProperties(bean, incomeCostList);
            MessageInfo messageInfo = remoteServiceToSC.saveOrderIncomeAndCost(incomeCostList);
            if (messageInfo.getCode() == 1) {
                throw new RuntimeException(messageInfo.getMessageInfo());
            }
        } else if ("IO".equals(businessScope)) {
            //修改IO订单费用
            com.efreight.common.remoteVo.IncomeCostList incomeCostList = new com.efreight.common.remoteVo.IncomeCostList();
            BeanUtils.copyProperties(bean, incomeCostList);
            MessageInfo messageInfo = remoteServiceToSC.saveIoOrderIncomeAndCost(incomeCostList);
            if (messageInfo.getCode() == 1) {
                throw new RuntimeException(messageInfo.getMessageInfo());
            }
        }
        if(!"IO".equals(businessScope)&&!"LC".equals(businessScope)) {
        	//更新订单应收状态：（order. income_status）
    		List<Map> listMap = detailMapper.getOrderIncomeStatus(SecurityUtils.getUser().getOrgId(), bean.getOrderId().toString(),businessScope);
    		if(listMap!=null&&listMap.size()>0) {
    			for(Map map:listMap) {
    				detailMapper.updateOrderIncomeStatus(Integer.valueOf(map.get("org_id").toString()),Integer.valueOf(map.get("order_id").toString()),map.get("income_status").toString(),UUID.randomUUID().toString(),businessScope);
    			}
    		}
        }
        //日志
        return true;
    }

    @Override
    public List<AfIncome> addIncomeTemplate(AfIncome bean) {
        List<AfIncome> resultList = new ArrayList<AfIncome>();
        if ("1".equals(bean.getTemplateCode())) {
            resultList = baseMapper.getIncomeTemplate(SecurityUtils.getUser().getOrgId(), bean.getBusinessScope());
        } else {
            resultList = baseMapper.getIncomeTemplate2(SecurityUtils.getUser().getOrgId(), bean.getBusinessScope(), 1, bean.getTemplateCode());
        }

        List<AfIncome> resultList2 = new ArrayList<AfIncome>();
        if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
            //空运
            AfOrder order = baseMapper.getOrder(SecurityUtils.getUser().getOrgId(), bean.getOrderId());
            if (order != null) {
                for (int i = 0; i < resultList.size(); i++) {
                    AfIncome income = resultList.get(i);
                    income.setOrderId(bean.getOrderId());
                    income.setOrderUuid(bean.getOrderUuid());
                    income.setBusinessScope(bean.getBusinessScope());
                    if ("1".equals(bean.getTemplateCode())) {
                        income.setCustomerId(bean.getCustomerId());
                        income.setCustomerName(bean.getCustomerName());
                    } else {
                        if (income.getCustomerId() == null) {
                            income.setCustomerId(bean.getCustomerId());
                            income.setCustomerName(bean.getCustomerName());
                        }

                    }

                    if ("计重".equals(income.getDebitNoteNum())) {
                        if (order.getConfirmChargeWeight() != null) {
                            income.setIncomeQuantity(new BigDecimal(order.getConfirmChargeWeight()));
                        } else if (order.getPlanChargeWeight() != null) {
                            income.setIncomeQuantity(new BigDecimal(order.getPlanChargeWeight()));
                        }
                    } else if ("毛重".equals(income.getDebitNoteNum())) {
                        if (order.getConfirmWeight() != null) {
                            income.setIncomeQuantity(order.getConfirmWeight());
                        } else if (order.getPlanWeight() != null) {
                            income.setIncomeQuantity(order.getPlanWeight());
                        }
                    } else if ("件数".equals(income.getDebitNoteNum())) {
                        if (order.getConfirmPieces() != null) {
                            income.setIncomeQuantity(new BigDecimal(order.getConfirmPieces()));
                        } else if (order.getPlanPieces() != null) {
                            income.setIncomeQuantity(new BigDecimal(order.getPlanPieces()));
                        }
                    } else if ("订单".equals(income.getDebitNoteNum())) {
                        income.setIncomeQuantity(new BigDecimal(1));
                    } else if ("分单".equals(income.getDebitNoteNum())) {
                        if (order.getHawbQuantity() != null) {
                            income.setIncomeQuantity(new BigDecimal(order.getHawbQuantity()));
                        }
                    }

//                    if(income.getIncomeQuantity()!=null&&income.getIncomeQuantity().compareTo(new BigDecimal(1))==0){
//                    	//最低 价
//                        if (income.getServiceAmountMin() != null) {
//                            if (income.getIncomeUnitPrice()!=null) {
//                            	if(income.getServiceAmountMin().compareTo(income.getIncomeUnitPrice()) > 0) {
//                            		income.setIncomeUnitPrice(income.getServiceAmountMin());
//                            	}
//                            }else {
//                            	income.setIncomeUnitPrice(income.getServiceAmountMin());
//                            }
//                        }
//                        //最高 价
//                        if (income.getServiceAmountMax() != null) {
//                        	if(income.getIncomeUnitPrice()!=null) {
//                        		if (income.getServiceAmountMax().compareTo(income.getIncomeUnitPrice()) < 0) {
//                                    income.setIncomeUnitPrice(income.getServiceAmountMax());
//                                }
//                        	}else {
//                        		income.setIncomeUnitPrice(income.getServiceAmountMax());
//                        	}
//                        }
//                    }


                    if (income.getIncomeQuantity() != null && income.getIncomeUnitPrice() != null) {
                        income.setIncomeAmount(income.getIncomeQuantity().multiply(income.getIncomeUnitPrice()));
                    } else {
                        income.setIncomeAmount(new BigDecimal(0));
                    }
                    if (income.getIncomeQuantity() != null && income.getIncomeUnitPrice() != null && income.getIncomeExchangeRate() != null) {
                        income.setIncomeFunctionalAmount(income.getIncomeQuantity().multiply(income.getIncomeUnitPrice().multiply(income.getIncomeExchangeRate())));
                    } else {
                        income.setIncomeFunctionalAmount(new BigDecimal(0));
                    }

                    //最低 价
                    if (income.getServiceAmountMin() != null) {
                        if (income.getServiceAmountMin().compareTo(income.getIncomeAmount()) > 0) {
                            income.setIncomeAmount(income.getServiceAmountMin());
                            income.setIncomeQuantity(new BigDecimal(1));
                            income.setIncomeUnitPrice(income.getServiceAmountMin());
                            if (income.getIncomeExchangeRate() != null) {
                                income.setIncomeFunctionalAmount(income.getIncomeAmount().multiply(income.getIncomeExchangeRate()));
                            } else {
                                income.setIncomeFunctionalAmount(new BigDecimal(0));
                            }
                        }
                    }
                    //最高 价
                    if (income.getServiceAmountMax() != null) {
                        if (income.getServiceAmountMax().compareTo(income.getIncomeAmount()) < 0) {
                            income.setIncomeAmount(income.getServiceAmountMax());
                            income.setIncomeQuantity(new BigDecimal(1));
                            income.setIncomeUnitPrice(income.getServiceAmountMax());
                            if (income.getIncomeExchangeRate() != null) {
                                income.setIncomeFunctionalAmount(income.getIncomeAmount().multiply(income.getIncomeExchangeRate()));
                            } else {
                                income.setIncomeFunctionalAmount(new BigDecimal(0));
                            }
                        }
                    }
                    //小数位进位
                    if ("四舍五入".equals(income.getServiceAmountCarry())) {
                        income.setIncomeAmount(income.getIncomeAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));
                        income.setIncomeFunctionalAmount(income.getIncomeFunctionalAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));
                    }
                    if ("向上进位".equals(income.getServiceAmountCarry())) {
                        income.setIncomeAmount(income.getIncomeAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                        income.setIncomeFunctionalAmount(income.getIncomeFunctionalAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                    }
                }
                if (order.getFreightUnitprice() != null || order.getFreightAmount() != null) {
                    if ("AE".equals(bean.getBusinessScope())) {
                        AfIncome income2 = baseMapper.getIncome(SecurityUtils.getUser().getOrgId(), bean.getBusinessScope());
                        income2.setOrderId(bean.getOrderId());
                        income2.setOrderUuid(bean.getOrderUuid());
                        income2.setBusinessScope(bean.getBusinessScope());

                        if ("1".equals(bean.getTemplateCode())) {
                            income2.setCustomerId(bean.getCustomerId());
                            income2.setCustomerName(bean.getCustomerName());
                        } else {
                            if (income2.getCustomerId() == null) {
                                income2.setCustomerId(bean.getCustomerId());
                                income2.setCustomerName(bean.getCustomerName());
                            }

                        }
                        if ("代操作".equals(order.getBusinessProduct())) {
                            income2.setIncomeExchangeRate(order.getIncomeExchangeRate());
                            income2.setIncomeCurrency(order.getCurrecnyCode());
                            if (order.getFreightUnitprice() != null && order.getFreightUnitprice() > 0) {
                                if (order.getConfirmChargeWeight() != null) {
                                    income2.setIncomeQuantity(new BigDecimal(order.getConfirmChargeWeight()));
                                } else if (order.getPlanChargeWeight() != null) {
                                    income2.setIncomeQuantity(new BigDecimal(order.getPlanChargeWeight()));
                                }
                                //							income2.setIncomeQuantity(new BigDecimal(order.getFreightUnitprice()));
                                income2.setIncomeUnitPrice(new BigDecimal(order.getFreightUnitprice()));
                                resultList2.add(income2);
                            } else if (order.getFreightAmount() != null && order.getFreightAmount() > 0) {
                                income2.setIncomeQuantity(new BigDecimal(1));
                                income2.setIncomeUnitPrice(new BigDecimal(order.getFreightAmount()));
                                resultList2.add(income2);
                            }
                        } else {
                            income2.setIncomeExchangeRate(order.getIncomeExchangeRate());
                            income2.setIncomeCurrency(order.getCurrecnyCode());
                            if (order.getFreightUnitprice() != null) {
                                if (order.getConfirmChargeWeight() != null) {
                                    income2.setIncomeQuantity(new BigDecimal(order.getConfirmChargeWeight()));
                                } else if (order.getPlanChargeWeight() != null) {
                                    income2.setIncomeQuantity(new BigDecimal(order.getPlanChargeWeight()));
                                }
                                //							income2.setIncomeQuantity(new BigDecimal(order.getFreightUnitprice()));
                                income2.setIncomeUnitPrice(new BigDecimal(order.getFreightUnitprice()));
                            } else if (order.getFreightAmount() != null) {
                                income2.setIncomeQuantity(new BigDecimal(1));
                                income2.setIncomeUnitPrice(new BigDecimal(order.getFreightAmount()));
                            }
                            resultList2.add(income2);
                        }
                        if (income2.getIncomeQuantity() != null && income2.getIncomeUnitPrice() != null) {
                            income2.setIncomeAmount(income2.getIncomeQuantity().multiply(income2.getIncomeUnitPrice()));
                        } else {
                            income2.setIncomeAmount(new BigDecimal(0));
                        }
                        if (income2.getIncomeQuantity() != null && income2.getIncomeUnitPrice() != null && income2.getIncomeExchangeRate() != null) {
                            income2.setIncomeFunctionalAmount(income2.getIncomeQuantity().multiply(income2.getIncomeUnitPrice().multiply(income2.getIncomeExchangeRate())));
                        } else {
                            income2.setIncomeFunctionalAmount(new BigDecimal(0));
                        }

                        //最低 价
                        if (income2.getServiceAmountMin() != null) {
                            if (income2.getServiceAmountMin().compareTo(income2.getIncomeAmount()) > 0) {
                                income2.setIncomeAmount(income2.getServiceAmountMin());
                                income2.setIncomeQuantity(new BigDecimal(1));
                                income2.setIncomeUnitPrice(income2.getServiceAmountMin());
                                if (income2.getIncomeExchangeRate() != null) {
                                    income2.setIncomeFunctionalAmount(income2.getIncomeAmount().multiply(income2.getIncomeExchangeRate()));
                                } else {
                                    income2.setIncomeFunctionalAmount(new BigDecimal(0));
                                }
                            }
                        }
                        //最高 价
                        if (income2.getServiceAmountMax() != null) {
                            if (income2.getServiceAmountMax().compareTo(income2.getIncomeAmount()) < 0) {
                                income2.setIncomeAmount(income2.getServiceAmountMax());
                                income2.setIncomeQuantity(new BigDecimal(1));
                                income2.setIncomeUnitPrice(income2.getServiceAmountMax());
                                if (income2.getIncomeExchangeRate() != null) {
                                    income2.setIncomeFunctionalAmount(income2.getIncomeAmount().multiply(income2.getIncomeExchangeRate()));
                                } else {
                                    income2.setIncomeFunctionalAmount(new BigDecimal(0));
                                }
                            }
                        }
                        //小数位进位
                        if ("四舍五入".equals(income2.getServiceAmountCarry())) {
                            income2.setIncomeAmount(income2.getIncomeAmount().setScale(income2.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));
                            income2.setIncomeFunctionalAmount(income2.getIncomeFunctionalAmount().setScale(income2.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));
                        }
                        if ("向上进位".equals(income2.getServiceAmountCarry())) {
                            income2.setIncomeAmount(income2.getIncomeAmount().setScale(income2.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                            income2.setIncomeFunctionalAmount(income2.getIncomeFunctionalAmount().setScale(income2.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                        }

                    }
                }
            }
        } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
        	//预先处理20GP 40GP 40HQ
        	AfIncome income20gp = new AfIncome();
        	List<AfIncome> list20gp = baseMapper.getServiceStandard(SecurityUtils.getUser().getOrgId(), bean.getOrderId(),"sc","20GP");
        	if(list20gp!=null&&list20gp.size()==1) {
        		income20gp = list20gp.get(0);
        	}else {
        		income20gp.setOrderId(bean.getOrderId());
        		income20gp.setIncomeQuantity(BigDecimal.ZERO);
        	}
        	AfIncome income40gp = new AfIncome();;
        	List<AfIncome> list40gp = baseMapper.getServiceStandard(SecurityUtils.getUser().getOrgId(), bean.getOrderId(),"sc","40GP");
        	if(list40gp!=null&&list40gp.size()==1) {
        		income40gp = list40gp.get(0);
        	}else {
        		income40gp.setOrderId(bean.getOrderId());
        		income40gp.setIncomeQuantity(BigDecimal.ZERO);
        	}
        	AfIncome income40hq = new AfIncome();;
        	List<AfIncome> list40hq = baseMapper.getServiceStandard(SecurityUtils.getUser().getOrgId(), bean.getOrderId(),"sc","40HQ");
        	if(list40hq!=null&&list40hq.size()==1) {
        		income40hq = list40hq.get(0);
        	}else {
        		income40hq.setOrderId(bean.getOrderId());
        		income40hq.setIncomeQuantity(BigDecimal.ZERO);
        	}
            //海运
            ScOrder order = baseMapper.getOrder2(SecurityUtils.getUser().getOrgId(), bean.getOrderId());
            if (order != null) {
                for (int i = 0; i < resultList.size(); i++) {
                    AfIncome income = resultList.get(i);
                    income.setOrderId(bean.getOrderId());
                    income.setOrderUuid(bean.getOrderUuid());
                    income.setBusinessScope(bean.getBusinessScope());

                    if ("1".equals(bean.getTemplateCode())) {
                        income.setCustomerId(bean.getCustomerId());
                        income.setCustomerName(bean.getCustomerName());
                    } else {
                        if (income.getCustomerId() == null) {
                            income.setCustomerId(bean.getCustomerId());
                            income.setCustomerName(bean.getCustomerName());
                        }
                    }
                    if ("毛重".equals(income.getDebitNoteNum())) {
                        if (order.getPlanWeight() != null) {
                            income.setIncomeQuantity(order.getPlanWeight());
                        }
                    } else if ("件数".equals(income.getDebitNoteNum())) {
                        if (order.getPlanPieces() != null) {
                            income.setIncomeQuantity(new BigDecimal(order.getPlanPieces()));
                        }
                    } else if ("订单".equals(income.getDebitNoteNum())) {
                        income.setIncomeQuantity(new BigDecimal(1));
                    } else if ("计费吨".equals(income.getDebitNoteNum())) {
                        if (order.getPlanChargeWeight() != null) {
                            income.setIncomeQuantity(order.getPlanChargeWeight());
                        }
                    } else if ("标箱".equals(income.getDebitNoteNum())) {
                        if (order.getContainerNumber() != null) {
                            income.setIncomeQuantity(new BigDecimal(order.getContainerNumber()));
                        }
                    }else if("体积".equals(income.getDebitNoteNum())){
                    	if (order.getPlanVolume() != null) {
                            income.setIncomeQuantity(order.getPlanVolume());
                        }
                    }else if("20GP".equals(income.getDebitNoteNum())) {
                    	income.setIncomeQuantity(income20gp.getIncomeQuantity());
                    }else if("40GP".equals(income.getDebitNoteNum())) {
                    	income.setIncomeQuantity(income40gp.getIncomeQuantity());
                    }else if("40HQ".equals(income.getDebitNoteNum())) {
                    	income.setIncomeQuantity(income40hq.getIncomeQuantity());
                    }else {
                    	//特殊需求  动态处理
                    	if(!StringUtils.isBlank(income.getDebitNoteNum())) {
                    		String str = income.getDebitNoteNum().substring(0, 2);
                    		Pattern pattern = Pattern.compile("^(\\-|\\+)?\\d+(\\.\\d+)?$");  
                            if(pattern.matcher(str).matches()){
                            	List<AfIncome> listStr = baseMapper.getServiceStandard(SecurityUtils.getUser().getOrgId(), bean.getOrderId(),"sc",income.getDebitNoteNum());
                            	if(listStr!=null&&listStr.size()==1) {
                            		income.setIncomeQuantity(listStr.get(0).getIncomeQuantity());
                            	}else {
                            		income.setIncomeQuantity(BigDecimal.ZERO);
                            	}
                            }
                    	}
                    	 
                    }
                    
                    if (income.getIncomeQuantity() != null && income.getIncomeUnitPrice() != null) {
                        income.setIncomeAmount(income.getIncomeQuantity().multiply(income.getIncomeUnitPrice()));
                    } else {
                        income.setIncomeAmount(new BigDecimal(0));
                    }
                    if (income.getIncomeQuantity() != null && income.getIncomeUnitPrice() != null && income.getIncomeExchangeRate() != null) {
                        income.setIncomeFunctionalAmount(income.getIncomeQuantity().multiply(income.getIncomeUnitPrice().multiply(income.getIncomeExchangeRate())));
                    } else {
                        income.setIncomeFunctionalAmount(new BigDecimal(0));
                    }
                    //最低 价
                    if (income.getServiceAmountMin() != null) {
                        if (income.getServiceAmountMin().compareTo(income.getIncomeAmount()) > 0) {
                            income.setIncomeAmount(income.getServiceAmountMin());
                            income.setIncomeQuantity(new BigDecimal(1));
                            income.setIncomeUnitPrice(income.getServiceAmountMin());
                            if (income.getIncomeExchangeRate() != null) {
                                income.setIncomeFunctionalAmount(income.getIncomeAmount().multiply(income.getIncomeExchangeRate()));
                            } else {
                                income.setIncomeFunctionalAmount(new BigDecimal(0));
                            }
                        }
                    }
                    //最高 价
                    if (income.getServiceAmountMax() != null) {
                        if (income.getServiceAmountMax().compareTo(income.getIncomeAmount()) < 0) {
                            income.setIncomeAmount(income.getServiceAmountMax());
                            income.setIncomeQuantity(new BigDecimal(1));
                            income.setIncomeUnitPrice(income.getServiceAmountMax());
                            if (income.getIncomeExchangeRate() != null) {
                                income.setIncomeFunctionalAmount(income.getIncomeAmount().multiply(income.getIncomeExchangeRate()));
                            } else {
                                income.setIncomeFunctionalAmount(new BigDecimal(0));
                            }
                        }
                    }
                    //小数位进位
                    if ("四舍五入".equals(income.getServiceAmountCarry())) {
                        income.setIncomeAmount(income.getIncomeAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));
                        income.setIncomeFunctionalAmount(income.getIncomeFunctionalAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));
                    }
                    if ("向上进位".equals(income.getServiceAmountCarry())) {
                        income.setIncomeAmount(income.getIncomeAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                        income.setIncomeFunctionalAmount(income.getIncomeFunctionalAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                    }
                }
            }
        } else if (bean.getBusinessScope().startsWith("T")) {
        	//预先处理20GP 40GP 40HQ
        	AfIncome income20gp = new AfIncome();;
        	List<AfIncome> list20gp = baseMapper.getServiceStandard(SecurityUtils.getUser().getOrgId(), bean.getOrderId(),"tc","20GP");
        	if(list20gp!=null&&list20gp.size()==1) {
        		income20gp = list20gp.get(0);
        	}else {
        		income20gp.setOrderId(bean.getOrderId());
        		income20gp.setIncomeQuantity(BigDecimal.ZERO);
        	}
        	AfIncome income40gp = new AfIncome();;
        	List<AfIncome> list40gp = baseMapper.getServiceStandard(SecurityUtils.getUser().getOrgId(), bean.getOrderId(),"tc","40GP");
        	if(list40gp!=null&&list40gp.size()==1) {
        		income40gp = list40gp.get(0);
        	}else {
        		income40gp.setOrderId(bean.getOrderId());
        		income40gp.setIncomeQuantity(BigDecimal.ZERO);
        	}
        	AfIncome income40hq = new AfIncome();;
        	List<AfIncome> list40hq = baseMapper.getServiceStandard(SecurityUtils.getUser().getOrgId(), bean.getOrderId(),"tc","40HQ");
        	if(list40hq!=null&&list40hq.size()==1) {
        		income40hq = list40hq.get(0);
        	}else {
        		income40hq.setOrderId(bean.getOrderId());
        		income40hq.setIncomeQuantity(BigDecimal.ZERO);
        	}
            //铁路
            TcOrder order = baseMapper.getOrderTC(SecurityUtils.getUser().getOrgId(), bean.getOrderId());
            if (order != null) {
                for (int i = 0; i < resultList.size(); i++) {
                    AfIncome income = resultList.get(i);
                    income.setOrderId(bean.getOrderId());
                    income.setOrderUuid(bean.getOrderUuid());
                    income.setBusinessScope(bean.getBusinessScope());

                    if ("1".equals(bean.getTemplateCode())) {
                        income.setCustomerId(bean.getCustomerId());
                        income.setCustomerName(bean.getCustomerName());
                    } else {
                        if (income.getCustomerId() == null) {
                            income.setCustomerId(bean.getCustomerId());
                            income.setCustomerName(bean.getCustomerName());
                        }
                    }
                    if ("毛重".equals(income.getDebitNoteNum())) {
                        if (order.getPlanWeight() != null) {
                            income.setIncomeQuantity(order.getPlanWeight());
                        }
                    } else if ("件数".equals(income.getDebitNoteNum())) {
                        if (order.getPlanPieces() != null) {
                            income.setIncomeQuantity(new BigDecimal(order.getPlanPieces()));
                        }
                    } else if ("订单".equals(income.getDebitNoteNum())) {
                        income.setIncomeQuantity(new BigDecimal(1));
                    } else if ("计费吨".equals(income.getDebitNoteNum())) {
                        if (order.getPlanChargeWeight() != null) {
                            income.setIncomeQuantity(order.getPlanChargeWeight());
                        }
                    }else if("体积".equals(income.getDebitNoteNum())){
                    	if (order.getPlanVolume() != null) {
                            income.setIncomeQuantity(order.getPlanVolume());
                        }
                    } else if ("标箱".equals(income.getDebitNoteNum())) {
                        if (order.getContainerNumber() != null) {
                            income.setIncomeQuantity(new BigDecimal(order.getContainerNumber()));
                        }
                    }else if("20GP".equals(income.getDebitNoteNum())) {
                    	income.setIncomeQuantity(income20gp.getIncomeQuantity());
                    }else if("40GP".equals(income.getDebitNoteNum())) {
                    	income.setIncomeQuantity(income40gp.getIncomeQuantity());
                    }else if("40HQ".equals(income.getDebitNoteNum())) {
                    	income.setIncomeQuantity(income40hq.getIncomeQuantity());
                    }else {
                    	//特殊需求  动态处理
                    	if(!StringUtils.isBlank(income.getDebitNoteNum())) {
                    		String str = income.getDebitNoteNum().substring(0, 2);
                    		Pattern pattern = Pattern.compile("^(\\-|\\+)?\\d+(\\.\\d+)?$");  
                            if(pattern.matcher(str).matches()){
                            	List<AfIncome> listStr = baseMapper.getServiceStandard(SecurityUtils.getUser().getOrgId(), bean.getOrderId(),"tc",income.getDebitNoteNum());
                            	if(listStr!=null&&listStr.size()==1) {
                            		income.setIncomeQuantity(listStr.get(0).getIncomeQuantity());
                            	}else {
                            		income.setIncomeQuantity(BigDecimal.ZERO);
                            	}
                            }
                    	}
                    	 
                    }
                    if (income.getIncomeQuantity() != null && income.getIncomeUnitPrice() != null) {
                        income.setIncomeAmount(income.getIncomeQuantity().multiply(income.getIncomeUnitPrice()));
                    } else {
                        income.setIncomeAmount(new BigDecimal(0));
                    }
                    if (income.getIncomeQuantity() != null && income.getIncomeUnitPrice() != null && income.getIncomeExchangeRate() != null) {
                        income.setIncomeFunctionalAmount(income.getIncomeQuantity().multiply(income.getIncomeUnitPrice().multiply(income.getIncomeExchangeRate())));
                    } else {
                        income.setIncomeFunctionalAmount(new BigDecimal(0));
                    }
                    //最低 价
                    if (income.getServiceAmountMin() != null) {
                        if (income.getServiceAmountMin().compareTo(income.getIncomeAmount()) > 0) {
                            income.setIncomeAmount(income.getServiceAmountMin());
                            income.setIncomeQuantity(new BigDecimal(1));
                            income.setIncomeUnitPrice(income.getServiceAmountMin());
                            if (income.getIncomeExchangeRate() != null) {
                                income.setIncomeFunctionalAmount(income.getIncomeAmount().multiply(income.getIncomeExchangeRate()));
                            } else {
                                income.setIncomeFunctionalAmount(new BigDecimal(0));
                            }
                        }
                    }
                    //最高 价
                    if (income.getServiceAmountMax() != null) {
                        if (income.getServiceAmountMax().compareTo(income.getIncomeAmount()) < 0) {
                            income.setIncomeAmount(income.getServiceAmountMax());
                            income.setIncomeQuantity(new BigDecimal(1));
                            income.setIncomeUnitPrice(income.getServiceAmountMax());
                            if (income.getIncomeExchangeRate() != null) {
                                income.setIncomeFunctionalAmount(income.getIncomeAmount().multiply(income.getIncomeExchangeRate()));
                            } else {
                                income.setIncomeFunctionalAmount(new BigDecimal(0));
                            }
                        }
                    }
                    //小数位进位
                    if ("四舍五入".equals(income.getServiceAmountCarry())) {
                        income.setIncomeAmount(income.getIncomeAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));
                        income.setIncomeFunctionalAmount(income.getIncomeFunctionalAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));
                    }
                    if ("向上进位".equals(income.getServiceAmountCarry())) {
                        income.setIncomeAmount(income.getIncomeAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                        income.setIncomeFunctionalAmount(income.getIncomeFunctionalAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                    }
                }
            }

        } else if ("LC".equals(bean.getBusinessScope())) {
            //陆运
            LcOrder order = remoteServiceToSC.viewLcOrder(bean.getOrderId()).getData();
            if (order != null) {
                for (int i = 0; i < resultList.size(); i++) {
                    AfIncome income = resultList.get(i);
                    income.setOrderId(bean.getOrderId());
                    income.setOrderUuid(bean.getOrderUuid());
                    income.setBusinessScope(bean.getBusinessScope());

                    if ("1".equals(bean.getTemplateCode())) {
                        income.setCustomerId(bean.getCustomerId());
                        income.setCustomerName(bean.getCustomerName());
                    } else {
                        if (income.getCustomerId() == null) {
                            income.setCustomerId(bean.getCustomerId());
                            income.setCustomerName(bean.getCustomerName());
                        }
                    }
                    if ("毛重".equals(income.getDebitNoteNum())) {
                        if (order.getPlanWeight() != null) {
                            income.setIncomeQuantity(order.getPlanWeight());
                        }
                    } else if ("件数".equals(income.getDebitNoteNum())) {
                        if (order.getPlanPieces() != null) {
                            income.setIncomeQuantity(new BigDecimal(order.getPlanPieces()));
                        }
                    } else if ("订单".equals(income.getDebitNoteNum())) {
                        income.setIncomeQuantity(new BigDecimal(1));
                    } else if ("计重".equals(income.getDebitNoteNum())) {
                        if (order.getPlanChargeWeight() != null) {
                            income.setIncomeQuantity(order.getPlanChargeWeight());
                        }
                    }
                    if (income.getIncomeQuantity() != null && income.getIncomeUnitPrice() != null) {
                        income.setIncomeAmount(income.getIncomeQuantity().multiply(income.getIncomeUnitPrice()));
                    } else {
                        income.setIncomeAmount(new BigDecimal(0));
                    }
                    if (income.getIncomeQuantity() != null && income.getIncomeUnitPrice() != null && income.getIncomeExchangeRate() != null) {
                        income.setIncomeFunctionalAmount(income.getIncomeQuantity().multiply(income.getIncomeUnitPrice().multiply(income.getIncomeExchangeRate())));
                    } else {
                        income.setIncomeFunctionalAmount(new BigDecimal(0));
                    }
                    //最低 价
                    if (income.getServiceAmountMin() != null) {
                        if (income.getServiceAmountMin().compareTo(income.getIncomeAmount()) > 0) {
                            income.setIncomeAmount(income.getServiceAmountMin());
                            income.setIncomeQuantity(new BigDecimal(1));
                            income.setIncomeUnitPrice(income.getServiceAmountMin());
                            if (income.getIncomeExchangeRate() != null) {
                                income.setIncomeFunctionalAmount(income.getIncomeAmount().multiply(income.getIncomeExchangeRate()));
                            } else {
                                income.setIncomeFunctionalAmount(new BigDecimal(0));
                            }
                        }
                    }
                    //最高 价
                    if (income.getServiceAmountMax() != null) {
                        if (income.getServiceAmountMax().compareTo(income.getIncomeAmount()) < 0) {
                            income.setIncomeAmount(income.getServiceAmountMax());
                            income.setIncomeQuantity(new BigDecimal(1));
                            income.setIncomeUnitPrice(income.getServiceAmountMax());
                            if (income.getIncomeExchangeRate() != null) {
                                income.setIncomeFunctionalAmount(income.getIncomeAmount().multiply(income.getIncomeExchangeRate()));
                            } else {
                                income.setIncomeFunctionalAmount(new BigDecimal(0));
                            }
                        }
                    }
                    //小数位进位
                    if ("四舍五入".equals(income.getServiceAmountCarry())) {
                        income.setIncomeAmount(income.getIncomeAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));
                        income.setIncomeFunctionalAmount(income.getIncomeFunctionalAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));
                    }
                    if ("向上进位".equals(income.getServiceAmountCarry())) {
                        income.setIncomeAmount(income.getIncomeAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                        income.setIncomeFunctionalAmount(income.getIncomeFunctionalAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                    }
                }
            }

        } else if ("IO".equals(bean.getBusinessScope())) {
            //其他业务
            IoOrder order = remoteServiceToSC.viewIoOrder(bean.getOrderId()).getData();
            if (order != null) {
                for (int i = 0; i < resultList.size(); i++) {
                    AfIncome income = resultList.get(i);
                    income.setOrderId(bean.getOrderId());
                    income.setOrderUuid(bean.getOrderUuid());
                    income.setBusinessScope(bean.getBusinessScope());

                    if ("1".equals(bean.getTemplateCode())) {
                        income.setCustomerId(bean.getCustomerId());
                        income.setCustomerName(bean.getCustomerName());
                    } else {
                        if (income.getCustomerId() == null) {
                            income.setCustomerId(bean.getCustomerId());
                            income.setCustomerName(bean.getCustomerName());
                        }
                    }
                    if ("毛重".equals(income.getDebitNoteNum())) {
                        if (order.getPlanWeight() != null) {
                            income.setIncomeQuantity(order.getPlanWeight());
                        }
                    } else if ("件数".equals(income.getDebitNoteNum())) {
                        if (order.getPlanPieces() != null) {
                            income.setIncomeQuantity(new BigDecimal(order.getPlanPieces()));
                        }
                    } else if ("订单".equals(income.getDebitNoteNum())) {
                        income.setIncomeQuantity(new BigDecimal(1));
                    } else if ("计重".equals(income.getDebitNoteNum())) {
                        if (order.getPlanChargeWeight() != null) {
                            income.setIncomeQuantity(order.getPlanChargeWeight());
                        }
                    }
                    if (income.getIncomeQuantity() != null && income.getIncomeUnitPrice() != null) {
                        income.setIncomeAmount(income.getIncomeQuantity().multiply(income.getIncomeUnitPrice()));
                    } else {
                        income.setIncomeAmount(new BigDecimal(0));
                    }
                    if (income.getIncomeQuantity() != null && income.getIncomeUnitPrice() != null && income.getIncomeExchangeRate() != null) {
                        income.setIncomeFunctionalAmount(income.getIncomeQuantity().multiply(income.getIncomeUnitPrice().multiply(income.getIncomeExchangeRate())));
                    } else {
                        income.setIncomeFunctionalAmount(new BigDecimal(0));
                    }
                    //最低 价
                    if (income.getServiceAmountMin() != null) {
                        if (income.getServiceAmountMin().compareTo(income.getIncomeAmount()) > 0) {
                            income.setIncomeAmount(income.getServiceAmountMin());
                            income.setIncomeQuantity(new BigDecimal(1));
                            income.setIncomeUnitPrice(income.getServiceAmountMin());
                            if (income.getIncomeExchangeRate() != null) {
                                income.setIncomeFunctionalAmount(income.getIncomeAmount().multiply(income.getIncomeExchangeRate()));
                            } else {
                                income.setIncomeFunctionalAmount(new BigDecimal(0));
                            }
                        }
                    }
                    //最高 价
                    if (income.getServiceAmountMax() != null) {
                        if (income.getServiceAmountMax().compareTo(income.getIncomeAmount()) < 0) {
                            income.setIncomeAmount(income.getServiceAmountMax());
                            income.setIncomeQuantity(new BigDecimal(1));
                            income.setIncomeUnitPrice(income.getServiceAmountMax());
                            if (income.getIncomeExchangeRate() != null) {
                                income.setIncomeFunctionalAmount(income.getIncomeAmount().multiply(income.getIncomeExchangeRate()));
                            } else {
                                income.setIncomeFunctionalAmount(new BigDecimal(0));
                            }
                        }
                    }
                    //小数位进位
                    if ("四舍五入".equals(income.getServiceAmountCarry())) {
                        income.setIncomeAmount(income.getIncomeAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));
                        income.setIncomeFunctionalAmount(income.getIncomeFunctionalAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));
                    }
                    if ("向上进位".equals(income.getServiceAmountCarry())) {
                        income.setIncomeAmount(income.getIncomeAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                        income.setIncomeFunctionalAmount(income.getIncomeFunctionalAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                    }
                }
            }

        }
        resultList2.addAll(resultList);
        return resultList2;
    }

    @Override
    public List<AfCost> addCostTemplate(AfIncome bean) {
        List<AfCost> resultList = new ArrayList<AfCost>();
        if ("1".equals(bean.getTemplateCode())) {
            resultList = baseMapper.getCostTemplate(SecurityUtils.getUser().getOrgId(), bean.getBusinessScope());
        } else {
            resultList = baseMapper.getCostTemplate2(SecurityUtils.getUser().getOrgId(), bean.getBusinessScope(), 0, bean.getTemplateCode());
        }
        if("AE".equals(bean.getBusinessScope())) {
        	//判断当前签约公司是否设置
    		Map map = rountingSignMapper.getOrgConfigForWhere(SecurityUtils.getUser().getOrgId(),"AE");
    		boolean flag = false;
    		if(map!=null&&map.containsKey("rounting_sign")) {
    			AfOrder orderCheck = afOrderMapper.selectById(bean.getOrderId());
    			if("true".equals(map.get("rounting_sign").toString())) {
    				if(orderCheck!=null&&StringUtils.isNotBlank(orderCheck.getBusinessProduct())&&(map.get("rounting_sign_business_product").toString().contains(orderCheck.getBusinessProduct()))) {
        				flag = true;
        			}
    			}
    		}
    		List<AfCost> resultListNew = new ArrayList<AfCost>();
    		if(flag&&resultList!=null&&resultList.size()>0) {
    			for(AfCost ac:resultList) {
    				if("干线 - 空运费".equals(ac.getServiceName())) {
    					//todo
    				}else {
    					resultListNew.add(ac);
    				}
    			}
    			resultList = resultListNew;
    		}
        }
        List<AfCost> resultList2 = new ArrayList<AfCost>();
        if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
            //空运
            AfOrder order = baseMapper.getOrder(SecurityUtils.getUser().getOrgId(), bean.getOrderId());
            if (order != null) {
                for (int i = 0; i < resultList.size(); i++) {
                    AfCost income = resultList.get(i);
                    income.setOrderId(bean.getOrderId());
                    income.setOrderUuid(bean.getOrderUuid());
                    income.setBusinessScope(bean.getBusinessScope());
//					if ("AE".equals(bean.getBusinessScope()) && "干线 - 空运费".equals(income.getServiceName())) {
//						income.setCustomerId(order.getAwbFromId());
//						income.setCustomerName(order.getAwbFromName());
//					}
                    if ("1".equals(bean.getTemplateCode())) {
                        if (bean.getCustomerId() != -1) {
                            income.setCustomerId(bean.getCustomerId());
                            income.setCustomerName(bean.getCustomerName());
                        }
                    } else {
                        if (income.getCustomerId() == null) {
                            income.setCustomerId(bean.getCustomerId());
                            income.setCustomerName(bean.getCustomerName());
                        }
                    }

                    if ("计重".equals(income.getDebitNoteNum())) {
                        if (order.getConfirmChargeWeight() != null) {
                            income.setCostQuantity(new BigDecimal(order.getConfirmChargeWeight()));
                        } else if (order.getPlanChargeWeight() != null) {
                            income.setCostQuantity(new BigDecimal(order.getPlanChargeWeight()));
                        }
                    } else if ("毛重".equals(income.getDebitNoteNum())) {
                        if (order.getConfirmWeight() != null) {
                            income.setCostQuantity(order.getConfirmWeight());
                        } else if (order.getPlanWeight() != null) {
                            income.setCostQuantity(order.getPlanWeight());
                        }
                    } else if ("件数".equals(income.getDebitNoteNum())) {
                        if (order.getConfirmPieces() != null) {
                            income.setCostQuantity(new BigDecimal(order.getConfirmPieces()));
                        } else if (order.getPlanPieces() != null) {
                            income.setCostQuantity(new BigDecimal(order.getPlanPieces()));
                        }
                    } else if ("订单".equals(income.getDebitNoteNum())) {
                        income.setCostQuantity(new BigDecimal(1));
                    } else if ("分单".equals(income.getDebitNoteNum())) {
                        if (order.getHawbQuantity() != null) {
                            income.setCostQuantity(new BigDecimal(order.getHawbQuantity()));
                        }
                    }
                    if (income.getCostQuantity() != null && income.getCostUnitPrice() != null) {
                        income.setCostAmount(income.getCostQuantity().multiply(income.getCostUnitPrice()));
                    } else {
                        income.setCostAmount(new BigDecimal(0));
                    }
                    if (income.getCostQuantity() != null && income.getCostUnitPrice() != null && income.getCostExchangeRate() != null) {
                        income.setCostFunctionalAmount(income.getCostQuantity().multiply(income.getCostUnitPrice().multiply(income.getCostExchangeRate())));
                    } else {
                        income.setCostFunctionalAmount(new BigDecimal(0));
                    }
                    //最低 价
                    if (income.getServiceAmountMin() != null) {
                        if (income.getServiceAmountMin().compareTo(income.getCostAmount()) > 0) {
                            income.setCostAmount(income.getServiceAmountMin());
                            income.setCostQuantity(new BigDecimal(1));
                            income.setCostUnitPrice(income.getServiceAmountMin());
                            if (income.getCostExchangeRate() != null) {
                                income.setCostFunctionalAmount(income.getCostAmount().multiply(income.getCostExchangeRate()));
                            } else {
                                income.setCostFunctionalAmount(new BigDecimal(0));
                            }
                        }
                    }
                    //最高 价
                    if (income.getServiceAmountMax() != null) {
                        if (income.getServiceAmountMax().compareTo(income.getCostAmount()) < 0) {
                            income.setCostAmount(income.getServiceAmountMax());
                            income.setCostQuantity(new BigDecimal(1));
                            income.setCostUnitPrice(income.getServiceAmountMax());
                            if (income.getCostExchangeRate() != null) {
                                income.setCostFunctionalAmount(income.getCostAmount().multiply(income.getCostExchangeRate()));
                            } else {
                                income.setCostFunctionalAmount(new BigDecimal(0));
                            }
                        }
                    }
                    //小数位进位
                    if ("四舍五入".equals(income.getServiceAmountCarry())) {
                        income.setCostAmount(income.getCostAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));
                        income.setCostFunctionalAmount(income.getCostFunctionalAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));
                    }
                    if ("向上进位".equals(income.getServiceAmountCarry())) {
                        income.setCostAmount(income.getCostAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                        income.setCostFunctionalAmount(income.getCostFunctionalAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                    }
                }
                if (order.getFreightUnitprice() != null || order.getFreightAmount() != null) {
                    if ("AE".equals(bean.getBusinessScope())) {
                        AfCost cost2 = baseMapper.getCost(SecurityUtils.getUser().getOrgId(), bean.getBusinessScope());
                        cost2.setOrderId(bean.getOrderId());
                        cost2.setOrderUuid(bean.getOrderUuid());
                        cost2.setBusinessScope(bean.getBusinessScope());
//						if (bean.getCustomerId()!=-1) {
//							cost2.setCustomerId(bean.getCustomerId());
//							cost2.setCustomerName(bean.getCustomerName());
//						}
                        if ("1".equals(bean.getTemplateCode())) {
                            if (bean.getCustomerId() != -1) {
                                cost2.setCustomerId(bean.getCustomerId());
                                cost2.setCustomerName(bean.getCustomerName());
                            }
                        } else {
                            if (cost2.getCustomerId() == null) {
                                cost2.setCustomerId(bean.getCustomerId());
                                cost2.setCustomerName(bean.getCustomerName());
                            }
                        }
                        if ("AE".equals(bean.getBusinessScope()) && "干线 - 空运费".equals(cost2.getServiceName())) {
                            cost2.setCustomerId(order.getAwbFromId());
                            cost2.setCustomerName(order.getAwbFromName());
                        }
                        if ("代操作".equals(order.getBusinessProduct())) {
                            cost2.setCostExchangeRate(order.getCostExchangeRate());
                            cost2.setCostCurrency(order.getMsrCurrecnyCode());
                            if (order.getMsrUnitprice() != null && order.getMsrUnitprice() > 0) {
                                if (order.getConfirmChargeWeight() != null) {
                                    cost2.setCostQuantity(new BigDecimal(order.getConfirmChargeWeight()));
                                } else if (order.getPlanChargeWeight() != null) {
                                    cost2.setCostQuantity(new BigDecimal(order.getPlanChargeWeight()));
                                }
                                //							cost2.setCostQuantity(new BigDecimal(order.getMsrUnitprice()));
                                cost2.setCostUnitPrice(new BigDecimal(order.getMsrUnitprice()));
                                resultList2.add(cost2);
                            } else if (order.getMsrAmount() != null && order.getMsrAmount() > 0) {
                                cost2.setCostQuantity(new BigDecimal(1));
                                cost2.setCostUnitPrice(new BigDecimal(order.getMsrAmount()));
                                resultList2.add(cost2);
                            }

                        } else {
                            cost2.setCostExchangeRate(order.getCostExchangeRate());
                            cost2.setCostCurrency(order.getMsrCurrecnyCode());
                            if (order.getMsrUnitprice() != null) {
                                if (order.getConfirmChargeWeight() != null) {
                                    cost2.setCostQuantity(new BigDecimal(order.getConfirmChargeWeight()));
                                } else if (order.getPlanChargeWeight() != null) {
                                    cost2.setCostQuantity(new BigDecimal(order.getPlanChargeWeight()));
                                }
                                //							cost2.setCostQuantity(new BigDecimal(order.getMsrUnitprice()));
                                cost2.setCostUnitPrice(new BigDecimal(order.getMsrUnitprice()));
                            } else if (order.getMsrAmount() != null) {
                                cost2.setCostQuantity(new BigDecimal(1));
                                cost2.setCostUnitPrice(new BigDecimal(order.getMsrAmount()));
                            }
                            resultList2.add(cost2);
                        }
                        if (cost2.getCostQuantity() != null && cost2.getCostUnitPrice() != null) {
                            cost2.setCostAmount(cost2.getCostQuantity().multiply(cost2.getCostUnitPrice()));
                        } else {
                            cost2.setCostAmount(new BigDecimal(0));
                        }
                        if (cost2.getCostQuantity() != null && cost2.getCostUnitPrice() != null && cost2.getCostExchangeRate() != null) {
                            cost2.setCostFunctionalAmount(cost2.getCostQuantity().multiply(cost2.getCostUnitPrice().multiply(cost2.getCostExchangeRate())));
                        } else {
                            cost2.setCostFunctionalAmount(new BigDecimal(0));
                        }

                        //最低 价
                        if (cost2.getServiceAmountMin() != null) {
                            if (cost2.getServiceAmountMin().compareTo(cost2.getCostAmount()) > 0) {
                                cost2.setCostAmount(cost2.getServiceAmountMin());
                                cost2.setCostQuantity(new BigDecimal(1));
                                cost2.setCostUnitPrice(cost2.getServiceAmountMin());
                                if (cost2.getCostExchangeRate() != null) {
                                    cost2.setCostFunctionalAmount(cost2.getCostAmount().multiply(cost2.getCostExchangeRate()));
                                } else {
                                    cost2.setCostFunctionalAmount(new BigDecimal(0));
                                }
                            }
                        }
                        //最高 价
                        if (cost2.getServiceAmountMax() != null) {
                            if (cost2.getServiceAmountMax().compareTo(cost2.getCostAmount()) < 0) {
                                cost2.setCostAmount(cost2.getServiceAmountMax());
                                cost2.setCostQuantity(new BigDecimal(1));
                                cost2.setCostUnitPrice(cost2.getServiceAmountMax());
                                if (cost2.getCostExchangeRate() != null) {
                                    cost2.setCostFunctionalAmount(cost2.getCostAmount().multiply(cost2.getCostExchangeRate()));
                                } else {
                                    cost2.setCostFunctionalAmount(new BigDecimal(0));
                                }
                            }
                        }
                        //小数位进位
                        if ("四舍五入".equals(cost2.getServiceAmountCarry())) {
                            cost2.setCostAmount(cost2.getCostAmount().setScale(cost2.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));
                            cost2.setCostFunctionalAmount(cost2.getCostFunctionalAmount().setScale(cost2.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));
                        }
                        if ("向上进位".equals(cost2.getServiceAmountCarry())) {
                            cost2.setCostAmount(cost2.getCostAmount().setScale(cost2.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                            cost2.setCostFunctionalAmount(cost2.getCostFunctionalAmount().setScale(cost2.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                        }
                    }
                }
            }
        } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
        	//预先处理20GP 40GP 40HQ
        	AfIncome income20gp = new AfIncome();;
        	List<AfIncome> list20gp = baseMapper.getServiceStandard(SecurityUtils.getUser().getOrgId(), bean.getOrderId(),"sc","20GP");
        	if(list20gp!=null&&list20gp.size()==1) {
        		income20gp = list20gp.get(0);
        	}else {
        		income20gp.setOrderId(bean.getOrderId());
        		income20gp.setIncomeQuantity(BigDecimal.ZERO);
        	}
        	AfIncome income40gp = new AfIncome();;
        	List<AfIncome> list40gp = baseMapper.getServiceStandard(SecurityUtils.getUser().getOrgId(), bean.getOrderId(),"sc","40GP");
        	if(list40gp!=null&&list40gp.size()==1) {
        		income40gp = list40gp.get(0);
        	}else {
        		income40gp.setOrderId(bean.getOrderId());
        		income40gp.setIncomeQuantity(BigDecimal.ZERO);
        	}
        	AfIncome income40hq = new AfIncome();;
        	List<AfIncome> list40hq = baseMapper.getServiceStandard(SecurityUtils.getUser().getOrgId(), bean.getOrderId(),"sc","40HQ");
        	if(list40hq!=null&&list40hq.size()==1) {
        		income40hq = list40hq.get(0);
        	}else {
        		income40hq.setOrderId(bean.getOrderId());
        		income40hq.setIncomeQuantity(BigDecimal.ZERO);
        	}
            //海运
            ScOrder order = baseMapper.getOrder2(SecurityUtils.getUser().getOrgId(), bean.getOrderId());
            if (order != null) {
                for (int i = 0; i < resultList.size(); i++) {
                    AfCost income = resultList.get(i);
                    income.setOrderId(bean.getOrderId());
                    income.setOrderUuid(bean.getOrderUuid());
                    income.setBusinessScope(bean.getBusinessScope());
//					if (bean.getCustomerId()!=-1) {
//						income.setCustomerId(bean.getCustomerId());
//						income.setCustomerName(bean.getCustomerName());
//					}
                    if ("1".equals(bean.getTemplateCode())) {
                        if (bean.getCustomerId() != -1) {
                            income.setCustomerId(bean.getCustomerId());
                            income.setCustomerName(bean.getCustomerName());
                        }
                    } else {
                        if (income.getCustomerId() == null) {
                            income.setCustomerId(bean.getCustomerId());
                            income.setCustomerName(bean.getCustomerName());
                        }
                    }
                    if("SE".equals(bean.getBusinessScope())) {
                    	if((income.getCustomerId()==null||(income.getCustomerId()!=null&&income.getCustomerId()==-1))&&order.getBookingAgentId()!=null) {
                    		CoopVo coopVo = remoteCoopService.viewCoop(order.getBookingAgentId().toString()).getData();
                            if (coopVo != null) {
                            	income.setCustomerId(order.getBookingAgentId());
                                income.setCustomerName(coopVo.getCoop_name());
                            }
                    	}
                    }
                    if ("毛重".equals(income.getDebitNoteNum())) {
                        if (order.getPlanWeight() != null) {
                            income.setCostQuantity(order.getPlanWeight());
                        }
                    } else if ("件数".equals(income.getDebitNoteNum())) {
                        if (order.getPlanPieces() != null) {
                            income.setCostQuantity(new BigDecimal(order.getPlanPieces()));
                        }
                    } else if ("订单".equals(income.getDebitNoteNum())) {
                        income.setCostQuantity(new BigDecimal(1));
                    } else if ("计费吨".equals(income.getDebitNoteNum())) {
                        if (order.getPlanChargeWeight() != null) {
                            income.setCostQuantity(order.getPlanChargeWeight());
                        }
                    }else if("体积".equals(income.getDebitNoteNum())){
                    	if (order.getPlanVolume() != null) {
                            income.setCostQuantity(order.getPlanVolume());
                        }
                    } else if ("标箱".equals(income.getDebitNoteNum())) {
                        if (order.getContainerNumber() != null) {
                            income.setCostQuantity(new BigDecimal(order.getContainerNumber()));
                        }
                    }else if("20GP".equals(income.getDebitNoteNum())) {
                    	income.setCostQuantity(income20gp.getIncomeQuantity());
                    }else if("40GP".equals(income.getDebitNoteNum())) {
                    	income.setCostQuantity(income40gp.getIncomeQuantity());
                    }else if("40HQ".equals(income.getDebitNoteNum())) {
                    	income.setCostQuantity(income40hq.getIncomeQuantity());
                    }else {
                    	//特殊需求  动态处理
                    	if(!StringUtils.isBlank(income.getDebitNoteNum())) {
                    		String str = income.getDebitNoteNum().substring(0, 2);
                    		Pattern pattern = Pattern.compile("^(\\-|\\+)?\\d+(\\.\\d+)?$");  
                            if(pattern.matcher(str).matches()){
                            	List<AfIncome> listStr = baseMapper.getServiceStandard(SecurityUtils.getUser().getOrgId(), bean.getOrderId(),"sc",income.getDebitNoteNum());
                            	if(listStr!=null&&listStr.size()==1) {
                            		income.setCostQuantity(listStr.get(0).getIncomeQuantity());
                            	}else {
                            		income.setCostQuantity(BigDecimal.ZERO);
                            	}
                            }
                    	}
                    	 
                    }
                    if (income.getCostQuantity() != null && income.getCostUnitPrice() != null) {
                        income.setCostAmount(income.getCostQuantity().multiply(income.getCostUnitPrice()));
                    } else {
                        income.setCostAmount(new BigDecimal(0));
                    }
                    if (income.getCostQuantity() != null && income.getCostUnitPrice() != null && income.getCostExchangeRate() != null) {
                        income.setCostFunctionalAmount(income.getCostQuantity().multiply(income.getCostUnitPrice().multiply(income.getCostExchangeRate())));
                    } else {
                        income.setCostFunctionalAmount(new BigDecimal(0));
                    }

                    //最低 价
                    if (income.getServiceAmountMin() != null) {
                        if (income.getServiceAmountMin().compareTo(income.getCostAmount()) > 0) {
                            income.setCostAmount(income.getServiceAmountMin());
                            income.setCostQuantity(new BigDecimal(1));
                            income.setCostUnitPrice(income.getServiceAmountMin());
                            if (income.getCostExchangeRate() != null) {
                                income.setCostFunctionalAmount(income.getCostAmount().multiply(income.getCostExchangeRate()));
                            } else {
                                income.setCostFunctionalAmount(new BigDecimal(0));
                            }
                        }
                    }
                    //最高 价
                    if (income.getServiceAmountMax() != null) {
                        if (income.getServiceAmountMax().compareTo(income.getCostAmount()) < 0) {
                            income.setCostAmount(income.getServiceAmountMax());
                            income.setCostQuantity(new BigDecimal(1));
                            income.setCostUnitPrice(income.getServiceAmountMax());
                            if (income.getCostExchangeRate() != null) {
                                income.setCostFunctionalAmount(income.getCostAmount().multiply(income.getCostExchangeRate()));
                            } else {
                                income.setCostFunctionalAmount(new BigDecimal(0));
                            }
                        }
                    }
                    //小数位进位
                    if ("四舍五入".equals(income.getServiceAmountCarry())) {
                        income.setCostAmount(income.getCostAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));
                        income.setCostFunctionalAmount(income.getCostFunctionalAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));

                    }
                    if ("向上进位".equals(income.getServiceAmountCarry())) {
                        income.setCostAmount(income.getCostAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                        income.setCostFunctionalAmount(income.getCostFunctionalAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                    }
                }
            }
        } else if (bean.getBusinessScope().startsWith("T")) {
        	//预先处理20GP 40GP 40HQ
        	AfIncome income20gp = new AfIncome();;
        	List<AfIncome> list20gp = baseMapper.getServiceStandard(SecurityUtils.getUser().getOrgId(), bean.getOrderId(),"tc","20GP");
        	if(list20gp!=null&&list20gp.size()==1) {
        		income20gp = list20gp.get(0);
        	}else {
        		income20gp.setOrderId(bean.getOrderId());
        		income20gp.setIncomeQuantity(BigDecimal.ZERO);
        	}
        	AfIncome income40gp = new AfIncome();;
        	List<AfIncome> list40gp = baseMapper.getServiceStandard(SecurityUtils.getUser().getOrgId(), bean.getOrderId(),"tc","40GP");
        	if(list40gp!=null&&list40gp.size()==1) {
        		income40gp = list40gp.get(0);
        	}else {
        		income40gp.setOrderId(bean.getOrderId());
        		income40gp.setIncomeQuantity(BigDecimal.ZERO);
        	}
        	AfIncome income40hq = new AfIncome();;
        	List<AfIncome> list40hq = baseMapper.getServiceStandard(SecurityUtils.getUser().getOrgId(), bean.getOrderId(),"tc","40HQ");
        	if(list40hq!=null&&list40hq.size()==1) {
        		income40hq = list40hq.get(0);
        	}else {
        		income40hq.setOrderId(bean.getOrderId());
        		income40hq.setIncomeQuantity(BigDecimal.ZERO);
        	}
            //铁路
            TcOrder order = baseMapper.getOrderTC(SecurityUtils.getUser().getOrgId(), bean.getOrderId());
            if (order != null) {
                for (int i = 0; i < resultList.size(); i++) {
                    AfCost income = resultList.get(i);
                    income.setOrderId(bean.getOrderId());
                    income.setOrderUuid(bean.getOrderUuid());
                    income.setBusinessScope(bean.getBusinessScope());
                    if ("1".equals(bean.getTemplateCode())) {
                        if (bean.getCustomerId() != -1) {
                            income.setCustomerId(bean.getCustomerId());
                            income.setCustomerName(bean.getCustomerName());
                        }
                    } else {
                        if (income.getCustomerId() == null) {
                            income.setCustomerId(bean.getCustomerId());
                            income.setCustomerName(bean.getCustomerName());
                        }
                    }
                    if ("毛重".equals(income.getDebitNoteNum())) {
                        if (order.getPlanWeight() != null) {
                            income.setCostQuantity(order.getPlanWeight());
                        }
                    } else if ("件数".equals(income.getDebitNoteNum())) {
                        if (order.getPlanPieces() != null) {
                            income.setCostQuantity(new BigDecimal(order.getPlanPieces()));
                        }
                    } else if ("订单".equals(income.getDebitNoteNum())) {
                        income.setCostQuantity(new BigDecimal(1));
                    } else if ("计费吨".equals(income.getDebitNoteNum())) {
                        if (order.getPlanChargeWeight() != null) {
                            income.setCostQuantity(order.getPlanChargeWeight());
                        }
                    }else if("体积".equals(income.getDebitNoteNum())){
                    	if (order.getPlanVolume() != null) {
                            income.setCostQuantity(order.getPlanVolume());
                        }
                    }else if ("标箱".equals(income.getDebitNoteNum())) {
                        if (order.getContainerNumber() != null) {
                            income.setCostQuantity(new BigDecimal(order.getContainerNumber()));
                        }
                    }else if("20GP".equals(income.getDebitNoteNum())) {
                    	income.setCostQuantity(income20gp.getIncomeQuantity());
                    }else if("40GP".equals(income.getDebitNoteNum())) {
                    	income.setCostQuantity(income40gp.getIncomeQuantity());
                    }else if("40HQ".equals(income.getDebitNoteNum())) {
                    	income.setCostQuantity(income40hq.getIncomeQuantity());
                    }else {
                    	//特殊需求  动态处理
                    	if(!StringUtils.isBlank(income.getDebitNoteNum())) {
                    		String str = income.getDebitNoteNum().substring(0, 2);
                    		Pattern pattern = Pattern.compile("^(\\-|\\+)?\\d+(\\.\\d+)?$");  
                            if(pattern.matcher(str).matches()){
                            	List<AfIncome> listStr = baseMapper.getServiceStandard(SecurityUtils.getUser().getOrgId(), bean.getOrderId(),"tc",income.getDebitNoteNum());
                            	if(listStr!=null&&listStr.size()==1) {
                            		income.setCostQuantity(listStr.get(0).getIncomeQuantity());
                            	}else {
                            		income.setCostQuantity(BigDecimal.ZERO);
                            	}
                            }
                    	}
                    	 
                    }
                    if (income.getCostQuantity() != null && income.getCostUnitPrice() != null) {
                        income.setCostAmount(income.getCostQuantity().multiply(income.getCostUnitPrice()));
                    } else {
                        income.setCostAmount(new BigDecimal(0));
                    }
                    if (income.getCostQuantity() != null && income.getCostUnitPrice() != null && income.getCostExchangeRate() != null) {
                        income.setCostFunctionalAmount(income.getCostQuantity().multiply(income.getCostUnitPrice().multiply(income.getCostExchangeRate())));
                    } else {
                        income.setCostFunctionalAmount(new BigDecimal(0));
                    }

                    //最低 价
                    if (income.getServiceAmountMin() != null) {
                        if (income.getServiceAmountMin().compareTo(income.getCostAmount()) > 0) {
                            income.setCostAmount(income.getServiceAmountMin());
                            income.setCostQuantity(new BigDecimal(1));
                            income.setCostUnitPrice(income.getServiceAmountMin());
                            if (income.getCostExchangeRate() != null) {
                                income.setCostFunctionalAmount(income.getCostAmount().multiply(income.getCostExchangeRate()));
                            } else {
                                income.setCostFunctionalAmount(new BigDecimal(0));
                            }
                        }
                    }
                    //最高 价
                    if (income.getServiceAmountMax() != null) {
                        if (income.getServiceAmountMax().compareTo(income.getCostAmount()) < 0) {
                            income.setCostAmount(income.getServiceAmountMax());
                            income.setCostQuantity(new BigDecimal(1));
                            income.setCostUnitPrice(income.getServiceAmountMax());
                            if (income.getCostExchangeRate() != null) {
                                income.setCostFunctionalAmount(income.getCostAmount().multiply(income.getCostExchangeRate()));
                            } else {
                                income.setCostFunctionalAmount(new BigDecimal(0));
                            }
                        }
                    }
                    //小数位进位
                    if ("四舍五入".equals(income.getServiceAmountCarry())) {
                        income.setCostAmount(income.getCostAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));
                        income.setCostFunctionalAmount(income.getCostFunctionalAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));

                    }
                    if ("向上进位".equals(income.getServiceAmountCarry())) {
                        income.setCostAmount(income.getCostAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                        income.setCostFunctionalAmount(income.getCostFunctionalAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                    }
                }
            }
        } else if ("LC".equals(bean.getBusinessScope())) {
            //陆运
            LcOrder order = remoteServiceToSC.viewLcOrder(bean.getOrderId()).getData();
            if (order != null) {
                for (int i = 0; i < resultList.size(); i++) {
                    AfCost income = resultList.get(i);
                    income.setOrderId(bean.getOrderId());
                    income.setOrderUuid(bean.getOrderUuid());
                    income.setBusinessScope(bean.getBusinessScope());
                    if ("1".equals(bean.getTemplateCode())) {
                        if (bean.getCustomerId() != -1) {
                            income.setCustomerId(bean.getCustomerId());
                            income.setCustomerName(bean.getCustomerName());
                        }
                    } else {
                        if (income.getCustomerId() == null) {
                            income.setCustomerId(bean.getCustomerId());
                            income.setCustomerName(bean.getCustomerName());
                        }
                    }
                    if ("毛重".equals(income.getDebitNoteNum())) {
                        if (order.getPlanWeight() != null) {
                            income.setCostQuantity(order.getPlanWeight());
                        }
                    } else if ("件数".equals(income.getDebitNoteNum())) {
                        if (order.getPlanPieces() != null) {
                            income.setCostQuantity(new BigDecimal(order.getPlanPieces()));
                        }
                    } else if ("订单".equals(income.getDebitNoteNum())) {
                        income.setCostQuantity(new BigDecimal(1));
                    } else if ("计重".equals(income.getDebitNoteNum())) {
                        if (order.getPlanChargeWeight() != null) {
                            income.setCostQuantity(order.getPlanChargeWeight());
                        }
                    }
                    if (income.getCostQuantity() != null && income.getCostUnitPrice() != null) {
                        income.setCostAmount(income.getCostQuantity().multiply(income.getCostUnitPrice()));
                    } else {
                        income.setCostAmount(new BigDecimal(0));
                    }
                    if (income.getCostQuantity() != null && income.getCostUnitPrice() != null && income.getCostExchangeRate() != null) {
                        income.setCostFunctionalAmount(income.getCostQuantity().multiply(income.getCostUnitPrice().multiply(income.getCostExchangeRate())));
                    } else {
                        income.setCostFunctionalAmount(new BigDecimal(0));
                    }

                    //最低 价
                    if (income.getServiceAmountMin() != null) {
                        if (income.getServiceAmountMin().compareTo(income.getCostAmount()) > 0) {
                            income.setCostAmount(income.getServiceAmountMin());
                            income.setCostQuantity(new BigDecimal(1));
                            income.setCostUnitPrice(income.getServiceAmountMin());
                            if (income.getCostExchangeRate() != null) {
                                income.setCostFunctionalAmount(income.getCostAmount().multiply(income.getCostExchangeRate()));
                            } else {
                                income.setCostFunctionalAmount(new BigDecimal(0));
                            }
                        }
                    }
                    //最高 价
                    if (income.getServiceAmountMax() != null) {
                        if (income.getServiceAmountMax().compareTo(income.getCostAmount()) < 0) {
                            income.setCostAmount(income.getServiceAmountMax());
                            income.setCostQuantity(new BigDecimal(1));
                            income.setCostUnitPrice(income.getServiceAmountMax());
                            if (income.getCostExchangeRate() != null) {
                                income.setCostFunctionalAmount(income.getCostAmount().multiply(income.getCostExchangeRate()));
                            } else {
                                income.setCostFunctionalAmount(new BigDecimal(0));
                            }
                        }
                    }
                    //小数位进位
                    if ("四舍五入".equals(income.getServiceAmountCarry())) {
                        income.setCostAmount(income.getCostAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));
                        income.setCostFunctionalAmount(income.getCostFunctionalAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));

                    }
                    if ("向上进位".equals(income.getServiceAmountCarry())) {
                        income.setCostAmount(income.getCostAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                        income.setCostFunctionalAmount(income.getCostFunctionalAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                    }
                }
            }

        } else if ("IO".equals(bean.getBusinessScope())) {
            //陆运
            IoOrder order = remoteServiceToSC.viewIoOrder(bean.getOrderId()).getData();
            if (order != null) {
                for (int i = 0; i < resultList.size(); i++) {
                    AfCost income = resultList.get(i);
                    income.setOrderId(bean.getOrderId());
                    income.setOrderUuid(bean.getOrderUuid());
                    income.setBusinessScope(bean.getBusinessScope());
                    if ("1".equals(bean.getTemplateCode())) {
                        if (bean.getCustomerId() != -1) {
                            income.setCustomerId(bean.getCustomerId());
                            income.setCustomerName(bean.getCustomerName());
                        }
                    } else {
                        if (income.getCustomerId() == null) {
                            income.setCustomerId(bean.getCustomerId());
                            income.setCustomerName(bean.getCustomerName());
                        }
                    }
                    if ("毛重".equals(income.getDebitNoteNum())) {
                        if (order.getPlanWeight() != null) {
                            income.setCostQuantity(order.getPlanWeight());
                        }
                    } else if ("件数".equals(income.getDebitNoteNum())) {
                        if (order.getPlanPieces() != null) {
                            income.setCostQuantity(new BigDecimal(order.getPlanPieces()));
                        }
                    } else if ("订单".equals(income.getDebitNoteNum())) {
                        income.setCostQuantity(new BigDecimal(1));
                    } else if ("计重".equals(income.getDebitNoteNum())) {
                        if (order.getPlanChargeWeight() != null) {
                            income.setCostQuantity(order.getPlanChargeWeight());
                        }
                    }
                    if (income.getCostQuantity() != null && income.getCostUnitPrice() != null) {
                        income.setCostAmount(income.getCostQuantity().multiply(income.getCostUnitPrice()));
                    } else {
                        income.setCostAmount(new BigDecimal(0));
                    }
                    if (income.getCostQuantity() != null && income.getCostUnitPrice() != null && income.getCostExchangeRate() != null) {
                        income.setCostFunctionalAmount(income.getCostQuantity().multiply(income.getCostUnitPrice().multiply(income.getCostExchangeRate())));
                    } else {
                        income.setCostFunctionalAmount(new BigDecimal(0));
                    }

                    //最低 价
                    if (income.getServiceAmountMin() != null) {
                        if (income.getServiceAmountMin().compareTo(income.getCostAmount()) > 0) {
                            income.setCostAmount(income.getServiceAmountMin());
                            income.setCostQuantity(new BigDecimal(1));
                            income.setCostUnitPrice(income.getServiceAmountMin());
                            if (income.getCostExchangeRate() != null) {
                                income.setCostFunctionalAmount(income.getCostAmount().multiply(income.getCostExchangeRate()));
                            } else {
                                income.setCostFunctionalAmount(new BigDecimal(0));
                            }
                        }
                    }
                    //最高 价
                    if (income.getServiceAmountMax() != null) {
                        if (income.getServiceAmountMax().compareTo(income.getCostAmount()) < 0) {
                            income.setCostAmount(income.getServiceAmountMax());
                            income.setCostQuantity(new BigDecimal(1));
                            income.setCostUnitPrice(income.getServiceAmountMax());
                            if (income.getCostExchangeRate() != null) {
                                income.setCostFunctionalAmount(income.getCostAmount().multiply(income.getCostExchangeRate()));
                            } else {
                                income.setCostFunctionalAmount(new BigDecimal(0));
                            }
                        }
                    }
                    //小数位进位
                    if ("四舍五入".equals(income.getServiceAmountCarry())) {
                        income.setCostAmount(income.getCostAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));
                        income.setCostFunctionalAmount(income.getCostFunctionalAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_HALF_UP));

                    }
                    if ("向上进位".equals(income.getServiceAmountCarry())) {
                        income.setCostAmount(income.getCostAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                        income.setCostFunctionalAmount(income.getCostFunctionalAmount().setScale(income.getServiceAmountDigits().intValue(), BigDecimal.ROUND_UP));
                    }
                }
            }

        }
        resultList2.addAll(resultList);
        return resultList2;
    }
}
