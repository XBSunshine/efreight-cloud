package com.efreight.sc.utils;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import com.itextpdf.text.pdf.*;

/**
 * 生成pdf
 *
 * @author qipm
 * @version 1.0.0
 */
public class PDFUtil {
    private static Logger logger = LoggerFactory.getLogger(PDFUtil.class);

    /**
     * 设置字体
     *
     * @param fontsize
     * @param color
     * @param isBold
     * @return
     */
    public static Font setFont(Float fontsize, BaseColor color, Boolean isBold) {
        Font font = new Font();
        try {
            //中文字体,解决中文不能显示问题
            BaseFont baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            font = new Font(baseFont, fontsize, isBold ? Font.BOLD : Font.NORMAL, color);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("PdfUtil.setFont 设置字体出错  e={}", e);
        }
        return font;
    }

    /**
     * 设置Table
     *
     * @param numLine   列数
     * @param width     表格的宽度
     * @param upWidth   表格上面空白宽度
     * @param downWidth 表格下面空白宽度
     * @param floats    每列分别设置宽度
     * @return
     */
    public static PdfPTable setTable(int numLine, int width, float upWidth, float downWidth, float[] floats) {
        // 添加表格，3列
        PdfPTable table = new PdfPTable(numLine);
        // 设置表格宽度比例为%100
        table.setWidthPercentage(100);
        // 设置表格的宽度
        table.setTotalWidth(width);
        // 也可以每列分别设置宽度
        try {
            table.setTotalWidth(floats);
        } catch (DocumentException e) {
            e.printStackTrace();
            logger.error("PdfUtil.setTable 设置table出错  e={}", e);
        }
        // 锁住宽度
        table.setLockedWidth(true);
        // 设置表格上面空白宽度
        table.setSpacingBefore(upWidth);
        // 设置表格下面空白宽度
        table.setSpacingAfter(downWidth);
        // 设置表格默认为无边框
        table.getDefaultCell().setBorder(0);
        return table;
    }

    /**
     * 设置表格左右边距
     *
     * @param msg     表格内容
     * @param lineNum 表格占几列
     * @param lineNum 表格占几行
     * @param left
     * @param right
     * @param font
     * @return
     */
    public static PdfPCell setSpace(String msg, int lineNum, int rowNum, int left, int right, Font font) {
        // 字体样式
        PdfPCell cell = null;
        if (font == null) {
            cell = new PdfPCell(new Paragraph(msg));
        } else {
            cell = new PdfPCell(new Paragraph(msg, font));
        }
        // 设置行数
        cell.setColspan(lineNum);
        cell.setRowspan(rowNum);
        if (left > 0) {
            // 左边距
            cell.setPaddingLeft(left);
        }
        if (right > 0) {
            // 右边距
            cell.setPaddingRight(right);
        }
        return cell;
    }

    /**
     * 表格设置
     *
     * @param table
     * @param borderColor 边框颜色
     * @param center      居中
     * @param height      高度
     * @param border      无边框
     * @param cell
     * @return
     */
    public static void setCell(PdfPTable table, BaseColor borderColor, Boolean center, int height, Boolean border, PdfPCell cell) {
        // 左右居中
        if (center) {
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        }
        // 无边框
        if (border) {
            cell.setBorder(Rectangle.NO_BORDER);
        }
        // 边框颜色
        cell.setBorderColor(borderColor);
        // 上下居中
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        // 高度
        cell.setFixedHeight(height);
        table.addCell(cell);
    }
    public static void setCell2(PdfPTable table, BaseColor borderColor, Boolean center, int height, Boolean border, PdfPCell cell,Boolean flag) {
    	// 左右居中
    	if (center) {
    		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    	}
    	// 无边框
    	if (border) {
    		cell.setBorder(Rectangle.NO_BORDER);
    	}
    	// 边框颜色
    	cell.setBorderColor(borderColor);
    	// 上下居中
    	cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    	// 高度
    	cell.setFixedHeight(height);
    	if (flag) {
    		cell.disableBorderSide(2);
		} else {
			cell.disableBorderSide(1);
		}
    	cell.disableBorderSide(4);
    	table.addCell(cell);
    }

    /**
     * 创建条形码
     *
     * @param writer
     * @param barcode  条形码(数字)
     * @param hidnCode 是否隐藏条形码(数字)
     * @return
     */
    public static Image createBarcode(PdfWriter writer, String barcode, Boolean hidnCode) {
        Barcode128 code128 = new Barcode128();
        code128.setCode(barcode);
        // 增加一个条形码到表格
        code128.setCodeType(Barcode128.CODE128);
        // 隐藏文字
        if (hidnCode) {
            code128.setFont(null);
        }
        // 生成条形码图片
        PdfContentByte cb = writer.getDirectContent();
        return code128.createImageWithBarcode(cb, null, null);
    }

    /**
     * 表格添加条形码图片
     *
     * @param table
     * @param lineNum      表格占几列 setRowspan : 设置行
     * @param height       左边距
     * @param height       右边距
     * @param height       高度
     * @param code128Image 条形码图片
     * @return
     */
    public static void setBarcodeCell(PdfPTable table, int left, int right, int lineNum, int height, Image code128Image) {
        // 加入到表格
        PdfPCell cell = new PdfPCell(code128Image, true);
        // 设置左右边距
        if (left > 0) {
            // 左边距
            cell.setPaddingLeft(left);
        }
        if (right > 0) {
            // 右边距
            cell.setPaddingRight(right);
        }
        // 设置行数
        cell.setColspan(lineNum);
        // 边框颜色
        cell.setBorderColor(BaseColor.BLACK);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        // 高度
        cell.setFixedHeight(height);
        table.addCell(cell);
    }
    public static void setBarcodeCell2(PdfPTable table, int left, int right, int lineNum, int height, Image code128Image) {
        // 加入到表格
        PdfPCell cell = new PdfPCell(code128Image, true);
        // 设置左右边距
        if (left > 0) {
            // 左边距
            cell.setPaddingLeft(left);
        }
        if (right > 0) {
            // 右边距
            cell.setPaddingRight(right);
        }
        // 设置行数
//        cell.setColspan(lineNum);
        cell.setRowspan(lineNum);
        // 边框颜色
        cell.setBorderColor(BaseColor.BLACK);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//        cell.setBorder(Rectangle.NO_BORDER);
        // 高度
        cell.setFixedHeight(height);
        cell.disableBorderSide(8);
        table.addCell(cell);
    }
}
