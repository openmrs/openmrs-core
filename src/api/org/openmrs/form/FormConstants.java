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

	public static final Integer DATATYPE_NUMERIC = 1;
	public static final Integer DATATYPE_CODED = 2;
	public static final Integer DATATYPE_TEXT = 3;
	public static final Integer DATATYPE_NA = 4;
	public static final Integer DATATYPE_DOCUMENT = 5;
	public static final Integer DATATYPE_DATE = 6;
	public static final Integer DATATYPE_TIME = 7;
	public static final Integer DATATYPE_DATETIME = 8;
	public static final Integer DATATYPE_BOOLEAN = 10;
	public static final Integer DATATYPE_STRUCTURED_NUMERIC = 12;

	public static final Integer CLASS_DRUG = 3;

	// List of datatypes that do not require complex definitions
	public static final Hashtable<Integer, String> simpleDatatypes = new Hashtable<Integer, String>();
	static {
		simpleDatatypes.put(DATATYPE_TEXT, "xs:string");
		simpleDatatypes.put(DATATYPE_DOCUMENT, "xs:string");
		simpleDatatypes.put(DATATYPE_DATE, "xs:date");
		simpleDatatypes.put(DATATYPE_TIME, "xs:time");
		simpleDatatypes.put(DATATYPE_DATETIME, "xs:dateTime");

		// We make a special boolean type with an extra attribute
		// to get InfoPath to treat booleans properly
		simpleDatatypes
				.put(FormConstants.DATATYPE_BOOLEAN, "_infopath_boolean");
	}
	
	public static final int INDENT_SIZE = 2;
	
	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
}
