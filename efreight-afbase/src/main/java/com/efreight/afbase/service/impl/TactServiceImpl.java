package com.efreight.afbase.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.afbase.dao.CarrierMapper;
import com.efreight.afbase.entity.Carrier;
import com.efreight.afbase.entity.OperationPlan;
import com.efreight.afbase.entity.exportExcel.TactExcel;
import com.efreight.afbase.service.AirportService;
import com.efreight.afbase.service.CarrierService;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;

import com.efreight.afbase.entity.Airport;
import com.efreight.afbase.entity.Tact;
import com.efreight.afbase.dao.AirportMapper;
import com.efreight.afbase.dao.TactMapper;
import com.efreight.afbase.service.TactService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
@Service
@AllArgsConstructor
public class TactServiceImpl extends ServiceImpl<TactMapper, Tact> implements TactService {

    private final TactMapper tactMapper;
    private final CarrierService carrierService;
    private final AirportService airportService;

    @Override
    public IPage<Tact> getListPage(Page page, Tact bean) {

        if(StrUtil.isBlank(bean.getCreateTimeBegin())){
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00"));
            bean.setCreateTimeBegin(now);
        }
//        if(bean.getIsTrue() == true){
//            if(!"".equals(bean.getArrivalStation())){
//                //查询目的港同城的港口
//                List<String> list = baseMapper.getCity(bean.getArrivalStation());
//                String citys="'"+bean.getArrivalStation()+"'";
//                if(list!=null && list.size()>0){
//                    for (int i = 0; i < list.size(); i++) {
//                        if (citys.length()==0) {
//                            citys="'"+list.get(i)+"'";
//                        } else {
//                            citys=citys+",'"+list.get(i)+"'";
//                        }
//                    }
//                }
//                bean.setArrivalStation(citys);
//            }
//        }else if(!"".equals(bean.getArrivalStation())){
//            String citys="'"+bean.getArrivalStation()+"'";
//            bean.setArrivalStation(citys);
//        }
        
        //获取机场代码以及城市代码
        if(bean.getDepartureStation()!=null&&!"".equals(bean.getDepartureStation())) {
        	List<Map<String,String>> depMap = baseMapper.getAirportOrCity(bean.getDepartureStation());
        	if(depMap!=null&&depMap.size()>0) {
        		StringBuffer sb = new StringBuffer();
        		for(Map<String,String> map :depMap) {
        			if("".equals(sb.toString())) {
        				sb.append("'").append(map.get("city_code")).append("'");
        				sb.append(",'").append(map.get("ap_code")).append("'");
        			}else {
        				sb.append(",'").append(map.get("ap_code")).append("'");
        			}
        		}
        		bean.setDepartureStation(sb.toString());
        	}
        }
        
        if(bean.getArrivalStation()!=null&&!"".equals(bean.getArrivalStation())) {
        	List<Map<String,String>> desMap = baseMapper.getAirportOrCity(bean.getArrivalStation());
        	if(desMap!=null&&desMap.size()>0) {
        		StringBuffer sb = new StringBuffer();
        		for(Map<String,String> map :desMap) {
        			if("".equals(sb.toString())) {
        				sb.append("'").append(map.get("city_code")).append("'");
        				sb.append(",'").append(map.get("ap_code")).append("'");
        			}else {
        				sb.append(",'").append(map.get("ap_code")).append("'");
        			}
        		}
        		bean.setArrivalStation(sb.toString());
        	}
        }
        
        IPage<Tact> pa = baseMapper.getList(page, bean);
        /*pa.setTotal(1);
        pa.setPages(1);
        pa.setCurrent(1);
        pa.setSize(1);
        List<Tact> listT = new ArrayList<Tact>();
        if(pa.getRecords()!=null&&pa.getRecords().size()>0) {
        	listT.add(pa.getRecords().get(0));
        }
        pa.setRecords(listT);*/
        return pa;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTactById(Integer tactId) {
        if (null == tactId) {
            return 0;
        }
        return tactMapper.deleteById(tactId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveTack(Tact tact) {
        //校验参数
        checkTack(tact);
        tact.setEndDate(null);
        //校验生效时间是否满足要求
        LambdaQueryWrapper<Tact> wrapper = Wrappers.<Tact>lambdaQuery();
        LocalDateTime now = LocalDateTime.now();
        wrapper.eq(Tact::getOrgId, SecurityUtils.getUser().getOrgId()).eq(Tact::getCarrierCode, tact.getCarrierCode()).eq(Tact::getDepartureStation, tact.getDepartureStation()).eq(Tact::getArrivalStation, tact.getArrivalStation()).orderByDesc(Tact::getCreateTime).last("limit 1");
        Tact one = baseMapper.selectOne(wrapper);
        if (one != null) {
            if (one.getBeginDate().isEqual(tact.getBeginDate()) || one.getBeginDate().isAfter(tact.getBeginDate())) {
                throw new RuntimeException("生效时间与已有数据有重叠,建议大于" + one.getBeginDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
            one.setEndDate(tact.getBeginDate().minusSeconds(1));
            baseMapper.updateById(one);
        }

        EUserDetails userDetail = SecurityUtils.getUser();
        tact.setCreatorId(userDetail.getId());
        tact.setCreatorName(buildName(userDetail));
        tact.setCreateTime(LocalDateTime.now());
        tact.setOrgId(userDetail.getOrgId());
        return tactMapper.insert(tact);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTack(Tact tact) {
        if (null == tact.getTactId()) {
            throw new IllegalArgumentException("数据参数异常!");
        }
        checkTack(tact);
        EUserDetails userDetail = SecurityUtils.getUser();
        tact.setEditorId(userDetail.getId());
        tact.setEditorName(buildName(userDetail));
        tact.setEditTime(LocalDateTime.now());

        return tactMapper.updateById(tact);
    }

    private void checkTack(Tact tact) {
        //数据验证
        if (StringUtils.isBlank(tact.getDepartureStation())) {
            throw new IllegalArgumentException("请输入始发港");
        }
        if (tact.getBeginDate() == null) {
            throw new IllegalArgumentException("请输入生效日期");
        }
        //检查航司
        String carrierCode = tact.getCarrierCode();
        if (StringUtils.isNotBlank(carrierCode)) {
            Carrier carrier = carrierService.queryOne(carrierCode);
            if (carrier == null) {
                throw new IllegalArgumentException("未查询到相关航司信息");
            }
        }
        //检查始发港
        //2020-04-21 保存 校验 始发港、 目的港 代码 改为 校验 港口 表的 城市代码；
//        Airport airport = airportService.getAirportCityNameENByApCode(tact.getDepartureStation());
        Airport airport = airportService.getAirport(tact.getDepartureStation());
        if (null == airport) {
            throw new IllegalArgumentException("未查询到相关始发港信息");
        }
        //检查目的港信息
        String arrivalStation = tact.getArrivalStation();
        if (StringUtils.isNotBlank(arrivalStation)) {
//            airport = airportService.getAirportCityNameENByApCode(arrivalStation);
        	airport = airportService.getAirport(tact.getDepartureStation());
            if (null == airport) {
                throw new IllegalArgumentException("未查询到相关目的港信息");
            }
        }


    }

    private String buildName(EUserDetails userDetail) {
        StringBuilder builder = new StringBuilder();
        builder.append(userDetail.getUserCname());
        builder.append(" ");
        builder.append(userDetail.getUserEmail());
        return builder.toString();
    }

	@Override
	public boolean checkAppid(String appid) {
		boolean flag = false;
		List<Map<String,String>> checkMap = tactMapper.getAppid(appid);
		if(checkMap!=null&&checkMap.size()>0) {
			flag = true;
		}
		return flag;
	}

    @Override
    public Tact getTactForBillMake(Tact bean) {

        //获取机场代码以及城市代码
        if(bean.getDepartureStation()!=null&&!"".equals(bean.getDepartureStation())) {
            List<Map<String,String>> depMap = baseMapper.getAirportOrCity(bean.getDepartureStation());
            if(depMap!=null&&depMap.size()>0) {
                StringBuffer sb = new StringBuffer();
                for(Map<String,String> map :depMap) {
                    if("".equals(sb.toString())) {
                        sb.append("'").append(map.get("city_code")).append("'");
                        sb.append(",'").append(map.get("ap_code")).append("'");
                    }else {
                        sb.append(",'").append(map.get("ap_code")).append("'");
                    }
                }
                bean.setDepartureStation(sb.toString());
            }
        }

        if(bean.getArrivalStation()!=null&&!"".equals(bean.getArrivalStation())) {
            List<Map<String,String>> desMap = baseMapper.getAirportOrCity(bean.getArrivalStation());
            if(desMap!=null&&desMap.size()>0) {
                StringBuffer sb = new StringBuffer();
                for(Map<String,String> map :desMap) {
                    if("".equals(sb.toString())) {
                        sb.append("'").append(map.get("city_code")).append("'");
                        sb.append(",'").append(map.get("ap_code")).append("'");
                    }else {
                        sb.append(",'").append(map.get("ap_code")).append("'");
                    }
                }
                bean.setArrivalStation(sb.toString());
            }
        }

        Tact pa = baseMapper.getTactForBillMake(bean);
        return pa;
    }

    @Override
    public List<TactExcel> queryListForExcel(Tact bean) {

        if(StrUtil.isBlank(bean.getCreateTimeBegin())){
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00"));
            bean.setCreateTimeBegin(now);
        }

        //获取机场代码以及城市代码
        if(bean.getDepartureStation()!=null&&!"".equals(bean.getDepartureStation())) {
            List<Map<String,String>> depMap = baseMapper.getAirportOrCity(bean.getDepartureStation());
            if(depMap!=null&&depMap.size()>0) {
                StringBuffer sb = new StringBuffer();
                for(Map<String,String> map :depMap) {
                    if("".equals(sb.toString())) {
                        sb.append("'").append(map.get("city_code")).append("'");
                        sb.append(",'").append(map.get("ap_code")).append("'");
                    }else {
                        sb.append(",'").append(map.get("ap_code")).append("'");
                    }
                }
                bean.setDepartureStation(sb.toString());
            }
        }

        if(bean.getArrivalStation()!=null&&!"".equals(bean.getArrivalStation())) {
            List<Map<String,String>> desMap = baseMapper.getAirportOrCity(bean.getArrivalStation());
            if(desMap!=null&&desMap.size()>0) {
                StringBuffer sb = new StringBuffer();
                for(Map<String,String> map :desMap) {
                    if("".equals(sb.toString())) {
                        sb.append("'").append(map.get("city_code")).append("'");
                        sb.append(",'").append(map.get("ap_code")).append("'");
                    }else {
                        sb.append(",'").append(map.get("ap_code")).append("'");
                    }
                }
                bean.setArrivalStation(sb.toString());
            }
        }

        List<TactExcel> pa = baseMapper.queryListForExcel(bean);
        return pa;
    }

}
