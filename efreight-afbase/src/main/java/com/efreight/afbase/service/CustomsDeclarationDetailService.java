package com.efreight.afbase.service;

import com.efreight.afbase.entity.CustomsDeclarationDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * AF 报关单 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-12-07
 */
public interface CustomsDeclarationDetailService extends IService<CustomsDeclarationDetail> {

    List<CustomsDeclarationDetail> listByCustomsDeclarationId(Integer customsDeclarationId);

    void updateCustomsDeclarationDetail(Integer customsDeclarationId,List<CustomsDeclarationDetail> list);
}
