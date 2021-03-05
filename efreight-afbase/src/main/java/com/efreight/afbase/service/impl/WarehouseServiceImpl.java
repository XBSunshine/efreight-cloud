package com.efreight.afbase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.WarehouseMapper;
import com.efreight.afbase.entity.Warehouse;
import com.efreight.afbase.entity.WarehouseLetter;
import com.efreight.afbase.service.WarehouseLetterService;
import com.efreight.afbase.service.WarehouseService;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Service
@AllArgsConstructor
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {

    private final WarehouseLetterService warehouseLetterService;
    @Override
    public IPage<Warehouse> getListPage(Page page, Warehouse bean) {
        QueryWrapper<Warehouse> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("business_scope", bean.getBusinessScope());
        if (bean.getApCode() != null && !"".equals(bean.getApCode())) {
            queryWrapper.eq("ap_code", bean.getApCode());
        }
//        if (bean.getWarehouseCode() != null && !"".equals(bean.getWarehouseCode())) {
//            queryWrapper.like("warehouse_code", bean.getWarehouseCode());
//        }
//        if (bean.getWarehouseNameCn() != null && !"".equals(bean.getWarehouseNameCn())) {
//            String keys = bean.getWarehouseNameCn();
//            queryWrapper.and(wrapper -> wrapper.like("warehouse_name_cn", keys).or().like("warehouse_name_en", keys));
//        }
        if (bean.getWarehouseNameCn() != null && !"".equals(bean.getWarehouseNameCn())) {
        	String keys = bean.getWarehouseNameCn();
        	queryWrapper.and(wrapper -> wrapper.like("warehouse_name_cn", keys).or().like("warehouse_name_en", keys)
        			.or().like("warehouse_code", keys).or().like("customs_code", keys));
        }

        queryWrapper.eq("org_id", SecurityUtils.getUser().getOrgId());
        queryWrapper.orderByAsc("business_scope", "ap_code", "warehouse_code");
        queryWrapper.eq("warehouse_status", 1);
        IPage<Warehouse> list = baseMapper.selectPage(page, queryWrapper);
        if(list != null && list.getRecords().size()>0){
            for(int i=0;i<list.getRecords().size();i++){
                if(!"".equals(list.getRecords().get(i).getShipperTemplate())){
                    list.getRecords().get(i).setShipperTemplateName(baseMapper.getTemplateNameById(list.getRecords().get(i).getShipperTemplate()));
                }
            }
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doSave(Warehouse bean) {
        bean.setCreateTime(new Date());
        bean.setCreatorId(SecurityUtils.getUser().getId());
        bean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        if (bean.getCustomsCode()!=null && !"".equals(bean.getCustomsCode())) {
        	List<Warehouse> list=baseMapper.getWarehouseList(bean);
        	if (list.size()>0) {
        		throw new RuntimeException("IDU_customs_code");
			}
		}
        //签约公司+业务范畴+货站代码 不能重复
        List<Warehouse> list=baseMapper.ifExistWarehouseCode1(bean);
        if (list.size()>0) {
            throw new RuntimeException("IDU_warehouse_code");
        }
        //签约公司+业务范畴+货站中文名称 不能重复
        List<Warehouse> list1=baseMapper.ifExistWarehouseNameCn1(bean);
        if (list1.size()>0) {
            throw new RuntimeException("IDU_warehouse_name_cn");
        }
        baseMapper.insert(bean);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doUpdate(Warehouse bean) {
        bean.setEditTime(new Date());
        bean.setEditorId(SecurityUtils.getUser().getId());
        bean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        if (bean.getCustomsCode()!=null && !"".equals(bean.getCustomsCode())) {
	        if ("1".equals(bean.getIsChange())) {
	        	List<Warehouse> list=baseMapper.getWarehouseList(bean);
	        	if (list.size()>0) {
	        		throw new RuntimeException("IDU_customs_code");
				}
			}
        }
        //签约公司+业务范畴+货站代码 不能重复
        List<Warehouse> list=baseMapper.ifExistWarehouseCode(bean);
        if (list.size()>0) {
            throw new RuntimeException("IDU_warehouse_code");
        }
        //签约公司+业务范畴+货站中文名称 不能重复
        List<Warehouse> list1=baseMapper.ifExistWarehouseNameCn(bean);
        if (list1.size()>0) {
            throw new RuntimeException("IDU_warehouse_name_cn");
        }
        UpdateWrapper<Warehouse> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("warehouse_id", bean.getWarehouseId());
        baseMapper.update(bean, updateWrapper);
        return true;
    }

    @Override
    public List<Warehouse> getListByDeparture(String departureStation, String type) {
        LambdaQueryWrapper<Warehouse> wrapper = Wrappers.<Warehouse>lambdaQuery();
        wrapper.eq(Warehouse::getOrgId, SecurityUtils.getUser().getOrgId()).eq(Warehouse::getApCode, departureStation).eq(Warehouse::getBusinessScope, "AE").eq(Warehouse::getWarehouseStatus,1);
        if ("库房".equals(type)) {
            wrapper.eq(Warehouse::getCustomsSupervision, "普货库");
        } else if ("货站".equals(type)) {
            wrapper.like(Warehouse::getCustomsSupervision, "%监管");
        }
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<WarehouseLetter> findshipperTemplates(String apCode) {
        LambdaQueryWrapper<WarehouseLetter> wrapper = Wrappers.<WarehouseLetter>lambdaQuery();
        wrapper.eq(WarehouseLetter::getApCode,apCode).eq(WarehouseLetter::getIsValid,1);
        return warehouseLetterService.list(wrapper);
    }

    @Override
    public int deleteById(Integer warehouseId) {
        int result = 0;
        if(null == warehouseId){
            return result;
        }
        Warehouse dbWarehouse = this.baseMapper.selectById(warehouseId);
        if(null != dbWarehouse){
            dbWarehouse.setWarehouseStatus(-1);
            result = baseMapper.updateById(dbWarehouse);
        }
        return result;
    }

    @Override
    public List<Warehouse> getList(String type) {
        LambdaQueryWrapper<Warehouse> wrapper = Wrappers.<Warehouse>lambdaQuery();
        wrapper.eq(Warehouse::getOrgId, SecurityUtils.getUser().getOrgId()).eq(Warehouse::getBusinessScope, "AE").eq(Warehouse::getWarehouseStatus,1);
        if ("库房".equals(type)) {
            wrapper.eq(Warehouse::getCustomsSupervision, "普货库");
        } else if ("货站".equals(type)) {
            wrapper.like(Warehouse::getCustomsSupervision, "%监管");
        }
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<Warehouse> getWarehouseListByQuery(String businessScope) {
        LambdaQueryWrapper<Warehouse> wrapper = Wrappers.<Warehouse>lambdaQuery();
        wrapper.eq(Warehouse::getOrgId, SecurityUtils.getUser().getOrgId()).eq(Warehouse::getBusinessScope, businessScope).eq(Warehouse::getWarehouseStatus,1);

        return baseMapper.selectList(wrapper);
    }
}
