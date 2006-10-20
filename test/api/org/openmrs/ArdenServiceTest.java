package org.openmrs;

import java.util.Iterator;

import org.openmrs.api.context.Context;
import org.openmrs.arden.ArdenDataSource;
import org.openmrs.arden.ArdenRule;
import org.openmrs.arden.DefaultArdenDataSource;
import org.openmrs.arden.compiled.*;

public class ArdenServiceTest extends BaseTest {
	int MAX_MLM = 1000;
	
	public void testClass() throws Exception {

		Integer pid = 1;
		Patient patient;
		ArdenDataSource dataSource;
		
		
		startup();
		Context.authenticate("vibha", "chicachica");
		
	//	Context.getArdenService().compileFile("test/arden test/6.mlm"); 
	//	Context.getArdenService().compileFile("test/arden test/directexphiriskcountry.mlm"); 
	//	Context.getArdenService().compileFile("test/arden test/directtbcontact.mlm");
		
		Context.getArdenService().compileFile("test/arden test/HiRiskLeadScreen.mlm");

 		patient = Context.getPatientService().getPatient(pid);
		dataSource = new DefaultArdenDataSource(); 

 /*		
		ArdenRule [] mlms = {
				 new HiRiskLeadScreen(patient ,dataSource)
//				,new directtbcontact(patient, dataSource)
					
		};
		for (int i = 0; i < mlms.length; i++){
			
			ArdenRule mlm = mlms[i].getInstance();
			if(mlm != null) {
				if(mlm.evaluate()) {
					System.out.println(mlm.doAction());
					mlm.printDebug();
				}
				
			}
		}
*/	
		shutdown();
	}
	
	
}