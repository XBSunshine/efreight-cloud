package com.efreight.afbase.service.impl;

import com.efreight.afbase.dao.AfCostMapper;
import com.efreight.afbase.dao.AfOrderMapper;
import com.efreight.afbase.dao.RountingSignMapper;
import com.efreight.afbase.entity.AfCost;
import com.efreight.afbase.entity.AfOrder;
import com.efreight.afbase.entity.LogBean;
import com.efreight.afbase.entity.RountingSign;
import com.efreight.afbase.service.LogService;
import com.efreight.afbase.service.RountingSignService;
import com.efreight.common.security.util.SecurityUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * AF 订单管理 出口订单 签单表 服务实现类
 * </p>
 *
 * @author cwd
 * @since 2020-11-18
 */
@Service
@AllArgsConstructor
@Slf4j
public class RountingSignServiceImpl extends ServiceImpl<RountingSignMapper, RountingSign> implements RountingSignService {
	private final AfOrderMapper afOrderMapper;
	private final AfCostMapper afCostMapper;
	private final LogService logService;
	@Override
	public Map checkOrderCost(RountingSign bean) {
        //签约公司设置了AE 是航线签约
        //当前订单的服务产品不为空 且 IN （启用签单功能支持的服务产品rounting_sign_business_product）
        //当前订单 配了主单号 （order表 主单uuid不为NULL）
        //当前订单 成本完成cost_recorded = 否
        //当前订单 cost表 服务=‘干线 – 空运费’ 的成本，从未对账 且 从未锁账日期 才可签单
		
		//查询订单信息
		AfOrder order = afOrderMapper.selectById(bean.getOrderId());
		
		//cost干线-空运费 信息
		List<Map> list = baseMapper.getCostByWhere(SecurityUtils.getUser().getOrgId(),bean.getOrderId());
		
		//根据订单id签约公司id 取签单数据
		RountingSign  checkSignQuery = new RountingSign();
		checkSignQuery.setOrderId(bean.getOrderId());
		checkSignQuery.setOrgId(SecurityUtils.getUser().getOrgId());
		checkSignQuery.setBusinessScope(order.getBusinessScope());
		RountingSign  checkSign =  baseMapper.getRountingSign(checkSignQuery);
		if(checkSign!=null) {
			if(checkSign.getSignState()==1&&checkSign.getEditorId()!=null) {
				if(checkSign.getEditorId().intValue()!=SecurityUtils.getUser().getId().intValue()&&
						checkSign.getRoutingPersonId().intValue()!=SecurityUtils.getUser().getId().intValue()) {
					throw new RuntimeException("您不是该订单的 签单人或 航线负责人，不能操作！");
				}
			}
		}
		//查询当前签约公司AE 的设置 值
		Map map = baseMapper.getOrgConfigForWhere(SecurityUtils.getUser().getOrgId(),"AE");
		if(map!=null&&map.containsKey("rounting_sign")) {
			if("false".equals(map.get("rounting_sign").toString())) {
				//当前签约公司AE订单没有签单设置
				throw new RuntimeException("当前签约公司AE订单没有设置签单");
			}else if(order!=null&&StringUtils.isEmpty(order.getBusinessProduct())){
				//当前订单没有服务产品
				throw new RuntimeException("当前订单没有服务产品");
			}else if(order!=null&&!StringUtils.isEmpty(order.getBusinessProduct())&&!(map.get("rounting_sign_business_product").toString().contains(order.getBusinessProduct()))) {
				//当前签约公司设置的AE订单签单服务产品不包含当前订单的服务产品
				throw new RuntimeException("当前签约公司设置的AE订单签单服务产品不包含当前订单的服务产品");
			}else if(order!=null &&StringUtils.isEmpty(order.getAwbUuid())) {
				//当前订单没有主单号
				throw new RuntimeException("当前订单没有主单号");
			}else if(order!=null &&order.getCostRecorded()) {
				//当前订单成本已完成
				throw new RuntimeException("当前订单成本已完成");
			}else if(list!=null&&list.size()>0) {
				//服务【干线 – 空运费】成本存在已对账或锁账记录
				throw new RuntimeException("服务【干线 – 空运费】成本存在已对账或锁账记录");
			}
		}else {
			//当前签约公司AE订单没有签单设置
			throw new RuntimeException("当前签约公司AE订单没有设置签单");
		}
		Map mapResult = new HashMap();
		mapResult.put("status", true);
		return mapResult;
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveOrModify(RountingSign bean) {
		//校验
		this.checkOrderCost(bean);
		//更新订单 服务产品、目的港、航班号、航班日期、中转港1、中转港2  订单状态：航线签单
		  //查询订单信息
	      AfOrder order = afOrderMapper.selectById(bean.getOrderId());
	      if(order!=null) {
	    	  order.setBusinessProduct(bean.getBusinessProduct());
	    	  order.setArrivalStation(bean.getArrivalStation());
	    	  order.setExpectFlight(bean.getExpectFlight());
	    	  order.setExpectDeparture(bean.getExpectDeparture());
	    	  order.setTransitStation(bean.getTransitStation());
	    	  order.setTransitStation2(bean.getTransitStation2());
	    	  order.setOrderStatus("航线签单");
	    	  order.setEditorId(SecurityUtils.getUser().getId());
	    	  order.setEditorName(SecurityUtils.getUser().buildOptName());
	    	  order.setEditTime(new Date());
	    	  if("单价".equals(bean.getMsrPriceType())) {
	    		  order.setMsrUnitprice(Double.valueOf(bean.getMsrUnitprice().toString()));
		    	  order.setMsrAmount(null);
	    	  }else {
	    		  order.setMsrUnitprice(null);
		    	  order.setMsrAmount(Double.valueOf(bean.getMsrUnitprice().toString()));
	    	  }
	    	  order.setMsrCurrecnyCode("CNY");
	    	  afOrderMapper.updateById(order);
	      }
		//签单内容
	    if(bean.getRountingSignId()!=null) {
	    	RountingSign ss = baseMapper.selectById(bean);
	    	if(ss.getSignState()==1&&ss.getEditorId()!=null) {
	    		if(ss.getEditorId().intValue()!=SecurityUtils.getUser().getId().intValue()&&ss.getRoutingPersonId().intValue()!=SecurityUtils.getUser().getId().intValue()) {
	    			throw new RuntimeException("您不是该订单的 签单人或 航线负责人，不能操作！");
	    		}
	    	}
	    	//更新
	    	ss.setSignState(1);
	    	ss.setRowUuid(UUID.randomUUID().toString());
	    	ss.setEditorId(SecurityUtils.getUser().getId());
	    	ss.setEditorName(SecurityUtils.getUser().buildOptName());
	    	ss.setEditTime(LocalDateTime.now());
	    	ss.setIncomeWeight(bean.getIncomeWeight());
	    	ss.setCostWeight(bean.getCostQuantity());
	    	if("单价".equals(bean.getMsrPriceType())) {
	    		 ss.setMsrUnitprice(bean.getMsrUnitprice());
	    		 ss.setMsrAmount(null);
	    	}else {
	    		 ss.setMsrUnitprice(null);
	    		 ss.setMsrAmount(bean.getMsrUnitprice());
	    	}
	    	if("单价".equals(bean.getCostPriceType())) {
	    		 ss.setCuAmount(null);
	    		 ss.setCuUnitprice(bean.getCostUnitPrice());
	    	}else {
	    		 ss.setCuUnitprice(null);
	    		 ss.setCuAmount(bean.getCostUnitPrice());
	    	}
	    	ss.setMsrFunctionalAmount(bean.getMsrFunctionalAmount());
	    	ss.setRoutingPersonName(bean.getRoutingPersonName());
	    	ss.setRoutingPersonId(bean.getRoutingPersonId());
	    	ss.setCuFunctionalAmount(bean.getCostAmount());
	    	baseMapper.updateById(ss);
	    }else {
	    	//新增
	    	bean.setSignState(1);
	    	bean.setBusinessScope("AE");
	    	bean.setOrgId(SecurityUtils.getUser().getOrgId());
	    	bean.setRowUuid(UUID.randomUUID().toString());
//	    	bean.setCuUnitprice(bean.getCostUnitPrice());
	    	if("单价".equals(bean.getMsrPriceType())) {
	    		 bean.setMsrAmount(null);
	    	}else {
	    		 bean.setMsrUnitprice(null);
	    		 bean.setMsrAmount(bean.getMsrUnitprice());
	    	}
	    	if("单价".equals(bean.getCostPriceType())) {
	    		 bean.setCuAmount(null);
	    		 bean.setCuUnitprice(bean.getCostUnitPrice());
	    	}else {
	    		 bean.setCuUnitprice(null);
	    		 bean.setCuAmount(bean.getCostUnitPrice());
	    	}
	    	bean.setCuFunctionalAmount(bean.getCostAmount());
	    	bean.setCostWeight(bean.getCostQuantity());
	    	bean.setEditorId(SecurityUtils.getUser().getId());
	    	bean.setEditorName(SecurityUtils.getUser().buildOptName());
	    	bean.setEditTime(LocalDateTime.now());
	    	baseMapper.insert(bean);
	    }
		
		//成本金额写入af_cost
		   //1、先删除已存在 的“干线 - 空运费” 成本  删除条件：未锁账、未对账
	  		List<AfCost> list = baseMapper.getCostByWhere2(SecurityUtils.getUser().getOrgId(),bean.getOrderId());
		    if(list!=null&&list.size()>0) {
		    	list.stream().forEach(o->{
		    		afCostMapper.deleteById(o.getCostId());
		    	});
		    }
		   //2、插入新内容
		    AfCost cost = new AfCost();
		    cost.setOrderId(bean.getOrderId());
		    cost.setOrgId(SecurityUtils.getUser().getOrgId());
		    cost.setCustomerId(bean.getAwbFromId());
		    cost.setCustomerName(bean.getAwbFromName());
		    List<Map> sList = baseMapper.getAfService(SecurityUtils.getUser().getOrgId());
		    if(sList!=null&&sList.size()>0) {
		    	Map m = sList.get(0);
		    	cost.setServiceId(Integer.valueOf(m.get("service_id").toString()));
			    cost.setServiceName("干线 - 空运费");
//			    if(m.containsKey("service_remark")&&m.get("service_remark")!=null) {
//			    	cost.setServiceRemark(m.get("service_remark").toString());
//			    	cost.setServiceNote(m.get("service_remark").toString());
//			    }
			    cost.setServiceRemark("航线签单");
		    	cost.setServiceNote("航线签单");
			    
		    }
		    if("单价".equals(bean.getCostPriceType())) {
		    	cost.setCostQuantity(bean.getCostQuantity());
		    	cost.setCostUnitPrice(bean.getCostUnitPrice());
	    	}else {
	    		cost.setCostQuantity(new BigDecimal(1));
	    		cost.setCostUnitPrice(bean.getCostAmount());
	    	}
		    cost.setCostAmount(bean.getCostAmount());
		    cost.setCostFunctionalAmount(bean.getCostAmount());
		    cost.setCostCurrency("CNY");
		    cost.setCostExchangeRate(new BigDecimal(1.000));
		    cost.setMainRouting(cost.getCostAmount());
		    cost.setRowUuid(UUID.randomUUID().toString());
		    cost.setBusinessScope("AE");
		    cost.setOrderUuid(order.getOrderUuid());
		    cost.setCreatorId(SecurityUtils.getUser().getId());
		    cost.setCreatorName(SecurityUtils.getUser().buildOptName());
		    cost.setCreateTime(LocalDateTime.now());
		    afCostMapper.insert(cost);
		    //更新订单应付情况状态
		    afOrderMapper.updateOrderCostStatus(SecurityUtils.getUser().getOrgId(), order.getOrderUuid(), "已录成本", UUID.randomUUID().toString());
		    //更新订单日志
		    LogBean logBean = new LogBean();
	        logBean.setPageName("AE订单");
	        logBean.setPageFunction("航线签单");
	        logBean.setBusinessScope("AE");
	        logBean.setOrderNumber(order.getOrderCode());
	        logBean.setOrderId(order.getOrderId());
	        logBean.setOrderUuid(order.getOrderUuid());
	        logBean.setLogRemark("航线负责人：" + bean.getRoutingPersonName().split(" ")[0]);
	        logService.saveLog(logBean);
	}

	@Override
	public RountingSign getRountingSign(RountingSign bean) {
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		return baseMapper.getRountingSign(bean);
	}

	@Override
	public Map concelSign(RountingSign bean) {
		
		//查询订单信息
		AfOrder order = afOrderMapper.selectById(bean.getOrderId());
		
		//查询签单信息
		bean.setBusinessScope(order.getBusinessScope());
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		bean.setSignState(1);
		RountingSign sign = baseMapper.getRountingSign(bean);
		if(sign==null) {
			throw new RuntimeException("当前订单未航线签单");
		}
		if(sign.getSignState()==1&&sign.getEditorId()!=null) {
			if(sign.getEditorId().intValue()!=SecurityUtils.getUser().getId().intValue()&&sign.getRoutingPersonId().intValue()!=SecurityUtils.getUser().getId().intValue()) {
				throw new RuntimeException("您不是该订单的 签单人或 航线负责人，不能操作！");
			}
		}
		if(order.getCostRecorded()){
			throw new RuntimeException("当前订单成本完成无法撤销航线签单");
		}
		//cost干线-空运费 信息
		List<Map> list = baseMapper.getCostByWhere(SecurityUtils.getUser().getOrgId(),bean.getOrderId());
		if(list!=null&&list.size()>0) {
			throw new RuntimeException("当前订单成本有锁账或对账操作无法撤销航线签单");
		}
		sign.setSignState(0);
		sign.setEditorId(SecurityUtils.getUser().getId());
		sign.setEditorName(SecurityUtils.getUser().buildOptName());
		sign.setEditTime(LocalDateTime.now());
		sign.setRowUuid(UUID.randomUUID().toString());
		sign.setMsrFunctionalAmount(null);
		sign.setMsrUnitprice(null);
		sign.setMsrAmount(null);
		sign.setCuAmount(null);
		sign.setCuFunctionalAmount(null);
		sign.setCuUnitprice(null);
		baseMapper.updateById(sign);
		//更新 订单状态
		if(order.getConfirmWeight()!=null) {
			order.setOrderStatus("货物出重");
		}else {
			order.setOrderStatus("舱位确认");
		}
		
		if("已录成本".equals(order.getCostStatus())) {
			List<AfCost> list2 = baseMapper.getCostByWhere3(SecurityUtils.getUser().getOrgId(),bean.getOrderId());
			if(list2!=null&&list2.size()>0) {
				//todo
			}else {
				order.setCostStatus("未录成本");
			}
		}
		order.setRowUuid(UUID.randomUUID().toString());
		order.setEditorId(SecurityUtils.getUser().getId());
		order.setEditorName(SecurityUtils.getUser().buildOptName());
		order.setEditTime(new Date());
		afOrderMapper.updateById(order);
		//更新订单日志
	    LogBean logBean = new LogBean();
        logBean.setPageName("AE订单");
        logBean.setPageFunction("撤销签单");
        logBean.setBusinessScope("AE");
        logBean.setOrderNumber(order.getOrderCode());
        logBean.setOrderId(order.getOrderId());
        logBean.setOrderUuid(order.getOrderUuid());
//        logBean.setLogRemark("航线负责人：" + sign.getRoutingPersonName());
        logService.saveLog(logBean);
		Map map = new HashMap();
		return map;
	}

	/**当前签约公司公司配置->AE订单配置 启用了签单功能， rounting_sign = 1；
	 *当前订单的服务产品不为空 且 IN （启用签单功能支持的服务产品rounting_sign_business_product）；
	 *且 订单 签单表 状态=0；
     *满足条件：则 弹出提示“航线未签订，不能做成本完成！”
	 */
	@Override
	public Map checkCostRecord(RountingSign bean) {
		boolean flag = false;
		
		//查询订单信息
		AfOrder order = afOrderMapper.selectById(bean.getOrderId());
		//查询当前签约公司AE 的设置 值
		Map map = baseMapper.getOrgConfigForWhere(SecurityUtils.getUser().getOrgId(),"AE");
		if(map!=null&&map.containsKey("rounting_sign")) {
			if("true".equals(map.get("rounting_sign").toString())) {
				//当前签约公司AE订单没有签单设置
				flag = true;
			}else {
				flag = false;
			}
			if(flag&&order!=null&&!StringUtils.isEmpty(order.getBusinessProduct())&&(map.get("rounting_sign_business_product").toString().contains(order.getBusinessProduct()))) {
				flag = true;
			}else {
				flag = false;
			}
		}
		//查询签单信息
		bean.setBusinessScope("AE");
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		RountingSign sign = baseMapper.getRountingSign(bean);
		if(flag&&sign!=null&&sign.getSignState()==0) {
			flag = true;
		}else {
			flag = false;
		}
		
		if(flag) {
			throw new RuntimeException("航线未签订，不能做成本完成！");
		}
		//备用
		Map mapResult = new HashMap();
		return mapResult;
	}

}
