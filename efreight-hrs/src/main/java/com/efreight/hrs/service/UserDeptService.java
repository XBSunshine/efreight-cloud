package com.efreight.hrs.service;

import com.efreight.hrs.entity.UserDept;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wangxx
 * @since 2019-07-15
 */
public interface UserDeptService extends IService<UserDept> {

    void updateByUserIdAndDeptId(UserDept userDept);

    void saveUserDept(UserDept userDept);

    List<UserDept> getByUserId(String userId);

    void save(Integer userId, Integer deptId, String jobPosition);

    void delete(Integer userId, Integer deptId);

    List<UserDept> getPartTimeJobByUserId(String userId);
}
