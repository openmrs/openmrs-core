package org.openmrs.web.controller.analysis;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.PatientAnalysis;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.ReportService;
import org.openmrs.web.WebConstants;
import org.openmrs.web.propertyeditor.ConceptEditor;
import org.openmrs.web.propertyeditor.LocationEditor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;

public class OnTheFlyAnalysisController implements Controller {
	
	protected Log log = LogFactory.getLog(getClass());

	private List<String> shortcuts;
	private List<String> links;
	private String formView;
	
	public String getFormView() {
		return formView;
	}

	public void setFormView(String formView) {
		this.formView = formView;
	}

	public List<String> getShortcuts() {
		return shortcuts;
	}
	
	public List<String> getLinks() {
		return links;
	}

	/**
	 * The spec string should be LINK_TO_SUBMIT_TO:KEY_OF_MESSAGE_FOR_BUTTON:name_1=val_1:name_2=val_2:...:name_n=val_n
	 * Example: nealreport.form:nealreport.ConsultReport.name:reportType=Consult+Report
	 */
	public void setLinks(List<String> links) {
		this.links = links;
	}

	// Example: gender!both:*,male:male_only_filter,female:female_only_filter
	public void setShortcuts(List<String> shortcuts) {
		this.shortcuts = shortcuts;
	}

	private List<ShortcutSpec> shortcutHelper() {
		try {
			List<ShortcutSpec> ret = new ArrayList<ShortcutSpec>();
			for (String s : shortcuts) {
				String[] temp = s.split("!");
				String shortcutLabel = temp[0];
				boolean allowMultiple = shortcutLabel.startsWith("+");
				if (allowMultiple)
					shortcutLabel = shortcutLabel.substring(1);
				String[] opts = temp[1].split("\\|");
				ShortcutSpec shortcut = new ShortcutSpec(shortcutLabel);
				shortcut.setAllowMultiple(allowMultiple);
				for (String opt : opts) {
					temp = opt.split(":");
					String optLabel = temp[0];
					String optFilter = temp.length > 1 ? temp[1] : null;
					if ("*".equals(optFilter)) {
						optFilter = null;
					}
					ShortcutOptionSpec spec = null;
					spec = new ShortcutOptionSpec();
					spec.setValue(optFilter);
					shortcut.options.put(optLabel, spec);
				}
				ret.add(shortcut);
			}
			return ret;
		} catch (Exception ex) {
			log.warn("Exception trying to parse ShortcutSpec", ex);
			throw new IllegalArgumentException("Bad shortcut spec string: " + shortcuts);
		}
	}
	
    @SuppressWarnings("unchecked")
	public ModelAndView handleRequest(HttpServletRequest request,
    		HttpServletResponse response) throws ServletException, IOException {

    	Map<String, Object> myModel = new HashMap<String, Object>();
    	
		if (Context.isAuthenticated()) {
			ReportService reportService = Context.getReportService();
			
			PatientAnalysis analysis = Context.getPatientSetService().getMyPatientAnalysis();
			if ("true".equals(request.getParameter("remove_all_filters"))) {
				analysis.getPatientFilters().clear();
			}
			
			List<ShortcutSpec> shortcutList = shortcutHelper();
			if (reportService != null) {
				for (ShortcutSpec s : shortcutList) {
					// make sure the filter exists
					s.test(reportService);
					// find which filter (if any) is currently selected for this shortcut
					PatientFilter pf = analysis.getPatientFilters().get(s.getLabel());
					log.debug("Looked at ShortcutSpec " + s + " to see if there's a currently-selected filter for label " + s.getLabel() + " (keyset = " + analysis.getPatientFilters().keySet() + "). Result: " + pf);
					if (pf != null) {
						log.debug("set current filter for " + s + " to " + pf.getName());
						s.setCurrentFilter((PatientFilter) pf);
					}
				}
			}
			
			String[] filterIds = request.getParameterValues("patient_filter_id");
			if (filterIds != null) {
				for (String filterId : filterIds) {
					try {
						analysis.addFilter(null, reportService.getPatientFilterById(new Integer(filterId.trim())));
					} catch (Exception ex) { }
				}
			}
			
			Map<String, PatientFilter> filters = analysis.getPatientFilters();
			
			List availableFilters = new ArrayList<PatientFilter>(reportService.getAllPatientFilters());
			for (PatientFilter pf : filters.values()) {
				availableFilters.remove(pf);
			}
			Collections.sort(availableFilters, new Comparator() {
					public int compare(Object a, Object b) {
						if (a.getClass().equals(b.getClass()) && a instanceof Comparable)
							return ((Comparable) a).compareTo((Comparable) b);
						AbstractReportObject left = (AbstractReportObject) a;
						AbstractReportObject right = (AbstractReportObject) b;
						int temp = left.getType().compareTo(right.getType());
						if (temp == 0) {
							temp = left.getSubType().compareTo(right.getSubType());
						}
						if (temp == 0) {
							temp = left.getName().compareTo(right.getName());
						}
						if (temp == 0) {
							temp = left.getDateCreated().compareTo(right.getDateCreated());
						}
						return temp;
					}
				});
			
			List<LinkSpec> linkList = linkHelper();
			
			Map<String, Object> filterPortletParams = new HashMap<String, Object>();
			filterPortletParams.put("patientAnalysis", analysis);
			filterPortletParams.put("suggestedFilters", availableFilters);
			filterPortletParams.put("deleteURL", "analysis.form?method=removeFilter");
			filterPortletParams.put("addURL", "analysis.form?method=addFilter");
			filterPortletParams.put("shortcuts", shortcutList);
		
			myModel.put("active_filters", filters);
			myModel.put("shortcuts", shortcutList);
			myModel.put("filterPortletParams", filterPortletParams);
			myModel.put("links", linkList);
			
			String viewMethod = request.getParameter("viewMethod");
			if (viewMethod != null && viewMethod.length() > 0)
				myModel.put("viewMethod", viewMethod);
		}

		return new ModelAndView(formView, "model", myModel);
	}

