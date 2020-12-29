package com.efreight.common.core.utils;

import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

public class PoiUtils {


    public static void copyCell(Workbook wb, XSSFDrawing patriarch, Cell fromCell, Cell toCell) {
        CellStyle newStyle = wb.createCellStyle();
        CellStyle srcStyle = fromCell.getCellStyle();
        newStyle.cloneStyleFrom(srcStyle);
        //样式
        toCell.setCellStyle(newStyle);
        //评论
        if (fromCell.getCellComment() != null) {
            toCell.setCellComment(fromCell.getCellComment());
        }
        //注释复制（暂时不用）
//        copyComment(toCell, fromCell, patriarch);
        // 不同数据类型处理
        CellType srcCellType = fromCell.getCellType();
        toCell.setCellType(srcCellType);
        if (srcCellType == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(fromCell)) {
                toCell.setCellValue(fromCell.getDateCellValue());
            } else {
                toCell.setCellValue(fromCell.getNumericCellValue());
            }
        } else if (srcCellType == CellType.STRING) {
            toCell.setCellValue(fromCell.getRichStringCellValue());
        } else if (srcCellType == CellType.BLANK) {

        } else if (srcCellType == CellType.BOOLEAN) {
            toCell.setCellValue(fromCell.getBooleanCellValue());
        } else if (srcCellType == CellType.ERROR) {
            toCell.setCellErrorValue(fromCell.getErrorCellValue());
        } else if (srcCellType == CellType.FORMULA) {
            toCell.setCellFormula(fromCell.getCellFormula());
        } else {

        }

    }

    public static void copyRow(Workbook wb, XSSFDrawing patriarch, XSSFRow fromRow, XSSFRow toRow) {
        toRow.setHeight(fromRow.getHeight());
        for (Iterator cellIt = fromRow.cellIterator(); cellIt.hasNext(); ) {
            Cell tmpCell = (Cell) cellIt.next();
            Cell newCell = toRow.createCell(tmpCell.getColumnIndex());
            copyCell(wb, patriarch, tmpCell, newCell);
        }
        Sheet worksheet = fromRow.getSheet();
        Sheet toRowSheet = toRow.getSheet();
        for (int i = 0; i < worksheet.getNumMergedRegions(); i++) {
            CellRangeAddress cellRangeAddress = worksheet.getMergedRegion(i);
            if (cellRangeAddress.getFirstRow() == fromRow.getRowNum()) {
                CellRangeAddress newCellRangeAddress = new CellRangeAddress(toRow.getRowNum(), (toRow.getRowNum() +
                        (cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow())), cellRangeAddress
                        .getFirstColumn(), cellRangeAddress.getLastColumn());
                toRowSheet.addMergedRegionUnsafe(newCellRangeAddress);
            }
        }
    }

    public static void copyComment(Cell targetCell, Cell sourceCell, XSSFDrawing targetPatriarch) {
        if (targetCell == null || sourceCell == null || targetPatriarch == null) {
            throw new IllegalArgumentException("调用PoiUtil.copyCommentr()方法时，targetCell、sourceCell、targetPatriarch都不能为空，故抛出该异常！");
        }

        //处理单元格注释
        XSSFComment comment = ((XSSFCell) sourceCell).getCellComment();
        if (comment != null) {
            XSSFComment newComment = targetPatriarch.createCellComment(new XSSFClientAnchor());
            newComment.setAuthor(comment.getAuthor());
            newComment.setColumn(comment.getColumn());
            newComment.setRow(comment.getRow());
            newComment.setString(comment.getString());
            newComment.setVisible(comment.isVisible());
            newComment.setString(comment.getString());
            targetCell.setCellComment(newComment);
        }
    }

    /**
     * 获取图片和位置 (xlsx)
     */
    public static List<Map<String, Object>> getPicturesFromXSSFSheet(XSSFSheet sheet) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        List<POIXMLDocumentPart> list = sheet.getRelations();
        for (POIXMLDocumentPart part : list) {
            if (part instanceof XSSFDrawing) {
                XSSFDrawing drawing = (XSSFDrawing) part;
                List<XSSFShape> shapes = drawing.getShapes();
                for (XSSFShape shape : shapes) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    XSSFPicture picture = (XSSFPicture) shape;
                    XSSFClientAnchor anchor = picture.getPreferredSize();
                    map.put("pictureAnchor", anchor);
                    map.put("pictureByteArray", picture.getPictureData().getData());
                    map.put("pictureType", picture.getPictureData().getPictureType());
                    result.add(map);
                }
            }
        }
        return result;
    }

    public static void copyPicture(XSSFWorkbook workbook, XSSFSheet sourceSheet, XSSFSheet targetSheet) {
        XSSFDrawing drawing = targetSheet.createDrawingPatriarch();
        List<Map<String, Object>> list = getPicturesFromXSSFSheet(sourceSheet);
        list.stream().forEach(map -> {
            drawing.createPicture((XSSFClientAnchor) map.get("pictureAnchor"),
                    workbook.addPicture((byte[]) map.get("pictureByteArray"),
                            Integer.parseInt(map.get("pictureType").toString())));
        });
    }

    @SneakyThrows
    public static void multiplySheetForStatement(String newFileName, String fileName, int statementAmount, String orgLogo, String orgSeal) {
        XSSFWorkbook fromExcel = new XSSFWorkbook(new FileInputStream(fileName));
        int length = fromExcel.getNumberOfSheets();
        for (int i = 2; i < length; i++) {// 遍历每个sheet
            XSSFSheet sheet = fromExcel.getSheetAt(i);
            sheet.setDisplayGridlines(false);
            XSSFPrintSetup printSetup = sheet.getPrintSetup();
            printSetup.setScale((short) 80);
            printSetup.setTopMargin(0.73);
            printSetup.setBottomMargin(0.73);
            printSetup.setLeftMargin(0.73);
            printSetup.setRightMargin(0.73);
        }
        fromExcel.setSheetHidden(1, true);
//        if (statementAmount > 0) {
//            //合并文件图片处理
//            XSSFDrawing drawingPatriarch = secondSheetNew.createDrawingPatriarch();
//            if (StrUtil.isNotBlank(orgLogo)) {
//                ByteArrayOutputStream byteArrayOutOrgLogo = new ByteArrayOutputStream();
//                BufferedImage bufferImg = ImageIO.read(new File(orgLogo));
//                if (orgLogo.endsWith("jpg")) {
//                    ImageIO.write(bufferImg, "jpg", byteArrayOutOrgLogo);
//                    for (int i = 0; i < statementAmount; i++) {
//                        XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 1023, 255, (short) 0, i * 55 + 1, (short) 15, i * 55 + 1);
//                        drawingPatriarch.createPicture(anchor, fromExcel.addPicture(byteArrayOutOrgLogo.toByteArray(), XSSFWorkbook.PICTURE_TYPE_JPEG));
//                    }
//                } else if (orgLogo.endsWith("png")) {
//                    ImageIO.write(bufferImg, "png", byteArrayOutOrgLogo);
//                    for (int i = 0; i < statementAmount; i++) {
//                        XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 1023, 255, (short) 0, i * 55 + 1, (short) 15, i * 55 + 2);
//                        drawingPatriarch.createPicture(anchor, fromExcel.addPicture(byteArrayOutOrgLogo.toByteArray(), XSSFWorkbook.PICTURE_TYPE_PNG));
//                    }
//                }
//            }
//
//            if (StrUtil.isNotBlank(orgSeal)) {
//                ByteArrayOutputStream byteArrayOutOrgSeal = new ByteArrayOutputStream();
//                BufferedImage bufferImg = ImageIO.read(new File(orgSeal));
//                if (orgSeal.endsWith("jpg")) {
//                    ImageIO.write(bufferImg, "jpg", byteArrayOutOrgSeal);
//                    for (int i = 0; i < statementAmount; i++) {
//                        XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 1023, 255, (short) 40, i * 55 + 44, (short) 52, i * 55 + 50);
//                        drawingPatriarch.createPicture(anchor, fromExcel.addPicture(byteArrayOutOrgSeal.toByteArray(), XSSFWorkbook.PICTURE_TYPE_JPEG));
//                    }
//                } else if (orgSeal.endsWith("png")) {
//                    ImageIO.write(bufferImg, "png", byteArrayOutOrgSeal);
//                    for (int i = 0; i < statementAmount; i++) {
//                        XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 1023, 255, (short) 40, i * 55 + 44, (short) 52, i * 55 + 50);
//                        drawingPatriarch.createPicture(anchor, fromExcel.addPicture(byteArrayOutOrgSeal.toByteArray(), XSSFWorkbook.PICTURE_TYPE_PNG));
//                    }
//                }
//            }
//        }

        String path = newFileName.substring(0, newFileName.lastIndexOf("/"));
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        FileOutputStream fileOut = new FileOutputStream(newFileName);
        fromExcel.write(fileOut);
        fileOut.flush();
        fileOut.close();
    }

    /**
     * af 交货托书打印
     *
     * @param lastPath
     * @param lastShipperFilePath
     * @param lastSecurityNotePath
     */
    @SneakyThrows
    public static void multiplySheetForShipperLetterPrint(String lastPath, String lastShipperFilePath, String lastSecurityNotePath) {
        XSSFWorkbook fromExcelShipper = new XSSFWorkbook(new FileInputStream(lastShipperFilePath));
        XSSFWorkbook fromExcelSecurityNote = new XSSFWorkbook(new FileInputStream(lastSecurityNotePath));
        for (int i = 0; i < fromExcelSecurityNote.getNumberOfSheets(); i++) {
            XSSFSheet fromSheet = fromExcelSecurityNote.getSheetAt(i);
            XSSFSheet toSheet = fromExcelShipper.createSheet(fromSheet.getSheetName());
            XSSFDrawing patriarch = toSheet.createDrawingPatriarch();
            for (Iterator rowIt = fromSheet.rowIterator(); rowIt.hasNext(); ) {
                XSSFRow oldRow = (XSSFRow) rowIt.next();
                XSSFRow newRow = toSheet.createRow(oldRow.getRowNum());
                copyRow(fromExcelShipper, patriarch, oldRow, newRow);
            }
            copyPicture(fromExcelShipper, fromSheet, toSheet);
            toSheet.setDisplayGridlines(false);
            for (int j = 0; j <= 65; j++) {
                toSheet.setColumnWidth(j, 416);
            }
//            XSSFPrintSetup printSetup = sheet.getPrintSetup();
//            printSetup.setScale((short) 80);
//            printSetup.setTopMargin(0.73);
//            printSetup.setBottomMargin(0.73);
//            printSetup.setLeftMargin(0.73);
//            printSetup.setRightMargin(0.73);
        }
        String path = lastPath.substring(0, lastPath.lastIndexOf("/"));
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        FileOutputStream fileOut = new FileOutputStream(lastPath);
        fromExcelShipper.write(fileOut);
        fileOut.flush();
        fileOut.close();
    }
}
