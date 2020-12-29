package com.efreight.hrs.service.impl;

import com.efreight.hrs.entity.Token;
import com.efreight.hrs.dao.TokenMapper;
import com.efreight.hrs.service.TokenService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
@Service
public class TokenServiceImpl extends ServiceImpl<TokenMapper, Token> implements TokenService {

}
