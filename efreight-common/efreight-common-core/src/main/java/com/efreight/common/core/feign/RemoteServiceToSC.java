package com.efreight.common.core.feign;

import com.efreight.common.remoteVo.*;
import com.efreight.common.security.constant.ServiceNameConstants;
import com.efreight.common.security.util.MessageInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.sound.midi.SoundbankResource;
import java.util.List;

@FeignClient(contextId = "remoteServiceToSC", value = ServiceNameConstants.SC_SERVICE)
public interface RemoteServiceToSC {
    /**
     * LC业务范畴
     */
    @GetMapping("/lcOrderFiles/{orderId}")
    MessageInfo<List<LcOrderFiles>> listLcOrderFiles(@PathVariable("orderId") Integer orderId);

    @GetMapping("/lcOrderFiles/getListByOrderFileIds/{orderFileIds}")
    MessageInfo<List<LcOrderFiles>> listLcOrderFilesByOrderFileIds(@PathVariable("orderFileIds") String orderFileIds);

    @PostMapping("/lcOrderFiles")
    MessageInfo saveLcOrderFiles(@RequestBody LcOrderFiles lcOrderFiles);

    @PutMapping("/lcOrderFiles")
    MessageInfo modifyLcOrderFiles(@RequestBody LcOrderFiles lcOrderFiles);

    @DeleteMapping("/lcOrderFiles/{orderFileId}")
    MessageInfo deleteLcOrderFiles(@PathVariable("orderFileId") Integer orderFileId);

    @GetMapping("/lcIncome/{orderId}")
    MessageInfo<List<LcIncome>> listLcIncome(@PathVariable("orderId") Integer orderId);

    @PostMapping("/lcIncome/saveOrderIncomeAndCost")
    MessageInfo saveOrderIncomeAndCost(@RequestBody IncomeCostList<LcIncome, LcCost> incomeCostList);

    @GetMapping("/lcIncome/view/{incomeId}")
    MessageInfo<LcIncome> viewLcIncome(@PathVariable("incomeId") Integer incomeId);

    @GetMapping("/lcCost/{orderId}")
    MessageInfo<List<LcCost>> listLcCost(@PathVariable("orderId") Integer orderId);

    @GetMapping("/lcOrder/{orderId}")
    MessageInfo<LcOrder> viewLcOrder(@PathVariable("orderId") Integer orderId);

    @PutMapping("/lcOrder/incomeComplete/{orderId}")
    MessageInfo incomeComplete(@PathVariable("orderId") Integer orderId);

    @PutMapping("/lcOrder/costComplete/{orderId}")
    MessageInfo costComplete(@PathVariable("orderId") Integer orderId);

    /**
     * VL业务范畴
     */
    @GetMapping("/vlOrderFiles/{orderId}")
    MessageInfo<List<VlOrderFiles>> listVLOrderFiles(@PathVariable("orderId") Integer orderId);

    @PostMapping("/vlOrderFiles")
    MessageInfo saveVLOrderFiles(@RequestBody VlOrderFiles vlOrderFiles);

    @PutMapping("/vlOrderFiles")
    MessageInfo modifyVLOrderFiles(@RequestBody VlOrderFiles vlOrderFiles);

    @DeleteMapping("/vlOrderFiles/{orderFileId}")
    MessageInfo deleteVLOrderFiles(@PathVariable("orderFileId") Integer orderFileId);

    /**
     * IO业务范畴
     */
    @GetMapping("/ioOrderFiles/{orderId}")
    MessageInfo<List<IoOrderFiles>> listIoOrderFiles(@PathVariable("orderId") Integer orderId);

    @GetMapping("/ioOrderFiles/getListByOrderFileIds/{orderFileIds}")
    MessageInfo<List<IoOrderFiles>> listIoOrderFilesByOrderFileIds(@PathVariable("orderFileIds") String orderFileIds);

    @PostMapping("/ioOrderFiles")
    MessageInfo saveIoOrderFiles(@RequestBody IoOrderFiles ioOrderFiles);

    @PutMapping("/ioOrderFiles")
    MessageInfo modifyIoOrderFiles(@RequestBody IoOrderFiles ioOrderFiles);

    @DeleteMapping("/ioOrderFiles/{orderFileId}")
    MessageInfo deleteIoOrderFiles(@PathVariable("orderFileId") Integer orderFileId);

    @GetMapping("/ioIncome/{orderId}")
    MessageInfo<List<IoIncome>> listIoIncome(@PathVariable("orderId") Integer orderId);

    @PostMapping("/ioIncome/saveOrderIncomeAndCost")
    MessageInfo saveIoOrderIncomeAndCost(@RequestBody IncomeCostList<IoIncome, IoCost> incomeCostList);

    @GetMapping("/ioIncome/view/{incomeId}")
    MessageInfo<IoIncome> viewIoIncome(@PathVariable("incomeId") Integer incomeId);

    @GetMapping("/ioCost/{orderId}")
    MessageInfo<List<IoCost>> listIoCost(@PathVariable("orderId") Integer orderId);

    @GetMapping("/ioOrder/{orderId}")
    MessageInfo<IoOrder> viewIoOrder(@PathVariable("orderId") Integer orderId);

    @PutMapping("/ioOrder/incomeComplete/{orderId}")
    MessageInfo ioIncomeComplete(@PathVariable("orderId") Integer orderId);

    @PutMapping("/ioOrder/costComplete/{orderId}")
    MessageInfo ioCostComplete(@PathVariable("orderId") Integer orderId);
}
