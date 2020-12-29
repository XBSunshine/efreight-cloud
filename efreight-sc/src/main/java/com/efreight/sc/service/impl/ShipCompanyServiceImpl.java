package com.efreight.sc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.dao.ShipCompanyMapper;
import com.efreight.sc.entity.ShipCompany;
import com.efreight.sc.service.ShipCompanyService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * <p>
 * 船司表 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-03
 */
@Service
public class ShipCompanyServiceImpl extends ServiceImpl<ShipCompanyMapper, ShipCompany> implements ShipCompanyService {

    @Override
    public IPage getPage(Page page, ShipCompany shipCompany) {
        LambdaQueryWrapper<ShipCompany> wrapper = Wrappers.<ShipCompany>lambdaQuery();
        if (StrUtil.isNotBlank(shipCompany.getShipCompanyNameCn())) {
            wrapper.and(i -> i.like(ShipCompany::getShipCompanyNameCn, "%" + shipCompany.getShipCompanyNameCn() + "%").or(j -> j.like(ShipCompany::getShipCompanyNameEn, "%" + shipCompany.getShipCompanyNameCn() + "%")));
        }
        if(StrUtil.isNotBlank(shipCompany.getShipCompanyCode())) {
        	wrapper.and(i->i.like(ShipCompany::getShipCompanyCode, "%" + shipCompany.getShipCompanyCode() + "%"));
        }
        wrapper.eq(ShipCompany::getIsValid, true).orderByAsc(ShipCompany::getShipCompanyCode);
        return page(page, wrapper);
    }

    @Override
    public int saveShipCompany(ShipCompany shipCompany) {
        Assert.notNull(shipCompany, "参数错误");
        Assert.hasLength(shipCompany.getShipCompanyCode(), "船司代码不能为空");
        ShipCompany dbShipCompany = getByCode(shipCompany.getShipCompanyCode());
        if(null != dbShipCompany){
            throw new RuntimeException("船司代码已经存在");
        }

        EUserDetails user = SecurityUtils.getUser();
        shipCompany.setCreatorId(user.getId());
        shipCompany.setCreatorName(user.getUserCname()+" "+user.getUserEmail());
        shipCompany.setCreateTime(new Date());
        return this.baseMapper.insert(shipCompany);
    }

    @Override
    public int editShipCompany(ShipCompany shipCompany) {
        Assert.notNull(shipCompany, "参数错误");
        Assert.notNull(shipCompany.getShipCompanyId(), "未查询到数据");
        Assert.hasLength(shipCompany.getShipCompanyCode(), "船司代码不能为空");
        ShipCompany dbShipCompany = getByCode(shipCompany.getShipCompanyCode());
        if(null != dbShipCompany && (!dbShipCompany.getShipCompanyId().equals(shipCompany.getShipCompanyId()))){
            throw new RuntimeException("船司代码已经存在");
        }

        EUserDetails user = SecurityUtils.getUser();
        shipCompany.setEditorId(user.getId());
        shipCompany.setEditorName(user.getUserCname()+" "+user.getUserEmail());
        shipCompany.setEditTime(new Date());
        return this.baseMapper.updateById(shipCompany);
    }

    @Override
    public ShipCompany getByCode(String code) {
        if(!StringUtils.hasLength(code)){
            return null;
        }
        LambdaQueryWrapper<ShipCompany> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(ShipCompany::getShipCompanyCode, code);
        return this.baseMapper.selectOne(lambdaQueryWrapper);
    }
}
