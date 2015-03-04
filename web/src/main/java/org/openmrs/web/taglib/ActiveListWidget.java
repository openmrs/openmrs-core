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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.util.OpenmrsUtil;

public class ActiveListWidget extends TagSupport {
	
	private static final long serialVersionUID = 14352322222L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	private Collection<Obs> observations;
	
	private Boolean showDate = false;
	
	private String displayStyle = "ol";
	
	private Date onDate;
	
	// pipe-separated lists of "conceptId" or "name:CONCEPT NAME". starting with "set:" means treat this as a set.
	private String addConcept;
	
	private String removeConcept;
	
	private String otherGroupedConcepts;
	
	public ActiveListWidget() {
	}
	
	public int doStartTag() {
		UserContext userContext = Context.getUserContext();
		
		Locale loc = userContext.getLocale();
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, loc);
		
		Set<Concept> addConceptList = OpenmrsUtil.conceptSetHelper(addConcept);
		Set<Concept> removeConceptList = OpenmrsUtil.conceptSetHelper(removeConcept);
		List<Concept> otherConceptList = OpenmrsUtil.conceptListHelper(otherGroupedConcepts);
		
		boolean doObsGroups = otherConceptList.size() > 0;
		
		if (onDate == null) {
			onDate = new Date();
		}
		
		// maps Concept to the date that became active
		Map<Concept, Obs> activeList = new HashMap<Concept, Obs>();
		for (Obs o : observations) {
			// skip observations in the future
			if (OpenmrsUtil.compare(o.getObsDatetime(), onDate) > 0) {
				continue;
			}
			Concept c = o.getConcept();
			Concept toDo = o.getValueCoded();
			if (toDo == null) {
				toDo = c;
			}
			if (addConceptList.contains(o.getConcept())) {
				Date newActiveDate = o.getObsDatetime();
				Obs tmp = activeList.get(c);
				Date currentActiveDate = tmp == null ? null : tmp.getObsDatetime();
				if (currentActiveDate == null || newActiveDate.compareTo(currentActiveDate) < 0) {
					activeList.put(toDo, o);
				}
			} else if (removeConceptList.contains(o.getConcept())) {
				activeList.remove(toDo);
			}
		}
		List<Map.Entry<Concept, Obs>> ordered = new ArrayList<Map.Entry<Concept, Obs>>(activeList.entrySet());
		Collections.sort(ordered, new Comparator<Map.Entry<Concept, Obs>>() {
			
			public int compare(Map.Entry<Concept, Obs> left, Map.Entry<Concept, Obs> right) {
				return left.getValue().getObsDatetime().compareTo(right.getValue().getObsDatetime());
			}
		});
		
		Map<Obs, Collection<Obs>> obsGroups = new HashMap<Obs, Collection<Obs>>();
		if (doObsGroups) {
			for (Obs o : activeList.values()) {
				if (o.isObsGrouping()) {
					obsGroups.put(o, o.getGroupMembers());
				}
			}
		}
		
		StringBuilder sb = new StringBuilder();
		String before = "";
		String after = "";
		String obsGroupHeader = "";
		String beforeItem = "";
		String afterItem = "";
		String obsGroupItemSeparator = "";
		
		if ("ol".equals(displayStyle) || "ul".equals(displayStyle)) {
			before = "<" + displayStyle + ">";
			after = "</" + displayStyle + ">";
			beforeItem = "<li>";
			afterItem = "</li>";
			obsGroupItemSeparator = ", ";
			
		} else if (displayStyle.startsWith("separator:")) {
			afterItem = displayStyle.substring(displayStyle.indexOf(":") + 1);
			obsGroupItemSeparator = " ";
			
		} else if ("table".equals(displayStyle)) {
			before = "<table>";
			after = "</table>";
			beforeItem = "<tr><td>";
			afterItem = "</td></tr>";
			obsGroupItemSeparator = "</td><td>";
			if (doObsGroups) {
				StringBuilder s = new StringBuilder();
				s.append("<tr><th></th>");
				for (Concept c : otherConceptList) {
					ConceptName cn = c.getBestShortName(loc);
					s.append("<th><small>" + cn.getName() + "</small></th>");
				}
				s.append("</tr>");
				obsGroupHeader = s.toString();
			}
			
		} else {
			throw new RuntimeException("Unknown displayStyle: " + displayStyle);
		}
		
		if (ordered.size() > 0) {
			sb.append(before);
			sb.append(obsGroupHeader);
			for (Map.Entry<Concept, Obs> e : ordered) {
				sb.append(beforeItem);
				sb.append(e.getKey().getName(loc, false).getName());
				if (showDate) {
					sb.append(" ").append(df.format(e.getValue().getObsDatetime()));
				}
				if (doObsGroups) {
					Collection<Obs> obsGroup = obsGroups.get(e.getValue());
					for (Concept c : otherConceptList) {
						sb.append(obsGroupItemSeparator);
						if (obsGroup != null) {
							for (Obs o : obsGroup) {
								if (c.equals(o.getConcept())) {
									sb.append(o.getValueAsString(loc));
									break;
								}
							}
						}
					}
				}
				sb.append(afterItem);
			}
			sb.append(after);
		}
		
		try {
			JspWriter w = pageContext.getOut();
			w.println(sb);
		}
		catch (IOException ex) {
			log.error("Error while writing to JSP", ex);
		}
		
		return SKIP_BODY;
	}
	
	public int doEndTag() {
		observations = null;
		addConcept = null;
		removeConcept = null;
		otherGroupedConcepts = null;
		showDate = false;
		displayStyle = "ol";
		onDate = null;
		return EVAL_PAGE;
	}
	
	// getters and setters
	
	public String getAddConcept() {
		return addConcept;
	}
	
	public void setAddConcept(String addConcept) {
		this.addConcept = addConcept;
	}
	
	public String getRemoveConcept() {
		return removeConcept;
	}
	
	public void setRemoveConcept(String removeConcept) {
		this.removeConcept = removeConcept;
	}
	
	public String getDisplayStyle() {
		return displayStyle;
	}
	
	public void setDisplayStyle(String displayStyle) {
		if (displayStyle == null || displayStyle.length() == 0) {
			return;
		}
		this.displayStyle = displayStyle;
	}
	
	public Collection<Obs> getObservations() {
		return observations;
	}
	
	public void setObservations(Collection<Obs> observations) {
		this.observations = observations;
	}
	
	public Boolean getShowDate() {
		return showDate;
	}
	
	public void setShowDate(Boolean showDate) {
		if (showDate == null) {
			return;
		}
		this.showDate = showDate;
	}
	
	public Date getOnDate() {
		return onDate;
	}
	
	public void setOnDate(Date onDate) {
		if (onDate == null) {
			return;
		}
		this.onDate = onDate;
	}
	
	public String getOtherGroupedConcepts() {
		return otherGroupedConcepts;
	}
	
	public void setOtherGroupedConcepts(String otherGroupedConcepts) {
		if (otherGroupedConcepts == null || otherGroupedConcepts.length() == 0) {
			return;
		}
		this.otherGroupedConcepts = otherGroupedConcepts;
	}
	
}
