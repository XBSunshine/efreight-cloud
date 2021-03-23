package com.efreight.afbase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.afbase.entity.CssCostFiles;
import com.efreight.afbase.dao.CssCostFilesMapper;
import com.efreight.afbase.entity.CssCostInvoiceDetail;
import com.efreight.afbase.entity.CssCostInvoiceDetailWriteoff;
import com.efreight.afbase.service.CssCostFilesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.service.CssCostInvoiceDetailService;
import com.efreight.afbase.service.CssCostInvoiceDetailWriteoffService;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * CSS 成本对账：附件表 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2021-03-10
 */
@Service
@AllArgsConstructor
public class CssCostFilesServiceImpl extends ServiceImpl<CssCostFilesMapper, CssCostFiles> implements CssCostFilesService {

    private final CssCostInvoiceDetailService cssCostInvoiceDetailService;
    private final CssCostInvoiceDetailWriteoffService cssCostInvoiceDetailWriteoffService;

    @Override
    public List<CssCostFiles> getList(String flag, Integer id) {
        LambdaQueryWrapper<CssCostFiles> wrapper = Wrappers.<CssCostFiles>lambdaQuery();
        wrapper.eq(CssCostFiles::getOrgId, SecurityUtils.getUser().getOrgId());
        if ("invoice".equals(flag)) {
            wrapper.eq(CssCostFiles::getInvoiceDetailId, id).isNull(CssCostFiles::getInvoiceDetailWriteoffId);
        } else if ("writeoff".equals(flag)) {
            wrapper.eq(CssCostFiles::getInvoiceDetailWriteoffId, id);
        }
        wrapper.orderByDesc(CssCostFiles::getFileId);
        return list(wrapper);
    }

    @Override
    public void insert(CssCostFiles cssCostFiles) {
        if (cssCostFiles.getInvoiceDetailWriteoffId() != null) {
            CssCostInvoiceDetailWriteoff cssCostInvoiceDetailWriteoff = cssCostInvoiceDetailWriteoffService.getById(cssCostFiles.getInvoiceDetailWriteoffId());
            if (cssCostInvoiceDetailWriteoff == null) {
                throw new RuntimeException("数据已变更，请刷洗再试");
            }
        } else {
            CssCostInvoiceDetail cssCostInvoiceDetail = cssCostInvoiceDetailService.getById(cssCostFiles.getInvoiceDetailId());
            if (cssCostInvoiceDetail == null) {
                throw new RuntimeException("数据已变更，请刷洗再试");
            }
        }

        FormatUtils.initDefaultInfo(cssCostFiles, false);
        save(cssCostFiles);
    }

    @Override
    public void insertBatch(List<CssCostFiles> list) {
        CssCostFiles cssCostFile = list.get(0);
        if (cssCostFile.getInvoiceDetailWriteoffId() != null) {
            CssCostInvoiceDetailWriteoff cssCostInvoiceDetailWriteoff = cssCostInvoiceDetailWriteoffService.getById(cssCostFile.getInvoiceDetailWriteoffId());
            if (cssCostInvoiceDetailWriteoff == null) {
                throw new RuntimeException("数据已变更，请刷洗再试");
            }
        } else {
            CssCostInvoiceDetail cssCostInvoiceDetail = cssCostInvoiceDetailService.getById(cssCostFile.getInvoiceDetailId());
            if (cssCostInvoiceDetail == null) {
                throw new RuntimeException("数据已变更，请刷洗再试");
            }
        }
        list.stream().forEach(cssCostFiles -> {
            FormatUtils.initDefaultInfo(cssCostFiles, false);
        });
        saveBatch(list);
    }

    @Override
    public void delete(Integer fileId) {
        removeById(fileId);
    }
}
