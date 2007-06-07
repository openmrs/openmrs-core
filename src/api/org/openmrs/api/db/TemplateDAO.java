package org.openmrs.api.db;

import java.util.List;

import org.openmrs.notification.Template;

/**
 * Message template related database functions
 * 
 * @version 1.0
 */
public interface TemplateDAO {

	/**
	 * Get all message templates
	 * 
	 * @throws DAOException
	 */
	public List<Template> getTemplates() throws DAOException;

	/**
	 * Get message template by id
	 * 
	 * @param id internal message template identifier
	 * @return message template with given internal identifier
	 * @throws DAOException
	 */
	public Template getTemplate(Integer id) throws DAOException;

	/**
	 * Get message template by name
	 * 
	 * @param name	message template name
	 * @return message template with given name
	 * @throws DAOException
	 */
	public List<Template> getTemplatesByName(String name) throws DAOException;
	
	/** 
	 * Create new template.
	 * 
	 * @param template
	 * @throws DAOException
	 */
	public void createTemplate(Template template) throws DAOException;

	/**
	 * Update existing template.
	 * 
	 * @param template
	 * @throws DAOException
	 */
	public void updateTemplate(Template template) throws DAOException;
	
	/**
	 * Delete existing template.
	 * 
	 * @param template
	 * @throws DAOException
	 */
	public void deleteTemplate(Template template) throws DAOException;
	
}
