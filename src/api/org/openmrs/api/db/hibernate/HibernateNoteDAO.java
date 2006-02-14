package org.openmrs.api.db.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.SimpleExpression;
import org.openmrs.api.db.NoteDAO;
import org.openmrs.domain.Note;
import org.openmrs.api.db.DAOException;

public class HibernateNoteDAO implements NoteDAO {

	protected final static Log log = LogFactory.getLog(HibernateNoteDAO.class);
	
	public HibernateNoteDAO() { }

	private static SessionFactory sessionFactory;
	
	static {
		try {
			// Create the sessionFactory
			log.debug("Creating sessionFactory");
			sessionFactory = new Configuration().configure().buildSessionFactory();
		} catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			System.err.println("Initial sessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}	



	/**
	 *  
	 * @return
	 */
	public List<Note> getNotes() {
		log.info("Getting all notes from the database");
		List<Note> notes = new ArrayList<Note>();
		
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			notes = session.createQuery("from Note").list();
			tx.commit();
		}
		catch (Exception e) {
			if (tx!=null) tx.rollback();
		}
		finally {
			session.close();
		}		
		return notes;
	}
	
	
	/**
	 * @see org.openmrs.api.db.NoteService#getNote(java.lang.Long)
	 */
	public Note getNote(Integer id) {
		log.info("Get note " + id);
		Note note = new Note();
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			note = (Note) session.get(Note.class, id);
			tx.commit();
		}
		catch (Exception e) {
			if (tx!=null) tx.rollback();
		}
		finally {
			session.close();
		}		
		return note;
	}
	

	public void createNote(Note note) {		
		log.debug("Creating new note");
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(note);
			tx.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
			if (tx!=null) tx.rollback();
		}
		finally {
			session.close();
		}	
	}

	

	public void updateNote(Note note) {		
		log.debug("Updating existing note");
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(note);
			tx.commit();
		}
		catch (Exception e) {
			if (tx!=null) tx.rollback();
		}
		finally {
			session.close();
		}	
		
	}


	public void deleteNote(Note note) throws DAOException {
		log.debug("Updating existing note");
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(note);
			tx.commit();
		}
		catch (Exception e) {
			if (tx!=null) tx.rollback();
		}
		finally {
			session.close();
		}	
	}
	
}
