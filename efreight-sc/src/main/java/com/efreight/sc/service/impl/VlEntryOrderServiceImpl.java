package com.efreight.sc.service.impl;

import com.efreight.sc.entity.VlEntryOrder;
import com.efreight.sc.entity.VlEntryOrderDetail;
import com.efreight.sc.entity.VlOrder;
import com.efreight.sc.entity.VlOrderDetailOrder;
import com.efreight.sc.entity.VlVehicleEntryOrder;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.dao.VlEntryOrderMapper;
import com.efreight.sc.dao.VlVehicleEntryOrderMapper;
import com.efreight.sc.service.VlEntryOrderDetailService;
import com.efreight.sc.service.VlEntryOrderService;
import com.efreight.sc.utils.PDFUtil;
import com.itextpdf.text.Anchor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import com.itextpdf.text.pdf.draw.LineSeparator;

import lombok.AllArgsConstructor;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author qipm
 * @since 2021-01-18
 */
@Service
@AllArgsConstructor
public class VlEntryOrderServiceImpl extends ServiceImpl<VlEntryOrderMapper, VlEntryOrder> implements VlEntryOrderService {
	
	private final VlVehicleEntryOrderMapper vlVehicleEntryOrderMapper;
	private final VlEntryOrderDetailService vlEntryOrderDetailService;
	@Override
	public List<VlEntryOrder> getListPage(VlVehicleEntryOrder bean) {
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		List<VlEntryOrder> list=baseMapper.getListPage(bean);
		for (int i = 0; i < list.size(); i++) {
			VlEntryOrder vlEntryOrder=list.get(0);
			List<VlEntryOrderDetail> vlEntryOrderDetails=baseMapper.getVlEntryOrderDetails(vlEntryOrder.getEntryOrderId());
			String mawbNumbers="";
			for (int j = 0; j < vlEntryOrderDetails.size(); j++) {
				VlEntryOrderDetail vlEntryOrderDetail=vlEntryOrderDetails.get(j);
				if (vlEntryOrderDetail.getMawbNumber()!=null && vlEntryOrderDetail.getMawbNumber().length()>0) {
					if (mawbNumbers.length()>0) {
						mawbNumbers=mawbNumbers+","+vlEntryOrderDetail.getMawbNumber();
					} else {
						mawbNumbers=vlEntryOrderDetail.getMawbNumber();
					}
				}
			}
			vlEntryOrder.setMawbNumbers(mawbNumbers);
		}
		return list;
	}

	@Override
	public Boolean doSave(VlEntryOrder bean) {
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		baseMapper.insert(bean);
		
		//
		VlVehicleEntryOrder vlVehicleEntryOrder=new VlVehicleEntryOrder();
		vlVehicleEntryOrder.setEntryOrderId(bean.getEntryOrderId());
		vlVehicleEntryOrder.setVlOrderId(bean.getVlOrderId());
		vlVehicleEntryOrderMapper.insert(vlVehicleEntryOrder);
		//
		List<VlEntryOrderDetail> vlEntryOrderDetails=bean.getVlEntryOrderDetails();
		for (int i = 0; i < vlEntryOrderDetails.size(); i++) {
			VlEntryOrderDetail vlEntryOrderDetail=vlEntryOrderDetails.get(i);
			vlEntryOrderDetail.setEntryOrderId(bean.getEntryOrderId());
		}
		vlEntryOrderDetailService.saveBatch(vlEntryOrderDetails);
		return true;
	}
	@Override
	public Boolean doUpdate(VlEntryOrder bean) {
		
		baseMapper.updateById(bean);
		
		//
		baseMapper.doDeleteDetails(bean.getEntryOrderId());
		List<VlEntryOrderDetail> vlEntryOrderDetails=bean.getVlEntryOrderDetails();
		for (int i = 0; i < vlEntryOrderDetails.size(); i++) {
			VlEntryOrderDetail vlEntryOrderDetail=vlEntryOrderDetails.get(i);
			vlEntryOrderDetail.setEntryOrderId(bean.getEntryOrderId());
		}
		vlEntryOrderDetailService.saveBatch(vlEntryOrderDetails);
		return true;
	}

