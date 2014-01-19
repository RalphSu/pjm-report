package org.pjm2.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
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
	public	static final String NEWS_TEMPLATE_TYPE = "新闻类模板";
	public 	static final String WEIBO_TEMPLATE_TYPE = "微博类模板";
	public 	static final String BLOG_TEMPLATE_TYPE = "博客类模板";
	public  static final String FORUM_TEMPLATE_TYPE = "论坛类模板";
	public  static final Integer MAX_GENERATION_COUNT = 6;

	private static Logger logger = LoggerFactory.getLogger(Dao.class);

	public EntityManagerFactory buildSessionFactory(Map<String, String> props) {
		try {
			EntityManagerFactory sf = Persistence.createEntityManagerFactory("pjmUnit", props);
			return sf;
		} catch (Exception e) {
			logger.error("Failed to load connection!", e);
			throw new RuntimeException(e);
		}
	}

	private EntityManagerFactory managerFactory;
	private final Map<String, IEntityDao> daos = new HashMap<String, IEntityDao>();

	public void init() {
		managerFactory = buildSessionFactory(new HashMap<String, String>());
		initDaos();
	}

	private void initDaos() {
		EntityManager manager = this.managerFactory.createEntityManager();
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
	public void setEntityManagerFactory(EntityManagerFactory mgrfct) {
		this.managerFactory = mgrfct;
		initDaos();
	}

	@SuppressWarnings("unchecked")
	public List<ReportTask> findTODOTasks() {
		String sql = "select * from report_tasks where status in ('%s', '%s') and ( gen_count IS NULL or gen_count <= %d )";
        sql = String.format(sql, Status.planned, Status.inprogress, MAX_GENERATION_COUNT);
        EntityManager manager = managerFactory.createEntityManager();
		Query query = manager.createNativeQuery(sql, ReportTask.class);
		List<?> result = query.getResultList();
		List<ReportTask> tasks = (List<ReportTask>) result;
		
		// fill project identifier
        if (!tasks.isEmpty()) {
            List<Long> pId = new ArrayList<Long>();
            for (ReportTask task : tasks) {
                pId.add(task.getProjectId());
            }
            Map<Long, String> projects = new HashMap<Long, String>();
            sql = "select id, name from projects where id in ( %s )";
            query = manager.createNativeQuery(String.format(sql, StringUtils.join(pId, ',')));
            result = query.getResultList();
            for (Object o : result) {
                Object[] objs = (Object[]) o;
                projects.put(((Number) objs[0]).longValue(), objs[1].toString());
            }
            for (ReportTask t : tasks) {
                t.setProjectName(projects.get(t.getProjectId()));
            }
        }
		
		logger.info("Find " + tasks.size() + " tasks! :: " + ToStringBuilder.reflectionToString(tasks));
		return tasks;
	}

	@SuppressWarnings("unchecked")
	public List<ReportTemplate> findReportTemplates(Long project_id) {
		String sql = "select * from report_templates where project_id = " + project_id;
		EntityManager manager = managerFactory.createEntityManager();
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
	
	public long findReportLineCount(ReportTemplate template, long pid, Date starTime, Date endTime) {
		 List<ReportLine> results = findReportLine(template, pid, starTime,endTime);
		 return results.size();
	}
	
	public void save(Object entity) {
		EntityManager manager = managerFactory.createEntityManager();
		EntityTransaction tx = manager.getTransaction();
		try{
			tx.begin();
			manager.merge(entity);
			tx.commit();
		}catch (PersistenceException e) {
			logger.error("error in update",e);
			if(e instanceof OptimisticLockException)
				throw new RuntimeException("Your data is not newest, someone else change it already ,please reselect and update");
			else 
				throw new RuntimeException(e.getCause().getMessage());
		}catch (Throwable t) {
			logger.error("error in update",t);
			throw new RuntimeException(t.getMessage());
		}
		finally {
			try {
				if (tx.isActive())
					tx.rollback();
				manager.close();
			} catch (Exception e) {
				logger.error("error in update,Could not close entitymanager",e);
			}
			
		}
		
	}

}
