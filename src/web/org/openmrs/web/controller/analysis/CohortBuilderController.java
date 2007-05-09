package org.openmrs.web.controller.analysis;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Drug;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.PersonAttributeType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortSearchHistory;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.ReportObjectXMLDecoder;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.openmrs.web.propertyeditor.ConceptEditor;
import org.openmrs.web.propertyeditor.DrugEditor;
import org.openmrs.web.propertyeditor.EncounterTypeEditor;
import org.openmrs.web.propertyeditor.LocationEditor;
import org.openmrs.web.propertyeditor.PersonAttributeTypeEditor;
import org.openmrs.web.propertyeditor.ProgramEditor;
import org.openmrs.web.propertyeditor.ProgramWorkflowStateEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
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

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = new HashMap<String, Object>();
		if (Context.isAuthenticated()) {
			List<PatientFilter> savedFilters = Context.getReportService().getAllPatientFilters();
			if (savedFilters == null)
				savedFilters = new ArrayList<PatientFilter>();
			CohortSearchHistory history = (CohortSearchHistory) Context.getVolatileUserData("CohortBuilderSearchHistory");
			if (history == null) {
				history = new CohortSearchHistory();
				Context.setVolatileUserData("CohortBuilderSearchHistory", history);
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
					orderStopReasons.addAll(cs.getConceptsInSet(c));
				if (c != null && c.getAnswers() != null)
					for (ConceptAnswer ca : c.getAnswers())
						orderStopReasons.add(ca.getAnswerConcept());
			}
			
			List<Concept> genericDrugs = new ArrayList<Concept>();
			/*
			{
				ConceptClass drugClass = Context.getConceptService().getConceptClassByName("Drug");
				if (drugClass != null) {
					genericDrugs = Context.getConceptService().getConceptsByClass(drugClass);
					Collections.sort(genericDrugs, new Comparator<Concept>() {
							public int compare(Concept left, Concept right) {
								return left.getName().getName().compareTo(right.getName().getName());
							}
						});
				} else
					log.warn("Cannot find ConceptClass named 'Drug'.");
			}
			*/
			genericDrugs = Context.getConceptService().getConceptsWithDrugsInFormulary();
			Collections.sort(genericDrugs, new Comparator<Concept>() {
					public int compare(Concept left, Concept right) {
						return left.getName().getName().compareTo(right.getName().getName());
					}
				});
			
			model.put("savedFilters", savedFilters);
			model.put("searchHistory", history);
			model.put("links", linkHelper());
			model.put("programs", Context.getProgramWorkflowService().getPrograms());
			model.put("encounterTypes", Context.getEncounterService().getEncounterTypes());
			model.put("locations", Context.getEncounterService().getLocations());
			model.put("drugs", Context.getConceptService().getDrugs());
			model.put("drugConcepts", genericDrugs);
			model.put("orderStopReasons", orderStopReasons);
			model.put("personAttributeTypes", Context.getPersonService().getPersonAttributeTypes());
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
				PatientFilter pf = Context.getReportService().getPatientFilterByName(spec);
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
			Context.setVolatileUserData("CohortBuilderSearchHistory", null);
		}
		return new ModelAndView(new RedirectView(getSuccessView()));
	}
	
	public ModelAndView addFilter(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (Context.isAuthenticated()) {
			CohortSearchHistory history = (CohortSearchHistory) Context.getVolatileUserData("CohortBuilderSearchHistory");
			String temp = request.getParameter("filter_id");
			if (temp != null) {
				Integer filterId = new Integer(temp);
				PatientFilter pf = Context.getReportService().getPatientFilterById(filterId);
				if (pf != null)
					history.addSearchItem(pf);
				else
					log.warn("addFilter(id) didn't find " + filterId);
			}
			temp = request.getParameter("filter_name");
			if (temp != null)
				history.addSearchItem(Context.getReportService().getPatientFilterByName(temp));
			temp = request.getParameter("filter_xml");
			if (temp != null) {
				AbstractReportObject ro = (new ReportObjectXMLDecoder(temp)).toAbstractReportObject();
				if (ro instanceof PatientFilter) {
					history.addSearchItem((PatientFilter) ro);
				} else {
					log.warn("addFilter(xml) found a report object that wasn't a PatientFilter: " + ro.getClass());
				}
			}
			temp = request.getParameter("composition");
			if (temp != null) {
				PatientFilter pf = history.createCompositionFilter(temp);
				if (pf != null)
					history.addSearchItem(pf);
				else
					request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Error building composition filter");
			}
		}
		return new ModelAndView(new RedirectView(getSuccessView()));
	}
	
	public ModelAndView removeFilter(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (Context.isAuthenticated()) {
			CohortSearchHistory history = (CohortSearchHistory) Context.getVolatileUserData("CohortBuilderSearchHistory");
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
	
	public ModelAndView addDynamicFilter(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

			PatientFilter filterInstance = null;
			try {
				Class filterClass = Class.forName(filterClassName);
				log.debug("about to call newInstance on " + filterClass);
				filterInstance = (PatientFilter) filterClass.newInstance();
				log.debug("result is " + filterInstance);
				for (ArgHolder arg : argValues) {
					log.debug("looking at " + arg);
					try {
						PropertyDescriptor pd = new PropertyDescriptor(arg.getArgName(), filterClass);
						// TODO: fix this hack
						Object argVal = null;
						if (arg.hasValue()) {
							String[] toLookAt = null;
							List<Object> vals = new ArrayList<Object>();
							boolean isList = false;
							if (arg.getArgValue() instanceof String[]) {
								isList = true;
								toLookAt = (String[]) arg.getArgValue();
							} else {
								toLookAt = new String[1];
								toLookAt[0] = (String) arg.getArgValue();
							}
							for (String val : toLookAt) {
								Object thisVal = null;
								if (checkClassHelper(Location.class, pd.getPropertyType(), arg.getArgClass())) {
									LocationEditor le = new LocationEditor();
									le.setAsText(val);
									thisVal = le.getValue();
								} else if (checkClassHelper(String.class, pd.getPropertyType(), arg.getArgClass())) {
									thisVal = val;
								} else if (checkClassHelper(Integer.class, pd.getPropertyType(), arg.getArgClass())) {
									try {
										thisVal = Integer.valueOf(val);
									} catch (Exception ex) { }
								} else if (checkClassHelper(Boolean.class, pd.getPropertyType(), arg.getArgClass())) {
									try {
										thisVal = Boolean.valueOf(val);
									} catch (Exception ex) { }
								} else if (checkClassHelper(Double.class, pd.getPropertyType(), arg.getArgClass())) {
									try {
										thisVal = Double.valueOf(val);
									} catch (Exception ex) { }
								} else if (checkClassHelper(Concept.class, pd.getPropertyType(), arg.getArgClass())) {
									ConceptEditor ce = new ConceptEditor();
									ce.setAsText(val);
									Concept concept = (Concept) ce.getValue();
									// force a lazy-load of this concept's name
									if (concept != null)
										concept.getName(Context.getLocale());
									thisVal = concept;
								} else if (checkClassHelper(Program.class, pd.getPropertyType(), arg.getArgClass())) {
									ProgramEditor pe = new ProgramEditor();
									pe.setAsText(val);
									Program program = (Program) pe.getValue();
									// force a lazy-load of the name
									if (program != null)
										program.getConcept().getName();
									thisVal = program;
								} else if (checkClassHelper(ProgramWorkflowState.class, pd.getPropertyType(), arg.getArgClass())) {
									ProgramWorkflowStateEditor ed = new ProgramWorkflowStateEditor();
									ed.setAsText(val);
									ProgramWorkflowState state = (ProgramWorkflowState) ed.getValue();
									// force a lazy-load of the name
									if (state != null)
										state.getConcept().getName();
									thisVal = state;
								} else if (checkClassHelper(EncounterType.class, pd.getPropertyType(), arg.getArgClass())) {
									EncounterTypeEditor ed = new EncounterTypeEditor();
									ed.setAsText(val);
									thisVal = ed.getValue();
								} else if (checkClassHelper(Drug.class, pd.getPropertyType(), arg.getArgClass())) {
									DrugEditor ed = new DrugEditor();
									ed.setAsText(val);
									thisVal = ed.getValue();
								} else if (checkClassHelper(Date.class, pd.getPropertyType(), arg.getArgClass())) {
									DateFormat df = new SimpleDateFormat(OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(Context.getLocale().toString().toLowerCase()), Context.getLocale());
									CustomDateEditor ed = new CustomDateEditor(df, true, 10);
									ed.setAsText(val);
									thisVal = ed.getValue();
								} else if (checkClassHelper(PersonAttributeType.class, pd.getPropertyType(), arg.getArgClass())) {
									PersonAttributeTypeEditor ed = new PersonAttributeTypeEditor();
									ed.setAsText(val);
									thisVal = ed.getValue();
								} else if (pd.getPropertyType().isEnum()) {
									List<Enum> constants = Arrays.asList((Enum[]) pd.getPropertyType().getEnumConstants());
									for (Enum e : constants) {
										if (e.toString().equals(val)) {
											thisVal = e;
											break;
										}
									}
								} else if (pd.getPropertyType().equals(Object.class)) {
									log.debug("fell through to plain object, treated as string");
									thisVal = val;
								}
								if (thisVal != null)
									vals.add(thisVal);
							}
							if (isList && vals.size() > 0)
								argVal = vals;
							else if (vals.size() > 0)
								argVal = vals.get(0);
						}
						if (argVal != null) {
							log.debug("about to set " + arg.getArgName() + " to " + argVal);
							pd.getWriteMethod().invoke(filterInstance, argVal);
						}
					} catch (Exception ex) {
						log.error("Couldn't set " + arg.getArgName() + " (" + arg.getArgClass() + ") to " + arg.getArgValue(), ex);
					}
				}
			} catch (Exception ex) {
				log.error("Couldn't instantiate class " + filterClassName, ex);
			}
			
			log.debug("final filter is " + filterInstance);
			
			if (filterInstance != null) {
				CohortSearchHistory history = (CohortSearchHistory) Context.getVolatileUserData("CohortBuilderSearchHistory");
				history.addSearchItem(filterInstance);
			}
		}
		return new ModelAndView(new RedirectView(getSuccessView()));
	}
	
	public ModelAndView saveHistory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (Context.isAuthenticated()) {
			String name = request.getParameter("name");
			String description = request.getParameter("description");
			if ( (name == null || "".equals(name)) && (description == null || "".equals(description)) ) {
				throw new RuntimeException("Name and Description are required");
			}
			CohortSearchHistory history = (CohortSearchHistory) Context.getVolatileUserData("CohortBuilderSearchHistory");
			if (history.getReportObjectId() != null)
				throw new RuntimeException("Re-saving histories is not yet implemented");
			history.setName(name);
			history.setDescription(description);
			Context.getReportService().createSearchHistory(history);
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
