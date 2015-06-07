/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
// $ANTLR 2.7.6 (2005-12-22): "ArdenRecognizer.g" -> "ArdenBaseParser.java"$

package org.openmrs.arden;

import java.io.*;
import antlr.CommonAST;
import antlr.collections.AST;
import antlr.*;
import org.openmrs.arden.MLMObject;
import org.openmrs.arden.MLMObjectElement;
import java.lang.Integer;
import org.openmrs.logic.*;
import org.openmrs.logic.op.*;
import java.util.GregorianCalendar;

public interface ArdenBaseTreeParserTokenTypes {
	
	int EOF = 1;
	
	int NULL_TREE_LOOKAHEAD = 3;
	
	int AND = 4;
	
	int WEIRD_IDENT = 5;
	
	int IS = 6;
	
	int ARE = 7;
	
	int WAS = 8;
	
	int WERE = 9;
	
	int COUNT = 10;
	
	int IN = 11;
	
	int LESS = 12;
	
	int GREATER = 13;
	
	int THE = 14;
	
	int THAN = 15;
	
	int FROM = 16;
	
	int BEFORE = 17;
	
	int AFTER = 18;
	
	int AGO = 19;
	
	int WRITE = 20;
	
	int AT = 21;
	
	int LET = 22;
	
	int NOW = 23;
	
	int BE = 24;
	
	int YEAR = 25;
	
	int YEARS = 26;
	
	int IF = 27;
	
	int IT = 28;
	
	int THEY = 29;
	
	int NOT = 30;
	
	int OR = 31;
	
	int THEN = 32;
	
	int READ = 33;
	
	int MINIMUM = 34;
	
	int MIN = 35;
	
	int MAXIMUM = 36;
	
	int MAX = 37;
	
	int LAST = 38;
	
	int FIRST = 39;
	
	int EARLIEST = 40;
	
	int LATEST = 41;
	
	int EVENT = 42;
	
	int WHERE = 43;
	
	int EXIST = 44;
	
	int EXISTS = 45;
	
	int PAST = 46;
	
	int DAYS = 47;
	
	int DAY = 48;
	
	int MONTH = 49;
	
	int MONTHS = 50;
	
	int WEEK = 51;
	
	int WEEKS = 52;
	
	int AVG = 53;
	
	int AVERAGE = 54;
	
	int SUM = 55;
	
	int MEDIAN = 56;
	
	int CONCLUDE = 57;
	
	int ELSE = 58;
	
	int ELSEIF = 59;
	
	int ENDIF = 60;
	
	int TRUE = 61;
	
	int FALSE = 62;
	
	int DATA = 63;
	
	int LOGIC = 64;
	
	int ACTION = 65;
	
	int MAINTENANCE = 66;
	
	int KNOWLEDGE = 67;
	
	int LIBRARY = 68;
	
	int FILENAME = 69;
	
	int MLMNAME = 70;
	
	int TITLE = 71;
	
	int INSTITUTION = 72;
	
	int AUTHOR = 73;
	
	int PRIORITY = 74;
	
	int VERSION = 75;
	
	int SPECIALIST = 76;
	
	int PURPOSE = 77;
	
	int EXPLANATION = 78;
	
	int KEYWORDS = 79;
	
	int CITATIONS = 80;
	
	int LINKS = 81;
	
	int TYPE = 82;
	
	int DATE = 83;
	
	int AGE_MIN = 84;
	
	int AGE_MAX = 85;
	
	int OF = 86;
	
	int TIME = 87;
	
	int WITHIN = 88;
	
	int CALL = 89;
	
	int WITH = 90;
	
	int TO = 91;
	
	int ANY = 92;
	
	int RESEARCH = 93;
	
	int SECOND = 94;
	
	int OCCUR = 95;
	
	int PRESENT = 96;
	
	int NUMBER = 97;
	
	int HTTP = 98;
	
	int NULL = 99;
	
	int LITERAL_end = 100;
	
	int COLON = 101;
	
	int ENDBLOCK = 102;
	
	int DOT = 103;
	
	int MINUS = 104;
	
	int UNDERSCORE = 105;
	
	int LITERAL_arden = 106;
	
	// "ASTM-E" = 107
	int INTLIT = 108;
	
	int DIGIT = 109;
	
	int SEMI = 110;
	
	int LITERAL_validation = 111;
	
	int LITERAL_production = 112;
	
	int LITERAL_testing = 113;
	
	int LITERAL_expired = 114;
	
	int TIMES = 115;
	
	int ID = 116;
	
	int APOSTROPHE = 117;
	
	int AMPERSAND = 118;
	
	int PERCENT = 119;
	
	int GT = 120;
	
	int GTE = 121;
	
	int LT = 122;
	
	int LTE = 123;
	
	int POUND = 124;
	
	int COMMA = 125;
	
	int DIV = 126;
	
	int STRING_LITERAL = 127;
	
	int LPAREN = 128;
	
	int RPAREN = 129;
	
	int PLUS = 130;
	
	int LITERAL_SUPPORT = 131;
	
	int LITERAL_REFUTE = 132;
	
	int NOT_COMMENT = 133;
	
	int SINGLE_QUOTE = 134;
	
	// "data-driven" = 135
	int LITERAL_data_driven = 136;
	
	int COMMENT = 137;
	
	int ML_COMMENT = 138;
	
	int BECOMES = 139;
	
	int ARDEN_CURLY_BRACKETS = 140;
	
	int LITERAL_PRESENT = 141;
	
	int LITERAL_NULL = 142;
	
	int LITERAL_BOOLEAN = 143;
	
	int LITERAL_NUMBER = 144;
	
	int LITERAL_DURATION = 145;
	
	int LITERAL_STRING = 146;
	
	int LITERAL_LIST = 147;
	
	int LITERAL_OBJECT = 148;
	
	int EQUAL = 149;
	
	int LITERAL_hour = 150;
	
	int LITERAL_hours = 151;
	
	int LITERAL_minute = 152;
	
	int LITERAL_minutes = 153;
	
	int LITERAL_seconds = 154;
	
	int LITERAL_OCCUR = 155;
	
	int LITERAL_Occur = 156;
	
	int LITERAL_OCCURS = 157;
	
	int LITERAL_Occurs = 158;
	
	int LITERAL_occurs = 159;
	
	int LITERAL_OCCURRED = 160;
	
	int LITERAL_Occurred = 161;
	
	int LITERAL_evoke = 162;
	
	int LITERAL_CALL = 163;
	
	int EQUALS = 164;
	
	int LITERAL_EQ = 165;
	
	int LITERAL_LT = 166;
	
	int LITERAL_GT = 167;
	
	int LITERAL_LE = 168;
	
	int LITERAL_GE = 169;
	
	int NE = 170;
	
	int LITERAL_NE = 171;
	
	int ACTION_OP = 172;
	
	int LITERAL_urgency = 173;
	
	int LITERAL_MERGE = 174;
	
	int LITERAL_SORT = 175;
	
	int LITERAL_DATA = 176;
	
	int LITERAL_SEQTO = 177;
	
	// "+" = 178
	// "-" = 179
	// "*" = 180
	// "/" = 181
	int TERM_LITERAL = 182;
}