	/*
	public ModelAndView setClassifier(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession httpSession = request.getSession();
		
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return null;
		}
		
		ReportService reportService = Context.getReportService();
		
		PatientAnalysis analysis = (PatientAnalysis) httpSession.getAttribute(WebConstants.OPENMRS_ANALYSIS_IN_PROGRESS_ATTR);
		if (analysis == null) {
			analysis = new PatientAnalysis();
			httpSession.setAttribute(WebConstants.OPENMRS_ANALYSIS_IN_PROGRESS_ATTR, analysis);
		}
		String idToSet = request.getParameter("patient_classifier_id");
		if (idToSet == null || idToSet.length() == 0) {
			analysis.setPatientClassifier(null);
		} else {
			try {
				PatientClassifier pc = reportService.getPatientClassifierById(new Integer(idToSet));
				analysis.setPatientClassifier(pc);
			} catch (NumberFormatException ex) { }
		}
		
		return new ModelAndView(new RedirectView("analysis.list"));
	}
	*/

	public ModelAndView addFilter(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering addFilter()...");
		
		ReportService reportService = Context.getReportService();
		
		PatientAnalysis analysis = Context.getPatientSetService().getMyPatientAnalysis();
		
		if ("true".equals(request.getParameter("remove_all_filters"))) {
			analysis.getPatientFilters().clear();
		}

		String[] idsToAdd = request.getParameterValues("patient_filter_id");
		if (idsToAdd != null) {
			for (String patientFilterId : idsToAdd) {
				log.debug("trying to add anonymous filter " + patientFilterId);
				try {
					PatientFilter pf = reportService.getPatientFilterById(new Integer(patientFilterId));
					analysis.addFilter(null, pf);
					log.debug("added");
				} catch (NumberFormatException ex) { }
			}
		}
		
		/*
		 * to allow multiple complex filters to be added at once, we're going to look for any parameters
		 * starting with "patient_filter_name", and then treat parameter groups with the same suffix as a group.
		 * E.g. patient_filter_name.1, patient_filter_key.1, filter_spec.1.
		 */
		List<String> suffixesToUse = new ArrayList<String>();
		{
			Enumeration en = request.getParameterNames();
			while (en.hasMoreElements()) {
				String s = (String) en.nextElement();
				if (s.startsWith("patient_filter_name")) {
					suffixesToUse.add(s.substring("patient_filter_name".length()));
				}
			}
		}
		
		for (String suffix : suffixesToUse) {
			String nameToAdd = request.getParameter("patient_filter_name" + suffix);
			log.debug(suffix + " -> " + nameToAdd);
			if (nameToAdd != null) {
				String addAsKey = request.getParameter("patient_filter_key" + suffix);
				ShortcutOptionSpec opt = new ShortcutOptionSpec();
				if (request.getParameter("filter_spec" + suffix) != null) {
					opt.setValue(request.getParameter("filter_spec" + suffix));
				} else {
					opt.setValue(nameToAdd);
				}
				PatientFilter pf = null;
				if (!opt.isConcrete()) {
					try {
						Class filterClass = Class.forName(opt.getClassName());
						log.debug("about to call newInstance on " + filterClass);
						PatientFilter filterInstance = (PatientFilter) filterClass.newInstance();
						log.debug("result is " + filterInstance);
						for (String argName : opt.getAllArgs()) {
							Object argVal = request.getParameter(argName);
							PropertyDescriptor pd = new PropertyDescriptor(argName, filterClass);
							// TODO: fix this hack
							if (argVal != null && ((String) argVal).trim().length() == 0) {
								argVal = null;
							} else if (pd.getPropertyType().equals(Location.class)) {
								LocationEditor le = new LocationEditor();
								le.setAsText((String) argVal);
								argVal = le.getValue();
							} else if (pd.getPropertyType().equals(Integer.class)) {
								try {
									argVal = Integer.valueOf((String) argVal);
								} catch (Exception ex) { }
							} else if (pd.getPropertyType().equals(Double.class)) {
								try {
									argVal = Double.valueOf((String) argVal);
								} catch (Exception ex) { }
							} else if (pd.getPropertyType().equals(Concept.class)) {
								ConceptEditor ce = new ConceptEditor();
								ce.setAsText((String) argVal);
								Concept concept = (Concept) ce.getValue();
								// force a lazy-load of this concept's name
								if (concept != null)
									concept.getName(Context.getLocale());
								argVal = concept;
							} else if (pd.getPropertyType().isEnum()) {
								List<Enum> constants = Arrays.asList((Enum[]) pd.getPropertyType().getEnumConstants());
								for (Enum e : constants) {
									if (e.toString().equals(argVal)) {
										argVal = e;
										break;
									}
								}
							}
							if (argVal != null) {
								log.debug("about to set " + argName + " to " + argVal);
								pd.getWriteMethod().invoke(filterInstance, argVal);
							}
						}
						pf = filterInstance;
					} catch (Exception ex) {
						log.error("Exception trying to instantiate parametrized filter " + opt, ex);
					}
				} else {
					pf = reportService.getPatientFilterByName(nameToAdd);
				}
				if (pf != null) {
					log.debug("adding filter " + pf + " to analysis");
					if (pf.isReadyToRun())
						analysis.addFilter(addAsKey, pf);
					else
						log.debug("skipping...not ready to run yet...");
				} else {
					log.warn("Can't find filter by that name: " + nameToAdd);
				}
			}
		}
		
		return new ModelAndView(new RedirectView("analysis.list?viewMethod=" + request.getParameter("viewMethod")));
	}
	
