header {
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

}



// @@parser
//-----------------------------------------------------------------------------
// Define a Parser, calling it ArdenRecognizer
//-----------------------------------------------------------------------------
class ArdenBaseParser extends Parser;
options {
  /*defaultErrorHandler = true; */     // Don't generate parser error handlers
  buildAST=true;
 // exportVocab=ArdenBaseParser;
 
  
}

tokens {
	AND="and" ;
	WEIRD_IDENT;
	IS="is";
 	ARE="are";
 	WAS="was";
 	WERE="were";
 	COUNT="count";
 	IN="in";
 	LESS="less";
 	GREATER="greater";
 	THE="the";
 	THAN="than";
 	FROM="from";
 	BEFORE="before";
 	AFTER="after";
 	AGO="ago";
 	WRITE="write";
 	AT="at";
 	LET="let";
 	NOW="now";
 	BE="be";
 	YEAR="year";
 	YEARS="years";
 	IF="if";
 	IT="it";
 	THEY="they";
 	NOT="not";
 	OR="or";
 	THEN="then";
 	READ="read";
//	READ<AST=ArdenReadNode>;
 	MINIMUM="minimum";
 	MIN="min";
	MAXIMUM="maximum";
	MAX="max";
	LAST="last";
	FIRST="first";
	EARLIEST="earliest";
	LATEST="lastest";
	EVENT="event";
	WHERE="where";
	EXIST="exist"; 
	EXISTS="exists";
	PAST="past";
	DAYS="days";
	DAY="day";
	MONTH="month";
	MONTHS="months";
	WEEK="week";
	WEEKS="weeks";
	AVG="avg";
	AVERAGE="average";
	SUM="sum";
	MEDIAN="median";
	CONCLUDE="conclude";
	ELSE="else";
    ELSEIF="elseif";
	ENDIF="endif";
	TRUE="true";
	FALSE="false";
	
	DATA="data" ;
	LOGIC="logic";
	ACTION="action";
	MAINTENANCE ="maintenance";
	KNOWLEDGE="knowledge";
	LIBRARY="library";
	FILENAME="filename";
	MLMNAME="mlmname";
	TITLE="title";
	INSTITUTION="institution";
	AUTHOR="author";
	PRIORITY="priority";
	VERSION="version";
	SPECIALIST="specialist";
	PURPOSE="purpose";
	EXPLANATION="explanation";
	KEYWORDS="keywords";
	CITATIONS="citations";
	LINKS="links";
	TYPE="type";
	DATE="date";
	AGE_MIN="age_min";
	AGE_MAX="age_max";
	
	OF = "of";
	TIME = "time";
	WITHIN = "within";
	CALL = "call";
	WITH = "with";
	TO = "to";
	ANY = "any";
	RESEARCH = "research";
	SECOND = "second";
	OCCUR = "occur";
	PRESENT = "present";
	NUMBER = "number";
	HTTP = "http";
	NULL = "null";
	
}


// the following tag is used to find the start of the rules section for
//   automated chunk-grabbing when displaying the page
// @@startrules

startRule:
	  maintenance_category
	  library_category
	  knowledge_category
	  "end"! COLON!
	  ;
	 
maintenance_category:	
   	("maintenance"^ COLON!) maintenance_body
  	;

maintenance_body :
	  title_slot
	  mlmname_slot
	  arden_version_slot
	  version_slot
	  institution_slot
	  author_slot
	  specialist_slot
	  date_slot
	  validation_slot
	;

library_category:
	 "library"^ COLON! library_body
	;

library_body:
	purpose_slot
	explanation_slot
	keywords_slot
	citations_slot
	links_slot
	;

knowledge_category
	: ("knowledge"^ COLON!) knowledge_body
	;

knowledge_body
	: type_slot!
	data_slot
	priority_slot
	evoke_slot!
	logic_slot
	action_slot
	urgency_slot!
	age_min_slot
	age_max_slot
	;

/********** Maintenance Slots **********************/
title_slot: (TITLE^ COLON (text)* ENDBLOCK
	)
   	;
exception catch [MismatchedTokenException mv]{
  reportError(mv); System.err.println("***Rule Priority NOT SET***");
  consumeUntil(ENDBLOCK); // throw away all until ';;'
  consume();
 }
 
mlmname_slot :
	  MLMNAME^ COLON mlmname_text 
	| FILENAME^ COLON mlmname_text  
	;
									
mlmname_text
	:
	text (mlmname_text_rest) 
	
	 
	;

mlmname_text_rest :
	| DOT (text)* ENDBLOCK
	| MINUS (text)* ENDBLOCK
	| UNDERSCORE (text)* ENDBLOCK
	| ENDBLOCK

	;

arden_version_slot :
	  "arden" COLON (("ASTM-E" INTLIT MINUS INTLIT) | ("version" version_num)) ENDBLOCK
	| /*empty*/	
	;
									
//arden_version :
//	(("ASTM-" (INTLIT MINUS INTLIT)) | ("version" version_num)) ENDBLOCK

//	  ;
//version_text 
//	:
//	"VERSION" | "Version" | "version"
//	;

version_num
	:
		INTLIT |  DIGIT DOT DIGIT
	;

version_slot :
	  VERSION^ COLON INTLIT DOT INTLIT ENDBLOCK
	  ;

mlm_version :
	  text
	  ;

institution_slot :
	  INSTITUTION^ COLON (text)* ENDBLOCK		/* text limited to 80 characters */
	  ;

author_slot :
	  AUTHOR^ COLON (text)* (SEMI (text)*)* ENDBLOCK			/* see 6.1.6 for details */
	  ;

specialist_slot :
	  SPECIALIST^ COLON (text)* ENDBLOCK		/* see 6.1.7 for details */
	  ;

date_slot :
	  DATE^ COLON mlm_date ENDBLOCK
	  ;


mlm_date :
 iso_date_time
	;

validation_slot :
	  "validation" COLON validation_code ENDBLOCK
	  ;

validation_code :
		  "production" 
		| "research"
		| "testing"
		| "expired"
		|
	;

//text:
//   (IDORTEXT | INTLIT | DOT)* 
//	;


// Wraps the ID token from the lexer, in order to provide
// 'keyword as identifier' trickery.
any_reserved_word
	: AND | IS | ARE | WAS | WERE | COUNT | IN | LESS | THE | THAN | FROM | BEFORE |AFTER | AGO | AT | OF
	| WRITE | BE | LET | YEAR | YEARS | IF | IT | THEY | NOT | OR | THEN | MONTH | MONTHS | TIME | TIMES | WITHIN
	| READ | MINIMUM | MIN | MAXIMUM | MAX | LAST | FIRST | EARLIEST | LATEST | EVENT | WHERE | EXIST | EXISTS | PAST
	| AVERAGE | AVG | SUM | MEDIAN | CONCLUDE | ELSE | ELSEIF | ENDIF | TRUE | FALSE | DATA | LOGIC | ACTION | CALL | WITH
	| TO | ANY | RESEARCH | DAY | SECOND | OCCUR | PURPOSE | PRESENT | NUMBER | KNOWLEDGE | PRIORITY | GREATER | LESS | MAINTENANCE
	;

text
    : ID (APOSTROPHE | AMPERSAND | PERCENT | GT | GTE | LT | LTE | POUND)? | (any_reserved_word) | INTLIT | 
    MINUS | COMMA| DOT | DIV | UNDERSCORE|AT|STRING_LITERAL| 
    (LPAREN (ID (APOSTROPHE | AMPERSAND | PERCENT | GT | GTE | LT | LTE | POUND)? | INTLIT| (any_reserved_word))* RPAREN)
//    exception
//    catch [RecognitionException ex]
//    {
//       text_AST = handleIdentifierError(LT(1),ex);
//    }
    ;

//iso_date : 				/* no spaces are permitted between elements */
//	datepart (timepart | ) 
//	;



iso_date_time : 			/* no spaces are permitted between elements */
	datepart 

	
	
	;

datepart:	
	(INTLIT MINUS INTLIT MINUS INTLIT ) // ******needs improvement
	(
		(ID)  (COLON INTLIT COLON INTLIT)  ( DOT DIGIT)?
		time_zone 	
	)?
	
	
	
	;



/*
timepart_opt
	:
	 |(timepart) ENDBLOCK
	
	;
*/

/*
timepart:
	time
	INTLIT ":" INTLIT ":" INTLIT
	fractional_seconds
	time_zone ENDBLOCK
	;
*/
/*
time :
	  "T"
	| "t"
	;
*/

//fractional_seconds : 		/* no spaces are permitted between elements */
//	  "." DIGIT
//	| /* empty */
//	;


time_zone : 				/* no spaces are permitted between elements */

