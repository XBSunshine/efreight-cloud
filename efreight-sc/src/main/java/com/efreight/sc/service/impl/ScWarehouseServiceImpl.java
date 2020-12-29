package com.efreight.sc.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.dao.ScWarehouseMapper;
import com.efreight.sc.entity.OrderDeliveryNotice;
import com.efreight.sc.entity.ScWarehouse;
import com.efreight.sc.service.ScWarehouseService;

import lombok.AllArgsConstructor;
@Service
@AllArgsConstructor
public class ScWarehouseServiceImpl extends ServiceImpl<ScWarehouseMapper, ScWarehouse> implements ScWarehouseService{
	
	@Override
	public IPage getPage(Page page, ScWarehouse bean) {
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		return baseMapper.getPage(page, bean);
	}

	@Override
	public boolean saveWarehouse(ScWarehouse bean) {
		    bean.setCreateTime(LocalDateTime.now());
	        bean.setCreatorId(SecurityUtils.getUser().getId());
	        bean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
	        bean.setOrgId(SecurityUtils.getUser().getOrgId());
	        if (bean.getWarehouseCode()!=null && !"".equals(bean.getWarehouseCode())) {
	        	ScWarehouse checkParam = new ScWarehouse();
	        	checkParam.setOrgId(SecurityUtils.getUser().getOrgId());
	        	checkParam.setBusinessScope(bean.getBusinessScope());
	        	checkParam.setWarehouseCodeCheck(bean.getWarehouseCode());
	        	List<ScWarehouse> pageList=baseMapper.getList(checkParam);
	        	if (pageList!=null&&pageList.size()>0) {
	        		throw new RuntimeException("IDU_warehouse_code");
				}
			}
	        if (bean.getWarehouseNameCn()!=null && !"".equals(bean.getWarehouseNameCn())) {
	        	ScWarehouse checkParam = new ScWarehouse();
	        	checkParam.setOrgId(SecurityUtils.getUser().getOrgId());
	        	checkParam.setBusinessScope(bean.getBusinessScope());
	        	checkParam.setWarehouseNameCnCheck(bean.getWarehouseNameCn());
	        	List<ScWarehouse> pageList=baseMapper.getList(checkParam);
	        	if (pageList!=null&&pageList.size()>0) {
	        		throw new RuntimeException("IDU_warehouse_name");
				}
			}
	        baseMapper.insert(bean);
	        return true;
	}

	@Override
	public boolean modifyWarehouse(ScWarehouse bean) {
		    bean.setEditTime(LocalDateTime.now());
	        bean.setEditorId(SecurityUtils.getUser().getId());
	        bean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
	        bean.setOrgId(SecurityUtils.getUser().getOrgId());
	        if (bean.getWarehouseCode()!=null && !"".equals(bean.getWarehouseCode())) {
	        	ScWarehouse checkParam = new ScWarehouse();
	        	checkParam.setOrgId(SecurityUtils.getUser().getOrgId());
	        	checkParam.setBusinessScope(bean.getBusinessScope());
	        	checkParam.setWarehouseCodeCheck(bean.getWarehouseCode());
	        	List<ScWarehouse> pageList=baseMapper.getList(checkParam);
	        	if (pageList!=null&&pageList.size()>0&&bean.getWarehouseId().intValue()!=pageList.get(0).getWarehouseId().intValue()) {
	        		throw new RuntimeException("IDU_warehouse_code");
				}
			}
	        if (bean.getWarehouseNameCn()!=null && !"".equals(bean.getWarehouseNameCn())) {
	        	ScWarehouse checkParam = new ScWarehouse();
	        	checkParam.setOrgId(SecurityUtils.getUser().getOrgId());
	        	checkParam.setBusinessScope(bean.getBusinessScope());
	        	checkParam.setWarehouseNameCnCheck(bean.getWarehouseNameCn());
	        	List<ScWarehouse> pageList=baseMapper.getList(checkParam);
	        	if (pageList!=null&&pageList.size()>0&&bean.getWarehouseId().intValue()!=pageList.get(0).getWarehouseId().intValue()) {
	        		throw new RuntimeException("IDU_warehouse_name");
				}
			}
	        baseMapper.updateById(bean);
	        return true;
	}

	@Override
	public List<ScWarehouse> getList(ScWarehouse bean) {
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		return baseMapper.getList(bean);
	}

	@Override
	public OrderDeliveryNotice getOrderDeliveryNotice(String uuid) {
		return baseMapper.getOrderDeliveryNotice(uuid);
	}

}
