package org.openmrs.notification.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.db.NoteDAO;
import org.openmrs.notification.Note;
import org.openmrs.notification.NoteService;

public class NoteServiceImpl implements NoteService, Serializable {    

	/**
	 * Serial version id
	 */
	private static final long serialVersionUID = 5649635694623650303L;
	
	private NoteDAO dao;
	
	private NoteDAO getNoteDAO() {
		return dao;
	}
	
	public void setNodeDAO(NoteDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * Logger 
	 */
	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * Public constructor.
	 *
	 */
	public NoteServiceImpl() { }
	
	/**
	 * Get all notes from the database.
	 * @return
	 * @throws Exception
	 */
	public Collection<Note> getNotes() throws Exception {		log.info("Get all notes");
		return getNoteDAO().getNotes();
	}
  
	
	/**
	 * Creates a new note.
	 * @param note to be created
	 * @throws APIException
	 */
	public void createNote(Note note) throws Exception {		log.info("Create a note " + note);
		getNoteDAO().createNote(note); 	}

	/**
	 * Get note by internal identifier
	 * @param noteId internal note identifier
	 * @return note with given internal identifier
	 * @throws APIException
	 */
	public Note getNote(Integer noteId) throws Exception {		log.info("Get note " + noteId);
		return getNoteDAO().getNote(noteId);
	}

	/**
	 * Update a note.
	 * @param note to be updated
	 * @throws APIException
	 */
	public void updateNote(Note note) throws Exception {		log.info("Update note " + note);		getNoteDAO().updateNote(note);
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
