package com.efreight.hrs.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.hrs.dao.TomHomeApplicaionMapper;
import com.efreight.hrs.entity.Log;
import com.efreight.hrs.entity.Permission;
import com.efreight.hrs.entity.TomHomeApplication;
import com.efreight.hrs.service.LogService;
import com.efreight.hrs.service.PermissionService;
import com.efreight.hrs.service.TomHomeApplicaionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class TomHomeApplicationServiceImpl extends ServiceImpl<TomHomeApplicaionMapper, TomHomeApplication> implements TomHomeApplicaionService {
    private final PermissionService permissionService;
    private final LogService logService;

    @Override
    public List<TomHomeApplication> queryList(String type) {
        return baseMapper.selectList(Wrappers.<TomHomeApplication>lambdaQuery().eq(TomHomeApplication::getOrgId, SecurityUtils.getUser().getOrgId()).eq(TomHomeApplication::getCreatorId, SecurityUtils.getUser().getId()).eq(TomHomeApplication::getApplicationType, type));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(String applicationIds) {
        //删除数据重新创建
        baseMapper.delete(Wrappers.<TomHomeApplication>lambdaUpdate().eq(TomHomeApplication::getOrgId, SecurityUtils.getUser().getOrgId()).eq(TomHomeApplication::getCreatorId, SecurityUtils.getUser().getId()));
        Arrays.asList(applicationIds.split(",")).stream().forEach(applicationId -> {
            applicationId = applicationId.replace("\"", "");
            Permission permission = permissionService.getById(applicationId);
            if (permission != null && "1".equals(permission.getPermissionType()) && StrUtil.isNotBlank(permission.getPath())) {
                TomHomeApplication application = new TomHomeApplication();
                application.setApplicationId(Integer.parseInt(applicationId));
                application.setApplicationName(permission.getPermissionName());
                application.setApplicationPath(permission.getPath());
                application.setApplicationType(permission.getPath().split("/")[1].split("_")[0]);
                application.setOrgId(SecurityUtils.getUser().getOrgId());
                application.setCreatorId(SecurityUtils.getUser().getId());
                application.setCreateTime(LocalDateTime.now());
                application.setIcon(permission.getIcon());
                baseMapper.insert(application);
            }
        });
//
//        try {
//            Log logBean = new Log();
//            logBean.setOpLevel("高");
//            logBean.setOpType("修改");
//            logBean.setOpName("我的常用");
//            logBean.setOpInfo("修改我的常用：" + applicationIds);
//            logService.doSave(logBean);
//        } catch (Exception e) {
//            log.info("修改我的常用成功，保存日志失败");
//        }
    }

    @Override
    public List<Integer> getCheckList() {
        return baseMapper.selectList(Wrappers.<TomHomeApplication>lambdaQuery().eq(TomHomeApplication::getOrgId, SecurityUtils.getUser().getOrgId()).eq(TomHomeApplication::getCreatorId, SecurityUtils.getUser().getId())).stream().map(TomHomeApplication::getApplicationId).collect(Collectors.toList());
    }
}
