package com.efreight.afbase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.WarehouseLetterAttachFileMapper;
import com.efreight.afbase.entity.WarehouseLetterAttachFile;
import com.efreight.afbase.service.WarehouseLetterAttachFileService;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lc
 * @date 2020/8/26 16:55
 */
@Service
public class WarehouseLetterAttachFileServiceImpl extends ServiceImpl<WarehouseLetterAttachFileMapper, WarehouseLetterAttachFile> implements WarehouseLetterAttachFileService {

    @Resource
    private WarehouseLetterAttachFileMapper warehouseLetterAttachFileMapper;

    @Override
    public boolean batchInsert(Integer warehouseLetterId, List<WarehouseLetterAttachFile> warehouseLetterAttachFiles) {
        Assert.notNull(warehouseLetterId, "非法参数");
        EUserDetails loginUser = SecurityUtils.getUser();

        warehouseLetterAttachFiles.stream().forEach((item)->{
            item.setWarehouseLetterId(warehouseLetterId);
            item.setCreatorId(loginUser.getId());
            item.setCreatorName(loginUser.buildOptName());
            item.setCreateTime(LocalDateTime.now());
        });
        return this.saveBatch(warehouseLetterAttachFiles);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAndSave(Integer warehouseLetterId, List<WarehouseLetterAttachFile> warehouseLetterAttachFiles) {
        Assert.notNull(warehouseLetterId, "非法参数");
        this.deleteByWarehouseLetterId(warehouseLetterId);
        return this.batchInsert(warehouseLetterId, warehouseLetterAttachFiles);
    }

    @Override
    public int deleteByWarehouseLetterId(Integer warehouseLetterId) {
        Assert.notNull(warehouseLetterId, "非法参数");
        LambdaUpdateWrapper<WarehouseLetterAttachFile> lambdaUpdateWrapper = Wrappers.lambdaUpdate();
        lambdaUpdateWrapper.eq(WarehouseLetterAttachFile::getWarehouseLetterId, warehouseLetterId);
        return this.warehouseLetterAttachFileMapper.delete(lambdaUpdateWrapper);
    }

    @Override
    public List<WarehouseLetterAttachFile> findByWarehouseLetterId(Integer warehouseLetterId) {
        Assert.notNull(warehouseLetterId, "非法参数");
        LambdaQueryWrapper<WarehouseLetterAttachFile> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(WarehouseLetterAttachFile::getWarehouseLetterId, warehouseLetterId);
        return this.warehouseLetterAttachFileMapper.selectList(queryWrapper);
    }
}
