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
package org.openmrs.api.db.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsData;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.SerializedObjectDAO;
import org.openmrs.serialization.OpenmrsSerializer;

/**
 * Hibernate specific database access methods for serialized objects
 */
public class HibernateSerializedObjectDAO implements SerializedObjectDAO {
	
	protected final Log log = LogFactory.getLog(getClass());
	private static HibernateSerializedObjectDAO instance;
	
	//********* PROPERTIES **********
	
	private SessionFactory sessionFactory;
	private List<Class<? extends OpenmrsObject>> supportedTypes;
	
	/**
	 * Private Constructor to support a singleton instance
	 */
	private HibernateSerializedObjectDAO() { }
	
	/**
	 * Singleton Factory method
	 * @return a singleton instance of this class
	 */
	public static HibernateSerializedObjectDAO getInstance() {
		if (instance == null) {
			instance = new HibernateSerializedObjectDAO();
		}
		return instance;
	}
	
	/**
	 * @see org.openmrs.api.db.SerializedObjectDAO#getObject(java.lang.Class, java.lang.Integer)
	 */
	public <T extends OpenmrsObject> T getObject(Class<T> baseClass, Integer id) throws DAOException {
		SerializedObject serializedObject = getSerializedObject(id);
		return convertSerializedObject(baseClass, serializedObject);
	}
	
