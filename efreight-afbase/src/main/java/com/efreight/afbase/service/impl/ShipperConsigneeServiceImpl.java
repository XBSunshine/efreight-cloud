package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.Airport;
import com.efreight.afbase.entity.ShipperConsignee;
import com.efreight.afbase.dao.ShipperConsigneeMapper;
import com.efreight.afbase.entity.exportExcel.ShipperConsigneeExcel;
import com.efreight.afbase.service.AirportService;
import com.efreight.afbase.service.ShipperConsigneeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.util.SecurityUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * AF 基础信息 收发货人 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2019-10-09
 */
@Service
@AllArgsConstructor
public class ShipperConsigneeServiceImpl extends ServiceImpl<ShipperConsigneeMapper, ShipperConsignee> implements ShipperConsigneeService {

    private final AirportService airportService;

    private final ShipperConsigneeMapper shipperConsigneeMapper;

    @Override
    public IPage<ShipperConsignee> getPage(Page page, ShipperConsignee shipperConsignee) {
        LambdaQueryWrapper<ShipperConsignee> wrapper = Wrappers.<ShipperConsignee>lambdaQuery();
        if (shipperConsignee.getScType() != null) {
            wrapper.eq(ShipperConsignee::getScType, shipperConsignee.getScType());
        }

        if (StrUtil.isNotBlank(shipperConsignee.getScName())) {
            wrapper.like(ShipperConsignee::getScName, "%" + shipperConsignee.getScName() + "%");
        }

        if (StrUtil.isNotBlank(shipperConsignee.getScMnemonic())) {
            wrapper.eq(ShipperConsignee::getScMnemonic, shipperConsignee.getScMnemonic());
        }

        if (StrUtil.isNotBlank(shipperConsignee.getScCode())) {
            wrapper.like(ShipperConsignee::getScCode, "%" + shipperConsignee.getScCode() + "%");
        }

        if (StrUtil.isNotBlank(shipperConsignee.getNationCode())) {
            wrapper.eq(ShipperConsignee::getNationCode, shipperConsignee.getNationCode());
        }

        if (StrUtil.isNotBlank(shipperConsignee.getCityName())) {
            wrapper.and(i -> i.like(ShipperConsignee::getCityName, "%" + shipperConsignee.getCityName() + "%").or(j -> j.like(ShipperConsignee::getCityCode, "%" + shipperConsignee.getCityName() + "%")));
        }
        if (shipperConsignee.getIsValid() != null) {
            wrapper.eq(ShipperConsignee::getIsValid, shipperConsignee.getIsValid());
        }
        wrapper.eq(ShipperConsignee::getOrgId, SecurityUtils.getUser().getOrgId()).orderByAsc(ShipperConsignee::getScMnemonic, ShipperConsignee::getScName);
        return baseMapper.selectPage(page, wrapper);
    }
    @Override
    public IPage<ShipperConsignee> getPage2(Page page, ShipperConsignee shipperConsignee) {
    	LambdaQueryWrapper<ShipperConsignee> wrapper = Wrappers.<ShipperConsignee>lambdaQuery();
    	if (shipperConsignee.getScType() != null) {
    		wrapper.eq(ShipperConsignee::getScType, shipperConsignee.getScType());
    	}
    	
    	if (StrUtil.isNotBlank(shipperConsignee.getScName())) {
//    		wrapper.like(ShipperConsignee::getScName, "%" + shipperConsignee.getScName() + "%");
//    		wrapper.and(i -> i.like(ShipperConsignee::getScName, "%" + shipperConsignee.getCityName() + "%").or(j -> j.like(ShipperConsignee::getScCode, "%" + shipperConsignee.getCityName() + "%")));
    		wrapper.and(i -> i.like(ShipperConsignee::getScName, shipperConsignee.getScName()).or().like(ShipperConsignee::getScCode, shipperConsignee.getScName()));
    	}
    	
    	if (StrUtil.isNotBlank(shipperConsignee.getScMnemonic())) {
    		wrapper.eq(ShipperConsignee::getScMnemonic, shipperConsignee.getScMnemonic());
    	}
    	
    	if (StrUtil.isNotBlank(shipperConsignee.getScCode())) {
    		wrapper.like(ShipperConsignee::getScCode, "%" + shipperConsignee.getScCode() + "%");
    	}
    	
    	if (StrUtil.isNotBlank(shipperConsignee.getNationCode())) {
    		wrapper.eq(ShipperConsignee::getNationCode, shipperConsignee.getNationCode());
    	}
    	
    	if (StrUtil.isNotBlank(shipperConsignee.getCityName())) {
    		wrapper.and(i -> i.like(ShipperConsignee::getCityName, "%" + shipperConsignee.getCityName() + "%").or(j -> j.like(ShipperConsignee::getCityCode, "%" + shipperConsignee.getCityName() + "%")));
    	}
    	if (shipperConsignee.getIsValid() != null) {
    		wrapper.eq(ShipperConsignee::getIsValid, shipperConsignee.getIsValid());
    	}
    	wrapper.eq(ShipperConsignee::getOrgId, SecurityUtils.getUser().getOrgId()).orderByDesc(ShipperConsignee::getCreateTime);
    	return baseMapper.selectPage(page, wrapper);
    }
    @Override
    public List<ShipperConsignee> queryList(ShipperConsignee shipperConsignee) {
    	LambdaQueryWrapper<ShipperConsignee> wrapper = Wrappers.<ShipperConsignee>lambdaQuery();
    	if (shipperConsignee.getScType() != null) {
    		wrapper.eq(ShipperConsignee::getScType, shipperConsignee.getScType());
    	}
    	
    	if (StrUtil.isNotBlank(shipperConsignee.getScName())) {
    		wrapper.like(ShipperConsignee::getScName, "%" + shipperConsignee.getScName() + "%");
    	}
    	
    	if (StrUtil.isNotBlank(shipperConsignee.getScMnemonic())) {
    		wrapper.eq(ShipperConsignee::getScMnemonic, shipperConsignee.getScMnemonic());
    	}
    	
    	if (StrUtil.isNotBlank(shipperConsignee.getScCode())) {
    		wrapper.like(ShipperConsignee::getScCode, "%" + shipperConsignee.getScCode() + "%");
    	}
    	
    	if (StrUtil.isNotBlank(shipperConsignee.getNationCode())) {
    		wrapper.eq(ShipperConsignee::getNationCode, shipperConsignee.getNationCode());
    	}
    	
    	if (StrUtil.isNotBlank(shipperConsignee.getCityName())) {
    		wrapper.and(i -> i.like(ShipperConsignee::getCityName, "%" + shipperConsignee.getCityName() + "%").or(j -> j.like(ShipperConsignee::getCityCode, "%" + shipperConsignee.getCityName() + "%")));
    	}
    	if (shipperConsignee.getIsValid() != null) {
    		wrapper.eq(ShipperConsignee::getIsValid, shipperConsignee.getIsValid());
    	}
    	wrapper.eq(ShipperConsignee::getOrgId, SecurityUtils.getUser().getOrgId()).orderByDesc(ShipperConsignee::getCreateTime);
//    	return baseMapper.selectPage(page, wrapper);
    	return baseMapper.selectList( wrapper);
    }

