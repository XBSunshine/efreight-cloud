package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.AwbPrint;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.procedure.AfPAwbPrintProcedure;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * AF 操作管理 运单制单 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2019-10-11
 */
public interface AwbPrintService extends IService<AwbPrint> {

    void delete(Integer awbPrintId);

    void modify(AwbPrint awbPrint);

    String insert(AwbPrint awbPrint);

    AwbPrint view(Integer awbPrintId);

    IPage getPage(Page page, AwbPrint awbPrint);

    AwbPrint callAfPAwbPrint(AfPAwbPrintProcedure afPAwbPrintProcedure);

    String awbDownloadWithPDF(AfPAwbPrintProcedure afPAwbPrintProcedure);

    String insertHawb(AwbPrint awbPrint);

    String hawbDownloadWithPDF(AfPAwbPrintProcedure afPAwbPrintProcedure);

    void deleteForUnloadOrder(String awbUuid);

    void finish(AwbPrint awbPrint);

    AwbPrint viewByAwbUuid(String awbUuid);

    List<AwbPrint> hawbListByAwbUuid(String awbUuid);

    AwbPrint viewByOrderUuid(String orderUuid);

    List<AwbPrint> hawbListByOrderUuid(String orderUuid);

    void getHawbInfo(String orderUuid);

    Map<String, Object> sendAmsCheckHasSend(Integer awbPrintId);

    String getAmsDataCheck(String type, String awbNumber, String letterId);

    Map<String, Object> sendAmsData(String type, String awbNumber, String letterId);
}
