/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.reporting;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptNumeric;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PersonAttributeType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.User;
import org.openmrs.api.PatientSetService;
import org.openmrs.propertyeditor.CohortEditor;
import org.openmrs.propertyeditor.ConceptAnswerEditor;
import org.openmrs.propertyeditor.ConceptEditor;
import org.openmrs.propertyeditor.ConceptNumericEditor;
import org.openmrs.propertyeditor.DrugEditor;
import org.openmrs.propertyeditor.EncounterEditor;
import org.openmrs.propertyeditor.EncounterTypeEditor;
import org.openmrs.propertyeditor.LocationEditor;
import org.openmrs.propertyeditor.PatientEditor;
import org.openmrs.propertyeditor.PersonAttributeTypeEditor;
import org.openmrs.propertyeditor.ProgramEditor;
import org.openmrs.propertyeditor.ProgramWorkflowEditor;
import org.openmrs.propertyeditor.ProgramWorkflowStateEditor;
import org.openmrs.propertyeditor.UserEditor;

public class ReportObjectXMLEncoder {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private Object objectToEncode;
	
	public ReportObjectXMLEncoder(Object objectToEncode) {
		this.objectToEncode = objectToEncode;
	}
	
	@SuppressWarnings("unchecked")
	public String toXmlString() {
		ByteArrayOutputStream arr = new ByteArrayOutputStream();
		EnumDelegate enumDelegate = new EnumDelegate();
		
		XMLEncoder enc = new XMLEncoder(new BufferedOutputStream(arr));
		enc.setPersistenceDelegate(User.class, new UserDelegate());
		enc.setPersistenceDelegate(Location.class, new LocationDelegate());
		enc.setPersistenceDelegate(Cohort.class, new CohortDelegate());
		enc.setPersistenceDelegate(Concept.class, new ConceptDelegate());
		enc.setPersistenceDelegate(Drug.class, new DrugDelegate());
		enc.setPersistenceDelegate(Encounter.class, new EncounterDelegate());
		enc.setPersistenceDelegate(Patient.class, new PatientDelegate());
		enc.setPersistenceDelegate(Program.class, new ProgramDelegate());
		enc.setPersistenceDelegate(ProgramWorkflow.class, new ProgramWorkflowDelegate());
		enc.setPersistenceDelegate(ProgramWorkflowState.class, new ProgramWorkflowStateDelegate());
		enc.setPersistenceDelegate(ConceptAnswer.class, new ConceptAnswerDelegate());
		enc.setPersistenceDelegate(EncounterType.class, new EncounterTypeDelegate());
		enc.setPersistenceDelegate(PersonAttributeType.class, new PersonAttributeTypeDelegate());
		enc.setPersistenceDelegate(ConceptNumeric.class, new ConceptNumericDelegate());
		
		Set<Class> alreadyAdded = new HashSet<Class>();
		{
			List<Class> enumClasses = new ArrayList<Class>();
			enumClasses.add(PatientSetService.Modifier.class);
			enumClasses.add(PatientSetService.TimeModifier.class);
			enumClasses.add(PatientSetService.BooleanOperator.class);
			enumClasses.add(PatientSetService.GroupMethod.class);
			for (Class clz : enumClasses) {
				enc.setPersistenceDelegate(clz, enumDelegate);
				alreadyAdded.add(clz);
			}
			
		}
		// This original implementation won't handle enums that aren't direct properties of the bean, but I'm leaving it here anyway.
		for (Field f : this.objectToEncode.getClass().getDeclaredFields()) {
			Class clz = f.getType();
			if (clz.isEnum() && !alreadyAdded.contains(clz)) {
				try {
					enc.setPersistenceDelegate(clz, enumDelegate);
					alreadyAdded.add(clz);
				}
				catch (Exception e) {
					log.error("ReportObjectXMLEncoder failed to write enumeration " + f.getName(), e);
				}
			}
		}
		log.debug("objectToEncode.type: " + objectToEncode.getClass());
		enc.writeObject(this.objectToEncode);
		enc.close();
		
		return arr.toString();
	}
	
	/**
	 * @return Returns the objectToEncode.
	 */
	public Object getObjectToEncode() {
		return objectToEncode;
	}
	
