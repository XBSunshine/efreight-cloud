package com.efreight.afbase.service;

import com.efreight.afbase.entity.AwbPrintSize;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * AF 操作管理 运单制单 尺寸表 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2019-10-11
 */
public interface AwbPrintSizeService extends IService<AwbPrintSize> {

    void deleteByAwbPrintId(String awbPrintId);
}