	@Override
	public VlEntryOrder getOrderById(Integer entryOrderId) {
		VlEntryOrder bean=baseMapper.selectById(entryOrderId);
		List<VlEntryOrderDetail> vlEntryOrderDetails=baseMapper.getVlEntryOrderDetails(entryOrderId);
		bean.setVlEntryOrderDetails(vlEntryOrderDetails);
		return bean;
	}
	@Override
	public VlEntryOrder getVlOrder(Integer entryOrderId) {
		List<VlOrderDetailOrder> vlOrderDetailOrders=baseMapper.getVlOrderDetailOrders(entryOrderId);
		String truckNumber="";
		for (int i = 0; i < vlOrderDetailOrders.size(); i++) {
			VlOrderDetailOrder vlOrderDetailOrder=vlOrderDetailOrders.get(i);
			
			List<VlOrder> vlOrders=baseMapper.getVlOrders(entryOrderId,vlOrderDetailOrder.getOrderId());
			for (int j = 0; j < vlOrders.size(); j++) {
				VlOrder vlOrder=vlOrders.get(j);
				if (vlOrder!=null && vlOrder.getTruckNumber()!=null && vlOrder.getTruckNumber().length()>0) {
					if (truckNumber.length()>0) {
						truckNumber=truckNumber+","+vlOrder.getTruckNumber();
					} else {
						truckNumber=vlOrder.getTruckNumber();
					}
				}
			}
		}
		VlEntryOrder bean=new VlEntryOrder();
		if (truckNumber.length()>0) {
			bean.setVehteamFlag(1);
			bean.setVehteamNo(truckNumber);
		}else {
			bean.setVehteamFlag(0);
		}
		return bean;
	}

	@Override
	public List<VlEntryOrderDetail> getVlOrderDetail(Integer vlOrderId,String flag) {
		List<VlEntryOrderDetail> resultList=new ArrayList<VlEntryOrderDetail>();
		
		List<VlEntryOrderDetail> vlEntryOrderDetails=baseMapper.getVlOrderDetail(vlOrderId);
		for (int i = 0; i < vlEntryOrderDetails.size(); i++) {
			VlEntryOrderDetail vlEntryOrderDetail=vlEntryOrderDetails.get(i);
			String airlineName=vlEntryOrderDetail.getAirlineName();
			if (airlineName!=null && airlineName.length()>2) {
				vlEntryOrderDetail.setAirlineName(airlineName.substring(0,2));
			}
			resultList.add(vlEntryOrderDetail);
			if ("FHL".equals(flag)) {
				List<VlEntryOrderDetail> fhlList=baseMapper.getFHL(vlEntryOrderDetail.getOrderId());
				if (fhlList.size()>0) {
					resultList.addAll(fhlList);
				}
			}
			
		}
		return resultList;
	}

	@Override
	public Boolean doPrintVlOrder(Integer orgId, String entryOrderId, String userId) {
		 Document document = new Document(PageSize.A4);
		try {
		
		 
		 File file = new File("D:\\PDFDemo.pdf");
         file.createNewFile();
         PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
//         writer.setPageEvent(new Watermark("HELLO ITEXTPDF"));// 水印
//		 writer.setPageEvent(new MyHeaderFooter());// 页眉/页脚

         document.open();
         document.addTitle("Title@PDF-Java");// 标题
		document.addAuthor("Author@umiz");// 作者
		document.addSubject("Subject@iText pdf sample");// 主题
		document.addKeywords("Keywords@iTextpdf");// 关键字
		document.addCreator("Creator@umiz`s");// 创建者
		
		
		// 4.向文档中添加内容
//        generatePDF(document);
         
         
         document.close();
		} catch (Exception e) {
            e.printStackTrace();
        } finally {
        	document.close();
        }
		return true;
	}
	@Override
	public String doPrintVlOrder1(VlEntryOrder bean) {
		Document document = new Document(PageSize.A4);
		String fileName=bean.getOrgId()+"PDFDemo.pdf";
		try {
			File file = new File("/datadisk/html/PDFtemplate/temp/"+fileName);
			file.createNewFile();
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
//         writer.setPageEvent(new Watermark("HELLO ITEXTPDF"));// 水印
//		 writer.setPageEvent(new MyHeaderFooter());// 页眉/页脚
			
			document.open();
			document.addTitle("Title@PDF-Java");// 标题
			document.addAuthor("Author@umiz");// 作者
			document.addSubject("Subject@iText pdf sample");// 主题
			document.addKeywords("Keywords@iTextpdf");// 关键字
			document.addCreator("Creator@umiz`s");// 创建者
			
			
			// 4.向文档中添加内容
			generatePDF(writer,document,bean);
			
			
			document.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			document.close();
		}
		return "/PDFtemplate/temp/"+fileName;
	}

