package com.efreight.hrs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.hrs.entity.TomVersion;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.hrs.entity.TomVersionRecord;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xiaobo
 * @since 2019-07-22
 */
public interface TomVersionService extends IService<TomVersion> {

    IPage<TomVersion> queryListPage(Page page, TomVersion tomVersion);

    IPage<TomVersionRecord> queryChildrenListPage(Page page, TomVersionRecord tomVersionRecord);

    void saveVersion(TomVersion tomVersion);

    void saveRecord(TomVersionRecord tomVersionRecord);

    void updateVersion(TomVersion tomVersion);

    void updateRecord(TomVersionRecord tomVersionRecord);

    void deleteVersion(Integer versionId);

    void deleteRecord(Integer recordId);

    TomVersion queryVersion(Integer versionId);

    TomVersionRecord queryRecord(Integer recordId);

    List<TomVersion> getListForHomePage();

    List<TomVersionRecord> getRecordListByVersionId(Integer versionId);
}
