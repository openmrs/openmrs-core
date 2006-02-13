package org.openmrs.form.db;

import org.openmrs.api.db.DAOException;
import org.openmrs.form.FormEntryQueue;

public interface FormEntryDAO {

	/****************************************************************
	 * FormEntryQueue Methods
	 ****************************************************************/
	
	public void createFormEntryQueue(FormEntryQueue formEntryQueue) throws DAOException;
	
	public FormEntryQueue getFormEntryQueue(int formEntryQueueId) throws DAOException;
	
	public void updateFormEntryQueue(FormEntryQueue formEntryQueue) throws DAOException;
	
	public FormEntryQueue getNextFormEntryQueue() throws DAOException;
	
	public void deleteFormEntryQueue(FormEntryQueue formEntryQueue) throws DAOException;
	
}
