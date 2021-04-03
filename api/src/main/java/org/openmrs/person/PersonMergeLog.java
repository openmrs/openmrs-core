/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.person;

import org.openmrs.BaseChangeableOpenmrsData;
import org.openmrs.Person;
import org.openmrs.api.PersonService;

/**
 * This class represents the audit of a merge of two persons. It provides as much details as
 * possible to allow potential recovery from an erroneous merge. The preferred <code>Person</code>
 * is represented by {@link #getWinner()}, the non-preferred <code>Person</code> by
 * {@link #getLoser()} and the the <code>User</code> who performed the merged by
 * {@link #getCreator()}. The details of the merge is represented in XML and stored in
 * {@link #getPersonMergeLogData()}.
 * 
 * @see PersonMergeLogData
 * @see PersonService#savePersonMergeLog(PersonMergeLog)
 * @since 1.9
 */
public class PersonMergeLog extends BaseChangeableOpenmrsData {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The unique identifier of the person merge log entity
	 */
	private Integer personMergeLogId;
	
	/**
	 * The object representing the preferred person of the merge
	 */
	private Person winner;
	
	/**
	 * The object representing the non-preferred person of the merge
	 */
	private Person loser;
	
	/**
	 * serialized data representing the details of the merge
	 */
	private String serializedMergedData;
	
	/**
	 * object representing the deserialized form of the merge data. This field is not directly
	 * mapped to the database.
	 */
	private transient PersonMergeLogData personMergeLogData;
	
	public Integer getPersonMergeLogId() {
		return personMergeLogId;
	}
	
	public void setPersonMergeLogId(Integer personMergeLogId) {
		this.personMergeLogId = personMergeLogId;
	}
	
	public Person getWinner() {
		return winner;
	}
	
	public void setWinner(Person winner) {
		this.winner = winner;
	}
	
	public Person getLoser() {
		return loser;
	}
	
	public void setLoser(Person loser) {
		this.loser = loser;
	}
	
	public String getSerializedMergedData() {
		return serializedMergedData;
	}
	
	public void setSerializedMergedData(String serializedMergedData) {
		this.serializedMergedData = serializedMergedData;
	}
	
	public PersonMergeLogData getPersonMergeLogData() {
		return personMergeLogData;
	}
	
	public void setPersonMergeLogData(PersonMergeLogData personMergeLogData) {
		this.personMergeLogData = personMergeLogData;
	}
	
	@Override
	public void setId(Integer id) {
		setPersonMergeLogId(id);
	}
	
	@Override
	public Integer getId() {
		return getPersonMergeLogId();
	}
	
}
