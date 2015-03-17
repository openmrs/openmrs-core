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

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;

public class ObsTableWidget extends TagSupport {
	
	private static final long serialVersionUID = 14352344444L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	/*
	 * pipe-separated list that could be:
	 *   a concept id (e.g. "5089")
	 *   a concept name (e.g. "name:CD4 COUNT")
	 *   a concept set, by id or name (e.g. "set:5089" or "set.en:LAB TESTS")
	 */
	private String concepts;
	
	private Collection<Obs> observations;
	
	private boolean sortDescending = true;
	
	private boolean orientVertical = true;
	
	private Boolean showEmptyConcepts = true;
	
	private Boolean showConceptHeader = true;
	
	private Boolean showDateHeader = true;
	
	private Boolean combineEqualResults = true;
	
	private String id;
	
	private String cssClass;
	
	private Date fromDate;
	
	private Date toDate;
	
	private Integer limit = 0;
	
	private String conceptLink = null;
	
	//private String combineBy = "day";
	
	public ObsTableWidget() {
	}
	
	public String getConcepts() {
		return concepts;
	}
	
	public void setConcepts(String concepts) {
		if (concepts == null || concepts.length() == 0) {
			return;
		}
		this.concepts = concepts;
	}
	
	public String getConceptLink() {
		return conceptLink;
	}
	
	public void setConceptLink(String conceptLink) {
		this.conceptLink = conceptLink;
	}
	
	public String getCssClass() {
		return cssClass;
	}
	
	public void setCssClass(String cssClass) {
		if (cssClass == null || cssClass.length() == 0) {
			return;
		}
		this.cssClass = cssClass;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		if (id == null || id.length() == 0) {
			return;
		}
		this.id = id;
	}
	
	public Boolean getShowEmptyConcepts() {
		return showEmptyConcepts;
	}
	
	public void setShowEmptyConcepts(Boolean showEmptyConcepts) {
		if (showEmptyConcepts == null) {
			return;
		}
		this.showEmptyConcepts = showEmptyConcepts;
	}
	
	public Boolean getShowConceptHeader() {
		return showConceptHeader;
	}
	
	public void setShowConceptHeader(Boolean showHeader) {
		if (showHeader == null) {
			return;
		}
		this.showConceptHeader = showHeader;
	}
	
	public Boolean getShowDateHeader() {
		return showDateHeader;
	}
	
	public void setShowDateHeader(Boolean showDateHeader) {
		if (showDateHeader == null) {
			return;
		}
		this.showDateHeader = showDateHeader;
	}
	
	public String getSort() {
		return sortDescending ? "desc" : "asc";
	}
	
	public void setSort(String sort) {
		if (sort == null || sort.length() == 0) {
			return;
		}
		sortDescending = !sort.equals("asc");
	}
	
	public String getOrientation() {
		return orientVertical ? "vertical" : "horizontal";
	}
	
	public void setOrientation(String orientation) {
		if (orientation == null || orientation.length() == 0) {
			return;
		}
		orientVertical = !orientation.equals("horizontal");
	}
	
	public Collection<Obs> getObservations() {
		return observations;
	}
	
	public void setObservations(Collection<Obs> observations) {
		this.observations = observations;
	}
	
	public Date getFromDate() {
		return fromDate;
	}
	
	public void setFromDate(Date fromDate) {
		if (fromDate == null) {
			return;
		}
		this.fromDate = fromDate;
	}
	
	public Date getToDate() {
		return toDate;
	}
	
	public void setToDate(Date toDate) {
		if (toDate == null) {
			return;
		}
		this.toDate = toDate;
	}
	
	public Integer getLimit() {
		return limit;
	}
	
	public void setLimit(Integer limit) {
		if (limit == null) {
			return;
		}
		this.limit = limit;
	}
	
	public Boolean getCombineEqualResults() {
		return combineEqualResults;
	}
	
	public void setCombineEqualResults(Boolean combineEqualResults) {
		this.combineEqualResults = combineEqualResults;
	}
	
