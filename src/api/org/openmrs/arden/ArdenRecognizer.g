header {
package org.openmrs.arden;

import java.io.*;
import antlr.CommonAST;
import antlr.collections.AST;
import antlr.*;
import org.openmrs.arden.MLMObject;
import org.openmrs.arden.MLMObjectElement;
import java.lang.Integer;
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
	
	DATA="data";
	LOGIC="logic";
	ACTION="action";
	MAINTENANCE ="maintenance";
	LIBRARY="library";
	FILENAME="filename";
	MLMNAME="mlmname";
	OF = "of";
	TIME = "time";
	WITHIN = "within";
	CALL = "call";
	WITH = "with";
	TO = "to";
	ANY = "any";
	
}
/*
// Define some methods and variables to use in the generated parser.
{
  // Define a main
  public static void main(String[] args) {
    // Use a try/catch block for parser exceptions
    try {
      // if we have at least one command-line argument
      if (args.length > 0 ) {
        System.err.println("Parsing...");

        // for each directory/file specified on the command line
        for(int i=0; i< args.length;i++)
          doFile(new File(args[i])); // parse it
      }
      else
        System.err.println("Usage: java ArdenRecogizer <filename or directory name>");

    }
    catch(Exception e) {
      System.err.println("exception: "+e);
      e.printStackTrace(System.err);   // so we can get stack trace
    }
  }


  // This method decides what action to take based on the type of
  //   file we are looking at
  public static void doFile(File f) throws Exception {
    // If this is a directory, walk each file/dir in that directory
    if (f.isDirectory()) {
      String files[] = f.list();
      System.err.println("------------Total files = " + files.length);
      for(int i=0; i < files.length; i++) {
      	doFile(new File(f, files[i]));
      }
    }

    // otherwise, if this is a mlm file, parse it!
    else if (f.getName().substring(f.getName().length()-4).equals(".mlm")) {
      System.err.println("-------------------------------------------");
      System.err.println("--------------File name--" + f.getName());
      System.err.println(f.getAbsolutePath());
      parseFile(new FileInputStream(f));
    }
  }

  // Here's where we do the real work...
  public static void parseFile(InputStream s) throws Exception {
  	//new ArdenReadNode();
    try {
      // Create a scanner that reads from the input stream passed to us
      ArdenLexer lexer = new ArdenLexer(s);

      // Create a parser that reads from the scanner
      ArdenParser parser = new ArdenParser(lexer);

      // start parsing at the compilationUnit rule
      parser.startRule();
      AST t = parser.getAST();
      DumpASTVisitor visitor = new DumpASTVisitor ();
      visitor.visit(t);
      
      //String tree = parser.getAST().toStringList();
      
     System.err.println(t.toStringTree());   // prints maintenance
      
      ArdenBaseTreeParser treeParser = new ArdenBaseTreeParser();
 //     String datastr = treeParser.data(t);
 	  MLMObject ardObj = new MLMObject();
 	  
 	  treeParser.maintenance(t, ardObj);
 	  
 	 System.err.println(t.getNextSibling().toStringTree());   // prints library
      
      treeParser.library(t.getNextSibling(), ardObj);
      
     System.err.println(t.getNextSibling().getNextSibling().toStringTree()); // Print data
 	  treeParser.data(t.getNextSibling().getNextSibling(),ardObj);
      

     System.err.println(t.getNextSibling().getNextSibling().getNextSibling().toStringTree()); // Print logic
      String logicstr = treeParser.logic(t.getNextSibling().getNextSibling().getNextSibling(), ardObj);
      
     System.err.println(t.getNextSibling().getNextSibling().getNextSibling().getNextSibling().toStringTree()); // Print action
      String actionstr = treeParser.action(t.getNextSibling().getNextSibling().getNextSibling().getNextSibling(), ardObj);
      
      
      System.err.println(actionstr);
      System.err.println(logicstr);
     // System.err.println(datastr);
     ardObj.PrintConceptMap();
      
      
    }
    catch (Exception e) {
      System.err.println("parser exception: "+e);
      e.printStackTrace();   // so we can get stack trace		
    }
  }
  
  // This method is overrident in the sub class in order to provide the
    // 'keyword as identifier' hack.
    public AST handleIdentifierError(Token token,RecognitionException ex) throws RecognitionException, TokenStreamException
    {
        // Base implementation: Just re-throw the exception.
        throw ex;
    }
  
}
*/

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
	: "knowledge"! COLON! knowledge_body
	;

