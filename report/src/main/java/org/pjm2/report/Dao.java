package org.pjm2.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.pjm2.report.db.model.ReportTask;
import org.pjm2.report.db.model.ReportTask.Status;
import org.pjm2.report.db.model.ReportTemplate;
import org.pjm2.report.internal.BlogDao;
import org.pjm2.report.internal.ForumDao;
import org.pjm2.report.internal.IEntityDao;
import org.pjm2.report.internal.NewsDao;
import org.pjm2.report.internal.WeiboDao;
import org.pjm2.report.model.ReportLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liasu
 * 
 */
public class Dao {
	private static final String NEWS_TEMPLATE_TYPE = "新闻类模板";
	private static final String WEIBO_TEMPLATE_TYPE = "微博类模板";
	private static final String BLOG_TEMPLATE_TYPE = "博客类模板";
	private static final String FORUM_TEMPLATE_TYPE = "论坛类模板";
	private static final Integer MAX_GENERATION_COUNT = 6;

	private static Logger logger = LoggerFactory.getLogger(Dao.class);

	public static EntityManager buildSessionFactory(Map<String, String> props) {
		try {
			EntityManagerFactory sf = Persistence.createEntityManagerFactory("pjmUnit", props);
			return sf.createEntityManager();
		} catch (Exception e) {
			logger.error("Failed to load connection!", e);
			throw new RuntimeException(e);
		}
	}

	private EntityManager manager;
	private final Map<String, IEntityDao> daos = new HashMap<String, IEntityDao>();

	public void init() {
		manager = buildSessionFactory(new HashMap<String, String>());
		initDaos();
	}

	private void initDaos() {
		IEntityDao dao = new NewsDao(manager);
		daos.put(NEWS_TEMPLATE_TYPE, dao);
		dao = new BlogDao(manager);
		daos.put(BLOG_TEMPLATE_TYPE, dao);
		dao = new ForumDao(manager);
		daos.put(FORUM_TEMPLATE_TYPE, dao);
		dao = new WeiboDao(manager);
		daos.put(WEIBO_TEMPLATE_TYPE, dao);
	}
	
	// for test only
	public void setEntityManager(EntityManager mgr) {
		this.manager = mgr;
		initDaos();
	}

	@SuppressWarnings("unchecked")
	public List<ReportTask> findTODOTasks() {
		String sql = "select * report_tasks where status in ('%s', '%s') and gen_count <= %d ";
        sql = String.format(sql, Status.planned, Status.inprogress, MAX_GENERATION_COUNT);
		Query query = manager.createNativeQuery(sql, ReportTask.class);
		List<?> result = query.getResultList();
		List<ReportTask> tasks = (List<ReportTask>) result;
		
		// fill project identifier
		List<Long> pId = new ArrayList<Long>();
		for (ReportTask task : tasks) {
			pId.add(task.getProjectId());
		}
		Map<Long, String> projects = new HashMap<Long, String>();
		sql = "select id, identifier from projects where id in ( %s )";
		query = manager.createNativeQuery(String.format(sql, StringUtils.join(pId, ',')));
		result = query.getResultList();
		for (Object o : result) {
			Object[] objs = (Object[])o;
			projects.put(((Number) objs[0]).longValue(), objs[1].toString());
		}
		for(ReportTask t:tasks){
			t.setProject_identifier(projects.get(t.getProjectId()));
		}
		return tasks;
	}

	@SuppressWarnings("unchecked")
	public List<ReportTemplate> findReportTemplates(Long project_id) {
		String sql = "select * from report_templates where project_id = " + project_id;
		Query query = manager.createNativeQuery(sql, ReportTemplate.class);
		List<?> result = query.getResultList();
		return (List<ReportTemplate>) result;
	}

	public List<ReportLine> findReportLine(ReportTemplate template, long pid, Date starTime, Date endTime) {
		String type = template.getTemplate_type();
		IEntityDao dao = daos.get(type);
		if (dao != null) {
			return dao.findReportLine(template, pid, starTime, endTime);
		} else {
			throw new IllegalArgumentException("unkonw tempalte" + template.getTemplate_type());
		}
	}
	
	public void save(Object entity) {
		manager.persist(entity);
	}

}
