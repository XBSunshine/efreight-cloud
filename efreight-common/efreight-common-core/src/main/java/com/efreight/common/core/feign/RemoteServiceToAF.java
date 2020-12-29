package com.efreight.common.core.feign;

import com.efreight.common.remoteVo.*;
import com.efreight.common.security.constant.ServiceNameConstants;
import com.efreight.common.security.vo.CurrencyRateVo;
import com.efreight.common.security.util.MessageInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(contextId = "remoteServiceToAF", value = ServiceNameConstants.AF_BASE_SERVICE)
public interface RemoteServiceToAF {

    @PostMapping("/currencyrate/remoteSave")
    MessageInfo createCurrencyRate(@RequestBody CurrencyRateVo currencyRateVo);

    @GetMapping("/currency")
    MessageInfo<List<CurrencyRateVo>> getCurrentListByOrgId();

    @GetMapping("/aforder/view/{id}")
    MessageInfo<AfOrder> getAfOrderById(@PathVariable("id") Integer id);

    @PostMapping("/aforder/getOrderListForVL")
    MessageInfo<List<OrderForVL>> getAFOrderListForVL(@RequestBody OrderForVL orderForVL);

    @GetMapping("/airport/city/{cityCode}")
    MessageInfo<Airport> viewAirportCity(@PathVariable("cityCode") String cityCode);

    @GetMapping("/service/queryListForVL/{businessScope}")
    MessageInfo<List<Service>> queryServiceListForVL(@PathVariable("businessScope") String businessScope);

    @PostMapping("/orderFiles/doBatchSaveForAF")
    MessageInfo doOrderFilesBatchSaveForAF(@RequestBody List<AfOrderFiles> orderFilesList);
}