( 			| ID
			| PLUS INTLIT (COLON INTLIT)*
			| MINUS INTLIT (COLON INTLIT)* 
			)
	;	exception catch [NoViableAltException nv]{
  reportError(nv);
  consumeUntil(ENDBLOCK); // throw away all until ';;'
 
 }
	
/*
zulu :
	  "Z"
	| "z" 
	;
*/

/*****************Library slots*********************************/

purpose_slot:
	  PURPOSE^ COLON (text)* ENDBLOCK
	;
exception catch [MismatchedTokenException mv]{
  reportError(mv); 
  consumeUntil(ENDBLOCK); // throw away all until ';;'
  consume();
 }

explanation_slot:
	  EXPLANATION^ COLON 
	  (text | INTLIT)*
	  ENDBLOCK
	;	
exception catch [MismatchedTokenException mv]{
  reportError(mv); 
  consumeUntil(ENDBLOCK); // throw away all until ';;'
  consume();
 }
 
keywords_slot:
	  KEYWORDS^ COLON (keyword_text) 
	  ;

keyword_text
	:
	  (text)* (SEMI (text)*)* ENDBLOCK
	;
	  
citations_slot:
	|
	| CITATIONS^ COLON (citations_list) ENDBLOCK
	;


citations_list:

	|  /* empty */
	| single_citation (SEMI single_citation)*
	;

single_citation:
	  (INTLIT DOT citation_type)* citation_text 
	
	;
exception catch [NoViableAltException nv]{
  reportError(nv);
  consumeUntil(ENDBLOCK); // throw away all until ';;'
 
 }
/* This is a separate definition to allow for future expansion */

citation_text:
//    (text | INTLIT)* (COLON (text)* | (INTLIT MINUS INTLIT DOT) | DOT)*  //ENDBLOCK
      (text | INTLIT)* (COLON (text)* (MINUS INTLIT)? (text)* (DOT)?)* 
      					 
	;	

citation_type :
	| /* empty */
	| "SUPPORT"
	| "REFUTE"
	;
	  
/* May require special processing to handle both list and text versions */

links_slot:
	|  /* empty */
	| LINKS^ COLON 
	(
		| /* empty */
		| HTTP^ NOT_COMMENT (text)*
	
		| (SINGLE_QUOTE (text | DOT| INTLIT)* SINGLE_QUOTE (SEMI)?)*           /* any string of characters enclosed in single quotes (' , ASCII 44) without ";;" */
		
		 
	
	
	) 
	
	ENDBLOCK
	;
exception catch [NoViableAltException nv]{
  reportError(nv);
  consumeUntil(ENDBLOCK); // throw away all until ';;'
  consume();
 }
//link_body:
//	(SINGLE_QUOTE (text | DOT| INTLIT)* SINGLE_QUOTE (SEMI)?)*
//	| HTTP COLON "//" (text)*
//	;
								/* compatibility */



type_slot
	: TYPE^ COLON type_code ENDBLOCK
	;

type_code
	: "data-driven" | "data_driven" 
	;

data_slot
	: 
	//"data"^ COLON! (data_assignment SEMI!)* ENDBLOCK
	DATA^ COLON (data_statement)* ENDBLOCK
	;

/*
data_block
	: 
	(
	data_statement 
	| data_comment
	) 
	;
*/
data_statement
	: 
    ( (data_if_statement) 
	| (data_assignment SEMI!)
	| data_elseif
	)
//	| "FOR" identifier "IN" expr "DO" data_block SEMI "ENDDO"
//	| "WHILE" expr "DO" data_block SEMI "ENDDO"
	;

data_if_statement
	:IF^ data_if_then_else2
	;
data_if_then_else2:
     (
     	expr
     	| (LPAREN!) expr (RPAREN!) 
     )
     THEN data_statement
    ;

data_elseif
	:
	 ELSE^ 
	 | ELSEIF^ data_if_then_else2
	 | 	  (ENDIF^)

	;
	

data_comment
	:	
	 (COMMENT)
	| (ML_COMMENT)
	|
	;

data_assignment
	: 
	identifier_becomes   // |( (LPAREN)? data_var_list (RPAREN)?  )
	(  
	  //data_assign_phrase 
		(READ^) 
			( 
			 //read_phrase
			   ((of_read_func_op) | (from_of_func_op (INTLIT /*FROM*/)?))?  
			     (
			      //read_where
				     mapping_factor ( (where it! occur!)?  (temporal_comp_op  | NOT temporal_comp_op ) )? // | range_comp_op | NOT range_comp_op)
				    | LPAREN! (mapping_factor (where it occur (temporal_comp_op  | NOT temporal_comp_op ))?) RPAREN!
			     )
			)
			
			
	//	| "MLM" <term>
	//	| "MLM" <term> FROM "INSTITUTION" <string>
	//	| "MLM" "MLM_SELF"
	//	| "INTERFACE" <mapping_factor>
		| (EVENT^) mapping_factor //endassignment 
	//	| "MESSAGE" <mapping_factor>
	//	| "DESTINATION" <mapping_factor>
	//	| "ARGUMENT"
	//	| "OBJECT" <object_definition>
		| (CALL^) call_phrase
	//	| <new_object_phrase>
		| expr
	  
	  
	)
	| /* empty */
    
//	| time_becomes expr
//	| LPAREN data_var_list RPAREN BECOMES "READ" "AS" identifier read_phrase
//	| "LET" LPAREN data_var_list RPAREN "BE" "READ" 
//                 "AS" identifier read_phrase
//   | LPAREN data_var_list RPAREN BECOMES "ARGUMENT"
//	| "LET" LPAREN data_var_list RPAREN "BE" "ARGUMENT"
	
	;

endassignment
	:
	  (SEMI) => SEMI
	| ()
	;

endblock:
	(SEMI SEMI) => ENDBLOCK
	| ()
	;

identifier_becomes
	:
	identifier_or_object_ref BECOMES!
	| LET! (LPAREN!)? ID (COMMA ID)* (RPAREN!)? BE!
	| NOW BECOMES
	;

identifier_or_object_ref
	:
	ID (DOT ID)*

	;

//data_assign_phrase
//	:(
//		READ read_phrase
	//	| "MLM" <term>
	//	| "MLM" <term> FROM "INSTITUTION" <string>
	//	| "MLM" "MLM_SELF"
	//	| "INTERFACE" <mapping_factor>
	//	| ("EVENT" | "Event"| "event") mapping_factor //endassignment 
	//	| "MESSAGE" <mapping_factor>
	//	| "DESTINATION" <mapping_factor>
	//	| "ARGUMENT"
	//	| "OBJECT" <object_definition>
	//	| <call_phrase>
	//	| <new_object_phrase>
	//	| <expr>
		
//	) 
//	;


data_var_list
	: 
	|
	ID (COMMA ID)*
	;

read_phrase :
     read_where
 //   | of_read_func_op read_where
	| of_read_func_op read_where
	 from_of_func_op read_where 
//	| from_of_func_op ("OF" | "Of") read_where
	| from_of_func_op INTLIT (FROM) read_where
	;


read_where :
	  (
	  	mapping_factor ( (where it occur)?  (temporal_comp_op  | NOT temporal_comp_op ) )? // | range_comp_op |  range_comp_op)
	  | LPAREN! (mapping_factor (where it occur (temporal_comp_op  | NOT temporal_comp_op ))?) RPAREN!
      ) //endassignment
	 ;

mapping_factor :
	  ARDEN_CURLY_BRACKETS^
	;

time_value
	: NOW
	| iso_date_time
	;
/*********************************OPERATORS***************************************************************/
in_comp_op
	: IN
	;

of_read_func_op :
	 AVERAGE
	| AVG
	| COUNT
	| (EXIST^)
	| EXISTS
	| SUM
	| MEDIAN
	;

from_of_func_op : 
	  (MINIMUM	| MIN)
	| (MAXIMUM	| MAX)
	| (LAST^ ) 
	| (FIRST)
	| (EARLIEST)
	| (LATEST)
	|
	;

unary_comp_op :
	  "PRESENT"
	| "NULL"
	| "BOOLEAN"
	| "NUMBER"
	| TIME
	| "DURATION"
	| "STRING"
	| "LIST"
	| "OBJECT"
	| ID	 // names an object i.e. left side of OBJECT statement
	;

binary_comp_op :
	  LESS^ THAN
	| GREATER^ THAN
	| GREATER^ THAN OR EQUAL
	| LESS^ THAN OR EQUAL
	| IN^
	;


duration_op
	: YEAR | YEARS
	| MONTH | MONTHS
	| WEEK | WEEKS
	| DAY | DAYS
	| "hour"	| "hours"
	| "minute"	| "minutes"
	| "second"	| "seconds"
 	;