    @Override
    public ShipperConsignee view(Integer scId) {

        return baseMapper.selectById(scId);
    }

    @Override
    public void modify(ShipperConsignee shipperConsignee) {
        //校验国家代码是否存在
    	checkNationCode(shipperConsignee);

        checkCityCode(shipperConsignee);
        if(shipperConsignee.getCityCode()!=null && !"".equals(shipperConsignee.getCityCode()) && shipperConsignee.getNationCode()!=null && !"".equals(shipperConsignee.getNationCode())){
            Integer count = shipperConsigneeMapper.getCountByCityAndNation(shipperConsignee.getCityCode(),shipperConsignee.getNationCode());
            if(count == 0){//国家代码和城市代码不一致
                throw new RuntimeException("国家和城市不一致，保存失败！");
            }
        }

        shipperConsignee.setEditorId(SecurityUtils.getUser().getId());
        shipperConsignee.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        shipperConsignee.setEditTime(LocalDateTime.now());
        baseMapper.updateById(shipperConsignee);
    }

    @Override
    public void cancel(Integer scId) {
        ShipperConsignee shipperConsignee = new ShipperConsignee();
        shipperConsignee.setScId(scId);
        shipperConsignee.setIsValid(false);
        shipperConsignee.setEditorId(SecurityUtils.getUser().getId());
        shipperConsignee.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        shipperConsignee.setEditTime(LocalDateTime.now());
        baseMapper.updateById(shipperConsignee);
    }

