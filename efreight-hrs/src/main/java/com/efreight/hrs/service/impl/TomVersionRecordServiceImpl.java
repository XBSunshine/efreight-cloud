package com.efreight.hrs.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.hrs.entity.TomVersionRecord;
import com.efreight.hrs.dao.TomVersionRecordMapper;
import com.efreight.hrs.service.TomVersionRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2019-07-22
 */
@Service
public class TomVersionRecordServiceImpl extends ServiceImpl<TomVersionRecordMapper, TomVersionRecord> implements TomVersionRecordService {
    /**
     * 删除记录表
     *
     * @param recordId
     */
    @Override
    public void deleteRecord(Integer recordId) {
        baseMapper.deleteById(recordId);
    }

    /**
     * 更新记录表
     *
     * @param tomVersionRecord
     */
    @Override
    public void updateRecord(TomVersionRecord tomVersionRecord) {
        if (tomVersionRecord.getPermissionId() == null) {
            throw new RuntimeException("权限不能为空");
        }
        if (tomVersionRecord.getVersionId() == null) {
            throw new RuntimeException("版本号不能为空");
        }
        if (StrUtil.isBlank(tomVersionRecord.getUpdateType())) {
            throw new RuntimeException("更新记录类型不能为空");
        }
        if (StrUtil.isBlank(tomVersionRecord.getUpdateText())) {
            throw new RuntimeException("更新记录内容不能为空");
        }
        baseMapper.updateById(tomVersionRecord);
    }

    /**
     * 新建记录表
     *
     * @param tomVersionRecord
     */
    @Override
    public void saveRecord(TomVersionRecord tomVersionRecord) {
        if (tomVersionRecord.getPermissionId() == null) {
            throw new RuntimeException("权限不能为空");
        }
        if (tomVersionRecord.getVersionId() == null) {
            throw new RuntimeException("版本号不能为空");
        }
        if (StrUtil.isBlank(tomVersionRecord.getUpdateType())) {
            throw new RuntimeException("更新记录类型不能为空");
        }
        if (StrUtil.isBlank(tomVersionRecord.getUpdateText())) {
            throw new RuntimeException("更新记录内容不能为空");
        }
        baseMapper.insert(tomVersionRecord);
    }

    /**
     * 分页查询记录表
     *
     * @param page
     * @param tomVersionRecord
     * @return
     */
    @Override
    public IPage<TomVersionRecord> queryListPage(Page page, TomVersionRecord tomVersionRecord) {
//        QueryWrapper<TomVersionRecord> wrapper = Wrappers.query();
//        if (tomVersionRecord.getVersionId() != null) {
//            wrapper.eq("permission_id", tomVersionRecord.getPermissionId());
//        }
//        if (StrUtil.isNotBlank(tomVersionRecord.getUpdateType())) {
//            wrapper.eq("update_type", tomVersionRecord.getUpdateType());
//        }
        IPage<TomVersionRecord> result = baseMapper.selectPageSelf(page, tomVersionRecord.getVersionId(), tomVersionRecord.getPermissionId(), tomVersionRecord.getUpdateType());
        return result;
    }

    /**
     * 根据versionId删除记录表
     *
     * @param versionId
     */
    @Override
    public void deleteRecordByVersionId(Integer versionId) {
        UpdateWrapper<TomVersionRecord> updateWrapper = Wrappers.update();
        updateWrapper.eq("version_id", versionId);
        baseMapper.delete(updateWrapper);
    }

    @Override
    public List<TomVersionRecord> getRecordListByVersionId(Integer versionId) {
        QueryWrapper<TomVersionRecord> queryWrapper = Wrappers.query();
        queryWrapper.eq("version_id", versionId);
        return baseMapper.selectList(queryWrapper);
    }
}