	/**
	 * @param objectToEncode The objectToEncode to set.
	 */
	public void setObjectToEncode(Object objectToEncode) {
		this.objectToEncode = objectToEncode;
	}
	
	class EnumDelegate extends DefaultPersistenceDelegate {
		
		@SuppressWarnings("unchecked")
		protected Expression instantiate(Object oldInstance, Encoder out) {
			return new Expression(Enum.class, "valueOf",
			        new Object[] { oldInstance.getClass(), ((Enum) oldInstance).name() });
		}
		
		protected boolean mutatesTo(Object oldInstance, Object newInstance) {
			return oldInstance == newInstance;
		}
	}
	
	class UserDelegate extends DefaultPersistenceDelegate {
		
		protected Expression instantiate(Object oldInstance, Encoder out) {
			log.debug("INSTANTIATING USER DELEGATE");
			UserEditor editor = new UserEditor();
			log.debug("OLD INSTANCE IS " + oldInstance);
			editor.setAsText(oldInstance.toString());
			return new Expression(editor, "getValue", null);
		}
		
		protected boolean mutatesTo(Object oldInstance, Object newInstance) {
			return oldInstance == newInstance;
		}
		
		/*
		@Override
		public void writeObject(Object obj, Encoder encoder) {
		   log.debug("IN WRITEOBJECT METHOD FOR USER");
		   User user = new User();
		   user.setUserId(((User)obj).getUserId());
		   super.writeObject(user, encoder);
		}
		*/
	}
	
	class LocationDelegate extends DefaultPersistenceDelegate {
		
		protected Expression instantiate(Object oldInstance, Encoder out) {
			LocationEditor editor = new LocationEditor();
			Location location = (Location) oldInstance;
			editor.setAsText(location.getLocationId().toString());
			return new Expression(editor, "getValue", null);
		}
		
		protected boolean mutatesTo(Object oldInstance, Object newInstance) {
			return oldInstance == newInstance;
		}
		
		@Override
		public void writeObject(Object obj, Encoder encoder) {
			Location location = new Location();
			location.setLocationId(((Location) obj).getLocationId());
			super.writeObject(location, encoder);
		}
	}
	
	class CohortDelegate extends DefaultPersistenceDelegate {
		
		protected Expression instantiate(Object oldInstance, Encoder out) {
			CohortEditor editor = new CohortEditor();
			Cohort cohort = (Cohort) oldInstance;
			editor.setAsText(cohort.getCohortId().toString());
			return new Expression(editor, "getValue", null);
		}
		
		protected boolean mutatesTo(Object oldInstance, Object newInstance) {
			return oldInstance == newInstance;
		}
	}
	
	class ConceptDelegate extends DefaultPersistenceDelegate {
		
		protected Expression instantiate(Object oldInstance, Encoder out) {
			ConceptEditor editor = new ConceptEditor();
			Concept concept = (Concept) oldInstance;
			editor.setAsText(concept.getConceptId().toString());
			return new Expression(editor, "getValue", null);
		}
		
		protected boolean mutatesTo(Object oldInstance, Object newInstance) {
			return oldInstance == newInstance;
		}
	}
	
	class DrugDelegate extends DefaultPersistenceDelegate {
		
		protected Expression instantiate(Object oldInstance, Encoder out) {
			DrugEditor editor = new DrugEditor();
			Drug drug = (Drug) oldInstance;
			editor.setAsText(drug.getDrugId().toString());
			return new Expression(editor, "getValue", null);
		}
		
		protected boolean mutatesTo(Object oldInstance, Object newInstance) {
			return oldInstance == newInstance;
		}
	}
	
	class EncounterDelegate extends DefaultPersistenceDelegate {
		
		protected Expression instantiate(Object oldInstance, Encoder out) {
			EncounterEditor editor = new EncounterEditor();
			Encounter encounter = (Encounter) oldInstance;
			editor.setAsText(encounter.getEncounterId().toString());
			return new Expression(editor, "getValue", null);
		}
		
		protected boolean mutatesTo(Object oldInstance, Object newInstance) {
			return oldInstance == newInstance;
		}
	}
	
	class PatientDelegate extends DefaultPersistenceDelegate {
		
