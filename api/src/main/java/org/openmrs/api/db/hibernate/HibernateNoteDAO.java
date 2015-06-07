/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.NoteDAO;
import org.openmrs.notification.Note;

public class HibernateNoteDAO implements NoteDAO {
	
	protected final static Log log = LogFactory.getLog(HibernateNoteDAO.class);
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernateNoteDAO() {
	}
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @return List<Note> object of all Notes from the database
	 */
	@SuppressWarnings("unchecked")
	public List<Note> getNotes() {
		log.info("Getting all notes from the database");
		return sessionFactory.getCurrentSession().createQuery("from Note").list();
	}
	
	/**
	 * @see org.openmrs.api.db.NoteDAO#getNote(Integer)
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
	
	/**
	 * @see org.openmrs.api.db.NoteDAO#voidNote(org.openmrs.notification.Note, java.lang.String)
	 */
	public Note voidNote(Note note, String reason) throws APIException {
		log.debug("voiding note because " + reason);
		sessionFactory.getCurrentSession().save(note);
		return note;
	}
}
