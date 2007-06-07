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

	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		super.onSetUpBeforeTransaction();
		authenticate();
	}

	public void testClass() throws Exception {

		Locale locale = Context.getLocale();

		Patient patient = new Patient();
		Set<Obs> myObs;
		patient.setPatientId(1);

		List<ConceptWord> conceptsWords;
		conceptsWords = Context.getConceptService().findConcepts("CD4 COUNT",
				locale, false);
		if (!conceptsWords.isEmpty()) {
			ConceptWord conceptWord = conceptsWords.get(0);
			Concept c = conceptWord.getConcept();
			myObs = Context.getObsService().getObservations(patient, c, true);
			Iterator iter = myObs.iterator();
			while (iter.hasNext()) {
				Obs o = (Obs) iter.next();
				log.error(o.getValueAsString(locale));
			}
			log.error("Total Obs: " + myObs.size());
		} else
			log.error("Couldn't find a concept named 'CD4 COUNT'");
	}

}