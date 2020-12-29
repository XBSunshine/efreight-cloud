package com.efreight.afbase.service;

import com.efreight.afbase.entity.AfOrderStorageMns;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author qipm
 * @since 2020-10-26
 */
public interface AfOrderStorageMnsService extends IService<AfOrderStorageMns> {

	Boolean doSave(AfOrderStorageMns bean);
	List<AfOrderStorageMns> queryList(AfOrderStorageMns bean);
}
