package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.*;
import com.itextpdf.text.DocumentException;

import java.io.IOException;
import java.util.List;

public interface OperationPlanService extends IService<OperationPlan> {
    IPage getPage(Page page, OperationPlan operationPlan);

    List<Warehouse> findStorehouse(String departure);

    List<Warehouse> findWarehouse(String departure);
    
    Boolean printLetters(Integer orgId,String awbUUIds);

    String printLetters1(Integer orgId,String awbUUIds) throws IOException, DocumentException;

    String printLetters2(Integer orgId,String awbUUIds) throws IOException, DocumentException;

    String printLetters3(Integer orgId,String awbUUIds,String shipperTemplateFile) throws IOException, DocumentException;

    String print(Letters orderLetters, boolean flag);

    String checkLetters(String awbUUIds);

    String isExistExcelTemplate(String awbUUIds);
    
    List<OperationPlanExcel> queryListForExcle(String orderIds);

    Boolean printTag(Integer orgId, String printScope, String orderUuid, String slIds);

    List<AfShipperLetter> getShipperLetterByOrderUuid(String orderUuid, String type);

    String printTagMany(Integer orgId, String orderUuids, String pageName) throws Exception;

    IPage<WarehouseLetter> selectTemplate(Page page,OperationPlan bean);

    String printTagNew(Integer orgId, String printScope, String orderUuid, String slIds, String pageName)throws Exception;

    void exportExcel(String awbUuid);

    List<OperationPlan> exportOperationPlanExcel(OperationPlan operationPlan);
}
