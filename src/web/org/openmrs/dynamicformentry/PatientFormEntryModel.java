package org.openmrs.dynamicformentry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;

public class PatientFormEntryModel {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Form form;
	private List<FormField> obsFields;
	private List<FormField> encounterFields;
	private List<Class> obsFieldClasses;
	private List<Class> encounterFieldClasses;
	private List<Object> encounterData;
	private List<Obs> obsData;
	private Patient patient;
	
	public PatientFormEntryModel() { }
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("form=" + form.getFormId());
		for (int i = 0; i < encounterFields.size(); ++i) {
			sb.append(" " + encounterFields.get(i).getField().getName() + " " + encounterFieldClasses.get(i) + " " + encounterData.get(i));
		}
		for (int i = 0; i < obsFields.size(); ++i) {
			sb.append(" " + obsFields.get(i).getField().getName() + " " + obsFieldClasses.get(i) + " " + obsData.get(i));
		}
		return sb.toString();
	}
	
	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public void setLocation(Location l) {
		for (int i = 0; i < encounterFields.size(); ++i)
			if ("location_id".equals(encounterFields.get(i).getField().getAttributeName()))
				encounterData.set(i, l);
	}

	public void setProvider(User u) {
		for (int i = 0; i < encounterFields.size(); ++i)
			if ("provider_id".equals(encounterFields.get(i).getField().getAttributeName()))
				encounterData.set(i, u);
	}
	
	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}

	public List<FormField> getEncounterFields() {
		return encounterFields;
	}

	public void setEncounterFields(List<FormField> encounterFields) {
		this.encounterFields = encounterFields;
		refreshEncounterData();
		refreshClassData();
	}

	public List<FormField> getObsFields() {
		return obsFields;
	}

	public void setObsFields(List<FormField> obsFields) {
		this.obsFields = obsFields;
		refreshObsData();
		refreshClassData();
	}

	public List<Class> getEncounterFieldClasses() {
		return encounterFieldClasses;
	}

	public List<Class> getObsFieldClasses() {
		return obsFieldClasses;
	}

	public List<Obs> getObsData() {
		return obsData;
	}
	
	public List<Object> getEncounterData() {
		return encounterData;
	}
	
	private void refreshObsData() {
		obsData = null;
		if (obsFields != null && obsFields.size() > 0) {
			obsData = new ArrayList<Obs>();
			for (FormField ff : obsFields) {
				Obs o = new Obs();
				Concept c = ff.getField().getConcept();
				if (c.getDatatype().getName().equals("Numeric"))
					c = Context.getConceptService().getConceptNumeric(c.getConceptId());
				o.setConcept(c);
				obsData.add(o);
			}
		}
	}
	
	private void refreshEncounterData() {
		encounterData = null;
		if (encounterFields != null && encounterFields.size() > 0) {
			encounterData = new ArrayList<Object>();
			for (FormField ff : encounterFields)
				encounterData.add(null);
		}
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
			if (c.getDatatype().getName().equals("Numeric")) {
				c = Context.getConceptService().getConceptNumeric(c.getConceptId());
				if ( ((ConceptNumeric) c).isPrecise() )
					return Double.class;
				else
					return Integer.class;
			}
			return String.class; 
		}
		return Object.class;
	}

}
