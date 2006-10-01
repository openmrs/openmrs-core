package org.openmrs;

import org.openmrs.api.context.Context;
import org.openmrs.arden.ArdenDataSource;
import org.openmrs.arden.ArdenRule;
import org.openmrs.arden.DSSObject;
import org.openmrs.arden.DefaultArdenDataSource;
import org.openmrs.arden.compiled.HiRiskLeadScreen;

public class ArdenServiceTest extends BaseTest {
	int MAX_MLM = 1;
	
	public void testClass() throws Exception {

		Integer pid = 1;
		Patient patient;
		ArdenDataSource dataSource;
		DSSObject dssObj;
		int all = MAX_MLM;
		
		startup();
		Context.authenticate("vibha", "chicachica");
		
	//	Context.getArdenService().compileFile("test/arden test/6.mlm"); 
	//	Context.getArdenService().compileFile("test/arden test/directexphiriskcountry.mlm"); 
	//	Context.getArdenService().compileFile("test/arden test/directtbcontact.mlm");
		
		Context.getArdenService().compileFile("test/arden test/HiRiskLeadScreen.mlm");

 		patient = Context.getPatientService().getPatient(pid);
		dataSource = new DefaultArdenDataSource(); 
			
		ArdenRule [] mlms = {
				 new HiRiskLeadScreen(patient ,dataSource)
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
		shutdown();
	}
	
	
}