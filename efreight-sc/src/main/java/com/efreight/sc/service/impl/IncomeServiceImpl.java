package com.efreight.sc.service.impl;

import com.efreight.sc.entity.Income;
import com.efreight.sc.dao.IncomeMapper;
import com.efreight.sc.service.IncomeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * CS 延伸服务 应收 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-03
 */
@Service
public class IncomeServiceImpl extends ServiceImpl<IncomeMapper, Income> implements IncomeService {

}
