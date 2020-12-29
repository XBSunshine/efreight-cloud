package com.efreight.afbase.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.AwbPrint;
import com.efreight.afbase.entity.procedure.AfPAwbPrintProcedure;
import com.efreight.afbase.service.AwbPrintService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * AF 操作管理 运单制单 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2019-10-11
 */
@RestController
@RequestMapping("/awbPrint")
@AllArgsConstructor
@Slf4j
public class AwbPrintController {

    private final AwbPrintService awbPrintService;

    /**
     * 分页列表查询
     *
     * @param page
     * @param awbPrint
     * @return
     */
    @GetMapping
    public MessageInfo page(Page page, AwbPrint awbPrint) {
        try {
            IPage result = awbPrintService.getPage(page, awbPrint);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查看详情
     *
     * @param awbPrintId
     * @return
     */
    @GetMapping("/view")
    public MessageInfo view(Integer awbPrintId) {
        try {
            AwbPrint awbPrint = awbPrintService.view(awbPrintId);
            return MessageInfo.ok(awbPrint);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查看主单-通过awbUuid
     *
     * @param awbUuid
     * @return
     */
    @GetMapping("/view/{awbUuid}")
    public MessageInfo viewByAwbUuid(@PathVariable("awbUuid") String awbUuid) {
        try {
            AwbPrint awbPrint = awbPrintService.viewByAwbUuid(awbUuid);
            return MessageInfo.ok(awbPrint);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查看主单-通过orderUuid
     *
     * @param orderUuid
     * @return
     */
    @GetMapping("/viewByOrderUuid/{orderUuid}")
    public MessageInfo viewByOrderUuid(@PathVariable("orderUuid") String orderUuid) {
        try {
            AwbPrint awbPrint = awbPrintService.viewByOrderUuid(orderUuid);
            return MessageInfo.ok(awbPrint);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查看分单-通过awbUuid
     *
     * @param awbUuid
     * @return
     */
    @GetMapping("/hawbList/{awbUuid}")
    public MessageInfo hawbListByAwbUuid(@PathVariable("awbUuid") String awbUuid) {
        try {
            List<AwbPrint> list = awbPrintService.hawbListByAwbUuid(awbUuid);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查看分单-通过orderUuid
     *
     * @param orderUuid
     * @return
     */
    @GetMapping("/hawbListByOrderUuid/{orderUuid}")
    public MessageInfo hawbListByOrderUuid(@PathVariable("orderUuid") String orderUuid){
        try {
            List<AwbPrint> list = awbPrintService.hawbListByOrderUuid(orderUuid);
            return MessageInfo.ok(list);
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 获取分单信息并保存
     *
     * @param orderUuid
     * @return
     */
    @GetMapping("/getHawbInfo/{orderUuid}")
    public MessageInfo getHawbInfo(@PathVariable("orderUuid") String orderUuid){
        try {
            awbPrintService.getHawbInfo(orderUuid);
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查看详情（新建查询调存储过程）
     *
     * @param afPAwbPrintProcedure
     * @return
     */
    @GetMapping("/callAfPAwbPrint")
    public MessageInfo callAfPAwbPrint(AfPAwbPrintProcedure afPAwbPrintProcedure) {
        try {
            AwbPrint awbPrint = awbPrintService.callAfPAwbPrint(afPAwbPrintProcedure);
            return MessageInfo.ok(awbPrint);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }


    /**
     * 新增主单
     *
     * @param awbPrint
     * @return
     */
    @PostMapping
    @PreAuthorize("@pms.hasPermission('af_button_awbPrint_saveMawb')")
    public MessageInfo saveMawb(@RequestBody AwbPrint awbPrint) {
        try {
            return MessageInfo.ok(awbPrintService.insert(awbPrint));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 新增分单
     *
     * @param awbPrint
     * @return
     */
    @PostMapping("/hawb")
    @PreAuthorize("@pms.hasPermission('af_button_awbPrint_saveHawb')")
    public MessageInfo saveHawb(@RequestBody AwbPrint awbPrint) {
        try {
            return MessageInfo.ok(awbPrintService.insertHawb(awbPrint));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 编辑主单
     *
     * @param awbPrint
     * @return
     */
    @PutMapping("/mawb")
    @PreAuthorize("@pms.hasPermission('af_button_awbPrint_editMawb')")
    public MessageInfo modifyMawb(@RequestBody AwbPrint awbPrint) {
        try {
            awbPrintService.modify(awbPrint);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 主单已完成
     *
     * @param awbPrint
     * @return
     */
    @PutMapping("/finish/mawb")
    @PreAuthorize("@pms.hasPermission('af_button_awbPrint_finishMawb')")
    public MessageInfo finishMawb(@RequestBody AwbPrint awbPrint) {
        try {
            awbPrintService.finish(awbPrint);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 编辑分单
     *
     * @param awbPrint
     * @return
     */
    @PutMapping("/hawb")
    @PreAuthorize("@pms.hasPermission('af_button_awbPrint_editHawb')")
    public MessageInfo modifyHawb(@RequestBody AwbPrint awbPrint) {
        try {
            awbPrintService.modify(awbPrint);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 分单已完成
     *
     * @param awbPrint
     * @return
     */
    @PutMapping("/finish/hawb")
    @PreAuthorize("@pms.hasPermission('af_button_awbPrint_finishHawb')")
    public MessageInfo finishHawb(@RequestBody AwbPrint awbPrint) {
        try {
            awbPrintService.finish(awbPrint);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 删除主单
     *
     * @param awbPrintId
     * @return
     */
    @DeleteMapping("/mawb/{awbPrintId}")
    public MessageInfo deleteMawb(@PathVariable Integer awbPrintId) {
        try {
            awbPrintService.delete(awbPrintId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 删除分单
     *
     * @param awbPrintId
     * @return
     */
    @DeleteMapping("/hawb/{awbPrintId}")
    public MessageInfo deleteHawb(@PathVariable Integer awbPrintId) {
        try {
            awbPrintService.delete(awbPrintId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 主单下载
     *
     * @param afPAwbPrintProcedure
     * @return
     */
    @PostMapping("/downloadAWB")
    public MessageInfo awbDownloadWithPDF(@RequestBody AfPAwbPrintProcedure afPAwbPrintProcedure) {
        try {
            return MessageInfo.ok(awbPrintService.awbDownloadWithPDF(afPAwbPrintProcedure));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 分单下载
     *
     * @param afPAwbPrintProcedure
     * @param
     * @return
     */
    @PostMapping("/downloadHAWB")
    public MessageInfo hawbDownloadWithPDF(@RequestBody AfPAwbPrintProcedure afPAwbPrintProcedure) {
        try {
            return MessageInfo.ok(awbPrintService.hawbDownloadWithPDF(afPAwbPrintProcedure));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 发送运单 校验是否已发送
     * @date 20200827
     * @author limr
     * @return
     */
    @GetMapping("sendAmsCheckHasSend/{awbPrintId}")
    public MessageInfo shippingSendCheckHasSend(@PathVariable("awbPrintId") Integer awbPrintId) {
        try {
            return MessageInfo.ok(awbPrintService.sendAmsCheckHasSend(awbPrintId));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 发送运单 校验必填项
     *
     * @return
     */
    @PostMapping(value={"/sendAmsDataCheck/{type}/{awbNumber}","/sendAmsDataCheck/{type}/{awbNumber}/{letterId}"})
    public MessageInfo sendAmsDataCheck(
            @PathVariable("type") String type,
            @PathVariable("awbNumber") String awbNumber,
            @PathVariable(value = "letterId",required = false) String letterId) {
        try {
            String amsdata = awbPrintService.getAmsDataCheck(type, awbNumber, letterId);
            return MessageInfo.ok(amsdata);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
     * 发送运单 、
     * @author limr 20201117
     * @return
     */
    @PostMapping(value={"/sendAmsData/{type}/{awbNumber}","/sendAmsData/{type}/{awbNumber}/{letterId}"})
    public MessageInfo sendShippersData(
            @PathVariable("type") String type,
            @PathVariable("awbNumber") String awbNumber,
            @PathVariable(value = "letterId",required = false) String letterId) {
        try {
            Map<String, Object> sendCallbackData = awbPrintService.sendAmsData(type, awbNumber, letterId);
            return MessageInfo.ok(sendCallbackData);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }
}

