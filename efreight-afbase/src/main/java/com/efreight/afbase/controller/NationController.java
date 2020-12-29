package com.efreight.afbase.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.Carrier;
import com.efreight.afbase.entity.Nation;
import com.efreight.afbase.service.NationService;
import com.efreight.common.security.util.MessageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;

import com.efreight.afbase.service.CategoryService;

import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
@RestController
@AllArgsConstructor
@RequestMapping("/nation")
@Slf4j
public class NationController {

    private final NationService nationService;

    @GetMapping
    public MessageInfo page(Page page, Nation nation) {
        try {
            IPage ipage = nationService.queryPage(page, nation);
            return MessageInfo.ok(ipage);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping("/view/{id}")
    public MessageInfo view(@PathVariable Integer id) {

        try {
            Nation nation = nationService.queryOne(id);
            return MessageInfo.ok(nation);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 数据导入
     *
     * @param
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/importData", method = RequestMethod.POST)
    @ResponseBody
    public MessageInfo importUserData(MultipartFile file) throws IOException {
        InputStream input = null;
        try {
            input = file.getInputStream();
            String fileAllName = file.getOriginalFilename();
            Workbook wb = null;
            if (fileAllName.endsWith(".xlsx")) {
                wb = new XSSFWorkbook(input);
            } else if (fileAllName.endsWith(".xls")) {
                wb = new HSSFWorkbook(input);
            } else {
                throw new RuntimeException("导入文件格式有误！");
            }
            //获得第一个表单
            Sheet sheet = wb.getSheetAt(0);
            int minRowIx = sheet.getFirstRowNum() + 1;
            int maxRowIx = sheet.getLastRowNum();
            List<Nation> list = new ArrayList<Nation>();
            DecimalFormat df = new DecimalFormat("#");
            for (int rowIx = minRowIx; rowIx <= maxRowIx; rowIx++) {

                Row row = sheet.getRow(rowIx);
                //国家代码
                String nationCode = "";
                if (row.getCell(0).getCellType() != CellType.BLANK) {
                    if (row.getCell(0).getCellType() == CellType.STRING) {
                        nationCode = row.getCell(0).getStringCellValue();
                    } else if (row.getCell(0).getCellType() == CellType.NUMERIC) {
                        nationCode = df.format(row.getCell(0).getNumericCellValue());
                    }
                }
                if (StrUtil.isBlank(nationCode)) {
                    throw new RuntimeException("第" + (rowIx + 1) + "行国家代码为空");
                }
                //国家中文名称

                String nationName = "";
                if (row.getCell(1).getCellType() != CellType.BLANK) {
                    if (row.getCell(1).getCellType() == CellType.STRING) {
                        nationName = row.getCell(1).getStringCellValue();
                    } else if (row.getCell(1).getCellType() == CellType.NUMERIC) {
                        nationName = df.format(row.getCell(1).getNumericCellValue());
                    }
                }
                if (StrUtil.isBlank(nationName)) {
                    throw new RuntimeException("第" + (rowIx + 1) + "行国家中文名称为空");
                }
                //国家英文名称
                String nationEname = "";
                if (row.getCell(2).getCellType() != CellType.BLANK) {
                    if (row.getCell(2).getCellType() == CellType.STRING) {
                        nationEname = row.getCell(2).getStringCellValue();
                    } else if (row.getCell(2).getCellType() == CellType.NUMERIC) {
                        nationEname = df.format(row.getCell(2).getNumericCellValue());
                    }
                }
                if (StrUtil.isBlank(nationEname)) {
                    throw new RuntimeException("第" + (rowIx + 1) + "行国家英文名称为空");
                }
                //洲

                String continent = "";
                if (row.getCell(3).getCellType() != CellType.BLANK) {
                    if (row.getCell(3).getCellType() == CellType.STRING) {
                        continent = row.getCell(3).getStringCellValue();
                    } else if (row.getCell(3).getCellType() == CellType.NUMERIC) {
                        continent = df.format(row.getCell(3).getNumericCellValue());
                    }
                }

                if (StrUtil.isBlank(continent)) {
                    throw new RuntimeException("第" + (rowIx + 1) + "行洲为空");
                }
                Nation bean = new Nation();
                bean.setNationCode(nationCode);
                bean.setNationContinent(continent);
                bean.setNationEname(nationEname);
                bean.setNationName(nationName);
                bean.setNationStatus(true);
                list.add(bean);
            }
            nationService.importData(list);
            return MessageInfo.ok();
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        } finally {
            //关闭
            input.close();
        }
    }
}

