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
		List<Note> notes = new ArrayList<Note>();
		
		notes = sessionFactory.getCurrentSession().createQuery("from Note").list();
		
		return notes;
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
	
}