	public int doStartTag() {
		Locale loc = Context.getLocale();
		DateFormat df = Context.getDateFormat();
		//DateFormat.getDateInstance(DateFormat.SHORT, loc);
		
		// determine which concepts we care about
		List<Concept> conceptList = new ArrayList<Concept>();
		Set<Integer> conceptIds = new HashSet<Integer>();
		ConceptService cs = Context.getConceptService();
		for (StringTokenizer st = new StringTokenizer(concepts, "|"); st.hasMoreTokens();) {
			String s = st.nextToken().trim();
			log.debug("looking at " + s);
			boolean isSet = s.startsWith("set:");
			if (isSet) {
				s = s.substring(4).trim();
			}
			Concept c = null;
			if (s.startsWith("name:")) {
				String name = s.substring(5).trim();
				c = cs.getConceptByName(name);
			} else {
				try {
					c = cs.getConcept(Integer.valueOf(s.trim()));
				}
				catch (Exception ex) {
					log.error("Error during concept c getConcept", ex);
				}
			}
			if (c != null) {
				if (isSet) {
					List<Concept> inSet = cs.getConceptsByConceptSet(c);
					for (Concept con : inSet) {
						if (!conceptIds.contains(con.getConceptId())) {
							conceptList.add(con);
							conceptIds.add(con.getConceptId());
						}
					}
				} else {
					if (!conceptIds.contains(c.getConceptId())) {
						conceptList.add(c);
						conceptIds.add(c.getConceptId());
					}
				}
			}
			log.debug("conceptList == " + conceptList);
		}
		
		// organize obs of those concepts by Date and Concept
		Set<Integer> conceptsWithObs = new HashSet<Integer>();
		SortedSet<Date> dates = new TreeSet<Date>();
		Map<String, List<Obs>> groupedObs = new HashMap<String, List<Obs>>(); // key is conceptId + "." + date
		for (Obs o : observations) {
			Integer conceptId = o.getConcept().getConceptId();
			if (conceptIds.contains(conceptId)) {
				Date thisDate = o.getObsDatetime();
				// TODO: allow grouping by day/week/month/etc
				if ((fromDate != null && thisDate.compareTo(fromDate) < 0)
				        || (toDate != null && thisDate.compareTo(toDate) > 0)) {
					continue;
				}
				dates.add(thisDate);
				String key = conceptId + "." + thisDate;
				List<Obs> group = groupedObs.get(key);
				if (group == null) {
					group = new ArrayList<Obs>();
					groupedObs.put(key, group);
				}
				group.add(o);
				conceptsWithObs.add(conceptId);
			}
		}
		
		if (!showEmptyConcepts) {
			for (Iterator<Concept> i = conceptList.iterator(); i.hasNext();) {
				if (!conceptsWithObs.contains(i.next().getConceptId())) {
					i.remove();
				}
			}
		}
		
		List<Date> dateOrder = new ArrayList<Date>(dates);
		if (sortDescending) {
			Collections.reverse(dateOrder);
		}
		
		if (limit > 0 && limit < dateOrder.size()) {
			if (!sortDescending) {
				dateOrder = dateOrder.subList(dateOrder.size() - limit, dateOrder.size());
			} else {
				dateOrder = dateOrder.subList(0, limit);
			}
		}
		
		StringBuilder ret = new StringBuilder();
		ret.append("<table");
		if (id != null) {
			ret.append(" id=\"" + id + "\"");
		}
		if (cssClass != null) {
			ret.append(" class=\"" + cssClass + "\"");
		}
		ret.append(">");
		
		if (orientVertical) {
			if (showConceptHeader) {
				ret.append("<tr>");
				ret.append("<th></th>");
				for (Concept c : conceptList) {
					showConceptHeader(loc, ret, c);
				}
				ret.append("</tr>");
			}
			for (Date date : dateOrder) {
				ret.append("<tr>");
				if (showDateHeader) {
					ret.append("<th>" + df.format(date) + "</th>");
				}
				for (Concept c : conceptList) {
					showConcept(loc, groupedObs, ret, date, c);
				}
				ret.append("</tr>");
			}
			
		} else { // horizontal
			if (showDateHeader) {
				ret.append("<tr>");
				ret.append("<th></th>");
				for (Date date : dateOrder) {
					ret.append("<th>" + df.format(date) + "</th>");
				}
			}
			for (Concept c : conceptList) {
				ret.append("<tr>");
				if (showConceptHeader) {
					showConceptHeader(loc, ret, c);
				}
				for (Date date : dateOrder) {
					showConcept(loc, groupedObs, ret, date, c);
				}
				ret.append("</tr>");
			}
		}
		ret.append("</table>");
		
		try {
			JspWriter w = pageContext.getOut();
			w.println(ret);
		}
		catch (IOException ex) {
			log.error("Error while starting ObsTableWidget tag", ex);
		}
		return SKIP_BODY;
	}
	
	private void showConcept(Locale loc, Map<String, List<Obs>> groupedObs, StringBuilder ret, Date date, Concept c) {
		ret.append("<td align=\"center\">");
		String key = c.getConceptId() + "." + date;
		List<Obs> list = groupedObs.get(key);
		showObservationsIfExists(loc, ret, list);
		ret.append("</td>");
	}
	
	private void showObservationsIfExists(Locale loc, StringBuilder ret, List<Obs> list) {
		if (list != null) {
			if (combineEqualResults) {
				Collection<String> unique = new LinkedHashSet<String>();
				for (Obs obs : list) {
					unique.add(obs.getValueAsString(loc));
				}
				for (String s : unique) {
					ret.append(s).append("<br/>");
				}
			} else {
				for (Obs obs : list) {
					ret.append(obs.getValueAsString(loc)).append("<br/>");
				}
			}
		}
	}
	
	private void showConceptHeader(Locale loc, StringBuilder ret, Concept c) {
		String name = getConceptName(loc, c);
		ret.append("<th>");
		if (conceptLink != null) {
			ret.append("<a href=\"" + conceptLink + "conceptId=" + c.getConceptId() + "\">");
		}
		ret.append(name);
		if (conceptLink != null) {
			ret.append("</a>");
		}
		ret.append("</th>");
	}
	
	private String getConceptName(Locale loc, Concept c) {
		ConceptName cn = c.getName();
		return cn.getName();
	}
	
	public int doEndTag() {
		concepts = null;
		observations = null;
		sortDescending = true;
		orientVertical = true;
		showEmptyConcepts = true;
		showConceptHeader = true;
		showDateHeader = true;
		combineEqualResults = true;
		id = null;
		cssClass = null;
		showEmptyConcepts = true;
		showConceptHeader = true;
		fromDate = null;
		toDate = null;
		limit = 0;
		conceptLink = null;
		return EVAL_PAGE;
	}
	
}
