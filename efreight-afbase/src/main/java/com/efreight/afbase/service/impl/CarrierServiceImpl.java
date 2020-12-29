package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.CarrierMapper;
import com.efreight.afbase.entity.Carrier;
import com.efreight.afbase.entity.view.CarrierSearch;
import com.efreight.afbase.service.CarrierService;
import com.efreight.common.security.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
@Service
public class CarrierServiceImpl extends ServiceImpl<CarrierMapper, Carrier> implements CarrierService {

    @Override
    public IPage<Carrier> queryPage(Page page, Carrier carrier) {
        QueryWrapper<Carrier> queryWrapper = Wrappers.query();
        if (StrUtil.isNotBlank(carrier.getCarrierNameCn())) {
        	queryWrapper.and(wrapper -> wrapper.like("carrier_name_en", "%" + carrier.getCarrierNameCn() + "%").or().like("carrier_name_cn", "%" + carrier.getCarrierNameCn() + "%"));
        }
        if (StrUtil.isNotBlank(carrier.getCarrierCode())) {
            queryWrapper.eq("carrier_code", carrier.getCarrierCode()).or().eq("carrier_prefix", carrier.getCarrierCode());
        }
        queryWrapper.orderByAsc("carrier_code");
        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Carrier queryOne(Integer id) {
        return baseMapper.selectById(id);
    }

    @Override
    public void importData(List<Carrier> list) {
        list.stream().forEach(carrier -> {
            baseMapper.insert(carrier);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addCarrier(Carrier bean) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        bean.setCreateTime(df.parse(df.format(new Date())));
        bean.setCreatorId(SecurityUtils.getUser().getId());
        bean.setCreatorName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
        baseMapper.insert(bean);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doUpdate(Carrier bean) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        bean.setEditTime(df.parse(df.format(new Date())));
        bean.setEditorId(SecurityUtils.getUser().getId());
        bean.setEditorName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
        UpdateWrapper<Carrier> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("carrier_id", bean.getCarrierId());
        baseMapper.update(bean, updateWrapper);
        return true;
    }

    @Override
    public void removeCarrierById(String carrierId) {
        baseMapper.deleteById(carrierId);
    }

    @Override
    public List<Carrier> isHaved(String carrierCode) {
        QueryWrapper<Carrier> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("carrier_code", carrierCode);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<Carrier> isHaved1(String carrierPrefix) {
        QueryWrapper<Carrier> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("carrier_prefix", carrierPrefix);
        return baseMapper.selectList(queryWrapper);
    }
	
	@Override
    public Carrier queryOne(String carrierCode) {
        if(StrUtil.isBlank(carrierCode)){
            return null;
        }
        LambdaQueryWrapper<Carrier> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Carrier::getCarrierCode, carrierCode);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public List<Carrier> getCarrierList() {
        List<Carrier> list = list();
        list.stream().forEach(carrier -> {
            carrier.setCarrierName(carrier.getCarrierCode()+","+carrier.getCarrierPrefix()+","+carrier.getCarrierNameCn());
        });
        return list;
    }

    @Override
    public List<CarrierSearch> search(String searchKey) {
        if(StrUtil.isBlank(searchKey)){
            return Collections.emptyList();
        }
        return this.baseMapper.search(searchKey);
    }
}
