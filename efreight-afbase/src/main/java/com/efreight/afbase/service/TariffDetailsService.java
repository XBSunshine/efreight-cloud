package com.efreight.afbase.service;

import com.efreight.afbase.entity.TariffDetails;
import com.efreight.afbase.entity.TariffDetailsCIQ;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * AF 关税税则 服务类
 * </p>
 *
 * @author qipm
 * @since 2020-05-20
 */
public interface TariffDetailsService extends IService<TariffDetails> {

	IPage<TariffDetails> getListPage(Page page, TariffDetails bean);
	List<TariffDetailsCIQ> getCIQ(TariffDetailsCIQ bean);

    List<TariffDetails> getList(String productName);

	TariffDetails view(String productCode);
}
