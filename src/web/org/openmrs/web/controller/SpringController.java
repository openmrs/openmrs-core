package org.openmrs.web.controller;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

public class SpringController implements Controller {

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
    	String path = request.getServletPath();
    	if (path.endsWith("htm"))
    		path = path.replace(".htm", "");
    	else if (path.endsWith("jsp"))
    		path = path.replace(".jsp", "");
    	int qmark = path.indexOf("?");
    	return new ModelAndView(path);
        
    }
}