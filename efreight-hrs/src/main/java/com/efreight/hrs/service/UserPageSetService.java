package com.efreight.hrs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.hrs.entity.UserPageSet;
import com.efreight.hrs.entity.UserRole;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
public interface UserPageSetService extends IService<UserPageSet> {
    Boolean removeUserPageSet(UserPageSet userPageSet);

    List<UserPageSet> listByMap(String pageName);
}
