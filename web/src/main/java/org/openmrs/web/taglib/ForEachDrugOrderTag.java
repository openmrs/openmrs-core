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
import org.openmrs.DrugOrder;

public class ForEachDrugOrderTag extends BodyTagSupport {
	
	public static final long serialVersionUID = 1L;
	
	private static final String defaultSortBy = "dateCreated";
	
	private final Log log = LogFactory.getLog(getClass());
	
	int count = 0;
	
	List<DrugOrder> matchingDrugOrders = null;
	
	// Properties accessible through tag attributes
	private Collection<DrugOrder> drugOrders;
	
	private Integer num = null;
	
	private String sortBy;
	
	private Boolean descending = Boolean.FALSE;
	
	private String var;
	
	public int doStartTag() {
		if (drugOrders == null || drugOrders.isEmpty()) {
			log.error("ForEachDrugOrderTag skipping body due to drugOrders param being null or empty: " + drugOrders);
			return SKIP_BODY;
		}
		// First retrieve all encounters matching the passed concept id, if provided.
		// If not provided, return all encounters
		matchingDrugOrders = new ArrayList<DrugOrder>();
		for (Iterator<DrugOrder> i = drugOrders.iterator(); i.hasNext();) {
			DrugOrder d = i.next();
			if (d != null) {
				// TODO: eventually we might want to have criteria, but not yet
				matchingDrugOrders.add(d);
			}
		}
		log.debug("ForEachDrugOrderTag found " + matchingDrugOrders.size() + " drug orders");
		
		// Next, sort the encounters
		if (StringUtils.isEmpty(sortBy)) {
			sortBy = defaultSortBy;
		}
		Comparator comp = new BeanComparator(sortBy, (descending ? new ReverseComparator(new ComparableComparator())
		        : new ComparableComparator()));
		try {
			Collections.sort(matchingDrugOrders, comp);
		}
		catch (ClassCastException cce) {
			log
			        .error("ForEachDrugTag unable to compare the list of drug orders passed.  Ensure they are compatible with Comparator used.");
		}
		
		// Return appropriate number of results
		if (matchingDrugOrders.isEmpty()) {
			return SKIP_BODY;
		} else {
			pageContext.setAttribute(var, matchingDrugOrders.get(count++));
			return EVAL_BODY_BUFFERED;
		}
	}
	
	/**
	 * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
	 */
	public int doAfterBody() throws JspException {
		if (matchingDrugOrders.size() > count && (num == null || count < num.intValue())) {
			pageContext.setAttribute(var, matchingDrugOrders.get(count++));
			return EVAL_BODY_BUFFERED;
		} else {
			return SKIP_BODY;
		}
	}
	
	/**
	 * @see javax.servlet.jsp.tagext.Tag#doEndTag()
	 */
	public int doEndTag() throws JspException {
		try {
			if (count > 0 && bodyContent != null) {
				count = 0;
				bodyContent.writeOut(bodyContent.getEnclosingWriter());
			}
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
	public Collection<DrugOrder> getDrugOrders() {
		return drugOrders;
	}
	
	/**
	 * @param encounters the encounters to set
	 */
	public void setDrugOrders(Collection<DrugOrder> drugOrders) {
		this.drugOrders = drugOrders;
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
		this.num = num;
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
