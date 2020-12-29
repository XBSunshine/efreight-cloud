package com.efreight.sc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.entity.OrderShipInfo;
import com.efreight.sc.dao.OrderShipInfoMapper;
import com.efreight.sc.service.OrderShipInfoService;

import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-03
 */
@Service
public class OrderShipInfoServiceImpl extends ServiceImpl<OrderShipInfoMapper, OrderShipInfo> implements OrderShipInfoService {

    @Override
    public List<OrderShipInfo> getList() {
        LambdaQueryWrapper<OrderShipInfo> wrapper = Wrappers.<OrderShipInfo>lambdaQuery();
        wrapper.eq(OrderShipInfo::getOrgId, SecurityUtils.getUser().getOrgId());
        return list(wrapper);
    }

	@Override
	public IPage getPageList(Page page, OrderShipInfo info) {
		LambdaQueryWrapper<OrderShipInfo> wrapper = Wrappers.<OrderShipInfo>lambdaQuery();
		if(StrUtil.isNotBlank(info.getShipName())) {
			wrapper.and(i -> i.like(OrderShipInfo::getShipNameCn, "%" + info.getShipName() + "%").or(j -> j.like(OrderShipInfo::getShipNameEn, "%" + info.getShipName() + "%")));
		}
		if(StrUtil.isNotBlank(info.getIsValidStr())&&info.getIsValidStr()!="") {
			if("1".equals(info.getIsValidStr())) {
				wrapper.eq(OrderShipInfo::getIsValid, true);
			}else {
				wrapper.eq(OrderShipInfo::getIsValid, false);
			}
		}
		wrapper.eq(OrderShipInfo::getOrgId,SecurityUtils.getUser().getOrgId());
		IPage result = page(page, wrapper);
		return result;
	}

	@Override
	public void deleteInfoById(Integer shipInfoId) {
		baseMapper.deleteById(shipInfoId);
	}

	@Override
	public OrderShipInfo queryOne(Integer shipInfoId) {
		
		return baseMapper.selectById(shipInfoId);
	}

	@Override
	public Boolean addInfo(OrderShipInfo info) {
		info.setOrgId(SecurityUtils.getUser().getOrgId());
		info.setCreateTime(LocalDateTime.now());
		info.setCreatorId(SecurityUtils.getUser().getId());
		info.setCreatorName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
		baseMapper.insert(info);
		return true;
	}

	@Override
	public Boolean doUpdate(OrderShipInfo info) {
		info.setEditorId(SecurityUtils.getUser().getId());
		info.setEditorName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
		info.setEditTime(LocalDateTime.now());
		UpdateWrapper<OrderShipInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("ship_info_id", info.getShipInfoId());
		baseMapper.update(info,updateWrapper);
		return true;
	}
}
