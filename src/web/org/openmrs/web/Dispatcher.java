package org.openmrs.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.context.Context;
import org.openmrs.context.ContextFactory;

public class Dispatcher extends HttpServlet {

	Context context;
	
	protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		
		context = ContextFactory.getContext();
		context.startTransaction();
		
		super.doGet(arg0, arg1);
	}

	
	protected void doPost(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		
		context.endTransaction();
		
		super.doPost(arg0, arg1);
	}
	
	

}
