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
package org.openmrs.web.controller.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortSearchHistory;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.reporting.PatientSearchReportObject;
import org.openmrs.web.WebConstants;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;

public class CohortBuilderController implements Controller {

	protected final Log log = LogFactory.getLog(getClass());
	
	private String formView;
	private String successView;
	private List<String> links;
	
	public CohortBuilderController() { }
	
	public String getFormView() {
		return formView;
	}

	public void setFormView(String formView) {
		this.formView = formView;
	}

	public String getSuccessView() {
		return successView;
	}

	public void setSuccessView(String successView) {
		this.successView = successView;
	}
	
	public List<String> getLinks() {
		return links;
	}

	public void setLinks(List<String> links) {
		this.links = links;
	}
	
	private void setMySearchHistory(HttpServletRequest request, CohortSearchHistory history) {
		Context.setVolatileUserData("CohortBuilderSearchHistory", history);
	}
	
	private CohortSearchHistory getMySearchHistory(HttpServletRequest request) {
		return (CohortSearchHistory) Context.getVolatileUserData("CohortBuilderSearchHistory");
	}

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = new HashMap<String, Object>();
		if (Context.isAuthenticated()) {
			CohortSearchHistory history = getMySearchHistory(request);
			if (history == null) {
				history = new CohortSearchHistory();
				setMySearchHistory(request, history);
			}
			List<Shortcut> shortcuts = new ArrayList<Shortcut>();
			String shortcutProperty = Context.getAdministrationService().getGlobalProperty("cohort.cohortBuilder.shortcuts");
			if (shortcutProperty != null && shortcutProperty.length() > 0) {
				String[] shortcutSpecs = shortcutProperty.split(";");
				for (int i = 0; i < shortcutSpecs.length; ++i) {
					try {
						shortcuts.add(new Shortcut(shortcutSpecs[i]));
					} catch (Exception ex) {
						log.error("Exception trying to create filter from shortcut", ex);
					}
				}
			}
			
			ConceptService cs = Context.getConceptService();
			// TODO: generalize this beyond PIH data model
			List<Concept> orderStopReasons = new ArrayList<Concept>();
			{
				Concept c = cs.getConceptByName("REASON ORDER STOPPED");
				if (c != null)
					orderStopReasons.addAll(cs.getConceptsByConceptSet(c));
				if (c != null && c.getAnswers() != null)
					for (ConceptAnswer ca : c.getAnswers())
						orderStopReasons.add(ca.getAnswerConcept());
			}
			
			List<Concept> genericDrugs = Context.getConceptService().getConceptsWithDrugsInFormulary();
			Collections.sort(genericDrugs, new Comparator<Concept>() {
					public int compare(Concept left, Concept right) {
						return left.getName().getName().compareTo(right.getName().getName());
					}
				});
			
			List<Concept> drugSets = new ArrayList<Concept>();
			{
				String temp = Context.getAdministrationService().getGlobalProperty("cohortBuilder.drugSets");
				if (StringUtils.hasText(temp)) {
					String[] drugSetNames = temp.split(",");
					for (String setName : drugSetNames) {
						Concept c = Context.getConceptService().getConcept(setName);
						if (c != null)
							drugSets.add(c);
					}
				}
			}
			
			model.put("searchHistory", history);
			model.put("links", linkHelper());
			model.put("programs", Context.getProgramWorkflowService().getAllPrograms());
			model.put("encounterTypes", Context.getEncounterService().getAllEncounterTypes());
			model.put("locations", Context.getLocationService().getAllLocations());
			model.put("forms", Context.getFormService().getAllForms());
			model.put("drugs", Context.getConceptService().getAllDrugs());
			model.put("drugConcepts", genericDrugs);
			model.put("drugSets", drugSets);
			model.put("orderStopReasons", orderStopReasons);
			model.put("personAttributeTypes", Context.getPersonService().getAllPersonAttributeTypes());
			model.put("shortcuts", shortcuts);
		}
		return new ModelAndView(formView, "model", model);
	}
	
	public class Shortcut {
		private String label;
		private PatientFilter patientFilter;
		private String className;
		private List<ArgHolder> args;
		private Boolean hasPromptArgs;
		private String vars;
		public Shortcut() { }
		public Shortcut(String spec) {
			// possible formats:
			// (1) just the name of a saved filter
			// (2) a label, then a colon, then the name of a saved filter
			// (3) a label, then a colon, then @className(argname=argvalue#argClassName,...)
			//			(e.g. male:@org.openmrs.reporting.PatientCharacteristicFilter(gender=m#java.lang.String)
			String label = null;
			if (spec.indexOf(":") > 0) {
				label = spec.substring(0, spec.indexOf(":"));
				spec = spec.substring(spec.indexOf(":") + 1);
			}
			if (spec.startsWith("@")) {		
				// could be "@org.openmrs.reporting.ArvTreatmentGroupFilter(group#java.lang.String)"
				// could be "@org.openmrs.reporting.PatientCharacteristicFilter(gender=m#java.lang.String)"
				List<ArgHolder> temp = new ArrayList<ArgHolder>();
				if (spec.indexOf('(') <= 0 || spec.indexOf(')') <= 0)
					log.error("this line is missing a ( or a ): " + spec);
				setClassName(spec.substring(1, spec.indexOf('(')));
				String s = spec.substring(spec.indexOf('(') + 1, spec.lastIndexOf(')'));
				String[] t = s.split(",");
				for (String arg : t) {
					if (arg.trim().length() == 0)
						continue;
					if (arg.startsWith("\'") && arg.endsWith("\'")) {
						temp.add(new ArgHolder(null, arg.substring(1, arg.length() - 1), null));
					} else {
						String[] u = arg.split("#");
						if (u.length != 2) {
							throw new IllegalArgumentException("Shortcut arguments must be name#Type. arg = " + arg);
						}
						Class clz;
						try {
							clz = Class.forName(u[1]);
						} catch (ClassNotFoundException ex) {
							throw new IllegalArgumentException(ex);
						}
						if (u[0].indexOf('=') > 0) {
							String[] v = u[0].split("=");
							if (v.length != 2) {
								throw new IllegalArgumentException("shortcut arguments can have only one equal sign");
							}
							/*
							String hidden = "<input type=hidden name=\"" + v[0] + "\" value=\"" + v[1] + "\"/>";
							tempHidden.add(hidden);
							tempHiddenNames.add(v[0]);
							log.debug("hidden arg " + v[0] + " -> " + v[1]);
							*/
							temp.add(new ArgHolder(clz, v[0], v[1]));
						} else {
							String name = u[0];
							setHasPromptArgs(true);
							temp.add(new ArgHolder(clz, name, null));
						}
					}
				}
				if (temp.size() > 0) {
					setArgs(temp);
					StringBuilder sb = new StringBuilder();
					for (ArgHolder arg : temp)
						if (arg.getArgClass() != null) {
							if (sb.length() > 0)
								sb.append(",");
							sb.append(arg.getArgName() + "#" + arg.getArgClass().getName());
						}
					setVars(sb.toString());
				}
				
			} else {
				PatientFilter pf = Context.getReportObjectService().getPatientFilterByName(spec);
				if (label == null)
					label = pf.getName();
				if (pf != null)
					setPatientFilter(pf);
				else
					throw new IllegalArgumentException("Couldn't find a patient filter called: " + spec);
			}
			setLabel(label);
		}
		public boolean isConcrete() {
			return patientFilter != null;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public PatientFilter getPatientFilter() {
			return patientFilter;
		}
		public void setPatientFilter(PatientFilter patientFilter) {
			this.patientFilter = patientFilter;
		}
		public String getClassName() {
			return className;
		}
		public void setClassName(String className) {
			this.className = className;
		}
		public List<ArgHolder> getArgs() {
			return args;
		}
		public void setArgs(List<ArgHolder> args) {
			this.args = args;
		}
		public Boolean getHasPromptArgs() {
			return hasPromptArgs;
		}
		public void setHasPromptArgs(Boolean hasPromptArgs) {
			this.hasPromptArgs = hasPromptArgs;
		}
		public String getVars() {
			return vars;
		}
		public void setVars(String vars) {
			this.vars = vars;
		}
	}
	
	public ModelAndView clearHistory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (Context.isAuthenticated()) {
			setMySearchHistory(request, null);
		}
		return new ModelAndView(new RedirectView(getSuccessView()));
	}
	
	public ModelAndView addFilter(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (Context.isAuthenticated()) {
			CohortSearchHistory history = getMySearchHistory(request);
			String temp = request.getParameter("filter_id");
			if (temp != null) {
				Integer filterId = new Integer(temp);
				PatientFilter pf = Context.getReportObjectService().getPatientFilterById(filterId);
				if (pf != null)
					history.addSearchItem(PatientSearch.createSavedFilterReference(filterId));
				else
					log.warn("addFilter(id) didn't find " + filterId);
			}
			temp = request.getParameter("search_id");
			if (temp != null) {
				Integer searchId = new Integer(temp);
				PatientSearchReportObject ro = (PatientSearchReportObject) Context.getReportObjectService().getReportObject(searchId);
				if (ro != null)
					history.addSearchItem(PatientSearch.createSavedSearchReference(searchId));
				else
					log.warn("addSearch(id) didn't find " + searchId);
			}
			temp = request.getParameter("cohort_id");
			if (temp != null) {
				Integer cohortId = new Integer(temp);
				Cohort c = Context.getCohortService().getCohort(cohortId);
				if (c != null) {
					history.addSearchItem(PatientSearch.createSavedCohortReference(cohortId));
				}
				else
					log.warn("addCohort(id) didn't find " + cohortId);
			}
			temp = request.getParameter("composition");
			if (temp != null) {
				PatientSearch ps = history.createCompositionFilter(temp);
				if (ps != null)
					history.addSearchItem(ps);
				else
					request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Error building composition filter");
			}
		}
		return new ModelAndView(new RedirectView(getSuccessView()));
	}
	
	public ModelAndView removeFilter(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (Context.isAuthenticated()) {
			CohortSearchHistory history = getMySearchHistory(request);
			String temp = request.getParameter("index");
			if (temp != null) {
				Integer index = new Integer(temp);
				history.removeSearchItem(index);
			}
		}
		return new ModelAndView(new RedirectView(getSuccessView()));
	}
	
	public class ArgHolder {
		private Class argClass;
		private String argName;
		private Object argValue;
		public ArgHolder() { }
		public ArgHolder(Class argClass, String argName, Object argValue) {
			this.argClass = argClass;
			this.argName = argName;
			this.argValue = argValue;
		}
		public Class getArgClass() {
			return argClass;
		}
		public void setArgClass(Class argClass) {
			this.argClass = argClass;
		}
		public String getArgName() {
			return argName;
		}
		public void setArgName(String argName) {
			this.argName = argName;
		}
		public Object getArgValue() {
			return argValue;
		}
		public void setArgValue(Object argValue) {
			this.argValue = argValue;
		}
		public boolean hasValue() {
			return argValue != null && ((argValue instanceof String && ((String) argValue).length() > 0) || (argValue instanceof String[] && ((String[]) argValue).length > 0));
		}
		public String toString() {
			return "(" + argClass + ") " + argName + " = " + argValue;
		}
	}
	
	private boolean checkClassHelper(Class checkFor, Class checkFirst, Class checkNext) {
		return checkFor.equals(checkFirst) || ((checkFirst.equals(Object.class) || checkFirst.equals(List.class) )&& checkFor.equals(checkNext));
	}
	
	public ModelAndView addDynamicFilter(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ClassNotFoundException {
		if (Context.isAuthenticated()) {
			String filterClassName = request.getParameter("filterClass");
			String temp = request.getParameter("vars");
			String[] args = temp.split(",");
			log.debug(args.length + " args: vars=" + temp);
			List<ArgHolder> argValues = new ArrayList<ArgHolder>();
	
			for (String arg : args) {
				log.debug("looking at: " + arg);
				if (arg.trim().length() == 0)
					continue;
				String[] u = arg.split("#");
				if (u.length != 2) {
					StringBuilder msg = new StringBuilder();
					msg.append(arg);
					msg.append(" -> ");
					for (String str : u) {
						msg.append(str).append(" , ");
					}
					throw new IllegalArgumentException("shortcut option arguments must be label#Type. " + msg);
				}
				String name = u[0];
				Class c;
				boolean isList = false;
				if (u[1].startsWith("*")) {
					u[1] = u[1].substring(1);
					isList = true;
				}
				try {
					c = Class.forName(u[1]);
				} catch (ClassNotFoundException ex) {
					throw new IllegalArgumentException(ex);
				}
				argValues.add(new ArgHolder(c, name, isList ? request.getParameterValues(name) : request.getParameter(name)));
			}
			
			log.debug("argValues has size " + argValues.size());
			
			// Refactoring to create a PatientSearch instead of a PatientFilter
			PatientSearch search = new PatientSearch();
            search.setFilterClass(Class.forName(filterClassName));
			for (ArgHolder arg : argValues) {
				Object val = arg.getArgValue();
				if (val instanceof String[]) {
					String[] valArray = (String[]) val;
					for (int i = 0; i < valArray.length; ++i)
						if (StringUtils.hasText(valArray[i]))
							search.addArgument(arg.getArgName(), valArray[i], arg.getArgClass());
				} else {
					if (StringUtils.hasText((String) val))
						search.addArgument(arg.getArgName(), (String) val, arg.getArgClass());
				}
			}
			log.debug("Created PatientSearch: " + search);

			if (search != null) {
				CohortSearchHistory history = getMySearchHistory(request);
				history.addSearchItem(search);
			}
		}
		return new ModelAndView(new RedirectView(getSuccessView()));
	}
	
	public ModelAndView saveHistory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO: fix this!
		if (Context.isAuthenticated()) {
			String name = request.getParameter("name");
			String description = request.getParameter("description");
			if ( (name == null || "".equals(name)) && (description == null || "".equals(description)) ) {
				throw new RuntimeException("Name and Description are required");
			}
			CohortSearchHistory history = getMySearchHistory(request);
			if (history.getReportObjectId() != null)
				throw new RuntimeException("Re-saving histories is not yet implemented");
			history.setName(name);
			history.setDescription(description);
			Context.getReportObjectService().saveSearchHistory(history);
		}
		return new ModelAndView(new RedirectView(getSuccessView()));
	}

	private List<LinkSpec> linkHelper() {
    	List<LinkSpec> ret = new ArrayList<LinkSpec>();
    	for (String spec : links) {
    		ret.add(new LinkSpec(spec));
    	}
		return ret;
	}
	
	public class LinkArg {
		String name;
		String value;
		public LinkArg() { }
		public LinkArg(String name, String value) {
			this.name = name;
			this.value = value;
		}
		public LinkArg(String nameEqualsValue) {
			int i = nameEqualsValue.indexOf('=');
			name = nameEqualsValue.substring(0, i);
			value = nameEqualsValue.substring(i + 1);
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
	
	public class LinkSpec {
		String label;
		String url;
		List<LinkArg> arguments = new ArrayList<LinkArg>();
		public LinkSpec(String spec) {
			StringTokenizer st = new StringTokenizer(spec, ":");
			url = st.nextToken();
			label = st.nextToken();
			while (st.hasMoreTokens()) {
				arguments.add(new LinkArg(st.nextToken()));
			}
		}
		public List<LinkArg> getArguments() {
			return arguments;
		}
		public String getLabel() {
			return label;
		}
		public String getUrl() {
			return url;
		}
	}
	
}
