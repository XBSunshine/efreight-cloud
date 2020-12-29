package com.efreight.hrs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.hrs.entity.TomPublicNotice;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wangxx
 * @since 2019-07-15
 */
public interface TomPublicNoticeService extends IService<TomPublicNotice> {

    IPage<TomPublicNotice> getTomPublicNoticeListPage(Page<TomPublicNotice> page, TomPublicNotice tomPublicNotice);

    TomPublicNotice getTomPublicNotice(Integer noticeId);

    void saveTomPublicNotice(TomPublicNotice tomPublicNotice);

    List<TomPublicNotice> getListForHomePage();

    void updateNoticeById(TomPublicNotice tomPublicNotice);

    void removeNoticeById(String noticeId);
}
