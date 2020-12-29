package com.efreight.afbase.service;

import com.efreight.afbase.entity.InboundFiles;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * AF 操作计划 操作出重表 照片文件 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2019-12-05
 */
public interface InboundFilesService extends IService<InboundFiles> {

    Integer saveInboundFile(InboundFiles inboundFiles);

    void delete(Integer fileId);

    List<InboundFiles> getList(Integer inboundId);
}
