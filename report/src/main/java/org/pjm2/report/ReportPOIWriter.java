/**
 * 
 */
package org.pjm2.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TextAlignment;
import org.apache.poi.xwpf.usermodel.VerticalAlign;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.pjm2.report.db.model.ReportTask;
import org.pjm2.report.db.model.ReportTemplate;
import org.pjm2.report.model.ReportLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.core.util.FileUtil;


/**
 * @author liasu
 * 
 */
public class ReportPOIWriter {
	private static final Logger logger = LoggerFactory.getLogger(ReportPOIWriter.class);

	public ReportPOIWriter() {
	}

	public void writeTest() throws IOException {
		XWPFDocument doc = new XWPFDocument(ReportPOIWriter.class.getResourceAsStream("/Template.docx"));
		XWPFParagraph p1 = doc.createParagraph();

		XWPFTable table = doc.createTable(11, 2);
		// 设置上下左右四个方向的距离，可以将表格撑大
		table.setCellMargins(20, 20, 20, 20);
		// table.set
		List<XWPFTableCell> tableCells = table.getRow(0).getTableCells();
		tableCells.get(0).setText("第一行第一列的数据");
		tableCells.get(1).setText("第一行第二列的数据");

		List<XWPFTableCell> tableCellsq = table.getRow(1).getTableCells();
		tableCellsq.get(0).setText("第二行第二列的数据");
		tableCellsq.get(1).setText("第二行第二列的数据");

		// 设置字体对齐方式
		p1.setAlignment(ParagraphAlignment.CENTER);
		p1.setVerticalAlignment(TextAlignment.TOP);
		p1.setStyle("heading 1");

		// 第一页要使用p1所定义的属性
		XWPFRun r1 = p1.createRun();
		// 设置字体是否加粗
		r1.setBold(true);
		r1.setFontSize(20);
		// 设置使用何种字体
		r1.setFontFamily("Courier");
		// 设置上下两行之间的间距
		r1.setTextPosition(20);
		r1.setText("公司招聘会入场须知");
		// insert

		// 设置个人信息
		XWPFParagraph p2 = doc.createParagraph();
		p2.setAlignment(ParagraphAlignment.LEFT);
		XWPFRun r2 = p2.createRun();
		r2.setTextPosition(15);
		r2.setText("姓名" + "                    " + "张三");
		r2.addCarriageReturn();
		r2.setText("性别" + "                    " + "女");
		r2.addCarriageReturn();
		r2.setText("手机号" + "               " + "12345678965");
		r2.addCarriageReturn();
		r2.setText("邮箱" + "                    " + "123@163.com");
		r2.addCarriageReturn();
		r2.setText("开始时间" + "      " + "2013-05-28 12:30");
		r2.addCarriageReturn();
		r2.setText("结束时间" + "      " + "2013-05-28 13:20");
		r2.addCarriageReturn();

		// 存放试题信息
		XWPFParagraph p3 = doc.createParagraph();
		p3.setWordWrap(true);
		XWPFRun r3 = p3.createRun();
		r3.setTextPosition(10);
		r3.setFontSize(15);
		r3.setText("一、选择题（共50分）");
		// 题目和选项
		XWPFParagraph p4 = doc.createParagraph();
		p4.setWordWrap(true);
		XWPFRun r4 = p4.createRun();
		r4.setTextPosition(13);
		r4.setText("    1、下面说法正确的是？（3分）");
		r4.addCarriageReturn();
		r4.setText("        A:子类如果使用父类的方法必须使用super关键字");
		r4.addCarriageReturn();
		r4.setText("        B:子类如果使用父类的方法必须使用super关键字");
		r4.addCarriageReturn();
		r4.setText("        C:子类如果使用父类的方法必须使用super关键字");
		r4.addCarriageReturn();
		r4.setText("        D:子类如果使用父类的方法必须使用super关键字");
		r4.addCarriageReturn();
		r4.setText("正确答案：ABCD");
		r4.setText("选择答案：AC");

		// 判断题
		XWPFParagraph p5 = doc.createParagraph();
		p5.setWordWrap(true);
		XWPFRun r5 = p5.createRun();
		r5.setTextPosition(10);
		r5.setFontSize(15);
		r5.setText("一、判断题（共50分）");
		XWPFParagraph p6 = doc.createParagraph();
		p6.setWordWrap(true);
		// 题目
		int i;
		for (i = 0; i < 5; i++) {
			XWPFRun r6 = p6.createRun();
			r6.setTextPosition(13);
			r6.setText("1、子类如果使用父类的方法必须使用super关键字(5分)");
			r6.addCarriageReturn();
			r6.setText("正确答案：对");
			r6.setText("      ");
			r6.setSubscript(VerticalAlign.BASELINE);
			r6.setText("选择答案：");
			XWPFRun r7 = p6.createRun();
			r7.setTextPosition(13);
			// 控制某一个字体颜色为红色
			if (i == 3) {
				r7.setColor("FF0000");
			}
			r7.setText("错");
			r7.addCarriageReturn();
		}
		
		XWPFParagraph p7 = doc.createParagraph();
		p7.setStyle("Title");
		XWPFRun run7 =  p7.createRun();
		run7.setText("Heading 1 text");
		
		
		XWPFTable table1 = doc.createTable(11, 2);
		// 设置上下左右四个方向的距离，可以将表格撑大
		table1.setCellMargins(20, 20, 20, 20);
		// table.set
		List<XWPFTableCell> tableCells1 = table1.getRow(0).getTableCells();
		tableCells1.get(0).setText("第一行第一列的数据");
		tableCells1.get(1).setText("第一行第二列的数据");

		List<XWPFTableCell> tableCellsq1 = table1.getRow(1).getTableCells();
		tableCellsq1.get(0).setText("第二行第二列的数据");
		tableCellsq1.get(1).setText("第二行第二列的数据");


		FileOutputStream out = null;
		try {
			out = new FileOutputStream("./simple.docx");
			doc.write(out);
		} catch (FileNotFoundException e) {
			logger.error("Can not open doc file for write!", e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			logger.error("Write doc file failed!", e);
			throw new RuntimeException(e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error("Close doc file failed! Ignore this failure!", e);
				}
			}
		}
		System.out.println("text success");
	}

//	public static void main(String[] args) throws IOException, InvalidFormatException {
//		new POIWriter().writeTest();
//		String fileName = "./simple.docx";
//		OPCPackage pack = POIXMLDocument.openPackage(fileName); 
//		CustomXWPFDocument doc = new CustomXWPFDocument(pack); 
//		String ind = doc.addPictureData(new FileInputStream("C:\\Users\\liasu\\Pictures\\20088822910855.jpg"), XWPFDocument.PICTURE_TYPE_JPEG); 
//		doc.createPicture(doc.getAllPictures().size() - 1, 259, 58); 
//		
//		FileOutputStream out = new FileOutputStream("./simple.image.docx");
//		doc.write(out);
//		out.flush();
//		out.close();
//		System.out.println("image success");
//	}

