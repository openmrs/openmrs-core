package org.openmrs.api.db.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.SimpleExpression;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.TemplateDAO;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.hibernate.HibernatePatientDAO;
import org.openmrs.notification.Template;

public class HibernateTemplateDAO implements TemplateDAO {

	protected final static Log log = LogFactory.getLog(HibernatePatientDAO.class);

	private Context context;
	
	public HibernateTemplateDAO() { }

	public HibernateTemplateDAO(Context context) { 
		this.context = context;
	}

	
	public List<Template> getTemplates() {
		log.info("Getting all templates from the database");
		List<Template> templates = new ArrayList<Template>();
		Session session = HibernateUtil.currentSession();
		
		try { 
			HibernateUtil.beginTransaction();
			templates = session.createQuery("from Template").list();
			HibernateUtil.commitTransaction();
		} catch (Exception e) { 
			log.error("Exception getting all templates", e);
		} finally { 
			session.close();
		}
		return templates;
	}
	
	

	public Template getTemplate(Integer id) {
		log.info("Get template " + id);
		Template template = new Template();
		Session session = HibernateUtil.currentSession();
		try { 
			HibernateUtil.beginTransaction();
			template = (Template) session.get(Template.class, id);
			HibernateUtil.commitTransaction();
		} catch (Exception e ) { 
			log.error("Exception getting template by id " + id, e);
		} finally { 
			session.close();
		}
		return template;
	}
	
	public List getTemplatesByName(String name) {
		log.info("Get template " + name);
		List<Template> templates = new ArrayList<Template>();
		Session session = HibernateUtil.currentSession();	
		try { 
			
			HibernateUtil.beginTransaction();
			templates = session.createQuery("from Template as template where template.name = ?")
									.setString(0, name)
									.list();
			HibernateUtil.commitTransaction();		
		} catch (Exception e) { 
			log.error("Exception getting templates by name " + name, e);
		} finally { 
			session.close();
		}
		return templates;
	}

	
	public void createTemplate(Template template) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.saveOrUpdate(template);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e.getMessage());
		} 
		finally { 
			session.close();
		}
	}


	public void updateTemplate(Template template) throws DAOException {
		if (template.getId() == null) { 
			createTemplate(template);
		}
		else {
			Session session = HibernateUtil.currentSession();
			try {
				HibernateUtil.beginTransaction();
				template = (Template)session.merge(template);
				session.saveOrUpdate(template);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e.getMessage());
			}
			finally { 
				session.close();
			}
		}
	}
	
	public void deleteTemplate(Template template) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(template);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e.getMessage());
		}
		finally { 
			session.close();
		}
	}	
	

	
}