	// 生成PDF文件
		public void generatePDF(PdfWriter writer,Document document,VlEntryOrder bean) throws Exception {
			BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
			BaseFont bfChinese2 = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.EMBEDDED);
            
			Font titlefont = new Font(bfChinese, 16, Font.BOLD);
			Font titlefont2 = new Font(bfChinese2, 16, Font.BOLD);
			Font headfont = new Font(bfChinese, 14, Font.BOLD);
			Font keyfont = new Font(bfChinese, 10, Font.BOLD);
			Font textfont = new Font(bfChinese, 10, Font.NORMAL);
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String date=format.format(new Date());
	    	// 段落
			Paragraph paragraph1 = new Paragraph(date, textfont);
			Paragraph paragraph = new Paragraph("广州白云国际机场国际1号货站", titlefont);
			Paragraph paragraph2 = new Paragraph("自助进出区车辆配载单", titlefont);
			Paragraph paragraph3 = new Paragraph(" ", titlefont);
			Paragraph paragraph4 = new Paragraph(" ", titlefont);
			paragraph.setAlignment(1); //设置文字居中 0靠左   1，居中     2，靠右
			paragraph2.setAlignment(1); //设置文字居中 0靠左   1，居中     2，靠右
//			paragraph.setIndentationLeft(5); //设置左缩进
//			paragraph.setIndentationRight(5); //设置右缩进
//			paragraph.setFirstLineIndent(5); //设置首行缩进
//			paragraph.setLeading(2f); //行间距
//			paragraph.setSpacingBefore(-20f); //设置段落上空白
//			paragraph.setSpacingAfter(2f); //设置段落下空白
//	 
			// 直线
			Paragraph p1 = new Paragraph();
			p1.add(new Chunk(new LineSeparator()));
	 
			// 点线
			Paragraph p2 = new Paragraph();
			p2.add(new Chunk(new DottedLineSeparator()));
	 
			// 超链接
			Anchor anchor = new Anchor("baidu");
			anchor.setReference("www.baidu.com");
	 
			// 定位
			Anchor gotoP = new Anchor("goto");
			gotoP.setReference("#top");
	 
			// 添加图片
//			Image image = Image.getInstance("https://img-blog.csdn.net/20180801174617455?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl8zNzg0ODcxMA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70");
//			image.setAlignment(Image.ALIGN_CENTER);
//			image.scalePercent(40); //依照比例缩放
	 
			// 表格
			List<VlEntryOrderDetail> vlEntryOrderDetails=baseMapper.getVlEntryOrderDetails(bean.getEntryOrderId());
			
			PdfPTable table1 = createTable(new float[] { 100, 340, 100, 200});
//			table1.addCell(createCell("", headfont, Element.ALIGN_LEFT, 4, false));
			table1.addCell(createCell("车牌号：", keyfont, Element.ALIGN_CENTER));
			table1.addCell(createCell(bean.getVehicleNo(), textfont));
			table1.addCell(createCell("配载单编号：", keyfont, Element.ALIGN_CENTER));
			table1.addCell(createCell(bean.getSeq(), textfont));

			PdfPTable table2 = createTable(new float[] { 100, 100, 100, 140, 100, 200});
//			table2.addCell(createCell("", headfont, Element.ALIGN_LEFT, 6, false));
			table2.addCell(createCell("业务类型：", keyfont, Element.ALIGN_CENTER));
			table2.addCell(createCell("F-口岸业务", textfont));
			table2.addCell(createCell("运输类型：", keyfont, Element.ALIGN_CENTER));
			table2.addCell(createCell("重进空出（出口）", textfont));
			table2.addCell(createCell("进出口标识：", keyfont, Element.ALIGN_CENTER));
			table2.addCell(createCell("出口", textfont));
			
			PdfPTable table3 = createTable(new float[] { 100, 640});
//			table3.addCell(createCell("", headfont, Element.ALIGN_LEFT, 2, false));
			table3.addCell(createCell("具体业务类型", keyfont, Element.ALIGN_CENTER));

			table3.addCell(createCell("F11直出货物运抵国际货站", textfont, Element.ALIGN_LEFT));
			
			
			table3.addCell(createCell("公司名称：", keyfont, Element.ALIGN_CENTER));
			String orgName=baseMapper.getOrgName(bean.getOrgId());
			table3.addCell(createCell(orgName, textfont, Element.ALIGN_LEFT));
			
