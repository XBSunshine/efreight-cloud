package com.efreight.hrs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.hrs.dao.UserDeptMapper;
import com.efreight.hrs.entity.Dept;
import com.efreight.hrs.entity.Log;
import com.efreight.hrs.entity.User;
import com.efreight.hrs.entity.UserDept;
import com.efreight.hrs.service.DeptService;
import com.efreight.hrs.service.LogService;
import com.efreight.hrs.service.UserDeptService;
import com.efreight.hrs.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
@Service
@Slf4j
@AllArgsConstructor
public class UserDeptServiceImpl extends ServiceImpl<UserDeptMapper, UserDept> implements UserDeptService {

    private final LogService logService;
    private final DeptService deptService;

    /**
     * 更新用户部门表
     *
     * @param userDept
     */
    @Override
    public void updateByUserIdAndDeptId(UserDept userDept) {
        baseMapper.updateByUserIdAndDeptId(userDept);
    }

    /**
     * 新建用户部门表
     *
     * @param userDept
     */
    @Override
    public void saveUserDept(UserDept userDept) {
        baseMapper.insert(userDept);

    }

    /**
     * 查询列表
     *
     * @param userId
     * @return
     */
    @Override
    public List<UserDept> getByUserId(String userId) {
        return baseMapper.getByUserId(userId, SecurityUtils.getUser().getOrgId());
    }

    /**
     * 前段调保存
     *
     * @param userId
     * @param deptId
     * @param jobPosition
     */
    @Override
    public void save(Integer userId, Integer deptId, String jobPosition) {
        //检验用户是否黑名单
        if (ifBlackList(userId.toString())) {
            throw new RuntimeException("该用户已列入黑名单，不可新建！");
        }

        //判断部门是否已为该用户的负责部门
        QueryWrapper<UserDept> queryWrapper = Wrappers.query();
        queryWrapper.eq("user_id", userId).eq("dept_id", deptId).eq("org_id", SecurityUtils.getUser().getOrgId());
        UserDept selectOne = baseMapper.selectOne(queryWrapper);
        if (selectOne != null) {
            throw new RuntimeException("该部门已为用户负责的部门,请选择其他部门");
        }
        UserDept userDept = new UserDept();
        userDept.setJobPosition(jobPosition);
        userDept.setDeptId(deptId);
        userDept.setCreatorId(SecurityUtils.getUser().getId());
        userDept.setCreateTime(LocalDateTime.now());
        userDept.setOrgId(SecurityUtils.getUser().getOrgId());
        userDept.setUserId(userId);
        userDept.setIsMain(false);
        saveUserDept(userDept);

        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("兼职部门新建");
            logBean.setOpName("用户管理兼职部门");
            logBean.setOpInfo("用户管理兼职部门新建：部门号为" + deptId);
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("用户管理兼职部门新建成功，添加日志失败");
        }
    }

    /**
     * 删除
     *
     * @param userId
     * @param deptId
     */
    @Override
    public void delete(Integer userId, Integer deptId) {
        //检验用户是否黑名单
        if (ifBlackList(userId.toString())) {
            throw new RuntimeException("该用户已列入黑名单，不可删除！");
        }
        //判断该部门是否为主负责部门
        QueryWrapper<UserDept> queryWrapper = Wrappers.query();
        queryWrapper.eq("user_id", userId).eq("dept_id", deptId).eq("org_id", SecurityUtils.getUser().getOrgId()).eq("isMain", true);
        UserDept selectOne = baseMapper.selectOne(queryWrapper);
        if (selectOne != null) {
            throw new RuntimeException("该部门为用户主负责部门,无法删除");
        }
        UpdateWrapper<UserDept> updateWrapper = Wrappers.update();
        updateWrapper.eq("user_id", userId).eq("dept_id", deptId).eq("org_id", SecurityUtils.getUser().getOrgId());
        baseMapper.delete(updateWrapper);

        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("兼职部门删除");
            logBean.setOpName("用户管理兼职部门");
            logBean.setOpInfo("用户管理兼职部门删除：部门号为" + deptId);
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("用户管理兼职部门删除成功，添加日志失败");
        }
    }

    @Override
    public List<UserDept> getPartTimeJobByUserId(String userId) {
        LambdaQueryWrapper<UserDept> wrapper = Wrappers.<UserDept>lambdaQuery().eq(UserDept::getIsMain, false).eq(UserDept::getUserId, userId).eq(UserDept::getOrgId, SecurityUtils.getUser().getOrgId());
        List<UserDept> userDeptList = baseMapper.selectList(wrapper);
        userDeptList.stream().forEach(userDept -> {
            Dept dept = deptService.getDeptByID(userDept.getDeptId());
            if (dept != null) {
                userDept.setDeptName(dept.getDeptName());
            }
        });

        return userDeptList;
    }

    private boolean ifBlackList(String userId) {
        User user = baseMapper.selectUser(userId, SecurityUtils.getUser().getOrgId().toString());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getBlacklistDate() == null) {
            return false;
        } else {
            return true;
        }
    }
}
