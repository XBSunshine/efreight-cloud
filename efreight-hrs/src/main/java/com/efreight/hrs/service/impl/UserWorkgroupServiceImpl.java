package com.efreight.hrs.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.hrs.dao.UserWorkgroupDetailMapper;
import com.efreight.hrs.dao.UserWorkgroupMapper;
import com.efreight.hrs.entity.UserWorkgroup;
import com.efreight.hrs.entity.UserWorkgroupDetail;
import com.efreight.hrs.pojo.org.workgroup.UserListBean;
import com.efreight.hrs.pojo.org.workgroup.UserWorkgroupBean;
import com.efreight.hrs.pojo.org.workgroup.UserWorkgroupExport;
import com.efreight.hrs.pojo.org.workgroup.UserWorkgroupQuery;
import com.efreight.hrs.service.UserWorkgroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lc
 * @date 2020/10/15 14:29
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class UserWorkgroupServiceImpl extends ServiceImpl<UserWorkgroupMapper, UserWorkgroup> implements UserWorkgroupService {

    private final UserWorkgroupDetailMapper userWorkgroupDetailMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(UserWorkgroupBean userWorkgroupBean){
        EUserDetails loginUser = SecurityUtils.getUser();
        userWorkgroupBean.setOrgId(loginUser.getOrgId());

        Assert.notNull(userWorkgroupBean.getOrgId(), "企业ID不能为空");
        Assert.hasLength(userWorkgroupBean.getWorkgroupName(), "工作组名称不能为空");

        UserWorkgroup userWorkgroup = userWorkgroupBean.buildUserWorkgroup();
        userWorkgroup.setCreateTime(LocalDateTime.now());
        userWorkgroup.setCreatorId(loginUser.getId());
        userWorkgroup.setCreatorName(loginUser.buildOptName());
        this.baseMapper.insert(userWorkgroup);

        saveUserWorkgroupDetail(userWorkgroupBean, userWorkgroup.getWorkgroupId());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserWorkgroupBean userWorkgroupBean) {
        EUserDetails loginUser = SecurityUtils.getUser();
        userWorkgroupBean.setOrgId(loginUser.getOrgId());
        Integer userWorkgroupId = userWorkgroupBean.getWorkgroupId();

        Assert.notNull(userWorkgroupId, "数据ID不能为空");
        Assert.notNull(userWorkgroupBean.getOrgId(), "企业ID不能为空");
        Assert.hasLength(userWorkgroupBean.getWorkgroupName(), "工作组名称不能为空");

        UserWorkgroup userWorkgroup = userWorkgroupBean.buildUserWorkgroup();
        userWorkgroup.setWorkgroupId(userWorkgroupId);
        userWorkgroup.setEditorId(loginUser.getId());
        userWorkgroup.setEditorName(loginUser.buildOptName());
        userWorkgroup.setEditTime(LocalDateTime.now());
        this.baseMapper.updateById(userWorkgroup);

        deleteUserWorkgroupDetailByWorkgroupId(userWorkgroupId);
        saveUserWorkgroupDetail(userWorkgroupBean, userWorkgroupId);
    }

    @Override
    public IPage<UserWorkgroup> query(Page page, UserWorkgroupQuery query) {
        EUserDetails loginUser = SecurityUtils.getUser();
        query.setOrgId(loginUser.getOrgId());
        return this.baseMapper.query(page, query);
    }

    @Override
    public List<UserWorkgroupExport> exportQuery(UserWorkgroupQuery query) {
        EUserDetails loginUser = SecurityUtils.getUser();
        query.setOrgId(loginUser.getOrgId());
        return this.baseMapper.exportQuery(query);
    }

    @Override
    public List<UserListBean> findUser(Integer orgId) {
        Assert.notNull(orgId, "企业ID不能为空");
        return this.baseMapper.findUser(orgId);
    }

    @Override
    public UserWorkgroup detail(Integer workgroupId) {
        Assert.notNull(workgroupId, "数据ID为空");
        UserWorkgroup workgroup = this.baseMapper.selectById(workgroupId);
        if(workgroup != null){
            List<UserListBean> userList = this.baseMapper.findByWorkgroupId(workgroupId);
            workgroup.setUserList(userList);
            workgroup.setUserCount(userList.size());
        }
        return workgroup;
    }

    @Override
    public void deleteByWorkgroupId(Integer workgroupId) {
        Assert.notNull(workgroupId, "数据ID为空");
        this.baseMapper.deleteById(workgroupId);
        LambdaUpdateWrapper<UserWorkgroupDetail> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(UserWorkgroupDetail::getWorkgroupId, workgroupId);
        this.userWorkgroupDetailMapper.delete(updateWrapper);
    }

    private void saveUserWorkgroupDetail(UserWorkgroupBean userWorkgroupBean, Integer userWorkgroupId) {
        List<UserWorkgroupDetail> userWorkgroupDetailList = userWorkgroupBean.buildUserWorkDetail(userWorkgroupId);
        userWorkgroupDetailList.stream().forEach((item)-> this.userWorkgroupDetailMapper.insert(item));
    }

    private int deleteUserWorkgroupDetailByWorkgroupId(Integer workgroupId){
        LambdaUpdateWrapper<UserWorkgroupDetail> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(UserWorkgroupDetail::getWorkgroupId, workgroupId);
        return this.userWorkgroupDetailMapper.delete(updateWrapper);
    }

    @Override
    public List<UserWorkgroup> selectWorkgroup(String businessScope) {
        return this.baseMapper.selectWorkgroup(businessScope,SecurityUtils.getUser().getOrgId());
    }

    @Override
    public List<Integer> selectWorkgroupByServicerId(Integer servicerId) {
        return this.baseMapper.selectWorkgroupByServicerId(servicerId);
    }
}