temporal_comp_op
	: WITHIN (the!)? PAST expr_string
	| AFTER expr_string
	| BEFORE expr_string
	;
/************************************************************************************************/
where
	:(WHERE^)
	;

it:
	(IT! | THEY!)
	;

/****** comparison synonyms ******/
is : //"IS" |"is" | "ARE" | "are" | "WAS" | "was" | "WERE" | "were"
		IS | ARE | WERE | WAS					

	;

occur
	: ("OCCUR"! | "Occur"! | "occur"!)
	| ("OCCURS"! | "Occurs"! | "occurs"! )
	| ("OCCURRED"! | "Occurred"! | "Occurred"! )
	;

the
	: THE
	;

priority_slot :
	|  /* empty */
	| PRIORITY^ COLON INTLIT ENDBLOCK
	;

exception catch [MismatchedTokenException mv]{
  reportError(mv); System.err.println("***Rule Priority NOT SET***");
  consumeUntil(ENDBLOCK); // throw away all until ';;'
  consume();
 }

evoke_slot :
	  "evoke" COLON (evoke_statement) ENDBLOCK
	  ;
	  
evoke_statement:
	| /*empty */
	| event_or
//	| evoke_time
//	| qualified_evoke_cycle
	| "CALL"
	;

event_or 
	: event_any (OR event_any)*
	;

event_any
	: ANY LPAREN event_list RPAREN
//	| "ANY" "OF" LPAREN event_list RPAREN
//	| "ANY" ID
//	| "ANY" "OF" ID
	| event_factor
	;

event_list
	:event_or (COMMA event_or)
	;

event_factor :
	  LPAREN event_or RPAREN
	| ID
	;
/*
evoke_time :
	  evoke_duration "AFTER" evoke_time
	| TIME event_any
	| "TIME" "OF" event_any
	| iso_date_time
	| iso_date
	;

qualified_evoke_cycle :
	  simple_evoke_cycle
	| simple_evoke_cycle "UNTIL" expr
	;

simple_evoke_cycle :
	  "EVERY" evoke_duration "FOR" evoke_duration "STARTING" evoke_time
	  ;

evoke_duration :
	  number duration_op
	;
*/


logic_slot:
	  "logic"^ COLON (logic_statement SEMI!|if_statement|logic_elseif )* ENDBLOCK
	  ;


logic_statement:
	 (conclude_statement
	 | logic_assignment
	 | CALL^ call_phrase
	 
	 )*
	 
	;

	
//	logic_assignment ("then" logic_assignment)* logic_elseif
	 
//	| /* empty */
//	| ("IF" | "if") logic_if_then_else2
//	| "FOR" identifier "IN" expr "DO" logic_block ";" "ENDDO"
//	| "WHILE" expr "DO" logic_block ";" "ENDDO"
//	| ("CONCLUDE" | "conclude") boolean_value
//	;

//logic_if_then_else2 :
//	  expr "then" logic_statement SEMI logic_elseif SEMI
//	;

logic_expr
	: (the)? from_of_func_op expr_factor (is)? binary_comp_op (the)? (expr_factor | from_of_func_op expr_factor)
	;

logic_condition
	:(
		 (AND)
		| (OR)
		| (NOT) 
	) 
	; 

simple_comp_op :

	  (EQUALS^)	| ("EQ")
	| LT^	| "LT"^
	| GT^	| "GT"^
	| LTE^	| "LE"^
	| GTE^	| "GE"^
	| NE^	| "NE"^
	;

main_comp_op
	:
	 binary_comp_op expr_string
	;
  
logic_elseif :
	 ELSE^ 
	 | ELSEIF^ logic_if_then_else2
	 | 	  (ENDIF^)

	;

if_statement:	
//     IF (expr_factor (logic_condition logic_expr)*) "then" 
 //    IF (expr (logic_condition logic_expr)*) "then" 
     IF^ logic_if_then_else2
     ;
  
logic_if_then_else2:
     (
         //(LPAREN!)? expr (RPAREN!)? 
         expr
     	
     )
     THEN //( (identifier_becomes expr)? | (CONCLUDE^ boolean_value))
    ;

conclude_statement
	:
	 (CONCLUDE^) boolean_value
	;
 
 call_phrase
	: ID (WITH expr)?
	;

logic_assignment
:
	 (ACTION_OP^)? identifier_or_object_ref (ACTION_OP)? (BECOMES! | EQUALS!) (expr | (CALL^) call_phrase)
//	 |  identifier_becomes (expr | (CALL^) call_phrase)
	;
		
//	 identifier_becomes expr
//	| time_becomes expr
//	| identifier_becomes call_phrase
//	| "(" data_var_list ")" BECOMES call_phrase
//	| "LET" "(" <data_var_list> ")" "BE" <call_phrase>
//	;

action_slot:
	  "action"^ COLON (action_statement SEMI!|if_statement|logic_elseif)* ENDBLOCK
	;
  
action_statement:
	(IF^) action_if_then_else2
	|
	(WRITE^) 
	(
	   	(LPAREN!)? ( (ACTION_OP expr_factor)* | expr ) (RPAREN!)? 
     	
		//expr_factor 
	)  /*(ACTION_OP^ expr_factor)* */ ((AT) ID)? 
	| /* Empty*/
	| (identifier_becomes)? (
							(CALL^) call_phrase
	)
	;

action_if_then_else2:
     (
         //(LPAREN!)? expr (RPAREN!)? 
         expr
     	
     )
     THEN 
    ;

urgency_slot:
	  /* empty */
	| "urgency" COLON urgency_val ENDBLOCK
	;
urgency_val:
	  INTLIT
	  |
	
	;
age_min_slot:
	|  /* empty */
	| AGE_MIN^ COLON INTLIT age_code ENDBLOCK
	;
age_max_slot:
	|  /* empty */
	| AGE_MAX^ COLON INTLIT age_code ENDBLOCK
	;

age_code
	: "days" | "weeks" | "months" | "years"
	;

/****** expressions ******/
expr :
	  expr_sort (COMMA! expr_sort)*
	  ;

expr_sort :
	  expr_where (("MERGE" | ("SORT" (sort_option)? )) expr_where)*
/*
	| expr_where "MERGE" expr_sort
	| "SORT"  expr_sort
	| "SORT"  sort_option  expr_sort
*/	;

sort_option :
	  TIME
	| "DATA"
	;

expr_where :
	expr_range (WHERE expr_range)*
	|
	
/*	  expr_range
	| expr_range "WHERE" expr_range
*/	;

expr_range :
	expr_or ("SEQTO" expr_or)*
	
/*	  expr_or
	| expr_or "SEQTO" expr_or
*/
	;

expr_or
	: expr_and (OR expr_and)*
	;

expr_and
	:
	 expr_not (AND expr_not)*
	;

expr_not
	: expr_comparison  // (NOT expr_comparison)*
	| NOT expr_comparison
	;

expr_comparison :
	  expr_string ((simple_comp_op expr_string) | (is! main_comp_op) )?
//	| expr_find_string
//	| expr_string simple_comp_op expr_string
//	| expr_string is main_comp_op
//	| expr_string is NOT main_comp_op
//	| expr_string in_comp_op
//	| expr_string NOT in_comp_op
//	| expr_string occur temporal_comp_op
//	| expr_string occur NOT temporal_comp_op
//	| expr_string occur range_comp_operator
//	| expr_string occur NOT range_comp_operator
//	| expr_string "MATCHES" "PATTERN" expr_string
	;

/*exp_find_string :
	  "FIND" expr_string "IN" "STRING" expr_string string_search_start
	| "FIND" expr_string "STRING" expr_string string_search_start
	;
*/


/**********************************************************************************/
expr_string
	: expr_plus (ACTION_OP expr_plus)*
//	| expr_string "||" expr_plus
	;
	
expr_plus
	:  expr_times ( ("+" | "-") expr_times)*
/*	
 //	expr_times
//	| expr_plus "+" expr_times
//	| expr_plus "-" expr_times
	| "+" expr_times
	| "-" expr_times
*/
	;

expr_times
	: expr_power (("*"| "/") expr_times)*
	
/*	 expr_power
//	| expr_times "*" expr_power
//	| expr_times "/" expr_power
*/
	;

//expr_power
//	: 
//	  expr_before
//	| expr_function "**" expr_function
//	;


//expr_before 
  expr_power
	: 	
	expr_duration ((BEFORE | AFTER | FROM) expr_duration)* (AGO)?
/*	expr_ago
	| expr_duration "before" expr_ago
	| expr_duration "after" expr_ago
	| expr_duration FROM expr_ago
*/
	;

