package org.openmrs.web.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class FieldGenController implements Controller {

	protected Log log = LogFactory.getLog(this.getClass());

	@SuppressWarnings("unchecked")
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// find the field that was identified in the openmrs:fieldGen taglib
		Object uri = request.getAttribute("javax.servlet.include.servlet_path.fieldGen");
		if ( uri == null ) uri = request.getAttribute("javax.servlet.include.servlet_path");
		String fieldGenPath = "";
		Map<String, Object> model = new HashMap<String, Object>();
		
		if (uri != null) {
			fieldGenPath = uri.toString();

			// Allowable extensions are '' (no extension) and '.field'
			if (fieldGenPath.endsWith("field"))
				fieldGenPath = fieldGenPath.replace(".field", "");
			else if (fieldGenPath.endsWith("jsp"))
				throw new ServletException("Illegal extension used for fieldGen: '.jsp'. Allowable extensions are '' (no extension) and '.field'");

			String type = (String)request.getAttribute("org.openmrs.fieldGen.type");
			String formFieldName = (String)request.getAttribute("org.openmrs.fieldGen.formFieldName");
			//String startVal = (String)request.getAttribute("org.openmrs.fieldGen.startVal");
			Map<String, Object> params = (Map<String, Object>)request.getAttribute("org.openmrs.fieldGen.parameters");
			Map<String, Object> moreParams = (Map<String, Object>) request.getAttribute("org.openmrs.fieldGen.parameterMap");

			model.put("type", type);
			model.put("formFieldName", formFieldName);
			model.put("obj", request.getAttribute("org.openmrs.fieldGen.object"));
			model.put("request", request.getAttribute("org.openmrs.fieldGen.request"));
			model.putAll(params);
			if (moreParams != null) {
				model.putAll(moreParams);
			}
			
			request.removeAttribute("org.openmrs.fieldGen.type");
			request.removeAttribute("org.openmrs.fieldGen.formFieldName");
			request.removeAttribute("org.openmrs.fieldGen.object");
			request.removeAttribute("org.openmrs.fieldGen.parameters");
			request.removeAttribute("org.openmrs.fieldGen.parameterMap");
		}
		
		return new ModelAndView(fieldGenPath, "model", model);

	}
}
