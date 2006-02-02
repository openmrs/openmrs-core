package org.openmrs.form;

import java.util.Hashtable;

/**
 * Constants used by the form module.
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class FormConstants {

	// TODO: these constants should be read from a configuration file

	public static final Integer FIELD_TYPE_CONCEPT = 1;
	public static final Integer FIELD_TYPE_DATABASE = 2;
	public static final Integer FIELD_TYPE_TERM_SET = 3;
	public static final Integer FIELD_TYPE_MISC_SET = 4;
	public static final Integer FIELD_TYPE_SECTION = 5;

//	public static final Integer DATATYPE_NUMERIC = 1;
//	public static final Integer DATATYPE_CODED = 2;
//	public static final Integer DATATYPE_TEXT = 3;
//	public static final Integer DATATYPE_NA = 4;
//	public static final Integer DATATYPE_DOCUMENT = 5;
//	public static final Integer DATATYPE_DATE = 6;
//	public static final Integer DATATYPE_TIME = 7;
//	public static final Integer DATATYPE_DATETIME = 8;
//	public static final Integer DATATYPE_BOOLEAN = 10;
//	public static final Integer DATATYPE_STRUCTURED_NUMERIC = 12;

	public static final String HL7_TEXT = "ST";
	public static final String HL7_CODED = "CE";
	public static final String HL7_CODED_WITH_EXCEPTIONS = "CWE";
	public static final String HL7_NUMERIC = "NM";
	public static final String HL7_DATE = "DT";
	public static final String HL7_TIME = "TM";
	public static final String HL7_DATETIME = "TS";
	public static final String HL7_BOOLEAN = "BIT";
	
	public static final Integer CLASS_DRUG = 3;
	
	public static final String HL7_LOCAL_CONCEPT = "99DCT";
	public static final String HL7_LOCAL_DRUG = "99RX";
	
	// List of datatypes that do not require complex definitions
	public static final Hashtable<String, String> simpleDatatypes = new Hashtable<String, String>();
	static {
		simpleDatatypes.put(HL7_TEXT, "xs:string");
		simpleDatatypes.put(HL7_DATE, "xs:date");
		simpleDatatypes.put(HL7_TIME, "xs:time");
		simpleDatatypes.put(HL7_DATETIME, "xs:dateTime");

		// We make a special boolean type with an extra attribute
		// to get InfoPath to treat booleans properly
		simpleDatatypes
				.put(HL7_BOOLEAN, "_infopath_boolean");
	}
	
	public static final int INDENT_SIZE = 2;
}
