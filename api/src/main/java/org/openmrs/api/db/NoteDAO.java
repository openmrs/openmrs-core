/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import java.util.List;

import org.openmrs.api.APIException;
import org.openmrs.notification.Note;

/**
 * Note-related database functions
 * 
 * @version 1.0
 */
public interface NoteDAO {
	
	/**
	 * Get all notes
	 * 
	 * @return List<Note> object with all Notes
	 * @throws DAOException
	 */
	public List<Note> getNotes() throws DAOException;
	
	/**
	 * Creates a new note record
	 * 
	 * @param note to be created
	 * @throws DAOException
	 */
	public void createNote(Note note) throws DAOException;
	
	/**
	 * Get note by internal identifier
	 * 
	 * @param noteId Internal integer identifier for requested Note
	 * @return <code>Note</code> with given internal identifier
	 * @throws DAOException
	 */
	public Note getNote(Integer noteId) throws DAOException;
	
	/**
	 * Update note
	 * 
	 * @param note <code>Note</code> to be updated
	 * @throws DAOException
	 */
	public void updateNote(Note note) throws DAOException;
	
	/**
	 * TODO: Couldn't find a voidNote method Delete note from database. This <b>should not be
	 * called</b> except for testing and administration purposes. Use the void method instead.
	 * 
	 * @param note <code>Note</code> to be deleted
	 * @throws DAOException
	 */
	public void deleteNote(Note note) throws DAOException;
	
	/**
	 * @param note <code>Note</code> to be voided
	 * @param reason <code>Reason</code> for having <code>Note</code> void
	 * @throws APIException
	 */
	public Note voidNote(Note note, String reason) throws APIException;
}
