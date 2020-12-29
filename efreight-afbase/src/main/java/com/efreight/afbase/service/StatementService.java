package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.ListSend;
import com.efreight.afbase.entity.Statement;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * CSS 应收：清单 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2019-12-24
 */
public interface StatementService extends IService<Statement> {

    IPage getPage2(Page page, Statement statement);
    IPage getPage(Page page, Statement statement);

    Boolean doSave(Statement bean);
    Boolean doUpdate(Statement bean);
    
//    String printStatement(Statement statement,String lang,Boolean replace);

//    String printStatementForMany(List<Statement> list,String lang);

    String print(String statementIds,String lang,String businessScope,Boolean isTrue);
    boolean send(ListSend bean);

    void delete(Integer statementId,String businessScope,String statementNum);
    
    Statement checkCssStatement(Integer statementId);
    List<Statement> getTatol(Statement statement);

    void printExcel(String statementId, String lang, String businessScope, Boolean isTrue);

    void exportSettleStatementExcel(String statementId, String lang, String businessScope);
    void exportExcelList(Statement bean);
}
