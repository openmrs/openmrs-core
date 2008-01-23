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
import java.util.Properties;
import java.util.UUID;

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
import org.openmrs.ConceptWord;
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
		
		log.warn("Trying to set value to " + propVal + " when propName is " + propName + " and context is " + n.getTextContent());
		
		if ( propVal !=  null ) {
			SyncUtil.setProperty(o, propName, propVal);
			log.warn("Successfully called set" + SyncUtil.propCase(propName) + "(" + propVal + ")" );
		}
	}

	public static void setProperty(Object o, String propName, Object propVal)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object[] setterParams = new Object[1];
		setterParams[0] = propVal;
		
		log.warn("getting setter method");
		Method m = SyncUtil.getSetterMethod(o.getClass(), propName, propVal.getClass());

		log.warn("about to call " + m.getName());
		
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

	public static Object newObject(String className) throws Exception {
		Object o = null;
		if ( className != null ) {
				Class clazz = Class.forName(className);
				Constructor ct = clazz.getConstructor();
				o = ct.newInstance();
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
				log.warn("found Field " + fieldName + " with type is " + f.getGenericType());

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

    /**
     * 
     * Finds property 'get' accessor based on target type and property name.
     * 
     * @return Method object matching name and param, else null
     * 
     * @see getPropertyAccessor(Class objType, String methodName, Class propValType)
     */
    public static Method getGetterMethod(Class objType, String propName) {
        String methodName = "get" + propCase(propName);
        return SyncUtil.getPropertyAccessor(objType, methodName, null);
    }

    /**
     * 
     * Finds property 'set' accessor based on target type, property name, and set method parameter type.
     * 
     * @return Method object matching name and param, else null
     * 
     * @see getPropertyAccessor(Class objType, String methodName, Class propValType)
     */
    public static Method getSetterMethod(Class objType, String propName, Class propValType) {
        String methodName = "set" + propCase(propName);
        return SyncUtil.getPropertyAccessor(objType, methodName, propValType);
    }
    
    /**
     * 
     * Constructs a Method object for invocation on instances of objType class 
     * based on methodName and the method parameter type. Handles only propery accessors - thus takes
     * Class propValType and not Class[] propValTypes.
     * <p>
     * If necessary, this implementation traverses both objType and  propValTypes type hierarchies in search for the 
     * method signature match.
     * 
     * @param objType Type to examine.
     * @param methodName Method name.
     * @param propValType Type of the parameter that method takes. If none (i.e. getter), pass null.
     * @return Method object matching name and param, else null
     */
    private static Method getPropertyAccessor(Class objType, String methodName, Class propValType) {
		// need to try to get setter, both in this object, and its parent class 
		Method m = null;
        boolean continueLoop = true;
        
        // Fix - CA - 22 Jan 2008 - extremely odd Java Bean convention that says getter/setter for fields
        // where 2nd letter is capitalized (like "aIsToB") first letter stays lower in getter/setter methods
        // like "getaIsToB()".  Hence we need to try that out too
        String altMethodName = methodName.substring(0, 3) + methodName.substring(3, 4).toLowerCase() + methodName.substring(4);

        try {
			Class[] setterParamClasses = null;
            if (propValType != null) { //it is a setter
                setterParamClasses = new Class[1];
                setterParamClasses[0] = propValType;
            }
			Class clazz = objType;
    
            // it could be that the setter method itself is in a superclass of objectClass/clazz, so loop through those
			while ( continueLoop && m == null && clazz != null && !clazz.equals(Object.class) ) {
				try {
					m = clazz.getMethod(methodName, setterParamClasses);
					continueLoop = false;
					break; //yahoo - we got it using exact type match
				} catch (SecurityException e) {
					m = null;
				} catch (NoSuchMethodException e) {
					m = null;
				}
				
				//not so lucky: try to find method by name, and then compare params for compatibility 
				//instead of looking for the exact method sig match 
                Method[] mes = objType.getMethods();
                for (Method me : mes) {
                	if (me.getName().equals(methodName) || me.getName().equals(altMethodName) ) {
                		Class[] meParamTypes = me.getParameterTypes();
                		if (propValType != null && meParamTypes != null && meParamTypes.length == 1 && meParamTypes[0].isAssignableFrom(propValType)) {
                			m = me;
            				continueLoop = false; //aha! found it
            				break;
                		}
                	}
                }
                
                if ( continueLoop ) clazz = clazz.getSuperclass();
    		}
        }
        catch(Exception ex) {
            //whatever happened, we didn't find the method - return null
            m = null;
            log.warn("Unexpected exception while looking for a Method object, returning null",ex);
        }
        
        if (m == null) {
	        if (log.isWarnEnabled())
	            log.warn("Failed to find matching method. type: " + objType.getName() + ", methodName: " + methodName);
        }
				
		return m;
	}
	
    /**
     * 
     * Uses openmrs API to commit an update to an instance of an openmrs class.
     * 
     * <p>Remarks: This method is used during data synchronization when changes from a server are received and
     * are to be processed and applied to the local data store. As state is parsed out of SyncRecords this method
     * provides a mechanism by which the changes are applied to the database. 
     * 
     * @param o object to be updated
     * @param className identifies openmrs type that o instantiates: normally this will be same as 
     * o.getClass().getName() however in case of dealing with hibernate proxy objects that does not work and we need
     * explicit openmrs type name 
     *
     * @param guid unique id of the object
     * @param knownToExist if true, update method on openmrs API is called using guid, else create method is used
     * @return
     */
	public static synchronized String updateOpenmrsObject(Object o, String className, String guid, boolean knownToExist) {
		String ret = null;
		if ( o != null ) {
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
					Context.getConceptService().createConcept((Concept)o, true);
				}
				else Context.getConceptService().updateConcept((Concept)o, true);
			} else if ( "org.openmrs.ConceptAnswer".equals(className) ) {
				if ( !knownToExist ) Context.getConceptService().createConceptAnswer((ConceptAnswer)o, true);
				else Context.getConceptService().updateConceptAnswer((ConceptAnswer)o, true);
			} else if ( "org.openmrs.ConceptClass".equals(className) ) {
				if ( !knownToExist ) Context.getAdministrationService().createConceptClass((ConceptClass)o, true);
				else Context.getAdministrationService().createConceptClass((ConceptClass)o, true);
			} else if ( "org.openmrs.ConceptDatatype".equals(className) ) {
				if ( !knownToExist ) Context.getAdministrationService().createConceptDatatype((ConceptDatatype)o, true);
				else Context.getAdministrationService().updateConceptDatatype((ConceptDatatype)o, true);
			} else if ( "org.openmrs.ConceptDerived".equals(className) ) {
				if ( !knownToExist ) Context.getConceptService().createConcept((ConceptDerived)o, true);
				else Context.getConceptService().updateConcept((ConceptDerived)o, true);
			} else if ( "org.openmrs.ConceptName".equals(className) ) {
				if ( !knownToExist ) Context.getConceptService().createConceptName((ConceptName)o, true);
				else Context.getConceptService().updateConceptName((ConceptName)o, true);
			} else if ( "org.openmrs.ConceptNumeric".equals(className) ) {
				if ( !knownToExist ) Context.getConceptService().createConcept((ConceptNumeric)o, true);
				else Context.getConceptService().updateConcept((ConceptNumeric)o, true);
			} else if ( "org.openmrs.ConceptProposal".equals(className) ) {
				if ( !knownToExist ) Context.getAdministrationService().createConceptProposal((ConceptProposal)o);
				else Context.getAdministrationService().updateConceptProposal((ConceptProposal)o);
			} else if ( "org.openmrs.ConceptSet".equals(className) ) {
				if ( !knownToExist ) Context.getConceptService().createConceptSet((ConceptSet)o, true);
				else Context.getConceptService().updateConceptSet((ConceptSet)o, true);
			} else if ( "org.openmrs.ConceptSetDerived".equals(className) ) {
				log.debug("UNABLE TO CREATE/UPDATE ConceptSetDerived in Synchronization process - no service method exists");
				isUpdated = false;
			} else if ( "org.openmrs.ConceptSource".equals(className) ) {
				if ( !knownToExist ) Context.getConceptService().createConceptSource((ConceptSource)o, true);
				else Context.getConceptService().updateConceptSource((ConceptSource)o, true);
			} else if ( "org.openmrs.ConceptStateConversion".equals(className) ) {
				if ( !knownToExist ) Context.getProgramWorkflowService().createConceptStateConversion((ConceptStateConversion)o);
				else Context.getProgramWorkflowService().updateConceptStateConversion((ConceptStateConversion)o);
			} else if ( "org.openmrs.ConceptSynonym".equals(className) ) {
				if ( !knownToExist ) Context.getConceptService().createConceptSynonym((ConceptSynonym)o, true);
				else Context.getConceptService().updateConceptSynonym((ConceptSynonym)o, true);
			} else if ( "org.openmrs.ConceptWord".equals(className) ) {
				if ( !knownToExist ) Context.getConceptService().createConceptWord((ConceptWord)o, true);
				else Context.getConceptService().updateConceptWord((ConceptWord)o, true);
			} else if ( "org.openmrs.Drug".equals(className) ) {
				if ( !knownToExist ) Context.getConceptService().createDrug((Drug)o, true);
				else Context.getConceptService().updateDrug((Drug)o, true);
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
    
    /**
     * @return  SyncStatusState runtime property defining sync status
     */
    public static SyncStatusState getSyncStatus() {
        
        SyncStatusState state = SyncStatusState.DISABLED; //default to disabled
        Properties properties = Context.getRuntimeProperties();
        String prop = properties.getProperty(SyncConstants.RUNTIMEPROPERTY_SYNC_STATUS, null);
        if (prop != null) {
            try {
                state = SyncStatusState.valueOf(prop);
                
            } catch(Exception e) {
                log.warn("Failed to parse RUNTIMEPROPERTY_SYNC_STATUS property, defaulting to disabled. Value read: " + prop,e);
            }
        }
        return state;
    }    
	
    public static String generateGuid() {
        return UUID.randomUUID().toString();
    }
    
    public static String displayName(String className, String guid) {

    	String ret = "";
    	
        // get more identifying info about this object so it's more user-friendly
        if ( className.equals("Person") || className.equals("User") || className.equals("Patient") ) {
            Person person = Context.getPersonService().getPersonByGuid(guid);
            if ( person != null ) ret = person.getPersonName().toString();
        }
        if ( className.equals("Encounter") ) {
            Encounter encounter = Context.getEncounterService().getEncounterByGuid(guid);
            if ( encounter != null ) {
                ret = encounter.getEncounterType().getName() 
                               + (encounter.getForm() == null ? "" : " (" + encounter.getForm().getName() + ")");
            }
        }
        if ( className.equals("Concept") ) {
            Concept concept = Context.getConceptService().getConceptByGuid(guid);
            if ( concept != null ) ret = concept.getName(Context.getLocale()).getName();
        }
        if ( className.equals("Drug") ) {
            Drug drug = Context.getConceptService().getDrugByGuid(guid);
            if ( drug != null ) ret = drug.getName();
        }
        if ( className.equals("Obs") ) {
            Obs obs = Context.getObsService().getObsByGuid(guid);
            if ( obs != null ) ret = obs.getConcept().getName(Context.getLocale()).getName();
        }
        if ( className.equals("DrugOrder") ) {
            DrugOrder drugOrder = (DrugOrder)Context.getOrderService().getOrderByGuid(guid);
            if ( drugOrder != null ) ret = drugOrder.getDrug().getConcept().getName(Context.getLocale()).getName();
        }
        if ( className.equals("Program") ) {
        	Program program = Context.getProgramWorkflowService().getProgramByGuid(guid);
        	if ( program != null ) ret = program.getConcept().getName(Context.getLocale()).getName();
        }
        if ( className.equals("ProgramWorkflow") ) {
        	ProgramWorkflow workflow = Context.getProgramWorkflowService().getWorkflowByGuid(guid);
        	if ( workflow != null ) ret = workflow.getConcept().getName(Context.getLocale()).getName();
        }
        if ( className.equals("ProgramWorkflowState") ) {
        	ProgramWorkflowState state = Context.getProgramWorkflowService().getStateByGuid(guid);
        	if ( state != null ) ret = state.getConcept().getName(Context.getLocale()).getName();
        }
        if ( className.equals("PatientProgram") ) {
        	PatientProgram patientProgram = Context.getProgramWorkflowService().getPatientProgramByGuid(guid);
        	String pat = patientProgram.getPatient().getPersonName().toString();
        	String prog = patientProgram.getProgram().getConcept().getName(Context.getLocale()).getName();
        	if ( pat != null && prog != null ) ret = pat + " - " + prog;
        }
        if ( className.equals("PatientState") ) {
        	PatientState patientState = Context.getProgramWorkflowService().getPatientStateByGuid(guid);
        	String pat = patientState.getPatientProgram().getPatient().getPersonName().toString();
        	String st = patientState.getState().getConcept().getName(Context.getLocale()).getName();
        	if ( pat != null && st != null ) ret = pat + " - " + st;
        }

        return ret;
    }
}
