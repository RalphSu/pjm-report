package org.pjm2.report.internal;

import javax.persistence.EntityManager;

public class ForumDao extends AbstractDAO {

	public ForumDao(EntityManager manager) {
		super(manager);
	}
	
	@Override
	protected String getFiledsSql() {
		return 
		"select n.id, n.image_date, t.column_name, f.body from forums as n, forum_fields as f, forum_classifieds as c, templates as t where "
		+ " n.id in (%s) and n.id = f.forums_id and f.forum_classifieds_id = c.id and c.template_id = t.id order by n.image_date  ASC";
	}


	@Override
	protected String getItemByClassifieldSql() {
		return "select n.id from forums as n where n.classified = '%s' ";
	}


	@Override
	protected String getItemByDateSql() {
		return
		"select n.id from forums as n, forum_fields as f, forum_classifieds as c where "
		+ "n.id = f.forums_id and f.forum_classifieds_id = c.id and c.id = %d and n.classified = '%s' ";
	}


	@Override
	protected String getDateFieldSql() {
		return "select c.id from forum_classifieds as c, templates as t where c.classified = '%s' and c.template_id = t.id and t.column_name='日期'";
	}

	@Override
	protected String getColumnNameSql() {
		return "select t.column_name from forum_classifieds as c, templates as t where c.classified = '%s' and c.template_id = t.id ";
	}
}