			PdfPTable table4 = createTable(new float[] { 100, 100, 100, 140, 100, 200});
//			table4.addCell(createCell("", headfont, Element.ALIGN_LEFT, 6, false));
			table4.addCell(createCell("司机姓名：", keyfont, Element.ALIGN_CENTER));
			table4.addCell(createCell("", textfont));
			table4.addCell(createCell("司机手机号：", keyfont, Element.ALIGN_CENTER));
			table4.addCell(createCell("", textfont));
			table4.addCell(createCell("电子关锁号：", keyfont, Element.ALIGN_CENTER));
			table4.addCell(createCell("", textfont));
			
			PdfPTable table5 = createTable(new float[] { 740});
//			table5.addCell(createCell("", headfont, Element.ALIGN_LEFT, 1, false));
			table5.addCell(createCell("海关审核签名：", textfont, Element.ALIGN_LEFT));
			
			PdfPTable table6 = createTable(new float[] { 740});
//			table6.addCell(createCell("", headfont, Element.ALIGN_LEFT, 1, false));
			table6.addCell(createCell("广州白云国际机场国际1号货站  自助进出区车辆配载单", titlefont2, Element.ALIGN_CENTER));
			table6.addCell(createCell("  ", titlefont, Element.ALIGN_CENTER));
			
			PdfPTable table = createTable(new float[] { 100, 120, 100,220, 50, 50, 100 });
//			table.addCell(createCell("", headfont, Element.ALIGN_LEFT, 7, false));
			table.addCell(createCell("序号", keyfont, Element.ALIGN_CENTER));
			table.addCell(createCell("卸货地代码", keyfont, Element.ALIGN_CENTER));
			table.addCell(createCell("单证编号", keyfont, Element.ALIGN_CENTER));
			table.addCell(createCell("总提运单号/分单号", keyfont, Element.ALIGN_CENTER));
			table.addCell(createCell("件数", keyfont, Element.ALIGN_CENTER));
			table.addCell(createCell("重量", keyfont, Element.ALIGN_CENTER));
			table.addCell(createCell("仓库签章", keyfont, Element.ALIGN_CENTER));
			Integer pieces = 0;
			BigDecimal totalWeight = new BigDecimal(0);
			for (int i = 0; i < vlEntryOrderDetails.size(); i++) {
				VlEntryOrderDetail VlEntryOrderDetail=vlEntryOrderDetails.get(i);
				table.addCell(createCell((i+1)+"", textfont));
				table.addCell(createCell(VlEntryOrderDetail.getWarehouseCode(), textfont));
				table.addCell(createCell(VlEntryOrderDetail.getDocNo(), textfont));
				String MawbNumber="";
				if (VlEntryOrderDetail.getMawbNumber()!=null && VlEntryOrderDetail.getMawbNumber().length()>0) {
					MawbNumber=VlEntryOrderDetail.getMawbNumber().replace("-", "");
				} 
				if (VlEntryOrderDetail.getHawbNumber()!=null && VlEntryOrderDetail.getHawbNumber().length()>0) {
					MawbNumber=MawbNumber+"_"+VlEntryOrderDetail.getHawbNumber();
				}
				table.addCell(createCell(MawbNumber, textfont));
				table.addCell(createCell(VlEntryOrderDetail.getPieces()+"", textfont));
				table.addCell(createCell(VlEntryOrderDetail.getTotalWeight()+"", textfont));
				table.addCell(createCell("", textfont));
				pieces=pieces +VlEntryOrderDetail.getPieces();
				totalWeight=totalWeight.add(VlEntryOrderDetail.getTotalWeight());
			}
			table.addCell(createCell("总计", keyfont));
			table.addCell(createCell("", textfont));
			table.addCell(createCell("", textfont));
			table.addCell(createCell(vlEntryOrderDetails.size()+"", textfont));
			table.addCell(createCell(pieces+"", textfont));
			table.addCell(createCell(totalWeight+"", textfont));
			table.addCell(createCell("", textfont));
	 
			
			BarcodeQRCode pdf417 = new BarcodeQRCode(bean.getSeq(), 100, 100, null);
			Image image128 = pdf417.getImage();
			
			
			document.add(paragraph1);
//			document.add(p1);
//			document.add(image128);
//			document.add(paragraph);
//			document.add(paragraph2);
			document.add(p1);
//			document.add(paragraph3);
//			document.add(paragraph4);
//			document.add(anchor);
//			document.add(p2);
//			document.add(gotoP);
			
			
			
			
			PdfPTable table0 = PDFUtil.setTable(2, 300, 0f, 0f, new float[]{150, 370});
			PDFUtil.setBarcodeCell2(table0, 5, 20, 2, 60,image128);
			PDFUtil.setCell2(table0, BaseColor.BLACK, true, 40, false,PDFUtil.setSpace("广州白云国际机场国际1号货站", 1, 1, 15, 0, PDFUtil.setFont(20f, BaseColor.BLACK, true)),true);
			PDFUtil.setCell2(table0, BaseColor.BLACK, true, 40, false,PDFUtil.setSpace("自助进出区车辆配载单", 1, 1, 15, 0, PDFUtil.setFont(20f, BaseColor.BLACK, true)),false);
			document.add(table0);
			
			
			
//			PdfPTable tableTwoCode = PDFUtil.setTable(3, 300, 0f, 0f, new float[]{220, 200, 100});
//			PDFUtil.setBarcodeCell(tableTwoCode, 220, 20, 3, 60,image128);
//			tableTwoCode.addCell(image128);
//			document.add(tableTwoCode);
			
			
			PdfPTable tableCode = PDFUtil.setTable(3, 300, 0f, 0f, new float[]{220, 200, 100});
			PDFUtil.setBarcodeCell(tableCode, 240, 20, 3, 50,PDFUtil.createBarcode(writer, bean.getSeq(), false));
			document.add(tableCode);
			
