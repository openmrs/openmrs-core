header {
package org.openmrs.logic.queryparser;

import java.io.*;
import antlr.CommonAST;
import antlr.collections.AST;
import antlr.*;
import java.lang.Integer;
import org.openmrs.logic.*;
import org.openmrs.logic.op.*;
import java.util.GregorianCalendar;

}



// @@parser
//-----------------------------------------------------------------------------
// Define a Parser, calling it LogicQueryParser
//-----------------------------------------------------------------------------
class LogicQueryBaseParser extends Parser;
options {
  /*defaultErrorHandler = true; */     // Don't generate parser error handlers
  buildAST=true;
  
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
	
	DATA="data";
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

// Wraps the ID token from the lexer, in order to provide
// 'keyword as identifier' trickery.
any_reserved_word
	: AND | IS | ARE | WAS | WERE | COUNT | IN | LESS | THE | THAN | FROM | BEFORE |AFTER | AGO | AT | OF
	| WRITE | BE | LET | YEAR | YEARS | IF | IT | THEY | NOT | OR | THEN | MONTH | MONTHS | TIME | TIMES | WITHIN
	| READ | MINIMUM | MIN | MAXIMUM | MAX | LAST | FIRST | EARLIEST | LATEST | EVENT | WHERE | EXIST | EXISTS | PAST
	| AVERAGE | AVG | SUM | MEDIAN | CONCLUDE | ELSE | ELSEIF | ENDIF | TRUE | FALSE | DATA | LOGIC | ACTION | CALL | WITH
	| TO | ANY | RESEARCH | DAY | SECOND | OCCUR | PURPOSE | PRESENT | NUMBER | KNOWLEDGE | PRIORITY
	;

text
    : ID | (any_reserved_word) | INTLIT | 
    MINUS | COMMA| DOT | DIV | UNDERSCORE|AT|STRING_LITERAL|
    (LPAREN (ID| INTLIT| (any_reserved_word))* RPAREN)
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

datepart:	(INTLIT (MINUS INTLIT)+ ) // ******needs improvement
			timepart_opt 
	;

timepart_opt
	:
	 |(timepart) ENDBLOCK
	
	;

timepart:
	time
	INTLIT ":" INTLIT ":" INTLIT
	fractional_seconds
	time_zone ENDBLOCK
	;

time :
	  "T"
	| "t"
	;

fractional_seconds : 		/* no spaces are permitted between elements */
	  "." DIGIT
	| /* empty */
	;

time_zone : 				/* no spaces are permitted between elements */
	  /* null */
	| zulu
	| "+" DIGIT DIGIT ":" DIGIT DIGIT
	| "-" DIGIT DIGIT ":" DIGIT DIGIT
	
	;

zulu :
	  "Z"
	| "z" 
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
	| (INTLIT  ((MINUS^ INTLIT)+  |   (DOT^ INTLIT))? )
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

// Openmrs parser
query_parse 
	:( (of_read_func_op) | (from_of_func_op (INTLIT )?) ) expr (temporal_comp_op iso_date_time) ?	SEMI!
	;

/*************************************************************************************/
class LogicQueryTreeParser extends TreeParser;

options {
	importVocab=LogicQueryBaseParser;
}

//(of_read_func_op) | (from_of_func_op (INTLIT )?) expr (temporal_comp_op iso_date_time) ?	SEMI; 
 
query_AST returns [LogicCriteria lc_return = null]
{String a = "", b="";
Operator transform = null, comp_op = null, temporal_op = null;
boolean lcFormed = false;
LogicCriteria lc = null;
}
:
(
	(transform = of_from_AST)? 
	(
		#(ift:ID 
				      { a = ift.getText(); //System.err.println("text = " + a);
				  				      	
				      }
				    
		   )  
		|
		#(ifst:STRING_LITERAL 
				      { a = ifst.getText(); //System.err.println("text = " + a);
				      	
				      }
				    
		   )
		|
		#(val:INTLIT
				      { a = val.getText(); //System.err.println("text = " + a);
				      	// This is an error on LHS of an expr
				      }
		
		)      
	) 	
	(
		(	      
			comp_op = simple_comp_op  
			(
				#(idt:ID 
					      { b += idt.getText(); //System.err.println("text = " + b);
					      	lc = new LogicCriteria(null,a);
			 				lc.appendExpression(comp_op, b);
			 				//lc_return = lc.applyTransform(transform);
			 				lcFormed = true;
					      }
					    
			    )
			    |
				#(idstr:STRING_LITERAL 
						  { b += idstr.getText(); //System.err.println("text = " + b);
						  	lc = new LogicCriteria(null,a);
						 	lc.appendExpression(comp_op, b);
			 				//lc_return = lc.applyTransform(transform);
							lcFormed = true;
						  }
						    
				   )
				|
				#(valstr:INTLIT
						  { b += valstr.getText(); //System.err.println("text = " + b);
						  	Integer i = null;
						
						  	i = Integer.parseInt(b);
						  	lc = new LogicCriteria(null,a);
			 				
				  			lc.appendExpression(comp_op, i);
			 				//lc_return = lc.applyTransform(transform);
			 				lcFormed = true;
						 }
				
				) 
				|
				 #(DOT val1: INTLIT
						{
						 	b = val1.getText();
							
											
						}
						val2: INTLIT
						{
						 	Double idbl = null;
						
							String dbl = b + "." + val2.getText();
							idbl = Double.parseDouble(dbl);
							  											
							
						  	lc = new LogicCriteria(null,a);
			 				
				  			lc.appendExpression(comp_op, idbl);
			 				//lc_return = lc.applyTransform(transform);
							lcFormed = true;
							
						}
				  )     
			)
	
			(
				temporal_op = temporal_comp_op  
				(
					{GregorianCalendar gc = new GregorianCalendar();} b = dateAST [gc] 
							{
								lc.appendExpression(temporal_op, gc.getTime());
						 		//lc_return = lc.applyTransform(transform);
						 		lcFormed = true;
							} 		  		
							    
				)
			
		   )?
			
		 
		) ?
		|
		(
			temporal_op = temporal_comp_op  
			(
				{GregorianCalendar gc = new GregorianCalendar();} b = dateAST [gc] 
						{
						    lc = new LogicCriteria(null,a);
					 		lc.appendExpression(temporal_op, gc.getTime());
					 		//lc_return = lc.applyTransform(transform);
					 		lcFormed = true;
						} 		  		
						    
			)
			
		)?
	) 
	{
		if(lcFormed == false)  // just a terminal symbol like CD4 COUNT
		{
			lc_return = new LogicCriteria(null,a);
		}
		else
		{
			lc_return = lc.applyTransform(transform);
		}
		return lc_return;
	}   		       
)
;