//expr_ago
//	: expr_duration ("ago")
//	;

expr_duration
	:  expr_function (duration_op)?
	;

expr_function
//expr_duration
	:
	expr_factor
    | (the!)?
     (
     	from_of_func_op (OF!)? expr_factor //(is)? binary_comp_op (the)? (expr_factor | from_of_func_op expr_factor)
    	| of_func_op (OF!)? expr_factor
      )
	//	("as" as_func_op ) 
	;

expr_factor
	:  expr_factor_atom (DOT expr_factor_atom)*
	;

expr_factor_atom
	: ID 
	| LPAREN! 
		//	( ID | ( (LPAREN expr_factor_atom RPAREN) (COMMA (LPAREN ID RPAREN))*) ) 
		expr		
	  RPAREN!
//	| INTLIT
	| (INTLIT  ((MINUS^ INTLIT)+ )? )
//	| time_value
	| boolean_value
	| STRING_LITERAL
	| TERM_LITERAL
	| NULL
	
	
	
	;

as_func_op
	:
	"NUMBER"
	;

boolean_value :
	 
	 ( TRUE^ )
	| ( FALSE^ )
	;

of_func_op:
	of_read_func_op
	| of_noread_func_op
;


of_noread_func_op:
	  TIME
	| ANY^
;


/*************************************************************************************/
class ArdenBaseTreeParser extends TreeParser;

options {
	importVocab=ArdenBaseParser;
}

data [MLMObject obj] returns [String s=""]
{String a,b;}
: //#(COLON {System.err.println("\n"); System.err.println("-------Starting Data--------");} 
	 (
	  {System.err.println("-----------Starting Read -------");obj.InitEvaluateList("data",null);}s=readAST[obj, s]  {obj.AddToEvaluateList("data",s);System.err.println("\n");System.err.println("-----------End Read -------");}
	 |{System.err.println("----------------Starting Event-------");} eventAST {System.err.println("\n");System.err.println("-----------End Event -------");}
 	 |{System.err.println("----------------Starting Data If-------");} dataIfAST[obj] {System.err.println("\n");System.err.println("-----------End Data If -------");}
 	 | #(ENDIF {System.err.println("ENDIF FOUND");a = "ENDIF"; obj.AddToEvaluateList("data",a);} )
  	 |{System.err.println("----------------Starting Data Else If-------");} data_elseifAST[obj] {System.err.println("\n");System.err.println("-----------End Data Else If -------");}
  	 | {System.err.println("-----------Starting Data CALL -------"); a = "" ;} callSectionAST[obj, a,"data"]  {System.err.println("\n");System.err.println("-----------End CALL -------");} 
	  
 //  |{System.err.println("----------------Starting from func op-------");} from_of_func_opAST [obj] {System.err.println("----------------End from func op-------");}
 //	 |{System.err.println("----------------Starting read func op-------");} of_read_func_opAST [obj] {System.err.println("----------------End read func op-------");}
 
	 )* 
  (ENDBLOCK){System.err.println("\n");System.err.println("-----------End Data -------");}
  //)

;



dataIfAST [MLMObject obj] returns [String s=""]
{String a, b;}
:  (
    #(IF { obj.InitEvaluateList("data","IF");obj.AddToEvaluateList("data","IF");} s=exprAST["data",obj] THEN {obj.AddToEvaluateList("data","THEN");}
    (
    {System.err.println("-----------Starting Read -------");}s=readAST[obj, s]  {obj.AddToEvaluateList("data",s);System.err.println("\n");System.err.println("-----------End Read -------");}
	 |{System.err.println("----------------Starting Event-------");} eventAST {System.err.println("\n");System.err.println("-----------End Event -------");}
 	 |{System.err.println("-----------Starting Data CALL -------"); a = "" ;} callSectionAST[obj, a,"data"]  {System.err.println("\n");System.err.println("-----------End CALL -------");} 
  	)*
  	 ) 
   )
   ;


data_elseifAST [MLMObject obj] returns [String s=""]
{String a,b;}
: (
     (
     	 #(ELSEIF { obj.AddToEvaluateList("data","ELSEIF");} s=exprAST["data",obj] THEN {obj.AddToEvaluateList("data","THEN");}
     (	
     {System.err.println("-----------Starting Read -------");}s=readAST[obj, s]  {obj.AddToEvaluateList("data",s);System.err.println("\n");System.err.println("-----------End Read -------");}
	 |{System.err.println("----------------Starting Event-------");} eventAST {System.err.println("\n");System.err.println("-----------End Event -------");}
 	 |{System.err.println("-----------Starting Data CALL -------"); a = "" ;} callSectionAST[obj, a,"data"]  {System.err.println("\n");System.err.println("-----------End CALL -------");} 
     )
  	 )
       | #(ELSE { obj.AddToEvaluateList("data","ELSE");} 
      (
      {System.err.println("-----------Starting Read -------");}s=readAST[obj, s]  {obj.AddToEvaluateList("data",s);System.err.println("\n");System.err.println("-----------End Read -------");}
	 |{System.err.println("----------------Starting Event-------");} eventAST {System.err.println("\n");System.err.println("-----------End Event -------");}
 	 |{System.err.println("-----------Starting Data CALL -------"); a = "" ;} callSectionAST[obj, a,"data"]  {System.err.println("\n");System.err.println("-----------End CALL -------");} 
  	 )
  	  )
       | #(ENDIF {System.err.println("ENDIF FOUND");} )
     )   
   )
;

where_it_occurredAST [MLMObject obj, String key] returns [String s=""]
{String a,b, ret_val="";}
:
	(WITHIN {obj.setWhere("withinPreceding", key);}
	    (PAST) (m:INTLIT n:duration_op) {obj.setDuration("past",m.getText(),n.getText(),key); System.err.println("Duration Clause - " + m.getText() + " " + n.getText());} 
		| a = exprAST["data",obj] TO b = exprAST["data",obj]
	)
	|(AFTER) (i:ID){System.err.println("Variable = " + i.getText());}
;


readAST [MLMObject obj, String instr] returns [String s=""]
{String a="",b="", ret_val="";}
: (
  #(READ  a=readAST[obj, instr] b=readAST[obj, a]) {s += a;}

  //  ( of_read_func_opAST [obj] 
  //   | from_of_func_opAST [obj] 
  //  )
  
   
	 // Following are from_of_func_op
	  | ((LAST | LATEST) (k:INTLIT /*FROM*/)? 
	  {s+=b;obj.setReadType("last"); obj.setHowMany("1");
	  							if(k != null) {
	   								obj.setHowMany(k.getText());
	   								System.err.println("ReadType = Last " + "How many? " + k.getText());
	  							}
	  							 else {
		  							 System.err.println("ReadType = Last " + "How many? 1" );	
	  							 }
	  							 
	  							}
	  
	  
	   b=readAST[obj, instr]) 
	  
	  | ((FIRST | EARLIEST) (x:INTLIT /*FROM*/)?
	  {s+=b;obj.setReadType("first"); obj.setHowMany("1");
	  								if(x != null) {
	   									obj.setHowMany(x.getText());
	   									System.err.println("ReadType = First " + "How many? " + x.getText());
	  								}
	  							 else {
		  							 System.err.println("ReadType = First " + "How many? 1" );	
	  							 }
	  							 
	  							}
	  
	  
	  b=readAST[obj, instr]) 
	  							
	  				
	  | ((MAXIMUM | MAX)( (y:INTLIT /*FROM*/)? 
	  {s+=b;obj.setReadType("max"); obj.setHowMany("1");
	  							if(y != null) {
	   									obj.setHowMany(y.getText());
	   									System.err.println("ReadType = Maximum " + "How many? " + y.getText());
	  								}
	  							 else {
		  							 System.err.println("ReadType = Maximum " + "How many? 1" );	
	  							 }
	  							 
	  							}
	  b=readAST[obj, instr]) 
	  					 |
	  					 ((intlit:INTLIT dop:duration_op) {System.err.println("Duration Clause - " + intlit.getText() + " " + dop.getText());})
	  					)
	  							
	  | ((MINIMUM | MIN) (z:INTLIT /*FROM*/)?
	  {s+=b;obj.setReadType("min"); obj.setHowMany("1");
	  							if(z != null) {
		   								obj.setHowMany(z.getText());
		   								System.err.println("ReadType = Minimum " + "How many? " + z.getText());
	  								}
	  							 else {
		  							 System.err.println("ReadType = Min " + "How many? 1" );	
	  							 }
	  							 
	  							}
	  							
	
	   b=readAST[obj, instr]) 	// end of from_of_func_op
		
  
	  	
	  // Following are of_read_func_op
	  			
	  
	  | ((EXIST | EXISTS)
	  {s+=b;obj.setReadType("exist"); obj.setHowMany("1");System.err.println("ReadType = Exist");}
	   b=readAST[obj, instr]) 
	  
	  | ((AVERAGE | AVG) b=readAST[obj, instr]) 
	  {s+=b;obj.setReadType("average");System.err.println("ReadType = Average");}
	          

	  | (COUNT b=readAST[obj, instr]) 
	  {s+=b;obj.setReadType("count");System.err.println("ReadType = Count");}					

	  | (SUM b=readAST[obj, instr]) {s+=b;System.err.println("ReadType = Sum");}						// To DO
	  | (MEDIAN b=readAST[obj, instr]) {s+=b;System.err.println("ReadType = Median");}					// To DO
	  
	  // End of of_read_func_op
	 	 	  
  |(
		(j:ARDEN_CURLY_BRACKETS /*b=readAST*/) {/*s=b;*/System.err.println("Fetch this data - " + j.getText());
		  	 										s = j.getText(); 
		  	 										obj.AddConcept(s);
		  	 									}
		
		   	      (WHERE {System.err.println("Where=TRUE");} where_it_occurredAST[obj, instr] )?
	  	 
	  
	  | i:ID {System.err.println("Variable = " + i.getText()); a= i.getText(); s=a; obj.SetConceptVar(a);}
	  | t:TRUE {s="true";
	            
	           }
	  | f:FALSE {s="false";
	 			 
	 			}
  ) )
