package org.openmrs.web.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Redirects the request to the given <code>formView</code>
 * 
 * @author bwolfe
 *
 */
public class RedirectController implements Controller {

	private String redirectView = "";
	
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
    	// default to the current path
    	if (redirectView == null)
    		redirectView = request.getServletPath();
    	
    	return new ModelAndView(redirectView);
    }
    
	public void setRedirectView(String view) {
		this.redirectView = view;
	}
	
	public String getRedirectView() {
		return this.redirectView;
	}
}