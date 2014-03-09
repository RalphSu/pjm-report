package org.pjm2.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;

import org.pjm2.report.db.model.ReportTask;
import org.pjm2.report.db.model.ReportTemplate;

public class TestEntityLoad extends TestCase {

	private static Dao dao = new Dao();

	protected void setUp() throws Exception {
		buildSessionFactory();
	}

	private void buildSessionFactory() throws Exception {

		try {
			Map<String, String> props = new HashMap<String, String>();
			EntityManagerFactory sf = Persistence.createEntityManagerFactory("pjmUnit", props);
			
			dao.setEntityManagerFactory(sf);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

//	public void testLoad() {
//		ReportTemplate t = new ReportTemplate();
//		t.setTemplate_type("微博类模板");
////		t.setTemplate_type("新闻类模板");
//		t.setClassified("微博推广");
////		t.setClassified("新闻稿推广");
//		dao.findReportLine(t, 7, new Date(), new Date());
//	}
	
	public void testfindTODOTasks() {
	    List<ReportTask> tasks = dao.findTODOTasks();
	    System.out.println("-------------find taks:: ");
	    for (ReportTask t : tasks) {
	    	System.out.println(t.getId());
	    }
	    System.out.println("-------------end find taks:: ");
	}
	
	public void testSortTemplates() {
	    List<String> tasks = dao.sortedTempalteTypes(23l);
	    System.out.println(tasks);
	}

	public void testReportLine() {
		List<ReportTemplate> templates = dao.findReportTemplates(23l);
		for (ReportTemplate template : templates) {
			dao.findReportLine(template, 23, null, null);
		}
	}

}