package org.openmrs;

import junit.framework.TestCase;

import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextFactory;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.Patient;
import org.openmrs.arden.compiled.*;
import org.openmrs.arden.*;

public class ArdenServiceTest extends TestCase {
	int MAX_MLM = 1;
	
	public void testClass() throws Exception {

		Integer pid = 1;
		Patient patient;
		ArdenDataSource dataSource;
		DSSObject dssObj;
		int all = MAX_MLM;
		
		HibernateUtil.startup();
		Context context = ContextFactory.getContext();
		context.authenticate("vibha", "chicachica");
		
	//	context.getArdenService().compileFile("test/arden test/6.mlm"); 
	//	context.getArdenService().compileFile("test/arden test/directexphiriskcountry.mlm"); 
	//	context.getArdenService().compileFile("test/arden test/directtbcontact.mlm");
		
		context.getArdenService().compileFile("test/arden test/HiRiskLeadScreen.mlm");
		
		
				

 		patient = context.getPatientService().getPatient(pid);
		dataSource = new DefaultArdenDataSource(); 
			
		ArdenRule [] mlms = {
				 new HiRiskLeadScreen(context,patient ,dataSource)
		};
		for (int i = 0; i < all; i++){
			
			ArdenRule mlm = mlms[0].getInstance();
			if(mlm != null) {
				dssObj = mlm.evaluate();
				if(dssObj.getConcludeVal()) {
					System.out.println(dssObj.getPrintString());
				}
				dssObj.PrintObsMap();
				
			}
		}
		HibernateUtil.shutdown();
	}
	
	
}