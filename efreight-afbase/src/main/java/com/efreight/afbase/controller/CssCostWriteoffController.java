package com.efreight.afbase.controller;


import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.CssCostWriteoff;
import com.efreight.afbase.entity.CssCostWriteoffDetail;
import com.efreight.afbase.entity.CssPayment;
import com.efreight.afbase.service.CssCostWriteoffService;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FieldValUtils;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * <p>
 * CSS 应付：核销 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-14
 */
@RestController
@RequestMapping("/cssCostWriteoff")
@AllArgsConstructor
@Slf4j
public class CssCostWriteoffController {

    private final CssCostWriteoffService cssCostWriteoffService;

    /**
     * 自动匹配结果
     *
     * @param paymentId
     * @param amountWriteoff
     * @return
     */
    @GetMapping("/automatch/{paymentId}/{amountWriteoff}")
    public MessageInfo automatch(@PathVariable("paymentId") Integer paymentId, @PathVariable("amountWriteoff") BigDecimal amountWriteoff) {
        try {
            List<CssCostWriteoffDetail> result = cssCostWriteoffService.automatch(paymentId, amountWriteoff);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 保存核销
     *
     * @param cssCostWriteoff
     * @return
     */
    @PostMapping
    public MessageInfo save(@RequestBody CssCostWriteoff cssCostWriteoff) {
        try {
            cssCostWriteoffService.insert(cssCostWriteoff);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping("/getVoucherDate")
    public MessageInfo getVoucherDate(Integer costWriteoffId) {
        return MessageInfo.ok(cssCostWriteoffService.getVoucherDate(costWriteoffId));
    }

    /**
     * 删除核销
     *
     * @param costWriteoffId
     * @return
     */
    @DeleteMapping("/{costWriteoffId}")
    public MessageInfo delete(@PathVariable("costWriteoffId") Integer costWriteoffId) {
        try {
            cssCostWriteoffService.delete(costWriteoffId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查看详情
     *
     * @param costWriteoffId
     * @return
     */
    @GetMapping("/{costWriteoffId}")
    public MessageInfo view(@PathVariable("costWriteoffId") Integer costWriteoffId) {
        try {
            CssCostWriteoff cssCostWriteoff = cssCostWriteoffService.view(costWriteoffId);
            return MessageInfo.ok(cssCostWriteoff);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 分页列表查询
     *
     * @param page
     * @param cssCostWriteoff
     * @return
     */
    @GetMapping
    public MessageInfo getPage(Page page, CssCostWriteoff cssCostWriteoff) {
        try {
            IPage result = cssCostWriteoffService.getPage(page, cssCostWriteoff);
            return MessageInfo.ok(result);
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
    @PostMapping("/exportWriteoffExcel")
    public void exportWriteoffExcel(HttpServletResponse response, @ModelAttribute("bean") CssCostWriteoff bean) throws IOException {
        //自定义字段
        List<CssCostWriteoff> list = cssCostWriteoffService.exportWriteoffExcel(bean);
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
                for (CssCostWriteoff cssCostWriteoff : list) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        if("createTime".equals(colunmStrs[j])){
                            if (cssCostWriteoff.getCreateTime() != null) {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                map.put("createTime", formatter.format(cssCostWriteoff.getCreateTime()));
                            } else {
                                map.put("createTime", "");
                            }
                        }else if("creatorName".equals(colunmStrs[j])){
                            if(!StringUtils.isEmpty(cssCostWriteoff.getCreatorName())) {
                                map.put("creatorName", cssCostWriteoff.getCreatorName().split(" ")[0]);
                            } else {
                                map.put("creatorName", "");
                            }
                        }else{
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], cssCostWriteoff));
                        }
                    }
                    listExcel.add(map);
                }
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");

        }
    }
}