doubleAST returns [String s=""]
{String a="",b="";}
: #(DOT val2: INTLIT
	{
		s = val2.getText();
						
	}
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

simple_comp_op returns [Operator s = null]
{String a = "", b="";}
:
(
   #(EQUALS {
   				//System.err.println("Found = ");
   				s = Operator.EQUALS;
   			}
   	 )
	|
	#(GTE {
   				//System.err.println("Found >= ");
   				s = Operator.GTE;
   			}
   	 )
   	 |
   	 #(GT {
   				//System.err.println("Found > ");
   				s = Operator.GT; 
   			}
   	 )
   	 |
   	 #(LT {
   				//System.err.println("Found < ");
   				s = Operator.LT;
   				 
   			}
   	 )
   	 |
   	 #(LTE {
   				//System.err.println("Found <= ");
   				s = Operator.LTE; 
   			}
   	 )
   	 #(NE {
   				//System.err.println("Found <> ");
   				s = Operator.NOT_EXISTS; 
   			}
   	 )
)
;

temporal_comp_op returns [Operator s = null]
{String a = "", b="";}
:
(
   #(BEFORE {
   				//System.err.println("Found BEFORE ");
   				s = Operator.BEFORE;
   			}
   	 )
	|
	#(AFTER {
   				//System.err.println("Found AFTER ");
   				s = Operator.AFTER;
   			}
   	 )
   	 
)
;


of_from_AST  returns [Operator s = null]
{}
:
	  ( (LAST | LATEST) 
	  {s = Operator.LAST; }
	  )
	  |
	  ( (FIRST | EARLIEST) 
	  {s = Operator.FIRST; }
	  )
	  |
	  ( (EXIST | EXISTS) 
	  {s = Operator.EXISTS; }
	  )
	   


; 
 

/*************************************************************************************/

class LogicQueryLexer extends Lexer;


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