knowledge_body
	: type_slot!
	data_slot
	priority_slot!
	evoke_slot!
	logic_slot
	action_slot
	urgency_slot!
	;

/********** Maintenance Slots **********************/
title_slot: ("title" COLON (text)* ENDBLOCK
	)
   	;

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
	  "version" COLON INTLIT DOT INTLIT ENDBLOCK
	  ;

mlm_version :
	  text
	  ;

institution_slot :
	  "institution" COLON (text)* ENDBLOCK		/* text limited to 80 characters */
	  ;

author_slot :
	  "author" COLON (text)* (SEMI (text)*)* ENDBLOCK			/* see 6.1.6 for details */
	  ;

specialist_slot :
	  "specialist" COLON (text)* ENDBLOCK		/* see 6.1.7 for details */
	  ;

date_slot :
	  "date" COLON mlm_date ENDBLOCK
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
	| TO | ANY
	;

text
    : ID | (any_reserved_word) | INTLIT | (LPAREN (ID| INTLIT| (any_reserved_word))* RPAREN)  
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
/*****************Library slots*********************************/

purpose_slot:
	  "purpose" COLON (text)* ENDBLOCK
	;

explanation_slot:
	  "explanation" COLON 
	  (text | INTLIT)*
	  ENDBLOCK
	;	
		
keywords_slot:
	  "keywords" COLON (keyword_text) 
	  ;

keyword_text
	:
	  (text)* (SEMI (text)*)* ENDBLOCK
	;
	  
citations_slot:
	|
	| "citations" COLON (citations_list) ENDBLOCK
	;


citations_list:

	|  /* empty */
	| single_citation (SEMI single_citation)*
	;

single_citation:
	  (INTLIT DOT citation_type)* citation_text 
	
	;

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
	| "links" COLON link_body ENDBLOCK
	;

/* any string of characters enclosed in single quotes (' , ASCII 44) without ";;" */
link_body:
	(SINGLE_QUOTE (text | DOT| INTLIT)* SINGLE_QUOTE (SEMI)?)*
	;
								/* compatibility */

type_slot
	: "type" COLON type_code ENDBLOCK
	;

type_code
	: "data-driven" | "data_driven" 
	;

data_slot
	: 
	//"data"^ COLON! (data_assignment SEMI!)* ENDBLOCK
	DATA^ COLON! (data_statement SEMI!)* ENDBLOCK
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
	| data_assignment
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
	//	| <call_phrase>
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

data_assign_phrase
	:(
		READ read_phrase
	//	| "MLM" <term>
	//	| "MLM" <term> FROM "INSTITUTION" <string>
	//	| "MLM" "MLM_SELF"
	//	| "INTERFACE" <mapping_factor>
		| ("EVENT" | "Event"| "event") mapping_factor //endassignment 
	//	| "MESSAGE" <mapping_factor>
	//	| "DESTINATION" <mapping_factor>
	//	| "ARGUMENT"
	//	| "OBJECT" <object_definition>
	//	| <call_phrase>
	//	| <new_object_phrase>
	//	| <expr>
		
	) 
	;


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
	| "priority" COLON INTLIT ENDBLOCK
	;

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
	  "logic"^ COLON! (logic_statement SEMI! )* ENDBLOCK
	  ;


