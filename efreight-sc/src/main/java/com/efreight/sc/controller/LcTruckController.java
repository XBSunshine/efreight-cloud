package com.efreight.sc.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.entity.LcTruck;
import com.efreight.sc.entity.view.LcTruckExcel;
import com.efreight.sc.service.LcTruckService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * LC  车辆管理 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-31
 */
@RestController
@RequestMapping("/lcTruck")
@Slf4j
@AllArgsConstructor
public class LcTruckController {

    private final LcTruckService lcTruckService;


    /**
     * 获取列表
     * @return
     */
    @GetMapping
    public MessageInfo list(){
        try {
            List<LcTruck> list = lcTruckService.getList();
            return MessageInfo.ok(list);
        }catch (Exception e){
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 车辆新增
     * @param lcTruck
     * @return
     */
    @PostMapping
    public MessageInfo save(@RequestBody LcTruck lcTruck){
        try {
            lcTruckService.insert(lcTruck);
            return MessageInfo.ok();
        }catch (Exception e){
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 数据保存
     * @param lcTruck
     * @return
     */
    @PostMapping("saveLcTruck")
    public MessageInfo saveLcTruck(@RequestBody LcTruck lcTruck){
        try {
            lcTruckService.saveLcTruck(lcTruck);
            return MessageInfo.ok();
        }catch (Exception e){
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查询列表
     * @param page
     * @param lcTruck
     * @return
     */
    @GetMapping("listPage")
    public MessageInfo listPage(Page page, LcTruck lcTruck){
        try{
            lcTruck.setOrgId(SecurityUtils.getUser().getOrgId());
            IPage result = lcTruckService.getPage(page, lcTruck);
            return MessageInfo.ok(result);
        }catch (Exception e){
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 删除信息
     * @param truckId
     * @return
     */
    @DeleteMapping("{truckId}")
    public MessageInfo delete(@PathVariable("truckId") Integer truckId){
        try{
            int result = lcTruckService.delete(truckId);
            return MessageInfo.ok(result);
        }catch (Exception e){
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 修改数据
     * @param lcTruck
     * @return
     */
    @PostMapping("update")
    public MessageInfo update(@RequestBody LcTruck lcTruck){
        try{
            int result = lcTruckService.update(lcTruck);
            return MessageInfo.ok(result);
        }catch (Exception e){
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 数据导出
     * @param lcTruck
     * @param response
     */
    @PostMapping("export")
    public void export(LcTruck lcTruck, HttpServletResponse response){
        lcTruck.setOrgId(SecurityUtils.getUser().getOrgId());
        List<LcTruckExcel> lcTruckList = lcTruckService.queryListForExcel(lcTruck);
        String[] excelTitles = new String[]{"车牌号", "车长(米)", "吨位", "限重(KG)", "最大体积(CBM)", "司机姓名", "司机电话", "操作人", "操作时间"};
        new ExportExcel<LcTruckExcel>().exportExcel(response, "车辆信息", excelTitles, lcTruckList, "陆运车辆信息表");
    }
}