    @Override
    public IPage<ShipperConsignee> selectToPage(Page page, ShipperConsignee shipperConsignee) {
        shipperConsignee.setOrgId(SecurityUtils.getUser().getOrgId());
        return shipperConsigneeMapper.selectToPage(page, shipperConsignee);
    }

    @Override
    public boolean save(ShipperConsignee shipperConsignee) {
        //校验国家代码是否存在
    	checkNationCode(shipperConsignee);
        checkCityCode(shipperConsignee);
        ////如果同时输入了国家代码和城市代码则验证是否一致
        if(shipperConsignee.getCityCode()!=null && !"".equals(shipperConsignee.getCityCode()) && shipperConsignee.getNationCode()!=null && !"".equals(shipperConsignee.getNationCode())){
            Integer count = shipperConsigneeMapper.getCountByCityAndNation(shipperConsignee.getCityCode(),shipperConsignee.getNationCode());
            if(count == 0){//国家代码和城市代码不一致
                throw new RuntimeException("国家和城市不一致，保存失败！");
            }
        }

        shipperConsignee.setCreatorId(SecurityUtils.getUser().getId());
        shipperConsignee.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        shipperConsignee.setCreateTime(LocalDateTime.now());

        shipperConsignee.setEditorId(SecurityUtils.getUser().getId());
        shipperConsignee.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        shipperConsignee.setEditTime(LocalDateTime.now());

        shipperConsignee.setOrgId(SecurityUtils.getUser().getOrgId());
        return super.save(shipperConsignee);
    }
  //校验国家代码是否存在
    private void checkNationCode(ShipperConsignee shipperConsignee){
        if (StrUtil.isNotBlank(shipperConsignee.getNationCode())) {
        	LambdaQueryWrapper<Airport> wrapper = Wrappers.<Airport>lambdaQuery();
            wrapper.eq(Airport::getNationCode, shipperConsignee.getNationCode());
            List<Airport> airportList = airportService.list(wrapper);
            if (airportList == null || airportList.size() == 0) {
                throw new RuntimeException("国家代码不存在，保存失败！");
            }
        }
    }
    //校验城市代码是否存在
    private void checkCityCode(ShipperConsignee shipperConsignee){
        if (StrUtil.isNotBlank(shipperConsignee.getCityCode())) {
            LambdaQueryWrapper<Airport> wrapper = Wrappers.<Airport>lambdaQuery();
            wrapper.eq(Airport::getCityCode, shipperConsignee.getCityCode());
            List<Airport> list = airportService.list(wrapper);
            if (list == null || list.size() == 0) {
                throw new RuntimeException("城市代码[" + shipperConsignee.getCityCode() + "]不存在，保存失败！");
            }
        }
    }

    @Override
    public String searchCityName(String cityCode){
        if (StrUtil.isNotBlank(cityCode)) {
            LambdaQueryWrapper<Airport> wrapper = Wrappers.<Airport>lambdaQuery();
            wrapper.eq(Airport::getCityCode, cityCode);
            List<Airport> list = airportService.list(wrapper);
            if(list != null && list.size()>0){
                return list.get(0).getCityNameEn();
            }
        }
        return "";
    }

    @Override
    public String searchNationalName(String nationCode){
        if (StrUtil.isNotBlank(nationCode)) {
            LambdaQueryWrapper<Airport> wrapper = Wrappers.<Airport>lambdaQuery();
            wrapper.eq(Airport::getNationCode, nationCode);
            List<Airport> list = airportService.list(wrapper);
            if(list != null && list.size()>0){
                return list.get(0).getNationNameEn();
            }
        }
        return "";
    }

    @Override
    public List<ShipperConsigneeExcel> queryListForExcel(ShipperConsignee shipperConsignee) {
        shipperConsignee.setOrgId(SecurityUtils.getUser().getOrgId());
        return shipperConsigneeMapper.queryListForExcel(shipperConsignee);
    }
}
