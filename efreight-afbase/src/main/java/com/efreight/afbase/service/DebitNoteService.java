package com.efreight.afbase.service;

import java.io.IOException;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.DebitNote;
import com.efreight.afbase.entity.DebitNoteTree;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.DebitNoteSendEntity;
import com.itextpdf.text.DocumentException;

/**
 * <p>
 * 清单 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2019-11-07
 */
public interface DebitNoteService extends IService<DebitNote> {

    IPage getPage(Page page, DebitNote debitNote);
    IPage getPage2(Page page, DebitNote debitNote);
    List<DebitNote> select( DebitNote debitNote);
    List<DebitNote> select2( DebitNote debitNote);
    List<DebitNote> selectOperation1( DebitNote debitNote);
    List<DebitNote> selectOperation2( DebitNote debitNote);
    List<DebitNoteTree> selectOperation( DebitNote debitNote);

    String print(String modelType, Integer debitNoteId,boolean flag,String template);

    String printMany(String modelType, String debitNoteIds);
    Boolean doDelete(DebitNote debitNote);

    boolean send(DebitNoteSendEntity debitNoteSendEntity);
    
    List<DebitNote> selectCheckDebit ( DebitNote debitNote);
    boolean deleteDebitNote(String debitNoteNum);
    
    String printManyNew(String modelType, String debitNoteIds,String businessScope) throws IOException, DocumentException;

    List<String> printManyNewForStatementPrint(String modelType, String debitNoteIds,String businessScope) throws IOException, DocumentException;

    void exportExcel(String modelType, String debitNoteIds, String businessScope);
    void exportExcelList(DebitNote bean);

    boolean updateDebitNote(Integer debitNoteId,Integer statementId);
}
