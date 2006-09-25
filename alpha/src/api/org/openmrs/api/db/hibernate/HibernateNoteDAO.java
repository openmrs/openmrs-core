package org.openmrs.api.db.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.NoteDAO;
import org.openmrs.notification.Note;

public class HibernateNoteDAO implements NoteDAO {

	protected final static Log log = LogFactory.getLog(HibernateNoteDAO.class);
	
	private Context context;
	
	public HibernateNoteDAO() { }
	
	public HibernateNoteDAO(Context context) { 
		this.context = context;
	}

	/**
	 *  
	 * @return
	 */
	public List<Note> getNotes() {
		log.info("Getting all notes from the database");
		List<Note> notes = new ArrayList<Note>();
		
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			notes = session.createQuery("from Note").list();
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			log.error(e); 
			HibernateUtil.rollbackTransaction();
		}
		
		return notes;
	}
	
	
	/**
	 * @see org.openmrs.api.db.NoteService#getNote(java.lang.Long)
	 */
	public Note getNote(Integer id) {
		log.info("Get note " + id);
		Note note = new Note();
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			note = (Note) session.get(Note.class, id);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			log.error(e); 
			HibernateUtil.rollbackTransaction();
		}
		
		return note;
	}
	

	public void createNote(Note note) {		
		log.debug("Creating new note");
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			session.save(note);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			log.error(e);
			HibernateUtil.rollbackTransaction();
		}

	}

	

	public void updateNote(Note note) {		
		log.debug("Updating existing note");
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			session.save(note);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			log.error(e);
			HibernateUtil.rollbackTransaction();
		}
	
		
	}


	public void deleteNote(Note note) throws DAOException {
		log.debug("Deleting existing note");
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			session.delete(note);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			log.error(e); 
			HibernateUtil.rollbackTransaction();
		}

	}
	
}