logic_statement:
	 (if_statement
	 | conclude_statement
	 | logic_assignment
	 | logic_elseif
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
	  "action"^ COLON! (action_statement SEMI!)* ENDBLOCK
	;
  
action_statement:
	(WRITE^) 
	(
	   	(LPAREN!)? ( (ACTION_OP expr_factor)* | expr ) (RPAREN!)? 
     	
		//expr_factor 
	)  /*(ACTION_OP^ expr_factor)* */ ((AT) ID)? 
	
	
	;

urgency_slot:
	  /* empty */
	| "urgency" COLON urgency_val ENDBLOCK
	;
urgency_val:
	  INTLIT
	  |
	
	;

/****** expressions ******/
expr :
	  expr_sort (COMMA expr_sort)*
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
	: expr_comparison (NOT expr_comparison)*
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
	| INTLIT
	| time_value
	| boolean_value
	| STRING_LITERAL
	| TERM_LITERAL
	
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
: #(DATA {System.err.println("\n"); System.err.println("-------Starting Data--------");} 
	 (
	  {System.err.println("-----------Starting Read -------");}s=readAST[obj, s]  {System.err.println("\n");System.err.println("-----------End Read -------");}
	 |{System.err.println("----------------Starting Event-------");} eventAST {System.err.println("\n");System.err.println("-----------End Event -------");}
 	 |{System.err.println("----------------Starting Data If-------");} dataIfAST[obj] {System.err.println("\n");System.err.println("-----------End Data If -------");}
  	 |{System.err.println("----------------Starting Data Else If-------");} data_elseifAST[obj] {System.err.println("\n");System.err.println("-----------End Data Else If -------");}
 // 	 |{System.err.println("----------------Starting from func op-------");} from_of_func_opAST [obj] {System.err.println("----------------End from func op-------");}
 //	 |{System.err.println("----------------Starting read func op-------");} of_read_func_opAST [obj] {System.err.println("----------------End read func op-------");}
 
	 )* 
  (ENDBLOCK){System.err.println("\n");System.err.println("-----------End Data -------");})

;



dataIfAST [MLMObject obj] returns [String s=""]
{String a, b;}
:  (
    #(IF {obj.ResetConceptVar(); obj.InitEvaluateList();} s=exprAST[obj] THEN ) 
   )
   ;


data_elseifAST [MLMObject obj] returns [String s=""]
{String a,b;}
: (
     (
     	 #(ELSEIF {obj.ResetConceptVar();} s=exprAST[obj] THEN )
       | #(ELSE {obj.ResetConceptVar();} s=exprAST[obj] {obj.AddConcept(s);obj.SetDBAccess(false,s);}  )
       | #(ENDIF {System.err.println("ENDIF FOUND");} )
     )   
   )
;

where_it_occurredAST [MLMObject obj, String key] returns [String s=""]
{String a,b, ret_val="";}
:
	(WITHIN {obj.setWhere("withinPreceding", key);}
	    (PAST) (m:INTLIT n:duration_op) {obj.setDuration("past",m.getText(),n.getText(),key); System.err.println("Duration Clause - " + m.getText() + " " + n.getText());} 
		| a = exprAST[obj] TO b = exprAST[obj]
	)
	|(AFTER) (i:ID){System.err.println("Variable = " + i.getText());}
;


readAST [MLMObject obj, String instr] returns [String s=""]
{String a="",b="", ret_val="";}
: (
  #(READ  a=readAST[obj, instr] b=readAST[obj, a]) {s += ret_val;}

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
	  {s+=b;obj.setReadType("last"); obj.setHowMany("1");System.err.println("ReadType = Exist");}
	   b=readAST[obj, instr]) 
	  
	  | ((AVERAGE | AVG) b=readAST[obj, instr]) {s+=b;System.err.println("ReadType = Average");}        //To DO
	  | (COUNT b=readAST[obj, instr]) {s+=b;System.err.println("ReadType = Count");}					//To DO
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
	            obj.AddConcept(s);obj.SetDBAccess(false,instr);
	           }
	  | f:FALSE {s="false";
	 			 obj.AddConcept(s);obj.SetDBAccess(false,instr);
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
  			
  
  | ((EXIST | EXISTS)) {obj.AddToEvaluateList("EXIST");System.err.println("ReadType = Exist");}
  
  | ((AVERAGE | AVG) ) {obj.AddToEvaluateList("AVG");System.err.println("ReadType = Average");}
  | (COUNT) {obj.AddToEvaluateList("COUNT");System.err.println("ReadType = Count");}
  | (SUM) {obj.AddToEvaluateList("SUM");System.err.println("ReadType = Sum");}
  | (MEDIAN) {obj.AddToEvaluateList("MEDIAN");System.err.println("ReadType = Median");}
  
  // End of of_read_func_op
  
);

