package org.pjm2.report;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;

import org.hibernate.cfg.Environment;
import org.pjm2.report.db.model.ReportTemplate;

public class TestEntityLoad extends TestCase {

	private static Dao dao = new Dao();

	protected void setUp() throws Exception {
		buildSessionFactory();
	}

	private void buildSessionFactory() throws Exception {

		try {
			Map<String, String> props = new HashMap<String, String>();
			props.put(Environment.USER, "chiliproject");
			props.put(Environment.PASS, "chili");
			props.put(Environment.URL, "jdbc:mysql://d-shc-liasu.corp.ebay.com:3306/chiliproject");
			EntityManagerFactory sf = Persistence.createEntityManagerFactory("pjmUnit", props);
			
			dao.setEntityManagerFactory(sf);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void testLoad() {
		ReportTemplate t = new ReportTemplate();
		t.setTemplate_type("微博类模板");
//		t.setTemplate_type("新闻类模板");
		t.setClassified("微博推广");
//		t.setClassified("新闻稿推广");
		dao.findReportLine(t, 7, new Date(), new Date());
	}
	
	public void findTODOTasks() {
	    dao.findTODOTasks();
	}

}