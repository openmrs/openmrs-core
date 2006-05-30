package org.openmrs;

import junit.framework.TestCase;
import java.io.*;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextFactory;
import org.openmrs.api.db.hibernate.HibernateUtil;
import java.util.*;
import java.util.Locale;
import java.util.Iterator;

import antlr.CommonAST;
import antlr.collections.AST;
import antlr.*;
import org.openmrs.arden.ArdenBaseLexer;
import org.openmrs.arden.ArdenBaseParser;
import org.openmrs.arden.ArdenBaseTreeParser;
import org.openmrs.arden.MLMObject;
import org.openmrs.arden.compiled.*;
import org.openmrs.api.*;


public class ArdenServiceTest extends TestCase {

	public void testClass() throws Exception {
		
		HibernateUtil.startup();
		Context context = ContextFactory.getContext();
		context.authenticate("vibha", "chicachica");
		
		context.getArdenService().compileFile("test/arden test/HiRiskLeadScreen.mlm"); 
		
	//	context.getArdenService().compileFile("test/arden test/6.mlm");
		
	//	HiRiskLeadScreen mlm = new HiRiskLeadScreen(context,1,context.getLocale());
	//	mlm.run();	
				
		HibernateUtil.shutdown();
	}
	
	
}