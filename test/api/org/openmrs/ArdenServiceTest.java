package org.openmrs;

import org.openmrs.api.context.Context;

public class ArdenServiceTest extends BaseTest {
	int MAX_MLM = 1000;
	
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		super.onSetUpBeforeTransaction();
		authenticate();
	}

	public void testClass() throws Exception {

	//	Integer pid = 1;
	//	Patient patient;
	//	ArdenDataSource dataSource;
		String [] mlmNames = {
			//	"directexphiriskcountry.mlm",
			//	"directtbcontact.mlm",
			//	"indirectexphiriskcountry.mlm",
			//	"indirecttbcontact.mlm",
			//	"HiRiskLeadScreen.mlm",
				"leadspecpws2.mlm"
		};
		String defaultPath = "test/arden test/";
		
		for (int i = 0; i<mlmNames.length; i++) {
			Context.getArdenService().compileFile(defaultPath + mlmNames[i]);
		}
			
	//	Context.getArdenService().compileFile("test/arden test/directexphiriskcountry.mlm");
	

 /*		patient = Context.getPatientService().getPatient(pid);
		dataSource = new DefaultArdenDataSource(); 

 		
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
	}
	
}