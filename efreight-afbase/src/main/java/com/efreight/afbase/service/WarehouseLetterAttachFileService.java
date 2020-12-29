package com.efreight.afbase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.WarehouseLetterAttachFile;

import java.util.List;

/**
 * @author lc
 * @date 2020/8/26 16:49
 */
public interface WarehouseLetterAttachFileService extends IService<WarehouseLetterAttachFile> {

    /**
     * 批量添加数据
     * @param warehouseLetterId 托书模板ID
     * @param warehouseLetterAttachFiles
     */
    boolean batchInsert(Integer warehouseLetterId, List<WarehouseLetterAttachFile> warehouseLetterAttachFiles);

    /**
     * 修改或保存
     * @param warehouseLetterId 托书模板ID
     * @param warehouseLetterAttachFiles
     */
    boolean updateAndSave(Integer warehouseLetterId, List<WarehouseLetterAttachFile> warehouseLetterAttachFiles);

    /**
     * 根据托书模板删除数据
     * @param warehouseLetterId 托书模板ID
     * @return
     */
    int deleteByWarehouseLetterId(Integer warehouseLetterId);

    /**
     * 根据托书模板ID查询数据
     * @param warehouseLetterId 托书模板ID
     * @return
     */
    List<WarehouseLetterAttachFile> findByWarehouseLetterId(Integer warehouseLetterId);
}
