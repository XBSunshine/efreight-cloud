package com.efreight.afbase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.AirportMapper;
import com.efreight.afbase.entity.Airport;
import com.efreight.afbase.entity.view.AirportCitySearch;
import com.efreight.afbase.entity.view.AirportCountrySearch;
import com.efreight.afbase.entity.view.AirportSearch;
import com.efreight.afbase.service.AirportService;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
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
public class AirportServiceImpl extends ServiceImpl<AirportMapper, Airport> implements AirportService {
	private final AirportMapper airportMapper;
	@Override
	public IPage<Airport> getListPage(Page page, Airport bean) {
		QueryWrapper<Airport> queryWrapper = new QueryWrapper<>();
		if(bean.getApCode()!=null && !"".equals(bean.getApCode())) {
			queryWrapper.like("ap_code", bean.getApCode());
		}
		if(bean.getApNameCn()!=null && !"".equals(bean.getApNameCn())) {
			String keys = bean.getApNameCn();
			queryWrapper.and(wrapper -> wrapper.like("ap_name_cn", keys).or().like("ap_name_en", keys));
		}
		if(bean.getCityNameCn()!=null && !"".equals(bean.getCityNameCn())) {
			String keys = bean.getCityNameCn();
			//如果录入的 3位 或 4位 英文字母，则 只按 城市代码查询
			if(keys.length() == 3 || keys.length() == 4){
				if(keys.matches("[a-zA-Z]+")){
					queryWrapper.and(wrapper -> wrapper.like("city_code", keys));
                }else{
					queryWrapper.and(wrapper -> wrapper.like("city_code", keys).or().like("city_name_en", keys).or().like("city_name_cn", keys));
				}
			}else{
				queryWrapper.and(wrapper -> wrapper.like("city_code", keys).or().like("city_name_en", keys).or().like("city_name_cn", keys));
			}
		}
		if(bean.getNationNameCn()!=null && !"".equals(bean.getNationNameCn())) {
			String keys = bean.getNationNameCn();
			//如果录入的 2位 英文字母，则 只按 国家代码查询
			if(keys.length() == 2){
				if(keys.matches("[a-zA-Z]+")){
					queryWrapper.and(wrapper -> wrapper.like("nation_code", keys));
				}else{
					queryWrapper.and(wrapper -> wrapper.like("nation_code", keys).or().like("nation_name_en", keys).or().like("nation_name_cn", keys));
				}
			}else{
				queryWrapper.and(wrapper -> wrapper.like("nation_code", keys).or().like("nation_name_en", keys).or().like("nation_name_cn", keys));
			}


		}
		queryWrapper.orderByAsc("ap_code");
		return baseMapper.selectPage(page, queryWrapper);
	}
	@Override
	public List<Airport> isHaved(String ap_code,String nation_code) {
		QueryWrapper<Airport> queryWrapper = new QueryWrapper<>();
		
		queryWrapper.eq("ap_code", ap_code);
		queryWrapper.eq("nation_code", nation_code);
		
		return baseMapper.selectList(queryWrapper);
	}
	@Override
	public List<Airport> checkCode(Airport bean) {
		QueryWrapper<Airport> queryWrapper = new QueryWrapper<>();
		
		queryWrapper.eq("ap_code", bean.getApCode());
		
		return baseMapper.selectList(queryWrapper);
	}
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void importData(List<Airport> list) {
		Date date1=new Date();
//		airportMapper.truncateTable();
		for (int i = 0; i < list.size(); i++) {
			System.out.println(i);
			baseMapper.insert(list.get(i));
		}
		Date date2=new Date();
		System.out.println("==========="+getDistanceTime2(date1,date2));
	}

	@Override
	public Airport getAirportCityNameENByApCode(String apCode) {
		LambdaQueryWrapper<Airport> wrapper = Wrappers.<Airport>lambdaQuery();
		wrapper.eq(Airport::getApCode,apCode);
		return baseMapper.selectOne(wrapper);
	}
	@Override
	public Airport getAirport(String cityCode) {
		List<Airport> list=baseMapper.getAirport(cityCode);
		if(list!=null&&list.size()>0) {
			return list.get(0);
		}else {
			return null;
		}
		
	}

