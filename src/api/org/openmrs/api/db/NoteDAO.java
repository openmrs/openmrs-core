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
package org.openmrs.api.db;

import java.util.List;

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
	
}