;



from_of_func_opAST [MLMObject obj] returns [String s=""]
{String a,b, ret_val="";}
:
(
 // Following are from_of_func_op
  | ((LAST | LATEST) (k:INTLIT FROM)? ) {if(k != null) {
   								System.err.println("ReadType = Last " + "How many? " + k.getText());
  								}
  							 else {
	  							 System.err.println("ReadType = Last " + "How many? 1" );	
  							 }
  							 
  							}
  
  | ((FIRST | EARLIEST) (x:INTLIT FROM)? ) {if(x != null) {
   								System.err.println("ReadType = First " + "How many? " + x.getText());
  								}
  							 else {
	  							 System.err.println("ReadType = First " + "How many? 1" );	
  							 }
  							 
  							}
  							
  				
  | ((MAXIMUM | MAX) (y:INTLIT FROM)? ) {if(y != null) {
   								System.err.println("ReadType = Maximum " + "How many? " + y.getText());
  								}
  							 else {
	  							 System.err.println("ReadType = Maximum " + "How many? 1" );	
  							 }
  							 
  							}
  							
  | ((MINIMUM | MIN) (z:INTLIT FROM)? ) {if(z != null) {
   								System.err.println("ReadType = Minimum " + "How many? " + z.getText());
  								}
  							 else {
	  							 System.err.println("ReadType = Min " + "How many? 1" );	
  							 }
  							 
  							}
  							
	// end of from_of_func_op
)	
	;
	
of_read_func_opAST [MLMObject obj] returns [String s=""]
{String a,b, ret_val="";}
	:
(	
  // Following are of_read_func_op
  			
  
  | ((EXIST | EXISTS)) {obj.AddToEvaluateList("data","EXIST");System.err.println("ReadType = Exist");}
  
  | ((AVERAGE | AVG) ) {obj.AddToEvaluateList("data","AVG");System.err.println("ReadType = Average");}
  | (COUNT) {obj.AddToEvaluateList("data","COUNT");System.err.println("ReadType = Count");}
  | (SUM) {obj.AddToEvaluateList("data","SUM");System.err.println("ReadType = Sum");}
  | (MEDIAN) {obj.AddToEvaluateList("data","MEDIAN");System.err.println("ReadType = Median");}
  
  // End of of_read_func_op
  
);

of_noread_func_opAST [MLMObject obj] returns [String s=""]
{String a,b, ret_val="";}
	:
(	
  // Following are of_read_func_op
  			
  
  | (ANY) {obj.AddToEvaluateList("data","ANY");System.err.println("Any of");}
  
  
  // End of of_noread_func_op
  
);

duration_op 
	:
	 YEAR | YEARS
	| MONTH | MONTHS
	| WEEK | WEEKS
	| DAY | DAYS
	| "hour"	| "hours"
	| "minute"	| "minutes"
	| "second"	| "seconds"
 	; 
	

eventAST returns [String s=""]
{String a,b;}
: (
    #(EVENT b=eventAST) 
    | i:ID {System.err.println("Event Variable = " + i.getText());}
  )
 
;


/********************LOGIC***********************************/
logic [MLMObject obj] returns [String s=""]
{String a,b; Integer i = 1;}
: //#(COLON {System.err.println("\n"); System.err.println("-------Starting LOGIC--------");} 
	
	 (
	   {System.err.println("-----------Starting IF -------");} a=ifAST[obj]
			   (	{System.err.println("-----------Starting Logic Assignment -------");} logicAssignmentAST[obj, a]  { System.err.println("\n");System.err.println("-----------End logic assignment -------");} 
		
			   )?
			   (
			   		{System.err.println("-----------Starting CONCLUDE -------"); } (concludeAST[obj, a])? {System.err.println("\n");System.err.println("-----------End CONCLUDE -------");}
			   )?
			    (
			   		{System.err.println("-----------Starting CALL -------"); } (callSectionAST[obj, a,"logic"])? {System.err.println("\n");System.err.println("-----------End CALL -------");}
			   )?   
	   {System.err.println("\n");System.err.println("-----------End IF -------");} 
	   
	  | {System.err.println("-----------Starting ELSE - ELSEIF -------");} a=logic_elseifAST[obj, i] 
			  (	 {System.err.println("-----------Starting Logic Assignment -------");} logicAssignmentAST[obj, a]  {System.err.println("\n");System.err.println("-----------End logic assignment -------");} 
					      
			  )?
			  (
			  	{System.err.println("-----------Starting CONCLUDE -------");} (concludeAST[obj, a])? {System.err.println("\n");System.err.println("-----------End CONCLUDE -------");}
			  )?	
			  (
			   		{System.err.println("-----------Starting CALL -------"); } (callSectionAST[obj, a,"logic"])? {System.err.println("\n");System.err.println("-----------End CALL -------");}
			   )?  
	   {System.err.println("\n");System.err.println("-----------End ELSE- ELSEIF -------");}
	  
	  | #(ENDIF {System.err.println("ENDIF FOUND");a = "ENDIF"; obj.AddToEvaluateList("logic",a);} )
	         
	  | {System.err.println("-----------Starting CONCLUDE -------");obj.InitEvaluateList("logic",null); a = "Conclude_" + Integer.toString(i);} concludeAST[obj, a]  {System.err.println("\n");System.err.println("-----------End CONCLUDE -------");}
	  | {System.err.println("-----------Starting CALL -------");obj.InitEvaluateList("logic",null); a = "" ;} callSectionAST[obj, a,"logic"]  {System.err.println("\n");System.err.println("-----------End CALL -------");}

	{i++;} )* 
 	
  (ENDBLOCK){System.err.println("\n");System.err.println("-----------End LOGIC -------");}
  //)
;

callSectionAST [MLMObject obj, String key,String section] returns [String s=""]
{String a="",b="";}
: 
(
    #(CALL (a = callStringAST [section,obj, key]) (b = callStringAST [section,obj, a])?
	   
	        
	   	    {
	   	    	
	   	    	if(b.equals(""))
	   	    	{
   	    			b = a;
   	    			a = "";
   	    		}
		   	    obj.AddToEvaluateList(section,"call");obj.addCall(section,a,b);
			}
	        ( (WITH b = callStringAST [section,obj, a])
			   ( a = callStringAST [section,obj, a] )*
			  ) ?
	     
	 ) 
  )
;



ifAST [MLMObject obj] returns [String s=""]
{String a,b;}
: (
    #(IF {obj.InitEvaluateList("logic","IF"); obj.AddToEvaluateList("logic","IF");} s=exprAST["logic",obj] 
     THEN {obj.AddToEvaluateList("logic","THEN");}) 
   )
   ;

logicAssignmentAST [MLMObject obj, String key] returns [String s=""]

:
     exprStringAST["logic",obj, "CTX"/*key Do not use key- depends on context so CTX*/] {obj.AddToEvaluateList("logic","Logic_Assignment");}
	   
     /* ( 
      	(strlit: STRING_LITERAL) 
      	(ACTION_OP 
		id: ID {a = id.getText(); } 
		ACTION_OP 
		str: STRING_LITERAL {b = str.getText(); 
				obj.addLogicAssignment(a, b);}
		
	  	)
      )?
      */