	@Override
    @Transactional(rollbackFor = Exception.class)
	public int saveAirport(Airport airport) {
		Airport dbAirport = getAirportCityNameENByApCode(airport.getApCode());
		if(null != dbAirport){
			throw new IllegalArgumentException("机场代码已经存在");
		}
		EUserDetails userDetails = SecurityUtils.getUser();
		airport.setCreatorId(userDetails.getId());
		airport.setCreatorName(buildName(userDetails));
		airport.setCreateTime(LocalDateTime.now());
		if("".equals(airport.getNationCodeThree())){
			airport.setNationCodeThree(null);
		}
		if("".equals(airport.getNationCodeNumber())){
			airport.setNationCodeNumber(null);
		}
		if("".equals(airport.getNationCodeCoo())){
			airport.setNationCodeCoo(null);
		}
		return baseMapper.insert(airport);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int editAirport(Airport airport) {
		if(null == airport.getApId()){
			throw new IllegalArgumentException("未查询到该数据");
		}
		Airport dbAirport = getAirportCityNameENByApCode(airport.getApCode());
		if(null != dbAirport && !dbAirport.getApId().equals(airport.getApId())){
			throw new IllegalArgumentException("机场代码已经存在");
		}
		EUserDetails userDetails = SecurityUtils.getUser();
		airport.setEditorId(userDetails.getId());
		airport.setEditorName(buildName(userDetails));
		airport.setEditTime(LocalDateTime.now());
		if("".equals(airport.getNationCodeThree())){
			airport.setNationCodeThree(null);
		}
		if("".equals(airport.getNationCodeNumber())){
			airport.setNationCodeNumber(null);
		}
		if("".equals(airport.getNationCodeCoo())){
			airport.setNationCodeCoo(null);
		}
		return baseMapper.updateById(airport);
	}

	@Override
	public List<Airport> listCity() {
		LambdaQueryWrapper<Airport> wrapper = Wrappers.<Airport>lambdaQuery();
		wrapper.select(Airport::getCityCode,Airport::getCityNameCn).eq(Airport::getNationCode,"CN").groupBy(Airport::getCityCode,Airport::getCityNameCn);
		return list(wrapper);
	}

    @Override
    public Airport viewCity(String cityCode) {
        LambdaQueryWrapper<Airport> wrapper = Wrappers.<Airport>lambdaQuery();
        wrapper.select(Airport::getCityCode, Airport::getCityNameCn).eq(Airport::getNationCode, "CN").eq(Airport::getCityCode, cityCode);
        List<Airport> list = list(wrapper);
        if (list.size() == 0) {
            return null;
        } else {
            return list.get(0);
        }
    }

	@Override
	public List<AirportSearch> search(String key) {
		if(StringUtils.isBlank(key)){
			return Collections.emptyList();
		}
		return this.airportMapper.search(key);
	}

	@Override
	public List<AirportCountrySearch> searchCountry(String searchKey) {
		if(StringUtils.isBlank(searchKey)){
			return Collections.emptyList();
		}
		return this.airportMapper.searchCountry(searchKey);
	}

	@Override
	public List<AirportCitySearch> searchCity(String searchKey) {
		if(StringUtils.isBlank(searchKey)){
			return Collections.emptyList();
		}
		return this.airportMapper.searchCities(searchKey);
	}

	@Override
	public List<Airport> queryNationWithNationCodeThreeIsNotNull() {
		return airportMapper.queryNationWithNationCodeThreeIsNotNull();
	}

	//计算时间差，以小时为单位。如：2018-08-08 和 2018-08-07 相差24h
    public double getDistanceTime2(Date startTime, Date endTime) {
        double hour = 0;
        long time1 = startTime.getTime();
        long time2 = endTime.getTime();

        long diff;
        if (time1 < time2) {
            diff = time2 - time1;
        } else {
            diff = time1 - time2;
        }
        hour = (diff / (  1000));
        return hour;
    }

    private String buildName(EUserDetails userDetails){
		StringBuilder builder = new StringBuilder();
		builder.append(userDetails.getUserCname());
		builder.append(" ");
		builder.append(userDetails.getUserEmail());
		return builder.toString();
	}
	
}
