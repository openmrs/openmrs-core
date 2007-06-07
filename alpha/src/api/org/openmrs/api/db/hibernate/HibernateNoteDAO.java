package org.openmrs.api.db.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.NoteDAO;
import org.openmrs.notification.Note;

public class HibernateNoteDAO implements NoteDAO {

	protected final static Log log = LogFactory.getLog(HibernateNoteDAO.class);

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernateNoteDAO() { }
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) { 
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 *  
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Note> getNotes() {
		log.info("Getting all notes from the database");
		List<Note> notes = new ArrayList<Note>();
		
		notes = sessionFactory.getCurrentSession().createQuery("from Note").list();
		
		return notes;
	}
	
	
	/**
	 * @see org.openmrs.api.db.NoteService#getNote(java.lang.Long)
	 */
	public Note getNote(Integer id) {
		log.info("Get note " + id);
		return (Note) sessionFactory.getCurrentSession().get(Note.class, id);
	}
	

	public void createNote(Note note) {		
		log.debug("Creating new note");
		sessionFactory.getCurrentSession().save(note);
	}

	

	public void updateNote(Note note) {		
		log.debug("Updating existing note");
		sessionFactory.getCurrentSession().save(note);
	}


	public void deleteNote(Note note) throws DAOException {
		log.debug("Deleting existing note");
		sessionFactory.getCurrentSession().delete(note);
	}
	
}
