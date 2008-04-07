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
package org.openmrs.hl7;

import java.util.Collection;

import org.openmrs.Encounter;
import org.openmrs.hl7.db.HL7DAO;
import org.springframework.transaction.annotation.Transactional;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.datatype.PL;
import ca.uhn.hl7v2.model.v25.datatype.XCN;
import ca.uhn.hl7v2.model.v25.segment.PID;

@Transactional
public interface HL7Service {

	public void setHL7DAO(HL7DAO dao);

	public void createHL7Source(HL7Source hl7Source);

	@Transactional(readOnly=true)
	public HL7Source getHL7Source(Integer hl7SourceId);
	
	@Transactional(readOnly=true)
	public HL7Source getHL7Source(String name);

	@Transactional(readOnly=true)
	public Collection<HL7Source> getHL7Sources();

	public void updateHL7Source(HL7Source hl7Source);

	public void deleteHL7Source(HL7Source hl7Source);

	public void createHL7InQueue(HL7InQueue hl7InQueue);

	@Transactional(readOnly=true)
	public HL7InQueue getHL7InQueue(Integer hl7InQueueId);

	@Transactional(readOnly=true)
	public Collection<HL7InQueue> getHL7InQueues();

	@Transactional(readOnly=true)
	public HL7InQueue getNextHL7InQueue();

	public void deleteHL7InQueue(HL7InQueue hl7InQueue);

	public void createHL7InArchive(HL7InArchive hl7InArchive);

	@Transactional(readOnly=true)
	public HL7InArchive getHL7InArchive(Integer hl7InArchiveId);

	@Transactional(readOnly=true)
	public Collection<HL7InArchive> getHL7InArchives();

	public void updateHL7InArchive(HL7InArchive hl7InArchive);

	public void deleteHL7InArchive(HL7InArchive hl7InArchive);

	public void createHL7InError(HL7InError hl7InError);

	@Transactional(readOnly=true)
	public HL7InError getHL7InError(Integer hl7InErrorId);

	@Transactional(readOnly=true)
	public Collection<HL7InError> getHL7InErrors();

	public void updateHL7InError(HL7InError hl7InError);

	public void deleteHL7InError(HL7InError hl7InError);

	/**
	 * @param xcn
	 *            HL7 component of data type XCN (extended composite ID number
	 *            and name for persons) (see HL7 2.5 manual Ch.2A.86)
	 * @return Internal ID # of the specified user, or null if that user can't
	 *         be found or is ambiguous
	 */
	public Integer resolveUserId(XCN xcn) throws HL7Exception;

	/**
	 * @param pl
	 *            HL7 component of data type PL (person location) (see Ch
	 *            2.A.53)
	 * @return internal identifier of the specified location, or null if it is
	 *         not found or ambiguous
	 */
	public Integer resolveLocationId(PL pl) throws HL7Exception;

	/**
	 * @param pid
	 *            A PID segment of an hl7 message
	 * @return The internal id number of the Patient described by the PID
	 *         segment, or null of the patient is not found, or if the PID
	 *         segment is ambiguous
	 * @throws HL7Exception
	 */
	public Integer resolvePatientId(PID pid) throws HL7Exception;

	public void garbageCollect();

	/**
	 * This method is called after an encounter and its obs are created.  This method can be removed
	 * once we have obs groups being created correctly
	 * 
	 * @param encounter
	 * 
	 * @deprecated This method is no longer needed.  When an encounter is created in the 
	 * 	ROUR01 handler, it is created with all obs. Any AOP hooking should be done on the
	 * 	EncounterService.createEncounter(Encounter) method   
	 */
	public void encounterCreated(Encounter encounter);

}