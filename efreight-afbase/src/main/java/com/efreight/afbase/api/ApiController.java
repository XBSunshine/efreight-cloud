package com.efreight.afbase.api;

import com.efreight.afbase.service.AirportService;
import com.efreight.afbase.service.TariffDetailsService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 外部接口
 * @Date 20210312
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api")
@Slf4j
public class ApiController {
    private final TariffDetailsService tdService;
    private final AirportService apService;

    /**
     * HS编码接口
     * @param key 关键字
     */
    @GetMapping("/hsList/{key}")
    public MessageInfo hsList(@PathVariable("key") String key) {
        try{
           return MessageInfo.ok(tdService.getListForApi(key));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
     * 机场三字码
     *
     */
    @GetMapping("/airportList")
    public MessageInfo airportList(@RequestParam(value="enKey",required = false) String enKey,
                                   @RequestParam(value="cnKey",required = false) String cnKey) {
        try{
            return MessageInfo.ok(apService.airportListForApi(enKey,cnKey));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }
}
