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

import org.apache.commons.io.FileUtils;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TextAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.pjm2.report.db.model.ReportTask;
import org.pjm2.report.db.model.ReportTemplate;
import org.pjm2.report.model.ReportLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author liasu
 * 
 */
public class ReportPOIWriter {
	private static final Logger logger = LoggerFactory.getLogger(ReportPOIWriter.class);

	public ReportPOIWriter() {
	}

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
			title.setText(String.format("报表 - %s : 从 %s 到  %s ", task.getProjectName(), task.getReportStartTime(), task.getReportEndTime()));

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
		String identifier = task.getProjectName();
		
		FileOutputStream out = null;
		try {
			String prefix = System.getenv("PJM_HOME");
			if (prefix == null) {
			    prefix = System.getProperty("PJM_HOME");
			}
			String path = "/reports/" + identifier + "/";
			String file = path + task.getId() + ".docx";

			// check parent directory
			String parent = prefix + path;
			FileUtils.forceMkdir(new File(parent));

            out = new FileOutputStream(prefix + file);
			doc.write(out);
			out.flush();
            task.setGen_path(file);
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
			widths.add(60);
		}
		XWPFTable table = doc.createTable(lines.size() + 1, headers.size());
		CTTblWidth width = table.getCTTbl().addNewTblPr().addNewTblW();
		width.setType(STTblWidth.DXA);
		width.setW(BigInteger.valueOf(9072));
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
		for (int j = 1; j < LINE_SIZE; j++) {
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
