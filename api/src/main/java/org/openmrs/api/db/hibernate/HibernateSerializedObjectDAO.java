/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
import org.openmrs.util.ExceptionUtil;

/**
 * Hibernate specific database access methods for serialized objects
 */
public class HibernateSerializedObjectDAO implements SerializedObjectDAO {
	
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
	 * 
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
	@Override
	public SerializedObject getSerializedObject(Integer id) throws DAOException {
		if (id != null) {
			return sessionFactory.getCurrentSession().get(SerializedObject.class, id);
		}
		return null;
	}
	
	/**
	 * @see SerializedObjectDAO#getObject(Class, Integer)
	 */
	@Override
	public <T extends OpenmrsObject> T getObject(Class<T> baseClass, Integer id) throws DAOException {
		SerializedObject serializedObject = getSerializedObject(id);
		return convertSerializedObject(baseClass, serializedObject);
	}
	
	/**
	 * @see SerializedObjectDAO#getSerializedObjectByUuid(String)
	 */
	@Override
	public SerializedObject getSerializedObjectByUuid(String uuid) throws DAOException {
		if (uuid != null) {
			return HibernateUtil.getUniqueEntityByUUID(sessionFactory, SerializedObject.class, uuid);
		}
		return null;
	}
	
	/**
	 * @see SerializedObjectDAO#getObjectByUuid(Class, String)
	 */
	@Override
	public <T extends OpenmrsObject> T getObjectByUuid(Class<T> baseClass, String uuid) throws DAOException {
		SerializedObject o = getSerializedObjectByUuid(uuid);
		if (o != null) {
			return convertSerializedObject(baseClass, o);
		}
		return null;
	}
	
	/**
	 * @see SerializedObjectDAO#getAllSerializedObjectsByName(Class, String, boolean)
	 */
	@Override
	public List<SerializedObject> getAllSerializedObjectsByName(Class<?> type, String name, boolean exactMatchOnly)
		throws DAOException {

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<SerializedObject> cq = cb.createQuery(SerializedObject.class);
		Root<SerializedObject> root = cq.from(SerializedObject.class);

		Predicate predicateForType = cb.or(
			cb.equal(root.get("type"), type.getName()), 
			cb.equal(root.get("subtype"), type.getName())
		);
		
		Predicate predicateForName;
		if (exactMatchOnly) {
			predicateForName = cb.equal(root.get("name"), name);
		} else {
			predicateForName = cb.like(cb.lower(root.get("name")), MatchMode.ANYWHERE.toLowerCasePattern(name));
		}

		cq.where(predicateForType, predicateForName);
		return session.createQuery(cq).getResultList();
	}
	
	/**
	 * @see SerializedObjectDAO#getAllObjectsByName(Class, String, boolean)
	 */
	@Override
	public <T extends OpenmrsMetadata> List<T> getAllObjectsByName(Class<T> type, String name, boolean exactMatchOnly)
	        throws DAOException {
		List<T> ret = new ArrayList<>();
		List<SerializedObject> objects = getAllSerializedObjectsByName(type, name, exactMatchOnly);
		for (SerializedObject serializedObject : objects) {
			ret.add(convertSerializedObject(type, serializedObject));
		}
		return ret;
	}
	
