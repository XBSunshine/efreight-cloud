package com.efreight.hrs.service;

import com.efreight.hrs.entity.Blacklist;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
public interface BlacklistService extends IService<Blacklist> {
     void saveBlackList(Blacklist blacklist);
}
