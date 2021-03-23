package com.efreight.afbase.service;

import com.efreight.afbase.entity.CssCostFiles;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * CSS 成本对账：附件表 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2021-03-10
 */
public interface CssCostFilesService extends IService<CssCostFiles> {

    List<CssCostFiles> getList(String flag, Integer id);

    void insert(CssCostFiles cssCostFiles);

    void insertBatch(List<CssCostFiles> list);

    void delete(Integer fileId);
}