		protected Expression instantiate(Object oldInstance, Encoder out) {
			PatientEditor editor = new PatientEditor();
			Patient patient = (Patient) oldInstance;
			editor.setAsText(patient.getPatientId().toString());
			return new Expression(editor, "getValue", null);
		}
		
		protected boolean mutatesTo(Object oldInstance, Object newInstance) {
			return oldInstance == newInstance;
		}
	}
	
	class ProgramDelegate extends DefaultPersistenceDelegate {
		
		protected Expression instantiate(Object oldInstance, Encoder out) {
			ProgramEditor editor = new ProgramEditor();
			Program program = (Program) oldInstance;
			editor.setAsText(program.getProgramId().toString());
			return new Expression(editor, "getValue", null);
		}
		
		protected boolean mutatesTo(Object oldInstance, Object newInstance) {
			return oldInstance == newInstance;
		}
	}
	
	class ProgramWorkflowDelegate extends DefaultPersistenceDelegate {
		
		protected Expression instantiate(Object oldInstance, Encoder out) {
			ProgramWorkflowEditor editor = new ProgramWorkflowEditor();
			ProgramWorkflow programWorkflow = (ProgramWorkflow) oldInstance;
			editor.setAsText(programWorkflow.getProgramWorkflowId().toString());
			return new Expression(editor, "getValue", null);
		}
		
		protected boolean mutatesTo(Object oldInstance, Object newInstance) {
			return oldInstance == newInstance;
		}
	}
	
	class ProgramWorkflowStateDelegate extends DefaultPersistenceDelegate {
		
		protected Expression instantiate(Object oldInstance, Encoder out) {
			ProgramWorkflowStateEditor editor = new ProgramWorkflowStateEditor();
			ProgramWorkflowState programWorkflowState = (ProgramWorkflowState) oldInstance;
			editor.setAsText(programWorkflowState.getProgramWorkflowStateId().toString());
			return new Expression(editor, "getValue", null);
		}
		
		protected boolean mutatesTo(Object oldInstance, Object newInstance) {
			return oldInstance == newInstance;
		}
	}
	
	class ConceptAnswerDelegate extends DefaultPersistenceDelegate {
		
		protected Expression instantiate(Object oldInstance, Encoder out) {
			ConceptAnswerEditor editor = new ConceptAnswerEditor();
			ConceptAnswer conceptAnswer = (ConceptAnswer) oldInstance;
			editor.setAsText(conceptAnswer.getConceptAnswerId().toString());
			return new Expression(editor, "getValue", null);
		}
		
		protected boolean mutatesTo(Object oldInstance, Object newInstance) {
			return oldInstance == newInstance;
		}
	}
	
	class EncounterTypeDelegate extends DefaultPersistenceDelegate {
		
		protected Expression instantiate(Object oldInstance, Encoder out) {
			EncounterTypeEditor editor = new EncounterTypeEditor();
			EncounterType encounterType = (EncounterType) oldInstance;
			editor.setAsText(encounterType.getEncounterTypeId().toString());
			return new Expression(editor, "getValue", null);
		}
		
		protected boolean mutatesTo(Object oldInstance, Object newInstance) {
			return oldInstance == newInstance;
		}
	}
	
	class PersonAttributeTypeDelegate extends DefaultPersistenceDelegate {
		
		protected Expression instantiate(Object oldInstance, Encoder out) {
			PersonAttributeTypeEditor editor = new PersonAttributeTypeEditor();
			PersonAttributeType personAttributeType = (PersonAttributeType) oldInstance;
			editor.setAsText(personAttributeType.getPersonAttributeTypeId().toString());
			return new Expression(editor, "getValue", null);
		}
		
		protected boolean mutatesTo(Object oldInstance, Object newInstance) {
			return oldInstance == newInstance;
		}
	}
	
	class ConceptNumericDelegate extends DefaultPersistenceDelegate {
		
		protected Expression instantiate(Object oldInstance, Encoder out) {
			ConceptNumericEditor editor = new ConceptNumericEditor();
			ConceptNumeric conceptNumeric = (ConceptNumeric) oldInstance;
			editor.setAsText(conceptNumeric.getConceptId().toString());
			return new Expression(editor, "getValue", null);
		}
		
		protected boolean mutatesTo(Object oldInstance, Object newInstance) {
			return oldInstance == newInstance;
		}
	}
}
