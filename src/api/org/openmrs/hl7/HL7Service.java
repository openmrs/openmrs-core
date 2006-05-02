package org.openmrs.hl7;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOContext;
import org.openmrs.hl7.db.HL7DAO;

/**
 * OpenMRS HL7 API
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class HL7Service {

	private Log log = LogFactory.getLog(this.getClass());

	private Context context;
	private DAOContext daoContext;

	public HL7Service(Context c, DAOContext d) {
		this.context = c;
		this.daoContext = d;
	}

	private HL7DAO dao() {
		return daoContext.getHL7DAO();
	}

	public void createHL7Source(HL7Source hl7Source) {
		if (!context.hasPrivilege(HL7Constants.PRIV_ADD_HL7_SOURCE))
			throw new APIAuthenticationException(
					"Insufficient privilege to create an HL7 source");
		dao().createHL7Source(hl7Source);
	}

	public HL7Source getHL7Source(Integer hl7SourceId) {
		if (!context.hasPrivilege(HL7Constants.PRIV_VIEW_HL7_SOURCE))
			throw new APIAuthenticationException(
					"Insufficient privilege to view an HL7 source");
		return dao().getHL7Source(hl7SourceId);
	}

	public Collection<HL7Source> getHL7Sources() {
		if (!context.hasPrivilege(HL7Constants.PRIV_VIEW_HL7_SOURCE))
			throw new APIAuthenticationException(
					"Insufficient privilege to view an HL7 source");
		return dao().getHL7Sources();
	}

	public void updateHL7Source(HL7Source hl7Source) {
		if (!context.hasPrivilege(HL7Constants.PRIV_UPDATE_HL7_SOURCE))
			throw new APIAuthenticationException(
					"Insufficient privilege to update an HL7 source");
		dao().updateHL7Source(hl7Source);
	}

	public void deleteHL7Source(HL7Source hl7Source) {
		if (!context.hasPrivilege(HL7Constants.PRIV_DELETE_HL7_SOURCE))
			throw new APIAuthenticationException(
					"Insufficient privilege to delete an HL7 source");
		dao().deleteHL7Source(hl7Source);
	}

	public void createHL7InQueue(HL7InQueue hl7InQueue) {
		if (!context.hasPrivilege(HL7Constants.PRIV_ADD_HL7_IN_QUEUE))
			throw new APIAuthenticationException(
					"Insufficient privilege to create an HL7 inbound queue entry");
		dao().createHL7InQueue(hl7InQueue);
	}

	public HL7InQueue getHL7InQueue(Integer hl7InQueueId) {
		if (!context.hasPrivilege(HL7Constants.PRIV_VIEW_HL7_IN_QUEUE))
			throw new APIAuthenticationException(
					"Insufficient privilege to view an HL7 inbound queue entry");
		return dao().getHL7InQueue(hl7InQueueId);
	}

	public Collection<HL7InQueue> getHL7InQueues() {
		if (!context.hasPrivilege(HL7Constants.PRIV_VIEW_HL7_IN_QUEUE))
			throw new APIAuthenticationException(
					"Insufficient privilege to view an HL7 inbound queue entry");
		return dao().getHL7InQueues();
	}

	public HL7InQueue getNextHL7InQueue() {
		if (!context.hasPrivilege(HL7Constants.PRIV_VIEW_HL7_IN_QUEUE))
			throw new APIAuthenticationException(
					"Insufficient privilege to view an HL7 inbound queue entry");
		return dao().getNextHL7InQueue();
	}

	public void deleteHL7InQueue(HL7InQueue hl7InQueue) {
		if (!context.hasPrivilege(HL7Constants.PRIV_DELETE_HL7_IN_QUEUE))
			throw new APIAuthenticationException(
					"Insufficient privilege to delete an HL7 inbound queue entry");
		dao().deleteHL7InQueue(hl7InQueue);
	}
	
	public void createHL7InArchive(HL7InArchive hl7InArchive) {
		if (!context.hasPrivilege(HL7Constants.PRIV_ADD_HL7_IN_ARCHIVE))
			throw new APIAuthenticationException(
					"Insufficient privilege to create an HL7 inbound archive entry");		
		dao().createHL7InArchive(hl7InArchive);
	}

	public HL7InArchive getHL7InArchive(Integer hl7InArchiveId) {
		if (!context.hasPrivilege(HL7Constants.PRIV_VIEW_HL7_IN_ARCHIVE))
			throw new APIAuthenticationException(
					"Insufficient privilege to view an HL7 inbound archive entry");		
		return dao().getHL7InArchive(hl7InArchiveId);
	}
	
	public Collection<HL7InArchive> getHL7InArchives() {
		if (!context.hasPrivilege(HL7Constants.PRIV_VIEW_HL7_IN_ARCHIVE))
			throw new APIAuthenticationException(
					"Insufficient privilege to view an HL7 inbound archive entry");		
		return dao().getHL7InArchives();
	}
	
	public void updateHL7InArchive(HL7InArchive hl7InArchive) {
		if (!context.hasPrivilege(HL7Constants.PRIV_UPDATE_HL7_IN_ARCHIVE))
			throw new APIAuthenticationException(
					"Insufficient privilege to update an HL7 inbound archive entry");		
		dao().updateHL7InArchive(hl7InArchive);
	}
	
	public void deleteHL7InArchive(HL7InArchive hl7InArchive) {
		if (!context.hasPrivilege(HL7Constants.PRIV_DELETE_HL7_IN_ARCHIVE))
			throw new APIAuthenticationException(
					"Insufficient privilege to delete an HL7 inbound archive entry");		
		dao().deleteHL7InArchive(hl7InArchive);
	}

	public void createHL7InError(HL7InError hl7InError) {
		if (!context.hasPrivilege(HL7Constants.PRIV_ADD_HL7_IN_ARCHIVE))
			throw new APIAuthenticationException(
					"Insufficient privilege to create an HL7 inbound archive entry");		
		dao().createHL7InError(hl7InError);
	}

	public HL7InError getHL7InError(Integer hl7InErrorId) {
		if (!context.hasPrivilege(HL7Constants.PRIV_VIEW_HL7_IN_ARCHIVE))
			throw new APIAuthenticationException(
					"Insufficient privilege to view an HL7 inbound archive entry");		
		return dao().getHL7InError(hl7InErrorId);
	}
	
	public Collection<HL7InError> getHL7InErrors() {
		if (!context.hasPrivilege(HL7Constants.PRIV_VIEW_HL7_IN_ARCHIVE))
			throw new APIAuthenticationException(
					"Insufficient privilege to view an HL7 inbound archive entry");		
		return dao().getHL7InErrors();
	}
	
	public void updateHL7InError(HL7InError hl7InError) {
		if (!context.hasPrivilege(HL7Constants.PRIV_UPDATE_HL7_IN_ARCHIVE))
			throw new APIAuthenticationException(
					"Insufficient privilege to update an HL7 inbound archive entry");		
		dao().updateHL7InError(hl7InError);
	}
	
	public void deleteHL7InError(HL7InError hl7InError) {
		if (!context.hasPrivilege(HL7Constants.PRIV_DELETE_HL7_IN_ARCHIVE))
			throw new APIAuthenticationException(
					"Insufficient privilege to delete an HL7 inbound archive entry");		
		dao().deleteHL7InError(hl7InError);
	}

	public void garbageCollect() {
		dao().garbageCollect();
	}
}
