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
import org.openmrs.api.db.SerializedObject;
import org.openmrs.api.db.SerializedObjectDAO;
import org.openmrs.serialization.OpenmrsSerializer;
import org.openmrs.serialization.SerializationException;

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
	private HibernateSerializedObjectDAO() {
	}
	
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
	 * @see SerializedObjectDAO#getSerializedObject(Integer)
	 */
	public SerializedObject getSerializedObject(Integer id) throws DAOException {
		if (id != null) {
			return (SerializedObject) sessionFactory.getCurrentSession().get(SerializedObject.class, id);
		}
		return null;
	}
	
	/**
	 * @see SerializedObjectDAO#getObject(Class, Integer)
	 */
	public <T extends OpenmrsObject> T getObject(Class<T> baseClass, Integer id) throws DAOException {
		SerializedObject serializedObject = getSerializedObject(id);
		return convertSerializedObject(baseClass, serializedObject);
	}
	
	/**
	 * @see SerializedObjectDAO#getSerializedObjectByUuid(String)
	 */
	public SerializedObject getSerializedObjectByUuid(String uuid) throws DAOException {
		SerializedObject ret = null;
		if (uuid != null) {
			Criteria c = sessionFactory.getCurrentSession().createCriteria(SerializedObject.class);
			c.add(Expression.eq("uuid", uuid));
			ret = (SerializedObject) c.uniqueResult();
		}
		return ret;
	}
	
	/**
	 * @see SerializedObjectDAO#getObjectByUuid(Class, String)
	 */
	public <T extends OpenmrsObject> T getObjectByUuid(Class<T> baseClass, String uuid) throws DAOException {
		SerializedObject o = getSerializedObjectByUuid(uuid);
		if (o != null) {
			return convertSerializedObject(baseClass, o);
		}
		return null;
	}
	
	/**
	 * @see SerializedObjectDAO#getAllObjectsByName(Class, String)
	 */
	@SuppressWarnings("unchecked")
	public List<SerializedObject> getAllSerializedObjectsByName(Class<?> type, String name, boolean exactMatchOnly)
	                                                                                                               throws DAOException {
		Criteria c = sessionFactory.getCurrentSession().createCriteria(SerializedObject.class);
		c.add(Expression.or(Expression.eq("type", type.getName()), Expression.eq("subtype", type.getName())));
		if (exactMatchOnly) {
			c.add(Expression.eq("name", name));
		} else {
			c.add(Expression.ilike("name", name, MatchMode.ANYWHERE));
		}
		return (List<SerializedObject>) c.list();
	}
	
	/**
	 * @see SerializedObjectDAO#getAllObjectsByName(Class, String)
	 */
	public <T extends OpenmrsMetadata> List<T> getAllObjectsByName(Class<T> type, String name, boolean exactMatchOnly)
	                                                                                                                  throws DAOException {
		List<T> ret = new ArrayList<T>();
		List<SerializedObject> objects = getAllSerializedObjectsByName(type, name, exactMatchOnly);
		for (SerializedObject serializedObject : objects) {
			ret.add(convertSerializedObject(type, serializedObject));
		}
		return ret;
	}
	
	/**
	 * @see SerializedObjectDAO#getAllObjects(Class, boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<SerializedObject> getAllSerializedObjects(Class<?> type, boolean includeRetired) throws DAOException {
		Criteria c = sessionFactory.getCurrentSession().createCriteria(SerializedObject.class);
		c.add(Expression.or(Expression.eq("type", type.getName()), Expression.eq("subtype", type.getName())));
		if (!includeRetired) {
			c.add(Expression.like("retired", false));
		}
		return (List<SerializedObject>) c.list();
	}
	
	/**
	 * @see SerializedObjectDAO#getAllObjects(Class, boolean)
	 */
	public <T extends OpenmrsObject> List<T> getAllObjects(Class<T> type, boolean includeRetired) throws DAOException {
		List<T> ret = new ArrayList<T>();
		List<SerializedObject> objects = getAllSerializedObjects(type, includeRetired);
		for (SerializedObject serializedObject : objects) {
			ret.add(convertSerializedObject(type, serializedObject));
		}
		return ret;
	}
	
	/**
	 * @see SerializedObjectDAO#getAllObjects(Class)
	 */
	public <T extends OpenmrsObject> List<T> getAllObjects(Class<T> type) throws DAOException {
		return getAllObjects(type, false);
	}
	
	/**
	 * @see SerializedObjectDAO#saveObject(OpenmrsObject)
	 */
	public <T extends OpenmrsObject> T saveObject(T object) throws DAOException {
		return saveObject(object, null);
	}
	
	/**
	 * @see SerializedObjectDAO#saveObject(OpenmrsObject, OpenmrsSerializer)
	 */
	public <T extends OpenmrsObject> T saveObject(T object, OpenmrsSerializer serializer) throws DAOException {
		
		Class<? extends OpenmrsObject> baseType = getRegisteredTypeForObject(object);
		if (baseType == null) {
			throw new DAOException("SerializedObjectDAO does not support saving objects of type <" + object.getClass() + ">");
		}
		
		SerializedObject serializedObject = getSerializedObject(object.getId());
		if (serializedObject == null) {
			serializedObject = new SerializedObject();
		}
		
		if (serializer == null) {
			serializer = getSerializer(serializedObject);
		}
		String data = null;
		try {
			data = serializer.serialize(object);
		}
		catch (SerializationException e) {
			throw new DAOException("Unable to save object <" + object + "> because serialization failed.", e);
		}
		
		serializedObject.setUuid(object.getUuid());
		serializedObject.setType(baseType.getName());
		serializedObject.setSubtype(object.getClass().getName());
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
	 * @see SerializedObjectDAO#purgeObject(Integer)
	 */
	public void purgeObject(Integer id) throws DAOException {
		SerializedObject o = getSerializedObject(id);
		sessionFactory.getCurrentSession().delete(o);
	}
	
	/**
	 * @see SerializedObjectDAO#registerSupportedType(Class)
	 */
	public void registerSupportedType(Class<? extends OpenmrsObject> clazz) throws DAOException {
		if (!getSupportedTypes().contains(clazz)) {
			supportedTypes.add(clazz);
		}
	}
	
	/**
	 * @see SerializedObjectDAO#unregisterSupportedType(Class)
	 */
	public void unregisterSupportedType(Class<? extends OpenmrsObject> clazz) throws DAOException {
		getSupportedTypes().remove(clazz);
	}
	
	/**
	 * @see SerializedObjectDAO#getRegisteredTypeForObject(OpenmrsObject)
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
	 * @see SerializedObjectDAO#convertSerializedObject(Class, SerializedObject)
	 */
	@SuppressWarnings("unchecked")
	public <T extends OpenmrsObject> T convertSerializedObject(Class<T> clazz, SerializedObject serializedObject)
	                                                                                                             throws DAOException {
		if (serializedObject == null) {
			return null;
		}
		OpenmrsSerializer serializer = getSerializer(serializedObject);
		T obj = null;
		try {
			Class<?> subtype = Context.loadClass(serializedObject.getSubtype());
			obj = (T) serializer.deserialize(serializedObject.getSerializedData(), subtype);
		}
		catch (Exception e) {
			// Do nothing here. Handled by null check below
		}
		if (obj == null) {
			throw new DAOException("Unable to deserialize object: " + serializedObject);
		}
		obj.setId(serializedObject.getId());
		obj.setUuid(serializedObject.getUuid());
		return obj;
	}
	
	/**
	 * Private method for retrieving the Serializer that should be used for the passed
	 * SerializedObject, defaulting to the default system serializer if none is explicitly set on
	 * the object
	 */
	private OpenmrsSerializer getSerializer(SerializedObject o) {
		if (o != null && o.getSerializationClass() != null) {
			return Context.getSerializationService().getSerializer(o.getSerializationClass());
		}
		return Context.getSerializationService().getDefaultSerializer();
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
		if (this.supportedTypes == null) {
			this.supportedTypes = new ArrayList<Class<? extends OpenmrsObject>>();
		}
		if (supportedTypes != null) {
			for (Class<? extends OpenmrsObject> clazz : supportedTypes) {
				this.supportedTypes.add(clazz);
			}
		}
		
	}
}