			document.add(table1);
			document.add(table2);
			document.add(table3);
			document.add(table4);
			document.add(table5);
			document.add(table);
			
			
			
//			document.add(image128);
		}
		public PdfPTable createTable(int colNumber, int align) {
			PdfPTable table = new PdfPTable(colNumber);
			try {
				int maxWidth = 520;
				table.setTotalWidth(maxWidth);
				table.setLockedWidth(true);
				table.setHorizontalAlignment(align);
				table.getDefaultCell().setBorder(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return table;
		}
		public PdfPTable createTable(float[] widths) {
			PdfPTable table = new PdfPTable(widths);
			try {
				int maxWidth = 520;
				table.setTotalWidth(maxWidth);
				table.setLockedWidth(true);
				table.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.getDefaultCell().setBorder(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return table;
		}
		public PdfPCell createCell(String value, Font font) {
	        PdfPCell cell = new PdfPCell();
	        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	        cell.setPhrase(new Phrase(value, font));
	        cell.setFixedHeight(30);
	        return cell;
	    }
		public PdfPCell createCell(String value, Font font, int align) {
			PdfPCell cell = new PdfPCell();
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.setHorizontalAlignment(align);
			cell.setPhrase(new Phrase(value, font));
			cell.setFixedHeight(30);
			return cell;
		}
		public PdfPCell createCell(String value, Font font, int align, int colspan) {
	        PdfPCell cell = new PdfPCell();
	        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	        cell.setHorizontalAlignment(align);
	        cell.setColspan(colspan);
	        cell.setPhrase(new Phrase(value, font));
	        cell.setFixedHeight(30);
	        return cell;
	    }
		public PdfPCell createCell(String value, Font font, int align, int colspan, boolean boderFlag) {
	        PdfPCell cell = new PdfPCell();
	        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	        cell.setHorizontalAlignment(align);
	        cell.setColspan(colspan);
	        cell.setPhrase(new Phrase(value, font));
	        cell.setPadding(3.0f);
	        if (!boderFlag) {
	            cell.setBorder(0);
	            cell.setPaddingTop(15.0f);
	            cell.setPaddingBottom(8.0f);
	        } else if (boderFlag) {
	            cell.setBorder(0);
	            cell.setPaddingTop(0.0f);
	            cell.setPaddingBottom(15.0f);
	        }
	        cell.setFixedHeight(30);
	        return cell;
	    }
		public PdfPCell createCell(String value, Font font, int align, float[] borderWidth, float[] paddingSize, boolean flag) {
			PdfPCell cell = new PdfPCell();
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.setHorizontalAlignment(align);
			cell.setPhrase(new Phrase(value, font));
			cell.setBorderWidthLeft(borderWidth[0]);
			cell.setBorderWidthRight(borderWidth[1]);
			cell.setBorderWidthTop(borderWidth[2]);
			cell.setBorderWidthBottom(borderWidth[3]);
			cell.setPaddingTop(paddingSize[0]);
			cell.setPaddingBottom(paddingSize[1]);
			if (flag) {
				cell.setColspan(2);
			}
			cell.setFixedHeight(30);
			return cell;
		}
		
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
}