;
expr_comparisonAST [String section,MLMObject obj] returns [String s=""]
{String a,b;}
	:
(	
	(a = exprStringAST[section,obj, ""] {s=a;}((simple_comp_opAST[section,obj, a] | binary_comp_opAST[section,obj, a]) (b = exprStringAST[section,obj, a] )*)? )
//	| expr_functionAST[obj] (a = exprStringAST[section,obj, ""] {s=a;}( (COMMA a = exprStringAST[section,obj, ""] {s=a;})* (binary_comp_opAST[section,obj, a]) b = exprStringAST[section,obj, a] {}) )
	| expr_functionAST[obj] (a = exprStringAST[section,obj, "notnull"] {s=a;}( (binary_comp_opAST[section,obj, a]) b = exprStringAST[section,obj, a] (COMMA exprStringAST[section,obj, a] {s=a;})*  {})? )
)	
	;

expr_notAST [String section,MLMObject obj] returns [String s=""]
	:
	 expr_comparisonAST[section,obj] //(NOT {obj.AddToEvaluateList(section,"NOT");} expr_comparisonAST[section,obj] )*
	 | (NOT {obj.AddToEvaluateList(section,"NOT");} expr_comparisonAST[section,obj] )
	;

expr_andAST [String section,MLMObject obj] returns [String s=""]
	: expr_notAST[section,obj] (AND {obj.AddToEvaluateList(section,"AND");} expr_notAST[section,obj])*
	;

expr_orAST [String section,MLMObject obj] returns [String s=""]
	: expr_andAST[section,obj] (OR {obj.AddToEvaluateList(section,"OR");} expr_andAST[section,obj])*
	
	;


exprAST [String section,MLMObject obj] returns [String s=""]
{String a,b;}
:
(  expr_orAST[section,obj]

);

expr_functionAST [MLMObject obj] returns [String s=""]
:
     (
    	from_of_func_opAST[obj] (OF)? 
    	| of_read_func_opAST[obj] (OF)? 
    	| of_noread_func_opAST[obj] (OF)?
     )

	;

callStringAST [String section, MLMObject obj, String instr] returns [String s=""]
{String a="",b="";}
:
(
 	#(ift:ID 
			      { a = ift.getText(); System.err.println("text = " + a); 
			        s=a;
			      }  
	   )  

	| (
    	  #(TRUE {})
    	| #(FALSE {})
      )
	  
	| (val:INTLIT
	    { 
		  b = val.getText();
		  obj.addParameter(section,b);
		}
		(
		    (DOT val2: INTLIT
			{
				a = val2.getText();	
				obj.addParameter(section,a);
			}
			)?
		)
	  )
	| (strlit: STRING_LITERAL
		{
			b = strlit.getText();
			obj.addParameter(section,b);
		}
	  )
	  
	| (termlit: TERM_LITERAL
		{
			b = termlit.getText();					
			obj.addParameter(section,b);
		}
	  )
	|(nulllit: NULL
		{
			b = nulllit.getText();					
			obj.addParameter(section,b);
		}
	  )
	  
	| #(ACTION_OP 
		id: ID {a = id.getText(); } 
		ACTION_OP 
		str: STRING_LITERAL {b = str.getText(); 
		}
		
	  )		      
  )
;

exprStringAST [String section,MLMObject obj, String instr] returns [String s=""]
{String a="",b="";}
:
(
 	#(ift:ID 
			      { a = ift.getText(); System.err.println("text = " + a); 
			        if(instr.equals("")) {
			        		obj.AddToEvaluateList(section,a);
			        		s= a;
			        		
				      //  	obj.RetrieveConcept(a); 
			        }
			        else if(instr.startsWith("__Temp__")){
			        	obj.SetAnswerListKey(section,a);  // adds key only if a previously formed list found...
			        	obj.AddToEvaluateList(section,a);// Add key to evaluate list if none found before
			        	
			        }
			        else if(instr.equals("CTX")) {
			        	s=a;
			        	// do nothing for now
			        }
			        else if(instr.equals("notnull")) {
			        	obj.AddToEvaluateList(section,a);
			        	if(obj.GetMLMObjectElement(a) == null) {
			        		s="Func_1";  // Func like Exist..          
			        	}
			        	else {
			        		s=a;
			        	}
			        	
			        }
			        else { // if instr is not empty then we are evaluating RHS of an equation, it can be a non string literal
			        	obj.SetAnswer(section,a, instr);					
			        	s=a;
			        }
			        
			      }
			    
	   )   		      
	      
    | (
    	  #(TRUE {obj.SetAnswer(section,true,instr);})
    	| #(FALSE {obj.SetAnswer(section,false,instr);})
      )
	  
	| (val:INTLIT
	    { 
		  b = val.getText();
		  Integer i = Integer.parseInt(b);
		  Double idbl = null;
		}
		(
		    (DOT val2: INTLIT
			{
				a = val2.getText();
				String dbl = b + "." + a;
				idbl = Double.parseDouble(dbl);
		  	}
			)?
			
		)
		{
			if(idbl == null) 
			{
				obj.SetAnswer(section,i,instr);
			}
			else
			{
				obj.SetAnswer(section,idbl,instr);		
			}
		}
	  )
	| (strlit: STRING_LITERAL
		{
			b = strlit.getText();
			obj.SetAnswer(section,b,instr);					
			
		}
	  )
	  
	| (termlit: TERM_LITERAL
		{
			if(instr.equals("")) {
				// LHS , example - 'ABC' in Variable
				b = termlit.getText();
				s = obj.SetAnswerList(section,b,instr);
			}
	        else if(instr.equals("CTX")) {
	        	s=a;
	        	// do nothing for now
	        }
	        else if(instr.equals("notnull")) {
	        
	        }
			else
			{
				b = termlit.getText();
				obj.SetAnswer(section,b,instr);					
			}
		}
	  )
	|(nulllit: NULL
		{
			b = nulllit.getText();
			obj.SetAnswer(section,null,instr);					
			
		}
	  )
	  
	| #(ACTION_OP 
		id: ID {a = id.getText(); } 
		ACTION_OP 
		str: STRING_LITERAL {b = str.getText(); 
				obj.addLogicAssignment(a, b);}
		
	  )

//	| ( // Empty as in else conclude
//		{ a = "tmp_01"; System.err.println("IF text = " + a); 
//			        if(instr.equals("")) {
//			        	 obj.AddToEvaluateList(a);
//			        	}
//			      //  	obj.RetrieveConcept(a); 
//			        s= a;
//        }
//	  )

      
  )
;

simple_comp_opAST [String section,MLMObject obj, String key] returns [String s=""]
{String a,b;}
:
   #(EQUALS {
   				System.err.println("Found = ");
   				 obj.addCompOperator(section,EQUALS, key);
   			}
   	 )
	|
	#(GTE {
   				System.err.println("Found >= ");
   				 obj.addCompOperator(section,GTE, key);
   			}
   	 )
   	 |
   	 #(GT {
   				System.err.println("Found > ");
   				 obj.addCompOperator(section,GT, key);
   			}
   	 )
   	 |
   	 #(LT {
   				System.err.println("Found < ");
   				 obj.addCompOperator(section,LT, key);
   			}
   	 )
   	 |
   	 #(LTE {
   				System.err.println("Found <= ");
   				 obj.addCompOperator(section,LTE, key);
   			}
   	 )
   	 #(NE {
   				System.err.println("Found <> ");
   				 obj.addCompOperator(section,NE, key);
   			}
   	 )
;
	
binary_comp_opAST [String section,MLMObject obj, String key] returns [String s=""]
{String a,b;}
:
   #(EQUALS {
   				System.err.println("Found = ");
   				 obj.addCompOperator(section,EQUALS, key);
   			}
   	 )
	|
   	 #(GREATER THAN {
   				System.err.println("Found > ");
   				 obj.addCompOperator(section,GT, key);
   			}
   			(OR EQUAL )? {
   			    System.err.println("Found >= ");
   				 obj.addCompOperator(section,GTE, key);
   			}
   	 )
   	 |
   	 #(LESS THAN {
   				System.err.println("Found < ");
   				 obj.addCompOperator(section,LT, key);
   			}
   	 )
   	 |
   	 #(LESS THAN OR EQUAL {
   				System.err.println("Found <= ");
   				 obj.addCompOperator(section,LTE, key);
   			}
   	 )
   	 |
   	 #(IN {
   				System.err.println("Found IN ");
   				 obj.addCompOperator(section,IN, key);
   			}
   	 )
;

	
//thenAST[MLMObject obj] returns [String s=""]
//{String a, b;}
//:
//   (
//    	#(THEN concludeAST[obj] { System.err.println("Evaluating expr ...\n");     }
//   		)
//    )
 
