package com.efreight.sc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.dao.PortMaintenanceMapper;
import com.efreight.sc.entity.PortMaintenance;
import com.efreight.sc.service.PortMaintenanceService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * CS 海运港口表 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-03
 */
@Service
public class PortMaintenanceServiceImpl extends ServiceImpl<PortMaintenanceMapper, PortMaintenance> implements PortMaintenanceService {

    @Override
    public List<PortMaintenance> getList() {
        LambdaQueryWrapper<PortMaintenance> wrapper = Wrappers.<PortMaintenance>lambdaQuery();
        wrapper.eq(PortMaintenance::getIsValid, 1);
        return list(wrapper);
    }

	@Override
	public IPage<PortMaintenance> getPage(Page page, PortMaintenance portMaintenance) {
		LambdaQueryWrapper<PortMaintenance> wrapper = Wrappers.<PortMaintenance>lambdaQuery();
        if (StrUtil.isNotBlank(portMaintenance.getPortNameCn())) {
            wrapper.and(i -> i.like(PortMaintenance::getPortNameCn, portMaintenance.getPortNameCn()).or().like(PortMaintenance::getPortNameEn, portMaintenance.getPortNameCn()));
        }
        if(StrUtil.isNotBlank(portMaintenance.getPortCode())) {
        	wrapper.and(i->i.like(PortMaintenance::getPortCode, portMaintenance.getPortCode()));
        }

        String cityCode = portMaintenance.getCityCode();
        if(StringUtils.isNotBlank(cityCode)){
            if(cityCode.length() == 3) {
                wrapper.likeLeft(PortMaintenance::getCityCode, cityCode);
            }else{
                wrapper.and(i->i.like(PortMaintenance::getCityCode, cityCode)
                        .or()
                        .like(PortMaintenance::getCityNameEn, cityCode)
                        .or()
                        .like(PortMaintenance::getCityNameCn, cityCode));
            }
        }

       String countryCode = portMaintenance.getCountryCode();
        if(StringUtils.isNotBlank(countryCode)){
            if(countryCode.length() == 2 && checkCodeRule(countryCode)){
                wrapper.eq(PortMaintenance::getCountryCode, countryCode);
            }else{
                wrapper.and(i->i.like(PortMaintenance::getCountryNameCn, countryCode)
                        .or()
                        .like(PortMaintenance::getCountryNameEn, countryCode));
            }
        }

        wrapper.eq(PortMaintenance::getIsValid, true).orderByAsc(PortMaintenance::getPortCode);
        return page(page, wrapper);
	}

    @Override
    public PortMaintenance getByCode(String code) {
        if(StringUtils.isBlank(code)){
            return null;
        }
        LambdaQueryWrapper<PortMaintenance> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(PortMaintenance::getPortCode, code);
        queryWrapper.eq(PortMaintenance::getIsValid, 1);
        return this.baseMapper.selectOne(queryWrapper);
    }

    @Override
    public int savePortMaintenance(PortMaintenance portMaintenance) {
        Objects.requireNonNull(portMaintenance, "非法参数");
        Assert.hasLength(portMaintenance.getPortCode(), "必填字段");

        PortMaintenance dbPortMaintenance = this.getByCode(portMaintenance.getPortCode());
        Assert.isNull(dbPortMaintenance, "数据已经存在");

        EUserDetails loginUser = SecurityUtils.getUser();
        portMaintenance.setCreatorId(loginUser.getId());
        portMaintenance.setCreatorName(loginUser.buildOptName());
        portMaintenance.setCreateTime(LocalDateTime.now());
        return this.baseMapper.insert(portMaintenance);
    }

    @Override
    public int editById(PortMaintenance portMaintenance) {
        Objects.requireNonNull(portMaintenance, "非法参数");
        Assert.hasLength(portMaintenance.getPortCode(), "必填字段");
        Assert.notNull(portMaintenance.getPortId(), "必填字段");

        PortMaintenance dbPortMaintenance = this.getByCode(portMaintenance.getPortCode());
        if(null != dbPortMaintenance && !dbPortMaintenance.getPortId().equals(portMaintenance.getPortId())){
            throw new IllegalArgumentException("数据已经存在");
        }

        EUserDetails loginUser = SecurityUtils.getUser();
        portMaintenance.setEditorId(loginUser.getId());
        portMaintenance.setEditorName(loginUser.buildOptName());
        portMaintenance.setEditTime(LocalDateTime.now());
        return this.baseMapper.updateById(portMaintenance);
    }

    /**
     * 检查字符串是否符合规则（包含字母或数字)
     * @param str
     * @return
     */
    private boolean checkCodeRule(String str){
        if(StringUtils.isBlank(str)){
            return false;
        }
        int len = str.length();
        for (int i = 0; i < len; i++) {
            //不是数字
            if(!Character.isDigit(str.charAt(i)) && !Character.isUpperCase(str.charAt(i))){
                return false;
            }
        }
        return true;
    }

    @Override
    public List<PortMaintenance> search(String key) {
        if(StringUtils.isBlank(key)){
            return Collections.emptyList();
        }
        return this.baseMapper.search(key);
    }

}
