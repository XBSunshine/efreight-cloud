package com.efreight.afbase.controller;



import lombok.AllArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.efreight.afbase.service.SendService;
import com.efreight.common.core.exception.CheckedException;
import com.efreight.common.security.util.MessageInfo;

import java.util.Map;

/**
 * <p>
 * AF 延伸服务 成本 前端控制器
 * </p>
 *
 * @author qipm
 * @since 2019-10-12
 */
@RestController
@AllArgsConstructor
@RequestMapping("/send")
@Slf4j
public class SendController {

    private final SendService service;

    /**
     * 预录入
     */
    @PostMapping(value={"doEsdDecleare/{hasMwb}/{orderUUID}","doEsdDecleare/{hasMwb}/{orderUUID}/{letterIds}"})
    public MessageInfo doEsdDecleare(
                    @PathVariable("hasMwb") String hasMwb,
                    @PathVariable("orderUUID") String orderUUID,
                    @PathVariable(value = "letterIds",required = false) String letterIds) {
        try {
            Map<String, Object> sendCallbackData = service.doEsdDecleare(hasMwb, orderUUID, letterIds);
            return MessageInfo.ok(sendCallbackData);
        } catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
    }
    /**
     * 分单预录入
     */
    @PostMapping(value = "/doEsdDecleareFHL/{orderUUID}/{letterIds}")
    public MessageInfo doEsdDecleareFHL(
                    @PathVariable("orderUUID") String orderUUID,
                    @PathVariable(value = "letterIds",required = false) String letterIds) {
        try {
            Map<String, Object> sendCallbackData = service.doEsdDecleareFHL(orderUUID, letterIds);
            return MessageInfo.ok(sendCallbackData);
        } catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
    }
    /**
     * 删除预录入
     */
    @PostMapping(value = "/doEawbPreDelete/{AWBNumber}/{orderUUID}")
    public MessageInfo doEawbPreDelete(@PathVariable("AWBNumber") String AWBNumber,@PathVariable("orderUUID") String orderUUID) {
    	try {
    		Map<String, Object> sendCallbackData = service.doEawbPreDelete(AWBNumber,orderUUID);
    		return MessageInfo.ok(sendCallbackData);
    	} catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
    }
    /**
     * 删除主单预录入
     */
    @PostMapping(value = "/doEawbPreDeleteAwb/{AWBNumber}/{orderCode}")
    public MessageInfo doEawbPreDeleteAwb(@PathVariable("AWBNumber") String AWBNumber,@PathVariable("orderCode") String orderCode) {
    	try {
    		Map<String, Object> sendCallbackData = service.doEawbPreDeleteAwb(AWBNumber,orderCode);
    		return MessageInfo.ok(sendCallbackData);
    	} catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
    }
    /**
     * 删除分单预录入
     */
    @PostMapping(value = "/doEawbPreDeleteFHL/{AWBNumber}/{HWBNumber}/{orderCode}")
    public MessageInfo doEawbPreDeleteFHL(@PathVariable("AWBNumber") String AWBNumber,@PathVariable("HWBNumber") String HWBNumber,@PathVariable("orderCode") String orderCode) {
    	try {
    		Map<String, Object> sendCallbackData = service.doEawbPreDeleteFHL(AWBNumber,HWBNumber,orderCode);
    		return MessageInfo.ok(sendCallbackData);
    	} catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
    }
    /**
     * 入库
     */
    @PostMapping(value={"doEAWB_WH/{hasMwb}/{orderUUID}","doEAWB_WH/{hasMwb}/{orderUUID}/{letterIds}"})
    public MessageInfo doEAWB_WH(
                    @PathVariable("hasMwb") String hasMwb,
                    @PathVariable("orderUUID") String orderUUID,
                    @PathVariable(value = "letterIds",required = false) String letterIds) {
        try {
            Map<String, Object> sendCallbackData = service.doEAWB_WH(hasMwb, orderUUID, letterIds);
            return MessageInfo.ok(sendCallbackData);
        } catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
    }
    /**
     * 一键申报
     */
    @PostMapping(value={"doOneDecleare_ForSend/{hasMwb}/{orderUUID}","doOneDecleare_ForSend/{hasMwb}/{orderUUID}/{letterIds}"})
    public MessageInfo doOneDecleare_ForSend(
                    @PathVariable("hasMwb") String hasMwb,
                    @PathVariable("orderUUID") String orderUUID,
                    @PathVariable(value = "letterIds",required = false) String letterIds) {
        try {
            Map<String, Object> sendCallbackData = service.doOneDecleare_ForSend(hasMwb, orderUUID, letterIds);
            return MessageInfo.ok(sendCallbackData);
        } catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
    }
    /**
     * 预配舱单
     */
    @PostMapping(value={"doMft2201_Decleare/{hasMwb}/{orderUUID}","doMft2201_Decleare/{hasMwb}/{orderUUID}/{letterIds}"})
    public MessageInfo doMft2201_Decleare(
                    @PathVariable("hasMwb") String hasMwb,
                    @PathVariable("orderUUID") String orderUUID,
                    @PathVariable(value = "letterIds",required = false) String letterIds) {
        try {
            Map<String, Object> sendCallbackData = service.doMft2201_Decleare(hasMwb, orderUUID, letterIds);
            return MessageInfo.ok(sendCallbackData);
        } catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
    }
    /**
     * 电子运单
     */
    @PostMapping(value={"doEAWB_AMS/{hasMwb}/{orderUUID}","doEAWB_AMS/{hasMwb}/{orderUUID}/{letterIds}"})
    public MessageInfo doEAWB_AMS(
                    @PathVariable("hasMwb") String hasMwb,
                    @PathVariable("orderUUID") String orderUUID,
                    @PathVariable(value = "letterIds",required = false) String letterIds) {
        try {
            Map<String, Object> sendCallbackData = service.doEAWB_AMS(hasMwb, orderUUID, letterIds);
            return MessageInfo.ok(sendCallbackData);
        } catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
    }
    @PostMapping(value="doSendGoodsName/{orderId}/{orderUUID}/{awbNumber}")
    public MessageInfo doSendGoodsName(@PathVariable("orderId") Integer orderId,@PathVariable("orderUUID") String orderUUID,@PathVariable("awbNumber") String awbNumber) {
    	try {
    		Map<String, Object> sendCallbackData = service.doSendGoodsName(orderId, orderUUID,awbNumber);
    		return MessageInfo.ok(sendCallbackData);
    	} catch (CheckedException e){
    		log.error("异常："+ e.getMessage());
    		return MessageInfo.failed(e.getMessage());
    	}catch (Exception e){
    		log.error("系统出现异常："+ e.getMessage());
    		return MessageInfo.failed("系统出现异常，请联系管理员！");
    	}
    }
}

