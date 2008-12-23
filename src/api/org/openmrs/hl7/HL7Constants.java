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
	
	public static final int HL7_STATUS_PENDING = 0;
	public static final int HL7_STATUS_PROCESSING = 1;
	public static final int HL7_STATUS_PROCESSED = 2;
	public static final int HL7_STATUS_ERROR = 3;
	public static final int HL7_STATUS_DELETED = 4;
}
