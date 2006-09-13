package org.openmrs.web.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class PseudoStaticContentController implements Controller {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private Boolean interpretJstl = false;
	
	public Boolean getInterpretJstl() {
		return interpretJstl;
	}

	public void setInterpretJstl(Boolean interpretJstl) {
		this.interpretJstl = interpretJstl;
	}

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getServletPath() + request.getPathInfo();
		if (interpretJstl)
			path += ".withjstl";
		return new ModelAndView(path);
	}
}