//;


concludeAST [MLMObject obj, String key] returns [String s=""]
{String a,b;}
: (
    #(CONCLUDE {
       			{
	    			a = "Conclude";
	    		//	key = a;
	    			if(key.startsWith("Conclude_")) { // Simply Conclude
	    				obj.AddToEvaluateList("logic",key); 
	    			}
	    			else {  // Associate with the Else before
	    				obj.AddToEvaluateList("logic",a); 
	    			}
	    		}
    			
    		   } 
      (   FALSE {System.err.println("***CONCLUDE FALSE " );
      				obj.addConcludeVal(false);} 
  	    | TRUE  {System.err.println("***CONCLUDE TRUE " );
  	    			obj.addConcludeVal(true);}  
  	  )
     ) 
  )
;

logic_elseifAST [MLMObject obj, Integer i] returns [String s=""]
{String a,b;}
: (
     (
     	 #(ELSEIF 
     	 { a = "ELSEIF"; System.err.println("ELSEIF" ); 
					        	 obj.AddToEvaluateList("logic",a);
		        }
     	      	 s=exprAST["logic",obj] THEN  {obj.AddToEvaluateList("logic","THEN");} )

		 | #(ELSE
		 
				{ a = "ELSE_"; s= a+ Integer.toString(i); System.err.println("ELSE" ); 
					        	 obj.AddToEvaluateList("logic",s);
		        }
		  )
 //      | #(ENDIF {System.err.println("ENDIF FOUND"); a = "ENDIF"; obj.AddToEvaluateList("logic",a);} )
     )   
   )
;

/***********************ACTION*******************************************/
action [MLMObject obj] returns [String s=""]
{String a,b;}
: //#(COLON {System.err.println("\n"); System.err.println("-------Starting Action--------");} 
	 (
	   {System.err.println("-----------Starting Write -------");} s = writeAST[obj] { System.err.println("\n");System.err.println("-----------End Write -------");}
	    | {System.err.println("-----------Starting CALL -------"); obj.InitEvaluateList("action",null); a = "" ;} callSectionAST[obj, a,"action"]  {System.err.println("\n");System.err.println("-----------End CALL -------");}
	 )* 
  (ENDBLOCK){System.err.println("\n");System.err.println("-----------End Action -------");}
  //)
;

writeAST [MLMObject obj] returns [String s=""]
{String a="",b="";}
: (
    #(WRITE 
       	
       ( 
       		(ACTION_OP id: ID {a = id.getText(); 
       							//b= obj.getUserVarVal(a);
       							b = "||" + a + "||";
       							s += b;}
       		ACTION_OP) 
           | (i:STRING_LITERAL  {s += i.getText();} /* {a = i.getText(); s += a.substring(1, a.length()-1); } */  )  /* get rid of "" sorrounding each string literal */
       )*
       {obj.addAction(s);}		
       (
          AT idat: ID {obj.setAt(idat.getText());} 		
       )?
       	
     ) 
    
      
     
  )
;

/*
actionExprAST [MLMObject obj] returns [String s=""]
{String a,b;}
: (

  	     ( 
  	      id: ID {a = id.getText(); s= obj.getUserVarVal(a);} 
		 )
		    
  
  )
 
;
*/

/***********************KNOWLEDGE*******************************************/
knowledge [MLMObject obj] returns [String s=""]
{String a="",b = "";}
: (
  #(KNOWLEDGE
    (
    	(
    	/*  #(TYPE
    	      COLON {s += " Type: "; }  b = textAST[obj] {obj.setType(b); s += b; s += "\n";} 
    	   )
    	 |
    	*/ #(DATA
    	       COLON {System.err.println("-----------Starting Data -------");} s = data[obj] {System.err.println("\n");System.err.println("-----------End Data -------");}
    	       
    	   )
    	 |#(PRIORITY 
    	      COLON {s += " Priority: "; }  b = doubleAST[obj] {obj.setPriority(b); s += b; s += "\n";} 
    	   )
    	 /*|#(EVOKE
    	        COLON {s += " Type: "; }  b = textAST[obj] {obj.setType(b); s += b; s += "\n";} 
    	   )
    	 */
    	 |
    	 #(LOGIC
    	       COLON {System.err.println("-----------Starting Logic -------");} s = logic[obj] {System.err.println("\n");System.err.println("-----------End Logic -------");}
    	    )
    	 |#(ACTION
    	       COLON {System.err.println("-----------Starting ACTION -------");} s = action[obj] {System.err.println("\n");System.err.println("-----------End Action -------");}
    	   )
    	 /*|#(URGENCY
    	        COLON {s += " Type: "; }  b = textAST[obj] {obj.setType(b); s += b; s += "\n";} 
    	   )
    	  */
    	  |#(AGE_MIN 
    	      COLON {s += " Age_Min: "; }  b = doubleAST[obj] {obj.setAgeMin(b); s += b; s += "\n";} 
    	   )
    	   |#(AGE_MAX 
    	      COLON {s += " Age_Max: "; }  b = doubleAST[obj] {obj.setAgeMax(b); s += b; s += "\n";} 
    	   )
		 | a = textAST[obj] {s += a;} ENDBLOCK {s += "\n";}     	
    	)
    
    )*
   )
   
  )
;



/***********************KNOWLEDGE TEXT- To return data, logic and action text to populate the DB *******************************************/
knowledge_text [MLMObject obj] returns [String s=""]
{String a="",b = "";}
: (
  #(KNOWLEDGE
    (
    	(
    	/*  #(TYPE
    	      COLON {s += " Type: "; }  b = textAST[obj] {obj.setType(b); s += b; s += "\n";} 
    	   )
    	 |
    	*/ #(DATA
    	       COLON  b = textAST[obj] {obj.setData(b); s += b; s += "\n";} 
    	       
    	   )
    	 |#(PRIORITY 
    	      COLON   b = doubleAST[obj] {obj.setPriority(b); s += b; s += "\n";} 
    	   )
    	 /*|#(EVOKE
    	        COLON {s += " Type: "; }  b = textAST[obj] {obj.setType(b); s += b; s += "\n";} 
    	   )
    	 */
    	 |
    	 #(LOGIC
    	       COLON  b = textAST[obj] {obj.setLogic(b); s += b; s += "\n";} 
    	    )
    	 |#(ACTION
    	       COLON  b = textAST[obj] {obj.setAction(b); s += b; s += "\n";} 
    	   )
    	 /*|#(URGENCY
    	        COLON {s += " Type: "; }  b = textAST[obj] {obj.setType(b); s += b; s += "\n";} 
    	   )
    	  */
    	  |#(AGE_MIN 
    	      COLON   b = doubleAST[obj] {obj.setAgeMin(b); s += b; s += "\n";} 
    	   )
    	   |#(AGE_MAX 
    	      COLON   b = doubleAST[obj] {obj.setAgeMax(b); s += b; s += "\n";} 
    	   )
		 | a = textAST[obj] {s += a;} ENDBLOCK {s += "\n";}     	
    	)
    
    )*
   )
   
  )
;



/***********************MAINTENANCE*******************************************/
maintenance [MLMObject obj] returns [String s=""]
{String a="",b = "";}
: (
  #(MAINTENANCE 
  	( 
  		(#(FILENAME 
  			 COLON {s += " Filename: "; }  b = textAST[obj] {obj.setClassName(b); s += b; s += "\n";} 
  		  )
  		|#(MLMNAME 
  			 COLON {s += " Filename: "; }  b = textAST[obj] {obj.setClassName(b); s += b; s += "\n";} 
  		  )
  		|#(VERSION 
  			 COLON {s += " Version: "; }  b = doubleAST[obj] {obj.setVersion(b); s += b; s += "\n";} 
  		  )
  		|#(TITLE
  			COLON {s += " Title: "; }  b = textAST[obj] {obj.setTitle(b); s += b; s += "\n";} 
  		  )
  		|#(AUTHOR
  			COLON {s += " Author: "; }  b = textAST[obj] {obj.setAuthor(b); s += b; s += "\n";} 
  		  )
  		|#(SPECIALIST
  			COLON {s += " Specialist: "; }  b = textAST[obj] {obj.setSpecialist(b); s += b; s += "\n";} 
  		  )
  		|#(DATE
  			COLON {s += " Date: "; }  b = doubleAST[obj] {obj.setDate(b); s += b; s += "\n";} 
  		  )
   		|#(INSTITUTION
  			COLON {s += " Institution: "; }  b = textAST[obj] {obj.setInstitution(b); s += b; s += "\n";} 
  		  )  
  		| a = textAST[obj] {s += a;} ENDBLOCK {s += "\n";} 
  		)
  		
  		
    )* 
   )
   
  )
