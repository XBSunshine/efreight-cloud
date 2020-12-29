package com.efreight.hrs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.hrs.entity.TomVersionRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xiaobo
 * @since 2019-07-22
 */
public interface TomVersionRecordService extends IService<TomVersionRecord> {

    void deleteRecord(Integer recordId);

    void updateRecord(TomVersionRecord tomVersionRecord);

    void saveRecord(TomVersionRecord tomVersionRecord);

    IPage<TomVersionRecord> queryListPage(Page page, TomVersionRecord tomVersionRecord);

    void deleteRecordByVersionId(Integer versionId);

    List<TomVersionRecord> getRecordListByVersionId(Integer versionId);
}
