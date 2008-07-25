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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
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
import org.openmrs.ConceptSetDerived;
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
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.LoginCredential;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.Order;
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
import org.openmrs.notification.Message;
import org.openmrs.notification.MessageException;
import org.openmrs.serialization.FilePackage;
import org.openmrs.serialization.IItem;
import org.openmrs.serialization.Item;
import org.openmrs.serialization.Record;
import org.openmrs.serialization.TimestampNormalizer;
import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.server.RemoteServer;
import org.openmrs.util.LocaleFactory;
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
		
		log.info("Trying to set value to " + propVal + " when propName is " + propName + " and context is " + n.getTextContent());
		
		if ( propVal !=  null ) {
			SyncUtil.setProperty(o, propName, propVal);
			log.info("Successfully called set" + SyncUtil.propCase(propName) + "(" + propVal + ")" );
		}
	}

	public static void setProperty(Object o, String propName, Object propVal)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object[] setterParams = new Object[1];
		setterParams[0] = propVal;
		
		log.debug("getting setter method");
		Method m = SyncUtil.getSetterMethod(o.getClass(), propName, propVal.getClass());

		log.debug("about to call " + m.getName());
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
		else if ( "org.openmrs.LoginCredential".equals(className) ) o = (Object)(Context.getUserService().getLoginCredentialByGuid(guid));
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
				log.info("found Field " + fieldName + " with type is " + f.getGenericType());

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
				} else if ( "java.util.Locale".equals(className) ) {
					o = LocaleFactory.fromSpecification(fieldVal);
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

    private static Synchronizable findByGUID(Collection<? extends Synchronizable> list, Synchronizable toCheck){

    	for(Synchronizable element:list){
    		if(element.getGuid().equals(toCheck.getGuid()))
    			return element;
    	}
    	
    	return null;
    }
    
    /**
     * Replaces updateOpenmrsObject by using generic hibernate API to perform the save as oposed to service API.
     * 
     * @param o object to save
     * @param className type
     * @param guid unique id of the object that is being saved
     * 
     * @see SyncUtil#updateOpenmrsObject(Object, String, String, boolean)
     */
    public static synchronized void updateOpenmrsObject2(Synchronizable o, String className, String guid) {
		if ( o != null ) {
			Context.getSynchronizationService().saveOrUpdate(o);
		} else {
			log.warn("Will not update OpenMRS object that is NULL");
		}
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
	public static synchronized String updateOpenmrsObject(Object o, String className, String guid) {
		String ret = null;
		if ( o != null ) {
			boolean isUpdated = true;
			if ( "org.openmrs.Cohort".equals(className) ) { 
				Context.getCohortService().saveCohort((Cohort)o);
			} else if ( "org.openmrs.ComplexObs".equals(className) ) {
				Context.getObsService().saveObs((Obs)o, ((Obs)o).getVoidReason());
			} else if ( "org.openmrs.Concept".equals(className) ) {
				Context.getConceptService().saveConcept((Concept)o);
			} else if ( "org.openmrs.ConceptAnswer".equals(className) ) {
				ConceptAnswer ca = (ConceptAnswer) o;
				ConceptAnswer toRemove = (ConceptAnswer) findByGUID(ca.getConcept().getAnswers(), ca);
				if(toRemove != null)
					ca.getConcept().removeAnswer(toRemove);
				ca.getConcept().addAnswer(ca);
				
				Context.getConceptService().saveConcept(ca.getConcept());
			} else if ( "org.openmrs.ConceptClass".equals(className) ) {
				Context.getConceptService().saveConceptClass((ConceptClass)o);
			} else if ( "org.openmrs.ConceptDatatype".equals(className) ) {
				Context.getConceptService().saveConceptDatatype((ConceptDatatype)o);
			} else if ( "org.openmrs.ConceptDerived".equals(className) ) {
				Context.getConceptService().saveConcept((ConceptDerived)o);
			} else if ( "org.openmrs.ConceptName".equals(className) ) {
				ConceptName cn = (ConceptName) o;
				ConceptName toRemove = (ConceptName) findByGUID(cn.getConcept().getNames(),cn);
				if(toRemove != null)
					cn.getConcept().removeName(toRemove);
				cn.getConcept().addName(cn);
				
				System.out.println("Concept name " + cn.getName() + " added to concept " + cn.getConcept().getConceptId());
				
				Context.getConceptService().saveConcept(cn.getConcept());
			} else if ( "org.openmrs.ConceptNumeric".equals(className) ) {
				if( ((ConceptNumeric)o).getName() == null)
					System.out.println("Concept name is NULL");
				else
					System.out.println("Concept name for numeric concept is now " + ((ConceptNumeric)o).getName().getName());
				
				Context.getConceptService().saveConcept((ConceptNumeric)o);
			} else if ( "org.openmrs.ConceptProposal".equals(className) ) {
				Context.getConceptService().saveConceptProposal((ConceptProposal)o);
			} else if ( "org.openmrs.ConceptSet".equals(className) ) {
				ConceptSet cs = (ConceptSet) o;
				ConceptSet toRemove = (ConceptSet) findByGUID(cs.getConcept().getConceptSets(),cs);
				if(toRemove !=null)
					cs.getConcept().getConceptSets().remove(toRemove);
				cs.getConcept().getConceptSets().add(cs);
				
				Context.getConceptService().saveConcept(cs.getConcept());
			} else if ( "org.openmrs.ConceptSetDerived".equals(className) ) {
				Context.getConceptService().updateConceptSetDerived(((ConceptSetDerived)o).getConcept());
			} else if ( "org.openmrs.ConceptSource".equals(className) ) {
				log.debug("UNABLE TO CREATE/UPDATE ConceptSource in Synchronization process - no service method exists");
				isUpdated = false;
			} else if ( "org.openmrs.ConceptStateConversion".equals(className) ) {
				Context.getProgramWorkflowService().saveConceptStateConversion((ConceptStateConversion)o);
			} else if ( "org.openmrs.ConceptSynonym".equals(className) ) {
				ConceptSynonym cs = (ConceptSynonym) o;
				ConceptSynonym toRemove = (ConceptSynonym) findByGUID(cs.getConcept().getSynonyms(), cs);
				if(toRemove!=null)
					cs.getConcept().getSynonyms().remove(toRemove);
				cs.getConcept().addSynonym(cs);
				
				Context.getConceptService().saveConcept(cs.getConcept());
			} else if ( "org.openmrs.ConceptWord".equals(className) ) {
				Context.getConceptService().saveConcept(((ConceptWord)o).getConcept());
			} else if ( "org.openmrs.Drug".equals(className) ) {
				Context.getConceptService().saveDrug((Drug)o);
			} else if ( "org.openmrs.DrugIngredient".equals(className) ) {
				log.debug("UNABLE TO CREATE/UPDATE DrugIngredient in Synchronization process - no service method exists");
				isUpdated = false;
			} else if ( "org.openmrs.DrugOrder".equals(className) ) {
				Context.getOrderService().saveOrder((DrugOrder)o);
			} else if ( "org.openmrs.Encounter".equals(className) ) {
				Context.getEncounterService().saveEncounter((Encounter)o);
			} else if ( "org.openmrs.EncounterType".equals(className) ) {
				Context.getEncounterService().saveEncounterType((EncounterType)o);
			} else if ( "org.openmrs.Field".equals(className) ) {
				Context.getFormService().saveField((org.openmrs.Field)o);
			} else if ( "org.openmrs.FieldAnswer".equals(className) ) {
				log.debug("UNABLE TO CREATE/UPDATE FieldAnswer in Synchronization process - no service method exists");
				isUpdated = false;
			} else if ( "org.openmrs.FieldType".equals(className) ) {
				Context.getFormService().saveFieldType((FieldType)o);
			} else if ( "org.openmrs.Form".equals(className) ) {
				Context.getFormService().saveForm((Form)o);
			} else if ( "org.openmrs.FormField".equals(className) ) {
				Context.getFormService().saveFormField((FormField)o);
			} else if ( "org.openmrs.GlobalProperty".equals(className) ) {
				Context.getAdministrationService().saveGlobalProperty((GlobalProperty)o);
			} else if ( "org.openmrs.Location".equals(className) ) { 
				Context.getLocationService().saveLocation((Location)o);
			} else if ( "org.openmrs.LoginCredential".equals(className) ) {
				log.debug("UNABLE TO CREATE/UPDATE LoginCredential in Synchronization process - no service method exists");
				isUpdated = false;
			} else if ( "org.openmrs.MimeType".equals(className) ) {
				Context.getObsService().saveMimeType((MimeType)o);
			} else if ( "org.openmrs.Obs".equals(className) ) {
				Context.getObsService().saveObs((Obs)o,((Obs)o).getVoidReason());
			} else if ( "org.openmrs.Order".equals(className) ){
				Context.getOrderService().saveOrder((Order)o);
			} else if ( "org.openmrs.OrderType".equals(className) ){
				Context.getOrderService().saveOrderType((OrderType)o);
			} else if ( "org.openmrs.Patient".equals(className) ){
				Context.getPatientService().savePatient((Patient)o);
			} else if ( "org.openmrs.PatientIdentifierType".equals(className) ) {
				Context.getPatientService().savePatientIdentifierType((PatientIdentifierType)o);
			} else if ( "org.openmrs.PatientIdentifier".equals(className) ) {
				Context.getPatientService().savePatient(((PatientIdentifier)o).getPatient());
			} else if ( "org.openmrs.PatientProgram".equals(className) ) {
				Context.getProgramWorkflowService().savePatientProgram((PatientProgram)o);
			} else if ( "org.openmrs.PatientState".equals(className) ) {
				Context.getProgramWorkflowService().savePatientProgram(((PatientState)o).getPatientProgram());
			} else if ( "org.openmrs.Person".equals(className) ) {
				Context.getPersonService().savePerson((Person)o);
			} else if ( "org.openmrs.PersonAddress".equals(className) ) {
				Context.getPersonService().savePerson(((PersonAddress)o).getPerson());
			} else if ( "org.openmrs.PersonAttribute".equals(className) ) {
				Context.getPersonService().savePerson(((PersonAttribute)o).getPerson());
			} else if ( "org.openmrs.PersonAttributeType".equals(className) ) {
				Context.getPersonService().savePersonAttributeType((PersonAttributeType)o);
			} else if ( "org.openmrs.PersonName".equals(className) ) {
				PersonName pn = (PersonName)o;
				Context.getPersonService().savePerson(pn.getPerson());
			} else if ( "org.openmrs.Privilege".equals(className) ) {
				Context.getUserService().savePrivilege((Privilege)o);
			} else if ( "org.openmrs.Program".equals(className) ) {
				Context.getProgramWorkflowService().saveProgram((Program)o);
			} else if ( "org.openmrs.ProgramWorkflow".equals(className) ) {
				ProgramWorkflow pw = (ProgramWorkflow) o;
				pw.getProgram().addWorkflow(pw);
				Context.getProgramWorkflowService().saveProgram(pw.getProgram());
			} else if ( "org.openmrs.ProgramWorkflowState".equals(className) ) {
				Context.getProgramWorkflowService().saveProgram(((ProgramWorkflowState)o).getProgramWorkflow().getProgram());
			} else if ( "org.openmrs.Relationship".equals(className) ) {
				Context.getPersonService().saveRelationship((Relationship)o);
			} else if ( "org.openmrs.RelationshipType".equals(className) ) {
				Context.getPersonService().saveRelationshipType((RelationshipType)o);
			} else if ( "org.openmrs.Role".equals(className) ) {
				Context.getUserService().saveRole((Role)o);
			} else if ( "org.openmrs.Tribe".equals(className) ) { 
				log.debug("UNABLE TO CREATE/UPDATE Tribe in Synchronization process - object not recognized");
				isUpdated = false;
			} else if ( "org.openmrs.User".equals(className) ) {
				Context.getUserService().saveUser((User)o,null);
			} else {
				log.debug("UNABLE TO CREATE/UPDATE " + className + " in Synchronization process - object not recognized");
				isUpdated = false;
			}

			if ( isUpdated ) {
				//SyncRecord record = Context.getSynchronizationService().getLatestRecord();
				//if ( record != null ) ret = record.getGuid();
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
        
        SyncStatusState state = SyncStatusState.DISABLED_SYNC; //default to disabled
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

        if ( className.equals("PersonAddress") ) {
        	PersonAddress address = Context.getPersonService().getPersonAddressByGuid(guid);
        	String name = address.getPerson().getFamilyName() + " " + address.getPerson().getGivenName();
        	name += address.getAddress1() != null && address.getAddress1().length() > 0 ? address.getAddress1() + " " : "";
        	name += address.getAddress2() != null && address.getAddress2().length() > 0 ? address.getAddress2() + " " : "";
        	name += address.getCityVillage() != null && address.getCityVillage().length() > 0 ? address.getCityVillage() + " " : "";
        	name += address.getStateProvince() != null && address.getStateProvince().length() > 0 ? address.getStateProvince() + " " : "";
        	if ( name != null ) ret = name;
        }

        if ( className.equals("PersonName") ) {
        	PersonName personName = Context.getPersonService().getPersonNameByGuid(guid);
        	String name = personName.getFamilyName() + " " + personName.getGivenName();
        	if ( name != null ) ret = name;
        }

        if ( className.equals("Relationship") ) {
        	Relationship relationship = Context.getPersonService().getRelationshipByGuid(guid);
        	String from = relationship.getPersonA().getFamilyName() + " " + relationship.getPersonA().getGivenName();
        	String to = relationship.getPersonB().getFamilyName() + " " + relationship.getPersonB().getGivenName();
        	if ( from != null && to != null ) ret += from + " to " + to;
        }

        if ( className.equals("RelationshipType") ) {
        	RelationshipType type = Context.getPersonService().getRelationshipTypeByGuid(guid);
        	ret += type.getaIsToB() + " - " + type.getbIsToA();
        }

        if ( className.equals("PersonAttributeType") ) {
        	PersonAttributeType type = Context.getPersonService().getPersonAttributeTypeByGuid(guid);
        	ret += type.getName();
        }

        if ( className.equals("Location") ) {
        	Location loc = Context.getEncounterService().getLocationByGuid(guid);
        	ret += loc.getName();
        }

        if ( className.equals("EncounterType") ) {
        	EncounterType type = Context.getEncounterService().getEncounterTypeByGuid(guid);
        	ret += type.getName();
        }

        if ( className.equals("OrderType") ) {
        	OrderType type = Context.getOrderService().getOrderTypeByGuid(guid);
        	ret += type.getName();
        }

        return ret;
    }
        
    /**
     * Deletes instance of Synchronizable. Used to process SyncItems with state of deleted.
     */
	public static synchronized void deleteOpenmrsObject(Synchronizable o) {
		Context.getSynchronizationService().deleteSynchronizable(o);
	}

	
	
    public static String getLocalServerGuid() {
        return Context.getSynchronizationService().getGlobalProperty(SyncConstants.SERVER_GUID);        
    }
    
    public static void setLocalServerGuid(String guid) {
        Context.getSynchronizationService().setGlobalProperty(SyncConstants.SERVER_GUID, guid);
        
        return;   
    }

    public static String getLocalServerName() {
        return Context.getSynchronizationService().getGlobalProperty(SyncConstants.SERVER_NAME);        
    }
    
    public static void setLocalServerName(String name) {
        Context.getSynchronizationService().setGlobalProperty(SyncConstants.SERVER_NAME, name);
        
        return;   
    }

    public static String getAdminEmail() {
        return Context.getSynchronizationService().getGlobalProperty(SyncConstants.SYNC_ADMIN_EMAIL);        
    }
    
    public static void setAdminEmail(String email) {
        Context.getSynchronizationService().setGlobalProperty(SyncConstants.SYNC_ADMIN_EMAIL, email);
        
        return;   
    }
    
	public static void sendSyncErrorMessage(SyncRecord syncRecord, RemoteServer server, Exception exception) { 
		
		try {
							
			
			String adminEmail = SyncUtil.getAdminEmail();
			
			if (adminEmail == null || adminEmail.length() == 0 ) { 
				log.warn("Sync error message could not be sent because " + SyncConstants.SYNC_ADMIN_EMAIL + " is not configured.");
			} 
			else if (adminEmail != null) { 
				log.info("Preparing to send sync error message via email to " + adminEmail);
			
				Message message = new Message();
				message.setSender("info@openmrs.org");
				message.setSentDate(new Date());
				message.setSubject(exception.getMessage());
				message.addRecipient(adminEmail);
			

			
				StringBuffer content = new StringBuffer();
			
			
				content.
					append("ALERT: Synchronization has stopped between\n").
					append("local server (").append(SyncUtil.getLocalServerName()).
					append(") and remote server ").append(server.getNickname()).append("\n\n").
					append("Summary of failing record\n").
					append("Original GUID:          " + syncRecord.getOriginalGuid()).
					append("Contained classes:      " + syncRecord.getContainedClassSet()).
					append("Contents:\n");
			
		        try {
		        	log.info("Sending email with sync record: " + syncRecord);
	
		        	for (SyncItem item :syncRecord.getItems()) { 
		        		log.info("Sync item content: " + item.getContent());
		        	}
		        	
					FilePackage pkg = new FilePackage();
			        Record record = pkg.createRecordForWrite("SyncRecord");
			        Item top = record.getRootItem();
			        ((IItem) syncRecord).save(record, top);
			        content.append(record.toString());
			        
		        } catch (Exception e) {
		        	log.warn("An error occurred while retrieving sync record payload", e);
		        	log.warn("Sync record: " + syncRecord.toString());
		        }			
				message.setContent(content.toString());
				
				// Send message
				Context.getMessageService().sendMessage(message);
			
		        log.info("Sent sync error message to " + adminEmail);
			}			
			
		} catch (MessageException e) { 
			log.error("An error occurred while sending the sync error message", e);
		} 
		
	}    
	
	
	
	/**
	 * 
	 * 
	 * @param inputStream
	 * @return
	 * @throws Exception
	 */
	public static String readContents(InputStream inputStream, boolean isCompressed) throws Exception { 
		StringBuffer contents = new StringBuffer();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, SyncConstants.UTF8));
		
		String line = "";
		while ((line = reader.readLine()) != null) {
			contents.append(line);
		}
		
		return contents.toString();		
	}
	

	public static byte [] compress(String content) throws IOException { 
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CheckedOutputStream cos = new CheckedOutputStream(baos, new CRC32());			
		GZIPOutputStream zos = new GZIPOutputStream(new BufferedOutputStream(cos));			
		IOUtils.copy(new ByteArrayInputStream(content.getBytes()), zos);		
		return baos.toByteArray();
	}
	
	public static String decompress(byte[] data) throws IOException { 		
		ByteArrayInputStream bais2 = new ByteArrayInputStream(data);	        
        CheckedInputStream cis = new CheckedInputStream(bais2, new CRC32());
        GZIPInputStream zis = new GZIPInputStream(new BufferedInputStream(cis));            
        InputStreamReader reader = new InputStreamReader(zis);
		BufferedReader br = new BufferedReader(reader);
		StringBuffer buffer = new StringBuffer();
		String line = "";
		while ((line = br.readLine()) != null) {
			buffer.append(line);
		}
		return buffer.toString();
	}
	
	

	

}
