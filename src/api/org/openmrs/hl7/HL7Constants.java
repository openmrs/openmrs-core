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
	
	/**
	 * @since 1.5
	 */
	public static final int HL7_STATUS_DELETED = 4;

    /**
     * NK1 Segment Constants
     * From HL7 2.5 Section 3.4.5.3 NK1-3 Relationship (CE) 00192
     * User-defined Table 0063 - Relationship
     * TODO: Not sure if this is the best way to handle this.
     * Does not seem that HAPI keeps these constants.
     * Using a HashMap of Code to English Description does not seem great either.
     * Putting this here allows use by Handlers in core as well as modules
     * that need to use the same constants.
     *
     */
    public class NK1 {
        public static final String SELF = "SEL";
        public static final String SPOUSE = "SPO";
        public static final String LIFE_PARTNER = "DOM";
        public static final String CHILD = "CHD";
        public static final String GRANDCHILD = "GCH";
        public static final String NATURAL_CHILD = "NCH";
        public static final String STEPCHILD = "SCH";
        public static final String FOSTER_CHILD = "FCH";
        public static final String HANDICAPPED_DEPENDENT = "DEP";
        public static final String WARD_OF_COURT = "WRD";
        public static final String PARENT = "PAR";
        public static final String MOTHER = "MTH";
        public static final String FATHER = "FTH";
        public static final String CAREGIVER = "CGV";
        public static final String GUARDIAN = "GRD";
        public static final String GRANDPARENT = "GRP";
        public static final String EXTENDED_FAMILY = "EXT";
        public static final String SIBLING = "SIB";
        public static final String BROTHER = "BRO";
        public static final String SISTER = "SIS";
        public static final String FRIEND = "FND";
        public static final String OTHER_ADULT = "OAD";
        public static final String EMPLOYEE = "EME";
        public static final String EMPLOYER = "EMR";
        public static final String ASSOCIATE = "ASC";
        public static final String EMERGENCY_CONTACT = "EMC";
        public static final String OWNER = "OWN";
        public static final String TRAINER = "TRA";
        public static final String MANAGER = "MGR";
        public static final String NONE = "NON";
        public static final String UNKNOWN = "UNK";
        public static final String OTHER = "OTH"; 
    }
}
