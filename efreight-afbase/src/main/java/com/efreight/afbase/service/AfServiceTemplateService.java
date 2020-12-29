package com.efreight.afbase.service;

import com.efreight.afbase.entity.AfServiceTemplate;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.exportExcel.AfServiceTemplateExcel;

/**
 * <p>
 * AF 基础信息 服务类别：收付模板设定 服务类
 * </p>
 *
 * @author qipm
 * @since 2020-06-01
 */
public interface AfServiceTemplateService extends IService<AfServiceTemplate> {

	IPage<AfServiceTemplate> getListPage(Page page, AfServiceTemplate bean);
	Boolean doSave(AfServiceTemplate bean);
	AfServiceTemplate getView(AfServiceTemplate bean);
	List<AfServiceTemplate> getServicetemplate(AfServiceTemplate bean);
	Boolean doEdit(AfServiceTemplate bean);
	Boolean doDelete(AfServiceTemplate bean);
	List<AfServiceTemplateExcel> queryListForExcel(AfServiceTemplate bean);
}