of_noread_func_opAST [MLMObject obj] returns [String s=""]
{String a,b, ret_val="";}
	:
(	
  // Following are of_read_func_op
  			
  
  | (ANY) {obj.AddToEvaluateList("ANY");System.err.println("Any of");}
  
  
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
: #(LOGIC {System.err.println("\n"); System.err.println("-------Starting LOGIC--------");} 
	
	 (
	   {System.err.println("-----------Starting IF -------");} a=ifAST[obj]
			   (	{System.err.println("-----------Starting Logic Assignment -------");} logicAssignmentAST[obj, a]  { System.err.println("\n");System.err.println("-----------End logic assignment -------");} 
		
			   )?
			   (
			   		{System.err.println("-----------Starting CONCLUDE -------"); } (concludeAST[obj, a])? {System.err.println("\n");System.err.println("-----------End CONCLUDE -------");}
			   )?
			       
	   {System.err.println("\n");System.err.println("-----------End IF -------");} 
	   
	  | {System.err.println("-----------Starting ELSE - ELSEIF -------");} a=logic_elseifAST[obj, i] 
			  (	 {System.err.println("-----------Starting Logic Assignment -------");} logicAssignmentAST[obj, a]  {System.err.println("\n");System.err.println("-----------End logic assignment -------");} 
					      
			  )?
			  (
			  	{System.err.println("-----------Starting CONCLUDE -------");} (concludeAST[obj, a])? {System.err.println("\n");System.err.println("-----------End CONCLUDE -------");}
			  )?	  
	   {System.err.println("\n");System.err.println("-----------End ELSE- ELSEIF -------");}
	  
	  | #(ENDIF {System.err.println("ENDIF FOUND");a = "ENDIF"; obj.AddToEvaluateList(a);obj.SetConceptVar(a);} )
	         
	  | {System.err.println("-----------Starting CONCLUDE -------");obj.InitEvaluateList(); a = "Conclude_" + Integer.toString(i);} concludeAST[obj, a]  {System.err.println("\n");System.err.println("-----------End CONCLUDE -------");}
	  | {System.err.println("-----------Starting CALL -------");obj.InitEvaluateList(); a = "" ;} callAST[obj, a]  {System.err.println("\n");System.err.println("-----------End CALL -------");}

	{i++;} )* 
 	
  (ENDBLOCK){System.err.println("\n");System.err.println("-----------End LOGIC -------");})
;

callAST [MLMObject obj, String key] returns [String s=""]
{String a,b;}
: (
    #(CALL b=exprStringAST[obj, key] {obj.SetConceptVar(b);} a = exprStringAST [obj, b] {obj.setReadType("call"); obj.AddConcept(a);obj.SetDBAccess(false,b); }
      
     ) 
  )
	;

ifAST [MLMObject obj] returns [String s=""]
{String a,b;}
: (
    #(IF {obj.ResetConceptVar(); obj.InitEvaluateList(); obj.AddToEvaluateList("IF");} s=exprAST[obj] ((OR {obj.AddToEvaluateList("OR");} | AND {obj.AddToEvaluateList("AND");} | NOT {obj.AddToEvaluateList("NOT");}) s=exprAST[obj])? THEN {obj.AddToEvaluateList("THEN");}) 
   )
   ;

//logic_if_then_else2AST [MLMObject obj] returns [String s=""]
//{String a,b;}
//:
//	 exprAST[obj] #(THEN ) 
//;

logicAssignmentAST [MLMObject obj, String key] returns [String s=""]
{String a="",b="";}
:
      a = exprStringAST[obj, "CTX"/*key Do not use key- depends on context so CTX*/] {obj.AddToEvaluateList("Logic_Assignment");}
      (
					thisstrlit: STRING_LITERAL {b += thisstrlit.getText(); } 
					    (ACTION_OP str1: STRING_LITERAL {b += str1.getText();} )*
					{obj.SetUserVarVal(a, b, key);}
	   )?
	   
     /* ( 
      	(strlit: STRING_LITERAL) 
      	(ACTION_OP 
		id: ID {a = id.getText(); } 
		ACTION_OP 
		str: STRING_LITERAL {b = str.getText(); 
				obj.SetUserVarVal(a, b, key);}
		
	  	)
      )?
      */
