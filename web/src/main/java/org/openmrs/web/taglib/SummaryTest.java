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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

public class SummaryTest extends TagSupport {
	
	private static final long serialVersionUID = 102731753333L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	private Collection<Obs> observations;
	
	private Collection<Encounter> encounters;
	
	private String var;
	
	private String ifTrue;
	
	private String ifFalse;
	
	public int doStartTag() {
		Boolean ret = false;
		if ((ifTrue == null || ifTrue.length() == 0) && (ifFalse == null || ifFalse.length() == 0)) {
			ret = true;
		} else {
			if (ifTrue != null && ifTrue.length() > 0) {
				ret |= evaluate(ifTrue);
			}
			if (ifFalse != null && ifFalse.length() > 0) {
				ret |= !evaluate(ifFalse);
			}
		}
		
		pageContext.setAttribute(var, ret);
		return SKIP_BODY;
	}
	
	public int doEndTag() {
		observations = null;
		var = null;
		ifTrue = null;
		ifFalse = null;
		return EVAL_PAGE;
	}
	
	private boolean evaluate(String expr) {
		expr = expr.trim();
		log.debug("evaluate " + expr);
		List<String> commands = new ArrayList<String>();
		{
			StringBuilder command = new StringBuilder();
			String[] lines = expr.split("\n");
			for (String line : lines) {
				if (line.trim().startsWith("!") && command.length() > 0) {
					commands.add(command.toString());
					command = new StringBuilder();
				}
				command.append(line.trim());
				command.append("\n");
			}
			if (command.length() > 0) {
				commands.add(command.toString());
			}
		}
		boolean andMode = true;
		List<Boolean> commandResults = new ArrayList<Boolean>();
		for (String s : commands) {
			String command = (new StringTokenizer(s.toUpperCase())).nextToken();
			if (command.equals("!AND")) {
				andMode = true;
			} else if (command.equals("!OR")) {
				andMode = false;
			} else if (command.equals("!OBSCHECK")) {
				s = s.substring("!OBSCHECK".length()).trim();
				commandResults.add(handleObsCheck(s));
			} else {
				throw new RuntimeException("Don't know how to handle command " + command + "\n" + s);
			}
		}
		
		boolean ret = andMode ? true : false;
		for (Boolean b : commandResults) {
			if (andMode) {
				ret &= b;
			} else {
				ret |= b;
			}
		}
		return ret;
	}
	
	private boolean handleObsCheck(String expr) {
		log.debug("handleObsCheck(" + expr + ")");
		expr = expr.trim();
		if (expr.length() == 0) {
			return true;
		}
		
		Map<String, String> args = new HashMap<String, String>();
		String[] lines = expr.split("\n");
		for (String s : lines) {
			s = s.trim();
			int ind = s.indexOf(':');
			String key = s.substring(0, ind).toLowerCase();
			String val = s.substring(ind + 1);
			args.put(key, val);
			log.debug(key + " -> " + val);
		}
		
		PatientSetService.TimeModifier test = PatientSetService.TimeModifier.ANY;
		Set<Concept> conceptsOfInterest = new HashSet<Concept>();
		Date fromDate = null;
		Date toDate = null;
		if (args.containsKey("test")) {
			test = PatientSetService.TimeModifier.valueOf(args.get("test").trim().toUpperCase());
		}
		String conceptName = args.get("concept");
		if (conceptName == null) {
			throw new IllegalArgumentException("You must specify a concept");
		}
		{
			ConceptService cs = Context.getConceptService();
			boolean isSet = conceptName.startsWith("set:");
			if (isSet) {
				conceptName = conceptName.substring("set:".length());
			}
			Concept c = cs.getConceptByName(conceptName);
			if (c == null) {
				log.warn("Can't find concept " + conceptName);
			} else {
				if (isSet) {
					conceptsOfInterest.addAll(cs.getConceptsByConceptSet(c));
				} else {
					conceptsOfInterest.add(c);
				}
			}
		}
		
		if (args.containsKey("timespan")) {
			// [last|next defaults to last] [# defaults to 1] [m|d|y defaults to m]
			boolean inPast = true;
			int timeUnit = Calendar.MONTH;
			Integer time = 1;
			String ts = args.get("timespan");
			String[] s = ts.split(" ");
			for (String str : s) {
				if (str.length() == 0) {
					continue;
				}
				if (str.startsWith("l")) {
					inPast = true;
				} else if (str.startsWith("n")) {
					inPast = false;
				} else if (str.startsWith("m")) {
					timeUnit = Calendar.MONTH;
				} else if (str.startsWith("d")) {
					timeUnit = Calendar.DAY_OF_MONTH;
				} else if (str.startsWith("y")) {
					timeUnit = Calendar.YEAR;
				} else {
					time = Integer.valueOf(str);
				}
			}
			Calendar c = Calendar.getInstance();
			c.add(timeUnit, (inPast ? -1 : 1) * time);
			if (inPast) {
				fromDate = c.getTime();
			} else {
				toDate = c.getTime();
			}
		}
		
		log.debug("test:" + test);
		log.debug("concepts of interest:" + conceptsOfInterest);
		log.debug("fromDate:" + fromDate);
		log.debug("toDate:" + toDate);
		
		List<Obs> obsThatMatter = new ArrayList<Obs>();
		for (Obs o : observations) {
			if (conceptsOfInterest.contains(o.getConcept())
			        && (fromDate == null || OpenmrsUtil.compare(fromDate, o.getObsDatetime()) <= 0)
			        && (toDate == null || OpenmrsUtil.compare(o.getObsDatetime(), toDate) <= 0)) {
				obsThatMatter.add(o);
			}
		}
		log.debug("obsThatMatter (" + obsThatMatter.size() + "): " + obsThatMatter);
		if (test == PatientSetService.TimeModifier.ANY) {
			return obsThatMatter.size() > 0;
		} else if (test == PatientSetService.TimeModifier.NO) {
			return obsThatMatter.size() == 0;
		} else {
			throw new RuntimeException("Can't handle test:" + test);
		}
	}
	
	public String getIfFalse() {
		return ifFalse;
	}
	
	public void setIfFalse(String ifFalse) {
		this.ifFalse = ifFalse;
	}
	
	public String getIfTrue() {
		return ifTrue;
	}
	
	public void setIfTrue(String ifTrue) {
		this.ifTrue = ifTrue;
	}
	
	public Collection<Obs> getObservations() {
		return observations;
	}
	
	public void setObservations(Collection<Obs> observations) {
		this.observations = observations;
	}
	
	public String getVar() {
		return var;
	}
	
	public void setVar(String var) {
		this.var = var;
	}
	
	public Collection<Encounter> getEncounters() {
		return encounters;
	}
	
	public void setEncounters(Collection<Encounter> encounters) {
		this.encounters = encounters;
	}
	
}
