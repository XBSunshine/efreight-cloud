package com.efreight.sc.service;

import com.efreight.sc.entity.IoCost;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * IO 费用录入 成本 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-17
 */
public interface IoCostService extends IService<IoCost> {

    List<IoCost> getList(Integer orderId);

    void modify(IoCost ioCost);

    void insert(IoCost ioCost);

    void delete(Integer costId);

    IoCost view(Integer costId);
}
