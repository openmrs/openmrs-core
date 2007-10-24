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
package org.openmrs.synchronization;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDerived;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSource;
import org.openmrs.ConceptStateConversion;
import org.openmrs.ConceptSynonym;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Privilege;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.Role;
import org.openmrs.Tribe;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.Item;
import org.openmrs.serialization.Record;
import org.openmrs.serialization.TimestampNormalizer;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.filter.SyncServerClass;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 */
public class SyncUtil {

	private static Log log = LogFactory.getLog(SyncUtil.class);		
	    
	public static Object getRootObject(String incoming)
			throws Exception {
		
		Object o = null;
		
		if ( incoming != null ) {
			Record xml = Record.create(incoming);
			Item root = xml.getRootItem();
			String className = root.getNode().getNodeName();
			o = SyncUtil.newObject(className);
		}
		
		return o;
	}

	public static NodeList getChildNodes(String incoming)
			throws Exception {
		NodeList nodes = null;
		
		if ( incoming != null ) {
			Record xml = Record.create(incoming);
			Item root = xml.getRootItem();
			nodes = root.getNode().getChildNodes();
		}
		
		return nodes;
	}

	public static void setProperty(Object o, Node n, ArrayList<Field> allFields ) 
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String propName = n.getNodeName();
		Object propVal = null;
		propVal = SyncUtil.valForField(propName, n.getTextContent(), allFields);
		