;

textAST [MLMObject obj] returns [String s=""]
{String a="",b="";}
: (
	(
 	//	 #(FILENAME COLON {s = " Filename: "; }  b = textAST[obj] {obj.setClassName(b); s += b;})
	//	|
		(str: ~(ENDBLOCK) {a = " " + str.getText();s += a; /*System.err.println(s);*/} )*  
		

	)
)
;

doubleAST [MLMObject obj] returns [String s=""]
{String a="",b="";}
: (
	(
 		(str: ~(ENDBLOCK) {a = str.getText();s += a; /*System.err.println(s);*/} )*  
	)
)
;



/***********************LIBRARY*******************************************/
library [MLMObject obj] returns [String s=""]
{String a="",b="";}
: (
  #(LIBRARY 
  	 (  
  	    #(PURPOSE 
  	    	COLON {s += " Purpose: "; }  b = textAST[obj] {obj.setPurpose(b); s += b; s += "\n";}
  	      )
  	    |#(EXPLANATION
  	    	COLON {s += " Explanation: "; }  b = textAST[obj] {obj.setExplanation(b); s += b; s += "\n";}
  	      )
  	    |#(KEYWORDS
  	    	COLON {s += " Keywords: "; }  b = textAST[obj] {obj.setKeywords(b); s += b; s += "\n";}
  	      )
  	    |#(CITATIONS
  	    	COLON {s += " Citations: "; }  b = textAST[obj] {obj.setCitations(b); s += b; s += "\n";}
  	      )
  	    |#(LINKS
  	    	COLON {s += " Links: "; } 	
  	    	(#(HTTP { s+= "http://"; })
  	    	| a = textAST[obj] {s += a;s += "\n";}
  	    	 )?
  	    		 b = textAST[obj] {obj.setLinks(b); s += b; s += "\n";} 
  	      )  
  		 
     )* 
   
   )
  )
;
 
 

dateAST [GregorianCalendar calendar] returns [String s = ""]
{String year = "", month="", day ="";}
	:#(MINUS 
		#(MINUS {calendar.clear();}
	      ( tyear: INTLIT 
	      	{	 year = tyear.getText();
	      		 s += year; s += "-";
	      		 calendar.set(calendar.YEAR, Integer.valueOf(year)); 
	       	} 
	     
	     	tmonth: INTLIT 
	      	{	 month = tmonth.getText();
	      		 s += month; s += "-";
	      		 calendar.set(calendar.MONTH, Integer.valueOf(month) - 1);  // Month is 0 -11 in the Calendar class 
	       	}
	      )	
	    		  
	   )
	   tday: INTLIT 
	   { day = tday.getText();
	   	 s += day; 
	     calendar.set(calendar.DAY_OF_MONTH, Integer.valueOf(day));
	   } 	 
	 ) 
	 
	;


/*************************************************************************************/

class ArdenBaseLexer extends Lexer;


options {
  charVocabulary = '\0'..'\377';
  testLiterals=false;    // don't automatically test for literals
  k=2;                   // two characters of lookahead
  caseSensitive=false;
  caseSensitiveLiterals=false;
  }

// -- Declarations --
//{
    // NOTE: The real implementations are in the subclass.
//    protected void setPossibleID(boolean possibleID) {}
//}


// @@startrules



ARDEN_CURLY_BRACKETS
	: 
	LCURLY
	(options {greedy=false;}: .)*
	RCURLY
	
	;

NOT_COMMENT
 : "://" (~('\n'|'\r'| ';'))*
 
	;	

// Single-line comments
COMMENT
  : "//" (~('\n'|'\r'))*
     { $setType(Token.SKIP); }
  ;

ML_COMMENT
  : "/*"
    (               /* '\r' '\n' can be matched in one alternative or by matching
                       '\r' in one iteration and '\n' in another. I am trying to
                       handle any flavor of newline that comes in, but the language
                       that allows both "\r\n" and "\r" and "\n" to all be valid
                       newline is ambiguous. Consequently, the resulting grammar
                       must be ambiguous. I'm shutting this warning off.
                    */
      options {
        generateAmbigWarnings=false;
      }
      :  { LA(2)!='/' }? '*'
      | '\r' '\n' {newline();}
      | '\r' {newline();}
      | '\n' {newline();}
      | ~('*'|'\n'|'\r')
    )*
    "*/"
    {$setType(Token.SKIP);}
;


// Literals
protected DIGIT
  : '0'..'9'
  ;

INTLIT 
  : (
      ((DIGIT)+ (COMMA)?)* 
	      (
	         (LPAREN (DIGIT)+ RPAREN) 
	      )?
      
    )
  ;

//INTDOT
//	:(DIGIT)+ DOT (DIGIT)+
//	;

//CHARLIT
//  : 
//  '\''! TEXT (WS)? (DOT)? (INTLIT)? (DOT)? (INTLIT)? (DOT)? (INTLIT)? '\''!
//  ;



// string literals
STRING_LITERAL
  : '"'!
    ( '"' '"'!
    | ~('"'|'\n'|'\r')
    )*
    ( '"'!
    | // nothing -- write error message
    )
	;

// term literals
TERM_LITERAL
  : '\''!
    ( '\'' '\''!
    | ~('\''|'\n'|'\r')
    )*
    ( '\''!
    | // nothing -- write error message
    )
	;


// Whitespace -- ignored
WS
  : ( ' '
    | '\t'
    | '\f'

    // handle newlines
    | ( "\r\n"  // DOS/Windows
      | '\r'    // Macintosh
      | '\n'    // Unix
      )
      // increment the line count in the scanner
      { newline(); }
    )
    { $setType(Token.SKIP); }
  ;


// an identifier.  Note that testLiterals is set to true!  This means
// that after we match the rule, we look in the literals table to see
// if it's a literal or really an identifer
//protected TEXT
 //  options {
//   	testLiterals=true;
//    }
//  : (('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'| MINUS | COMMA| DOT | DIV | UNDERSCORE)* | ( LPAREN ('a'..'z'|'A'..'Z'|'0'..'9'| MINUS | COMMA| DOT | AT | DIV)* RPAREN ))
//    ;
 
 
 
//IDORTEXT:
//          i:ID  {

//                  if ( i.getType() == LITERAL_FROM )
//                  {
//                          $setType(Token.SKIP);
//                  }
//                  else
//                  {
//                          $setType($setType(i.getType()));
//                  }
//
//                }
//          ;
 


  
ID
  options {testLiterals=true; paraphrase = "an identifier";}
//  	: ('a'..'z'|'A'..'Z') (('a'..'z'|'A'..'Z'| UNDERSCORE) | ('0'..'9'))*
  : (
      ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'0'..'9' |MINUS | COMMA| DOT | DIV | UNDERSCORE|AT)*
  //  | ( LPAREN ('a'..'z'|'A'..'Z'|'0'..'9'| MINUS | COMMA| DOT | AT | DIV)* RPAREN )
    )
 // 	{
 // 		setPossibleID(true);
 // 	}
	;

//protected LETTER
//	: ('a'..'z'|'A'..'Z')
//	;
  
// Operators
DOT        : '.'   ;
BECOMES    : ":="  ;
COLON      : ':'   ;

SEMI       : ';'   ;

COMMA      : ','   ;
EQUALS     : '='   ;
LBRACKET   : '['   ;
RBRACKET   : ']'   ;
DOTDOT     : ".."  ;
LPAREN     : '('   ;
RPAREN     : ')'   ;
NOT_EQUALS : "/="  ;
LT         : '<'   ;
LTE        : "<="  ;
GT         : '>'   ;
GTE        : ">="  ;
PLUS       : '+'   ;
MINUS      : '-'   ;
TIMES      : '*'   ;
DIV        : '/'   ;
NE		   : "<>"  ;
APOSTROPHE : '\''  ;
AMPERSAND  : '&'   ;
PERCENT	   : '%'   ;
POUND      : '#'   ;
QUESTION   : '?'   ;

ENDBLOCK    options {paraphrase = ";;";} : ";;" ;
ACTION_OP  : "||" ;
	

/*
ENDBLOCK   :                           
			";"
			( 
			options {generateAmbigWarnings=false;}
			 : { LA(1) !=';'}? '.'
			 
			 | '\r' '\n' {newline();}
		     | '\r' {newline();}
		     | '\n' {newline();}
		     | ~(';' | '\n'|'\r') {$setType(SEMI);}
			) 
			";" {$setType(ENDBLOCK);}
			;
*/

UNDERSCORE : "_"   ;
AT		   : "@"   ;
SINGLE_QUOTE: '\''! ;
LCURLY	   : '{'	;
RCURLY	   : '}'	;

// Reserved Words