	/**
	 * @see org.openmrs.api.db.SerializedObjectDAO#getObjectByUuid(java.lang.Class, java.lang.String)
	 */
	public <T extends OpenmrsObject> T getObjectByUuid(Class<T> baseClass, String uuid) throws DAOException {
		if (uuid != null) {
			Criteria c = sessionFactory.getCurrentSession().createCriteria(SerializedObject.class);
			c.add(Expression.eq("uuid", uuid));
			SerializedObject o = (SerializedObject) c.uniqueResult();
			return convertSerializedObject(baseClass, o);
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.api.db.SerializedObjectDAO#getAllObjectsByName(Class, String)
	 */
	@SuppressWarnings("unchecked")
	public <T extends OpenmrsMetadata> List<T> getAllObjectsByName(Class<T> type, 
																   String name, 
																   boolean exactMatchOnly) 
																   throws DAOException {
		List<T> ret = new ArrayList<T>();
		Criteria c = sessionFactory.getCurrentSession().createCriteria(SerializedObject.class);
		c.add(Expression.or(Expression.eq("type", type), Expression.eq("subtype", type)));
		if (exactMatchOnly) {
			c.add(Expression.eq("name", name));
		}
		else {
			c.add(Expression.ilike("name", name, MatchMode.ANYWHERE));
		}
		List<SerializedObject> objects = (List<SerializedObject>) c.list();
		for (SerializedObject serializedObject : objects) {
			ret.add(convertSerializedObject(type, serializedObject));
		}
		return ret;
	}
	
	/**
	 * @see org.openmrs.api.db.SerializedObjectDAO#getAllObjects(Class)
	 */
	public <T extends OpenmrsObject> List<T> getAllObjects(Class<T> type) throws DAOException {
		return getAllObjects(type, false);
	}
	
	/**
	 * @see org.openmrs.api.db.SerializedObjectDAO#getAllObjects(Class, boolean)
	 */
	@SuppressWarnings("unchecked")
	public <T extends OpenmrsObject> List<T> getAllObjects(Class<T> type, boolean includeRetired) throws DAOException {
		List<T> ret = new ArrayList<T>();
		Criteria c = sessionFactory.getCurrentSession().createCriteria(SerializedObject.class);
		c.add(Expression.or(Expression.eq("type", type), Expression.eq("subtype", type)));
		if (!includeRetired) {
			c.add(Expression.like("retired", false));
		}
		List<SerializedObject> objects = (List<SerializedObject>) c.list();
		for (SerializedObject serializedObject : objects) {
			ret.add(convertSerializedObject(type, serializedObject));
		}
		return ret;
	}
	
	/**
	 * @see org.openmrs.api.db.SerializedObjectDAO#saveObject(OpenmrsObject)
	 */
	public <T extends OpenmrsObject> T saveObject(T object) throws DAOException {
		
		Class<? extends OpenmrsObject> baseType = getRegisteredTypeForObject(object);
		if (baseType == null) {
			throw new DAOException("SerializedObjectDAO does not support saving objects of type <" + object.getClass() + ">");
		}
		
		SerializedObject serializedObject = getSerializedObject(object.getId());
		if (serializedObject == null) {
			serializedObject = new SerializedObject();
		}
		
		OpenmrsSerializer serializer = getSerializer(serializedObject);
		String data = serializer.serialize(object);
		
		serializedObject.setType(baseType);
		serializedObject.setSubtype(object.getClass());
		serializedObject.setSerializationClass(serializer.getClass());
		serializedObject.setSerializedData(data);
		
		if (object instanceof Auditable) {
			Auditable auditableObj = (Auditable) object;
			serializedObject.setCreator(auditableObj.getCreator());
			serializedObject.setDateCreated(auditableObj.getDateCreated());
			if (serializedObject.getCreator() == null) {
				serializedObject.setCreator(Context.getAuthenticatedUser());
			}
			if (serializedObject.getDateCreated() == null) {
				serializedObject.setDateCreated(new Date());
			}
			serializedObject.setChangedBy(auditableObj.getChangedBy());
			serializedObject.setDateChanged(auditableObj.getDateChanged());
		}
		
		if (object instanceof OpenmrsMetadata) {
			OpenmrsMetadata metaObj = (OpenmrsMetadata) object;
			serializedObject.setName(metaObj.getName());
			serializedObject.setDescription(metaObj.getDescription());
			serializedObject.setRetired(metaObj.isRetired() == Boolean.TRUE);
			serializedObject.setRetiredBy(metaObj.getRetiredBy());
			serializedObject.setDateRetired(metaObj.getDateRetired());
			serializedObject.setRetireReason(metaObj.getRetireReason());
		}
		
		if (object instanceof OpenmrsData) {
			OpenmrsData dataObj = (OpenmrsData) object;
			serializedObject.setRetired(dataObj.isVoided() == Boolean.TRUE);
			serializedObject.setRetiredBy(dataObj.getVoidedBy());
			serializedObject.setDateRetired(dataObj.getDateVoided());
			serializedObject.setRetireReason(dataObj.getVoidReason());
		}
		
		sessionFactory.getCurrentSession().saveOrUpdate(serializedObject);
		
		object.setId(serializedObject.getId());
		return object;
	}
	
	/**
	 * @see org.openmrs.api.db.SerializedObjectDAO#purgeObject(Integer)
	 */
	public void purgeObject(Integer id) throws DAOException {
		SerializedObject o = getSerializedObject(id);
		sessionFactory.getCurrentSession().delete(o);
	}
	
	/**
	 * @see org.openmrs.api.db.SerializedObjectDAO#registerSupportedType(java.lang.Class)
	 */
	public void registerSupportedType(Class<? extends OpenmrsObject> clazz) throws DAOException {
		if (!getSupportedTypes().contains(clazz)) {
			supportedTypes.add(clazz);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.SerializedObjectDAO#unregisterSupportedType(java.lang.Class)
	 */
	public void unregisterSupportedType(Class<? extends OpenmrsObject> clazz) throws DAOException {
		getSupportedTypes().remove(clazz);
	}
	
	/**
	 * @see org.openmrs.api.db.SerializedObjectDAO#getRegisteredTypeForObject(org.openmrs.OpenmrsObject)
	 */
	public Class<? extends OpenmrsObject> getRegisteredTypeForObject(OpenmrsObject object) {
		for (Class<? extends OpenmrsObject> clazz : getSupportedTypes()) {
			if (clazz.isAssignableFrom(object.getClass())) {
				return clazz;
			}
		}
		return null;
	}
	
	/**
	 * Private method for retrieving the SerializedObject from the database by id
	 * 
	 * @param id the id to lookup
	 * @return the SerializedObject with the given id
	 */
	private SerializedObject getSerializedObject(Integer id) throws DAOException {
		if (id != null) {
			return (SerializedObject) sessionFactory.getCurrentSession().get(SerializedObject.class, id);
		}
		return null;
	}
	
	/**
	 * Private method for converting a serialized object to a deserialized object of the given type
	 * 
	 * @param clazz the class to deserialize into
	 * @param serializedObject the serialized object to convert
	 * @return the deserialized Object
	 */
	@SuppressWarnings("unchecked")
	private <T extends OpenmrsObject> T convertSerializedObject(Class<T> clazz, SerializedObject serializedObject)
	                                                                                                              throws DAOException {
		if (serializedObject == null) {
			return null;
		}
		OpenmrsSerializer serializer = getSerializer(serializedObject);
		T obj = (T) serializer.deserialize(serializedObject.getSerializedData(), serializedObject.getSubtype());
		if (obj == null) {
			throw new DAOException("Unable to deserialize object: " + serializedObject);
		}
		obj.setId(serializedObject.getId());
		return obj;
	}
	
	/**
	 * Private method for retrieving the Serializer that should be used for the passed
	 * SerializedObject, defaulting to the default system serializer if none is explicitly set on
	 * the object
	 */
	private OpenmrsSerializer getSerializer(SerializedObject o) {
		OpenmrsSerializer s = Context.getSerializationService().getDefaultSerializer();
		if (o != null && o.getSerializationClass() != null) {
			s = Context.getSerializationService().getSerializer(o.getSerializationClass());
		}
		return s;
	}
	
	//***** Property access *****
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @return the supportedTypes
	 */
	public List<Class<? extends OpenmrsObject>> getSupportedTypes() {
		if (supportedTypes == null) {
			supportedTypes = new ArrayList<Class<? extends OpenmrsObject>>();
		}
		return supportedTypes;
	}
	
	/**
	 * @param supportedTypes the supportedTypes to set
	 */
	public void setSupportedTypes(List<Class<? extends OpenmrsObject>> supportedTypes) {
		this.supportedTypes = supportedTypes;
	}
}
