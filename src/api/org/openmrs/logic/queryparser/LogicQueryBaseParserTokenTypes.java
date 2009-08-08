// $ANTLR 2.7.6 (2005-12-22): "LogicQueryParser.g" -> "LogicQueryTreeParser.java"$

package org.openmrs.logic.queryparser;

public interface LogicQueryBaseParserTokenTypes {
	
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
	
	int TIMES = 100;
	
	int ID = 101;
	
	int INTLIT = 102;
	
	int MINUS = 103;
	
	int COMMA = 104;
	
	int DOT = 105;
	
	int DIV = 106;
	
	int UNDERSCORE = 107;
	
	int STRING_LITERAL = 108;
	
	int LPAREN = 109;
	
	int RPAREN = 110;
	
	int ENDBLOCK = 111;
	
	// ":" = 112
	int LITERAL_T = 113;
	
	int LITERAL_t = 114;
	
	// "." = 115
	int DIGIT = 116;
	
	// "+" = 117
	// "-" = 118
	int LITERAL_Z = 119;
	
	int LITERAL_z = 120;
	
	int LITERAL_PRESENT = 121;
	
	int LITERAL_NULL = 122;
	
	int LITERAL_BOOLEAN = 123;
	
	int LITERAL_NUMBER = 124;
	
	int LITERAL_DURATION = 125;
	
	int LITERAL_STRING = 126;
	
	int LITERAL_LIST = 127;
	
	int LITERAL_OBJECT = 128;
	
	int EQUAL = 129;
	
	int LITERAL_hour = 130;
	
	int LITERAL_hours = 131;
	
	int LITERAL_minute = 132;
	
	int LITERAL_minutes = 133;
	
	int LITERAL_seconds = 134;
	
	int EQUALS = 135;
	
	int LITERAL_EQ = 136;
	
	int LT = 137;
	
	int LITERAL_LT = 138;
	
	int GT = 139;
	
	int LITERAL_GT = 140;
	
	int LTE = 141;
	
	int LITERAL_LE = 142;
	
	int GTE = 143;
	
	int LITERAL_GE = 144;
	
	int NE = 145;
	
	int LITERAL_NE = 146;
	
	int LITERAL_OCCUR = 147;
	
	int LITERAL_Occur = 148;
	
	int LITERAL_OCCURS = 149;
	
	int LITERAL_Occurs = 150;
	
	int LITERAL_occurs = 151;
	
	int LITERAL_OCCURRED = 152;
	
	int LITERAL_Occurred = 153;
	
	int LITERAL_MERGE = 154;
	
	int LITERAL_SORT = 155;
	
	int LITERAL_DATA = 156;
	
	int LITERAL_SEQTO = 157;
	
	int ACTION_OP = 158;
	
	// "*" = 159
	// "/" = 160
	int TERM_LITERAL = 161;
	
	int SEMI = 162;
	
	int ARDEN_CURLY_BRACKETS = 163;
	
	int NOT_COMMENT = 164;
	
	int COMMENT = 165;
	
	int ML_COMMENT = 166;
	
	int WS = 167;
	
	int BECOMES = 168;
	
	int COLON = 169;
	
	int LBRACKET = 170;
	
	int RBRACKET = 171;
	
	int DOTDOT = 172;
	
	int NOT_EQUALS = 173;
	
	int PLUS = 174;
	
	int SINGLE_QUOTE = 175;
	
	int LCURLY = 176;
	
	int RCURLY = 177;
}
