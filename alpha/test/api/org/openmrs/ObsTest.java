package org.openmrs;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import junit.framework.TestCase;

import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextFactory;
import org.openmrs.api.db.hibernate.HibernateUtil;


public class ObsTest extends TestCase {

	
	public void testClass() throws Exception {
		
		HibernateUtil.startup();
		Context context = ContextFactory.getContext();
		context.authenticate("vibha", "chicachica");
		Locale locale = context.getLocale();
		
		Patient patient = new Patient();
		Set <Obs> MyObs ;
		patient.setPatientId(1);
		
		List <ConceptWord>  conceptsWords;
		conceptsWords = context.getConceptService().findConcepts("BLOOD LEAD LEVEL", locale, false);
		if (!conceptsWords.isEmpty()) {
			    ConceptWord conceptWord = conceptsWords.get(0);
			    Concept c = conceptWord.getConcept(); 
				MyObs = context.getObsService().getObservations(patient, c);
				Iterator iter = MyObs.iterator();
				while(iter.hasNext())
			{
				Obs o =(Obs) iter.next();
				
			    System.out.println(o.getValueAsString(locale));
			 //   iter.remove();
			}
			System.out.println("Total Obs: " + MyObs.size());
		}
		HibernateUtil.shutdown();
	}
	
}