package com.efreight.hrs.service.impl;

import com.efreight.hrs.entity.Blacklist;
import com.efreight.hrs.dao.BlacklistMapper;
import com.efreight.hrs.service.BlacklistService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
@Service
public class BlacklistServiceImpl extends ServiceImpl<BlacklistMapper, Blacklist> implements BlacklistService {
    @Override
    public void saveBlackList(Blacklist blacklist) {
        baseMapper.insert(blacklist);
    }
}
