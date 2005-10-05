package org.openmrs.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Dispatcher extends HttpServlet {

	static final long serialVersionUID = 9472334345356L;
	protected final Log log = LogFactory.getLog(getClass());
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		log.debug("Dispatcher: doing GET request");
		
		String selectedScreen = request.getServletPath();
		
		request.getRequestDispatcher(selectedScreen.replace(".html", ".jsp"))
			   .forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		log.debug("Dispatcher: doing POST request");
		
		String selectedScreen = request.getServletPath();
		
		request.getRequestDispatcher(selectedScreen.replace(".html", ".jsp"))
		   .forward(request, response);
	}
	
	public void init() throws ServletException {
		
		log.debug("Dispatcher: Starting init");
	}
	
	public void destroy() {
		log.debug("Dispatcher: Destroying");
	}
	
}
