package org.openmrs.notification.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.db.DAOContext;
import org.openmrs.notification.Note;
import org.openmrs.notification.NoteService;

public class NoteServiceImpl implements NoteService, Serializable {    

	/**
	 * Serial version id
	 */
	private static final long serialVersionUID = 5649635694623650303L;
	
	/**
	 * Logger 
	 */
	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * Data access context
	 */
	private DAOContext daoContext;
	
	
	/**
	 * Public constructor.
	 *
	 */
	public NoteServiceImpl() { }
	
	
	/**
	 * Set the data access context.
	 * @param context
	 */
	public void setDAOContext(DAOContext context) { 
		this.daoContext = context;
	}
	
	/**
	 * Get all notes from the database.
	 * @return
	 * @throws Exception
	 */
	public Collection<Note> getNotes() throws Exception {		log.info("Get all notes");
		return daoContext.getNoteDAO().getNotes();
	}
  
	
	/**
	 * Creates a new note.
	 * @param note to be created
	 * @throws APIException
	 */
	public void createNote(Note note) throws Exception {		log.info("Create a note " + note);
		daoContext.getNoteDAO().createNote(note); 	}

	/**
	 * Get note by internal identifier
	 * @param noteId internal note identifier
	 * @return note with given internal identifier
	 * @throws APIException
	 */
	public Note getNote(Integer noteId) throws Exception {		log.info("Get note " + noteId);
		return daoContext.getNoteDAO().getNote(noteId);
	}

	/**
	 * Update a note.
	 * @param note to be updated
	 * @throws APIException
	 */
	public void updateNote(Note note) throws Exception {		log.info("Update note " + note);		daoContext.getNoteDAO().updateNote(note);
	}


	/**
	 * Get notes by user. 
	 * @param note to be updated
	 * @throws APIException
	 */
	public List<Note> getNotes(User user) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}  
	
}
