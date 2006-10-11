package org.openmrs.web.dwr;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.formentry.FormEntryService;

public class DWRFormEntryService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public void enterForm(Integer patientId, Integer formId, boolean useEncounter, List<String> fields) {
		FormService fs = Context.getFormService();
		FormEntryService fes = Context.getFormEntryService();
		Patient patient = fes.getPatient(patientId);
		Form form = fs.getForm(formId);
		Encounter encounter = new Encounter();
		encounter.setPatient(patient);
		encounter.setForm(form);
		encounter.setEncounterType(form.getEncounterType());
		List<Obs> obs = new ArrayList<Obs>();
		for (String temp : fields) {
			EnteredField enteredField = new EnteredField(temp);
			if (enteredField.isEmpty())
				continue;
			Field field = fs.getField(enteredField.getFieldId());
			log.debug("field: " + field);
			if (field.getTableName() != null && field.getTableName().length() > 0) {
				if (field.getTableName().toLowerCase().equals("encounter")) {
					String attrName = field.getAttributeName().toLowerCase();
					if ("location_id".equals(attrName))
						encounter.setLocation(fes.getLocation(Integer.valueOf(enteredField.getValue())));
					else if ("encounter_datetime".equals(attrName)) {
						try {
							encounter.setEncounterDatetime(dateHelper(enteredField.getValue()));
						} catch (ParseException ex) {
							throw new RuntimeException("Error in encounter datetime", ex);
						}
					} else if ("provider_id".equals(attrName)) {
						encounter.setProvider(Context.getUserService().getUser(Integer.valueOf(enteredField.getValue())));
					}
				}
			} else {
				Concept question = field.getConcept();
				log.debug("question: " + question);
				Obs o = new Obs();
				o.setPatient(patient);
				o.setConcept(question);
				try {
					log.debug("o.getConcept() == " + o.getConcept());
					o.setValueAsString(enteredField.getValue());
				} catch (Exception ex) {
					throw new RuntimeException("Can't handle value " + enteredField.getValue() + " for concept " + question, ex);
				}
				if (enteredField.getDateTime() != null) {
					try {
						o.setObsDatetime(dateHelper(enteredField.getDateTime()));	
					} catch (ParseException ex) {
						throw new RuntimeException("Error in obs datetime: " + enteredField.getDateTime(), ex);
					}
				}
				obs.add(o);
			}
		}
		if (useEncounter) {
			for (Obs o : obs) {
				if (o.getObsDatetime() == null)
					o.setObsDatetime(encounter.getEncounterDatetime());
				if (o.getLocation() == null)
					o.setLocation(encounter.getLocation());
				encounter.addObs(o);
			}
			Context.getEncounterService().createEncounter(encounter);
		} else {
			// TODO: need to specify obsDatetime and location in case those are null
			for (Obs o : obs)
				Context.getObsService().createObs(o);
		}
	}
	
	static DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); 
	private static Date dateHelper(String value) throws ParseException {
		if (value == null || value.length() == 0)
			return null;
		else
			return df.parse(value);
	}
}
