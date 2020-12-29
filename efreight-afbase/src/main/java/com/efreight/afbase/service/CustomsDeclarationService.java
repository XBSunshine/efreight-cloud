package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.CustomsDeclaration;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * AF 报关单 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-12-07
 */
public interface CustomsDeclarationService extends IService<CustomsDeclaration> {

    IPage getPage(Page page, CustomsDeclaration customsDeclaration);

    CustomsDeclaration view(Integer customsDeclarationId);

    Integer insert(CustomsDeclaration customsDeclaration);

    void modify(CustomsDeclaration customsDeclaration);

    void delete(Integer customsDeclarationId);

    CustomsDeclaration total(CustomsDeclaration customsDeclaration);

    void exportExcel(Integer customsDeclarationId);
}
