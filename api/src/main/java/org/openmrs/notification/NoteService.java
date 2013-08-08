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
package org.openmrs.notification;

import java.util.List;

import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.util.PrivilegeConstants;

/**
 * The NoteService is concerned with creating/updating/voiding and getting a Note to/from the
 * database Usage.
 */
public interface NoteService {
	
	/**
	 * Get all Notes for a given user, Does not return voided notes.</br>
	 * 
	 * @param user the user to match on
	 * @return a List<Note> object containing all non-voided notes for the specified user Get all
	 *         Notes for a given user, Does not return voided notes.</br>
	 * @param user the user to match on
	 * @return a List<Note> object containing all non-voided notes for the specified user
	 */
	@Authorized(PrivilegeConstants.GET_NOTE)
	public List<Note> getNotes(User user) throws Exception;
	
	/**
	 * Creates a Note for a user. Creates a Note for a person.
	 * 
	 * @param note, the Note being created
	 */
	public void createNote(Note note) throws Exception;
	
	/**
	 * Gets a note by internal primary key identifier </br>
	 * 
	 * @param noteId, the internal primary key identifier for a Note
	 */
	public Note getNote(Integer noteId) throws Exception;
	
	/**
	 * Save or update the given <code>note</code> in the database </br>
	 * 
	 * @param note, note being updated
	 */
	public void updateNote(Note note) throws Exception;
	
	/**
	 * Mark a note as voided. This functionally removes the Note from the system while keeping a
	 * semblance </br>
	 * 
	 * @param note, note being voided
	 * @param reason, a reason for the the void action </br>
	 * @should void the Note and set the voidReason
	 */
	@Authorized(PrivilegeConstants.DELETE_NOTE)
	public Note voidNote(Note note, String reason) throws APIException;
}