	public boolean write(ReportTask task, Map<ReportTemplate, List<ReportLine>> reportData) {
		try {
			CustomXWPFDocument doc = new CustomXWPFDocument(ReportPOIWriter.class.getResourceAsStream("/Template.docx"));
			XWPFParagraph p1 = doc.createParagraph();
			p1.setStyle("Title");
			XWPFRun title = p1.createRun();
			p1.setAlignment(ParagraphAlignment.CENTER);
			p1.setVerticalAlignment(TextAlignment.TOP);
			title.setBold(true);
			title.setFontSize(25);
			title.setFontFamily("Courier");
			title.setTextPosition(25);
			title.setText(String.format("报表 - %s : 从 %s 到  %s ", task.getProject_identifier(), task.getReportStartTime(), task.getReportEndTime()));

			Map<String, List<Entry<ReportTemplate, List<ReportLine>>>> sortedData = shuffle(reportData);
			for (Entry<String, List<Entry<ReportTemplate, List<ReportLine>>>> e : sortedData.entrySet()) {
				writeTemplateType(doc, e);
			}

			save(doc, task);
		} catch (Exception e) {
			logger.error("Fail to write the template!", e);
			return false;
		}
		return true;
	}

	private void save(XWPFDocument doc, ReportTask task) {
		String identifier = task.getProject_identifier();
		
		FileOutputStream out = null;
		try {
			String prefix = System.getenv("HOME");
			String path = prefix + "/reports/" + identifier + "/" + task.getId() + ".docx";
			// check parent directory
			FileUtil.createMissingParentDirectories(new File(path));

			out = new FileOutputStream(path);
			doc.write(out);
			out.flush();
			task.setReportPath(path);
		} catch (FileNotFoundException e) {
			logger.error("Can not open doc file for write!", e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			logger.error("Write doc file failed!", e);
			throw new RuntimeException(e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error("Close doc file failed! Ignore this failure!", e);
				}
			}
		}
	}

