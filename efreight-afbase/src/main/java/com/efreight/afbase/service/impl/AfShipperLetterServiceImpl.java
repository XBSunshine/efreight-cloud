package com.efreight.afbase.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.service.LogService;
import com.efreight.common.security.service.EUserDetails;
import lombok.AllArgsConstructor;

import com.efreight.afbase.dao.AfShipperLetterMapper;
import com.efreight.afbase.dao.LogMapper;
import com.efreight.afbase.service.AfShipperLetterService;
import com.efreight.common.security.util.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * AF 订单管理 出口订单 托书信息 服务实现类
 * </p>
 *
 * @author qipm
 * @since 2019-10-10
 */
@Service
@AllArgsConstructor
public class AfShipperLetterServiceImpl extends ServiceImpl<AfShipperLetterMapper, AfShipperLetter> implements AfShipperLetterService {
	 private final LogMapper logMapper;
	private final LogService logService;
	@Override
	public List<AfShipperLetter> getListPage(AfShipperLetter bean) {
		List<AfShipperLetter> resultList=new ArrayList<AfShipperLetter>();
		if (bean.getAwbId()==null||"".equals(bean.getAwbId())) {
			//分单
			List<AfOrder> orderList=baseMapper.getOrderList2(bean.getOrderId(),SecurityUtils.getUser().getOrgId());
			for (int i = 0; i < orderList.size(); i++) {
				AfOrder order=orderList.get(i);
				List<AfShipperLetter> letterList=baseMapper.getLetterList(order.getOrderId(),SecurityUtils.getUser().getOrgId(),"HAWB");
				resultList.addAll(letterList);
				for (int j = 0; j < order.getHawbQuantity()-letterList.size(); j++) {
					AfShipperLetter letter=new AfShipperLetter();
					letter.setSlType("HAWB");
					letter.setMawbNumber(order.getAwbNumber());
					letter.setOrderCode(order.getOrderCode());
					letter.setOrderId(order.getOrderId());
					letter.setOrderUuid(order.getOrderUuid());
					letter.setGoodsNameCn(order.getGoodsNameCn());
					letter.setAwbId(order.getAwbId());
					
					if (order.getHawbQuantity()==1) {
						letter.setPlanPieces(order.getConfirmPieces()==null?order.getPlanPieces():order.getConfirmPieces());
						letter.setPlanWeight(order.getConfirmWeight()==null?order.getPlanWeight():order.getConfirmWeight());
					}else{
						letter.setPlanPieces(0);
						letter.setPlanWeight(new BigDecimal(0));
					}
					
					
					letter.setArrivalStation(order.getArrivalStation());
					letter.setDepartureStation(order.getDepartureStation());
					resultList.add(letter);
				}
				
			}
		} else {
			//主单
			List<AfShipperLetter> awbOrderList=baseMapper.getMAWBLetterList(bean.getAwbId(),SecurityUtils.getUser().getOrgId(),"MAWB");
			if (awbOrderList.size()>0) {
				resultList.add(awbOrderList.get(0));
			} else {
				AfShipperLetter map=baseMapper.getSum(bean.getAwbId(),SecurityUtils.getUser().getOrgId());
				AfShipperLetter letter=new AfShipperLetter();
				letter.setSlType("MAWB");
				letter.setMawbNumber(bean.getMawbNumber());
//				letter.setOrderCode(bean.getOrderCode());
				letter.setOrderId(bean.getOrderId());
				letter.setOrderUuid(bean.getOrderUuid());
				letter.setGoodsNameCn(bean.getGoodsNameCn());
				letter.setAwbId(bean.getAwbId());
				
				letter.setPlanPieces(map.getPlanPieces());
				letter.setPlanWeight(map.getPlanWeight());
				letter.setArrivalStation(bean.getArrivalStation());
				letter.setDepartureStation(bean.getDepartureStation());
				resultList.add(letter);
			}
			//分单
			List<AfOrder> orderList=baseMapper.getOrderList(bean.getAwbId(),SecurityUtils.getUser().getOrgId());
			for (int i = 0; i < orderList.size(); i++) {
				AfOrder order=orderList.get(i);
				List<AfShipperLetter> letterList=baseMapper.getLetterList(order.getOrderId(),SecurityUtils.getUser().getOrgId(),"HAWB");
				resultList.addAll(letterList);
				for (int j = 0; j < order.getHawbQuantity()-letterList.size(); j++) {
					AfShipperLetter letter=new AfShipperLetter();
					letter.setSlType("HAWB");
					letter.setMawbNumber(order.getAwbNumber());
					letter.setOrderCode(order.getOrderCode());
					letter.setOrderId(order.getOrderId());
					letter.setOrderUuid(order.getOrderUuid());
					letter.setGoodsNameCn(order.getGoodsNameCn());
					letter.setAwbId(order.getAwbId());
					
//					letter.setPlanPieces(order.getPlanPieces());
//					letter.setPlanWeight(order.getPlanWeight());
					if (order.getHawbQuantity()==1) {
						letter.setPlanPieces(order.getConfirmPieces()==null?order.getPlanPieces():order.getConfirmPieces());
						letter.setPlanWeight(order.getConfirmWeight()==null?order.getPlanWeight():order.getConfirmWeight());
					}else{
						letter.setPlanPieces(0);
						letter.setPlanWeight(new BigDecimal(0));
					}
					letter.setArrivalStation(order.getArrivalStation());
					letter.setDepartureStation(order.getDepartureStation());
					resultList.add(letter);
				}
				
			}
		}
		//baseMapper.getListPage( bean.getProjectId(), SecurityUtils.getUser().getOrgId());
		return resultList;
	}
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean doSave(AfShipperLetter bean) {	
		bean.setCreateTime(LocalDateTime.now());
		bean.setCreatorId(SecurityUtils.getUser().getId());
		bean.setCreatorName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		baseMapper.insert(bean);
		return true;
	}
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean doUpdate(AfShipperLetter bean) {
		bean.setEditTime(LocalDateTime.now());
		bean.setEditorId(SecurityUtils.getUser().getId());
		bean.setEditorName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
		UpdateWrapper<AfShipperLetter> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("sl_id", bean.getSlId());
		baseMapper.update(bean, updateWrapper);
		return true;
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean doDelete(AfShipperLetter bean) {
		baseMapper.deleteById(bean.getSlId());
		//日志
		LogBean logBean = new LogBean();
		logBean.setBusinessScope("AE");
		logBean.setLogType("受控操作");
        logBean.setNodeName("出口订单");
		logBean.setPageName("出口订单");
        logBean.setPageFunction("删除舱单");
        
        logBean.setOrderNumber(bean.getOrderCode());
        logBean.setOrderUuid(bean.getOrderUuid());
        logBean.setLogRemark(bean.getHawbNumber());
        
        logBean.setCreatorId(SecurityUtils.getUser().getId());
        logBean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        logBean.setCreatTime(LocalDateTime.now());
        logBean.setOrgId(SecurityUtils.getUser().getOrgId());
		logMapper.insert(logBean);
		return true;
	}


	@Override
	public Boolean saveAiShippers(AfShipperLetter bean) {
		Integer slId = bean.getSlId();
		EUserDetails user = SecurityUtils.getUser();
		if(slId == null){
			//第一次制作
			bean.setSlType("MAWB");
			bean.setOrgId(user.getOrgId());
			bean.setOrderUuid(bean.getOrderUuid());
			bean.setCreateTime(LocalDateTime.now());
			bean.setCreatorId(user.getId());
			bean.setCreatorName(user.getUserCname() + " " + user.getUserEmail());

			bean.setEditTime(LocalDateTime.now());
			bean.setEditorId(user.getId());
			bean.setEditorName(user.getUserCname() + " " + user.getUserEmail());

			baseMapper.insert(bean);
		}else{
			bean.setEditTime(LocalDateTime.now());
			bean.setEditorId(user.getId());
			bean.setEditorName(user.getUserCname() + " " + user.getUserEmail());
			baseMapper.updateById(bean);
		}

		//添加日志信息
		LogBean logBean = new LogBean();
		logBean.setBusinessScope("AI");
		logBean.setLogType("AI 订单");
		logBean.setNodeName("进口订单");
		logBean.setPageName("进口订单");
		logBean.setPageFunction("制作舱单");

		logBean.setOrderNumber(bean.getOrderCode());
		logBean.setOrderUuid(bean.getOrderUuid());
		logBean.setLogRemark(bean.getAwbNumber());

//		logBean.setLogRemark(this.getLogRemark(order, bean));
		logBean.setBusinessScope("AI");
		logBean.setAwbNumber(bean.getAwbNumber());
		logBean.setOrderId(bean.getOrderId());
		logBean.setOrderUuid(bean.getOrderUuid());
		logService.saveLog(logBean);

		return true;
	}
}
