/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.hl7;

import java.util.Hashtable;

import org.openmrs.util.PrivilegeConstants;

/**
 * Constants used by the hl7 package
 */
public class HL7Constants {
	
	@Deprecated
	public static final String PRIV_ADD_HL7_SOURCE = PrivilegeConstants.PRIV_ADD_HL7_SOURCE;
	
	@Deprecated
	public static final String PRIV_VIEW_HL7_SOURCE = PrivilegeConstants.PRIV_VIEW_HL7_SOURCE;
	
	@Deprecated
	public static final String PRIV_MANAGE_HL7_SOURCE = PrivilegeConstants.PRIV_UPDATE_HL7_SOURCE;
	
	@Deprecated
	public static final String PRIV_PURGE_HL7_SOURCE = PrivilegeConstants.PRIV_PURGE_HL7_SOURCE;
	
	@Deprecated
	public static final String PRIV_ADD_HL7_IN_QUEUE = PrivilegeConstants.PRIV_ADD_HL7_IN_QUEUE;
	
	@Deprecated
	public static final String PRIV_VIEW_HL7_IN_QUEUE = PrivilegeConstants.PRIV_VIEW_HL7_IN_QUEUE;
	
	@Deprecated
	public static final String PRIV_UPDATE_HL7_IN_QUEUE = PrivilegeConstants.PRIV_UPDATE_HL7_IN_QUEUE;
	
	@Deprecated
	public static final String PRIV_DELETE_HL7_IN_QUEUE = PrivilegeConstants.PRIV_DELETE_HL7_IN_QUEUE;
	
	@Deprecated
	public static final String PRIV_PURGE_HL7_IN_QUEUE = PrivilegeConstants.PRIV_PURGE_HL7_IN_QUEUE;
	
	@Deprecated
	public static final String PRIV_ADD_HL7_IN_ARCHIVE = PrivilegeConstants.PRIV_ADD_HL7_IN_ARCHIVE;
	
	@Deprecated
	public static final String PRIV_VIEW_HL7_IN_ARCHIVE = PrivilegeConstants.PRIV_VIEW_HL7_IN_ARCHIVE;
	
	@Deprecated
	public static final String PRIV_UPDATE_HL7_IN_ARCHIVE = PrivilegeConstants.PRIV_UPDATE_HL7_IN_ARCHIVE;
	
	@Deprecated
	public static final String PRIV_DELETE_HL7_IN_ARCHIVE = PrivilegeConstants.PRIV_DELETE_HL7_IN_ARCHIVE;
	
	@Deprecated
	public static final String PRIV_PURGE_HL7_IN_ARCHIVE = PrivilegeConstants.PRIV_PURGE_HL7_IN_ARCHIVE;
	
	@Deprecated
	public static final String PRIV_ADD_HL7_IN_EXCEPTION = PrivilegeConstants.PRIV_ADD_HL7_IN_EXCEPTION;
	
	@Deprecated
	public static final String PRIV_VIEW_HL7_IN_EXCEPTION = PrivilegeConstants.PRIV_VIEW_HL7_IN_EXCEPTION;
	
	@Deprecated
	public static final String PRIV_UPDATE_HL7_IN_EXCEPTION = PrivilegeConstants.PRIV_UPDATE_HL7_IN_EXCEPTION;
	
	@Deprecated
	public static final String PRIV_DELETE_HL7_IN_EXCEPTION = PrivilegeConstants.PRIV_DELETE_HL7_IN_EXCEPTION;
	
	@Deprecated
	public static final String PRIV_PURGE_HL7_IN_EXCEPTION = PrivilegeConstants.PRIV_PURGE_HL7_IN_EXCEPTION;
	
	public static final Integer HL7_STATUS_PENDING = 0;
	
	public static final Integer HL7_STATUS_PROCESSING = 1;
	
	public static final Integer HL7_STATUS_PROCESSED = 2;
	
	public static final Integer HL7_STATUS_ERROR = 3;
	
	public static final String HL7_TEXT = "ST";
	
	public static final String HL7_CODED = "CE";
	
