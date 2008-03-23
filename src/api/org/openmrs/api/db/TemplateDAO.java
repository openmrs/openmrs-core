/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
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
