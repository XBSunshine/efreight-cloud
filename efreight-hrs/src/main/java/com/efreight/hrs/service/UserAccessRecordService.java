package com.efreight.hrs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.hrs.entity.Org;
import com.efreight.hrs.entity.UserAccessRecord;

import java.util.List;

/**
 * @author lc
 * @date 2020/5/12 14:58
 */
public interface UserAccessRecordService extends IService<UserAccessRecord> {

    /**
     * 记录访问日志
     * @param accessRecord 数据实体
     */
    void recordAccess(UserAccessRecord accessRecord);

    /**
     * 查询某个用户热门访问
     * @param userId 某个用户
     * @param number 热门个数
     * @return
     */
    List<UserAccessRecord> topAccess(Integer userId, Integer number);

    /**
     * 查询签约公司活跃指数
     * @param
     * @return
     */
    List<Org> topActiveIndex();
}
