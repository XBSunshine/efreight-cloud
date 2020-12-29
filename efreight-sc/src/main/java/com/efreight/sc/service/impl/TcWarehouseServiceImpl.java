package com.efreight.sc.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.entity.OrderDeliveryNotice;
import com.efreight.sc.entity.TcWarehouse;
import com.efreight.sc.dao.TcWarehouseMapper;
import com.efreight.sc.service.TcWarehouseService;

import lombok.AllArgsConstructor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * <p>
 * TC 基础信息 堆场仓库 服务实现类
 * </p>
 *
 * @author caiwd
 * @since 2020-07-14
 */
@Service
@AllArgsConstructor
public class TcWarehouseServiceImpl extends ServiceImpl<TcWarehouseMapper, TcWarehouse> implements TcWarehouseService {

	@Override
	public IPage getPage(Page page, TcWarehouse bean) {
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		return baseMapper.getPage(page, bean);
	}

	@Override
	public List<TcWarehouse> getList(TcWarehouse bean) {
		LambdaQueryWrapper<TcWarehouse> wrapper = Wrappers.<TcWarehouse>lambdaQuery();
		wrapper.eq(TcWarehouse::getBusinessScope, bean.getBusinessScope());
		wrapper.eq(TcWarehouse::getOrgId, bean.getOrgId());
		wrapper.eq(TcWarehouse::getWarehouseStatus, 1);
		return baseMapper.selectList(wrapper);
	}

	@Override
	public boolean saveWarehouse(TcWarehouse bean) {
		bean.setCreateTime(LocalDateTime.now());
		bean.setCreatorId(SecurityUtils.getUser().getId());
		bean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		if (bean.getWarehouseCode()!=null && !"".equals(bean.getWarehouseCode())) {
			TcWarehouse checkParam = new TcWarehouse();
			checkParam.setOrgId(SecurityUtils.getUser().getOrgId());
			checkParam.setBusinessScope(bean.getBusinessScope());
			checkParam.setWarehouseCodeCheck(bean.getWarehouseCode());
			List<TcWarehouse> pageList=baseMapper.getList(checkParam);
			if (pageList!=null&&pageList.size()>0) {
				throw new RuntimeException("IDU_warehouse_code");
			}
		}
		if (bean.getWarehouseNameCn()!=null && !"".equals(bean.getWarehouseNameCn())) {
			TcWarehouse checkParam = new TcWarehouse();
			checkParam.setOrgId(SecurityUtils.getUser().getOrgId());
			checkParam.setBusinessScope(bean.getBusinessScope());
			checkParam.setWarehouseNameCnCheck(bean.getWarehouseNameCn());
			List<TcWarehouse> pageList=baseMapper.getList(checkParam);
			if (pageList!=null&&pageList.size()>0) {
				throw new RuntimeException("IDU_warehouse_name");
			}
		}
		baseMapper.insert(bean);
		return true;
	}

	@Override
	public boolean modifyWarehouse(TcWarehouse bean) {
		bean.setEditTime(LocalDateTime.now());
		bean.setEditorId(SecurityUtils.getUser().getId());
		bean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		if (bean.getWarehouseCode()!=null && !"".equals(bean.getWarehouseCode())) {
			TcWarehouse checkParam = new TcWarehouse();
			checkParam.setOrgId(SecurityUtils.getUser().getOrgId());
			checkParam.setBusinessScope(bean.getBusinessScope());
			checkParam.setWarehouseCodeCheck(bean.getWarehouseCode());
			List<TcWarehouse> pageList=baseMapper.getList(checkParam);
			if (pageList!=null&&pageList.size()>0&&bean.getWarehouseId().intValue()!=pageList.get(0).getWarehouseId().intValue()) {
				throw new RuntimeException("IDU_warehouse_code");
			}
		}
		if (bean.getWarehouseNameCn()!=null && !"".equals(bean.getWarehouseNameCn())) {
			TcWarehouse checkParam = new TcWarehouse();
			checkParam.setOrgId(SecurityUtils.getUser().getOrgId());
			checkParam.setBusinessScope(bean.getBusinessScope());
			checkParam.setWarehouseNameCnCheck(bean.getWarehouseNameCn());
			List<TcWarehouse> pageList=baseMapper.getList(checkParam);
			if (pageList!=null&&pageList.size()>0&&bean.getWarehouseId().intValue()!=pageList.get(0).getWarehouseId().intValue()) {
				throw new RuntimeException("IDU_warehouse_name");
			}
		}
		baseMapper.updateById(bean);
		return true;
	}

	@Override
	public OrderDeliveryNotice getOrderDeliveryNotice(String orderUUID) {
		return baseMapper.getOrderDeliveryNotice(orderUUID);
	}

}
