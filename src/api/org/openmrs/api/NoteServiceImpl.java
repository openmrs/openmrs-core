package org.openmrs.api;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOContext;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.domain.Note;
import org.openmrs.User;


import java.util.Collection;
import java.util.List;

public class NoteServiceImpl implements NoteService, Serializable {    

	/**
	 * 
	 */
	private static final long serialVersionUID = 5649635694623650303L;
	
	private Log log = LogFactory.getLog(this.getClass());
	private DAOContext daoContext;
	
	public NoteServiceImpl() { }
	
	
	public void setDAOContext(DAOContext context) { 
		this.daoContext = context;
	}
	

	public Collection<Note> getNotes() throws Exception {
    	log.info("Get all notes");
		return daoContext.getNoteDAO().getNotes();
	}

    
	
	/**
	 * Creates a new note record
	 * 
	 * @param note to be created
	 * @throws APIException
	 */
	public void createNote(Note note) throws Exception {
    	log.info("Create a note " + note);
		daoContext.getNoteDAO().createNote(note);
        
		//context.publishEvent( new CreateNoteEvent(this, note) );
		//mailSender.sendMessage("justin.miranda@tlakeenterprises.com", "notes have been activated.");
	}

	/**
	 * Get note by internal identifier
	 * 
	 * @param noteId internal note identifier
	 * @return note with given internal identifier
	 * @throws APIException
	 */
	public Note getNote(Integer noteId) throws Exception {
    	log.info("Get note " + noteId);
		return daoContext.getNoteDAO().getNote(noteId);
	}

	/**
	 * Update note 
	 * 
	 * @param note to be updated
	 * @throws APIException
	 */
	public void updateNote(Note note) throws Exception {
    	log.info("Update note " + note);
    	daoContext.getNoteDAO().updateNote(note);
		//context.publishEvent( new UpdateNoteEvent(this, note) );
	}


	public List<Note> getNotes(User user) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
    
	
}
