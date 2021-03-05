package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.AfAwbRouteHawbMapper;
import com.efreight.afbase.dao.AfAwbRouteMapper;
import com.efreight.afbase.entity.AfAwbRoute;
import com.efreight.afbase.entity.route.AfAwbRouteHawb;
import com.efreight.afbase.service.AfAwbRouteService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class AfAwbRouteServiceImpl extends ServiceImpl<AfAwbRouteMapper, AfAwbRoute> implements AfAwbRouteService{

	private final AfAwbRouteHawbMapper afAwbRouteHawbMapper;
	
	@Override
	public List<AfAwbRoute> queryAfAwbRoute(String awbNumberStr, String[] arrayAwbNumber) {
		List<AfAwbRoute> listReturn = new ArrayList<AfAwbRoute>();
		if(StrUtil.isBlank(awbNumberStr)) {
			listReturn = baseMapper.queryAfAwbRouteListAll();
		}else {
			listReturn = baseMapper.queryAfAwbRouteList(arrayAwbNumber);
		}
		//更新查到的结果集  讲为追踪变为已追踪
		if(listReturn!=null&&listReturn.size()>0) {
			for(AfAwbRoute route:listReturn) {
				route.setIsTrack(1);
				route.setTrackTime(LocalDateTime.now());
				LambdaQueryWrapper<AfAwbRoute> afAwbRouteWrapper = Wrappers.<AfAwbRoute>lambdaQuery();
				afAwbRouteWrapper.eq(AfAwbRoute::getAwbNumber,route.getAwbNumber());
				baseMapper.update(route,afAwbRouteWrapper);
			}
		}
		return listReturn;
	}

	@Override
	public String inputAfAwbRoute() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AfAwbRoute findByAwbNumber(String awbNumber) {
		Assert.hasText(awbNumber, "非法参数");
		LambdaQueryWrapper<AfAwbRoute> queryWrapper = Wrappers.lambdaQuery();
		queryWrapper.eq(AfAwbRoute::getAwbNumber, awbNumber);
		return this.baseMapper.selectOne(queryWrapper);
	}

	@Override
	public Integer insert(AfAwbRoute afAwbRoute) {
		AfAwbRoute dbRoute = this.findByAwbNumber(afAwbRoute.getAwbNumber());
		Integer id;
		if(null == dbRoute){
			afAwbRoute.setCreateTime(LocalDateTime.now());
			this.save(afAwbRoute);
			id = afAwbRoute.getAwbRouteId();
		}else{
			id = dbRoute.getAwbRouteId();
		}
		return id;
	}

	@Override
	public boolean saveRouteInfo(String awbNumber, String hawNumber, String businessScope) {
		if(StringUtils.isBlank(awbNumber)){
			log.warn("[awbNumber] Parameter cannot be empty.");
			return false;
		}
		//保存主单追踪信息
		int[] result  = this.saveAwbRouteInfo(awbNumber);
		//保存分单追踪信息
		this.saveHawbRouteInfo(result[1], awbNumber, hawNumber, businessScope);
		return result[0] == 1;
	}


	/**
	 *
	 * @param awbNumber
	 * @return Array array[0]:是否为新增,1为是 0为否，array[1]:数据ID
	 */
	private int[] saveAwbRouteInfo(String awbNumber){
		LambdaQueryWrapper<AfAwbRoute> afAwbRouteWrapper = Wrappers.lambdaQuery();
		afAwbRouteWrapper.eq(AfAwbRoute::getAwbNumber, awbNumber);
		List<AfAwbRoute> afAwbRouteList = this.list(afAwbRouteWrapper);
		int[] result = new int[2];
		if (afAwbRouteList.size() == 0) {
			//插入主单路线信息
			AfAwbRoute afAwbRoute = new AfAwbRoute();
			afAwbRoute.setAwbNumber(awbNumber);
			afAwbRoute.setIsTrack(0);
			afAwbRoute.setCreateTime(LocalDateTime.now());
			this.save(afAwbRoute);
			Integer id = afAwbRoute.getAwbRouteId();
			result[0] = 1;
			result[1] = id;
		}else{
			AfAwbRoute dbAwbRoute = afAwbRouteList.get(0);
			result[0] = 0;
			result[1] = dbAwbRoute.getAwbRouteId();
			if(dbAwbRoute.getTrackTime() != null && dbAwbRoute.getTrackTime().plusMonths(1).isBefore(LocalDateTime.now())){
			    dbAwbRoute.setIsTrack(0);
				this.baseMapper.updateById(dbAwbRoute);
			}
		}
		return result;
	}

	private void saveHawbRouteInfo(Integer awbRouteId, String awbNumber, String hawbNumber, String businessScope){
		if(StringUtils.isNotBlank(hawbNumber)){
			LambdaQueryWrapper<AfAwbRouteHawb> queryWrapper = Wrappers.lambdaQuery();
			queryWrapper.eq(AfAwbRouteHawb::getAwbNumber, awbNumber);
			queryWrapper.eq(AfAwbRouteHawb::getHawbNumber, hawbNumber);
			if(StringUtils.isNotBlank(businessScope)){
				queryWrapper.eq(AfAwbRouteHawb::getBusinessScope, businessScope);
			}
			List<AfAwbRouteHawb> afAwbRouteHawbList = afAwbRouteHawbMapper.selectList(queryWrapper);
			if(afAwbRouteHawbList.size() == 0){
				AfAwbRouteHawb afAwbRouteHawb = new AfAwbRouteHawb();
				afAwbRouteHawb.setAwbNumber(awbNumber);
				afAwbRouteHawb.setHawbNumber(hawbNumber);
				afAwbRouteHawb.setIsTrack(0);
				afAwbRouteHawb.setAwbRouteId(awbRouteId);
				afAwbRouteHawb.setBusinessScope(businessScope);
				afAwbRouteHawb.setCreateTime(LocalDateTime.now());
				afAwbRouteHawbMapper.insert(afAwbRouteHawb);
			}
		}
	}

}
