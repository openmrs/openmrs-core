package org.openmrs.formentry.db;

import java.util.Collection;

import org.openmrs.api.db.DAOException;
import org.openmrs.formentry.FormEntryArchive;
import org.openmrs.formentry.FormEntryError;
import org.openmrs.formentry.FormEntryQueue;

public interface FormEntryDAO {

	/****************************************************************
	 * FormEntryQueue Methods
	 ****************************************************************/
	
	public void createFormEntryQueue(FormEntryQueue formEntryQueue) throws DAOException;
	
	public FormEntryQueue getFormEntryQueue(Integer formEntryQueueId) throws DAOException;
	
	public Collection<FormEntryQueue> getFormEntryQueues() throws DAOException;
	
	public void updateFormEntryQueue(FormEntryQueue formEntryQueue) throws DAOException;
	
	public FormEntryQueue getNextFormEntryQueue() throws DAOException;
	
	public void deleteFormEntryQueue(FormEntryQueue formEntryQueue) throws DAOException;
	
	public Integer getFormEntryQueueSize() throws DAOException;
	
	
	public void createFormEntryArchive(FormEntryArchive formEntryArchive) throws DAOException;
	
	public FormEntryArchive getFormEntryArchive(Integer formEntryArchiveId) throws DAOException;
	
	public Collection<FormEntryArchive> getFormEntryArchives() throws DAOException;
	
	public void deleteFormEntryArchive(FormEntryArchive formEntryArchive) throws DAOException;
	
	public Integer getFormEntryArchiveSize() throws DAOException;
	
	
	public void createFormEntryError(FormEntryError formEntryError) throws DAOException;
	
	public FormEntryError getFormEntryError(Integer formEntryErrorId) throws DAOException;
	
	public Collection<FormEntryError> getFormEntryErrors() throws DAOException;
	
	public void updateFormEntryError(FormEntryError formEntryError) throws DAOException;
	
	public void deleteFormEntryError(FormEntryError formEntryError) throws DAOException;

	public Integer getFormEntryErrorSize() throws DAOException;
	
	public void garbageCollect();
}
