package com.efreight.afbase.service.impl;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.AfAwbPrintShipperConsigneeMapper;
import com.efreight.afbase.dao.AfOrderMapper;
import com.efreight.afbase.dao.AfOrderShareMapper;
import com.efreight.afbase.dao.AwbPrintChargesOtherMapper;
import com.efreight.afbase.dao.AwbPrintMapper;
import com.efreight.afbase.dao.AwbPrintSizeMapper;
import com.efreight.afbase.dao.InboundMapper;
import com.efreight.afbase.dao.OrderFilesMapper;
import com.efreight.afbase.entity.AfAwbPrintShipperConsignee;
import com.efreight.afbase.entity.AfOrder;
import com.efreight.afbase.entity.AfOrderShare;
import com.efreight.afbase.entity.AwbPrint;
import com.efreight.afbase.entity.AwbPrintChargesOther;
import com.efreight.afbase.entity.AwbPrintSize;
import com.efreight.afbase.entity.Inbound;
import com.efreight.afbase.entity.LogBean;
import com.efreight.afbase.entity.OrderFiles;
import com.efreight.afbase.service.AfOrderShareService;
import com.efreight.afbase.service.LogService;
import com.efreight.common.core.feign.RemoteCoopService;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.common.security.vo.CoopVo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class AfOrderShareServiceImpl extends ServiceImpl<AfOrderShareMapper, AfOrderShare> implements AfOrderShareService{
	
	private final AfOrderMapper afOrderMapper;
    private final LogService logService;
    private final RemoteCoopService remoteCoopService;
    private final InboundMapper inboundMapper;
    private final OrderFilesMapper orderFilesMapper;
    private final AwbPrintMapper awbPrintMapper;
    private final AwbPrintChargesOtherMapper awbPrintChargesOtherMapper;
    private final AfAwbPrintShipperConsigneeMapper afAwbPrintShipperConsigneeMapper;
    private final AwbPrintSizeMapper awbPrintSizeMapper;
     
    
    @Override
	public IPage<HashMap> getCoopList(Page page, AfOrderShare bean) {
	 bean.setOrgId(SecurityUtils.getUser().getOrgId());
     IPage<HashMap> afPage = baseMapper.getCoopList(page, bean);
     return afPage;
	}

  @Override
  public boolean afOrderShareCheck(Integer orderId, Integer coopId,Integer shareOrgId) {
	  LambdaQueryWrapper<AfOrderShare> wrapper = Wrappers.lambdaQuery();
	  wrapper.eq(AfOrderShare::getShareScope, "订单协作").eq(AfOrderShare::getBusinessScope,"AE").eq(AfOrderShare::getProcess,"out").eq(AfOrderShare::getOrgId, SecurityUtils.getUser().getOrgId());
	  wrapper.eq(AfOrderShare::getOrderId, orderId).eq(AfOrderShare::getShareCoopId, coopId);
	  AfOrderShare aos = baseMapper.selectOne(wrapper);
	  if(aos!=null) {
		  if(aos.getShareOrderId()==null) {
			  aos.setEditTime(LocalDateTime.now());
			  baseMapper.updateById(aos);
			  return true;
		  }else {
			  return false;
		  }
	  }else {
		  //插入
		  AfOrderShare updateAos = new AfOrderShare();
		  updateAos.setShareScope("订单协作");
		  updateAos.setProcess("out");
		  updateAos.setOrderId(orderId);
		  updateAos.setOrgId(SecurityUtils.getUser().getOrgId());
		  updateAos.setBusinessScope("AE");
		  updateAos.setShareCoopId(coopId);
		  updateAos.setShareOrgId(shareOrgId);
		  updateAos.setCreateTime(LocalDateTime.now());
		  updateAos.setCreatorId(SecurityUtils.getUser().getId());
		  updateAos.setCreatorName(SecurityUtils.getUser().buildOptName());
		  baseMapper.insert(updateAos);
		  //日志
		  LogBean logBean = new LogBean();
          logBean.setPageName("AE订单");
          logBean.setPageFunction("订单协作");
          logBean.setBusinessScope("AE");
          LambdaQueryWrapper<AfOrder> wrapperOrder = Wrappers.lambdaQuery();
          wrapperOrder.eq(AfOrder::getOrderId, orderId);
          AfOrder order = afOrderMapper.selectOne(wrapperOrder);
          logBean.setOrderNumber(order.getOrderCode());
          logBean.setOrderId(orderId);
          logBean.setOrderUuid(order.getOrderUuid());
          CoopVo coopVo = remoteCoopService.viewCoop(coopId.toString()).getData();
          if (coopVo != null) {
        	  logBean.setLogRemark("订单协作分享："+coopVo.getCoop_name());
          }
          logService.saveLog(logBean);
		  return true;
	  }
  }

	@Override
	public List<String> queryPrmCoopShareFields(Integer orgId, Integer coopId) {
		List<String>  list = baseMapper.queryPrmCoopShareFields(orgId,coopId);
		return list;
	}
	@Override
	public List<String> queryPrmCoopShareFieldsTwo(Integer orgId, Integer coopId) {
		List<String>  list = baseMapper.queryPrmCoopShareFieldsTwo(orgId,coopId);
		return list;
	}
	
	@Override
	public AfOrderShare afOrderShareInfo(Integer orgId, Integer coopId, Integer orderId,String type) {
		 LambdaQueryWrapper<AfOrderShare> wrapper = Wrappers.lambdaQuery();
		  wrapper.eq(AfOrderShare::getShareScope, "订单协作").eq(AfOrderShare::getBusinessScope,"AE").eq(AfOrderShare::getProcess,type).eq(AfOrderShare::getOrgId,orgId);
		  wrapper.eq(AfOrderShare::getOrderId, orderId).eq(AfOrderShare::getShareCoopId, coopId);
		  AfOrderShare aos = baseMapper.selectOne(wrapper);
		return aos;
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void shareInbound(AfOrderShare bean) {
		//查询当前公司指定订单下的所有协作记录  
		LambdaQueryWrapper<AfOrderShare> wrapperOrderList = Wrappers.lambdaQuery();
		wrapperOrderList.eq(AfOrderShare::getShareScope, "订单协作").eq(AfOrderShare::getBusinessScope,"AE").eq(AfOrderShare::getOrgId,SecurityUtils.getUser().getOrgId());
		wrapperOrderList.eq(AfOrderShare::getOrderId, bean.getOrderId());
	    List<AfOrderShare> aosOrderList = baseMapper.selectList(wrapperOrderList);
		if(aosOrderList!=null&&aosOrderList.size()>0) {
			for(AfOrderShare orderList:aosOrderList) {
				if(orderList.getShareOrderId()==null) {
					continue;
				}
				Integer orgId = null,coopId = null,orderId = null;
				Integer shareOrgId = null,shareCoopId = null,shareOrderId = null;
				String coopName = null,shareCoopName = null;
				//根据 订单ID  签约公司 查看是否 的订单协作记录 如果存在取  
				LambdaQueryWrapper<AfOrderShare> wrapperOrderOut = Wrappers.lambdaQuery();
//				.eq(AfOrderShare::getProcess,"out")
				wrapperOrderOut.eq(AfOrderShare::getShareScope, "订单协作").eq(AfOrderShare::getBusinessScope,"AE").eq(AfOrderShare::getOrgId,SecurityUtils.getUser().getOrgId());
				wrapperOrderOut.eq(AfOrderShare::getOrderId, bean.getOrderId()).eq(AfOrderShare::getShareCoopId, orderList.getShareCoopId());
			    List<AfOrderShare> aosOrderOutList = baseMapper.selectList(wrapperOrderOut);
				if(aosOrderOutList!=null&&aosOrderOutList.size()>0) {
					AfOrderShare aosOrderOut = aosOrderOutList.get(0);
					if(aosOrderOut.getShareOrderId()==null) {
						throw new RuntimeException("该订单没有协作绑定订单，不能执行此操作。");
					}
					orgId = aosOrderOut.getOrgId();
					coopId = aosOrderOut.getShareCoopId();
					orderId = aosOrderOut.getOrderId();
					shareOrgId = aosOrderOut.getShareOrgId();
					shareOrderId = aosOrderOut.getShareOrderId();
					CoopVo coopVo = remoteCoopService.viewCoop(aosOrderOut.getShareCoopId().toString()).getData();
			        if (coopVo != null) {
			        	shareCoopId = coopVo.getCoop_org_coop_id();
			        	shareCoopName = coopVo.getCoop_name();
			        }else {
			        	throw new RuntimeException("客商资料异常,请联系管理员。");
			        }
				}
				CoopVo coopVoTwo = remoteCoopService.viewCoop(coopId.toString()).getData();
		        if (coopVoTwo != null) {
		        	coopName = coopVoTwo.getCoop_name();
		        }else {
		        	throw new RuntimeException("客商资料异常,请联系管理员。");
		        }
		        //取分享 以及订阅信息
		        List<String> listShare = this.queryPrmCoopShareFields(orgId,coopId);
		        List<String> listSubscribe = this.queryPrmCoopShareFieldsTwo(shareOrgId,shareCoopId);
		        String listShareStr = new JSONArray().toJSONString(listShare);
		        String listSubscribeStr = new JSONArray().toJSONString(listSubscribe);
				//查询 是否有 协作出重信息
				LambdaQueryWrapper<AfOrderShare> wrapper = Wrappers.lambdaQuery();
			    wrapper.eq(AfOrderShare::getShareScope, "货物出重").eq(AfOrderShare::getBusinessScope,"AE").eq(AfOrderShare::getOrgId,orgId);
			    wrapper.eq(AfOrderShare::getOrderId,orderId).eq(AfOrderShare::getShareCoopId, coopId);
			    AfOrderShare aos = baseMapper.selectOne(wrapper);
			    if(aos!=null) {
			    	throw new RuntimeException("货物出重已进行过协作，不能重复操作。");
			    }
				//插入协作出重记录 当前登录用户所选订单
			    AfOrderShare updateAos = new AfOrderShare();
				  updateAos.setShareScope("货物出重");
				  updateAos.setProcess("out");
				  updateAos.setOrderId(orderId);
				  updateAos.setOrgId(orgId);
				  updateAos.setBusinessScope("AE");
				  updateAos.setShareCoopId(coopId);
				  updateAos.setShareOrgId(shareOrgId);
				  updateAos.setShareOrderId(shareOrderId);
				  updateAos.setCreateTime(LocalDateTime.now());
				  updateAos.setCreatorId(SecurityUtils.getUser().getId());
				  updateAos.setCreatorName(SecurityUtils.getUser().buildOptName());
				  baseMapper.insert(updateAos);
				  //插入协作出重记录 当前登录用户所选订单绑定的订单
				  AfOrderShare updateAosTwo = new AfOrderShare();
				  updateAosTwo.setShareScope("货物出重");
				  updateAosTwo.setProcess("in");
				  updateAosTwo.setOrderId(shareOrderId);
				  updateAosTwo.setOrgId(shareOrgId);
				  updateAosTwo.setBusinessScope("AE");
				  updateAosTwo.setShareCoopId(shareCoopId);
				  updateAosTwo.setShareOrgId(orgId);
				  updateAosTwo.setShareOrderId(orderId);
				  updateAosTwo.setCreateTime(LocalDateTime.now());
				  updateAosTwo.setCreatorId(SecurityUtils.getUser().getId());
				  updateAosTwo.setCreatorName(SecurityUtils.getUser().buildOptName());
				  baseMapper.insert(updateAosTwo);
				  
				  //更新绑定订单 出重信息  以当前订单为主
				  AfOrder orderInbound = afOrderMapper.selectById(orderId);
				  AfOrder shareOrderInbound = afOrderMapper.selectById(shareOrderId);
				  if(listShareStr.contains("出重信息")&&listSubscribeStr.contains("出重信息")) {
		          
		          //当前签约公司出重
		          LambdaQueryWrapper<Inbound> wrapperInboundOne = Wrappers.<Inbound>lambdaQuery();
		          wrapperInboundOne.eq(Inbound::getOrderId,orderId).eq(Inbound::getOrgId,orgId);
		          Inbound inbound = inboundMapper.selectOne(wrapperInboundOne);
		          //绑定签约公司出重明细
		          LambdaQueryWrapper<Inbound> wrapperInboundTwo = Wrappers.<Inbound>lambdaQuery();
		          wrapperInboundTwo.eq(Inbound::getOrderId,shareOrderId).eq(Inbound::getOrgId,shareOrgId);
		          Inbound one = inboundMapper.selectOne(wrapperInboundTwo);
		          if(one!=null) {
		        	  //更新
		        	  one.setOrderSize(inbound.getOrderSize());
			          one.setOrderPieces(inbound.getOrderPieces());
			          one.setOrderGrossWeight(inbound.getOrderGrossWeight());
			          one.setOrderVolume(inbound.getOrderVolume());
			          one.setOrderChargeWeight(inbound.getOrderChargeWeight());
			          one.setEditorId(SecurityUtils.getUser().getId());
			          one.setEditorName(SecurityUtils.getUser().getUsername());
			          one.setEditTime(LocalDateTime.now());
			          one.setOrderDimensions(inbound.getOrderDimensions());
			          inboundMapper.updateById(one);
		          }else {
		        	  //新增
		        	  one = new Inbound();
		        	  one.setOrgId(shareOrgId);
		        	  one.setOrderId(shareOrderId);
		        	  one.setOrderUuid(shareOrderInbound.getOrderUuid());
		        	  one.setOrderChargeWeight(inbound.getOrderChargeWeight());
		        	  one.setOrderGrossWeight(inbound.getOrderGrossWeight());
		        	  one.setOrderPieces(inbound.getOrderPieces());
		        	  one.setOrderVolume(inbound.getOrderVolume());
		        	  one.setOrderSize(inbound.getOrderSize());
		        	  one.setCreateTime(LocalDateTime.now());
		        	  one.setCreatorId(SecurityUtils.getUser().getId());
		        	  one.setCreatorName(SecurityUtils.getUser().buildOptName());
		        	  inboundMapper.insert(one);
		          }
		          
		          //out
		          LogBean logBeanInbound = new LogBean();
		          logBeanInbound.setPageName("操作出重");
		          logBeanInbound.setPageFunction("协作分享");
		          logBeanInbound.setLogRemark("协作公司:["+coopName+"]  "+this.getLogRemark(orderInbound,inbound));
		          logBeanInbound.setBusinessScope("AE");
		          logBeanInbound.setOrderNumber(orderInbound.getOrderCode());
		          logBeanInbound.setOrderId(orderInbound.getOrderId());
		          logBeanInbound.setOrderUuid(orderInbound.getOrderUuid());
		          logBeanInbound.setCreatorId(SecurityUtils.getUser().getId());
		          logBeanInbound.setCreatorName(SecurityUtils.getUser().buildOptName());
		          logBeanInbound.setCreatTime(LocalDateTime.now());
		          logBeanInbound.setOrgId(orgId);
			  	  logService.save(logBeanInbound);
			  	  //in
		          LogBean logBeanInboundTwo = new LogBean();
		          logBeanInboundTwo.setPageName("操作出重");
		          logBeanInboundTwo.setPageFunction("协作接收");
		          logBeanInboundTwo.setLogRemark("协作公司:["+shareCoopName+"]  "+this.getLogRemark(shareOrderInbound, one));
		          logBeanInboundTwo.setBusinessScope("AE");
		          logBeanInboundTwo.setOrderNumber(shareOrderInbound.getOrderCode());
		          logBeanInboundTwo.setOrderId(shareOrderInbound.getOrderId());
		          logBeanInboundTwo.setOrderUuid(shareOrderInbound.getOrderUuid());
		          logBeanInboundTwo.setCreatorId(SecurityUtils.getUser().getId());
		          logBeanInboundTwo.setCreatorName(SecurityUtils.getUser().buildOptName());
		          logBeanInboundTwo.setCreatTime(LocalDateTime.now());
		          logBeanInboundTwo.setOrgId(shareOrgId);
			  	  logService.save(logBeanInboundTwo);
			  	  
			  	shareOrderInbound.setConfirmChargeWeight(orderInbound.getConfirmChargeWeight());
			    shareOrderInbound.setConfirmDensity(orderInbound.getConfirmDensity());
			    shareOrderInbound.setConfirmPieces(orderInbound.getConfirmPieces());
			    shareOrderInbound.setConfirmVolume(orderInbound.getConfirmVolume());
			    shareOrderInbound.setConfirmWeight(orderInbound.getConfirmWeight());
			    shareOrderInbound.setRowUuid(UUID.randomUUID().toString());
			    if("财务锁账".equals(shareOrderInbound.getOrderStatus())) {
			    	
			    }else if (shareOrderInbound.getDeliverySignDate() != null) {
			    	shareOrderInbound.setOrderStatus("目的港签收");
		        } else if (shareOrderInbound.getArrivalCustomsClearanceDate() != null) {
		        	shareOrderInbound.setOrderStatus("目的港放行");
		        } else if (shareOrderInbound.getArrivalCustomsInspectionDate() != null) {
		        	shareOrderInbound.setOrderStatus("目的港查验");
		        } else if (shareOrderInbound.getCustomsClearanceDate() != null) {
		        	shareOrderInbound.setOrderStatus("海关放行");
		        } else if (shareOrderInbound.getCustomsInspectionDate() != null) {
		        	shareOrderInbound.setOrderStatus("海关查验");
		        } else if (shareOrderInbound.getConfirmWeight() != null) {
		        	shareOrderInbound.setOrderStatus("货物出重");
		        } else if (shareOrderInbound.getAwbNumber() != null && !"".equals(shareOrderInbound.getAwbNumber())) {
		        	shareOrderInbound.setOrderStatus("舱位确认");
		        } else {
		        	shareOrderInbound.setOrderStatus("订单创建");
		        }
			    afOrderMapper.updateById(shareOrderInbound);
			   }else {
				 //out
		          LogBean logBeanInbound = new LogBean();
		          logBeanInbound.setPageName("操作出重");
		          logBeanInbound.setPageFunction("协作分享");
		          logBeanInbound.setLogRemark("协作公司:["+coopName+"]  "+"协作出重->己方未在协作公司处分享出重信息或者协作方未设置订阅出重信息");
		          logBeanInbound.setBusinessScope("AE");
		          logBeanInbound.setOrderNumber(orderInbound.getOrderCode());
		          logBeanInbound.setOrderId(orderInbound.getOrderId());
		          logBeanInbound.setOrderUuid(orderInbound.getOrderUuid());
		          logBeanInbound.setCreatorId(SecurityUtils.getUser().getId());
		          logBeanInbound.setCreatorName(SecurityUtils.getUser().buildOptName());
		          logBeanInbound.setCreatTime(LocalDateTime.now());
		          logBeanInbound.setOrgId(orgId);
			  	  logService.save(logBeanInbound);
			  	  //in
		          LogBean logBeanInboundTwo = new LogBean();
		          logBeanInboundTwo.setPageName("操作出重");
		          logBeanInboundTwo.setPageFunction("协作接收");
		          logBeanInboundTwo.setLogRemark("协作公司:["+shareCoopName+"]  "+"协作出重->己方未在协作公司处订阅出重信息或者协作方未设置分享出重信息");
		          logBeanInboundTwo.setBusinessScope("AE");
		          logBeanInboundTwo.setOrderNumber(shareOrderInbound.getOrderCode());
		          logBeanInboundTwo.setOrderId(shareOrderInbound.getOrderId());
		          logBeanInboundTwo.setOrderUuid(shareOrderInbound.getOrderUuid());
		          logBeanInboundTwo.setCreatorId(SecurityUtils.getUser().getId());
		          logBeanInboundTwo.setCreatorName(SecurityUtils.getUser().buildOptName());
		          logBeanInboundTwo.setCreatTime(LocalDateTime.now());
		          logBeanInboundTwo.setOrgId(shareOrgId);
			  	  logService.save(logBeanInboundTwo);
			   }
			}
			
		}else {
			throw new RuntimeException("该订单没有协作绑定订单，不能执行此操作。");
		}
	}

	@Override
	public boolean afOrderShareCheckOrder(Integer orderId) {
		  LambdaQueryWrapper<AfOrderShare> wrapper = Wrappers.lambdaQuery();
		  wrapper.eq(AfOrderShare::getShareScope, "订单协作").eq(AfOrderShare::getBusinessScope,"AE").eq(AfOrderShare::getProcess,"out").eq(AfOrderShare::getOrgId,SecurityUtils.getUser().getOrgId());
		  wrapper.eq(AfOrderShare::getOrderId, orderId);
		  List<AfOrderShare> aos = baseMapper.selectList(wrapper);
		  if(aos!=null&&aos.size()>0) {
			  return false;
		  }else {
			  return true;
		  }
		
	}
	private String getLogRemark(AfOrder order, Inbound bean) {
        StringBuffer logremark = new StringBuffer();

        String planPieces = this.getStr("" + order.getConfirmPieces(), "" + bean.getOrderPieces());
        logremark.append(StringUtils.isBlank(planPieces) ? "" : "件数：" + planPieces);
        String planWeight = this.getStr(String.valueOf(order.getConfirmWeight()), this.fmtMicrometer2(String.valueOf(bean.getOrderGrossWeight())));
        logremark.append(StringUtils.isBlank(planWeight) ? "" : "毛重：" + planWeight);
        String planVolume = this.getStr("" + order.getConfirmVolume(), "" + bean.getOrderVolume());
        logremark.append(StringUtils.isBlank(planVolume) ? "" : "体积：" + planVolume);
        String planChargeWeight = this.getStr("" + order.getConfirmChargeWeight(), "" + bean.getOrderChargeWeight());
        logremark.append(StringUtils.isBlank(planChargeWeight) ? "" : "计重：" + planChargeWeight);
       
        return logremark.toString();
    }

	public static String fmtMicrometer2(String text) {
        DecimalFormat df = null;

        df = new DecimalFormat("#####0.0");

        double number = 0.0;
        try {
            number = Double.parseDouble(text);
            return df.format(number);
        } catch (Exception e) {
            number = 0.0;
            return "";
        }
    }
	private String getStr(String str1, String str2) {
        String str = "";
        if (StringUtils.isBlank(str1) || "null".equals(str1)) {
            str1 = "0";
        }
        if (StringUtils.isBlank(str2) || "null".equals(str2)) {
            str2 = "0";
        }
        if (!str1.equals(str2)) {
            str = str1 + " -> " + str2;
//            str =  str2;
        }
        return str + "  ";
    }

	@Override
	public boolean checkShareScope(Integer orderId, String shareScope,String orderUuid) {
		 if(!StringUtils.isEmpty(orderUuid)&&!"null".equals(orderUuid)) {
			 LambdaQueryWrapper<AfOrder> wrapperOrder = Wrappers.lambdaQuery();
			 wrapperOrder.eq(AfOrder::getOrderUuid, orderUuid);
			 AfOrder order = afOrderMapper.selectOne(wrapperOrder);
			 if(order!=null) {
				 orderId = order.getOrderId();
			 }
		 }
		 LambdaQueryWrapper<AfOrderShare> wrapper = Wrappers.lambdaQuery();
		  wrapper.eq(AfOrderShare::getShareScope,"订单协作").eq(AfOrderShare::getBusinessScope,"AE").eq(AfOrderShare::getOrgId,SecurityUtils.getUser().getOrgId());
		  wrapper.eq(AfOrderShare::getOrderId, orderId);
		  List<AfOrderShare> aosList = baseMapper.selectList(wrapper);
		  if(aosList!=null&&aosList.size()>0) {
			  if("电子单证".equals(shareScope)) {
				  return true;
			  }else if("订单协作".equals(shareScope)){
				  return true;
			  }else {
				  //同步信息校验    后来的就同步不了（暂定这样后面可能会改）
				  LambdaQueryWrapper<AfOrderShare> wrapperTwo = Wrappers.lambdaQuery();
				  wrapperTwo.eq(AfOrderShare::getShareScope,shareScope).eq(AfOrderShare::getBusinessScope,"AE").eq(AfOrderShare::getOrgId,SecurityUtils.getUser().getOrgId());
				  wrapperTwo.eq(AfOrderShare::getOrderId, orderId);
				  List<AfOrderShare> aosTwo = baseMapper.selectList(wrapperTwo);
				  if(aosTwo!=null&&aosTwo.size()>0) {
					  return false;
				  }else {
					  return true;
				  }
			  }
			  
		  }else {
			  return false;
		  }
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void shareOrderFiles(AfOrderShare bean) {
		//查询当前公司指定订单下的所有协作记录  
		LambdaQueryWrapper<AfOrderShare> wrapperOrderList = Wrappers.lambdaQuery();
		wrapperOrderList.eq(AfOrderShare::getShareScope, "订单协作").eq(AfOrderShare::getBusinessScope,"AE").eq(AfOrderShare::getOrgId,SecurityUtils.getUser().getOrgId());
		wrapperOrderList.eq(AfOrderShare::getOrderId, bean.getOrderId());
	    List<AfOrderShare> aosOrderList = baseMapper.selectList(wrapperOrderList);
		if(aosOrderList!=null&&aosOrderList.size()>0) {
			for(AfOrderShare orderList:aosOrderList) {
				if(orderList.getShareOrderId()==null) {
					continue;
				}
				Integer orgId = null,coopId = null,orderId = null;
				Integer shareOrgId = null,shareCoopId = null,shareOrderId = null;
				String coopName = null,shareCoopName = null;
				//根据 订单ID  签约公司 查看是否 的订单协作记录 如果存在取  
				LambdaQueryWrapper<AfOrderShare> wrapperOrderOut = Wrappers.lambdaQuery();
				wrapperOrderOut.eq(AfOrderShare::getShareScope, "订单协作").eq(AfOrderShare::getBusinessScope,"AE").eq(AfOrderShare::getOrgId,SecurityUtils.getUser().getOrgId());
				wrapperOrderOut.eq(AfOrderShare::getOrderId, bean.getOrderId()).eq(AfOrderShare::getShareCoopId, orderList.getShareCoopId());
			    List<AfOrderShare> aosOrderOutList = baseMapper.selectList(wrapperOrderOut);
				if(aosOrderOutList!=null&&aosOrderOutList.size()>0) {
					AfOrderShare aosOrderOut = aosOrderOutList.get(0);
					if(aosOrderOut.getShareOrderId()==null) {
						throw new RuntimeException("该订单没有协作绑定订单，不能执行此操作。");
					}
					orgId = aosOrderOut.getOrgId();
					coopId = aosOrderOut.getShareCoopId();
					orderId = aosOrderOut.getOrderId();
					shareOrgId = aosOrderOut.getShareOrgId();
					shareOrderId = aosOrderOut.getShareOrderId();
					CoopVo coopVo = remoteCoopService.viewCoop(aosOrderOut.getShareCoopId().toString()).getData();
			        if (coopVo != null) {
			        	shareCoopId = coopVo.getCoop_org_coop_id();
			        	shareCoopName = coopVo.getCoop_name();
			        }else {
			        	throw new RuntimeException("客商资料异常,请联系管理员。");
			        }
				}
				CoopVo coopVoTwo = remoteCoopService.viewCoop(coopId.toString()).getData();
		        if (coopVoTwo != null) {
		        	coopName = coopVoTwo.getCoop_name();
		        }else {
		        	throw new RuntimeException("客商资料异常,请联系管理员。");
		        }
		        //取分享 以及订阅信息
		        List<String> listShare = this.queryPrmCoopShareFields(orgId,coopId);
		        List<String> listSubscribe = this.queryPrmCoopShareFieldsTwo(shareOrgId,shareCoopId);
		        String listShareStr = new JSONArray().toJSONString(listShare);
		        String listSubscribeStr = new JSONArray().toJSONString(listSubscribe);
				
				//插入协作电子单证记录 当前登录用户所选订单
			    AfOrderShare updateAos = new AfOrderShare();
				  updateAos.setShareScope("电子单证");
				  updateAos.setProcess("out");
				  updateAos.setOrderId(orderId);
				  updateAos.setOrgId(orgId);
				  updateAos.setBusinessScope("AE");
				  updateAos.setShareCoopId(coopId);
				  updateAos.setShareOrgId(shareOrgId);
				  updateAos.setShareOrderId(shareOrderId);
				  updateAos.setCreateTime(LocalDateTime.now());
				  updateAos.setCreatorId(SecurityUtils.getUser().getId());
				  updateAos.setCreatorName(SecurityUtils.getUser().buildOptName());
				  baseMapper.insert(updateAos);
				  //插入电子单证  当前登录用户所选订单绑定的订单
				  AfOrderShare updateAosTwo = new AfOrderShare();
				  updateAosTwo.setShareScope("电子单证");
				  updateAosTwo.setProcess("in");
				  updateAosTwo.setOrderId(shareOrderId);
				  updateAosTwo.setOrgId(shareOrgId);
				  updateAosTwo.setBusinessScope("AE");
				  updateAosTwo.setShareCoopId(shareCoopId);
				  updateAosTwo.setShareOrgId(orgId);
				  updateAosTwo.setShareOrderId(orderId);
				  updateAosTwo.setCreateTime(LocalDateTime.now());
				  updateAosTwo.setCreatorId(SecurityUtils.getUser().getId());
				  updateAosTwo.setCreatorName(SecurityUtils.getUser().buildOptName());
				  baseMapper.insert(updateAosTwo);
				  if(listShareStr.contains("电子单证")&&listSubscribeStr.contains("电子单证")) {
		          //插入电子单证信息
		          //查询订单
//				          AfOrder shareOrderInbound = afOrderMapper.selectById(shareOrderId);
		          //查询电子单证
		          LambdaQueryWrapper<OrderFiles> wrapperOrderFiles = Wrappers.<OrderFiles>lambdaQuery();
		          wrapperOrderFiles.eq(OrderFiles::getOrderId,orderId).in(OrderFiles::getOrderFileId, bean.getListOrderFilesId());
		          List<OrderFiles> listFiles = orderFilesMapper.selectList(wrapperOrderFiles);
		          final String  nameF = shareCoopName;
		          final Integer orderF = shareOrderId;
		          final Integer orgIdF = shareOrgId;
		          if(listFiles!=null&&listFiles.size()>0) {
		        	  listFiles.stream().forEach(o->{
		        		  LambdaQueryWrapper<OrderFiles> inserCheck = Wrappers.<OrderFiles>lambdaQuery();
		        		  inserCheck.eq(OrderFiles::getBusinessScope, "AE");
		        		  inserCheck.eq(OrderFiles::getOrderId, orderF).eq(OrderFiles::getOrgId, orgIdF).eq(OrderFiles::getFileUrl, o.getFileUrl());
		        		  List<OrderFiles> inserCheckList = orderFilesMapper.selectList(inserCheck);
		        		  if(inserCheckList!=null&&inserCheckList.size()>0) {
		        			  //todo
		        		  }else {
		        			  OrderFiles  files = new OrderFiles();
		        			  BeanUtils.copyProperties(o, files);
		        			  files.setCreateTime(LocalDateTime.now());
		        			  files.setFileRemark("单证协作:("+nameF+")");
		        			  files.setOrderFileId(null);
		        			  files.setOrderId(orderF);
		        			  files.setOrgId(orgIdF);
		        			  orderFilesMapper.insert(files);
		        		  }
		        	  });
		          }
				}
			}
		}else {
			throw new RuntimeException("该订单没有协作绑定订单，不能执行此操作。");
		}
		
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void shareWayBillMake(AfOrderShare bean) {
		 if(!StringUtils.isEmpty(bean.getOrderUuid())) {
			 LambdaQueryWrapper<AfOrder> wrapperOrder = Wrappers.lambdaQuery();
			 wrapperOrder.eq(AfOrder::getOrderUuid, bean.getOrderUuid());
			 AfOrder orderInfo = afOrderMapper.selectOne(wrapperOrder);
			 if(orderInfo!=null) {
				 bean.setOrderId(orderInfo.getOrderId());
			 }
		 }
		//查询当前公司指定订单下的所有协作记录  
		LambdaQueryWrapper<AfOrderShare> wrapperOrderList = Wrappers.lambdaQuery();
		wrapperOrderList.eq(AfOrderShare::getShareScope, "订单协作").eq(AfOrderShare::getBusinessScope,"AE").eq(AfOrderShare::getOrgId,SecurityUtils.getUser().getOrgId());
		wrapperOrderList.eq(AfOrderShare::getOrderId, bean.getOrderId());
	    List<AfOrderShare> aosOrderList = baseMapper.selectList(wrapperOrderList);
		if(aosOrderList!=null&&aosOrderList.size()>0) {
			for(AfOrderShare orderList:aosOrderList) {
				if(orderList.getShareOrderId()==null) {
					continue;
				}
				Integer orgId = null,coopId = null,orderId = null;
				Integer shareOrgId = null,shareCoopId = null,shareOrderId = null;
				String coopName = null,shareCoopName = null;
				//根据 订单ID  签约公司 查看是否 的订单协作记录 如果存在取  
				LambdaQueryWrapper<AfOrderShare> wrapperOrderOut = Wrappers.lambdaQuery();
				wrapperOrderOut.eq(AfOrderShare::getShareScope, "订单协作").eq(AfOrderShare::getBusinessScope,"AE").eq(AfOrderShare::getOrgId,SecurityUtils.getUser().getOrgId());
				wrapperOrderOut.eq(AfOrderShare::getOrderId, bean.getOrderId()).eq(AfOrderShare::getShareCoopId, orderList.getShareCoopId());
			    List<AfOrderShare> aosOrderOutList = baseMapper.selectList(wrapperOrderOut);
				if(aosOrderOutList!=null&&aosOrderOutList.size()>0) {
					AfOrderShare aosOrderOut = aosOrderOutList.get(0);
					if(aosOrderOut.getShareOrderId()==null) {
						throw new RuntimeException("该订单没有协作绑定订单，不能执行此操作。");
					}
					orgId = aosOrderOut.getOrgId();
					coopId = aosOrderOut.getShareCoopId();
					orderId = aosOrderOut.getOrderId();
					shareOrgId = aosOrderOut.getShareOrgId();
					shareOrderId = aosOrderOut.getShareOrderId();
					CoopVo coopVo = remoteCoopService.viewCoop(aosOrderOut.getShareCoopId().toString()).getData();
			        if (coopVo != null) {
			        	shareCoopId = coopVo.getCoop_org_coop_id();
			        	shareCoopName = coopVo.getCoop_name();
			        }else {
			        	throw new RuntimeException("客商资料异常,请联系管理员。");
			        }
				}
				CoopVo coopVoTwo = remoteCoopService.viewCoop(coopId.toString()).getData();
		        if (coopVoTwo != null) {
		        	coopName = coopVoTwo.getCoop_name();
		        }else {
		        	throw new RuntimeException("客商资料异常,请联系管理员。");
		        }
		        
		        //取分享 以及订阅信息
		        List<String> listShare = this.queryPrmCoopShareFields(orgId,coopId);
		        List<String> listSubscribe = this.queryPrmCoopShareFieldsTwo(shareOrgId,shareCoopId);
		        String listShareStr = new JSONArray().toJSONString(listShare);
		        String listSubscribeStr = new JSONArray().toJSONString(listSubscribe);
				
				//插入协作制单信息记录 当前登录用户所选订单
			    AfOrderShare updateAos = new AfOrderShare();
				  updateAos.setShareScope("制单操作");
				  updateAos.setProcess("out");
				  updateAos.setOrderId(orderId);
				  updateAos.setOrgId(orgId);
				  updateAos.setBusinessScope("AE");
				  updateAos.setShareCoopId(coopId);
				  updateAos.setShareOrgId(shareOrgId);
				  updateAos.setShareOrderId(shareOrderId);
				  updateAos.setCreateTime(LocalDateTime.now());
				  updateAos.setCreatorId(SecurityUtils.getUser().getId());
				  updateAos.setCreatorName(SecurityUtils.getUser().buildOptName());
				  baseMapper.insert(updateAos);
				  //插入只单信息  当前登录用户所选订单绑定的订单
				  AfOrderShare updateAosTwo = new AfOrderShare();
				  updateAosTwo.setShareScope("制单操作");
				  updateAosTwo.setProcess("in");
				  updateAosTwo.setOrderId(shareOrderId);
				  updateAosTwo.setOrgId(shareOrgId);
				  updateAosTwo.setBusinessScope("AE");
				  updateAosTwo.setShareCoopId(shareCoopId);
				  updateAosTwo.setShareOrgId(orgId);
				  updateAosTwo.setShareOrderId(orderId);
				  updateAosTwo.setCreateTime(LocalDateTime.now());
				  updateAosTwo.setCreatorId(SecurityUtils.getUser().getId());
				  updateAosTwo.setCreatorName(SecurityUtils.getUser().buildOptName());
				  baseMapper.insert(updateAosTwo);
		          //查询订单
		          AfOrder shareOrder = afOrderMapper.selectById(shareOrderId);
		          AfOrder orderTwo = afOrderMapper.selectById(orderId);
		          
		          if(listShareStr.contains("制单操作")&&listSubscribeStr.contains("制单操作")) {
		          //查询制单信息
		          LambdaQueryWrapper<AwbPrint> wrapperAwbPrint = Wrappers.<AwbPrint>lambdaQuery();
		          wrapperAwbPrint.eq(AwbPrint::getOrderId,shareOrderId).eq(AwbPrint::getOrgId, shareOrgId);
		          List<AwbPrint> listAwbPrint = awbPrintMapper.selectList(wrapperAwbPrint);
		          if(listAwbPrint!=null&&listAwbPrint.size()>0) {
		        	  //out
			          LogBean logBeanAwbOut = new LogBean();
			          logBeanAwbOut.setPageName("单证制作");
			          logBeanAwbOut.setPageFunction("协作分享");
			          logBeanAwbOut.setLogRemark("协作公司:["+coopName+"]  "+"协作单证->协作方已有单证信息");
			          logBeanAwbOut.setBusinessScope("AE");
			          logBeanAwbOut.setOrderNumber(orderTwo.getOrderCode());
			          logBeanAwbOut.setOrderId(orderTwo.getOrderId());
			          logBeanAwbOut.setOrderUuid(orderTwo.getOrderUuid());
			          logBeanAwbOut.setCreatorId(SecurityUtils.getUser().getId());
			          logBeanAwbOut.setCreatorName(SecurityUtils.getUser().buildOptName());
			          logBeanAwbOut.setCreatTime(LocalDateTime.now());
			          logBeanAwbOut.setOrgId(orgId);
				  	  logService.save(logBeanAwbOut);
				  	  //in
			          LogBean logBeanAwbIn = new LogBean();
			          logBeanAwbIn.setPageName("单证制作");
			          logBeanAwbIn.setPageFunction("协作接收");
			          logBeanAwbIn.setLogRemark("协作公司:["+shareCoopName+"]  "+"协作单证->己方已有单证信息");
			          logBeanAwbIn.setBusinessScope("AE");
			          logBeanAwbIn.setOrderNumber(shareOrder.getOrderCode());
			          logBeanAwbIn.setOrderId(shareOrder.getOrderId());
			          logBeanAwbIn.setOrderUuid(shareOrder.getOrderUuid());
			          logBeanAwbIn.setCreatorId(SecurityUtils.getUser().getId());
			          logBeanAwbIn.setCreatorName(SecurityUtils.getUser().buildOptName());
			          logBeanAwbIn.setCreatTime(LocalDateTime.now());
			          logBeanAwbIn.setOrgId(shareOrgId);
				  	  logService.save(logBeanAwbIn); 
		        	
		          }else {
		        	  LambdaQueryWrapper<AwbPrint> wrapperAwbPrintTwo = Wrappers.<AwbPrint>lambdaQuery();
		        	  wrapperAwbPrintTwo.eq(AwbPrint::getOrderId,orderId).eq(AwbPrint::getOrgId, orgId);
		              List<AwbPrint> listInsert = awbPrintMapper.selectList(wrapperAwbPrintTwo);
		              StringBuffer sb = new StringBuffer();
		              if(listInsert!=null&&listInsert.size()>0) {
		            	  for(AwbPrint ap:listInsert) {
//		            		  sb.append(ap.getHawbNumber()==null?ap.getHawbNumber():ap.getAwbNumber()).append(",");
		            		  //插入AwbPrint
		            		  AwbPrint insertAp = new AwbPrint();
		            		  BeanUtils.copyProperties(ap, insertAp);
		            		  insertAp.setAwbPrintId(null);
		            		  insertAp.setOrderId(shareOrderId);
		            		  insertAp.setOrderUuid(shareOrder.getOrderUuid());
		            		  insertAp.setOrgId(shareOrgId);
		            		  insertAp.setCreatorId(SecurityUtils.getUser().getId());
		            		  insertAp.setCreateTime(LocalDateTime.now());
		            		  insertAp.setCreatorName(SecurityUtils.getUser().buildOptName());
		            		  insertAp.setEditorId(null);
		            		  insertAp.setEditorName(null);
		            		  insertAp.setEditTime(null);
		            		  insertAp.setRowid(UUID.randomUUID().toString());
		            		  awbPrintMapper.insert(insertAp);
		            		  //查询收发货人 AwbPrintChargesOther  AfAwbPrintShipperConsignee AwbPrintSize
		            		  LambdaQueryWrapper<AfAwbPrintShipperConsignee> wrapperAfAwbPrintShipperConsignee = Wrappers.<AfAwbPrintShipperConsignee>lambdaQuery();
		            		  wrapperAfAwbPrintShipperConsignee.eq(AfAwbPrintShipperConsignee::getAwbPrintId, ap.getAwbPrintId()).eq(AfAwbPrintShipperConsignee::getOrgId, orgId);
		            		  List<AfAwbPrintShipperConsignee> listAPSC = afAwbPrintShipperConsigneeMapper.selectList(wrapperAfAwbPrintShipperConsignee);
		            		  if(listAPSC!=null&&listAPSC.size()>0) {
		            			  for(AfAwbPrintShipperConsignee aapsc:listAPSC) {
		            				  AfAwbPrintShipperConsignee aapscInsert = new AfAwbPrintShipperConsignee();
		            				  BeanUtils.copyProperties(aapsc, aapscInsert);
		            				  aapscInsert.setAwbScId(null);
		            				  aapscInsert.setAwbPrintId(Integer.valueOf(insertAp.getAwbPrintId()));
		            				  aapscInsert.setOrgId(shareOrgId);
		            				  aapscInsert.setCreatorId(SecurityUtils.getUser().getId());
		            				  aapscInsert.setCreateTime(LocalDateTime.now());
		            				  aapscInsert.setCreatorName(SecurityUtils.getUser().buildOptName());
		            				  aapscInsert.setEditorId(null);
		            				  aapscInsert.setEditorName(null);
		            				  aapscInsert.setEditTime(null);
		            				  afAwbPrintShipperConsigneeMapper.insert(aapscInsert);
		            				  if(aapscInsert.getScType()==0) {
		            					  //发货人
		            					  insertAp.setShipperId(aapscInsert.getAwbScId());
		            				  }else {
		            					  //收货人
		            					  insertAp.setConsigneeId(aapscInsert.getAwbScId());
		            				  }
		            			  }
		            		  }
		            		 
		            		  //运单制单 杂费表
		            		  LambdaQueryWrapper<AwbPrintChargesOther> wrapperAwbPrintChargesOther = Wrappers.<AwbPrintChargesOther>lambdaQuery();
		            		  wrapperAwbPrintChargesOther.eq(AwbPrintChargesOther::getAwbPrintId, ap.getAwbPrintId()).eq(AwbPrintChargesOther::getOrgId, orgId);
		            		  List<AwbPrintChargesOther> listChangeOther = awbPrintChargesOtherMapper.selectList(wrapperAwbPrintChargesOther);
		            		  if(listChangeOther!=null&&listChangeOther.size()>0) {
		            			  for(AwbPrintChargesOther aco:listChangeOther) {
		            				  AwbPrintChargesOther acoInsert = new AwbPrintChargesOther();
		            				  BeanUtils.copyProperties(aco, acoInsert);
		            				  acoInsert.setAwbChargesId(null);
		            				  acoInsert.setAwbPrintId(Integer.valueOf(insertAp.getAwbPrintId()));
		            				  acoInsert.setOrgId(shareOrgId);
		            				  awbPrintChargesOtherMapper.insert(acoInsert);
		            			  }
		            		  }
		            		  
		            		  //运单制单 尺寸表
		            		  LambdaQueryWrapper<AwbPrintSize> wrapperAwbPrintSize = Wrappers.<AwbPrintSize>lambdaQuery();
		            		  wrapperAwbPrintSize.eq(AwbPrintSize::getAwbPrintId, ap.getAwbPrintId()).eq(AwbPrintSize::getOrgId, orgId);
		            		  List<AwbPrintSize> listAwbPrintSize = awbPrintSizeMapper.selectList(wrapperAwbPrintSize);
		            		  if(listAwbPrintSize!=null&&listAwbPrintSize.size()>0) {
		            			  for(AwbPrintSize aps:listAwbPrintSize) {
		            				  AwbPrintSize apsInsert = new AwbPrintSize();
		            				  BeanUtils.copyProperties(aps, apsInsert);
		            				  aps.setAwbSizeId(null);
		            				  aps.setAwbPrintId(Integer.valueOf(insertAp.getAwbPrintId()));
		            				  aps.setOrgId(shareOrgId);
		            				  awbPrintSizeMapper.insert(apsInsert);
		            			  }
		            		  }
		            		  //更新收发货人ID
		            		  awbPrintMapper.updateById(insertAp);
		            	  }
		              }
		              //out
			          LogBean logBeanAwbOut = new LogBean();
			          logBeanAwbOut.setPageName("单证制作");
			          logBeanAwbOut.setPageFunction("协作分享");
//			          ,分享单证号["+sb.toString()+"]"
			          logBeanAwbOut.setLogRemark("协作公司:["+coopName+"]  "+"协作单证->分享成功");
			          logBeanAwbOut.setBusinessScope("AE");
			          logBeanAwbOut.setOrderNumber(orderTwo.getOrderCode());
			          logBeanAwbOut.setOrderId(orderTwo.getOrderId());
			          logBeanAwbOut.setOrderUuid(orderTwo.getOrderUuid());
			          logBeanAwbOut.setCreatorId(SecurityUtils.getUser().getId());
			          logBeanAwbOut.setCreatorName(SecurityUtils.getUser().buildOptName());
			          logBeanAwbOut.setCreatTime(LocalDateTime.now());
			          logBeanAwbOut.setOrgId(orgId);
				  	  logService.save(logBeanAwbOut);
				  	  //in
			          LogBean logBeanAwbIn = new LogBean();
			          logBeanAwbIn.setPageName("单证制作");
			          logBeanAwbIn.setPageFunction("协作接收");
//			          ,订阅单证号["+sb.toString()+"]"
			          logBeanAwbIn.setLogRemark("协作公司:["+shareCoopName+"]  "+"协作单证->订阅成功");
			          logBeanAwbIn.setBusinessScope("AE");
			          logBeanAwbIn.setOrderNumber(shareOrder.getOrderCode());
			          logBeanAwbIn.setOrderId(shareOrder.getOrderId());
			          logBeanAwbIn.setOrderUuid(shareOrder.getOrderUuid());
			          logBeanAwbIn.setCreatorId(SecurityUtils.getUser().getId());
			          logBeanAwbIn.setCreatorName(SecurityUtils.getUser().buildOptName());
			          logBeanAwbIn.setCreatTime(LocalDateTime.now());
			          logBeanAwbIn.setOrgId(shareOrgId);
				  	  logService.save(logBeanAwbIn); 
		          }
		       }else {
		    	      //out
			          LogBean logBeanAwbOut = new LogBean();
			          logBeanAwbOut.setPageName("单证制作");
			          logBeanAwbOut.setPageFunction("协作分享");
			          logBeanAwbOut.setLogRemark("协作公司:["+coopName+"]  "+"协作单证->己方未在协作公司处分享制单操作或者协作方未设置订阅制单操作");
			          logBeanAwbOut.setBusinessScope("AE");
			          logBeanAwbOut.setOrderNumber(orderTwo.getOrderCode());
			          logBeanAwbOut.setOrderId(orderTwo.getOrderId());
			          logBeanAwbOut.setOrderUuid(orderTwo.getOrderUuid());
			          logBeanAwbOut.setCreatorId(SecurityUtils.getUser().getId());
			          logBeanAwbOut.setCreatorName(SecurityUtils.getUser().buildOptName());
			          logBeanAwbOut.setCreatTime(LocalDateTime.now());
			          logBeanAwbOut.setOrgId(orgId);
				  	  logService.save(logBeanAwbOut);
				  	  //in
			          LogBean logBeanAwbIn = new LogBean();
			          logBeanAwbIn.setPageName("单证制作");
			          logBeanAwbIn.setPageFunction("协作接收");
			          logBeanAwbIn.setLogRemark("协作公司:["+shareCoopName+"]  "+"协作单证->己方未在协作公司处订阅制单操作或者协作方未设置分享制单操作");
			          logBeanAwbIn.setBusinessScope("AE");
			          logBeanAwbIn.setOrderNumber(shareOrder.getOrderCode());
			          logBeanAwbIn.setOrderId(shareOrder.getOrderId());
			          logBeanAwbIn.setOrderUuid(shareOrder.getOrderUuid());
			          logBeanAwbIn.setCreatorId(SecurityUtils.getUser().getId());
			          logBeanAwbIn.setCreatorName(SecurityUtils.getUser().buildOptName());
			          logBeanAwbIn.setCreatTime(LocalDateTime.now());
			          logBeanAwbIn.setOrgId(shareOrgId);
				  	  logService.save(logBeanAwbIn); 
		    	   
		       }
			}
		}else {
			throw new RuntimeException("该订单没有协作绑定订单，不能执行此操作。");
		}
	}
}
