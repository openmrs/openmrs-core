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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Retireable;
import org.openmrs.Voidable;
import org.openmrs.api.UnchangeableObjectException;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass for all Interceptors that would like to ensure that changes to immutable entities of
 * specific types don't get persisted to the database, more granularity of the immutable properties
 * is also supported so as to allow editing some properties while not for others <pre>
 * <b>NOTE:</b> Subclasses MUST not make any changes to the persistent object because they get
 * called last, if they make any changes other interceptors would never know about them.
 * </pre>
 *
 * @since 1.10
 */
public abstract class ImmutableEntityInterceptor implements Interceptor {

	private static final Logger log = LoggerFactory.getLogger(ImmutableEntityInterceptor.class);

	// Holds the patient merge running on a thread, see setPatientMergeForThread
	private static final ThreadLocal<PatientMerge> patientMerge = new ThreadLocal<>();

	// This thread local enables storing additional mutable properties to allow for a given thread
	private final ThreadLocal<String[]> additionalMutableProperties = new ThreadLocal<>();

	/**
	 * Returns the class handled by the interceptor
	 */
	protected abstract Class<?> getSupportedType();

	/**
	 * Subclasses can override this to return fields that are allowed to be edited, returning null or an
	 * empty array implies the entity is immutable
	 *
	 * @return an array of properties
	 */
	protected String[] getMutablePropertyNames() {
		return null;
	}

	/**
	 * Subclasses can override this to specify whether voided or retired items are mutable
	 *
	 * @return true if objects that are voided or retired in the database are mutable otherwise false
	 *         means they are immutable
	 */
	protected boolean ignoreVoidedOrRetiredObjects() {
		return false;
	}

	/**
	 * Subclasses can override this to allow a change to a property that is not in
	 * {@link #getMutablePropertyNames()}. It is only called for a property whose value has changed.
	 *
	 * @param entity the entity being flushed
	 * @param property the name of the changed property
	 * @param previousValue the value in the database
	 * @param currentValue the value about to be written
	 * @return true if the change is allowed
	 * @since 3.0.0
	 */
	protected boolean isAllowedChange(Object entity, String property, Object previousValue, Object currentValue) {
		return false;
	}

