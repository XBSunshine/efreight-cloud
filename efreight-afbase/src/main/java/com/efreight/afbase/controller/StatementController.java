package com.efreight.afbase.controller;


import javax.validation.Valid;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.DebitNote;
import com.efreight.afbase.entity.ListSend;
import com.efreight.afbase.entity.Statement;
import com.efreight.afbase.service.StatementCurrencyService;
import com.efreight.afbase.service.StatementService;
import com.efreight.common.security.util.MessageInfo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * CSS 应收：清单 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2019-12-24
 */
@RestController
@RequestMapping("/statement")
@AllArgsConstructor
@Slf4j
public class StatementController {

    private final StatementService statementService;
    private final StatementCurrencyService service;

    /**
     * 分页查询
     *
     * @param page
     * @param statement
     * @return
     */
    @GetMapping
    public MessageInfo page(Page page, Statement statement) {
        try {
            IPage iPage = statementService.getPage(page, statement);
            return MessageInfo.ok(iPage);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping(value = "/page2")
    public MessageInfo getPage2(Page page, Statement statement) {
        IPage iPage = statementService.getPage2(page, statement);
        return MessageInfo.ok(iPage);
    }

    //合计行
    @GetMapping("/getTatol")
    public MessageInfo getTatol(Statement statement) {
        return MessageInfo.ok(statementService.getTatol(statement));
    }

    /**
     * 添加
     *
     * @param bean 实体
     * @return success/false
     * @throws Exception
     */
    @PostMapping(value = "/doSave")
    public MessageInfo doSave(@Valid @RequestBody Statement bean) {
        try {
            return MessageInfo.ok(statementService.doSave(bean));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 添加
     *
     * @param bean 实体
     * @return success/false
     * @throws Exception
     */
    @PostMapping(value = "/doUpdate")
    public MessageInfo doUpdate(@Valid @RequestBody Statement bean) {
        try {
            return MessageInfo.ok(statementService.doUpdate(bean));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }


    @PostMapping(value = "/queryBill")
    public MessageInfo queryBill(@Valid @RequestBody Statement bean) {

        return MessageInfo.ok(service.queryBill(bean.getDebitNoteIds()));
    }

    /**
     * 清单打印-PDF版
     *
     * @param statementIds
     * @return
     */
    @GetMapping("/print/{statementIds}/{lang}/{businessScope}/{isTrue}")
    public MessageInfo printStatement(@PathVariable("statementIds") String statementIds, @PathVariable("lang") String lang, @PathVariable("businessScope") String businessScope, @PathVariable("isTrue") Boolean isTrue) {
        try {
            String filePath = statementService.print(statementIds, lang, businessScope, isTrue);
            return MessageInfo.ok(filePath);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 账单清单导出-Excel版
     *
     * @param statementId
     * @param lang
     * @param businessScope
     * @param isTrue
     * @return
     */
    @PostMapping("/printExcel/{statementId}/{lang}/{businessScope}/{isTrue}")
    public void printStatementExcel(@PathVariable("statementId") String statementId, @PathVariable("lang") String lang, @PathVariable("businessScope") String businessScope, @PathVariable("isTrue") Boolean isTrue) {
        try {
            statementService.printExcel(statementId, lang, businessScope, isTrue);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 结算清单导出-Excel版
     *
     * @param statementId
     * @param lang
     * @param businessScope
     * @return
     */
    @PostMapping("/exportSettleStatementExcel/{statementId}/{lang}/{businessScope}")
    public void exportSettleStatementExcel(@PathVariable("statementId") String statementId, @PathVariable("lang") String lang, @PathVariable("businessScope") String businessScope) {
        try {
            statementService.exportSettleStatementExcel(statementId, lang, businessScope);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 清单发送
     *
     * @param bean
     * @return
     */
    @PostMapping("/send")
    public MessageInfo send(@RequestBody ListSend bean) {
        try {
            return MessageInfo.ok(statementService.send(bean));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 删除清单
     *
     * @param statementId
     * @return
     */
    @DeleteMapping("/{statementId}/{businessScope}/{statementNum}")
    public MessageInfo delete(@PathVariable("statementId") Integer statementId, @PathVariable("businessScope") String businessScope, @PathVariable("statementNum") String statementNum) {
        try {
            statementService.delete(statementId, businessScope, statementNum);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }

    }

    @GetMapping(value = "/checkCssStatement")
    public MessageInfo checkCssStatement(Integer statementId) {
        try {
            return MessageInfo.ok(statementService.checkCssStatement(statementId));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    
    /**
     * 收入对账清单列表导出
     *
     * @param bean
     * @return
     */
    @PostMapping("/exportExcelList")
    public void exportExcelList(Statement bean) {
        try {
        	statementService.exportExcelList(bean);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}

