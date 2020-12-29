package com.efreight.afbase.controller;


import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.OrderInquiry;
import com.efreight.afbase.service.OrderInquiryService;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.core.utils.FieldValUtils;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * AF 询价单 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-05-26
 */
@RestController
@RequestMapping("/orderInquiry")
@AllArgsConstructor
@Slf4j
public class OrderInquiryController {

    private final OrderInquiryService orderInquiryService;

    /**
     * 分页查询
     *
     * @param page
     * @param orderInquiry
     * @return
     */
    @GetMapping
    public MessageInfo page(Page page, OrderInquiry orderInquiry) {
        try {
            IPage result = orderInquiryService.getPage(page, orderInquiry);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查看详情
     *
     * @param orderInquiryId
     * @return
     */
    @GetMapping("/{orderInquiryId}")
    public MessageInfo view(@PathVariable("orderInquiryId") Integer orderInquiryId) {
        try {
            OrderInquiry orderInquiry = orderInquiryService.view(orderInquiryId);
            return MessageInfo.ok(orderInquiry);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 新增
     *
     * @param orderInquiry
     * @return
     */
    @PostMapping
    public MessageInfo save(@RequestBody OrderInquiry orderInquiry) {
        try {
            Integer orderInquiryId = orderInquiryService.insert(orderInquiry);
            return MessageInfo.ok(orderInquiryId);
        } catch (Exception e) {
            log.info(e.getMessage());
            if (e.getMessage().contains("Out of range value")) {
                return MessageInfo.failed("字段长度太大，请核实");
            } else {
                return MessageInfo.failed(e.getMessage());
            }
        }
    }

    /**
     * 修改
     *
     * @param orderInquiry
     * @return
     */
    @PutMapping
    public MessageInfo modify(@RequestBody OrderInquiry orderInquiry) {
        try {
            orderInquiryService.modify(orderInquiry);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            if (e.getMessage().contains("Out of range value")) {
                return MessageInfo.failed("字段长度太大，请核实");
            } else {
                return MessageInfo.failed(e.getMessage());
            }
        }
    }

    /**
     * 关闭
     *
     * @param orderInquiryId
     * @return
     */
    @PutMapping("/stop/{orderInquiryId}")
    public MessageInfo stop(@PathVariable("orderInquiryId") Integer orderInquiryId) {
        try {
            orderInquiryService.stop(orderInquiryId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 通过询价代理查看联系人
     *
     * @param inquryAgentIds
     * @return
     */
    @GetMapping("/getInquryAgentContactList/{inquryAgentIds}")
    public MessageInfo getInquryAgentContactList(@PathVariable("inquryAgentIds") String inquryAgentIds) {
        try {
            List<Map<String, String>> result = orderInquiryService.getInquryAgentContactList(inquryAgentIds);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 发送二维码
     *
     * @param param
     * @return
     */
    @PostMapping("/sendQrcode")
    public MessageInfo sendQrcode(@RequestBody Map<String, Object> param) {
        try {
            orderInquiryService.sendQrcode(param);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }


    /**
     * 导出Excel
     *
     * @param response
     * @param bean
     * @throws IOException
     */
    @PostMapping("/exportExcel")
    public void exportExcel(HttpServletResponse response, @ModelAttribute("bean") OrderInquiry bean) throws IOException {
        //自定义字段
        List<OrderInquiry> list = orderInquiryService.exportExcel(bean);
        if (!StringUtils.isEmpty(bean.getColumnStrs())) {
            List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(bean.getColumnStrs());
            String[] headers = new String[jsonArr.size()];
            String[] colunmStrs = new String[jsonArr.size()];

            //生成表头跟字段
            if (jsonArr != null && jsonArr.size() > 0) {
                for (int i = 0; i < jsonArr.size(); i++) {
                    JSONObject job = jsonArr.getJSONObject(i);
                    headers[i] = job.getString("label");
                    colunmStrs[i] = job.getString("prop");
                }
            }
            //遍历结果集 区相应字段封装 linkedHashMap后存储list发往工具类
            if (list != null && list.size() > 0) {
                for (OrderInquiry orderInquiry : list) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], orderInquiry));
                    }
                    listExcel.add(map);
                }
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");

        }
    }

    /**
     * 通过始发港 查询匹配询盘代理
     *
     * @param dep
     * @return
     */
    @GetMapping("/getInquryAgentDepList/{dep}")
    public MessageInfo getInquryAgentDepList(@PathVariable("dep") String dep) {
        try {
            Map result = orderInquiryService.getInquryAgentDepList(dep);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 询报价单导出-Excel版
     *
     * @param orderInquiryId
     * @return
     */
    @PostMapping("/exportInquiryQuotationExcel/{orderInquiryId}")
    public void exportInquiryQuotationExcel(@PathVariable("orderInquiryId") Integer orderInquiryId) {
        try {
            orderInquiryService.exportInquiryQuotationExcel(orderInquiryId);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 询价时创建是四友舱信息数据信息
     *
     * @return MessageInfo
     */
    @PostMapping("/createFourYCWhenInquiry")
    public MessageInfo createFourYCWhenInquiry() {
        try {
            orderInquiryService.createFourYCWhenInquiry();
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 询价时查询当前签约公司是否已保存过四个友舱数据信息
     *
     * @return MessageInfo
     */
    @GetMapping("/getFourYCWhenInquiry")
    public MessageInfo getFourYCWhenInquiry() {
        try {
            Boolean result = orderInquiryService.getFourYCWhenInquiry();
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }


}