;



exprAST [MLMObject obj] returns [String s=""]
{String a,b;}
:
( 
	(a = exprStringAST[obj, ""] {s=a;}((simple_comp_opAST[obj, a] | binary_comp_opAST[obj, a]) b = exprStringAST[obj, a] )? )
//	| expr_functionAST[obj] (a = exprStringAST[obj, ""] {s=a;}( (COMMA a = exprStringAST[obj, ""] {s=a;})* (binary_comp_opAST[obj, a]) b = exprStringAST[obj, a] {/*obj.SetAnswer(b, a);*/}) )
	| expr_functionAST[obj] (a = exprStringAST[obj, "notnull"] {s=a;}( (binary_comp_opAST[obj, a]) b = exprStringAST[obj, a] (COMMA exprStringAST[obj, a] {s=a;})*  {/*obj.SetAnswer(b, a);*/})? )
	

);

expr_functionAST [MLMObject obj] returns [String s=""]
:
     (
    	from_of_func_opAST[obj] (OF)? 
    	| of_read_func_opAST[obj] (OF)? 
    	| of_noread_func_opAST[obj] (OF)?
     )

	;


exprStringAST [MLMObject obj, String instr] returns [String s=""]
{String a="",b="";}
:
(
 	#(ift:ID 
			      { a = ift.getText(); System.err.println("text = " + a); 
			        if(instr.equals("")) {
			        		obj.AddToEvaluateList(a); obj.SetConceptVar(a);
			        		s= a;
				      //  	obj.RetrieveConcept(a); 
			        }
			        else if(instr.equals("CTX")) {
			        	s=a;
			        	// do nothing for now
			        }
			        else if(instr.equals("notnull")) {
			        	obj.AddToEvaluateList(a);
			        	if(obj.GetMLMObjectElement(a) == null) {
			        		s="Func_1";  // Func like Exist..          
			        	}
			        	else {
			        		s=a;
			        	}
			        	
			        }
			        else { // if instr is not empty then we are evaluating RHS of an equation, it can be a non string literal
			        	obj.SetAnswer(a,instr);					
			        	s=a;
			        }
			        
			      }
			    
	   )   		      
	      
    | (
    	  #(TRUE {obj.SetAnswer(true, instr);})
    	| #(FALSE {obj.SetAnswer(false, instr);})
      )
	  
	| (val:INTLIT
		{ 
		  b = val.getText();
		  Integer i = Integer.parseInt(b);
		  obj.SetAnswer(i, instr);
		}
	  )
	| (strlit: STRING_LITERAL
		{
			b = strlit.getText();
			obj.SetAnswer(b,instr);					
			
		}
	  )
	  
	| (termlit: TERM_LITERAL
		{
			b = termlit.getText();
			obj.SetAnswer(b,instr);					
			
		}
	  )
	  
	| #(ACTION_OP 
		id: ID {a = id.getText(); } 
		ACTION_OP 
		str: STRING_LITERAL {b = str.getText(); 
				obj.SetUserVarVal(a, b, instr);}
		
	  )

//	| ( // Empty as in else conclude
//		{ a = "tmp_01"; System.err.println("IF text = " + a); 
//			        if(instr.equals("")) {
//			        	 obj.AddToEvaluateList(a);
//			        	 obj.SetConceptVar(a);
//			        	}
//			      //  	obj.RetrieveConcept(a); 
//			        s= a;
//        }
//	  )

      
  )
;