	private void writeTemplateType(XWPFDocument doc, Entry<String, List<Entry<ReportTemplate, List<ReportLine>>>> e) {
		XWPFParagraph templateParagraph = doc.createParagraph();
		templateParagraph.setAlignment(ParagraphAlignment.LEFT);
		templateParagraph.setVerticalAlignment(TextAlignment.CENTER);
		XWPFRun templateRun = templateParagraph.createRun();
		templateRun.setFontSize(20);
		templateRun.setText(e.getKey().substring(0, e.getKey().length() - 2)); // assume last two word is "模板"
		templateRun.setBold(true);

		int i = 0;
		for (Entry<ReportTemplate, List<ReportLine>> module : e.getValue()) {
			i++;
			writeModule(doc, i, module.getKey(), module.getValue());
		}

		writeImageAnaylysis(doc);
	}
	
	private void writeImageAnaylysis(XWPFDocument doc) {
		// TODO Auto-generated method stub
		
	}

	private void writeModule(XWPFDocument doc, int index, ReportTemplate template, List<ReportLine> lines) {
		XWPFParagraph moduleParagraph = doc.createParagraph();
		moduleParagraph.setAlignment(ParagraphAlignment.LEFT);
		moduleParagraph.setVerticalAlignment(TextAlignment.CENTER);
		XWPFRun moduleIndex = moduleParagraph.createRun();
		moduleIndex.setBold(true);
		moduleIndex.setFontSize(18);
		moduleIndex.setText("模块 " + index);
		
		XWPFParagraph nameParagraph = doc.createParagraph();
		nameParagraph.setAlignment(ParagraphAlignment.LEFT);
		nameParagraph.setVerticalAlignment(TextAlignment.CENTER);
		XWPFRun moduleName = nameParagraph.createRun();
		moduleName.setBold(false);
		moduleName.setFontSize(16);
		moduleName.setText("模块名称: " + template.getClassified());

		// table
		List<String> headers = template.getColumnHeaders();
		List<Integer> widths = new ArrayList<Integer>(headers.size());
		for (int i = 0; i < headers.size(); i++ ){
			widths.add(2000);
		}
		XWPFTable table = doc.createTable(lines.size() + 1, headers.size());
		// 设置上下左右四个方向的距离，可以将表格撑大
		table.setCellMargins(20, 20, 20, 20);
		XWPFTableRow headRow = table.getRow(0);
		List<XWPFTableCell> headerCells = headRow.getTableCells();
		for (int i = 0; i < headers.size(); i++) {
			headerCells.get(i).setText(headers.get(i));
			headerCells.get(i).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(widths.get(i)));
		}

		// now lines : should we limit the line size??
		final int LINE_SIZE = Math.min(lines.size(), 1000);
		for (int j = 0; j < LINE_SIZE; j++) {
			ReportLine line = lines.get(j);
			XWPFTableRow row = table.getRow(j);
			for (int i = 0; i < headers.size(); i++) {
				Object obj = line.getColumns().get(headers.get(i));
				if (obj != null) {
					row.getCell(i).setText(obj.toString());
				} else {
					row.getCell(i).setText("");
				}
				row.getCell(i).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(widths.get(i)));
			}
		}

		// picture
		// TODO
	}

	private Map<String, List<Entry<ReportTemplate, List<ReportLine>>>> shuffle(Map<ReportTemplate, List<ReportLine>> reportData) {
		// {template_type => [ report_template => List<ReportLine> ] }
		Map<String, List<Entry<ReportTemplate, List<ReportLine>>>> sortedData = new TreeMap<String, List<Entry<ReportTemplate, List<ReportLine>>>>();
		
		for (Entry<ReportTemplate, List<ReportLine>> e :reportData.entrySet()) {
			List<Entry<ReportTemplate, List<ReportLine>>> entries = sortedData.get(e.getKey().getTemplate_type());
			if (entries == null) {
				entries = new LinkedList<Map.Entry<ReportTemplate,List<ReportLine>>>();
				sortedData.put(e.getKey().getTemplate_type(), entries);
			}
			entries.add(e);
		}
		
		// sort
		for (Entry<String, List<Entry<ReportTemplate, List<ReportLine>>>> e : sortedData.entrySet()) {
			Collections.sort(e.getValue(), new ReportComparator());
		}
		
		return sortedData;
	}
	
	private static class ReportComparator implements Comparator<Entry<ReportTemplate, List<ReportLine>>> {
		public int compare(Entry<ReportTemplate, List<ReportLine>> object1, Entry<ReportTemplate, List<ReportLine>> object2) {
			return object1.getKey().getClassified().compareTo(object2.getKey().getClassified());
		}
	}

	public static String parseDateValue(Date d) {
		if (d == null) {
			return "";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(d);
	}

}  
