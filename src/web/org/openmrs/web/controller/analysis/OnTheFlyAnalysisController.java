package org.openmrs.web.controller.analysis;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.ObsListProducer;
import org.openmrs.reporting.PatientAnalysis;
import org.openmrs.reporting.PatientDataSet;
import org.openmrs.reporting.PatientDataSetFormatter;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.PatientSet;
import org.openmrs.reporting.ReportService;
import org.openmrs.reporting.ShortDescriptionProducer;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;

public class OnTheFlyAnalysisController implements Controller {
	
	final static String ON_THE_FLY_ANALYSIS_ATTR = "__openmrs_on_the_fly_analysis";
	protected Log log = LogFactory.getLog(getClass());

	private List<String> shortcuts;
	private List<String> links;
	
	public List<String> getShortcuts() {
		return shortcuts;
	}

	// Example: gender!both:*,male:male_only_filter,female:female_only_filter
	public void setShortcuts(List<String> shortcuts) {
		this.shortcuts = shortcuts;
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

	private List<ShortcutSpec> shortcutHelper() {
		try {
			List<ShortcutSpec> ret = new ArrayList<ShortcutSpec>();
			for (String s : shortcuts) {
				String[] temp = s.split("!");
				String shortcutLabel = temp[0];
				String[] opts = temp[1].split(",");
				ShortcutSpec shortcut = new ShortcutSpec(shortcutLabel);
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
			log.warn("Exception trying to parse ShortcutSpec");
			log.warn(ex);
			throw new IllegalArgumentException("Bad shortcut spec string: " + shortcuts);
		}
	}
	
    public ModelAndView handleRequest(HttpServletRequest request,
    		HttpServletResponse response) throws ServletException, IOException {

		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return null;
		}
		
		Locale locale = context.getLocale();
		
		ReportService reportService = context.getReportService();
		PatientSetService patientSetService = context.getPatientSetService();
		
		PatientAnalysis analysis = (PatientAnalysis) httpSession.getAttribute(ON_THE_FLY_ANALYSIS_ATTR);
		if (analysis == null) {
			analysis = new PatientAnalysis();
		}
		
		List<LinkSpec> linkList = linkHelper();
		
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
					s.setCurrentFilter(((AbstractReportObject) pf).getName());
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

		PatientSet everyone = patientSetService.getPatientsByCharacteristics(null, null, null);
		PatientSet result = analysis.runFilters(context, everyone);
		
		PatientDataSet pds = new PatientDataSet();
		ShortDescriptionProducer sdp = new ShortDescriptionProducer();
		pds.putDataSeries("description", sdp.produceData(context, result));
		
		Object resultsToDisplay = pds;
		Object xmlToDisplay = null;
		
		String viewMethod = request.getParameter("view");
		if ("cd4".equals(viewMethod)) {
			log.debug("preparing cd4 view");
			ObsListProducer olp = new ObsListProducer(context.getConceptService().getConcept(new Integer(5497)));
			pds.putDataSeries("cd4s", olp.produceData(context, result));
			PatientDataSetFormatter formatter = new ChronologicalObsFormatterHtml("cd4s");
			resultsToDisplay = formatter.format(pds, locale);
		} else if ("xml".equals(viewMethod)) {
			Set<Integer> temp = result.getPatientIds();
			if (temp.size() > 0) {
				Integer ptId = temp.iterator().next();
				log.debug("preparing xml view of patient " + ptId);
				xmlToDisplay = patientSetService.exportXml(ptId).replaceAll(">", ">\n");
			}
		}
		
		/*
		Collection<PatientFilter> filters = analysis.getPatientFilters().values();
		if (filters == null) {
			filters = new ArrayList<PatientFilter>();
		}
		*/
		Map<String, PatientFilter> filters = analysis.getPatientFilters();
		
		List availableFilters = new ArrayList<PatientFilter>(reportService.getAllPatientFilters());
		for (PatientFilter pf : filters.values()) {
			availableFilters.remove(pf);
		}
		
		/*
		PatientClassifier classifier = analysis.getPatientClassifier();
		List availableClassifiers = new ArrayList<PatientClassifier>(reportService.getAllPatientClassifiers());
		if (classifier != null) {
			availableClassifiers.remove(classifier);
		}
		*/
		
		Map myModel = new HashMap();
		myModel.put("no_filters", new Boolean(filters.size() == 0));
		myModel.put("active_filters", filters);
		myModel.put("suggested_filters", availableFilters);
		//myModel.put("classifier", classifier);
		//myModel.put("suggested_classifiers", availableClassifiers);
		myModel.put("number_of_results", new Integer(result.size()));
		myModel.put("analysis_results", resultsToDisplay);
		myModel.put("xml_debug", xmlToDisplay);
		myModel.put("shortcuts", shortcutList);
		myModel.put("patient_set_for_links", result.toCommaSeparatedPatientIds());
		myModel.put("links", linkList);

		return new ModelAndView("/analysis/on-the-fly-analysis", "model", myModel);
	}

    private List<LinkSpec> linkHelper() {
    	List<LinkSpec> ret = new ArrayList<LinkSpec>();
    	for (String spec : links) {
    		ret.add(new LinkSpec(spec));
    	}
		return ret;
	}

	/*
	public ModelAndView setClassifier(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return null;
		}
		
		ReportService reportService = context.getReportService();
		
		PatientAnalysis analysis = (PatientAnalysis) httpSession.getAttribute(ON_THE_FLY_ANALYSIS_ATTR);
		if (analysis == null) {
			analysis = new PatientAnalysis();
			httpSession.setAttribute(ON_THE_FLY_ANALYSIS_ATTR, analysis);
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
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return null;
		}
		log.debug("Entering addFilter()...");
		
		ReportService reportService = context.getReportService();
		
		PatientAnalysis analysis = (PatientAnalysis) httpSession.getAttribute(ON_THE_FLY_ANALYSIS_ATTR);
		if (analysis == null) {
			log.debug("creating new PatientAnalysis");
			analysis = new PatientAnalysis();
			httpSession.setAttribute(ON_THE_FLY_ANALYSIS_ATTR, analysis);
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
		
		String nameToAdd = request.getParameter("patient_filter_name");
		if (nameToAdd != null) {
			String addAsKey = request.getParameter("patient_filter_key");
			ShortcutOptionSpec opt = new ShortcutOptionSpec();
			if (request.getParameter("filter_spec") != null) {
				opt.setValue(request.getParameter("filter_spec"));
			} else {
				opt.setValue(nameToAdd);
			}
			log.debug("trying to add filter " + opt + " as key " + addAsKey);
			PatientFilter pf = null;
			if (!opt.isConcrete()) {
				try {
					Class filterClass = Class.forName(opt.getClassName());
					log.debug("about to call newInstance on " + filterClass);
					PatientFilter filterInstance = (PatientFilter) filterClass.newInstance();
					log.debug("result is " + filterInstance);
					for (String argName : opt.getAllArgs()) {
						String argVal = request.getParameter(argName);
						log.debug("about to set " + argName + " to " + argVal);
						PropertyDescriptor pd = new PropertyDescriptor(argName, filterClass);
						pd.getWriteMethod().invoke(filterInstance, argVal);
					}
					pf = filterInstance;
				} catch (Exception ex) {
					log.error(ex);
				}
			} else {
				pf = reportService.getPatientFilterByName(nameToAdd);
			}
			if (pf != null) {
				log.debug("adding filter " + pf + " to analysis");
				analysis.addFilter(addAsKey, pf);
			} else {
				log.warn("Can't find filter by that name: " + nameToAdd);
			}
		}
		
		return new ModelAndView(new RedirectView("analysis.list"));
	}
	
	public ModelAndView removeFilter(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return null;
		}
		
		PatientAnalysis analysis = (PatientAnalysis) httpSession.getAttribute(ON_THE_FLY_ANALYSIS_ATTR);
		if (analysis != null) {
			String keyToRemove = request.getParameter("patient_filter_key");
			log.debug("removing filter " + keyToRemove);
			if (keyToRemove != null) {
				PatientFilter pf = analysis.removeFilter(keyToRemove);
				log.debug("returns " + pf);
			}
		}
		return new ModelAndView(new RedirectView("analysis.list"));
	}
	
	
	public class ShortcutSpec {
		private String label;
		private String currentFilter;
		public LinkedHashMap<String, ShortcutOptionSpec> options;
		public ShortcutSpec() { }
		public ShortcutSpec(String label) {
			this.label = label;
			options = new LinkedHashMap<String, ShortcutOptionSpec>();
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public String getCurrentFilter() {
			return currentFilter;
		}
		public void setCurrentFilter(String currentFilter) {
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
	
	public class ShortcutOptionSpec {
		private String value;
		private boolean concrete;
		private boolean remove;
		private String className;
		List<String> args;
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
		// could be "@org.openmrs.reporting.ArvTreatmentGroupFilter(group$String)"
		// could be "@org.openmrs.reporting.PatientCharacteristicFilter(gender=m$String)"
		public void setValue(String value) {
			this.value = value;
			concrete = value == null || !value.startsWith("@");
			remove = value == null || "*".equals(value);
			if (concrete) {
				args = null;
			} else {
				try {
					List<String> temp = new ArrayList<String>();
					List<String> tempHidden = new ArrayList<String>();
					List<String> tempHiddenNames = new ArrayList<String>();
					className = value.substring(1, value.indexOf('('));
					String s = value.substring(value.indexOf('(') + 1, value.lastIndexOf(')'));
					String[] t = s.split(",");
					for (String arg : t) {
						String[] u = arg.split("\\$");
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
							temp.add(u[0]); // treat everything as a String for now
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
					log.warn("Error parsing arguments list in " + value);
					log.warn(ex);
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
		public List<String> getArgs() {
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
				ret.addAll(args);
			}
			if (hiddenArgNames != null) {
				ret.addAll(hiddenArgNames);
			}
			return ret;
		}
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
