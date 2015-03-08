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
import org.openmrs.Visit;

public class ForEachVisitTag extends BodyTagSupport {
	
	public static final long serialVersionUID = 1L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	int count = 0;
	
	List<Visit> matchingVisits = null;
	
	// Properties accessible through tag attributes
	private Collection<Visit> visits;
	
	private Integer type;
	
	private Integer num = null;
	
	private String sortBy;
	
	private Boolean descending = Boolean.FALSE;
	
	private String var;
	
	@Override
	public int doStartTag() {
		if (visits == null || visits.isEmpty()) {
			log.debug("ForEachVisitTag skipping body due to 'visits' param = " + visits);
			return SKIP_BODY;
		}
		// First retrieve all visits matching the passed visit type id, if provided.
		// If not provided, return all visits
		matchingVisits = new ArrayList<Visit>();
		for (Iterator<Visit> i = visits.iterator(); i.hasNext();) {
			Visit e = i.next();
			if (type == null || e.getVisitType().getVisitTypeId().intValue() == type.intValue()) {
				matchingVisits.add(e);
			}
		}
		log.debug("ForEachVisitTag found " + matchingVisits.size() + " visits matching type = " + type);
		
		// Next, sort the visits
		if (StringUtils.isEmpty(sortBy)) {
			sortBy = "visitDatetime";
		}
		Comparator comp = new BeanComparator(sortBy, (descending ? new ReverseComparator(new ComparableComparator())
		        : new ComparableComparator()));
		Collections.sort(matchingVisits, comp);
		
		// Return appropriate number of results
		if (matchingVisits.isEmpty()) {
			return SKIP_BODY;
		} else {
			pageContext.setAttribute(var, matchingVisits.get(count++));
			return EVAL_BODY_BUFFERED;
		}
	}
	
	/**
	 * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
	 */
	@Override
	public int doAfterBody() throws JspException {
		if (matchingVisits.size() > count && (num == null || count < num.intValue())) {
			pageContext.setAttribute("count", count);
			pageContext.setAttribute(var, matchingVisits.get(count++));
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
	 * @return the visits
	 */
	public Collection<Visit> getVisits() {
		return visits;
	}
	
	/**
	 * @param visits the visits to set
	 */
	public void setVisits(Collection<Visit> visits) {
		this.visits = visits;
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
