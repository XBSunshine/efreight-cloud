package com.efreight.hrs.controller;

import com.efreight.common.security.util.MessageInfo;
import com.efreight.hrs.entity.TomHomeApplication;
import com.efreight.hrs.service.TomHomeApplicaionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/home")
@Slf4j
public class TomHomeApplicationController {

    private final TomHomeApplicaionService tomHomeApplicaionService;

    /**
     * 首页展示我的常用
     * @return
     */
    @GetMapping
    public MessageInfo getList(String type) {
        try {
            List<TomHomeApplication> list = tomHomeApplicaionService.queryList(type);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    } /**
     * 首页展示选择的菜单
     * @return
     */
    @GetMapping("/check")
    public MessageInfo getCheckList() {
        try {
            List<Integer> list = tomHomeApplicaionService.getCheckList();
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 设置首页我的常用
     * @param map
     * @return
     */
    @PostMapping
    public MessageInfo save(@RequestBody Map<String,String> map){
        try {
            tomHomeApplicaionService.save(map.get("applicationIds"));
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}
