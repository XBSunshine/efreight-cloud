package com.efreight.hrs.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.hrs.entity.Log;
import com.efreight.hrs.entity.TomVersion;
import com.efreight.hrs.dao.TomVersionMapper;
import com.efreight.hrs.entity.TomVersionRecord;
import com.efreight.hrs.service.LogService;
import com.efreight.hrs.service.TomVersionRecordService;
import com.efreight.hrs.service.TomVersionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2019-07-22
 */
@Slf4j
@AllArgsConstructor
@Service
public class TomVersionServiceImpl extends ServiceImpl<TomVersionMapper, TomVersion> implements TomVersionService {

    private final TomVersionRecordService tomVersionRecordService;
    private final LogService logService;

    /**
     * 版本主表分页查询
     *
     * @param page
     * @param tomVersion
     * @return
     */
    @Override
    public IPage<TomVersion> queryListPage(Page page, TomVersion tomVersion) {
        QueryWrapper<TomVersion> wrapper = Wrappers.query();
        if (StrUtil.isNotBlank(tomVersion.getVersionCode())) {
            wrapper.eq("version_code", tomVersion.getVersionCode());
        }
        if (tomVersion.getVersionDateStart() != null && tomVersion.getVersionDateEnd() != null) {
            wrapper.between("version_date", tomVersion.getVersionDateStart(), tomVersion.getVersionDateEnd());
        }
        if (tomVersion.getVersionDateStart() != null && tomVersion.getVersionDateEnd() == null) {
            wrapper.ge("version_date", tomVersion.getVersionDateStart());
        }
        if (tomVersion.getVersionDateStart() == null && tomVersion.getVersionDateEnd() != null) {
            wrapper.le("version_date", tomVersion.getVersionDateEnd());
        }

        return baseMapper.selectPage(page, wrapper);
    }

    /**
     * 子记录表分页查询
     *
     * @param page
     * @param tomVersionRecord
     * @return
     */
    @Override
    public IPage<TomVersionRecord> queryChildrenListPage(Page page, TomVersionRecord tomVersionRecord) {
        return tomVersionRecordService.queryListPage(page, tomVersionRecord);
    }

    /**
     * 版本主表新建
     *
     * @param tomVersion
     */
    @Override
    public void saveVersion(TomVersion tomVersion) {
        if (StrUtil.isBlank(tomVersion.getVersionCode())) {
            throw new RuntimeException("版本号不能为空");
        }
        if (tomVersion.getVersionDate() == null) {
            throw new RuntimeException("版本更新时间不能为空");
        }
        if (StrUtil.isBlank(tomVersion.getVersionText())) {
            throw new RuntimeException("版本内容不能为空");
        }
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("新建");
            logBean.setOpName("版本管理");
            logBean.setOpInfo("版本新建:" + tomVersion.getVersionText());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("版本新建成功，日志添加失败");
        }
        baseMapper.insert(tomVersion);
    }

    /**
     * 记录子表新建
     *
     * @param tomVersionRecord
     */
    @Override
    public void saveRecord(TomVersionRecord tomVersionRecord) {
        tomVersionRecordService.saveRecord(tomVersionRecord);
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("新建");
            logBean.setOpName("版本记录");
            logBean.setOpInfo("版本记录新建:" + tomVersionRecord.getUpdateText());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("版本记录新建成功，日志添加失败");
        }
    }

    /**
     * 修改版本主表
     *
     * @param tomVersion
     */
    @Override
    public void updateVersion(TomVersion tomVersion) {
        if (tomVersion.getVersionId() == null) {
            throw new RuntimeException("版本id不能为空");
        }
        if (tomVersion.getVersionDate() == null) {
            throw new RuntimeException("版本更新时间不能为空");
        }
        if (StrUtil.isBlank(tomVersion.getVersionText())) {
            throw new RuntimeException("版本内容不能为空");
        }
        if (StrUtil.isBlank(tomVersion.getVersionCode())) {
            throw new RuntimeException("版本号不能为空");
        }
        baseMapper.updateById(tomVersion);
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("修改");
            logBean.setOpName("版本管理");
            logBean.setOpInfo("版本修改:" + tomVersion.getVersionText());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("版本修改成功，日志添加失败");
        }
    }

    /**
     * 修改记录子表
     *
     * @param tomVersionRecord
     */
    @Override
    public void updateRecord(TomVersionRecord tomVersionRecord) {
        tomVersionRecordService.updateRecord(tomVersionRecord);
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("修改");
            logBean.setOpName("版本记录");
            logBean.setOpInfo("版本记录修改:" + tomVersionRecord.getUpdateText());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("版本记录修改成功，日志添加失败");
        }
    }

    /**
     * 删除版本
     *
     * @param versionId
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteVersion(Integer versionId) {
        //删除版本主表
        baseMapper.deleteById(versionId);

        //删除记录表
        tomVersionRecordService.deleteRecordByVersionId(versionId);
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("删除");
            logBean.setOpName("版本管理");
            logBean.setOpInfo("版本删除:" + versionId);
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("版本删除成功，日志添加失败");
        }
    }

    /**
     * 删除记录表
     *
     * @param recordId
     */
    @Override
    public void deleteRecord(Integer recordId) {
        tomVersionRecordService.deleteRecord(recordId);
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("删除");
            logBean.setOpName("版本记录");
            logBean.setOpInfo("版本记录删除:" + recordId);
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("版本记录删除成功，日志添加失败");
        }
    }

    /**
     * 查询单个版本
     *
     * @param versionId
     * @return
     */
    @Override
    public TomVersion queryVersion(Integer versionId) {
        return baseMapper.selectById(versionId);
    }

    /**
     * 查询单个记录表
     *
     * @param recordId
     * @return
     */
    @Override
    public TomVersionRecord queryRecord(Integer recordId) {
        return tomVersionRecordService.getById(recordId);
    }

    /**
     * 首页展示
     *
     * @return
     */
    @Override
    public List<TomVersion> getListForHomePage() {
        QueryWrapper<TomVersion> queryWrapper = Wrappers.query();
        queryWrapper.orderByDesc("version_id").last("limit 20");
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 首页查询版本记录
     *
     * @param versionId
     * @return
     */
    @Override
    public List<TomVersionRecord> getRecordListByVersionId(Integer versionId) {
        return tomVersionRecordService.getRecordListByVersionId(versionId);
    }
}
