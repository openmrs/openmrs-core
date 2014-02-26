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
package org.openmrs.person;

import org.openmrs.BaseOpenmrsData;
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
public class PersonMergeLog extends BaseOpenmrsData {
	
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
	
	public void setId(Integer id) {
		setPersonMergeLogId(id);
	}
	
	public Integer getId() {
		return getPersonMergeLogId();
	}
	
}
