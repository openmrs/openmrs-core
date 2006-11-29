package org.openmrs.dynamicformentry;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.formentry.FormEntryConstants;
import org.openmrs.reporting.PatientSet;

public class BatchFormEntryModel {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Form form;
	private PatientSet patientSet;
	private List<FormField> obsFields;
	private List<Class> obsFieldClasses;
	private List<FormField> encounterFields;
	private List<Class> encounterFieldClasses;
	private User provider;
	private Location location;
	private Date encounterDatetime;
	private Map<Integer, List<Object>> encounterData;
	private Map<Integer, List<Obs>> obsData;
	
	public BatchFormEntryModel() { }
		
	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}

	public Date getEncounterDatetime() {
		return encounterDatetime;
	}

	public void setEncounterDatetime(Date encounterDatetime) {
		this.encounterDatetime = encounterDatetime;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public User getProvider() {
		return provider;
	}

	public void setProvider(User provider) {
		this.provider = provider;
	}

	public List<FormField> getEncounterFields() {
		return encounterFields;
	}

	public void setEncounterFields(List<FormField> encounterFields) {
		this.encounterFields = encounterFields;
		refreshEncounterData();
		refreshClassData();
	}

	public List<Class> getEncounterFieldClasses() {
		return encounterFieldClasses;
	}

	public void setEncounterFieldClasses(List<Class> encounterFieldClasses) {
		this.encounterFieldClasses = encounterFieldClasses;
	}

	public List<FormField> getObsFields() {
		return obsFields;
	}

	public void setObsFields(List<FormField> obsFields) {
		this.obsFields = obsFields;
		refreshObsData();
		refreshClassData();
	}

	public List<Class> getObsFieldClasses() {
		return obsFieldClasses;
	}

	public void setObsFieldClasses(List<Class> obsFieldClasses) {
		this.obsFieldClasses = obsFieldClasses;
	}

	public PatientSet getPatientSet() {
		return patientSet;
	}

	public void setPatientSet(PatientSet patientSet) {
		this.patientSet = patientSet;
		refreshObsData();
		refreshEncounterData();
	}

	public Map<Integer, List<Obs>> getObsData() {
		return obsData;
	}
	
	public Map<Integer, List<Object>> getEncounterData() {
		return encounterData;
	}
	
	private void refreshObsData() {
		obsData = null;
		if (patientSet != null && patientSet.size() > 0 && obsFields != null && obsFields.size() > 0) {
			obsData = new HashMap<Integer, List<Obs>>();
			for (Integer ptId : patientSet.getPatientIds()) {
				List<Obs> obs = new ArrayList<Obs>();
				obsData.put(ptId, obs);
				for (FormField ff : obsFields) {
					Obs o = new Obs();
					o.setConcept(ff.getField().getConcept());
				}
			}
		}
	}
	
	private void refreshEncounterData() {
		encounterData = null;
		if (patientSet != null && patientSet.size() > 0 && encounterFields != null && encounterFields.size() > 0) {
			encounterData = new HashMap<Integer, List<Object>>();
			for (Integer ptId : patientSet.getPatientIds()) {
				List<Object> objects = new ArrayList<Object>();
				encounterData.put(ptId, objects);
				for (FormField ff : encounterFields) {
					objects.add("");
				}
			}
		}
	}
	
	public void getFieldsFromForm() {
		List<FormField> obsFields = new ArrayList<FormField>();
		List<FormField> encounterFields = new ArrayList<FormField>();
		Set<FormField> fields = form.getFormFields();
		for (FormField ff : fields) {
			// for now we're only interested in children of the top-level Obs and Encounter fields
			FormField parent = ff.getParent();
			if (parent == null)
				continue;
			if (parent.getField().getName().equals("OBS")) {
				obsFields.add(ff);
			} else if (parent.getField().getName().equals("ENCOUNTER")) {
				encounterFields.add(ff);
			}
		}
		setObsFields(obsFields);
		setEncounterFields(encounterFields);
	}
	
	private Class getAnswerType(FormField ff) {
		if (ff.getField().getTableName() != null) {
			String dbTable = ff.getField().getTableName();
			if (dbTable != null)
				dbTable = dbTable.toLowerCase();
			String dbColumn = ff.getField().getAttributeName();
			if (dbColumn != null)
				dbColumn = dbColumn.toLowerCase();
			log.debug("table/column: " + dbTable + "/" + dbColumn);
			if ("location_id".equals(dbColumn))
				return Location.class;
			if ("provider_id".equals(dbColumn))
				return User.class;
			if (dbColumn != null && dbColumn.endsWith("datetime"))
				return Date.class;
		}
		if (ff.getField().getConcept() != null) {
			Concept c = ff.getField().getConcept();
			log.debug("concept, datatype: " + c.getDatatype().getName());
			if (c.getDatatype().getHl7Abbreviation().equals(FormEntryConstants.HL7_NUMERIC)) {
				c = Context.getConceptService().getConceptNumeric(c.getConceptId());
				if ( ((ConceptNumeric) c).isPrecise() )
					return Double.class;
				else
					return Integer.class;
			} else if (c.getDatatype().getHl7Abbreviation().equals(FormEntryConstants.HL7_DATE) || c.getDatatype().getHl7Abbreviation().equals(FormEntryConstants.HL7_DATETIME)) {
				return Date.class; 
			}
			return String.class; 
		}
		return Object.class;
	}
	
	private void refreshClassData() {
		if (encounterFields != null) {
			encounterFieldClasses = new ArrayList<Class>();
			for (FormField ff : encounterFields)
				encounterFieldClasses.add(getAnswerType(ff));
		}
		if (obsFields != null) {
			obsFieldClasses = new ArrayList<Class>();
			for (FormField ff : obsFields)
				obsFieldClasses.add(getAnswerType(ff));
		}
	}
}
