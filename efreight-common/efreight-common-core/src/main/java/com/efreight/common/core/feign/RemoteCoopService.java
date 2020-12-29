package com.efreight.common.core.feign;

import com.efreight.common.security.constant.ServiceNameConstants;
import com.efreight.common.security.vo.CoopVo;
import com.efreight.common.security.util.MessageInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(contextId = "remoteCoopService", value = ServiceNameConstants.PRM_SERVICE)
public interface RemoteCoopService {
    @GetMapping("/coop/{coopId}")
    MessageInfo<CoopVo> viewCoop(@PathVariable("coopId") String coopId);

    @GetMapping("/coop/listByType/{coopType}")
    MessageInfo<List<CoopVo>> listByType(@PathVariable("coopType") String coopType);

    @GetMapping("/coop/listByCoopName/{coopName}")
    MessageInfo<List<CoopVo>> listByCoopName(@PathVariable("coopName") String coopName);

    @GetMapping("/coop/getCoopCountByCode/{orgId}/{coopCode}")
    MessageInfo<CoopVo> getCoopCountByCode(@PathVariable("orgId") Integer orgId,@PathVariable("coopCode") String coopCode);

    @GetMapping("/coop/getCoopCountByName/{orgId}/{coopName}")
    MessageInfo<CoopVo> getCoopCountByName(@PathVariable("orgId") Integer orgId,@PathVariable("coopName") String coopName);

    @PostMapping("/coop/remoteSaveCoop")
    MessageInfo<Integer> remoteSaveCoop(@RequestBody CoopVo coopVo);

    @GetMapping("/coop/selectPrmCoopsForAwb/{orgId}/{businessScope}")
    MessageInfo<List<CoopVo>> selectPrmCoopsForAwb(@PathVariable("orgId")Integer orgId, @PathVariable("businessScope") String businessScope);
}
