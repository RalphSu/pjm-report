/**
 * 
 */
package org.pjm2.report;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TextAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
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
	private Dao dao;
	private ReportTask task;
	private static final int CHART_WIDTH=400;
	private static final int CHART_HEIGHT=280;



	public ReportPOIWriter(Dao dao, ReportTask task) {
		this.dao = dao;
		this.task = task;
	}

	public boolean write( Map<ReportTemplate, List<ReportLine>> reportData) {
		try {
			CustomXWPFDocument doc = new CustomXWPFDocument(ReportPOIWriter.class.getResourceAsStream("/Template.docx"));
			{
				XWPFParagraph p1 = doc.createParagraph();
				XWPFRun title = p1.createRun();
				p1.setAlignment(ParagraphAlignment.CENTER);
				p1.setVerticalAlignment(TextAlignment.TOP);
				title.setBold(true);
				title.setFontSize(20);
//				title.setFontFamily("微软雅黑");
				title.setText(String.format("%s",task.getProjectName()));
			}
			XWPFParagraph p2 = doc.createParagraph();
			{
				String type = "日报";
				if (ReportTask.TASKTYPE.weekly.name().equalsIgnoreCase(
						task.getTaskType())) {
					type = "周报 ";
				} else if (ReportTask.TASKTYPE.summary.name().equalsIgnoreCase(
						task.getTaskType())) {
					type = "结案报告";
				}
				XWPFRun title1 = p2.createRun();
				p2.setAlignment(ParagraphAlignment.CENTER);
				p2.setVerticalAlignment(TextAlignment.TOP);
				title1.setBold(false);
				title1.setFontSize(14);
//				title1.setFontFamily("微软雅黑");
				SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
				String startDate = format.format(task.getReportStartTime());
				title1.setText(String.format("（%s - %s）", startDate, type));
			}

			// insert a page break
			{
				XWPFRun breakRun = p2.createRun();
				breakRun.addBreak(BreakType.PAGE);
			}
			
			Map<String, List<Entry<ReportTemplate, List<ReportLine>>>> sortedData = shuffle(reportData);
			for (Entry<String, List<Entry<ReportTemplate, List<ReportLine>>>> e : sortedData.entrySet()) {
				writeTemplateType(doc, e);
			}
			
				writeImageAnaylysis(doc,reportData);	
			
			save(doc);
		} catch (Exception e) {
			logger.error("Fail to write the template!", e);
			return false;
		}
		return true;
	}
	
	
	private String getReportFilePath(){
		String prefix = System.getenv("PJM_HOME");
		if(prefix==null)
			prefix = System.getProperty("PJM_HOME");
		if(prefix==null)
		    prefix = " ../../pjm2/";
		String path = prefix + "/reports/" + this.task.getProjectName() + "/";
		// check parent directory
//		path = "/home/likewise-open/CORP/liasu/2.docx";
		try{
			FileUtils.forceMkdir(new File(path));	
		}catch(Throwable t){
			logger.error("create path error "+path, t);
			throw new RuntimeException(t);
		}
		return path;
	}

	private void save(XWPFDocument doc) {
		FileOutputStream out = null;
		try {
			String path = getReportFilePath();
			String file = path + task.getId() + ".docx";
            logger.info(" write to file path:" + file);
			out = new FileOutputStream(file);
			doc.write(out);
			out.flush();
            task.setGen_path("/reports/" + this.task.getProjectName() + "/"+task.getId()+".docx");
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

	private void writeTemplateType(CustomXWPFDocument doc, Entry<String, List<Entry<ReportTemplate, List<ReportLine>>>> e) {
		XWPFParagraph templateParagraph = doc.createParagraph();
//		templateParagraph.setAlignment(ParagraphAlignment.LEFT);
//		templateParagraph.setVerticalAlignment(TextAlignment.CENTER);
		templateParagraph.setStyle("Heading1");
		XWPFRun templateRun = templateParagraph.createRun();
		templateRun.setFontSize(20);
		templateRun.setText(e.getKey().substring(0, e.getKey().length() - 2)); // assume last two word is "模板"
//		templateRun.setBold(true);

		int i = 0;
		for (Entry<ReportTemplate, List<ReportLine>> module : e.getValue()) {
			i++;
			writeModule(doc, i, module.getKey(), module.getValue());
		}

		
	}
	
	public void writeImageAnaylysis( CustomXWPFDocument doc, Map<ReportTemplate, List<ReportLine>> reportData) {
		{
			XWPFParagraph templateParagraph = doc.createParagraph();
			templateParagraph.setStyle("Heading1");
			XWPFRun templateRun = templateParagraph.createRun();
			templateRun.setFontSize(20);
			templateRun.setText("图表类");
		}
		
		{
			XWPFParagraph labelParagraph = doc.createParagraph();
			labelParagraph.setStyle("Heading2");
			XWPFRun templateRun = labelParagraph.createRun();
			templateRun.setFontSize(14);
			templateRun.setBold(false);
			templateRun.setText("网络與情信息每日與情走势");
		}
		String trendCountFileName = createTrendCountFile(reportData.keySet());
		try{
			FileInputStream fis=new FileInputStream(trendCountFileName);
			doc.addPictureData(fis,  XWPFDocument.PICTURE_TYPE_PNG);	
			 doc.createPicture(doc.getAllPictures().size()-1, CHART_WIDTH, CHART_HEIGHT);  
		}catch(Throwable t){
			logger.error("write trendCountFileName ",t);
		}
		
		{
			XWPFParagraph labelParagraph = doc.createParagraph();
			labelParagraph.setStyle("Heading2");
			XWPFRun templateRun = labelParagraph.createRun();
			templateRun.setFontSize(14);
			templateRun.setText("监测信息平台分布图");
		}
		String distroPieChartFileName = createDistroPieChartFileName(reportData);
		try{
			FileInputStream fis=new FileInputStream(distroPieChartFileName);
			doc.addPictureData(fis,  XWPFDocument.PICTURE_TYPE_PNG);	
			doc.createPicture(doc.getAllPictures().size()-1, CHART_WIDTH, CHART_HEIGHT);
		}catch(Throwable t){
			logger.error("write distroPieChartFileName ",t);
		}
	
		{
			XWPFParagraph labelParagraph = doc.createParagraph();
			labelParagraph.setStyle("Heading2");
			XWPFRun templateRun = labelParagraph.createRun();
			templateRun.setFontSize(14);
			templateRun.setText("视频信息比重");
		}
		String videoPieChartFileName = createVideoPieChartFileName(reportData);
		try{
			FileInputStream fis=new FileInputStream(videoPieChartFileName);
			doc.addPictureData(fis,  XWPFDocument.PICTURE_TYPE_PNG);	
			doc.createPicture(doc.getAllPictures().size()-1, CHART_WIDTH, CHART_HEIGHT);
		}catch(Throwable t){
			logger.error("write distroPieChartFileName ",t);
		}
		
		//FIXME 4.	网络关注情况分布
		
		//5.	网络新闻信息每日舆情走势
		{
			XWPFParagraph labelParagraph = doc.createParagraph();
			labelParagraph.setStyle("Heading2");
			XWPFRun templateRun = labelParagraph.createRun();
			templateRun.setFontSize(14);
			templateRun.setText("网络新闻信息每日舆情走势");
		}
		String newsTrendCountFileName = createNewsTrendCountFile(reportData.keySet());
		try{
			FileInputStream fis=new FileInputStream(newsTrendCountFileName);
			doc.addPictureData(fis,  XWPFDocument.PICTURE_TYPE_PNG);	
			doc.createPicture(doc.getAllPictures().size()-1, CHART_WIDTH, CHART_HEIGHT);
		}catch(Throwable t){
			logger.error("write newsTrendCountFileName ",t);
		}
		
		{
			XWPFParagraph labelParagraph = doc.createParagraph();
			labelParagraph.setStyle("Heading2");
			XWPFRun templateRun = labelParagraph.createRun();
			templateRun.setFontSize(14);
			templateRun.setText("网络新闻信息媒体覆盖情况");
		}
		String newsPlatformDistro = createNewsPlatformDistro(reportData);
		try{
			FileInputStream fis=new FileInputStream(newsPlatformDistro);
			doc.addPictureData(fis,  XWPFDocument.PICTURE_TYPE_PNG);	
			doc.createPicture(doc.getAllPictures().size()-1, CHART_WIDTH, CHART_HEIGHT);
		}catch(Throwable t){
			logger.error("write newsPlatformDistro ",t);
		}
	}
	
    private String createNewsPlatformDistro(
			Map<ReportTemplate, List<ReportLine>> reportData) {
    	List<ReportLine> newsReportLine = new ArrayList<ReportLine>();
    	for(ReportTemplate reportTemplate:reportData.keySet()){
			if(Dao.NEWS_TEMPLATE_TYPE.equalsIgnoreCase(reportTemplate.getTemplate_type())){
				newsReportLine.addAll(reportData.get(reportTemplate));
			}
		}
    	Map<String, Integer> platformDistro = new HashMap<String, Integer>();
    	for(ReportLine reportLine:newsReportLine){
    		if(reportLine.getColumns()==null) continue;
    		for(String columnName:reportLine.getColumns().keySet()){
    			if("发布平台".equals(columnName)){
    				Object platform = reportLine.getColumns().get(columnName);
    				if(platform instanceof String){
    					String platformCatalog="其他";
    					if(((String)platform).contains("新浪")){
    						platformCatalog="新浪";
    					}else if(((String)platform).contains("搜狐")){
    						platformCatalog="搜狐";
    					}else if(((String)platform).contains("网易")){
    						platformCatalog="网易";
    					}else if(((String)platform).contains("腾讯")){
    						platformCatalog="腾讯";
    					}else if(((String)platform).contains("凤凰")){
    						platformCatalog="凤凰";
    					}else if(((String)platform).contains("人民网")){
    						platformCatalog="人民网";
    					}else if(((String)platform).contains("东方网")){
    						platformCatalog="东方网";
    					}else if(((String)platform).contains("新华网")){
    						platformCatalog="新华网";
    					}
    					
    					if(platformDistro.containsKey(platformCatalog)){
    						int count = platformDistro.get(platformCatalog);
    						count++;
    						platformDistro.put(platformCatalog, count);
    					}else{
    						platformDistro.put(platformCatalog, 1);
    					}
    				}else continue;
    			}
    		}
    	}
    	DefaultCategoryDataset mDataset = new DefaultCategoryDataset(); 
    	for(String platformName:platformDistro.keySet()){
    		mDataset.addValue(platformDistro.get(platformName), "", platformName);
    	}
    	
    	JFreeChart chart = ChartFactory.createBarChart(
    			"新闻发布平台分布", // 图表标题
    			"新闻发布平台", // 目录轴的显示标签
    			"信息", // 数值轴的显示标"
    			mDataset, // 数据
    			PlotOrientation.VERTICAL , // 图表方向：垂�    			
    			false, // 是否显示图例(对于简单的柱状图必须是false)
    			false, // 是否生成工具
    			false // 是否生成URL链接
    			); 
         chart.setTitle(getTextTile("新闻发布平台分布"));
         
    	chart.setBackgroundPaint(Color.WHITE);   
         CategoryPlot categoryplot = (CategoryPlot) chart.getPlot();   
         categoryplot.setBackgroundPaint(Color.WHITE);   
         categoryplot.setDomainGridlinePaint(Color.white);   
         categoryplot.setDomainGridlinesVisible(true);   
         //x� 
         CategoryAxis mDomainAxis = categoryplot.getDomainAxis();  
         //设置x轴标题的字体  
         mDomainAxis.setLabelFont(new Font("宋体", Font.PLAIN, 15));  
         //设置x轴坐标字� 
         mDomainAxis.setTickLabelFont(new Font("宋体", Font.PLAIN, 15));  
         //y� 
         ValueAxis mValueAxis = categoryplot.getRangeAxis();  
         //设置y轴标题字� 
         mValueAxis.setLabelFont(new Font("宋体", Font.PLAIN, 15));  
         //设置y轴坐标字� 
         mValueAxis.setTickLabelFont(new Font("宋体", Font.PLAIN, 15));  
         categoryplot.setRangeGridlinePaint(Color.white);   
         return chartToFile(chart, "platformdistro");
		
	}

	private String createNewsTrendCountFile(Set<ReportTemplate> keySet) {
		Set<ReportTemplate> newsReportTemplate = new HashSet<ReportTemplate>();
		for(ReportTemplate reportTemplate:keySet){
			if(Dao.NEWS_TEMPLATE_TYPE.equalsIgnoreCase(reportTemplate.getTemplate_type())){
				newsReportTemplate.add(reportTemplate);
			}
		}
		//截止报告生成日前每日舆情走势，类似图.但纵坐标只统计新闻类的信息量
		TimeSeriesCollection timeSeriesCollection = getTimeSeriesCollection(newsReportTemplate);
		JFreeChart chart = createChartPress(timeSeriesCollection, "网络新闻信息日走势");  
		return chartToFile(chart, "news_trend");
		
	}

	private String createVideoPieChartFileName(
			Map<ReportTemplate, List<ReportLine>> reportData) {
    	int nonVideoCount=0;
    	int videoCount=0;
    	for(ReportTemplate template:reportData.keySet()){
			if(Dao.NEWS_TEMPLATE_TYPE.equalsIgnoreCase(template.getTemplate_type())){
				String classified = template.getClassified();
				if(classified!=null&&classified.contains("视频")){
					videoCount+=reportData.get(template).size();
				}else{
					nonVideoCount+=reportData.get(template).size();
				}
		    }
		}
    	 DefaultPieDataset dataset = new DefaultPieDataset();  
    	 dataset.setValue("视频信息",videoCount);  
     	 dataset.setValue("非视频网络信息", nonVideoCount);
     	 
     	JFreeChart chart = ChartFactory.createPieChart3D("视频信息分布", dataset, true, true, true);  
        
        Font font = new Font("", Font.BOLD, 14);  
        chart.setTitle(getTextTile("视频信息分布"));
        chart.getLegend().setItemFont(font);  
        
        PiePlot3D  piePlot = (PiePlot3D ) chart.getPlot();  
        piePlot.setBackgroundPaint(new Color(255, 255, 255)); 
        piePlot.setLabelLinksVisible(false);
        piePlot.setLabelFont(font);  
        piePlot.setLabelBackgroundPaint(new Color(255, 255, 255));

        piePlot.setForegroundAlpha(1.0F);    
        piePlot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} {2}")); 
        piePlot.setSectionPaint("视频信息", new Color(79, 129, 189));    
        piePlot.setSectionPaint("非视频网络信息", new Color(192,80,77));     
        piePlot.setStartAngle(10.0); 
        return chartToFile(chart, "videodistro");
	}

	private String createDistroPieChartFileName(Map<ReportTemplate, List<ReportLine>> reportData) {
    	 DefaultPieDataset dataset = new DefaultPieDataset();  
         int newsCount=0;
         int blogCount=0;
         int forumCount=0;
         int weiboCount=0;
          
		for(ReportTemplate template:reportData.keySet()){
			if(Dao.NEWS_TEMPLATE_TYPE.equalsIgnoreCase(template.getTemplate_type())){
				newsCount+= reportData.get(template).size();
		    }else if(Dao.BLOG_TEMPLATE_TYPE.equalsIgnoreCase(template.getTemplate_type())){
		    	blogCount+= reportData.get(template).size();
		    }else if(Dao.FORUM_TEMPLATE_TYPE.equalsIgnoreCase(template.getTemplate_type())){
		    	forumCount+= reportData.get(template).size();
		    }else if(Dao.WEIBO_TEMPLATE_TYPE.equalsIgnoreCase(template.getTemplate_type())){
		    	weiboCount+= reportData.get(template).size();
		    }
		}
		dataset.setValue("新闻",newsCount);  
    	dataset.setValue("博客", blogCount);
    	dataset.setValue("论坛", forumCount);
       	dataset.setValue("微博", weiboCount);
		 
    	
		JFreeChart chart = ChartFactory.createPieChart3D("网络信息平台分布", dataset, true, true, true);  
		chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,
	        	    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	 
        Font font = new Font("", Font.BOLD, 18);  
        chart.setTitle(getTextTile("网络信息平台分布"));
        chart.getLegend().setItemFont(font);  
        PiePlot3D  piePlot = (PiePlot3D ) chart.getPlot();  
        piePlot.setBackgroundPaint(new Color(255, 255, 255));  
        piePlot.setLabelFont(font);  
        piePlot.setLabelBackgroundPaint(new Color(255, 255, 255));
        piePlot.setLabelLinkStyle(PieLabelLinkStyle.STANDARD);
        piePlot.setForegroundAlpha(1.0F);    
        piePlot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} {2}")); 
        piePlot.setSectionPaint("新闻", new Color(0, 112, 192));    
        piePlot.setSectionPaint("博客", new Color(146,208,80));    
        piePlot.setSectionPaint("论坛", new Color(192, 80, 77));    
        piePlot.setSectionPaint("微博", new Color(75, 172, 198));    
        piePlot.setStartAngle(10.0); 
        return chartToFile(chart, "distro");
        
	}
    
    private String chartToFile(JFreeChart chart, String type){
    	 try{
    		 String fileName = this.getReportFilePath()+ task.getId()+type+".png";
    		 logger.info("generate charts file name is "+fileName);
         	ChartUtilities.saveChartAsPNG(new File(fileName), chart, 650, 380);  
         	return fileName; 
 		 }catch(Exception e){
 			 logger.info("chart to file fail type "+type,e);
 			 throw new RuntimeException(e);
 		 }
    }

	private String createTrendCountFile(Set<ReportTemplate> reportData) {
    	//1.	网络舆情信息每日舆情走势 纵坐标：新闻类，微博类，论坛类，博客类，微信类所有信息条数总和
		TimeSeriesCollection timeSeriesCollection = getTimeSeriesCollection(reportData);
		JFreeChart chart = createChartPress(timeSeriesCollection, "网络信息监测日走势");  
		return chartToFile(chart, "trend");
	}
	
	@SuppressWarnings("deprecation")
    private TimeSeriesCollection getTimeSeriesCollection(Set<ReportTemplate> reportData){
		//default get previous 2 weeks data 
		 TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();
        TimeSeries timeseries = new TimeSeries("ffffff", org.jfree.data.time.Day.class);
		for(int m=15;m>0;m--){
			Calendar cal = Calendar.getInstance();
			cal.setTime(this.task.getReportStartTime());
			cal.add(Calendar.DAY_OF_MONTH, (0-m));
			Date calStartDate = cal.getTime();
			cal.add(Calendar.DAY_OF_MONTH, 1);
			Date calEndDate = cal.getTime();
			long totalcount =0;
			for(ReportTemplate template:reportData){
				long count = dao.findReportLineCount(template, this.task.getProjectId(),
						calStartDate, calEndDate);
				totalcount +=count;
			}
			
			Calendar cal2 = Calendar.getInstance();
			cal2.setTime(calStartDate);
			Day days = new Day(cal2.get(Calendar.DAY_OF_MONTH),cal2.get(Calendar.MONTH)+1, cal2.get(Calendar.YEAR));
			
			if(totalcount==0) continue;
			 timeseries.add(days, (int)totalcount); 
			 
			 
		}
		timeSeriesCollection.addSeries(timeseries);
		return timeSeriesCollection;
	}

	@SuppressWarnings("deprecation")
    private  JFreeChart createChartPress(XYDataset xydataset,  
            String title) {  
 
     
          
        if (xydataset != null) {  
            int counts = xydataset.getItemCount(0);  
            if (counts == 0) {  
                xydataset = null;  
            }  
        }  
 
        JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(title, "",  
                "", xydataset, false, false, false);  
        jfreechart.setBackgroundPaint(Color.white);  
        jfreechart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,
        	    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
 
        TextTitle text = getTextTile(title);
        jfreechart.setTitle(text);  
        jfreechart.setBorderVisible(false);
        
        XYPlot xyplot = jfreechart.getXYPlot();  
        xyplot.setBackgroundPaint(new Color(255, 255, 255));  
        
        ValueAxis vaxis = xyplot.getDomainAxis();  
        vaxis.setAxisLineStroke(new BasicStroke(1.0f)); // 坐标轴粗� 
        vaxis.setAxisLinePaint(new Color(10, 10, 10)); // 坐标轴颜� 
        
        vaxis.setLabelPaint(new Color(10, 10, 10)); // 坐标轴标题颜� 
        vaxis.setLowerMargin(0.06d);// 分类轴下（左）边� 
        vaxis.setUpperMargin(0.14d);// 分类轴下（右）边�防止最后边的一个数据靠近了坐标轴� 
          
        //X轴为日期格式，这里是专门的处理日期的类，  
        SimpleDateFormat format = new SimpleDateFormat("MM/dd");  
        DateAxis dateaxis = (DateAxis) xyplot.getDomainAxis();  
        dateaxis.setTickUnit(new DateTickUnit(DateTickUnit.DAY, 1, format));  
        dateaxis.setVerticalTickLabels(true); // 设为true表示横坐标旋转到垂直� 
        dateaxis.setTickMarkPosition(DateTickMarkPosition.START);  
 
        ValueAxis valueAxis = xyplot.getRangeAxis();  
        valueAxis.setAutoRange(true);
        valueAxis.setAxisLineStroke(new BasicStroke(1.0f)); // 坐标轴粗� 
        valueAxis.setAxisLinePaint(new Color(10,10, 10)); // 坐标轴颜� 
        valueAxis.setLabelPaint(new Color(10, 10, 10)); // 坐标轴标题颜� 
        (( NumberAxis)valueAxis).setAutoRangeStickyZero(true);
        (( NumberAxis)valueAxis).setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        xyplot.setRangeGridlinesVisible(true);  
        xyplot.setDomainGridlinesVisible(false);  
        xyplot.setRangeGridlinePaint(Color.LIGHT_GRAY);  
        xyplot.setRangeGridlineStroke(new BasicStroke(1.0f));
        
        
        
        xyplot.setNoDataMessageFont(new Font("", Font.BOLD, 14));//字体的大小，粗体� 
        xyplot.setNoDataMessagePaint(new Color(87, 149, 117));//字体颜色  
        xyplot.setAxisOffset(new RectangleInsets(0d, 0d, 0d, 5d)); //  
 
     
      
 
        XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer) xyplot  
                .getRenderer();  
        //第一条折线的颜色  
        xylineandshaperenderer.setBaseItemLabelsVisible(false);  
        xylineandshaperenderer.setSeriesFillPaint(0, new Color(51, 102, 255));  
        xylineandshaperenderer.setSeriesPaint(0, new Color(51, 102, 255));  
 
      
    
        //折线的粗细调  
        StandardXYToolTipGenerator xytool = new StandardXYToolTipGenerator();  
        xylineandshaperenderer.setToolTipGenerator(xytool);  
        xylineandshaperenderer.setStroke(new BasicStroke(1.5f));  
   
 
        return jfreechart;  
    } 
    
	private TextTitle getTextTile(String title){
		   // 设置标题的颜� 
        TextTitle text = new TextTitle(title);  
        text.setPaint(new Color(255, 255, 255));
        text.setBackgroundPaint(new Color(0,112,192));
        text.setExpandToFitSpace(true);
        
        text.setFont(new Font("", Font.BOLD, 18));
        return text;
	}


	private void writeModule(CustomXWPFDocument doc, int index, ReportTemplate template, List<ReportLine> lines) {
		XWPFParagraph nameParagraph = doc.createParagraph();
		nameParagraph.setAlignment(ParagraphAlignment.LEFT);
		nameParagraph.setVerticalAlignment(TextAlignment.CENTER);
		nameParagraph.setStyle("Heading2");
		XWPFRun moduleName = nameParagraph.createRun();
		moduleName.setBold(false);
		moduleName.setFontSize(14);
		moduleName.setText(template.getClassified());

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
		// 设置上下左右四个方向的距离，可以将表格撑�		table.setCellMargins(20, 20, 20, 20);
		XWPFTableRow headRow = table.getRow(0);
		List<XWPFTableCell> headerCells = headRow.getTableCells();
		for (int i = 0; i < headers.size(); i++) {
			headerCells.get(i).setText(headers.get(i));
			headerCells.get(i).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(widths.get(i)));
		}

        Set<String> imagePaths = new HashSet<String>();
		final int LINE_SIZE = lines.size();
		for (int j = 1; j < LINE_SIZE; j++) {
			ReportLine line = lines.get(j);
			XWPFTableRow row = table.getRow(j);
			for (int i = 0; i < headers.size(); i++) {
				Object obj = line.getColumns().get(headers.get(i));
				if (obj != null) {
					String body = obj.toString();
                    row.getCell(i).setText(body);
                    // find matched image if any
                    List<String> path = dao.findImagePathByUrl(body);
                    imagePaths.addAll(path);
				} else {
					row.getCell(i).setText("");
				}
				row.getCell(i).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(widths.get(i)));
			}
		}

		// picture
		writeModuleImage(doc, imagePaths);
	}

	private void writeModuleImage(CustomXWPFDocument doc, Set<String> imagePaths) {
	    logger.info("start write image for module... ");
        for (String path : imagePaths) {
            FileInputStream fis = null;
            // detect extension
            int index = path.lastIndexOf('.');
            int fileType = XWPFDocument.PICTURE_TYPE_PNG;
            if (index >=0) {
                String ext = path.substring(index);
                ext = ext.toUpperCase();
                if (ext.endsWith("JPEG") ) {
                    fileType = XWPFDocument.PICTURE_TYPE_JPEG;
                } else if (ext.endsWith("BMP")) {
                    fileType = XWPFDocument.PICTURE_TYPE_BMP;
                } else if (ext.endsWith("GIF")) {
                    fileType = XWPFDocument.PICTURE_TYPE_GIF;
                }
            }
            
            try {
                fis = new FileInputStream(path);
                doc.addPictureData(fis, fileType);
                doc.createPicture(doc.getAllPictures().size() - 1, CHART_WIDTH, CHART_HEIGHT);
            } catch (Throwable t) {
                logger.error("write module image failed. Path is " + path, t);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (Exception e) {
                        // ignore
                        logger.warn("close failure", e);
                    }
                }
            }
        }
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
	
	
    public static void main(String[] args) {
    	
    	DaoImpl daotest = new DaoImpl();
    	ReportTask task = new ReportTask();
    	task.setReportStartTime(new Date());
    	task.setProjectId(1l);
    	task.setProjectName("null");
    	task.setId(2l);
    	ReportPOIWriter writer = new ReportPOIWriter(daotest, task);
    	Set<ReportTemplate> templates = new HashSet<ReportTemplate>();
    	templates.add(new ReportTemplate());
    	templates.add(new ReportTemplate());
    	Map<ReportTemplate, List<ReportLine>> reportData = new HashMap<ReportTemplate, List<ReportLine>>();
    	{
    		// news
    		ReportTemplate reportTemplate = new ReportTemplate();
    		reportTemplate.setClassified("新闻稿发布");
    		reportTemplate.setTemplate_type(Dao.NEWS_TEMPLATE_TYPE);
    		List<ReportLine> line1 = new ArrayList<ReportLine>();
        	for(int m=0;m<80;m++){
        		ReportLine line =  new ReportLine();
        		line.setColumns(new HashMap<String, Object>());
        		line.getColumns().put("发布平台", "人民网");
        		line1.add(line);	
        	}
        	for(int m=0;m<60;m++){
        		ReportLine line =  new ReportLine();
        		line.setColumns(new HashMap<String, Object>());
        		line.getColumns().put("发布平台", "搜狐");
        		line1.add(line);	
        	}
        	for(int m=0;m<40;m++){
        		ReportLine line =  new ReportLine();
        		line.setColumns(new HashMap<String, Object>());
        		line.getColumns().put("发布平台", "网易");
        		line1.add(line);	
        	}
        	for(int m=0;m<30;m++){
        		ReportLine line =  new ReportLine();
        		line.setColumns(new HashMap<String, Object>());
        		line.getColumns().put("发布平台", "凤凰");
        		line1.add(line);	
        	}
        	for(int m=0;m<10;m++){
        		ReportLine line =  new ReportLine();
        		line.setColumns(new HashMap<String, Object>());
        		line.getColumns().put("发布平台", "新浪");
        		line1.add(line);	
        	}
        	
        	for(int m=0;m<10;m++){
        		ReportLine line =  new ReportLine();
        		line.setColumns(new HashMap<String, Object>());
        		line.getColumns().put("发布平台", "吉祥");
        		line1.add(line);	
        	}
        	reportData.put(reportTemplate, line1);
        	
    	}
    	
    	{
    		// blog
    		ReportTemplate reportTemplate = new ReportTemplate();
    		reportTemplate.setTemplate_type(Dao.BLOG_TEMPLATE_TYPE);
    		reportTemplate.setClassified("名人博客");
    		List<ReportLine> line1 = new ArrayList<ReportLine>();
        	for(int m=0;m<300;m++){
        		line1.add(new ReportLine());	
        	}
        	reportData.put(reportTemplate, line1);
        	
    	}
    	
    	{
    		// forum
    		ReportTemplate reportTemplate = new ReportTemplate();
    		reportTemplate.setTemplate_type(Dao.FORUM_TEMPLATE_TYPE);
    		reportTemplate.setClassified("论坛");
    		List<ReportLine> line1 = new ArrayList<ReportLine>();
        	for(int m=0;m<200;m++){
        		line1.add(new ReportLine());	
        	}
        	reportData.put(reportTemplate, line1);
        	
    	}
    	{
    		// twitter
    		ReportTemplate reportTemplate = new ReportTemplate();
    		reportTemplate.setTemplate_type(Dao.WEIBO_TEMPLATE_TYPE);
    		reportTemplate.setClassified("微博直发");
    		List<ReportLine> line1 = new ArrayList<ReportLine>();
        	for(int m=0;m<2500;m++){
        		line1.add(new ReportLine());	
        	}
        	reportData.put(reportTemplate, line1);
        	
    	}
    	
    	writer.write(reportData);
    	
	}
    
    public static  class DaoImpl extends Dao{
    	
    	@Override
    	public long findReportLineCount(ReportTemplate template,
    			long pid, Date startTime, Date endTime) {
    		long mili = System.currentTimeMillis();
    		return  (mili%1000) ;
    		
    	}
    }

}  
