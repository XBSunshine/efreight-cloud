package com.efreight.afbase.controller;


import com.efreight.afbase.entity.CssIncomeWriteoffStatementDetail;
import com.efreight.afbase.service.CssIncomeWriteoffStatementDetailService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * CSS 应收：核销单 明细（清单） 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-07
 */
@RestController
@RequestMapping("/cssIncomeWriteoffStatementDetail")
@AllArgsConstructor
@Slf4j
public class CssIncomeWriteoffStatementDetailController {

    private final CssIncomeWriteoffStatementDetailService cssIncomeWriteoffStatementDetailService;

    /**
     * 根据核销单id查询清单核销明细（新表）
     * @param incomeWriteoffId
     * @return
     */
    @GetMapping("/{incomeWriteoffId}")
    public MessageInfo queryListById(@PathVariable("incomeWriteoffId") Integer incomeWriteoffId){
        try {
          List<CssIncomeWriteoffStatementDetail> list = cssIncomeWriteoffStatementDetailService.queryListByIncomeWriteoffId(incomeWriteoffId);
          return MessageInfo.ok(list);
        }catch (Exception e){
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

}

