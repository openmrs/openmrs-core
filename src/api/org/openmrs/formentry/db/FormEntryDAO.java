package org.openmrs.formentry.db;

import java.util.Collection;

import org.openmrs.api.db.DAOException;
import org.openmrs.formentry.FormEntryQueue;

public interface FormEntryDAO {

	/****************************************************************
	 * FormEntryQueue Methods
	 ****************************************************************/
	
	public void createFormEntryQueue(FormEntryQueue formEntryQueue) throws DAOException;
	
	public FormEntryQueue getFormEntryQueue(int formEntryQueueId) throws DAOException;
	
	public Collection<FormEntryQueue> getFormEntryQueues() throws DAOException;
	
	public void updateFormEntryQueue(FormEntryQueue formEntryQueue) throws DAOException;
	
	public FormEntryQueue getNextFormEntryQueue() throws DAOException;
	
	public void deleteFormEntryQueue(FormEntryQueue formEntryQueue) throws DAOException;
	
}
