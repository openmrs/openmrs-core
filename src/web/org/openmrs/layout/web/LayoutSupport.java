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
package org.openmrs.layout.web;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class LayoutSupport<T extends LayoutTemplate> {

	private Log log = LogFactory.getLog(getClass());
	
	protected String defaultLayoutFormat;
	protected List<T> layoutTemplates;
	protected List<String> specialTokens;

	/**
	 * @return Returns the layoutTemplates.
	 */
	public List<T> getLayoutTemplates() {
		return layoutTemplates;
	}

	/**
	 * @param layoutTemplates The layoutTemplates to set.
	 */
	public void setLayoutTemplates(List<T> layoutTemplates) {
		this.layoutTemplates = layoutTemplates;
	}

	/**
	 * @return Returns the defaultLayoutTemplate.
	 */
	public T getDefaultLayoutTemplate() {
		return getLayoutTemplateByName(getDefaultLayoutFormat());
	}

	public T getLayoutTemplateByName(String templateName) {
		log.debug("looking for template name: " + templateName);
		
		if ( this.layoutTemplates != null && templateName != null ) {
			T ret = null;
			
			for ( T at : this.layoutTemplates ) {
				if ( at != null ) {
					if ( templateName.equalsIgnoreCase(at.getDisplayName())
							|| templateName.equalsIgnoreCase(at.getCodeName())
							|| templateName.equalsIgnoreCase(at.getCountry())) {
						ret = at;
						log.debug("Found Layout Template named " + at.getDisplayName());
					}
				}
			}
			
			return ret;
		} else {
			log.debug("No Layout Templates defined");
			return null;
		}
	}

	public T getLayoutTemplateByCodeName(String templateName) {
		if ( this.layoutTemplates != null && templateName != null ) {
			T ret = null;
			
			for ( T at : this.layoutTemplates ) {
				if ( at != null ) {
					if ( templateName.equalsIgnoreCase(at.getCodeName())) {
						ret = at;
						log.debug("Found Layout Template named " + at.getDisplayName());
					}
				}
			}
			
			return ret;
		} else {
			log.debug("No Layout Templates defined");
			return null;
		}
	}

	public T getLayoutTemplateByCountry(String templateName) {
		if ( this.layoutTemplates != null && templateName != null ) {
			T ret = null;
			
			for ( T at : this.layoutTemplates ) {
				if ( at != null ) {
					if ( templateName.equalsIgnoreCase(at.getCountry())) {
						ret = at;
						log.debug("Found Layout Template named " + at.getDisplayName());
					}
				}
			}
			
			return ret;
		} else {
			log.debug("No Layout Templates defined");
			return null;
		}
	}

	public T getLayoutTemplateByDisplayName(String templateName) {
		if ( this.layoutTemplates != null && templateName != null ) {
			T ret = null;
			
			for ( T at : this.layoutTemplates ) {
				if ( at != null ) {
					if ( templateName.equalsIgnoreCase(at.getDisplayName())) {
						ret = at;
						log.debug("Found Layout Template named " + at.getDisplayName());
					}
				}
			}
			
			return ret;
		} else {
			log.debug("No Layout Templates defined");
			return null;
		}
	}

	/**
	 * @return Returns the specialTokens.
	 */
	public List<String> getSpecialTokens() {
		return specialTokens;
	}

	/**
	 * @param specialTokens The specialTokens to set.
	 */
	public void setSpecialTokens(List<String> specialTokens) {
		this.specialTokens = specialTokens;
	}

	/**
	 * @return Returns the defaultLayoutFormat.
	 */
	public abstract String getDefaultLayoutFormat();

	/**
	 * @param defaultLayoutFormat The defaultLayoutFormat to set.
	 */
	public void setDefaultLayoutFormat(String defaultLayoutFormat) {
		this.defaultLayoutFormat = defaultLayoutFormat;
	}

}