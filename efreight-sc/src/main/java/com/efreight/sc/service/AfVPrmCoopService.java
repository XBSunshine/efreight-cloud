package com.efreight.sc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.sc.entity.view.AfVPrmCoop;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * VIEW 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-09
 */
public interface AfVPrmCoopService extends IService<AfVPrmCoop> {

    List<AfVPrmCoop> getList(AfVPrmCoop afVPrmCoop);

    IPage getPage(Page page, AfVPrmCoop afVPrmCoop);
}
