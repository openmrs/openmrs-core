package org.openmrs.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class DynamicPageController implements Controller {

	protected static final Log log = LogFactory.getLog(DynamicPageController.class);
	
	private String pageName;

	public String getPageName() {
		return pageName;
	}

	public void setPageName(String pageName) {
		this.pageName = pageName;
	}

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> model = new HashMap<String, Object>();
		List<WidgetSpec> list = new ArrayList<WidgetSpec>();
		
		String spec = "";
		if ("homePage".equals(pageName)) {
			spec = OpenmrsConstants.DYNAMIC_HOME_PAGE_SPEC;
		}
		log.debug("handleRequest() called with spec = '" + spec + "'");
		// TODO: use a proper tokenizer for this
		boolean inTag = !spec.startsWith("@");
		for (StringTokenizer st = new StringTokenizer(spec, "@"); st.hasMoreTokens(); ) {
			String s = st.nextToken();
			log.debug(s + " ->");
			inTag = !inTag;
			if (inTag) { // this is a widget tag
				WidgetSpec w = new WidgetSpec();
				String[] getId = s.split("!");
				if (getId.length > 1) {
					w.setDivId(getId[1]);
				}
				String[] temp = getId[0].split(":");
				String widgetName = temp[0];
				log.debug("widgetName = " + widgetName);
				w.setWidgetName(widgetName);
				if (temp.length > 1) {
					w.setArgs(temp[1]);
				}
				System.out.println(" -> " + w);
				list.add(w);
			} else {
				WidgetSpec w = new WidgetSpec();
				w.setHtml(s);
				System.out.println(" -> " + w);
				list.add(w);
			}
		}
		model.put("spec", list);
		
		return new ModelAndView("/dynamic", "model", model);
	}
	
	public class WidgetSpec {
		private String html;
		private String widgetName;
		private String args;
		private String divId;

		public WidgetSpec() {
			args = "";
		}
		
		public boolean isWidget() { return widgetName != null; }

		public String toString() {
			if (isWidget()) {
				return "Widget: " + widgetName + ", args = " + args;
			} else {
				return "Html: " + html;
			}
		}
		
		public String getHtml() {
			return html;
		}

		public void setHtml(String html) {
			this.html = html;
		}

		public String getWidgetName() {
			return widgetName;
		}

		public void setWidgetName(String widgetName) {
			this.widgetName = widgetName;
		}

		public String getArgs() {
			return args;
		}

		public void setArgs(String args) {
			if (args == null) {
				args = "";
			}
			this.args = args;
		}
		
		public String getDivId() {
			return divId;
		}

		public void setDivId(String divId) {
			this.divId = divId;
		}
		
	}
	
}
