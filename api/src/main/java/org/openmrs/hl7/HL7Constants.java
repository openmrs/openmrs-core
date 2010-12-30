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

import java.util.Hashtable;

/**
 * Constants used by the hl7 package
 */
public class HL7Constants {
	
	public static final String PRIV_ADD_HL7_SOURCE = "Add HL7 Source";
	
	public static final String PRIV_VIEW_HL7_SOURCE = "View HL7 Source";
	
	public static final String PRIV_MANAGE_HL7_SOURCE = "Update HL7 Source";
	
	public static final String PRIV_PURGE_HL7_SOURCE = "Purge HL7 Source";
	
	public static final String PRIV_ADD_HL7_IN_QUEUE = "Add HL7 Inbound Queue";
	
	public static final String PRIV_VIEW_HL7_IN_QUEUE = "View HL7 Inbound Queue";
	
	public static final String PRIV_UPDATE_HL7_IN_QUEUE = "Update HL7 Inbound Queue";
	
	public static final String PRIV_DELETE_HL7_IN_QUEUE = "Delete HL7 Inbound Queue";
	
	public static final String PRIV_PURGE_HL7_IN_QUEUE = "Purge HL7 Inbound Queue";
	
	public static final String PRIV_ADD_HL7_IN_ARCHIVE = "Add HL7 Inbound Archive";
	
	public static final String PRIV_VIEW_HL7_IN_ARCHIVE = "View HL7 Inbound Archive";
	
	public static final String PRIV_UPDATE_HL7_IN_ARCHIVE = "Update HL7 Inbound Archive";
	
	public static final String PRIV_DELETE_HL7_IN_ARCHIVE = "Delete HL7 Inbound Archive";
	
	public static final String PRIV_PURGE_HL7_IN_ARCHIVE = "Purge HL7 Inbound Archive";
	
	public static final String PRIV_ADD_HL7_IN_EXCEPTION = "Add HL7 Inbound Exception";
	
	public static final String PRIV_VIEW_HL7_IN_EXCEPTION = "View HL7 Inbound Exception";
	
	public static final String PRIV_UPDATE_HL7_IN_EXCEPTION = "Update HL7 Inbound Exception";
	
	public static final String PRIV_DELETE_HL7_IN_EXCEPTION = "Delete HL7 Inbound Exception";
	
	public static final String PRIV_PURGE_HL7_IN_EXCEPTION = "Purge HL7 Inbound Exception";
	
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
}
