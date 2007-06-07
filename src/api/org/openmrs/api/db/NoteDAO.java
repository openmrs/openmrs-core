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
	 * @param note to be created
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
	 * @param noteId internal note identifier
	 * @return note with given internal identifier
	 * @throws DAOException
	 */
	public Note getNote(Integer noteId) throws DAOException;

	/**
	 * Update note 
	 * 
	 * @param note to be updated
	 * @throws DAOException
	 */
	public void updateNote(Note note) throws DAOException;

	/**
	 * Delete note from database. This <b>should not be called</b>
	 * except for testing and administration purposes.  Use the void
	 * method instead.
	 * 
	 * @param note note to be deleted
	 * 
	 * @see #voidNote(Note, String) 
	 */
	public void deleteNote(Note note) throws DAOException;
	

}