		if ( propVal !=  null ) {
			SyncUtil.setProperty(o, propName, propVal);
			log.debug("Successfully called set" + SyncUtil.propCase(propName) + "(" + propVal + ")" );
		}
	}

	public static void setProperty(Object o, String propName, Object propVal)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object[] setterParams = new Object[1];
		setterParams[0] = propVal;
		Method m = SyncUtil.getSetterMethod(o.getClass(), propName, propVal.getClass());

        Object voidObj = m.invoke(o, setterParams);
	}
	
	public static String getAttribute(NodeList nodes, String attName, ArrayList<Field> allFields ) {
		String ret = null;
		if ( nodes != null && attName != null ) {
			for ( int i = 0; i < nodes.getLength(); i++ ) {
				Node n = nodes.item(i);
				String propName = n.getNodeName();
				if ( attName.equals(propName) ) {
					Object obj = SyncUtil.valForField(propName, n.getTextContent(), allFields);
					if ( obj != null ) ret = obj.toString();
				}
			}
		}

		return ret;
	}
	
	public static String propCase(String text) {
		if ( text != null ) {
			return text.substring(0, 1).toUpperCase() + text.substring(1);
		} else {
			return null;
		}
	}

	public static Object newObject(String className) {
		Object o = null;
		if ( className != null ) {
			try {
				Class clazz = Class.forName(className);
				Constructor ct = clazz.getConstructor();
				o = ct.newInstance();
			} catch (ClassNotFoundException e) {
				log.debug("Could not find class with name " + className);
				e.printStackTrace();
			} catch (SecurityException e) {
				log.debug("Security problem when instantiating new object of type " + className);
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				log.debug("Empty constructor does not exist in " + className);
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				log.debug("Constructor requires arguments in " + className);
				e.printStackTrace();
			} catch (InstantiationException e) {
				log.debug("Could not instantiate class of type " + className);
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				log.debug("No access to constructor in " + className);
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				log.debug("InvokationTargetException while trying to create new " + className);
				e.printStackTrace();
			}
		}
		return o;
	}
	
	public static ArrayList<Field> getAllFields(Object o) {
		Class clazz = o.getClass();
		ArrayList<Field> allFields = new ArrayList<Field>();
		if ( clazz != null ) {
			Field[] nativeFields = clazz.getDeclaredFields();
			Field[] superFields = null;
			Class superClazz = clazz.getSuperclass();
			while ( superClazz != null && !(superClazz.equals(Object.class)) ) {
				// loop through to make sure we get ALL relevant superclasses and their fields
                if (log.isDebugEnabled())
                    log.debug("Now inspecting superclass: " + superClazz.getName());
                
				superFields = superClazz.getDeclaredFields();
				if ( superFields != null ) {
					for ( Field f : superFields ) {
						allFields.add(f);
					}
				}
				superClazz = superClazz.getSuperclass();
			}
			if ( nativeFields != null ) {
				// add native fields
				for ( Field f : nativeFields ) {
					allFields.add(f);
				}
			}
		}

		return allFields;
	}
	
	public static Object getOpenmrsObj(String className, String guid) {
		Object o = null;
		boolean isMethod = true;
		
		//o = (Object)(new String("OPENMRS OBJECT " + className));
		if ( "org.openmrs.Cohort".equals(className) ) o = (Object)(Context.getCohortService().getCohortByGuid(guid));
		else if ( "org.openmrs.ComplexObs".equals(className) ) o = (Object)(Context.getObsService().getComplexObsByGuid(guid));
		else if ( "org.openmrs.Concept".equals(className) ) o = (Object)(Context.getConceptService().getConceptByGuid(guid));
		else if ( "org.openmrs.Concept".equals(className) ) o = (Object)(Context.getConceptService().getConceptByGuid(guid));
		else if ( "org.openmrs.ConceptAnswer".equals(className) ) o = (Object)(Context.getConceptService().getConceptAnswerByGuid(guid));
		else if ( "org.openmrs.ConceptClass".equals(className) ) o = (Object)(Context.getConceptService().getConceptClassByGuid(guid));
		else if ( "org.openmrs.ConceptDatatype".equals(className) ) o = (Object)(Context.getConceptService().getConceptDatatypeByGuid(guid));
		else if ( "org.openmrs.ConceptDerived".equals(className) ) o = (Object)(Context.getConceptService().getConceptDerivedByGuid(guid));
		else if ( "org.openmrs.ConceptName".equals(className) ) o = (Object)(Context.getConceptService().getConceptNameByGuid(guid));
		else if ( "org.openmrs.ConceptNumeric".equals(className) ) o = (Object)(Context.getConceptService().getConceptNumericByGuid(guid));
		else if ( "org.openmrs.ConceptProposal".equals(className) ) o = (Object)(Context.getConceptService().getConceptProposalByGuid(guid));
		else if ( "org.openmrs.ConceptSet".equals(className) ) o = (Object)(Context.getConceptService().getConceptSetByGuid(guid));
		else if ( "org.openmrs.ConceptSetDerived".equals(className) ) o = (Object)(Context.getConceptService().getConceptSetDerivedByGuid(guid));
		else if ( "org.openmrs.ConceptSource".equals(className) ) o = (Object)(Context.getConceptService().getConceptSourceByGuid(guid));
		else if ( "org.openmrs.ConceptStateConversion".equals(className) ) o = (Object)(Context.getProgramWorkflowService().getConceptStateConversionByGuid(guid));
		else if ( "org.openmrs.ConceptSynonym".equals(className) ) o = (Object)(Context.getConceptService().getConceptSynonymByGuid(guid));
		else if ( "org.openmrs.ConceptWord".equals(className) ) o = (Object)(Context.getConceptService().getConceptWordByGuid(guid));
		else if ( "org.openmrs.Drug".equals(className) ) o = (Object)(Context.getConceptService().getDrugByGuid(guid));
		else if ( "org.openmrs.DrugIngredient".equals(className) ) o = (Object)(Context.getConceptService().getDrugIngredientByGuid(guid));
		else if ( "org.openmrs.DrugOrder".equals(className) ) o = (Object)(Context.getOrderService().getOrderByGuid(guid));
		else if ( "org.openmrs.Encounter".equals(className) ) o = (Object)(Context.getEncounterService().getEncounterByGuid(guid));
		else if ( "org.openmrs.EncounterType".equals(className) ) o = (Object)(Context.getEncounterService().getEncounterTypeByGuid(guid));
		else if ( "org.openmrs.Field".equals(className) ) o = (Object)(Context.getFormService().getFieldByGuid(guid));
		else if ( "org.openmrs.FieldAnswer".equals(className) ) o = (Object)(Context.getFormService().getFieldAnswerByGuid(guid));
		else if ( "org.openmrs.FieldType".equals(className) ) o = (Object)(Context.getFormService().getFieldTypeByGuid(guid));
		else if ( "org.openmrs.Form".equals(className) ) o = (Object)(Context.getFormService().getFormByGuid(guid));
		else if ( "org.openmrs.FormField".equals(className) ) o = (Object)(Context.getFormService().getFormFieldByGuid(guid));
		else if ( "org.openmrs.GlobalProperty".equals(className) ) o = (Object)(Context.getAdministrationService().getGlobalPropertyByGuid(guid));
		else if ( "org.openmrs.Location".equals(className) ) o = (Object)(Context.getEncounterService().getLocationByGuid(guid));
		else if ( "org.openmrs.MimeType".equals(className) ) o = (Object)(Context.getObsService().getMimeTypeByGuid(guid));
		else if ( "org.openmrs.Obs".equals(className) ) o = (Object)(Context.getObsService().getObsByGuid(guid));
		else if ( "org.openmrs.Order".equals(className) ) o = (Object)(Context.getOrderService().getOrderByGuid(guid));
		else if ( "org.openmrs.OrderType".equals(className) ) o = (Object)(Context.getOrderService().getOrderTypeByGuid(guid));
		else if ( "org.openmrs.Patient".equals(className) ) o = (Object)(Context.getPatientService().getPatientByGuid(guid));
		else if ( "org.openmrs.PatientIdentifierType".equals(className) ) o = (Object)(Context.getPatientService().getPatientIdentifierTypeByGuid(guid));
		else if ( "org.openmrs.PatientIdentifier".equals(className) ) o = (Object)(Context.getPatientService().getPatientIdentifierByGuid(guid));
		else if ( "org.openmrs.PatientProgram".equals(className) ) o = (Object)(Context.getProgramWorkflowService().getPatientProgramByGuid(guid));
		else if ( "org.openmrs.PatientState".equals(className) ) o = (Object)(Context.getProgramWorkflowService().getPatientStateByGuid(guid));
		else if ( "org.openmrs.Person".equals(className) ) o = (Object)(Context.getPersonService().getPersonByGuid(guid));
		else if ( "org.openmrs.PersonAddress".equals(className) ) o = (Object)(Context.getPersonService().getPersonAddressByGuid(guid));
		else if ( "org.openmrs.PersonAttribute".equals(className) ) o = (Object)(Context.getPersonService().getPersonAttributeByGuid(guid));
		else if ( "org.openmrs.PersonAttributeType".equals(className) ) o = (Object)(Context.getPersonService().getPersonAttributeTypeByGuid(guid));
		else if ( "org.openmrs.PersonName".equals(className) ) o = (Object)(Context.getPersonService().getPersonNameByGuid(guid));
		else if ( "org.openmrs.Privilege".equals(className) ) o = (Object)(Context.getUserService().getPrivilegeByGuid(guid));
		else if ( "org.openmrs.Program".equals(className) ) o = (Object)(Context.getProgramWorkflowService().getProgramByGuid(guid));
		else if ( "org.openmrs.ProgramWorkflow".equals(className) ) o = (Object)(Context.getProgramWorkflowService().getWorkflowByGuid(guid));
		else if ( "org.openmrs.ProgramWorkflowState".equals(className) ) o = (Object)(Context.getProgramWorkflowService().getStateByGuid(guid));
		else if ( "org.openmrs.Relationship".equals(className) ) o = (Object)(Context.getPersonService().getRelationshipByGuid(guid));
		else if ( "org.openmrs.RelationshipType".equals(className) ) o = (Object)(Context.getPersonService().getRelationshipTypeByGuid(guid));
		else if ( "org.openmrs.Role".equals(className) ) o = (Object)(Context.getUserService().getRoleByGuid(guid));
		else if ( "org.openmrs.Tribe".equals(className) ) o = (Object)(Context.getPatientService().getTribeByGuid(guid));
		else if ( "org.openmrs.User".equals(className) ) o = (Object)(Context.getUserService().getUserByGuid(guid));
		else {
			isMethod = false;
		}
		
        if (log.isDebugEnabled()) {
    		if ( o == null ) {
    			if ( isMethod ) 
                    log.debug("Unable to get an object of type " + className + " with guid " + guid + "; object doesn't exist yet");
    			else 
                    log.debug("NO GUID-GETTING method for type " + className + " found"); 
    			// not sure this is ever a good idea.  by default should return null
    			//o = newObject(className);
    		}			
    		else 
                log.debug("Found " + className + " in db with GUID " + guid);
        }
		return o;
	}
	
	public static Object valForField(String fieldName, String fieldVal, ArrayList<Field> allFields) {
		Object o = null;
		
		for ( Field f : allFields ) {
			//log.debug("field is " + f.getName());
			if ( f.getName().equals(fieldName) ) {
				//log.debug("found Field " + fieldName + " with type is " + f.getGenericType());

				String className = f.getGenericType().toString();
				if ( className.startsWith("class ") ) className = className.substring("class ".length());

				// we have to explicitly create a new value object here because all we have is a string - won't know how to convert
				if ( className.startsWith("org.openmrs.") ) {
					o = getOpenmrsObj(className, fieldVal);
				} else if ( "java.lang.String".equals(className) ) {
					o = (Object)(new String(fieldVal));
                } else if ( "java.lang.Short".equals(className) ) {
                    try {
                        o = (Object)(Short.valueOf(fieldVal));
                    } catch (NumberFormatException nfe) {
                        log.debug("NumberFormatException trying to turn " + fieldVal + " into a Short");
                    }
				} else if ( "java.lang.Integer".equals(className) ) {
					try {
						o = (Object)(Integer.valueOf(fieldVal));
					} catch (NumberFormatException nfe) {
						log.debug("NumberFormatException trying to turn " + fieldVal + " into a Integer");
					}
				} else if ( "java.lang.Long".equals(className) ) {
					try {
						o = (Object)(Long.valueOf(fieldVal));
					} catch (NumberFormatException nfe) {
						log.debug("NumberFormatException trying to turn " + fieldVal + " into a Long");
					}
				} else if ( "java.lang.Float".equals(className) ) {
					try {
						o = (Object)(Float.valueOf(fieldVal));
					} catch (NumberFormatException nfe) {
						log.debug("NumberFormatException trying to turn " + fieldVal + " into a Float");
					}
				} else if ( "java.lang.Double".equals(className) ) {
					try {
						o = (Object)(Double.valueOf(fieldVal));
					} catch (NumberFormatException nfe) {
						log.debug("NumberFormatException trying to turn " + fieldVal + " into a Double");
					}
				} else if ( "java.lang.Boolean".equals(className) ) {
					o = (Object)(Boolean.valueOf(fieldVal));
				} else if ( "java.util.Date".equals(className) ) {
					SimpleDateFormat sdf = new SimpleDateFormat(TimestampNormalizer.DATETIME_MASK);
					Date d;
					try {
						d = sdf.parse(fieldVal);
						o = (Object)(d);
					} catch (ParseException e) {
						log.debug("DateParsingException trying to turn " + fieldVal + " into a date, so retrying with backup mask");
						try {
							SimpleDateFormat sdfBackup = new SimpleDateFormat(TimestampNormalizer.DATETIME_MASK_BACKUP);
							d = sdfBackup.parse(fieldVal);
							o = (Object)(d);
						} catch (ParseException pee) {
							log.debug("Still getting DateParsingException trying to turn " + fieldVal + " into a date, so retrying with backup mask");
						}
					}
				}
			}
		}
		
		if ( o == null ) log.debug("Never found a property named: " + fieldName + " for this class");
		
		return o;
	}

	public static Method getSetterMethod(Class objClass, String propName, Class propValClass) {
		// need to try to get setter, both in this object, and 
		Method m = null;
		String methodName = "set" + propCase(propName);

		while ( m == null && propValClass != null && !propValClass.equals(Object.class)) {
			Class[] setterParamClasses = new Class[1];
			setterParamClasses[0] = propValClass;
			Class clazz = objClass;
			//String methodDisplay = "set" + propCase(propName) + "(" + propValClass + ")";

			// it could be that the method is called with a superclass of propValClass, so loop through supers
			while ( m == null && clazz != null && !clazz.equals(Object.class) ) {
				// it could also be that the setter method itself is in a superclass of objectClass/clazz, so loop through those, too
				//log.debug("Trying to find method " + methodDisplay + " in " + clazz.getName());
				try {
					m = clazz.getMethod(methodName, setterParamClasses);
					//log.debug("Found method " + methodDisplay + " in " + clazz.getName());
				} catch (SecurityException e) {
					m = null;
					//log.debug("SecurityException on " + methodDisplay + " in " + clazz.getName());
					clazz = clazz.getSuperclass();
				} catch (NoSuchMethodException e) {
					m = null;
					//log.debug("NoSuchMethod " + methodDisplay + " in " + clazz.getName());
					clazz = clazz.getSuperclass();
				}
			}
			propValClass = propValClass.getSuperclass();
		}
				
		return m;
	}
	
	public static synchronized String updateOpenmrsObject(Object o, String guid, boolean knownToExist) {
		String ret = null;
		if ( o != null ) {
			String className = o.getClass().getName();
			boolean isUpdated = true;
			
			if ( "org.openmrs.Cohort".equals(className) ) { 
				if ( !knownToExist ) Context.getCohortService().createCohort((Cohort)o);
				else Context.getCohortService().updateCohort((Cohort)o);
			} else if ( "org.openmrs.ComplexObs".equals(className) ) {
				log.debug("UNABLE TO CREATE/UPDATE ComplexObs in Synchronization process - no service method exists");
				isUpdated = false;
			} else if ( "org.openmrs.Concept".equals(className) ) { 
				if ( !knownToExist ) {
					Integer id = Context.getConceptService().getNextAvailableId();
					((Concept)o).setConceptId(id);
					Context.getConceptService().createConcept((Concept)o);
				}
				else Context.getConceptService().updateConcept((Concept)o);
			} else if ( "org.openmrs.ConceptAnswer".equals(className) ) {
				if ( !knownToExist ) Context.getConceptService().createConceptAnswer((ConceptAnswer)o);
				else Context.getConceptService().updateConceptAnswer((ConceptAnswer)o);
			} else if ( "org.openmrs.ConceptClass".equals(className) ) {
				if ( !knownToExist ) Context.getAdministrationService().createConceptClass((ConceptClass)o);
				else Context.getAdministrationService().createConceptClass((ConceptClass)o);
			} else if ( "org.openmrs.ConceptDatatype".equals(className) ) {
				if ( !knownToExist ) Context.getAdministrationService().createConceptDatatype((ConceptDatatype)o);
				else Context.getAdministrationService().updateConceptDatatype((ConceptDatatype)o);
			} else if ( "org.openmrs.ConceptDerived".equals(className) ) {
				if ( !knownToExist ) Context.getConceptService().createConcept((ConceptDerived)o);
				else Context.getConceptService().updateConcept((ConceptDerived)o);
			} else if ( "org.openmrs.ConceptName".equals(className) ) {
				if ( !knownToExist ) Context.getConceptService().createConceptName((ConceptName)o);
				else Context.getConceptService().updateConceptName((ConceptName)o);
			} else if ( "org.openmrs.ConceptNumeric".equals(className) ) {
				if ( !knownToExist ) Context.getConceptService().createConcept((ConceptNumeric)o);
				else Context.getConceptService().updateConcept((ConceptNumeric)o);
			} else if ( "org.openmrs.ConceptProposal".equals(className) ) {
				if ( !knownToExist ) Context.getAdministrationService().createConceptProposal((ConceptProposal)o);
				else Context.getAdministrationService().updateConceptProposal((ConceptProposal)o);
			} else if ( "org.openmrs.ConceptSet".equals(className) ) {
				if ( !knownToExist ) Context.getConceptService().createConceptSet((ConceptSet)o);
				else Context.getConceptService().updateConceptSet((ConceptSet)o);
			} else if ( "org.openmrs.ConceptSetDerived".equals(className) ) {
				log.debug("UNABLE TO CREATE/UPDATE ConceptSetDerived in Synchronization process - no service method exists");
				isUpdated = false;
			} else if ( "org.openmrs.ConceptSource".equals(className) ) {
				if ( !knownToExist ) Context.getConceptService().createConceptSource((ConceptSource)o);
				else Context.getConceptService().updateConceptSource((ConceptSource)o);
			} else if ( "org.openmrs.ConceptStateConversion".equals(className) ) {
				if ( !knownToExist ) Context.getProgramWorkflowService().createConceptStateConversion((ConceptStateConversion)o);
				else Context.getProgramWorkflowService().updateConceptStateConversion((ConceptStateConversion)o);
			} else if ( "org.openmrs.ConceptSynonym".equals(className) ) {
				if ( !knownToExist ) Context.getConceptService().createConceptSynonym((ConceptSynonym)o);
				else Context.getConceptService().updateConceptSynonym((ConceptSynonym)o);
			} else if ( "org.openmrs.ConceptWord".equals(className) ) {
				log.debug("UNABLE TO CREATE/UPDATE ConceptWord in Synchronization process - no service method exists");
				isUpdated = false;
			} else if ( "org.openmrs.Drug".equals(className) ) {
				if ( !knownToExist ) Context.getConceptService().createDrug((Drug)o);
				else Context.getConceptService().updateDrug((Drug)o);
			} else if ( "org.openmrs.DrugIngredient".equals(className) ) {
				log.debug("UNABLE TO CREATE/UPDATE DrugIngredient in Synchronization process - no service method exists");
				isUpdated = false;
			} else if ( "org.openmrs.DrugOrder".equals(className) ) {
				if ( !knownToExist ) Context.getOrderService().createOrder((DrugOrder)o);
				else Context.getOrderService().createOrder((DrugOrder)o);
			} else if ( "org.openmrs.Encounter".equals(className) ) {
				if ( !knownToExist ) Context.getEncounterService().createEncounter((Encounter)o);
				else Context.getEncounterService().updateEncounter((Encounter)o);
			} else if ( "org.openmrs.EncounterType".equals(className) ) {
				if ( !knownToExist ) Context.getAdministrationService().createEncounterType((EncounterType)o);
				else Context.getAdministrationService().updateEncounterType((EncounterType)o);
			} else if ( "org.openmrs.Field".equals(className) ) {
				if ( !knownToExist ) Context.getFormService().createField((org.openmrs.Field)o);
				else Context.getFormService().updateField((org.openmrs.Field)o);
			} else if ( "org.openmrs.FieldAnswer".equals(className) ) {
				log.debug("UNABLE TO CREATE/UPDATE FieldAnswer in Synchronization process - no service method exists");
				isUpdated = false;
			} else if ( "org.openmrs.FieldType".equals(className) ) {
				if ( !knownToExist ) Context.getAdministrationService().createFieldType((FieldType)o);
				else Context.getAdministrationService().updateFieldType((FieldType)o);
			} else if ( "org.openmrs.Form".equals(className) ) {
				if ( !knownToExist ) Context.getFormService().createForm((Form)o);
				else Context.getFormService().updateForm((Form)o);
			} else if ( "org.openmrs.FormField".equals(className) ) {
				if ( !knownToExist ) Context.getFormService().createFormField((FormField)o);
				else Context.getFormService().updateFormField((FormField)o);
			} else if ( "org.openmrs.GlobalProperty".equals(className) ) {
				log.debug("UNABLE TO CREATE/UPDATE GlobalProperty in Synchronization process - no service method exists");
				isUpdated = false;
			} else if ( "org.openmrs.Location".equals(className) ) { 
				if ( !knownToExist ) Context.getAdministrationService().createLocation((Location)o);
				else Context.getAdministrationService().updateLocation((Location)o);
			} else if ( "org.openmrs.MimeType".equals(className) ) {
				if ( !knownToExist ) Context.getAdministrationService().createMimeType((MimeType)o);
				else Context.getAdministrationService().updateMimeType((MimeType)o);
			} else if ( "org.openmrs.Obs".equals(className) ) {
				if ( !knownToExist ) Context.getObsService().createObs((Obs)o);
				else Context.getObsService().updateObs((Obs)o);
			} else if ( "org.openmrs.Order".equals(className) ){
				if ( !knownToExist ) Context.getOrderService().createOrder((DrugOrder)o);
				else Context.getOrderService().createOrder((DrugOrder)o);
			} else if ( "org.openmrs.OrderType".equals(className) ){
				if ( !knownToExist ) Context.getOrderService().createOrderType((OrderType)o);
				else Context.getOrderService().updateOrderType((OrderType)o);
			} else if ( "org.openmrs.Patient".equals(className) ){
				if ( !knownToExist ) Context.getPatientService().createPatientNoChecks((Patient)o);
				else Context.getPatientService().updatePatientNoChecks((Patient)o);
			} else if ( "org.openmrs.PatientIdentifierType".equals(className) ) {
				if ( !knownToExist ) Context.getAdministrationService().createPatientIdentifierType((PatientIdentifierType)o);
				else Context.getAdministrationService().updatePatientIdentifierType((PatientIdentifierType)o);
			} else if ( "org.openmrs.PatientIdentifier".equals(className) ) {
				if ( !knownToExist ) Context.getPatientService().createPatientIdentifierSync((PatientIdentifier)o);
				else Context.getPatientService().updatePatientIdentifierSync((PatientIdentifier)o);
			} else if ( "org.openmrs.PatientProgram".equals(className) ) {
				if ( !knownToExist ) Context.getProgramWorkflowService().createPatientProgram((PatientProgram)o);
				else Context.getProgramWorkflowService().updatePatientProgram((PatientProgram)o);
			} else if ( "org.openmrs.PatientState".equals(className) ) {
				if ( !knownToExist ) Context.getProgramWorkflowService().createPatientState((PatientState)o);
				else Context.getProgramWorkflowService().updatePatientState((PatientState)o);
			} else if ( "org.openmrs.Person".equals(className) ) {
				if ( !knownToExist ) Context.getPersonService().createPerson((Person)o);
				else Context.getPersonService().updatePerson((Person)o);
			} else if ( "org.openmrs.PersonAddress".equals(className) ) {
				if ( !knownToExist ) Context.getPersonService().createPersonAddress((PersonAddress)o);
				else Context.getPersonService().updatePersonAddress((PersonAddress)o);
			} else if ( "org.openmrs.PersonAttribute".equals(className) ) {
				if ( !knownToExist ) Context.getPersonService().createPersonAttribute((PersonAttribute)o);
				else Context.getPersonService().updatePersonAttribute((PersonAttribute)o);
			} else if ( "org.openmrs.PersonAttributeType".equals(className) ) {
				if ( !knownToExist ) Context.getPersonService().createPersonAttributeType((PersonAttributeType)o);
				else Context.getPersonService().updatePersonAttributeType((PersonAttributeType)o);
			} else if ( "org.openmrs.PersonName".equals(className) ) {
				if ( !knownToExist ) Context.getPersonService().createPersonName((PersonName)o);
				else Context.getPersonService().updatePersonName((PersonName)o);
			} else if ( "org.openmrs.Privilege".equals(className) ) {
				if ( !knownToExist ) Context.getAdministrationService().createPrivilege((Privilege)o);
				else Context.getAdministrationService().updatePrivilege((Privilege)o);
			} else if ( "org.openmrs.Program".equals(className) ) {
				if ( !knownToExist ) Context.getProgramWorkflowService().createOrUpdateProgram((Program)o);
				else Context.getProgramWorkflowService().createOrUpdateProgram((Program)o);
			} else if ( "org.openmrs.ProgramWorkflow".equals(className) ) {
				if ( !knownToExist ) Context.getProgramWorkflowService().createWorkflow((ProgramWorkflow)o);
				else Context.getProgramWorkflowService().updateWorkflow((ProgramWorkflow)o);
			} else if ( "org.openmrs.ProgramWorkflowState".equals(className) ) {
				if ( !knownToExist ) Context.getProgramWorkflowService().createState((ProgramWorkflowState)o);
				else Context.getProgramWorkflowService().updateState((ProgramWorkflowState)o);
			} else if ( "org.openmrs.Relationship".equals(className) ) {
				if ( !knownToExist ) Context.getPersonService().createRelationship((Relationship)o);
				else Context.getPersonService().updateRelationship((Relationship)o);
			} else if ( "org.openmrs.RelationshipType".equals(className) ) {
				if ( !knownToExist ) Context.getPersonService().createRelationshipType((RelationshipType)o);
				else Context.getPersonService().updateRelationshipType((RelationshipType)o);
			} else if ( "org.openmrs.Role".equals(className) ) {
				if ( !knownToExist ) Context.getAdministrationService().createRole((Role)o);
				else Context.getAdministrationService().updateRole((Role)o);
			} else if ( "org.openmrs.Tribe".equals(className) ) { 
				if ( !knownToExist ) Context.getAdministrationService().createTribe((Tribe)o);
				else Context.getAdministrationService().updateTribe((Tribe)o);
			} else if ( "org.openmrs.User".equals(className) ) {
				if ( !knownToExist ) Context.getUserService().createUser((User)o);
				else Context.getUserService().updateUser((User)o);
			} else {
				log.debug("UNABLE TO CREATE/UPDATE " + className + " in Synchronization process - object not recognized");
				isUpdated = false;
			}

			if ( isUpdated ) {
				SyncRecord record = Context.getSynchronizationService().getLatestRecord();
				if ( record != null ) ret = record.getGuid();
			}
		} else {
			log.debug("ERROR updating OpenMRS object - not all parameters are NOT NULL");
		}
		return ret;
	}
	
}