	public static final String HL7_CODED_WITH_EXCEPTIONS = "CWE";
	
	public static final String HL7_NUMERIC = "NM";
	
	public static final String HL7_DATE = "DT";
	
	public static final String HL7_TIME = "TM";
	
	public static final String HL7_DATETIME = "TS";
	
	public static final String HL7_BOOLEAN = "BIT";
	
	public static final String HL7_AUTHORITY_UUID = "UUID";
	
	public static final String HL7_AUTHORITY_LOCAL = "L";
	
	public static final Object HL7_ID_PERSON = "PN";
	
	public static final Object HL7_ID_PATIENT = "PI";
	
	public static final Integer CLASS_DRUG = 3;
	
	/**
	 * Used in hl7 sextuplets: 123^Primary name^99DCT^345^Chosen name^99NAM
	 */
	public static final String HL7_LOCAL_CONCEPT = "99DCT";
	
	public static final String HL7_LOCAL_CONCEPT_NAME = "99NAM";
	
	public static final String HL7_LOCAL_DRUG = "99RX";
	
	public static final String HL7_LOCAL_RELATIONSHIP = "99REL";
	
	/**
	 * @since 1.5
	 */
	public static final int HL7_STATUS_DELETED = 4;
	
	/**
	 * @since 1.7
	 */
	public static final Integer HL7_STATUS_MIGRATED = 5;
	
	/**
	 * default name for HL7_archives destination directory
	 * 
	 * @since 1.7
	 */
	public static final String HL7_ARCHIVE_DIRECTORY_NAME = "hl7_archives";
	
	/**
	 * @since 1.10
	 */
	public static final String HL7_FORM_ID = "AMRS.ELD.FORMID";
	
	public static final String HL7_FORM_UUID = "AMRS.ELD.FORMUUID";
	
	/**
	 * the key to be used for numberTransferred archives in the progressStatusMap
	 * 
	 * @since 1.7
	 */
	public static final String NUMBER_TRANSFERRED_KEY = "transferred";
	
	/**
	 * the key to be used for numberOfFailedTransfers in the progressStatusMap
	 * 
	 * @since 1.7
	 */
	public static final String NUMBER_OF_FAILED_TRANSFERS_KEY = "failures";
	
	/**
	 * time taken before static/state properties of an instance of the hl7 in archive migration
	 * thread are reset
	 * 
	 * @since 1.7
	 */
	public static final long THREAD_SLEEP_PERIOD = 2000;
	
	/**
	 * the maximum number if archives to fetch per query to save on memory
	 * 
	 * @since 1.7
	 */
	public static final int MIGRATION_MAX_BATCH_SIZE = 2000;
	
	// List of datatypes that do not require complex definitions
	public static final Hashtable<String, String> simpleDatatypes = new Hashtable<String, String>();
	
	static {
		simpleDatatypes.put(HL7_TEXT, "xs:string");
		simpleDatatypes.put(HL7_DATE, "xs:date");
		simpleDatatypes.put(HL7_TIME, "xs:time");
		simpleDatatypes.put(HL7_DATETIME, "xs:dateTime");
		
		// We make a special boolean type with an extra attribute
		// to get InfoPath to treat booleans properly
		simpleDatatypes.put(HL7_BOOLEAN, "_infopath_boolean");
	}
	
	/**
	 * Assigning authority for an id for a provider in an HL7 message that specifies that it is a
	 * provider identifier
	 */
	public static final String PROVIDER_ASSIGNING_AUTH_IDENTIFIER = "PROVIDER.IDENTIFIER";
	
	/**
	 * Assigning authority for an id for a provider in an HL7 message that specifies that it is a
	 * provider id
	 */
	public static final String PROVIDER_ASSIGNING_AUTH_PROV_ID = "PROVIDER.ID";
	
	/**
	 * Assigning authority for an id for a provider in an HL7 message that specifies that it is a
	 * provider uuid
	 */
	public static final String PROVIDER_ASSIGNING_AUTH_PROV_UUID = "PROVIDER.UUID";
}
