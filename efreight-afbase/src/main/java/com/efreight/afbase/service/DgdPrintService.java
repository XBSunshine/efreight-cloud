package com.efreight.afbase.service;

import com.efreight.afbase.entity.DgdPrint;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.DgdPrintList;

import java.util.List;

/**
 * <p>
 * AF 出口订单 DGD 制单 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-01-14
 */
public interface DgdPrintService extends IService<DgdPrint> {

    List<DgdPrint> getList(String orderUuid);

    void saveDgdPrint(DgdPrint dgdPrint);

    void updateDgdPrint(DgdPrint dgdPrint);

    DgdPrint view(Integer dgdPrintId);

    void delete(Integer dgdPrintId);

    String printG(Integer dgdPrintId);

    String printT(Integer dgdPrintId);
}
