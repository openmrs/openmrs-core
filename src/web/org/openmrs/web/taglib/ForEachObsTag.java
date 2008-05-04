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

import java.util.*;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.collections.comparators.ReverseComparator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;


public class ForEachObsTag extends BodyTagSupport {

	public static final long serialVersionUID = 1L;
	
	private final Log log = LogFactory.getLog(getClass());
	int count = 0;
	List<Obs> matchingObs = null;
	
	// Properties accessible through tag attributes
	private Collection<Obs> obs;
	private Integer conceptId;
	private Integer num = null;
	private String sortBy;
	private Boolean descending = Boolean.FALSE;
	private String var;

	public int doStartTag() {
		
		if (obs == null || obs.isEmpty()) {
			log.error("ForEachObsTag skipping body due to obs param = " + obs);
			return SKIP_BODY;
		}
		// First retrieve all observations matching the passed concept id, if provided.
		// If not provided, return all observations
		matchingObs = new ArrayList<Obs>();
		for (Iterator<Obs> i=obs.iterator(); i.hasNext();) {
			Obs o = i.next();
			if (conceptId == null || (o.getConcept() != null && o.getConcept().getConceptId().intValue() == conceptId.intValue())) {
				matchingObs.add(o);
			}
		}
		log.debug("ForEachObsTag found " + matchingObs.size() + " observations matching conceptId = " + conceptId);
		
		// Next, sort these observations
		if (sortBy == null || sortBy.equals("")) {
			sortBy = "obsDatetime";
		}
		Comparator comp = new BeanComparator(sortBy, (descending ? new ReverseComparator(new ComparableComparator()) : new ComparableComparator()));
		Collections.sort(matchingObs, comp);

		// Return appropriate number of results
		if (matchingObs.isEmpty()) {
			return SKIP_BODY;
		} else {
        	pageContext.setAttribute(var, matchingObs.get(count++));
            return EVAL_BODY_BUFFERED;
		}
	}

	/**
	 * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
	 */
	public int doAfterBody() throws JspException {
        if(matchingObs.size() > count && (num == null || count < num.intValue())) {
        	pageContext.setAttribute(var, matchingObs.get(count++));
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
        catch(java.io.IOException e) {
            throw new JspTagException("IO Error: " + e.getMessage());
        }
        return EVAL_PAGE;
	}

	/**
	 * @return the obs
	 */
	public Collection<Obs> getObs() {
		return obs;
	}

	/**
	 * @param obs the obs to set
	 */
	public void setObs(Collection<Obs> obs) {
		this.obs = obs;
	}

	/**
	 * @return the conceptId
	 */
	public Integer getConceptId() {
		return conceptId;
	}

	/**
	 * @param conceptId the conceptId to set
	 */
	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
	}

	/**
	 * @return the Num
	 */
	public Integer getNum() {
		return num;
	}

	/**
	 * @param Num the Num to set
	 */
	public void setNum(Integer num) {
		this.num = num;
	}

	/**
	 * @param var the var to set
	 */
	public void setVar(String var) {
		this.var = var;
	}

	/**
	 * @return the descending
	 */
	public Boolean getDescending() {
		return descending;
	}

	/**
	 * @param descending the descending to set
	 */
	public void setDescending(Boolean descending) {
		this.descending = descending;
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
}
