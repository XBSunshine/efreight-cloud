package com.efreight.afbase.service;

import com.efreight.afbase.entity.AwbPrintChargesOther;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * AF 操作管理 运单制单 杂费表 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2019-10-11
 */
public interface AwbPrintChargesOtherService extends IService<AwbPrintChargesOther> {

    void deleteByAwbPrintId(String awbPrintId);
}
