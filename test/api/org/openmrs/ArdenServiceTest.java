package org.openmrs;

import junit.framework.TestCase;

import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextFactory;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.Patient;
import org.openmrs.arden.compiled.*;
import org.openmrs.arden.*;

public class ArdenServiceTest extends TestCase {

	public void testClass() throws Exception {

		Integer pid = 1;
		Patient patient;
		ArdenDataSource dataSource;
		
		HibernateUtil.startup();
		Context context = ContextFactory.getContext();
		context.authenticate("vibha", "chicachica");
		
		context.getArdenService().compileFile("test/arden test/6.mlm"); 
	//	context.getArdenService().compileFile("test/arden test/directexphiriskcountry.mlm"); 
	//	context.getArdenService().compileFile("test/arden test/directtbcontact.mlm");
		
	//	context.getArdenService().compileFile("test/arden test/HiRiskLeadScreen.mlm");
		
		
				
/*
 		patient = context.getPatientService().getPatient(pid);
		dataSource = new DefaultArdenDataSource(); 
*/
		
/*		HiRiskLeadScreen mlm = new HiRiskLeadScreen(context,patient ,dataSource);
		if(mlm.evaluate()) {
			String str = mlm.action();
			System.out.println(str);
		}
*/	
		
/*		directtbcontact mlm1 = new directtbcontact(context,patient, dataSource);
		if(mlm1.evaluate()) {
				String str = mlm1.action();
				System.out.println(str);
			}
		
*/		
		HibernateUtil.shutdown();
	}
	
	
}