	public ModelAndView removeFilter(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession httpSession = request.getSession();
		
		if (!Context.isAuthenticated()) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return null;
		}
		
		PatientAnalysis analysis = Context.getPatientSetService().getMyPatientAnalysis();
		if (analysis != null) {
			if ("true".equals(request.getParameter("remove_all_filters"))) {
				analysis.getPatientFilters().clear();
			}
			String keyToRemove = request.getParameter("patient_filter_key");
			log.debug("removing filter " + keyToRemove);
			if (keyToRemove != null) {
				PatientFilter pf = analysis.removeFilter(keyToRemove);
				log.debug("returns " + pf);
			}
		}
		return new ModelAndView(new RedirectView("analysis.list?viewMethod=" + request.getParameter("viewMethod")));
	}
	
	
	public class ShortcutSpec {
		private String label;
		private boolean allowMultiple = false;
		private PatientFilter currentFilter;
		public LinkedHashMap<String, ShortcutOptionSpec> options;
		public ShortcutSpec() { }
		public ShortcutSpec(String label) {
			this.label = label;
			options = new LinkedHashMap<String, ShortcutOptionSpec>();
		}
		public boolean isAllowMultiple() {
			return allowMultiple;
		}
		public void setAllowMultiple(boolean allowMultiple) {
			this.allowMultiple = allowMultiple;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public PatientFilter getCurrentFilter() {
			return currentFilter;
		}
		public void setCurrentFilter(PatientFilter currentFilter) {
			this.currentFilter = currentFilter;
		}
		public List<Map.Entry<String, ShortcutOptionSpec>> getList() {
			return new ArrayList<Map.Entry<String, ShortcutOptionSpec>>(options.entrySet());
		}
		public void test(ReportService rs) {
			if (options == null) {
				throw new IllegalArgumentException("options is null for " + label);
			}
			for (ShortcutOptionSpec filterSpec : options.values()) {
				if (filterSpec != null) {
					if (filterSpec.isConcrete()) {
						if (filterSpec.getValue() != null) {
							PatientFilter pf = rs.getPatientFilterByName(filterSpec.getValue());
							if (pf == null) {
								throw new IllegalArgumentException("Cannot find filter " + filterSpec.getValue());
							}
						}
					} else {
						// TODO: test whether @method(args) exists
					}
				}
			}
		}
	}
	
	public class ShortcutArg {
		private String name;
		private Class fieldClass;
		public ShortcutArg() { }
		public ShortcutArg(String name, Class fieldClass) {
			this.name = name;
			this.fieldClass = fieldClass;
		}
		public Class getFieldClass() {
			return fieldClass;
		}
		public void setFieldClass(Class fieldClass) {
			this.fieldClass = fieldClass;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public boolean isLabel() {
			return fieldClass == null;
		}
		public String toString() {
			if (isLabel())
				return name;
			else
				return name + "#" + fieldClass;
		}
	}
	
	public class ShortcutOptionSpec {
		private String value;
		private boolean concrete;
		private boolean remove;
		private String className;
		List<ShortcutArg> args;
		List<String> hiddenArgs;
		List<String> hiddenArgNames;
		
		public ShortcutOptionSpec() { }
		public ShortcutOptionSpec(String value) {
			setValue(value);
		}
		public String getValue() {
			return value;
		}
		// could be "child_only_filter"
		// could be "@org.openmrs.reporting.ArvTreatmentGroupFilter(group#java.lang.String)"
		// could be "@org.openmrs.reporting.PatientCharacteristicFilter(gender=m#java.lang.String)"
		public void setValue(String value) {
			this.value = value;
			concrete = value == null || !value.startsWith("@");
			remove = value == null || "*".equals(value);
			if (concrete) {
				args = null;
			} else {
				try {
					List<ShortcutArg> temp = new ArrayList<ShortcutArg>();
					List<String> tempHidden = new ArrayList<String>();
					List<String> tempHiddenNames = new ArrayList<String>();
					className = value.substring(1, value.indexOf('('));
					log.debug("looking at " + value);
					String s = value.substring(value.indexOf('(') + 1, value.lastIndexOf(')'));
					log.debug("Looking at: " + s);
					String[] t = s.split(",");
					for (String arg : t) {
						log.debug("looking at: " + arg);
						if (arg.startsWith("\'") && arg.endsWith("\'")) {
							temp.add(new ShortcutArg(arg.substring(1, arg.length() - 1), null));
						} else {
							String[] u = arg.split("#");
							if (u.length != 2) {
								StringBuilder msg = new StringBuilder();
								msg.append(arg);
								msg.append(" -> ");
								for (String str : u) {
									msg.append(str).append(" , ");
								}
								throw new IllegalArgumentException("shortcut option arguments must be label$Type. " + msg);
							}
							if (u[0].indexOf('=') > 0) {
								String[] v = u[0].split("=");
								if (v.length != 2) {
									StringBuilder msg = new StringBuilder();
									msg.append(arg);
									msg.append(" -> ");
									for (String str : u) {
										msg.append(str).append(" , ");
									}
									throw new IllegalArgumentException("shortcut option arguments has an incorrect equal sign. " + msg);
								}
								String hidden = "<input type=hidden name=\"" + v[0] + "\" value=\"" + v[1] + "\"/>";
								tempHidden.add(hidden);
								tempHiddenNames.add(v[0]);
								log.debug("hidden arg " + v[0] + " -> " + v[1]);
							} else {
								String name = u[0];
								Class c;
								try {
									c = Class.forName(u[1]);
								} catch (ClassNotFoundException ex) {
									throw new IllegalArgumentException(ex);
								}
								temp.add(new ShortcutArg(name, c));
							}
						}
					}
					if (temp.size() > 0) {
						args = temp;
					}
					if (tempHidden.size() > 0) {
						hiddenArgs = tempHidden;
						hiddenArgNames = tempHiddenNames;
					}
				} catch (IndexOutOfBoundsException ex) {
					log.warn("Error parsing arguments list in " + value, ex);
				}
			}
		}
		public boolean isConcrete() {
			return concrete;
		}
		public boolean isPromptArgs() {
			return args != null;
		}
		public boolean isAnyArgs() {
			return args != null || hiddenArgs != null;
		}
		public List<ShortcutArg> getArgs() {
			return args;
		}
		public String getClassName() {
			return className;
		}
		public boolean isRemove() {
			return remove;
		}
		public String toString() {
			return value;
		}
		public List<String> getHiddenArgs() {
			return hiddenArgs;
		}
		public List<String> getAllArgs() {
			List<String> ret = new ArrayList<String>();
			if (args != null) {
				for (ShortcutArg arg : args)
					if (!arg.isLabel())
						ret.add(arg.getName());
			}
			if (hiddenArgNames != null) {
				ret.addAll(hiddenArgNames);
			}
			return ret;
		}
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