simple_comp_opAST [MLMObject obj, String key] returns [String s=""]
{String a,b;}
:
   #(EQUALS {
   				System.err.println("Found = ");
   				 obj.SetCompOperator(EQUALS, key);
   			}
   	 )
	|
	#(GTE {
   				System.err.println("Found >= ");
   				 obj.SetCompOperator(GTE, key);
   			}
   	 )
   	 |
   	 #(GT {
   				System.err.println("Found > ");
   				 obj.SetCompOperator(GT, key);
   			}
   	 )
   	 |
   	 #(LT {
   				System.err.println("Found < ");
   				 obj.SetCompOperator(LT, key);
   			}
   	 )
   	 |
   	 #(LTE {
   				System.err.println("Found <= ");
   				 obj.SetCompOperator(LTE, key);
   			}
   	 )
;
	
binary_comp_opAST [MLMObject obj, String key] returns [String s=""]
{String a,b;}
:
   #(EQUALS {
   				System.err.println("Found = ");
   				 obj.SetCompOperator(EQUALS, key);
   			}
   	 )
	|
   	 #(GREATER THAN {
   				System.err.println("Found > ");
   				 obj.SetCompOperator(GT, key);
   			}
   			(OR EQUAL )? {
   			    System.err.println("Found >= ");
   				 obj.SetCompOperator(GTE, key);
   			}
   	 )
   	 |
   	 #(LESS THAN {
   				System.err.println("Found < ");
   				 obj.SetCompOperator(LT, key);
   			}
   	 )
   	 |
   	 #(LESS THAN OR EQUAL {
   				System.err.println("Found <= ");
   				 obj.SetCompOperator(LTE, key);
   			}
   	 )
   	 |
   	 #(IN {
   				System.err.println("Found IN ");
   				 obj.SetCompOperator(IN, key);
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
	    			obj.SetConceptVar(key);
	    			obj.AddConcept(key);
	    			if(key.startsWith("Conclude_")) { // Simply Conclude
	    				obj.AddToEvaluateList(key); 
	    			}
	    			else {  // Associate with the Else before
	    				obj.AddToEvaluateList(a); 
	    			}
	    			if(key.startsWith("Func_")) {obj.SetDBAccess(false,key);}
	    		}
    			
    		   } 
      (   FALSE {System.err.println("***CONCLUDE FALSE " );
      				obj.SetConcludeVal(false, key);} 
  	    | TRUE  {System.err.println("***CONCLUDE TRUE " );
  	    			obj.SetConcludeVal(true, key);}  
  	  )
     ) 
  )
;

logic_elseifAST [MLMObject obj, Integer i] returns [String s=""]
{String a,b;}
: (
     (
     	 #(ELSEIF {obj.ResetConceptVar();} 
     	 { a = "ELSEIF"; System.err.println("ELSEIF" ); 
					        	 obj.AddToEvaluateList(a);
					        	 obj.SetConceptVar(a);
		        }
     	      	 s=exprAST[obj] THEN  {obj.AddToEvaluateList("THEN");} )
 //      | #(ELSE {obj.ResetConceptVar();} s=exprAST[obj] {obj.AddConcept(s);obj.SetDBAccess(false,s);}  )

		 | #(ELSE {obj.ResetConceptVar();}  
		 
				{ a = "ELSE_"; s= a+ Integer.toString(i); System.err.println("ELSE" ); 
					        	 obj.AddToEvaluateList(s);
					        	 obj.SetConceptVar(s);
		        }
		  )
 //      | #(ENDIF {System.err.println("ENDIF FOUND"); a = "ENDIF"; obj.AddToEvaluateList(a);obj.SetConceptVar(a);} )
     )   
   )
;

/***********************ACTION*******************************************/
action [MLMObject obj] returns [String s=""]
{String a,b;}
: #(ACTION {System.err.println("\n"); System.err.println("-------Starting Action--------");} 
	 (
	   {System.err.println("-----------Starting Write -------");} s = writeAST[obj] {System.err.println("\n");System.err.println("-----------End Write -------");}
	 )* 
  (ENDBLOCK){System.err.println("\n");System.err.println("-----------End Action -------");})
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

library [MLMObject obj] returns [String s=""]
{String a="",b="";}
: (
  #(LIBRARY 
  	 ( 
  		a = textAST[obj] {s += a;} ENDBLOCK {s += "\n";} 
    )* 
   
   )
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


