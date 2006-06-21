package org.openmrs;

import junit.framework.TestCase;

import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextFactory;
import org.openmrs.api.db.hibernate.HibernateUtil;


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