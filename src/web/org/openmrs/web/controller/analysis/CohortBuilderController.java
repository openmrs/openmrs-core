package org.openmrs.web.controller.analysis;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortSearchHistory;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.ReportObjectXMLDecoder;
import org.openmrs.web.controller.analysis.OnTheFlyAnalysisController.LinkArg;
import org.openmrs.web.controller.analysis.OnTheFlyAnalysisController.LinkSpec;
import org.openmrs.web.propertyeditor.ConceptEditor;
import org.openmrs.web.propertyeditor.LocationEditor;
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
		List<PatientFilter> savedFilters = Context.getReportService().getAllPatientFilters();
		if (savedFilters == null)
			savedFilters = new ArrayList<PatientFilter>();
		CohortSearchHistory history = (CohortSearchHistory) Context.getVolatileUserData("CohortBuilderSearchHistory");
		if (history == null) {
			history = new CohortSearchHistory();
			Context.setVolatileUserData("CohortBuilderSearchHistory", history);
		}
		model.put("savedFilters", savedFilters);
		model.put("searchHistory", history);
		model.put("links", linkHelper());
		return new ModelAndView(formView, "model", model);
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
	
	private class ArgHolder {
		private Class argClass;
		private String argName;
		private String argValue;
		public ArgHolder() { }
		public ArgHolder(Class argClass, String argName, String argValue) {
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
		public String getArgValue() {
			return argValue;
		}
		public void setArgValue(String argValue) {
			this.argValue = argValue;
		}
		public String toString() {
			return "(" + argClass + ") " + argName + " = " + argValue;
		}
	}
	
	private boolean checkClassHelper(Class checkFor, Class checkFirst, Class checkNext) {
		return checkFor.equals(checkFirst) || (checkFirst.equals(Object.class) && checkFor.equals(checkNext));
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
				String name = u[0];
				Class c;
				try {
					c = Class.forName(u[1]);
				} catch (ClassNotFoundException ex) {
					throw new IllegalArgumentException(ex);
				}
				argValues.add(new ArgHolder(c, name, request.getParameter(name)));
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
						Object argVal = null;
						PropertyDescriptor pd = new PropertyDescriptor(arg.getArgName(), filterClass);
						// TODO: fix this hack
						if (arg.getArgValue() != null && arg.getArgValue().trim().length() == 0) {
							log.debug("A");
							argVal = null;
						} else if (checkClassHelper(Location.class, pd.getPropertyType(), arg.getArgClass())) {
							log.debug("B");
							LocationEditor le = new LocationEditor();
							le.setAsText(arg.getArgValue());
							argVal = le.getValue();
						} else if (checkClassHelper(String.class, pd.getPropertyType(), arg.getArgClass())) {
							argVal = arg.getArgValue();
						} else if (checkClassHelper(Integer.class, pd.getPropertyType(), arg.getArgClass())) {
							log.debug("C");
							try {
								argVal = Integer.valueOf(arg.getArgValue());
							} catch (Exception ex) { }
						} else if (checkClassHelper(Double.class, pd.getPropertyType(), arg.getArgClass())) {
							log.debug("D");
							try {
								argVal = Double.valueOf(arg.getArgValue());
							} catch (Exception ex) { }
						} else if (checkClassHelper(Concept.class, pd.getPropertyType(), arg.getArgClass())) {
							log.debug("E");
							ConceptEditor ce = new ConceptEditor();
							ce.setAsText(arg.getArgValue());
							Concept concept = (Concept) ce.getValue();
							// force a lazy-load of this concept's name
							if (concept != null)
								concept.getName(Context.getLocale());
							argVal = concept;
						} else if (pd.getPropertyType().isEnum()) {
							log.debug("F");
							List<Enum> constants = Arrays.asList((Enum[]) pd.getPropertyType().getEnumConstants());
							for (Enum e : constants) {
								if (e.toString().equals(arg.getArgValue())) {
									argVal = e;
									break;
								}
							}
						} else if (pd.getPropertyType().equals(Object.class)) {
							log.debug("G fell through to plain object, treated as string");
							argVal = arg.getArgValue();
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
