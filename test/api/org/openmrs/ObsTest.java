package org.openmrs;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;


public class ObsTest extends BaseTest {
	
	private Log log = LogFactory.getLog(this.getClass());

	public void testClass() throws Exception {
		
		startup();
		
		Context.authenticate("admin", "test");
		Locale locale = Context.getLocale();
		
		Patient patient = new Patient();
		Set <Obs> MyObs ;
		patient.setPatientId(1);
		
		List <ConceptWord>  conceptsWords;
		conceptsWords = Context.getConceptService().findConcepts("CD4 COUNT", locale, false);
		if (!conceptsWords.isEmpty()) {
			    ConceptWord conceptWord = conceptsWords.get(0);
			    Concept c = conceptWord.getConcept(); 
				MyObs = Context.getObsService().getObservations(patient, c);
				Iterator iter = MyObs.iterator();
				while(iter.hasNext())
			{
				Obs o =(Obs) iter.next();
				
			    log.error(o.getValueAsString(locale));
			 //   iter.remove();
			}
			log.error("Total Obs: " + MyObs.size());
		}
		else
			log.error("Couldn't find a concept named 'CD4 COUNT'");
		
		shutdown();
	}
	
}