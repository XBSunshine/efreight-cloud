package com.efreight.afbase.service;

import com.efreight.afbase.entity.IoCost;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * IO 费用录入 成本 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-18
 */
public interface IoCostService extends IService<IoCost> {

    List<IoCost> getCostList(IoCost ioCost);
}
