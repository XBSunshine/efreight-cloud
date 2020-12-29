package com.efreight.afbase.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.WarehouseLetterMapper;
import com.efreight.afbase.entity.WarehouseLetter;
import com.efreight.afbase.service.WarehouseLetterAttachFileService;
import com.efreight.afbase.service.WarehouseLetterService;
import com.efreight.common.security.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class WarehouseLetterServiceImpl extends ServiceImpl<WarehouseLetterMapper, WarehouseLetter> implements WarehouseLetterService {

    @Resource
    private WarehouseLetterAttachFileService warehouseLetterAttachFileService;

	@Override
    public IPage<WarehouseLetter> getListPage(Page page, WarehouseLetter bean) {
        QueryWrapper<WarehouseLetter> queryWrapper = new QueryWrapper<>();
        if (bean.getApCode() != null && !"".equals(bean.getApCode())) {
            queryWrapper.eq("ap_code", bean.getApCode());
        }
        if (bean.getShipperTemplateName() != null && !"".equals(bean.getShipperTemplateName())) {
            queryWrapper.like("shipper_template_name", bean.getShipperTemplateName());
        }
        if (bean.getIsValid() != null && !"".equals(bean.getIsValid())) {
            queryWrapper.eq("is_valid", bean.getIsValid());
        }

        queryWrapper.orderByAsc("ap_code", "shipper_template_name");
        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doSave(WarehouseLetter bean) {
        bean.setCreateTime(new Date());
        bean.setCreatorId(SecurityUtils.getUser().getId());
        bean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        baseMapper.insert(bean);

        warehouseLetterAttachFileService.batchInsert(bean.getWarehouseLetterId(), bean.getWarehouseLetterAttachFiles());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doUpdate(WarehouseLetter bean) {
        bean.setEditTime(new Date());
        bean.setEditorId(SecurityUtils.getUser().getId());
        bean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
       
        UpdateWrapper<WarehouseLetter> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("warehouse_letter_id", bean.getWarehouseLetterId());
        baseMapper.update(bean, updateWrapper);

        warehouseLetterAttachFileService.updateAndSave(bean.getWarehouseLetterId(), bean.getWarehouseLetterAttachFiles());
        return true;
    }

    @Override
    public WarehouseLetter getWarehouseLetter(Integer warehouseLetterId) {
        Assert.notNull(warehouseLetterId, "非法参数");
	    WarehouseLetter warehouseLetter =this.getById(warehouseLetterId);
	    warehouseLetter.setWarehouseLetterAttachFiles(warehouseLetterAttachFileService.findByWarehouseLetterId(warehouseLetterId));
        return warehouseLetter;
    }
}
