/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;

public class ForEachEncounterTag extends BodyTagSupport {
	
	public static final long serialVersionUID = 1L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	int count = 0;
	
	List<Encounter> matchingEncs = null;
	
	// Properties accessible through tag attributes
	private Collection<Encounter> encounters;
	
	private Integer type;
	
	private Integer num = null;
	
	private String sortBy;
	
	private Boolean descending = Boolean.FALSE;
	
	private String var;
	
	/**
	 * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
	 * @should sort encounters by encounterDatetime in descending order
	 * @should pass for a patient with no encounters
	 */
	@SuppressWarnings( { "rawtypes", "unchecked" })
	@Override
	public int doStartTag() {
		if (encounters == null || encounters.isEmpty()) {
			log.debug("ForEachEncounterTag skipping body due to 'encounters' param = " + encounters);
			return SKIP_BODY;
		}
		
		//First, sort the encounters
		if (StringUtils.isEmpty(sortBy)) {
			sortBy = "encounterDatetime";
		}
		
		Comparator comp = new BeanComparator(sortBy, (descending ? new ReverseComparator(new ComparableComparator())
		        : new ComparableComparator()));
		Collections.sort((List) encounters, comp);
		
		// Next, retrieve all encounters matching the passed encounter type id, if provided.
		// If not provided, return all encounters
		matchingEncs = new ArrayList<Encounter>();
		for (Iterator<Encounter> i = encounters.iterator(); i.hasNext();) {
			if (getNum() != null && getNum() <= matchingEncs.size()) {
				break;
			}
			Encounter e = i.next();
			if (type == null || e.getEncounterType().getEncounterTypeId().intValue() == type.intValue()) {
				matchingEncs.add(e);
			}
		}
		log.debug("ForEachEncounterTag found " + matchingEncs.size() + " encounters matching type = " + type);
		
		// Return appropriate number of results
		if (matchingEncs.isEmpty()) {
			return SKIP_BODY;
		} else {
			pageContext.setAttribute("count", count);
			pageContext.setAttribute(var, matchingEncs.get(count++));
			return EVAL_BODY_BUFFERED;
		}
	}
	
	/**
	 * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
	 */
	@Override
	public int doAfterBody() throws JspException {
		if (matchingEncs.size() > count && (num == null || count < num.intValue())) {
			pageContext.setAttribute("count", count);
			pageContext.setAttribute(var, matchingEncs.get(count++));
			return EVAL_BODY_BUFFERED;
		} else {
			return SKIP_BODY;
		}
	}
	
	/**
	 * @see javax.servlet.jsp.tagext.Tag#doEndTag()
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			if (count > 0 && bodyContent != null) {
				count = 0;
				bodyContent.writeOut(bodyContent.getEnclosingWriter());
			}
			num = null;
		}
		catch (java.io.IOException e) {
			throw new JspTagException("IO Error: " + e.getMessage());
		}
		return EVAL_PAGE;
	}
	
	/**
	 * @return the descending
	 */
	public boolean isDescending() {
		return descending;
	}
	
	/**
	 * @param descending the descending to set
	 */
	public void setDescending(boolean descending) {
		this.descending = descending;
	}
	
	/**
	 * @return the encounters
	 */
	public Collection<Encounter> getEncounters() {
		return encounters;
	}
	
	/**
	 * @param encounters the encounters to set
	 */
	public void setEncounters(Collection<Encounter> encounters) {
		this.encounters = encounters;
	}
	
	/**
	 * @return the num
	 */
	public Integer getNum() {
		return num;
	}
	
	/**
	 * @param num the num to set
	 */
	public void setNum(Integer num) {
		if (num != 0) {
			this.num = num;
		} else {
			num = null;
		}
	}
	
	/**
	 * @return the sortBy
	 */
	public String getSortBy() {
		return sortBy;
	}
	
	/**
	 * @param sortBy the sortBy to set
	 */
	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}
	
	/**
	 * @return the type
	 */
	public Integer getType() {
		return type;
	}
	
	/**
	 * @param type the type to set
	 */
	public void setType(Integer type) {
		this.type = type;
	}
	
	/**
	 * @return the var
	 */
	public String getVar() {
		return var;
	}
	
	/**
	 * @param var the var to set
	 */
	public void setVar(String var) {
		this.var = var;
	}
	
}
