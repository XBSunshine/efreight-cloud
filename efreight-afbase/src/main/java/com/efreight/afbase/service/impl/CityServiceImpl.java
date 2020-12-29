package com.efreight.afbase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.CityMapper;
import com.efreight.afbase.entity.City;
import com.efreight.afbase.entity.view.AirportCitySearch;
import com.efreight.afbase.service.CityService;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
@Service
@AllArgsConstructor
public class CityServiceImpl extends ServiceImpl<CityMapper, City> implements CityService {
	private final CityMapper cityMapper;
	
	@Override
	public IPage<City> getListPage(Page page, City bean) {
		return baseMapper.getListPage(page, bean);
	}

	@Override
	public int saveCity(City city) {
		City dbCity = getCityByCode(city.getCityCode());
		if(null != dbCity){
			throw new IllegalArgumentException("城市代码已经存在");
		}
		EUserDetails userDetails = SecurityUtils.getUser();
		city.setCreatorId(userDetails.getId());
		city.setCreatorName(buildName(userDetails));
		city.setCreateTime(LocalDateTime.now());
		return baseMapper.insert(city);
	}


	public City getCityByCode(String CityCode) {
		LambdaQueryWrapper<City> wrapper = Wrappers.<City>lambdaQuery();
		wrapper.eq(City::getCityCode,CityCode);
		return baseMapper.selectOne(wrapper);
	}

	private String buildName(EUserDetails userDetails){
		StringBuilder builder = new StringBuilder();
		builder.append(userDetails.getUserCname());
		builder.append(" ");
		builder.append(userDetails.getUserEmail());
		return builder.toString();
	}

	@Override
	public int editCity(City city) {
		if(null == city.getCityId()){
			throw new IllegalArgumentException("未查询到该数据");
		}
		City dbCity = getCityByCode(city.getCityCode());
		if(null != dbCity && !dbCity.getCityId().equals(city.getCityId())){
			throw new IllegalArgumentException("城市代码已经存在");
		}
		EUserDetails userDetails = SecurityUtils.getUser();
		city.setEditorId(userDetails.getId());
		city.setEditorName(buildName(userDetails));
		city.setEditTime(LocalDateTime.now());
		return baseMapper.updateById(city);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeCityById(String cityId) {
		baseMapper.deleteById(cityId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void importData(List<City> list) {
		for (int i = 0; i < list.size(); i++) {
			baseMapper.insert(list.get(i));
		}	
	}

	@Override
	public List<AirportCitySearch> searchCity(String key) {
		if(StringUtils.isBlank(key)){
			return Collections.emptyList();
		}
		return this.cityMapper.searchCities(key);
	}
}
