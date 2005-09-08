package org.openmrs.formentry.web;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class PatientSearchFormController extends SimpleFormController {

	public ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object command,
			BindException errors) throws ServletException {
		SimpleQueryCommand query = (SimpleQueryCommand) command;
		
		List<String> results = new LinkedList<String>();
		results.add("First");
		results.add("Second");
		query.setResults(results);
		
		Map<Object,Object> model = new HashMap<Object,Object>();
		model.put("results", results);
		
		return new ModelAndView("formentry/patientSearch", model);
	}

}