	/**
	 * <p>
	 * <strong>Should</strong> fail if an entity has a changed property<br/>
	 * <strong>Should</strong> pass if an entity has changes for an allowed mutable property<br/>
	 * <strong>Should</strong> pass if the edited object is voided in the database and ignore is set to
	 * true<br/>
	 * <strong>Should</strong> pass if the edited object is retired in the database and ignore is set to
	 * true<br/>
	 * <strong>Should</strong> fail if the edited object is voided in the same flush and ignore is set
	 * to true<br/>
	 * <strong>Should</strong> fail if the edited object is voided in the database and ignore is set to
	 * false<br/>
	 * <strong>Should</strong> pass if a date differs only below a second<br/>
	 * <strong>Should</strong> pass if a date before 1970 differs only below a second<br/>
	 * <strong>Should</strong> fail if a date falls in another second
	 */
	@Override
	public boolean onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState,
	        String[] propertyNames, Type[] types) {

		if (getSupportedType().isAssignableFrom(entity.getClass())) {
			if (ignoreVoidedOrRetiredObjects() && isVoidedOrRetiredInDatabase(entity, previousState, propertyNames)) {
				return false;
			}

			List<String> changedProperties = null;
			for (int i = 0; i < propertyNames.length; i++) {
				String property = propertyNames[i];
				if (isMutableProperty(property)) {
					continue;
				}

				Object previousValue = (previousState != null) ? previousState[i] : null;
				Object currentValue = (currentState != null) ? currentState[i] : null;
				if (!isSameValue(currentValue, previousValue)
				        && !isAllowedChange(entity, property, previousValue, currentValue)) {
					if (changedProperties == null) {
						changedProperties = new ArrayList<>();
					}
					changedProperties.add(property);
				}
			}
			if (CollectionUtils.isNotEmpty(changedProperties)) {
				log.debug("The following fields cannot be changed for {} : {}", getSupportedType(), changedProperties);

				throw new UnchangeableObjectException("editing.fields.not.allowed",
				        new Object[] { changedProperties, getSupportedType().getSimpleName() });
			}
		}

		return false;
	}

	/**
	 * Reads the voided or retired flag from the state in the database, not from the entity, whose flag
	 * may have been set in the same flush.
	 */
	private boolean isVoidedOrRetiredInDatabase(Object entity, Object[] previousState, String[] propertyNames) {
		String flag;
		if (entity instanceof Voidable) {
			flag = "voided";
		} else if (entity instanceof Retireable) {
			flag = "retired";
		} else {
			return false;
		}
		int index = ArrayUtils.indexOf(propertyNames, flag);
		return previousState != null && index >= 0 && Boolean.TRUE.equals(previousState[index]);
	}

	/**
	 * Compares dates to the second, because {@link DropMillisecondsHibernateInterceptor} drops the
	 * milliseconds of the dates it is about to write before this interceptor runs.
	 */
	private static boolean isSameValue(Object currentValue, Object previousValue) {
		// an unchanged lazy association is the same proxy in both states, and equals would load it
		if (currentValue == previousValue) {
			return true;
		}
		if (currentValue instanceof Date && previousValue instanceof Date) {
			return Math.floorDiv(((Date) currentValue).getTime(), 1000L) == Math.floorDiv(((Date) previousValue).getTime(),
			    1000L);
		}
		return OpenmrsUtil.nullSafeEquals(currentValue, previousValue);
	}

	/**
	 * @return true if the given property is allowed to be mutable
	 */
	private boolean isMutableProperty(String property) {
		if (ArrayUtils.contains(getMutablePropertyNames(), property)) {
			return true;
		}
		String[] threadMutable = additionalMutableProperties.get();
		if (threadMutable != null && ArrayUtils.contains(threadMutable, property)) {
			return true;
		}
		return false;
	}

	/**
	 * This allows code that is attempting to save immutable entities to bypass standard validation by
	 * allowing additional properties to be considered mutable for the duration of the thread. Note that
	 * the caller should ensure that {@link #removeMutablePropertiesForThread} is invoked when the
	 * operation is done
	 *
	 * @param properties any additional properties that one wishes to make mutable for a given thread
	 */
	public void addMutablePropertiesForThread(String... properties) {
		additionalMutableProperties.set(properties);
	}

	/**
	 * If any additional properties were added for a given thread by invoking
	 * {@link #addMutablePropertiesForThread}, this removes them. NOTE, any usage of
	 * {@link #addMutablePropertiesForThread} should typically be followed by an invocation of this
	 * method in a finally block
	 */
	public void removeMutablePropertiesForThread() {
		additionalMutableProperties.remove();
	}

	/**
	 * Lets a voided obs, or an order, move in place from {@code notPreferred} to {@code preferred}
	 * while a patient merge runs on the current thread. A move is checked when the session is flushed,
	 * so the caller must flush the session before it invokes {@link #removePatientMergeForThread()},
	 * which it must do in a finally block.
	 * <p>
	 * <strong>Should</strong> let a voided obs move in a patient merge only from the non preferred to
	 * the preferred patient
	 *
	 * @param preferred the patient the records move to
	 * @param notPreferred the patient being merged away
	 * @since 3.0.0
	 */
	public static void setPatientMergeForThread(Patient preferred, Patient notPreferred) {
		patientMerge.set(new PatientMerge(notPreferred.getPatientId(), preferred.getPatientId()));
	}

	/**
	 * Ends the patient merge started by {@link #setPatientMergeForThread(Patient, Patient)}.
	 *
	 * @since 3.0.0
	 */
	public static void removePatientMergeForThread() {
		patientMerge.remove();
	}

	/**
	 * @param encounter the encounter the changed entity belongs to, or null
	 * @param previousPersonId the id of the person the entity belongs to in the database
	 * @param currentPersonId the id of the person the entity is about to belong to
	 * @return true if the entity moves to its encounter's patient, or from the non-preferred to the
	 *         preferred patient of the patient merge running on this thread
	 */
	static boolean isAllowedPersonChange(Encounter encounter, Integer previousPersonId, Integer currentPersonId) {
		if (currentPersonId == null) {
			return false;
		}
		if (encounter != null && encounter.getPatient() != null
		        && currentPersonId.equals(encounter.getPatient().getPatientId())) {
			return true;
		}
		return isPatientMergeMove(previousPersonId, currentPersonId);
	}

	/**
	 * @param fromPersonId the id of the person a record belongs to
	 * @param toPersonId the id of the person it is to belong to
	 * @return true if the patient merge running on the current thread moves records from the first
	 *         person to the second
	 * @since 3.0.0
	 */
	public static boolean isPatientMergeMove(Integer fromPersonId, Integer toPersonId) {
		PatientMerge merge = patientMerge.get();
		return merge != null && merge.notPreferredId().equals(fromPersonId) && merge.preferredId().equals(toPersonId);
	}

	/**
	 * @param personOrId a {@link Person}, a person id, or null
	 * @return the person id
	 */
	static Integer toPersonId(Object personOrId) {
		return personOrId instanceof Person ? ((Person) personOrId).getPersonId() : (Integer) personOrId;
	}

	private record PatientMerge(Integer notPreferredId, Integer preferredId) {
	}
}