	/**
	 * @see SerializedObjectDAO#getAllObjects(Class, boolean)
	 */
	@Override
	public List<SerializedObject> getAllSerializedObjects(Class<?> type, boolean includeRetired) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<SerializedObject> cq = cb.createQuery(SerializedObject.class);
		Root<SerializedObject> root = cq.from(SerializedObject.class);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.or(
			cb.equal(root.get("type"), type.getName()),
			cb.equal(root.get("subtype"), type.getName())
		));
		
		if (!includeRetired) {
			predicates.add(cb.isFalse(root.get("retired")));
		}
		
		cq.where(predicates.toArray(new Predicate[]{}));

		return session.createQuery(cq).getResultList();
	}
	
	/**
	 * @see SerializedObjectDAO#getAllObjects(Class, boolean)
	 */
	@Override
	public <T extends OpenmrsObject> List<T> getAllObjects(Class<T> type, boolean includeRetired) throws DAOException {
		List<T> ret = new ArrayList<>();
		List<SerializedObject> objects = getAllSerializedObjects(type, includeRetired);
		for (SerializedObject serializedObject : objects) {
			ret.add(convertSerializedObject(type, serializedObject));
		}
		return ret;
	}
	
	/**
	 * @see SerializedObjectDAO#getAllObjects(Class)
	 */
	@Override
	public <T extends OpenmrsObject> List<T> getAllObjects(Class<T> type) throws DAOException {
		return getAllObjects(type, false);
	}
	
	/**
	 * @see SerializedObjectDAO#saveObject(OpenmrsObject)
	 */
	@Override
	public <T extends OpenmrsObject> T saveObject(T object) throws DAOException {
		return saveObject(object, null);
	}
	
	/**
	 * @see SerializedObjectDAO#saveObject(OpenmrsObject, OpenmrsSerializer)
	 */
	@Override
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
		
		if (object instanceof Auditable) {
			Auditable auditableObj = (Auditable) object;
			if (auditableObj.getCreator() == null) {
				auditableObj.setCreator(Context.getAuthenticatedUser());
			}
			serializedObject.setCreator(auditableObj.getCreator());
			
			if (auditableObj.getDateCreated() == null) {
				auditableObj.setDateCreated(new Date());
			}
			serializedObject.setDateCreated(auditableObj.getDateCreated());
			serializedObject.setChangedBy(auditableObj.getChangedBy());
			serializedObject.setDateChanged(auditableObj.getDateChanged());
		}
		
		String data;
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
		
		if (object instanceof OpenmrsMetadata) {
			OpenmrsMetadata metaObj = (OpenmrsMetadata) object;
			serializedObject.setName(metaObj.getName());
			serializedObject.setDescription(metaObj.getDescription());
			serializedObject.setRetired(metaObj.getRetired());
			serializedObject.setRetiredBy(metaObj.getRetiredBy());
			serializedObject.setDateRetired(metaObj.getDateRetired());
			serializedObject.setRetireReason(metaObj.getRetireReason());
		}
		
		if (object instanceof OpenmrsData) {
			OpenmrsData dataObj = (OpenmrsData) object;
			serializedObject.setRetired(dataObj.getVoided());
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
	@Override
	public void purgeObject(Integer id) throws DAOException {
		SerializedObject o = getSerializedObject(id);
		sessionFactory.getCurrentSession().delete(o);
	}
	
	/**
	 * @see SerializedObjectDAO#registerSupportedType(Class)
	 */
	@Override
	public void registerSupportedType(Class<? extends OpenmrsObject> clazz) throws DAOException {
		if (!getSupportedTypes().contains(clazz)) {
			supportedTypes.add(clazz);
		}
	}
	
	/**
	 * @see SerializedObjectDAO#unregisterSupportedType(Class)
	 */
	@Override
	public void unregisterSupportedType(Class<? extends OpenmrsObject> clazz) throws DAOException {
		getSupportedTypes().remove(clazz);
	}
	
	/**
	 * @see SerializedObjectDAO#getRegisteredTypeForObject(OpenmrsObject)
	 */
	@Override
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
	@Override
	@SuppressWarnings("unchecked")
	public <T extends OpenmrsObject> T convertSerializedObject(Class<T> clazz, SerializedObject serializedObject)
	        throws DAOException {
		if (serializedObject == null) {
			return null;
		}
		OpenmrsSerializer serializer = getSerializer(serializedObject);
		T obj;
		try {
			Class<?> subtype = Context.loadClass(serializedObject.getSubtype());
			obj = (T) serializer.deserialize(serializedObject.getSerializedData(), subtype);
		}
		catch (Exception e) {
			ExceptionUtil.rethrowAPIAuthenticationException(e);
			throw new DAOException("Unable to deserialize object: " + serializedObject, e);
		}
		if (obj == null) {
			// it's probably impossible to reach this code branch
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
	@Override
	public List<Class<? extends OpenmrsObject>> getSupportedTypes() {
		if (supportedTypes == null) {
			supportedTypes = new ArrayList<>();
		}
		return supportedTypes;
	}
	
	/**
	 * @param supportedTypes the supportedTypes to set
	 */
	public void setSupportedTypes(List<Class<? extends OpenmrsObject>> supportedTypes) {
		if (this.supportedTypes == null) {
			this.supportedTypes = new ArrayList<>();
		}
		if (supportedTypes != null) {
			this.supportedTypes.addAll(supportedTypes);
		}
		
	}
}
