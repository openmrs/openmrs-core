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
package org.openmrs.web.taglib;

import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.core.LoopTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;

/**
 * Loops over the display attributes for a person. If 'type' is 'patient', only patient display
 * attributes are returned. If 'type' is 'user', only user display attributes are returned. This
 * list is maintained with global properties: patient.displayAttributeTypes and
 * user.displayAttributeTypes. Both are comma delimited lists of either PersonAttributeType names or
 * ids.
 */
public class ForEachDisplayAttributeTag extends LoopTagSupport {
	
	public static final long serialVersionUID = 123230012322221123L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	private String personType = "";
	
	private String displayType = "listing";
	
	private Iterator<PersonAttributeType> attrTypes;
	
	public void prepare() throws JspTagException {
		
		attrTypes = null;
		
		try {
			PersonService ps = Context.getPersonService();
			List<PersonAttributeType> types = ps.getPersonAttributeTypes(getPersonType(), getDisplayType());
			
			attrTypes = types.iterator();
			setVarStatus("varStatus");
			
		}
		catch (Exception e) {
			log.error("Error getting attributes", e);
		}
	}
	
	@Override
	protected boolean hasNext() throws JspTagException {
		if (attrTypes == null)
			return false;
		return attrTypes.hasNext();
	}
	
	@Override
	protected Object next() throws JspTagException {
		if (attrTypes == null)
			throw new JspTagException("The attr iterator is null");
		return attrTypes.next();
	}
	
	@Override
	public void release() {
		// Clean out the variables
		personType = "";
		attrTypes = null;
	}
	
	/**
	 * @return the type
	 */
	public String getPersonType() {
		return personType;
	}
	
	/**
	 * @param type the type to set
	 */
	public void setPersonType(String type) {
		this.personType = type.toLowerCase();
	}
	
	/**
	 * @return the displayType
	 */
	public String getDisplayType() {
		return displayType;
	}
	
	/**
	 * @param displayType the displayType to set
	 */
	public void setDisplayType(String displayType) {
		this.displayType = displayType.toLowerCase();
	}
	
}
