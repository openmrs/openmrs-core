// $ANTLR 2.7.6 (2005-12-22): "ArdenRecognizer.g" -> "ArdenBaseParser.java"$

package org.openmrs.arden;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.DumpASTVisitor;
import antlr.NoViableAltException;
import antlr.ParserSharedInputState;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenBuffer;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.collections.AST;
import antlr.collections.impl.BitSet;

public class ArdenBaseParser extends antlr.LLkParser       implements ArdenBaseParserTokenTypes
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
  

protected ArdenBaseParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public ArdenBaseParser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected ArdenBaseParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public ArdenBaseParser(TokenStream lexer) {
  this(lexer,1);
}

public ArdenBaseParser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

	public final void startRule() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST startRule_AST = null;
		
		try {      // for error handling
			maintenance_category();
			astFactory.addASTChild(currentAST, returnAST);
			library_category();
			astFactory.addASTChild(currentAST, returnAST);
			knowledge_category();
			astFactory.addASTChild(currentAST, returnAST);
			match(LITERAL_end);
			match(COLON);
			startRule_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = startRule_AST;
	}
	
	public final void maintenance_category() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST maintenance_category_AST = null;
		
		try {      // for error handling
			{
			AST tmp75_AST = null;
			tmp75_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp75_AST);
			match(MAINTENANCE);
			match(COLON);
			}
			maintenance_body();
			astFactory.addASTChild(currentAST, returnAST);
			maintenance_category_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_1);
			} else {
			  throw ex;
			}
		}
		returnAST = maintenance_category_AST;
	}
	
	public final void library_category() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST library_category_AST = null;
		
		try {      // for error handling
			AST tmp77_AST = null;
			tmp77_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp77_AST);
			match(LIBRARY);
			match(COLON);
			library_body();
			astFactory.addASTChild(currentAST, returnAST);
			library_category_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_2);
			} else {
			  throw ex;
			}
		}
		returnAST = library_category_AST;
	}
	
	public final void knowledge_category() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST knowledge_category_AST = null;
		
		try {      // for error handling
			match(LITERAL_knowledge);
			match(COLON);
			knowledge_body();
			astFactory.addASTChild(currentAST, returnAST);
			knowledge_category_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_3);
			} else {
			  throw ex;
			}
		}
		returnAST = knowledge_category_AST;
	}
	
	public final void maintenance_body() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST maintenance_body_AST = null;
		
		try {      // for error handling
			title_slot();
			astFactory.addASTChild(currentAST, returnAST);
			mlmname_slot();
			astFactory.addASTChild(currentAST, returnAST);
			arden_version_slot();
			astFactory.addASTChild(currentAST, returnAST);
			version_slot();
			astFactory.addASTChild(currentAST, returnAST);
			institution_slot();
			astFactory.addASTChild(currentAST, returnAST);
			author_slot();
			astFactory.addASTChild(currentAST, returnAST);
			specialist_slot();
			astFactory.addASTChild(currentAST, returnAST);
			date_slot();
			astFactory.addASTChild(currentAST, returnAST);
			validation_slot();
			astFactory.addASTChild(currentAST, returnAST);
			maintenance_body_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_1);
			} else {
			  throw ex;
			}
		}
		returnAST = maintenance_body_AST;
	}
	
/********** Maintenance Slots **********************/
	public final void title_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST title_slot_AST = null;
		
		try {      // for error handling
			{
			AST tmp81_AST = null;
			tmp81_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp81_AST);
			match(LITERAL_title);
			AST tmp82_AST = null;
			tmp82_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp82_AST);
			match(COLON);
			{
			_loop12:
			do {
				if ((_tokenSet_4.member(LA(1)))) {
					text();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop12;
				}
				
			} while (true);
			}
			AST tmp83_AST = null;
			tmp83_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp83_AST);
			match(ENDBLOCK);
			}
			title_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_5);
			} else {
			  throw ex;
			}
		}
		returnAST = title_slot_AST;
	}
	
	public final void mlmname_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST mlmname_slot_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case MLMNAME:
			{
				AST tmp84_AST = null;
				tmp84_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp84_AST);
				match(MLMNAME);
				AST tmp85_AST = null;
				tmp85_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp85_AST);
				match(COLON);
				mlmname_text();
				astFactory.addASTChild(currentAST, returnAST);
				mlmname_slot_AST = (AST)currentAST.root;
				break;
			}
			case FILENAME:
			{
				AST tmp86_AST = null;
				tmp86_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp86_AST);
				match(FILENAME);
				AST tmp87_AST = null;
				tmp87_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp87_AST);
				match(COLON);
				mlmname_text();
				astFactory.addASTChild(currentAST, returnAST);
				mlmname_slot_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_6);
			} else {
			  throw ex;
			}
		}
		returnAST = mlmname_slot_AST;
	}
	
	public final void arden_version_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST arden_version_slot_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_arden:
			{
				AST tmp88_AST = null;
				tmp88_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp88_AST);
				match(LITERAL_arden);
				AST tmp89_AST = null;
				tmp89_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp89_AST);
				match(COLON);
				{
				switch ( LA(1)) {
				case 81:
				{
					{
					AST tmp90_AST = null;
					tmp90_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp90_AST);
					match(81);
					AST tmp91_AST = null;
					tmp91_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp91_AST);
					match(INTLIT);
					AST tmp92_AST = null;
					tmp92_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp92_AST);
					match(MINUS);
					AST tmp93_AST = null;
					tmp93_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp93_AST);
					match(INTLIT);
					}
					break;
				}
				case LITERAL_version:
				{
					{
					AST tmp94_AST = null;
					tmp94_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp94_AST);
					match(LITERAL_version);
					version_num();
					astFactory.addASTChild(currentAST, returnAST);
					}
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				AST tmp95_AST = null;
				tmp95_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp95_AST);
				match(ENDBLOCK);
				arden_version_slot_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_version:
			{
				arden_version_slot_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_7);
			} else {
			  throw ex;
			}
		}
		returnAST = arden_version_slot_AST;
	}
	
	public final void version_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST version_slot_AST = null;
		
		try {      // for error handling
			AST tmp96_AST = null;
			tmp96_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp96_AST);
			match(LITERAL_version);
			AST tmp97_AST = null;
			tmp97_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp97_AST);
			match(COLON);
			AST tmp98_AST = null;
			tmp98_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp98_AST);
			match(INTLIT);
			AST tmp99_AST = null;
			tmp99_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp99_AST);
			match(DOT);
			AST tmp100_AST = null;
			tmp100_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp100_AST);
			match(INTLIT);
			AST tmp101_AST = null;
			tmp101_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp101_AST);
			match(ENDBLOCK);
			version_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_8);
			} else {
			  throw ex;
			}
		}
		returnAST = version_slot_AST;
	}
	
	public final void institution_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST institution_slot_AST = null;
		
		try {      // for error handling
			AST tmp102_AST = null;
			tmp102_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp102_AST);
			match(LITERAL_institution);
			AST tmp103_AST = null;
			tmp103_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp103_AST);
			match(COLON);
			{
			_loop32:
			do {
				if ((_tokenSet_4.member(LA(1)))) {
					text();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop32;
				}
				
			} while (true);
			}
			AST tmp104_AST = null;
			tmp104_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp104_AST);
			match(ENDBLOCK);
			institution_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
		returnAST = institution_slot_AST;
	}
	
	public final void author_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST author_slot_AST = null;
		
		try {      // for error handling
			AST tmp105_AST = null;
			tmp105_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp105_AST);
			match(LITERAL_author);
			AST tmp106_AST = null;
			tmp106_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp106_AST);
			match(COLON);
			{
			_loop35:
			do {
				if ((_tokenSet_4.member(LA(1)))) {
					text();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop35;
				}
				
			} while (true);
			}
			{
			_loop39:
			do {
				if ((LA(1)==SEMI)) {
					AST tmp107_AST = null;
					tmp107_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp107_AST);
					match(SEMI);
					{
					_loop38:
					do {
						if ((_tokenSet_4.member(LA(1)))) {
							text();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else {
							break _loop38;
						}
						
					} while (true);
					}
				}
				else {
					break _loop39;
				}
				
			} while (true);
			}
			AST tmp108_AST = null;
			tmp108_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp108_AST);
			match(ENDBLOCK);
			author_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_10);
			} else {
			  throw ex;
			}
		}
		returnAST = author_slot_AST;
	}
	
	public final void specialist_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST specialist_slot_AST = null;
		
		try {      // for error handling
			AST tmp109_AST = null;
			tmp109_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp109_AST);
			match(LITERAL_specialist);
			AST tmp110_AST = null;
			tmp110_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp110_AST);
			match(COLON);
			{
			_loop42:
			do {
				if ((_tokenSet_4.member(LA(1)))) {
					text();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop42;
				}
				
			} while (true);
			}
			AST tmp111_AST = null;
			tmp111_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp111_AST);
			match(ENDBLOCK);
			specialist_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_11);
			} else {
			  throw ex;
			}
		}
		returnAST = specialist_slot_AST;
	}
	
	public final void date_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST date_slot_AST = null;
		
		try {      // for error handling
			AST tmp112_AST = null;
			tmp112_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp112_AST);
			match(LITERAL_date);
			AST tmp113_AST = null;
			tmp113_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp113_AST);
			match(COLON);
			mlm_date();
			astFactory.addASTChild(currentAST, returnAST);
			AST tmp114_AST = null;
			tmp114_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp114_AST);
			match(ENDBLOCK);
			date_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_12);
			} else {
			  throw ex;
			}
		}
		returnAST = date_slot_AST;
	}
	
	public final void validation_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST validation_slot_AST = null;
		
		try {      // for error handling
			AST tmp115_AST = null;
			tmp115_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp115_AST);
			match(LITERAL_validation);
			AST tmp116_AST = null;
			tmp116_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp116_AST);
			match(COLON);
			validation_code();
			astFactory.addASTChild(currentAST, returnAST);
			AST tmp117_AST = null;
			tmp117_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp117_AST);
			match(ENDBLOCK);
			validation_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_1);
			} else {
			  throw ex;
			}
		}
		returnAST = validation_slot_AST;
	}
	
	public final void library_body() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST library_body_AST = null;
		
		try {      // for error handling
			purpose_slot();
			astFactory.addASTChild(currentAST, returnAST);
			explanation_slot();
			astFactory.addASTChild(currentAST, returnAST);
			keywords_slot();
			astFactory.addASTChild(currentAST, returnAST);
			citations_slot();
			astFactory.addASTChild(currentAST, returnAST);
			links_slot();
			astFactory.addASTChild(currentAST, returnAST);
			library_body_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_2);
			} else {
			  throw ex;
			}
		}
		returnAST = library_body_AST;
	}
	
/*****************Library slots*********************************/
	public final void purpose_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST purpose_slot_AST = null;
		
		try {      // for error handling
			AST tmp118_AST = null;
			tmp118_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp118_AST);
			match(LITERAL_purpose);
			AST tmp119_AST = null;
			tmp119_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp119_AST);
			match(COLON);
			{
			_loop68:
			do {
				if ((_tokenSet_4.member(LA(1)))) {
					text();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop68;
				}
				
			} while (true);
			}
			AST tmp120_AST = null;
			tmp120_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp120_AST);
			match(ENDBLOCK);
			purpose_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_13);
			} else {
			  throw ex;
			}
		}
		returnAST = purpose_slot_AST;
	}
	
	public final void explanation_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST explanation_slot_AST = null;
		
		try {      // for error handling
			AST tmp121_AST = null;
			tmp121_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp121_AST);
			match(LITERAL_explanation);
			AST tmp122_AST = null;
			tmp122_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp122_AST);
			match(COLON);
			{
			_loop71:
			do {
				if ((_tokenSet_4.member(LA(1)))) {
					text();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else if ((LA(1)==INTLIT)) {
					AST tmp123_AST = null;
					tmp123_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp123_AST);
					match(INTLIT);
				}
				else {
					break _loop71;
				}
				
			} while (true);
			}
			AST tmp124_AST = null;
			tmp124_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp124_AST);
			match(ENDBLOCK);
			explanation_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_14);
			} else {
			  throw ex;
			}
		}
		returnAST = explanation_slot_AST;
	}
	
	public final void keywords_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST keywords_slot_AST = null;
		
		try {      // for error handling
			AST tmp125_AST = null;
			tmp125_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp125_AST);
			match(LITERAL_keywords);
			AST tmp126_AST = null;
			tmp126_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp126_AST);
			match(COLON);
			{
			keyword_text();
			astFactory.addASTChild(currentAST, returnAST);
			}
			keywords_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_15);
			} else {
			  throw ex;
			}
		}
		returnAST = keywords_slot_AST;
	}
	
	public final void citations_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST citations_slot_AST = null;
		
		try {      // for error handling
			if ((LA(1)==LITERAL_knowledge||LA(1)==LITERAL_links)) {
				citations_slot_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==LITERAL_knowledge||LA(1)==LITERAL_links)) {
				citations_slot_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==LITERAL_citations)) {
				AST tmp127_AST = null;
				tmp127_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp127_AST);
				match(LITERAL_citations);
				AST tmp128_AST = null;
				tmp128_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp128_AST);
				match(COLON);
				{
				citations_list();
				astFactory.addASTChild(currentAST, returnAST);
				}
				AST tmp129_AST = null;
				tmp129_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp129_AST);
				match(ENDBLOCK);
				citations_slot_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_16);
			} else {
			  throw ex;
			}
		}
		returnAST = citations_slot_AST;
	}
	
	public final void links_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST links_slot_AST = null;
		
		try {      // for error handling
			if ((LA(1)==LITERAL_knowledge)) {
				links_slot_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==LITERAL_knowledge)) {
				links_slot_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==LITERAL_links)) {
				AST tmp130_AST = null;
				tmp130_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp130_AST);
				match(LITERAL_links);
				AST tmp131_AST = null;
				tmp131_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp131_AST);
				match(COLON);
				link_body();
				astFactory.addASTChild(currentAST, returnAST);
				AST tmp132_AST = null;
				tmp132_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp132_AST);
				match(ENDBLOCK);
				links_slot_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_2);
			} else {
			  throw ex;
			}
		}
		returnAST = links_slot_AST;
	}
	
	public final void knowledge_body() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST knowledge_body_AST = null;
		
		try {      // for error handling
			type_slot();
			data_slot();
			astFactory.addASTChild(currentAST, returnAST);
			priority_slot();
			evoke_slot();
			logic_slot();
			astFactory.addASTChild(currentAST, returnAST);
			action_slot();
			astFactory.addASTChild(currentAST, returnAST);
			urgency_slot();
			knowledge_body_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_3);
			} else {
			  throw ex;
			}
		}
		returnAST = knowledge_body_AST;
	}
	
	public final void type_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST type_slot_AST = null;
		
		try {      // for error handling
			AST tmp133_AST = null;
			tmp133_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp133_AST);
			match(LITERAL_type);
			AST tmp134_AST = null;
			tmp134_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp134_AST);
			match(COLON);
			type_code();
			astFactory.addASTChild(currentAST, returnAST);
			AST tmp135_AST = null;
			tmp135_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp135_AST);
			match(ENDBLOCK);
			type_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_17);
			} else {
			  throw ex;
			}
		}
		returnAST = type_slot_AST;
	}
	
	public final void data_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST data_slot_AST = null;
		
		try {      // for error handling
			AST tmp136_AST = null;
			tmp136_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp136_AST);
			match(DATA);
			match(COLON);
			{
			_loop112:
			do {
				if ((_tokenSet_18.member(LA(1)))) {
					data_statement();
					astFactory.addASTChild(currentAST, returnAST);
					match(SEMI);
				}
				else {
					break _loop112;
				}
				
			} while (true);
			}
			AST tmp139_AST = null;
			tmp139_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp139_AST);
			match(ENDBLOCK);
			data_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_19);
			} else {
			  throw ex;
			}
		}
		returnAST = data_slot_AST;
	}
	
	public final void priority_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST priority_slot_AST = null;
		
		try {      // for error handling
			if ((LA(1)==LITERAL_evoke)) {
				priority_slot_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==LITERAL_evoke)) {
				priority_slot_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==LITERAL_priority)) {
				AST tmp140_AST = null;
				tmp140_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp140_AST);
				match(LITERAL_priority);
				AST tmp141_AST = null;
				tmp141_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp141_AST);
				match(COLON);
				AST tmp142_AST = null;
				tmp142_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp142_AST);
				match(INTLIT);
				AST tmp143_AST = null;
				tmp143_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp143_AST);
				match(ENDBLOCK);
				priority_slot_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_20);
			} else {
			  throw ex;
			}
		}
		returnAST = priority_slot_AST;
	}
	
	public final void evoke_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST evoke_slot_AST = null;
		
		try {      // for error handling
			AST tmp144_AST = null;
			tmp144_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp144_AST);
			match(LITERAL_evoke);
			AST tmp145_AST = null;
			tmp145_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp145_AST);
			match(COLON);
			{
			evoke_statement();
			astFactory.addASTChild(currentAST, returnAST);
			}
			AST tmp146_AST = null;
			tmp146_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp146_AST);
			match(ENDBLOCK);
			evoke_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_21);
			} else {
			  throw ex;
			}
		}
		returnAST = evoke_slot_AST;
	}
	
	public final void logic_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logic_slot_AST = null;
		
		try {      // for error handling
			AST tmp147_AST = null;
			tmp147_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp147_AST);
			match(LOGIC);
			match(COLON);
			{
			_loop213:
			do {
				if ((_tokenSet_22.member(LA(1)))) {
					logic_statement();
					astFactory.addASTChild(currentAST, returnAST);
					match(SEMI);
				}
				else {
					break _loop213;
				}
				
			} while (true);
			}
			AST tmp150_AST = null;
			tmp150_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp150_AST);
			match(ENDBLOCK);
			logic_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_23);
			} else {
			  throw ex;
			}
		}
		returnAST = logic_slot_AST;
	}
	
	public final void action_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST action_slot_AST = null;
		
		try {      // for error handling
			AST tmp151_AST = null;
			tmp151_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp151_AST);
			match(ACTION);
			match(COLON);
			{
			_loop246:
			do {
				if ((LA(1)==WRITE)) {
					action_statement();
					astFactory.addASTChild(currentAST, returnAST);
					match(SEMI);
				}
				else {
					break _loop246;
				}
				
			} while (true);
			}
			AST tmp154_AST = null;
			tmp154_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp154_AST);
			match(ENDBLOCK);
			action_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_24);
			} else {
			  throw ex;
			}
		}
		returnAST = action_slot_AST;
	}
	
	public final void urgency_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST urgency_slot_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_end:
			{
				urgency_slot_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_urgency:
			{
				AST tmp155_AST = null;
				tmp155_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp155_AST);
				match(LITERAL_urgency);
				AST tmp156_AST = null;
				tmp156_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp156_AST);
				match(COLON);
				urgency_val();
				astFactory.addASTChild(currentAST, returnAST);
				AST tmp157_AST = null;
				tmp157_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp157_AST);
				match(ENDBLOCK);
				urgency_slot_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_3);
			} else {
			  throw ex;
			}
		}
		returnAST = urgency_slot_AST;
	}
	
	public final void text() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST text_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ID:
			{
				AST tmp158_AST = null;
				tmp158_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp158_AST);
				match(ID);
				text_AST = (AST)currentAST.root;
				break;
			}
			case AND:
			case IS:
			case ARE:
			case WAS:
			case WERE:
			case COUNT:
			case IN:
			case LESS:
			case THE:
			case THAN:
			case FROM:
			case BEFORE:
			case AFTER:
			case AGO:
			case WRITE:
			case AT:
			case LET:
			case BE:
			case YEAR:
			case YEARS:
			case IF:
			case IT:
			case THEY:
			case NOT:
			case OR:
			case THEN:
			case READ:
			case MINIMUM:
			case MIN:
			case MAXIMUM:
			case MAX:
			case LAST:
			case FIRST:
			case EARLIEST:
			case LATEST:
			case EVENT:
			case WHERE:
			case EXIST:
			case EXISTS:
			case PAST:
			case MONTH:
			case MONTHS:
			case AVG:
			case AVERAGE:
			case SUM:
			case MEDIAN:
			case CONCLUDE:
			case ELSE:
			case ELSEIF:
			case ENDIF:
			case TRUE:
			case FALSE:
			case DATA:
			case LOGIC:
			case ACTION:
			case OF:
			case TIME:
			case WITHIN:
			case TIMES:
			{
				{
				any_reserved_word();
				astFactory.addASTChild(currentAST, returnAST);
				}
				text_AST = (AST)currentAST.root;
				break;
			}
			case INTLIT:
			{
				AST tmp159_AST = null;
				tmp159_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp159_AST);
				match(INTLIT);
				text_AST = (AST)currentAST.root;
				break;
			}
			case LPAREN:
			{
				{
				AST tmp160_AST = null;
				tmp160_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp160_AST);
				match(LPAREN);
				{
				_loop53:
				do {
					switch ( LA(1)) {
					case ID:
					{
						AST tmp161_AST = null;
						tmp161_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp161_AST);
						match(ID);
						break;
					}
					case INTLIT:
					{
						AST tmp162_AST = null;
						tmp162_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp162_AST);
						match(INTLIT);
						break;
					}
					case AND:
					case IS:
					case ARE:
					case WAS:
					case WERE:
					case COUNT:
					case IN:
					case LESS:
					case THE:
					case THAN:
					case FROM:
					case BEFORE:
					case AFTER:
					case AGO:
					case WRITE:
					case AT:
					case LET:
					case BE:
					case YEAR:
					case YEARS:
					case IF:
					case IT:
					case THEY:
					case NOT:
					case OR:
					case THEN:
					case READ:
					case MINIMUM:
					case MIN:
					case MAXIMUM:
					case MAX:
					case LAST:
					case FIRST:
					case EARLIEST:
					case LATEST:
					case EVENT:
					case WHERE:
					case EXIST:
					case EXISTS:
					case PAST:
					case MONTH:
					case MONTHS:
					case AVG:
					case AVERAGE:
					case SUM:
					case MEDIAN:
					case CONCLUDE:
					case ELSE:
					case ELSEIF:
					case ENDIF:
					case TRUE:
					case FALSE:
					case DATA:
					case LOGIC:
					case ACTION:
					case OF:
					case TIME:
					case WITHIN:
					case TIMES:
					{
						{
						any_reserved_word();
						astFactory.addASTChild(currentAST, returnAST);
						}
						break;
					}
					default:
					{
						break _loop53;
					}
					}
				} while (true);
				}
				AST tmp163_AST = null;
				tmp163_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp163_AST);
				match(RPAREN);
				}
				text_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
				text_AST = handleIdentifierError(LT(1),ex);
				
			} else {
				throw ex;
			}
		}
		returnAST = text_AST;
	}
	
	public final void mlmname_text() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST mlmname_text_AST = null;
		
		try {      // for error handling
			text();
			astFactory.addASTChild(currentAST, returnAST);
			{
			mlmname_text_rest();
			astFactory.addASTChild(currentAST, returnAST);
			}
			mlmname_text_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_6);
			} else {
			  throw ex;
			}
		}
		returnAST = mlmname_text_AST;
	}
	
	public final void mlmname_text_rest() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST mlmname_text_rest_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_arden:
			case LITERAL_version:
			{
				mlmname_text_rest_AST = (AST)currentAST.root;
				break;
			}
			case DOT:
			{
				AST tmp164_AST = null;
				tmp164_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp164_AST);
				match(DOT);
				{
				_loop18:
				do {
					if ((_tokenSet_4.member(LA(1)))) {
						text();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop18;
					}
					
				} while (true);
				}
				AST tmp165_AST = null;
				tmp165_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp165_AST);
				match(ENDBLOCK);
				mlmname_text_rest_AST = (AST)currentAST.root;
				break;
			}
			case MINUS:
			{
				AST tmp166_AST = null;
				tmp166_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp166_AST);
				match(MINUS);
				{
				_loop20:
				do {
					if ((_tokenSet_4.member(LA(1)))) {
						text();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop20;
					}
					
				} while (true);
				}
				AST tmp167_AST = null;
				tmp167_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp167_AST);
				match(ENDBLOCK);
				mlmname_text_rest_AST = (AST)currentAST.root;
				break;
			}
			case UNDERSCORE:
			{
				AST tmp168_AST = null;
				tmp168_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp168_AST);
				match(UNDERSCORE);
				{
				_loop22:
				do {
					if ((_tokenSet_4.member(LA(1)))) {
						text();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop22;
					}
					
				} while (true);
				}
				AST tmp169_AST = null;
				tmp169_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp169_AST);
				match(ENDBLOCK);
				mlmname_text_rest_AST = (AST)currentAST.root;
				break;
			}
			case ENDBLOCK:
			{
				AST tmp170_AST = null;
				tmp170_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp170_AST);
				match(ENDBLOCK);
				mlmname_text_rest_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_6);
			} else {
			  throw ex;
			}
		}
		returnAST = mlmname_text_rest_AST;
	}
	
	public final void version_num() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST version_num_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case INTLIT:
			{
				AST tmp171_AST = null;
				tmp171_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp171_AST);
				match(INTLIT);
				version_num_AST = (AST)currentAST.root;
				break;
			}
			case DIGIT:
			{
				AST tmp172_AST = null;
				tmp172_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp172_AST);
				match(DIGIT);
				AST tmp173_AST = null;
				tmp173_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp173_AST);
				match(DOT);
				AST tmp174_AST = null;
				tmp174_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp174_AST);
				match(DIGIT);
				version_num_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_25);
			} else {
			  throw ex;
			}
		}
		returnAST = version_num_AST;
	}
	
	public final void mlm_version() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST mlm_version_AST = null;
		
		try {      // for error handling
			text();
			astFactory.addASTChild(currentAST, returnAST);
			mlm_version_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = mlm_version_AST;
	}
	
	public final void mlm_date() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST mlm_date_AST = null;
		
		try {      // for error handling
			iso_date_time();
			astFactory.addASTChild(currentAST, returnAST);
			mlm_date_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_25);
			} else {
			  throw ex;
			}
		}
		returnAST = mlm_date_AST;
	}
	
	public final void iso_date_time() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST iso_date_time_AST = null;
		
		try {      // for error handling
			datepart();
			astFactory.addASTChild(currentAST, returnAST);
			iso_date_time_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
		returnAST = iso_date_time_AST;
	}
	
	public final void validation_code() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST validation_code_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_production:
			{
				AST tmp175_AST = null;
				tmp175_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp175_AST);
				match(LITERAL_production);
				validation_code_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_research:
			{
				AST tmp176_AST = null;
				tmp176_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp176_AST);
				match(LITERAL_research);
				validation_code_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_testing:
			{
				AST tmp177_AST = null;
				tmp177_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp177_AST);
				match(LITERAL_testing);
				validation_code_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_expired:
			{
				AST tmp178_AST = null;
				tmp178_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp178_AST);
				match(LITERAL_expired);
				validation_code_AST = (AST)currentAST.root;
				break;
			}
			case ENDBLOCK:
			{
				validation_code_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_25);
			} else {
			  throw ex;
			}
		}
		returnAST = validation_code_AST;
	}
	
	public final void any_reserved_word() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST any_reserved_word_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case AND:
			{
				AST tmp179_AST = null;
				tmp179_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp179_AST);
				match(AND);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case IS:
			{
				AST tmp180_AST = null;
				tmp180_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp180_AST);
				match(IS);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case ARE:
			{
				AST tmp181_AST = null;
				tmp181_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp181_AST);
				match(ARE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case WAS:
			{
				AST tmp182_AST = null;
				tmp182_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp182_AST);
				match(WAS);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case WERE:
			{
				AST tmp183_AST = null;
				tmp183_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp183_AST);
				match(WERE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case COUNT:
			{
				AST tmp184_AST = null;
				tmp184_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp184_AST);
				match(COUNT);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case IN:
			{
				AST tmp185_AST = null;
				tmp185_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp185_AST);
				match(IN);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case LESS:
			{
				AST tmp186_AST = null;
				tmp186_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp186_AST);
				match(LESS);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case THE:
			{
				AST tmp187_AST = null;
				tmp187_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp187_AST);
				match(THE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case THAN:
			{
				AST tmp188_AST = null;
				tmp188_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp188_AST);
				match(THAN);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case FROM:
			{
				AST tmp189_AST = null;
				tmp189_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp189_AST);
				match(FROM);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case BEFORE:
			{
				AST tmp190_AST = null;
				tmp190_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp190_AST);
				match(BEFORE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case AFTER:
			{
				AST tmp191_AST = null;
				tmp191_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp191_AST);
				match(AFTER);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case AGO:
			{
				AST tmp192_AST = null;
				tmp192_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp192_AST);
				match(AGO);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case AT:
			{
				AST tmp193_AST = null;
				tmp193_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp193_AST);
				match(AT);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case OF:
			{
				AST tmp194_AST = null;
				tmp194_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp194_AST);
				match(OF);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case WRITE:
			{
				AST tmp195_AST = null;
				tmp195_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp195_AST);
				match(WRITE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case BE:
			{
				AST tmp196_AST = null;
				tmp196_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp196_AST);
				match(BE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case LET:
			{
				AST tmp197_AST = null;
				tmp197_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp197_AST);
				match(LET);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case YEAR:
			{
				AST tmp198_AST = null;
				tmp198_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp198_AST);
				match(YEAR);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case YEARS:
			{
				AST tmp199_AST = null;
				tmp199_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp199_AST);
				match(YEARS);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case IF:
			{
				AST tmp200_AST = null;
				tmp200_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp200_AST);
				match(IF);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case IT:
			{
				AST tmp201_AST = null;
				tmp201_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp201_AST);
				match(IT);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case THEY:
			{
				AST tmp202_AST = null;
				tmp202_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp202_AST);
				match(THEY);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case NOT:
			{
				AST tmp203_AST = null;
				tmp203_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp203_AST);
				match(NOT);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case OR:
			{
				AST tmp204_AST = null;
				tmp204_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp204_AST);
				match(OR);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case THEN:
			{
				AST tmp205_AST = null;
				tmp205_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp205_AST);
				match(THEN);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case MONTH:
			{
				AST tmp206_AST = null;
				tmp206_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp206_AST);
				match(MONTH);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case MONTHS:
			{
				AST tmp207_AST = null;
				tmp207_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp207_AST);
				match(MONTHS);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case TIME:
			{
				AST tmp208_AST = null;
				tmp208_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp208_AST);
				match(TIME);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case TIMES:
			{
				AST tmp209_AST = null;
				tmp209_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp209_AST);
				match(TIMES);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case WITHIN:
			{
				AST tmp210_AST = null;
				tmp210_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp210_AST);
				match(WITHIN);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case READ:
			{
				AST tmp211_AST = null;
				tmp211_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp211_AST);
				match(READ);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case MINIMUM:
			{
				AST tmp212_AST = null;
				tmp212_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp212_AST);
				match(MINIMUM);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case MIN:
			{
				AST tmp213_AST = null;
				tmp213_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp213_AST);
				match(MIN);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case MAXIMUM:
			{
				AST tmp214_AST = null;
				tmp214_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp214_AST);
				match(MAXIMUM);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case MAX:
			{
				AST tmp215_AST = null;
				tmp215_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp215_AST);
				match(MAX);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case LAST:
			{
				AST tmp216_AST = null;
				tmp216_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp216_AST);
				match(LAST);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case FIRST:
			{
				AST tmp217_AST = null;
				tmp217_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp217_AST);
				match(FIRST);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case EARLIEST:
			{
				AST tmp218_AST = null;
				tmp218_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp218_AST);
				match(EARLIEST);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case LATEST:
			{
				AST tmp219_AST = null;
				tmp219_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp219_AST);
				match(LATEST);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case EVENT:
			{
				AST tmp220_AST = null;
				tmp220_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp220_AST);
				match(EVENT);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case WHERE:
			{
				AST tmp221_AST = null;
				tmp221_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp221_AST);
				match(WHERE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case EXIST:
			{
				AST tmp222_AST = null;
				tmp222_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp222_AST);
				match(EXIST);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case EXISTS:
			{
				AST tmp223_AST = null;
				tmp223_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp223_AST);
				match(EXISTS);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case PAST:
			{
				AST tmp224_AST = null;
				tmp224_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp224_AST);
				match(PAST);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case AVERAGE:
			{
				AST tmp225_AST = null;
				tmp225_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp225_AST);
				match(AVERAGE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case AVG:
			{
				AST tmp226_AST = null;
				tmp226_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp226_AST);
				match(AVG);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case SUM:
			{
				AST tmp227_AST = null;
				tmp227_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp227_AST);
				match(SUM);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case MEDIAN:
			{
				AST tmp228_AST = null;
				tmp228_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp228_AST);
				match(MEDIAN);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case CONCLUDE:
			{
				AST tmp229_AST = null;
				tmp229_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp229_AST);
				match(CONCLUDE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case ELSE:
			{
				AST tmp230_AST = null;
				tmp230_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp230_AST);
				match(ELSE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case ELSEIF:
			{
				AST tmp231_AST = null;
				tmp231_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp231_AST);
				match(ELSEIF);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case ENDIF:
			{
				AST tmp232_AST = null;
				tmp232_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp232_AST);
				match(ENDIF);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case TRUE:
			{
				AST tmp233_AST = null;
				tmp233_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp233_AST);
				match(TRUE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case FALSE:
			{
				AST tmp234_AST = null;
				tmp234_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp234_AST);
				match(FALSE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case DATA:
			{
				AST tmp235_AST = null;
				tmp235_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp235_AST);
				match(DATA);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case LOGIC:
			{
				AST tmp236_AST = null;
				tmp236_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp236_AST);
				match(LOGIC);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case ACTION:
			{
				AST tmp237_AST = null;
				tmp237_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp237_AST);
				match(ACTION);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_27);
			} else {
			  throw ex;
			}
		}
		returnAST = any_reserved_word_AST;
	}
	
	public final void datepart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST datepart_AST = null;
		
		try {      // for error handling
			{
			AST tmp238_AST = null;
			tmp238_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp238_AST);
			match(INTLIT);
			{
			int _cnt58=0;
			_loop58:
			do {
				if ((LA(1)==MINUS)) {
					AST tmp239_AST = null;
					tmp239_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp239_AST);
					match(MINUS);
					AST tmp240_AST = null;
					tmp240_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp240_AST);
					match(INTLIT);
				}
				else {
					if ( _cnt58>=1 ) { break _loop58; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt58++;
			} while (true);
			}
			}
			timepart_opt();
			astFactory.addASTChild(currentAST, returnAST);
			datepart_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
		returnAST = datepart_AST;
	}
	
	public final void timepart_opt() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST timepart_opt_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case EOF:
			case AND:
			case IS:
			case ARE:
			case WAS:
			case WERE:
			case IN:
			case LESS:
			case FROM:
			case BEFORE:
			case AFTER:
			case AGO:
			case AT:
			case LET:
			case NOW:
			case YEAR:
			case YEARS:
			case IF:
			case NOT:
			case OR:
			case THEN:
			case MINIMUM:
			case MIN:
			case MAXIMUM:
			case MAX:
			case LAST:
			case FIRST:
			case EARLIEST:
			case LATEST:
			case WHERE:
			case DAYS:
			case DAY:
			case MONTH:
			case MONTHS:
			case WEEK:
			case WEEKS:
			case CONCLUDE:
			case ELSE:
			case ELSEIF:
			case ENDIF:
			case ENDBLOCK:
			case DOT:
			case SEMI:
			case ID:
			case LPAREN:
			case RPAREN:
			case 103:
			case 104:
			case COMMA:
			case ARDEN_CURLY_BRACKETS:
			case LITERAL_GREATER:
			case LITERAL_hour:
			case LITERAL_hours:
			case LITERAL_minute:
			case LITERAL_minutes:
			case LITERAL_second:
			case LITERAL_seconds:
			case EQUALS:
			case LITERAL_EQ:
			case LT:
			case LITERAL_LT:
			case GT:
			case LITERAL_GT:
			case LTE:
			case LITERAL_LE:
			case GTE:
			case LITERAL_GE:
			case NE:
			case LITERAL_NE:
			case ACTION_OP:
			case LITERAL_MERGE:
			case LITERAL_SORT:
			case LITERAL_SEQTO:
			case 171:
			case 172:
			{
				timepart_opt_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_T:
			case LITERAL_t:
			{
				{
				timepart();
				astFactory.addASTChild(currentAST, returnAST);
				}
				AST tmp241_AST = null;
				tmp241_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp241_AST);
				match(ENDBLOCK);
				timepart_opt_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
		returnAST = timepart_opt_AST;
	}
	
	public final void timepart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST timepart_AST = null;
		
		try {      // for error handling
			time();
			astFactory.addASTChild(currentAST, returnAST);
			AST tmp242_AST = null;
			tmp242_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp242_AST);
			match(INTLIT);
			AST tmp243_AST = null;
			tmp243_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp243_AST);
			match(99);
			AST tmp244_AST = null;
			tmp244_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp244_AST);
			match(INTLIT);
			AST tmp245_AST = null;
			tmp245_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp245_AST);
			match(99);
			AST tmp246_AST = null;
			tmp246_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp246_AST);
			match(INTLIT);
			fractional_seconds();
			astFactory.addASTChild(currentAST, returnAST);
			time_zone();
			astFactory.addASTChild(currentAST, returnAST);
			AST tmp247_AST = null;
			tmp247_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp247_AST);
			match(ENDBLOCK);
			timepart_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_25);
			} else {
			  throw ex;
			}
		}
		returnAST = timepart_AST;
	}
	
	public final void time() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST time_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_T:
			{
				AST tmp248_AST = null;
				tmp248_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp248_AST);
				match(LITERAL_T);
				time_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_t:
			{
				AST tmp249_AST = null;
				tmp249_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp249_AST);
				match(LITERAL_t);
				time_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_28);
			} else {
			  throw ex;
			}
		}
		returnAST = time_AST;
	}
	
	public final void fractional_seconds() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST fractional_seconds_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case 102:
			{
				AST tmp250_AST = null;
				tmp250_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp250_AST);
				match(102);
				AST tmp251_AST = null;
				tmp251_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp251_AST);
				match(DIGIT);
				fractional_seconds_AST = (AST)currentAST.root;
				break;
			}
			case ENDBLOCK:
			case 103:
			case 104:
			case LITERAL_Z:
			case LITERAL_z:
			{
				fractional_seconds_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_29);
			} else {
			  throw ex;
			}
		}
		returnAST = fractional_seconds_AST;
	}
	
	public final void time_zone() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST time_zone_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ENDBLOCK:
			{
				time_zone_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_Z:
			case LITERAL_z:
			{
				zulu();
				astFactory.addASTChild(currentAST, returnAST);
				time_zone_AST = (AST)currentAST.root;
				break;
			}
			case 103:
			{
				AST tmp252_AST = null;
				tmp252_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp252_AST);
				match(103);
				AST tmp253_AST = null;
				tmp253_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp253_AST);
				match(DIGIT);
				AST tmp254_AST = null;
				tmp254_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp254_AST);
				match(DIGIT);
				AST tmp255_AST = null;
				tmp255_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp255_AST);
				match(99);
				AST tmp256_AST = null;
				tmp256_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp256_AST);
				match(DIGIT);
				AST tmp257_AST = null;
				tmp257_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp257_AST);
				match(DIGIT);
				time_zone_AST = (AST)currentAST.root;
				break;
			}
			case 104:
			{
				AST tmp258_AST = null;
				tmp258_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp258_AST);
				match(104);
				AST tmp259_AST = null;
				tmp259_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp259_AST);
				match(DIGIT);
				AST tmp260_AST = null;
				tmp260_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp260_AST);
				match(DIGIT);
				AST tmp261_AST = null;
				tmp261_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp261_AST);
				match(99);
				AST tmp262_AST = null;
				tmp262_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp262_AST);
				match(DIGIT);
				AST tmp263_AST = null;
				tmp263_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp263_AST);
				match(DIGIT);
				time_zone_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_25);
			} else {
			  throw ex;
			}
		}
		returnAST = time_zone_AST;
	}
	
	public final void zulu() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST zulu_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_Z:
			{
				AST tmp264_AST = null;
				tmp264_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp264_AST);
				match(LITERAL_Z);
				zulu_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_z:
			{
				AST tmp265_AST = null;
				tmp265_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp265_AST);
				match(LITERAL_z);
				zulu_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_25);
			} else {
			  throw ex;
			}
		}
		returnAST = zulu_AST;
	}
	
	public final void keyword_text() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST keyword_text_AST = null;
		
		try {      // for error handling
			{
			_loop76:
			do {
				if ((_tokenSet_4.member(LA(1)))) {
					text();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop76;
				}
				
			} while (true);
			}
			{
			_loop80:
			do {
				if ((LA(1)==SEMI)) {
					AST tmp266_AST = null;
					tmp266_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp266_AST);
					match(SEMI);
					{
					_loop79:
					do {
						if ((_tokenSet_4.member(LA(1)))) {
							text();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else {
							break _loop79;
						}
						
					} while (true);
					}
				}
				else {
					break _loop80;
				}
				
			} while (true);
			}
			AST tmp267_AST = null;
			tmp267_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp267_AST);
			match(ENDBLOCK);
			keyword_text_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_15);
			} else {
			  throw ex;
			}
		}
		returnAST = keyword_text_AST;
	}
	
	public final void citations_list() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST citations_list_AST = null;
		
		try {      // for error handling
			if ((LA(1)==ENDBLOCK)) {
				citations_list_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==ENDBLOCK)) {
				citations_list_AST = (AST)currentAST.root;
			}
			else if ((_tokenSet_30.member(LA(1)))) {
				single_citation();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop85:
				do {
					if ((LA(1)==SEMI)) {
						AST tmp268_AST = null;
						tmp268_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp268_AST);
						match(SEMI);
						single_citation();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop85;
					}
					
				} while (true);
				}
				citations_list_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_25);
			} else {
			  throw ex;
			}
		}
		returnAST = citations_list_AST;
	}
	
	public final void single_citation() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST single_citation_AST = null;
		
		try {      // for error handling
			{
			_loop88:
			do {
				if ((LA(1)==INTLIT)) {
					AST tmp269_AST = null;
					tmp269_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp269_AST);
					match(INTLIT);
					AST tmp270_AST = null;
					tmp270_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp270_AST);
					match(DOT);
					citation_type();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop88;
				}
				
			} while (true);
			}
			citation_text();
			astFactory.addASTChild(currentAST, returnAST);
			single_citation_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_31);
			} else {
			  throw ex;
			}
		}
		returnAST = single_citation_AST;
	}
	
	public final void citation_type() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST citation_type_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_SUPPORT:
			{
				AST tmp271_AST = null;
				tmp271_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp271_AST);
				match(LITERAL_SUPPORT);
				citation_type_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_REFUTE:
			{
				AST tmp272_AST = null;
				tmp272_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp272_AST);
				match(LITERAL_REFUTE);
				citation_type_AST = (AST)currentAST.root;
				break;
			}
			default:
				if ((_tokenSet_30.member(LA(1)))) {
					citation_type_AST = (AST)currentAST.root;
				}
				else if ((_tokenSet_30.member(LA(1)))) {
					citation_type_AST = (AST)currentAST.root;
				}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_30);
			} else {
			  throw ex;
			}
		}
		returnAST = citation_type_AST;
	}
	
	public final void citation_text() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST citation_text_AST = null;
		
		try {      // for error handling
			{
			_loop91:
			do {
				if ((_tokenSet_4.member(LA(1)))) {
					text();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else if ((LA(1)==INTLIT)) {
					AST tmp273_AST = null;
					tmp273_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp273_AST);
					match(INTLIT);
				}
				else {
					break _loop91;
				}
				
			} while (true);
			}
			{
			_loop99:
			do {
				if ((LA(1)==COLON)) {
					AST tmp274_AST = null;
					tmp274_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp274_AST);
					match(COLON);
					{
					_loop94:
					do {
						if ((_tokenSet_4.member(LA(1)))) {
							text();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else {
							break _loop94;
						}
						
					} while (true);
					}
					{
					switch ( LA(1)) {
					case MINUS:
					{
						AST tmp275_AST = null;
						tmp275_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp275_AST);
						match(MINUS);
						AST tmp276_AST = null;
						tmp276_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp276_AST);
						match(INTLIT);
						break;
					}
					case AND:
					case IS:
					case ARE:
					case WAS:
					case WERE:
					case COUNT:
					case IN:
					case LESS:
					case THE:
					case THAN:
					case FROM:
					case BEFORE:
					case AFTER:
					case AGO:
					case WRITE:
					case AT:
					case LET:
					case BE:
					case YEAR:
					case YEARS:
					case IF:
					case IT:
					case THEY:
					case NOT:
					case OR:
					case THEN:
					case READ:
					case MINIMUM:
					case MIN:
					case MAXIMUM:
					case MAX:
					case LAST:
					case FIRST:
					case EARLIEST:
					case LATEST:
					case EVENT:
					case WHERE:
					case EXIST:
					case EXISTS:
					case PAST:
					case MONTH:
					case MONTHS:
					case AVG:
					case AVERAGE:
					case SUM:
					case MEDIAN:
					case CONCLUDE:
					case ELSE:
					case ELSEIF:
					case ENDIF:
					case TRUE:
					case FALSE:
					case DATA:
					case LOGIC:
					case ACTION:
					case OF:
					case TIME:
					case WITHIN:
					case COLON:
					case ENDBLOCK:
					case DOT:
					case INTLIT:
					case SEMI:
					case TIMES:
					case ID:
					case LPAREN:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					{
					_loop97:
					do {
						if ((_tokenSet_4.member(LA(1)))) {
							text();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else {
							break _loop97;
						}
						
					} while (true);
					}
					{
					switch ( LA(1)) {
					case DOT:
					{
						AST tmp277_AST = null;
						tmp277_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp277_AST);
						match(DOT);
						break;
					}
					case COLON:
					case ENDBLOCK:
					case SEMI:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
				}
				else {
					break _loop99;
				}
				
			} while (true);
			}
			citation_text_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_31);
			} else {
			  throw ex;
			}
		}
		returnAST = citation_text_AST;
	}
	
	public final void link_body() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST link_body_AST = null;
		
		try {      // for error handling
			{
			_loop107:
			do {
				if ((LA(1)==SINGLE_QUOTE)) {
					AST tmp278_AST = null;
					tmp278_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp278_AST);
					match(SINGLE_QUOTE);
					{
					_loop105:
					do {
						if ((_tokenSet_4.member(LA(1)))) {
							text();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else if ((LA(1)==DOT)) {
							AST tmp279_AST = null;
							tmp279_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp279_AST);
							match(DOT);
						}
						else if ((LA(1)==INTLIT)) {
							AST tmp280_AST = null;
							tmp280_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp280_AST);
							match(INTLIT);
						}
						else {
							break _loop105;
						}
						
					} while (true);
					}
					AST tmp281_AST = null;
					tmp281_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp281_AST);
					match(SINGLE_QUOTE);
					{
					switch ( LA(1)) {
					case SEMI:
					{
						AST tmp282_AST = null;
						tmp282_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp282_AST);
						match(SEMI);
						break;
					}
					case ENDBLOCK:
					case SINGLE_QUOTE:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
				}
				else {
					break _loop107;
				}
				
			} while (true);
			}
			link_body_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_25);
			} else {
			  throw ex;
			}
		}
		returnAST = link_body_AST;
	}
	
	public final void type_code() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST type_code_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case 116:
			{
				AST tmp283_AST = null;
				tmp283_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp283_AST);
				match(116);
				type_code_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_data_driven:
			{
				AST tmp284_AST = null;
				tmp284_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp284_AST);
				match(LITERAL_data_driven);
				type_code_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_25);
			} else {
			  throw ex;
			}
		}
		returnAST = type_code_AST;
	}
	
	public final void data_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST data_statement_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case IF:
			{
				{
				data_if_statement();
				astFactory.addASTChild(currentAST, returnAST);
				}
				break;
			}
			case LET:
			case NOW:
			case SEMI:
			case ID:
			{
				data_assignment();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case ELSE:
			case ELSEIF:
			case ENDIF:
			{
				data_elseif();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			data_statement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_32);
			} else {
			  throw ex;
			}
		}
		returnAST = data_statement_AST;
	}
	
	public final void data_if_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST data_if_statement_AST = null;
		
		try {      // for error handling
			AST tmp285_AST = null;
			tmp285_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp285_AST);
			match(IF);
			data_if_then_else2();
			astFactory.addASTChild(currentAST, returnAST);
			data_if_statement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_32);
			} else {
			  throw ex;
			}
		}
		returnAST = data_if_statement_AST;
	}
	
	public final void data_assignment() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST data_assignment_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LET:
			case NOW:
			case ID:
			{
				identifier_becomes();
				astFactory.addASTChild(currentAST, returnAST);
				{
				switch ( LA(1)) {
				case READ:
				{
					{
					AST tmp286_AST = null;
					tmp286_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp286_AST);
					match(READ);
					}
					{
					{
					if ((_tokenSet_33.member(LA(1)))) {
						{
						of_read_func_op();
						astFactory.addASTChild(currentAST, returnAST);
						}
					}
					else if ((_tokenSet_34.member(LA(1)))) {
						{
						from_of_func_op();
						astFactory.addASTChild(currentAST, returnAST);
						{
						switch ( LA(1)) {
						case INTLIT:
						{
							AST tmp287_AST = null;
							tmp287_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp287_AST);
							match(INTLIT);
							AST tmp288_AST = null;
							tmp288_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp288_AST);
							match(FROM);
							break;
						}
						case LPAREN:
						case ARDEN_CURLY_BRACKETS:
						{
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
						}
					}
					else if ((LA(1)==LPAREN||LA(1)==ARDEN_CURLY_BRACKETS)) {
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
					{
					switch ( LA(1)) {
					case ARDEN_CURLY_BRACKETS:
					{
						mapping_factor();
						astFactory.addASTChild(currentAST, returnAST);
						{
						switch ( LA(1)) {
						case AFTER:
						case NOT:
						case WHERE:
						case WITHIN:
						{
							{
							switch ( LA(1)) {
							case WHERE:
							{
								where();
								astFactory.addASTChild(currentAST, returnAST);
								it();
								occur();
								break;
							}
							case AFTER:
							case NOT:
							case WITHIN:
							{
								break;
							}
							default:
							{
								throw new NoViableAltException(LT(1), getFilename());
							}
							}
							}
							{
							switch ( LA(1)) {
							case AFTER:
							case WITHIN:
							{
								temporal_comp_op();
								astFactory.addASTChild(currentAST, returnAST);
								break;
							}
							case NOT:
							{
								AST tmp289_AST = null;
								tmp289_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp289_AST);
								match(NOT);
								temporal_comp_op();
								astFactory.addASTChild(currentAST, returnAST);
								break;
							}
							default:
							{
								throw new NoViableAltException(LT(1), getFilename());
							}
							}
							}
							break;
						}
						case SEMI:
						{
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
						break;
					}
					case LPAREN:
					{
						match(LPAREN);
						{
						mapping_factor();
						astFactory.addASTChild(currentAST, returnAST);
						{
						switch ( LA(1)) {
						case WHERE:
						{
							where();
							astFactory.addASTChild(currentAST, returnAST);
							it();
							astFactory.addASTChild(currentAST, returnAST);
							occur();
							astFactory.addASTChild(currentAST, returnAST);
							{
							switch ( LA(1)) {
							case AFTER:
							case WITHIN:
							{
								temporal_comp_op();
								astFactory.addASTChild(currentAST, returnAST);
								break;
							}
							case NOT:
							{
								AST tmp291_AST = null;
								tmp291_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp291_AST);
								match(NOT);
								temporal_comp_op();
								astFactory.addASTChild(currentAST, returnAST);
								break;
							}
							default:
							{
								throw new NoViableAltException(LT(1), getFilename());
							}
							}
							}
							break;
						}
						case RPAREN:
						{
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
						}
						match(RPAREN);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					}
					break;
				}
				case EVENT:
				{
					{
					AST tmp293_AST = null;
					tmp293_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp293_AST);
					match(EVENT);
					}
					mapping_factor();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case COUNT:
				case THE:
				case NOW:
				case MINIMUM:
				case MIN:
				case MAXIMUM:
				case MAX:
				case LAST:
				case FIRST:
				case EARLIEST:
				case LATEST:
				case EXIST:
				case EXISTS:
				case AVG:
				case AVERAGE:
				case SUM:
				case MEDIAN:
				case TRUE:
				case FALSE:
				case OF:
				case TIME:
				case INTLIT:
				case SEMI:
				case ID:
				case LPAREN:
				case COMMA:
				case LITERAL_MERGE:
				case LITERAL_SORT:
				case STRING_LITERAL:
				{
					expr();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				data_assignment_AST = (AST)currentAST.root;
				break;
			}
			case SEMI:
			{
				data_assignment_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_32);
			} else {
			  throw ex;
			}
		}
		returnAST = data_assignment_AST;
	}
	
	public final void data_elseif() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST data_elseif_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ELSE:
			{
				AST tmp294_AST = null;
				tmp294_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp294_AST);
				match(ELSE);
				data_elseif_AST = (AST)currentAST.root;
				break;
			}
			case ELSEIF:
			{
				AST tmp295_AST = null;
				tmp295_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp295_AST);
				match(ELSEIF);
				data_if_then_else2();
				astFactory.addASTChild(currentAST, returnAST);
				data_elseif_AST = (AST)currentAST.root;
				break;
			}
			case ENDIF:
			{
				{
				AST tmp296_AST = null;
				tmp296_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp296_AST);
				match(ENDIF);
				}
				data_elseif_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_32);
			} else {
			  throw ex;
			}
		}
		returnAST = data_elseif_AST;
	}
	
	public final void data_if_then_else2() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST data_if_then_else2_AST = null;
		
		try {      // for error handling
			{
			if ((_tokenSet_35.member(LA(1)))) {
				expr();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else if ((LA(1)==LPAREN)) {
				{
				match(LPAREN);
				}
				expr();
				astFactory.addASTChild(currentAST, returnAST);
				{
				match(RPAREN);
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			AST tmp299_AST = null;
			tmp299_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp299_AST);
			match(THEN);
			data_statement();
			astFactory.addASTChild(currentAST, returnAST);
			data_if_then_else2_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_32);
			} else {
			  throw ex;
			}
		}
		returnAST = data_if_then_else2_AST;
	}
	
/****** expressions ******/
	public final void expr() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_AST = null;
		
		try {      // for error handling
			expr_sort();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop261:
			do {
				if ((LA(1)==COMMA)) {
					AST tmp300_AST = null;
					tmp300_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp300_AST);
					match(COMMA);
					expr_sort();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop261;
				}
				
			} while (true);
			}
			expr_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_36);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_AST;
	}
	
	public final void data_comment() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST data_comment_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case COMMENT:
			{
				{
				AST tmp301_AST = null;
				tmp301_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp301_AST);
				match(COMMENT);
				}
				data_comment_AST = (AST)currentAST.root;
				break;
			}
			case ML_COMMENT:
			{
				{
				AST tmp302_AST = null;
				tmp302_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp302_AST);
				match(ML_COMMENT);
				}
				data_comment_AST = (AST)currentAST.root;
				break;
			}
			case EOF:
			{
				data_comment_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = data_comment_AST;
	}
	
	public final void identifier_becomes() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST identifier_becomes_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ID:
			{
				identifier_or_object_ref();
				astFactory.addASTChild(currentAST, returnAST);
				match(BECOMES);
				identifier_becomes_AST = (AST)currentAST.root;
				break;
			}
			case LET:
			{
				match(LET);
				{
				switch ( LA(1)) {
				case LPAREN:
				{
					match(LPAREN);
					break;
				}
				case ID:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				AST tmp306_AST = null;
				tmp306_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp306_AST);
				match(ID);
				{
				_loop153:
				do {
					if ((LA(1)==COMMA)) {
						AST tmp307_AST = null;
						tmp307_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp307_AST);
						match(COMMA);
						AST tmp308_AST = null;
						tmp308_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp308_AST);
						match(ID);
					}
					else {
						break _loop153;
					}
					
				} while (true);
				}
				{
				switch ( LA(1)) {
				case RPAREN:
				{
					match(RPAREN);
					break;
				}
				case BE:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(BE);
				identifier_becomes_AST = (AST)currentAST.root;
				break;
			}
			case NOW:
			{
				AST tmp311_AST = null;
				tmp311_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp311_AST);
				match(NOW);
				AST tmp312_AST = null;
				tmp312_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp312_AST);
				match(BECOMES);
				identifier_becomes_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_37);
			} else {
			  throw ex;
			}
		}
		returnAST = identifier_becomes_AST;
	}
	
/*********************************OPERATORS***************************************************************/
	public final void of_read_func_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST of_read_func_op_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case AVERAGE:
			{
				AST tmp313_AST = null;
				tmp313_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp313_AST);
				match(AVERAGE);
				of_read_func_op_AST = (AST)currentAST.root;
				break;
			}
			case AVG:
			{
				AST tmp314_AST = null;
				tmp314_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp314_AST);
				match(AVG);
				of_read_func_op_AST = (AST)currentAST.root;
				break;
			}
			case COUNT:
			{
				AST tmp315_AST = null;
				tmp315_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp315_AST);
				match(COUNT);
				of_read_func_op_AST = (AST)currentAST.root;
				break;
			}
			case EXIST:
			{
				{
				AST tmp316_AST = null;
				tmp316_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp316_AST);
				match(EXIST);
				}
				of_read_func_op_AST = (AST)currentAST.root;
				break;
			}
			case EXISTS:
			{
				AST tmp317_AST = null;
				tmp317_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp317_AST);
				match(EXISTS);
				of_read_func_op_AST = (AST)currentAST.root;
				break;
			}
			case SUM:
			{
				AST tmp318_AST = null;
				tmp318_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp318_AST);
				match(SUM);
				of_read_func_op_AST = (AST)currentAST.root;
				break;
			}
			case MEDIAN:
			{
				AST tmp319_AST = null;
				tmp319_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp319_AST);
				match(MEDIAN);
				of_read_func_op_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_38);
			} else {
			  throw ex;
			}
		}
		returnAST = of_read_func_op_AST;
	}
	
	public final void from_of_func_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST from_of_func_op_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case MINIMUM:
			case MIN:
			{
				{
				switch ( LA(1)) {
				case MINIMUM:
				{
					AST tmp320_AST = null;
					tmp320_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp320_AST);
					match(MINIMUM);
					break;
				}
				case MIN:
				{
					AST tmp321_AST = null;
					tmp321_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp321_AST);
					match(MIN);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				from_of_func_op_AST = (AST)currentAST.root;
				break;
			}
			case MAXIMUM:
			case MAX:
			{
				{
				switch ( LA(1)) {
				case MAXIMUM:
				{
					AST tmp322_AST = null;
					tmp322_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp322_AST);
					match(MAXIMUM);
					break;
				}
				case MAX:
				{
					AST tmp323_AST = null;
					tmp323_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp323_AST);
					match(MAX);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				from_of_func_op_AST = (AST)currentAST.root;
				break;
			}
			case LAST:
			{
				{
				AST tmp324_AST = null;
				tmp324_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp324_AST);
				match(LAST);
				}
				from_of_func_op_AST = (AST)currentAST.root;
				break;
			}
			case FIRST:
			{
				{
				AST tmp325_AST = null;
				tmp325_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp325_AST);
				match(FIRST);
				}
				from_of_func_op_AST = (AST)currentAST.root;
				break;
			}
			case EARLIEST:
			{
				{
				AST tmp326_AST = null;
				tmp326_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp326_AST);
				match(EARLIEST);
				}
				from_of_func_op_AST = (AST)currentAST.root;
				break;
			}
			case LATEST:
			{
				{
				AST tmp327_AST = null;
				tmp327_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp327_AST);
				match(LATEST);
				}
				from_of_func_op_AST = (AST)currentAST.root;
				break;
			}
			case NOW:
			case TRUE:
			case FALSE:
			case OF:
			case INTLIT:
			case ID:
			case LPAREN:
			case ARDEN_CURLY_BRACKETS:
			case STRING_LITERAL:
			{
				from_of_func_op_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_39);
			} else {
			  throw ex;
			}
		}
		returnAST = from_of_func_op_AST;
	}
	
	public final void mapping_factor() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST mapping_factor_AST = null;
		
		try {      // for error handling
			AST tmp328_AST = null;
			tmp328_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp328_AST);
			match(ARDEN_CURLY_BRACKETS);
			mapping_factor_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_40);
			} else {
			  throw ex;
			}
		}
		returnAST = mapping_factor_AST;
	}
	
/************************************************************************************************/
	public final void where() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST where_AST = null;
		
		try {      // for error handling
			{
			AST tmp329_AST = null;
			tmp329_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp329_AST);
			match(WHERE);
			}
			where_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_41);
			} else {
			  throw ex;
			}
		}
		returnAST = where_AST;
	}
	
	public final void it() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST it_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case IT:
			{
				match(IT);
				break;
			}
			case THEY:
			{
				match(THEY);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			it_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_42);
			} else {
			  throw ex;
			}
		}
		returnAST = it_AST;
	}
	
	public final void occur() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST occur_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_OCCUR:
			case LITERAL_Occur:
			case LITERAL_occur:
			{
				{
				switch ( LA(1)) {
				case LITERAL_OCCUR:
				{
					match(LITERAL_OCCUR);
					break;
				}
				case LITERAL_Occur:
				{
					match(LITERAL_Occur);
					break;
				}
				case LITERAL_occur:
				{
					match(LITERAL_occur);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				occur_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_OCCURS:
			case LITERAL_Occurs:
			case LITERAL_occurs:
			{
				{
				switch ( LA(1)) {
				case LITERAL_OCCURS:
				{
					match(LITERAL_OCCURS);
					break;
				}
				case LITERAL_Occurs:
				{
					match(LITERAL_Occurs);
					break;
				}
				case LITERAL_occurs:
				{
					match(LITERAL_occurs);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				occur_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_OCCURRED:
			case LITERAL_Occurred:
			{
				{
				if ((LA(1)==LITERAL_OCCURRED)) {
					match(LITERAL_OCCURRED);
				}
				else if ((LA(1)==LITERAL_Occurred)) {
					match(LITERAL_Occurred);
				}
				else if ((LA(1)==LITERAL_Occurred)) {
					match(LITERAL_Occurred);
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				occur_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_43);
			} else {
			  throw ex;
			}
		}
		returnAST = occur_AST;
	}
	
	public final void temporal_comp_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST temporal_comp_op_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case WITHIN:
			{
				match(WITHIN);
				{
				switch ( LA(1)) {
				case THE:
				{
					the();
					break;
				}
				case PAST:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				AST tmp342_AST = null;
				tmp342_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp342_AST);
				match(PAST);
				expr_string();
				astFactory.addASTChild(currentAST, returnAST);
				temporal_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case AFTER:
			{
				AST tmp343_AST = null;
				tmp343_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp343_AST);
				match(AFTER);
				expr_string();
				astFactory.addASTChild(currentAST, returnAST);
				temporal_comp_op_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_44);
			} else {
			  throw ex;
			}
		}
		returnAST = temporal_comp_op_AST;
	}
	
	public final void endassignment() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST endassignment_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case SEMI:
			{
				AST tmp344_AST = null;
				tmp344_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp344_AST);
				match(SEMI);
				endassignment_AST = (AST)currentAST.root;
				break;
			}
			case EOF:
			{
				{
				}
				endassignment_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = endassignment_AST;
	}
	
	public final void endblock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST endblock_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ENDBLOCK:
			{
				AST tmp345_AST = null;
				tmp345_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp345_AST);
				match(ENDBLOCK);
				endblock_AST = (AST)currentAST.root;
				break;
			}
			case EOF:
			{
				{
				}
				endblock_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = endblock_AST;
	}
	
	public final void identifier_or_object_ref() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST identifier_or_object_ref_AST = null;
		
		try {      // for error handling
			AST tmp346_AST = null;
			tmp346_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp346_AST);
			match(ID);
			{
			_loop157:
			do {
				if ((LA(1)==DOT)) {
					AST tmp347_AST = null;
					tmp347_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp347_AST);
					match(DOT);
					AST tmp348_AST = null;
					tmp348_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp348_AST);
					match(ID);
				}
				else {
					break _loop157;
				}
				
			} while (true);
			}
			identifier_or_object_ref_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_45);
			} else {
			  throw ex;
			}
		}
		returnAST = identifier_or_object_ref_AST;
	}
	
	public final void data_assign_phrase() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST data_assign_phrase_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case READ:
			{
				AST tmp349_AST = null;
				tmp349_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp349_AST);
				match(READ);
				read_phrase();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case EVENT:
			case LITERAL_EVENT:
			case LITERAL_Event:
			{
				{
				switch ( LA(1)) {
				case LITERAL_EVENT:
				{
					AST tmp350_AST = null;
					tmp350_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp350_AST);
					match(LITERAL_EVENT);
					break;
				}
				case LITERAL_Event:
				{
					AST tmp351_AST = null;
					tmp351_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp351_AST);
					match(LITERAL_Event);
					break;
				}
				case EVENT:
				{
					AST tmp352_AST = null;
					tmp352_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp352_AST);
					match(EVENT);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				mapping_factor();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			data_assign_phrase_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = data_assign_phrase_AST;
	}
	
	public final void read_phrase() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST read_phrase_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LPAREN:
			case ARDEN_CURLY_BRACKETS:
			{
				read_where();
				astFactory.addASTChild(currentAST, returnAST);
				read_phrase_AST = (AST)currentAST.root;
				break;
			}
			case COUNT:
			case EXIST:
			case EXISTS:
			case AVG:
			case AVERAGE:
			case SUM:
			case MEDIAN:
			{
				of_read_func_op();
				astFactory.addASTChild(currentAST, returnAST);
				read_where();
				astFactory.addASTChild(currentAST, returnAST);
				from_of_func_op();
				astFactory.addASTChild(currentAST, returnAST);
				read_where();
				astFactory.addASTChild(currentAST, returnAST);
				read_phrase_AST = (AST)currentAST.root;
				break;
			}
			case MINIMUM:
			case MIN:
			case MAXIMUM:
			case MAX:
			case LAST:
			case FIRST:
			case EARLIEST:
			case LATEST:
			case INTLIT:
			{
				from_of_func_op();
				astFactory.addASTChild(currentAST, returnAST);
				AST tmp353_AST = null;
				tmp353_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp353_AST);
				match(INTLIT);
				{
				AST tmp354_AST = null;
				tmp354_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp354_AST);
				match(FROM);
				}
				read_where();
				astFactory.addASTChild(currentAST, returnAST);
				read_phrase_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = read_phrase_AST;
	}
	
	public final void data_var_list() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST data_var_list_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case EOF:
			{
				data_var_list_AST = (AST)currentAST.root;
				break;
			}
			case ID:
			{
				AST tmp355_AST = null;
				tmp355_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp355_AST);
				match(ID);
				{
				_loop163:
				do {
					if ((LA(1)==COMMA)) {
						AST tmp356_AST = null;
						tmp356_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp356_AST);
						match(COMMA);
						AST tmp357_AST = null;
						tmp357_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp357_AST);
						match(ID);
					}
					else {
						break _loop163;
					}
					
				} while (true);
				}
				data_var_list_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = data_var_list_AST;
	}
	
	public final void read_where() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST read_where_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case ARDEN_CURLY_BRACKETS:
			{
				mapping_factor();
				astFactory.addASTChild(currentAST, returnAST);
				{
				switch ( LA(1)) {
				case AFTER:
				case NOT:
				case WHERE:
				case WITHIN:
				{
					{
					switch ( LA(1)) {
					case WHERE:
					{
						where();
						astFactory.addASTChild(currentAST, returnAST);
						it();
						astFactory.addASTChild(currentAST, returnAST);
						occur();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					case AFTER:
					case NOT:
					case WITHIN:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					{
					switch ( LA(1)) {
					case AFTER:
					case WITHIN:
					{
						temporal_comp_op();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					case NOT:
					{
						AST tmp358_AST = null;
						tmp358_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp358_AST);
						match(NOT);
						temporal_comp_op();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					break;
				}
				case EOF:
				case MINIMUM:
				case MIN:
				case MAXIMUM:
				case MAX:
				case LAST:
				case FIRST:
				case EARLIEST:
				case LATEST:
				case LPAREN:
				case ARDEN_CURLY_BRACKETS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				break;
			}
			case LPAREN:
			{
				match(LPAREN);
				{
				mapping_factor();
				astFactory.addASTChild(currentAST, returnAST);
				{
				switch ( LA(1)) {
				case WHERE:
				{
					where();
					astFactory.addASTChild(currentAST, returnAST);
					it();
					astFactory.addASTChild(currentAST, returnAST);
					occur();
					astFactory.addASTChild(currentAST, returnAST);
					{
					switch ( LA(1)) {
					case AFTER:
					case WITHIN:
					{
						temporal_comp_op();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					case NOT:
					{
						AST tmp360_AST = null;
						tmp360_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp360_AST);
						match(NOT);
						temporal_comp_op();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					break;
				}
				case RPAREN:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				}
				match(RPAREN);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			read_where_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_46);
			} else {
			  throw ex;
			}
		}
		returnAST = read_where_AST;
	}
	
	public final void time_value() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST time_value_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case NOW:
			{
				AST tmp362_AST = null;
				tmp362_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp362_AST);
				match(NOW);
				time_value_AST = (AST)currentAST.root;
				break;
			}
			case INTLIT:
			{
				iso_date_time();
				astFactory.addASTChild(currentAST, returnAST);
				time_value_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_47);
			} else {
			  throw ex;
			}
		}
		returnAST = time_value_AST;
	}
	
	public final void unary_comp_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST unary_comp_op_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_PRESENT:
			{
				AST tmp363_AST = null;
				tmp363_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp363_AST);
				match(LITERAL_PRESENT);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_NULL:
			{
				AST tmp364_AST = null;
				tmp364_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp364_AST);
				match(LITERAL_NULL);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_BOOLEAN:
			{
				AST tmp365_AST = null;
				tmp365_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp365_AST);
				match(LITERAL_BOOLEAN);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_NUMBER:
			{
				AST tmp366_AST = null;
				tmp366_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp366_AST);
				match(LITERAL_NUMBER);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case TIME:
			{
				AST tmp367_AST = null;
				tmp367_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp367_AST);
				match(TIME);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_DURATION:
			{
				AST tmp368_AST = null;
				tmp368_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp368_AST);
				match(LITERAL_DURATION);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_STRING:
			{
				AST tmp369_AST = null;
				tmp369_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp369_AST);
				match(LITERAL_STRING);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_LIST:
			{
				AST tmp370_AST = null;
				tmp370_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp370_AST);
				match(LITERAL_LIST);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_OBJECT:
			{
				AST tmp371_AST = null;
				tmp371_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp371_AST);
				match(LITERAL_OBJECT);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case ID:
			{
				AST tmp372_AST = null;
				tmp372_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp372_AST);
				match(ID);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = unary_comp_op_AST;
	}
	
	public final void binary_comp_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST binary_comp_op_AST = null;
		
		try {      // for error handling
			if ((LA(1)==LESS)) {
				AST tmp373_AST = null;
				tmp373_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp373_AST);
				match(LESS);
				AST tmp374_AST = null;
				tmp374_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp374_AST);
				match(THAN);
				binary_comp_op_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==LITERAL_GREATER)) {
				AST tmp375_AST = null;
				tmp375_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp375_AST);
				match(LITERAL_GREATER);
				AST tmp376_AST = null;
				tmp376_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp376_AST);
				match(THAN);
				binary_comp_op_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==LITERAL_GREATER)) {
				AST tmp377_AST = null;
				tmp377_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp377_AST);
				match(LITERAL_GREATER);
				AST tmp378_AST = null;
				tmp378_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp378_AST);
				match(THAN);
				AST tmp379_AST = null;
				tmp379_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp379_AST);
				match(OR);
				AST tmp380_AST = null;
				tmp380_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp380_AST);
				match(LITERAL_EQUAL);
				binary_comp_op_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==LESS)) {
				AST tmp381_AST = null;
				tmp381_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp381_AST);
				match(LESS);
				AST tmp382_AST = null;
				tmp382_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp382_AST);
				match(THAN);
				AST tmp383_AST = null;
				tmp383_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp383_AST);
				match(OR);
				AST tmp384_AST = null;
				tmp384_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp384_AST);
				match(LITERAL_EQUAL);
				binary_comp_op_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==IN)) {
				AST tmp385_AST = null;
				tmp385_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp385_AST);
				match(IN);
				binary_comp_op_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_48);
			} else {
			  throw ex;
			}
		}
		returnAST = binary_comp_op_AST;
	}
	
	public final void duration_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST duration_op_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case YEAR:
			{
				AST tmp386_AST = null;
				tmp386_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp386_AST);
				match(YEAR);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case YEARS:
			{
				AST tmp387_AST = null;
				tmp387_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp387_AST);
				match(YEARS);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case MONTH:
			{
				AST tmp388_AST = null;
				tmp388_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp388_AST);
				match(MONTH);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case MONTHS:
			{
				AST tmp389_AST = null;
				tmp389_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp389_AST);
				match(MONTHS);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case WEEK:
			{
				AST tmp390_AST = null;
				tmp390_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp390_AST);
				match(WEEK);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case WEEKS:
			{
				AST tmp391_AST = null;
				tmp391_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp391_AST);
				match(WEEKS);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case DAY:
			{
				AST tmp392_AST = null;
				tmp392_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp392_AST);
				match(DAY);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case DAYS:
			{
				AST tmp393_AST = null;
				tmp393_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp393_AST);
				match(DAYS);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_hour:
			{
				AST tmp394_AST = null;
				tmp394_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp394_AST);
				match(LITERAL_hour);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_hours:
			{
				AST tmp395_AST = null;
				tmp395_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp395_AST);
				match(LITERAL_hours);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_minute:
			{
				AST tmp396_AST = null;
				tmp396_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp396_AST);
				match(LITERAL_minute);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_minutes:
			{
				AST tmp397_AST = null;
				tmp397_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp397_AST);
				match(LITERAL_minutes);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_second:
			{
				AST tmp398_AST = null;
				tmp398_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp398_AST);
				match(LITERAL_second);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_seconds:
			{
				AST tmp399_AST = null;
				tmp399_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp399_AST);
				match(LITERAL_seconds);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_49);
			} else {
			  throw ex;
			}
		}
		returnAST = duration_op_AST;
	}
	
	public final void the() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST the_AST = null;
		
		try {      // for error handling
			AST tmp400_AST = null;
			tmp400_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp400_AST);
			match(THE);
			the_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_50);
			} else {
			  throw ex;
			}
		}
		returnAST = the_AST;
	}
	
/**********************************************************************************/
	public final void expr_string() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_string_AST = null;
		
		try {      // for error handling
			expr_plus();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop290:
			do {
				if ((LA(1)==ACTION_OP)) {
					AST tmp401_AST = null;
					tmp401_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp401_AST);
					match(ACTION_OP);
					expr_plus();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop290;
				}
				
			} while (true);
			}
			expr_string_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_51);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_string_AST;
	}
	
/****** comparison synonyms ******/
	public final void is() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST is_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case IS:
			{
				AST tmp402_AST = null;
				tmp402_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp402_AST);
				match(IS);
				is_AST = (AST)currentAST.root;
				break;
			}
			case ARE:
			{
				AST tmp403_AST = null;
				tmp403_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp403_AST);
				match(ARE);
				is_AST = (AST)currentAST.root;
				break;
			}
			case WERE:
			{
				AST tmp404_AST = null;
				tmp404_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp404_AST);
				match(WERE);
				is_AST = (AST)currentAST.root;
				break;
			}
			case WAS:
			{
				AST tmp405_AST = null;
				tmp405_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp405_AST);
				match(WAS);
				is_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_52);
			} else {
			  throw ex;
			}
		}
		returnAST = is_AST;
	}
	
	public final void evoke_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST evoke_statement_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ID:
			case LPAREN:
			case LITERAL_Any:
			{
				event_or();
				astFactory.addASTChild(currentAST, returnAST);
				evoke_statement_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_CALL:
			{
				AST tmp406_AST = null;
				tmp406_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp406_AST);
				match(LITERAL_CALL);
				evoke_statement_AST = (AST)currentAST.root;
				break;
			}
			default:
				if ((LA(1)==ENDBLOCK)) {
					evoke_statement_AST = (AST)currentAST.root;
				}
				else if ((LA(1)==ENDBLOCK)) {
					evoke_statement_AST = (AST)currentAST.root;
				}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_25);
			} else {
			  throw ex;
			}
		}
		returnAST = evoke_statement_AST;
	}
	
	public final void event_or() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST event_or_AST = null;
		
		try {      // for error handling
			event_any();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop206:
			do {
				if ((LA(1)==OR)) {
					AST tmp407_AST = null;
					tmp407_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp407_AST);
					match(OR);
					event_any();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop206;
				}
				
			} while (true);
			}
			event_or_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_53);
			} else {
			  throw ex;
			}
		}
		returnAST = event_or_AST;
	}
	
	public final void event_any() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST event_any_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_Any:
			{
				AST tmp408_AST = null;
				tmp408_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp408_AST);
				match(LITERAL_Any);
				AST tmp409_AST = null;
				tmp409_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp409_AST);
				match(LPAREN);
				event_list();
				astFactory.addASTChild(currentAST, returnAST);
				AST tmp410_AST = null;
				tmp410_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp410_AST);
				match(RPAREN);
				event_any_AST = (AST)currentAST.root;
				break;
			}
			case ID:
			case LPAREN:
			{
				event_factor();
				astFactory.addASTChild(currentAST, returnAST);
				event_any_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_54);
			} else {
			  throw ex;
			}
		}
		returnAST = event_any_AST;
	}
	
	public final void event_list() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST event_list_AST = null;
		
		try {      // for error handling
			event_or();
			astFactory.addASTChild(currentAST, returnAST);
			{
			AST tmp411_AST = null;
			tmp411_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp411_AST);
			match(COMMA);
			event_or();
			astFactory.addASTChild(currentAST, returnAST);
			}
			event_list_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_55);
			} else {
			  throw ex;
			}
		}
		returnAST = event_list_AST;
	}
	
	public final void event_factor() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST event_factor_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LPAREN:
			{
				AST tmp412_AST = null;
				tmp412_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp412_AST);
				match(LPAREN);
				event_or();
				astFactory.addASTChild(currentAST, returnAST);
				AST tmp413_AST = null;
				tmp413_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp413_AST);
				match(RPAREN);
				event_factor_AST = (AST)currentAST.root;
				break;
			}
			case ID:
			{
				AST tmp414_AST = null;
				tmp414_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp414_AST);
				match(ID);
				event_factor_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_54);
			} else {
			  throw ex;
			}
		}
		returnAST = event_factor_AST;
	}
	
	public final void logic_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logic_statement_AST = null;
		
		try {      // for error handling
			{
			_loop216:
			do {
				switch ( LA(1)) {
				case IF:
				{
					if_statement();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case CONCLUDE:
				{
					conclude_statement();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case LET:
				case NOW:
				case ID:
				case ACTION_OP:
				{
					logic_assignment();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case ELSE:
				case ELSEIF:
				case ENDIF:
				{
					logic_elseif();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				default:
				{
					break _loop216;
				}
				}
			} while (true);
			}
			logic_statement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_32);
			} else {
			  throw ex;
			}
		}
		returnAST = logic_statement_AST;
	}
	
	public final void if_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST if_statement_AST = null;
		
		try {      // for error handling
			AST tmp415_AST = null;
			tmp415_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp415_AST);
			match(IF);
			logic_if_then_else2();
			astFactory.addASTChild(currentAST, returnAST);
			if_statement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_22);
			} else {
			  throw ex;
			}
		}
		returnAST = if_statement_AST;
	}
	
	public final void conclude_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST conclude_statement_AST = null;
		
		try {      // for error handling
			{
			AST tmp416_AST = null;
			tmp416_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp416_AST);
			match(CONCLUDE);
			}
			boolean_value();
			astFactory.addASTChild(currentAST, returnAST);
			conclude_statement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_22);
			} else {
			  throw ex;
			}
		}
		returnAST = conclude_statement_AST;
	}
	
	public final void logic_assignment() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logic_assignment_AST = null;
		
		try {      // for error handling
			if ((LA(1)==ID||LA(1)==ACTION_OP)) {
				{
				switch ( LA(1)) {
				case ACTION_OP:
				{
					AST tmp417_AST = null;
					tmp417_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp417_AST);
					match(ACTION_OP);
					break;
				}
				case ID:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				identifier_or_object_ref();
				astFactory.addASTChild(currentAST, returnAST);
				{
				switch ( LA(1)) {
				case ACTION_OP:
				{
					AST tmp418_AST = null;
					tmp418_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp418_AST);
					match(ACTION_OP);
					break;
				}
				case BECOMES:
				case EQUALS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				{
				switch ( LA(1)) {
				case BECOMES:
				{
					match(BECOMES);
					break;
				}
				case EQUALS:
				{
					match(EQUALS);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				expr();
				astFactory.addASTChild(currentAST, returnAST);
				logic_assignment_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==LET||LA(1)==NOW||LA(1)==ID)) {
				identifier_becomes();
				astFactory.addASTChild(currentAST, returnAST);
				expr();
				astFactory.addASTChild(currentAST, returnAST);
				logic_assignment_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_22);
			} else {
			  throw ex;
			}
		}
		returnAST = logic_assignment_AST;
	}
	
	public final void logic_elseif() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logic_elseif_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ELSE:
			{
				AST tmp421_AST = null;
				tmp421_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp421_AST);
				match(ELSE);
				logic_elseif_AST = (AST)currentAST.root;
				break;
			}
			case ELSEIF:
			{
				AST tmp422_AST = null;
				tmp422_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp422_AST);
				match(ELSEIF);
				logic_if_then_else2();
				astFactory.addASTChild(currentAST, returnAST);
				logic_elseif_AST = (AST)currentAST.root;
				break;
			}
			case ENDIF:
			{
				{
				AST tmp423_AST = null;
				tmp423_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp423_AST);
				match(ENDIF);
				}
				logic_elseif_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_22);
			} else {
			  throw ex;
			}
		}
		returnAST = logic_elseif_AST;
	}
	
	public final void logic_expr() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logic_expr_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case THE:
			{
				the();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case NOW:
			case MINIMUM:
			case MIN:
			case MAXIMUM:
			case MAX:
			case LAST:
			case FIRST:
			case EARLIEST:
			case LATEST:
			case TRUE:
			case FALSE:
			case INTLIT:
			case ID:
			case LPAREN:
			case STRING_LITERAL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			from_of_func_op();
			astFactory.addASTChild(currentAST, returnAST);
			expr_factor();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case IS:
			case ARE:
			case WAS:
			case WERE:
			{
				is();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case IN:
			case LESS:
			case LITERAL_GREATER:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			binary_comp_op();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case THE:
			{
				the();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case NOW:
			case MINIMUM:
			case MIN:
			case MAXIMUM:
			case MAX:
			case LAST:
			case FIRST:
			case EARLIEST:
			case LATEST:
			case TRUE:
			case FALSE:
			case INTLIT:
			case ID:
			case LPAREN:
			case STRING_LITERAL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			if ((_tokenSet_56.member(LA(1)))) {
				expr_factor();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else if ((_tokenSet_57.member(LA(1)))) {
				from_of_func_op();
				astFactory.addASTChild(currentAST, returnAST);
				expr_factor();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			logic_expr_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = logic_expr_AST;
	}
	
	public final void expr_factor() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_factor_AST = null;
		
		try {      // for error handling
			expr_factor_atom();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop313:
			do {
				if ((LA(1)==DOT)) {
					AST tmp424_AST = null;
					tmp424_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp424_AST);
					match(DOT);
					expr_factor_atom();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop313;
				}
				
			} while (true);
			}
			expr_factor_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_58);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_factor_AST;
	}
	
	public final void logic_condition() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logic_condition_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case AND:
			{
				{
				AST tmp425_AST = null;
				tmp425_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp425_AST);
				match(AND);
				}
				break;
			}
			case OR:
			{
				{
				AST tmp426_AST = null;
				tmp426_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp426_AST);
				match(OR);
				}
				break;
			}
			case NOT:
			{
				{
				AST tmp427_AST = null;
				tmp427_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp427_AST);
				match(NOT);
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			logic_condition_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = logic_condition_AST;
	}
	
	public final void simple_comp_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST simple_comp_op_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case EQUALS:
			{
				{
				AST tmp428_AST = null;
				tmp428_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp428_AST);
				match(EQUALS);
				}
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_EQ:
			{
				{
				AST tmp429_AST = null;
				tmp429_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp429_AST);
				match(LITERAL_EQ);
				}
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LT:
			{
				AST tmp430_AST = null;
				tmp430_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp430_AST);
				match(LT);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_LT:
			{
				AST tmp431_AST = null;
				tmp431_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp431_AST);
				match(LITERAL_LT);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case GT:
			{
				AST tmp432_AST = null;
				tmp432_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp432_AST);
				match(GT);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_GT:
			{
				AST tmp433_AST = null;
				tmp433_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp433_AST);
				match(LITERAL_GT);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LTE:
			{
				AST tmp434_AST = null;
				tmp434_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp434_AST);
				match(LTE);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_LE:
			{
				AST tmp435_AST = null;
				tmp435_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp435_AST);
				match(LITERAL_LE);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case GTE:
			{
				AST tmp436_AST = null;
				tmp436_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp436_AST);
				match(GTE);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_GE:
			{
				AST tmp437_AST = null;
				tmp437_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp437_AST);
				match(LITERAL_GE);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case NE:
			{
				AST tmp438_AST = null;
				tmp438_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp438_AST);
				match(NE);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_NE:
			{
				AST tmp439_AST = null;
				tmp439_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp439_AST);
				match(LITERAL_NE);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_48);
			} else {
			  throw ex;
			}
		}
		returnAST = simple_comp_op_AST;
	}
	
	public final void main_comp_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST main_comp_op_AST = null;
		
		try {      // for error handling
			binary_comp_op();
			astFactory.addASTChild(currentAST, returnAST);
			expr_string();
			astFactory.addASTChild(currentAST, returnAST);
			main_comp_op_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_59);
			} else {
			  throw ex;
			}
		}
		returnAST = main_comp_op_AST;
	}
	
	public final void logic_if_then_else2() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logic_if_then_else2_AST = null;
		
		try {      // for error handling
			{
			if ((_tokenSet_35.member(LA(1)))) {
				expr();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else if ((LA(1)==LPAREN)) {
				{
				match(LPAREN);
				}
				expr();
				astFactory.addASTChild(currentAST, returnAST);
				{
				match(RPAREN);
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			AST tmp442_AST = null;
			tmp442_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp442_AST);
			match(THEN);
			logic_if_then_else2_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_22);
			} else {
			  throw ex;
			}
		}
		returnAST = logic_if_then_else2_AST;
	}
	
	public final void boolean_value() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST boolean_value_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case TRUE:
			{
				{
				AST tmp443_AST = null;
				tmp443_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp443_AST);
				match(TRUE);
				}
				boolean_value_AST = (AST)currentAST.root;
				break;
			}
			case FALSE:
			{
				{
				AST tmp444_AST = null;
				tmp444_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp444_AST);
				match(FALSE);
				}
				boolean_value_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_47);
			} else {
			  throw ex;
			}
		}
		returnAST = boolean_value_AST;
	}
	
	public final void action_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST action_statement_AST = null;
		
		try {      // for error handling
			{
			AST tmp445_AST = null;
			tmp445_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp445_AST);
			match(WRITE);
			}
			{
			{
			if ((LA(1)==LPAREN)) {
				match(LPAREN);
			}
			else if ((_tokenSet_60.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((_tokenSet_61.member(LA(1)))) {
				{
				_loop253:
				do {
					if ((LA(1)==ACTION_OP)) {
						AST tmp447_AST = null;
						tmp447_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp447_AST);
						match(ACTION_OP);
						expr_factor();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop253;
					}
					
				} while (true);
				}
			}
			else if ((_tokenSet_62.member(LA(1)))) {
				expr();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			switch ( LA(1)) {
			case RPAREN:
			{
				match(RPAREN);
				break;
			}
			case AT:
			case SEMI:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			}
			{
			switch ( LA(1)) {
			case AT:
			{
				{
				AST tmp449_AST = null;
				tmp449_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp449_AST);
				match(AT);
				}
				AST tmp450_AST = null;
				tmp450_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp450_AST);
				match(ID);
				break;
			}
			case SEMI:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			action_statement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_32);
			} else {
			  throw ex;
			}
		}
		returnAST = action_statement_AST;
	}
	
	public final void urgency_val() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST urgency_val_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case INTLIT:
			{
				AST tmp451_AST = null;
				tmp451_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp451_AST);
				match(INTLIT);
				urgency_val_AST = (AST)currentAST.root;
				break;
			}
			case ENDBLOCK:
			{
				urgency_val_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_25);
			} else {
			  throw ex;
			}
		}
		returnAST = urgency_val_AST;
	}
	
	public final void expr_sort() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_sort_AST = null;
		
		try {      // for error handling
			expr_where();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop267:
			do {
				if ((LA(1)==LITERAL_MERGE||LA(1)==LITERAL_SORT)) {
					{
					switch ( LA(1)) {
					case LITERAL_MERGE:
					{
						AST tmp452_AST = null;
						tmp452_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp452_AST);
						match(LITERAL_MERGE);
						break;
					}
					case LITERAL_SORT:
					{
						{
						AST tmp453_AST = null;
						tmp453_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp453_AST);
						match(LITERAL_SORT);
						{
						if ((LA(1)==TIME||LA(1)==LITERAL_DATA)) {
							sort_option();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else if ((_tokenSet_63.member(LA(1)))) {
						}
						else {
							throw new NoViableAltException(LT(1), getFilename());
						}
						
						}
						}
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					expr_where();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop267;
				}
				
			} while (true);
			}
			expr_sort_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_64);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_sort_AST;
	}
	
	public final void expr_where() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_where_AST = null;
		
		try {      // for error handling
			if ((_tokenSet_48.member(LA(1)))) {
				expr_range();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop271:
				do {
					if ((LA(1)==WHERE)) {
						AST tmp454_AST = null;
						tmp454_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp454_AST);
						match(WHERE);
						expr_range();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop271;
					}
					
				} while (true);
				}
				expr_where_AST = (AST)currentAST.root;
			}
			else if ((_tokenSet_65.member(LA(1)))) {
				expr_where_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_65);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_where_AST;
	}
	
	public final void sort_option() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST sort_option_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case TIME:
			{
				AST tmp455_AST = null;
				tmp455_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp455_AST);
				match(TIME);
				sort_option_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_DATA:
			{
				AST tmp456_AST = null;
				tmp456_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp456_AST);
				match(LITERAL_DATA);
				sort_option_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_63);
			} else {
			  throw ex;
			}
		}
		returnAST = sort_option_AST;
	}
	
	public final void expr_range() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_range_AST = null;
		
		try {      // for error handling
			expr_or();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop274:
			do {
				if ((LA(1)==LITERAL_SEQTO)) {
					AST tmp457_AST = null;
					tmp457_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp457_AST);
					match(LITERAL_SEQTO);
					expr_or();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop274;
				}
				
			} while (true);
			}
			expr_range_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_66);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_range_AST;
	}
	
	public final void expr_or() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_or_AST = null;
		
		try {      // for error handling
			expr_and();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop277:
			do {
				if ((LA(1)==OR)) {
					AST tmp458_AST = null;
					tmp458_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp458_AST);
					match(OR);
					expr_and();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop277;
				}
				
			} while (true);
			}
			expr_or_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_67);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_or_AST;
	}
	
	public final void expr_and() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_and_AST = null;
		
		try {      // for error handling
			expr_not();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop280:
			do {
				if ((LA(1)==AND)) {
					AST tmp459_AST = null;
					tmp459_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp459_AST);
					match(AND);
					expr_not();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop280;
				}
				
			} while (true);
			}
			expr_and_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_68);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_and_AST;
	}
	
	public final void expr_not() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_not_AST = null;
		
		try {      // for error handling
			expr_comparison();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop283:
			do {
				if ((LA(1)==NOT)) {
					AST tmp460_AST = null;
					tmp460_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp460_AST);
					match(NOT);
					expr_comparison();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop283;
				}
				
			} while (true);
			}
			expr_not_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_69);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_not_AST;
	}
	
	public final void expr_comparison() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_comparison_AST = null;
		
		try {      // for error handling
			expr_string();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case EQUALS:
			case LITERAL_EQ:
			case LT:
			case LITERAL_LT:
			case GT:
			case LITERAL_GT:
			case LTE:
			case LITERAL_LE:
			case GTE:
			case LITERAL_GE:
			case NE:
			case LITERAL_NE:
			{
				{
				simple_comp_op();
				astFactory.addASTChild(currentAST, returnAST);
				expr_string();
				astFactory.addASTChild(currentAST, returnAST);
				}
				break;
			}
			case IS:
			case ARE:
			case WAS:
			case WERE:
			{
				{
				is();
				astFactory.addASTChild(currentAST, returnAST);
				main_comp_op();
				astFactory.addASTChild(currentAST, returnAST);
				}
				break;
			}
			case AND:
			case AT:
			case LET:
			case NOW:
			case IF:
			case NOT:
			case OR:
			case THEN:
			case WHERE:
			case CONCLUDE:
			case ELSE:
			case ELSEIF:
			case ENDIF:
			case SEMI:
			case ID:
			case RPAREN:
			case COMMA:
			case ACTION_OP:
			case LITERAL_MERGE:
			case LITERAL_SORT:
			case LITERAL_SEQTO:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			expr_comparison_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_59);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_comparison_AST;
	}
	
	public final void expr_plus() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_plus_AST = null;
		
		try {      // for error handling
			expr_times();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop294:
			do {
				if ((LA(1)==103||LA(1)==104)) {
					{
					switch ( LA(1)) {
					case 103:
					{
						AST tmp461_AST = null;
						tmp461_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp461_AST);
						match(103);
						break;
					}
					case 104:
					{
						AST tmp462_AST = null;
						tmp462_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp462_AST);
						match(104);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					expr_times();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop294;
				}
				
			} while (true);
			}
			expr_plus_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_51);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_plus_AST;
	}
	
	public final void expr_times() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_times_AST = null;
		
		try {      // for error handling
			expr_power();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop298:
			do {
				if ((LA(1)==171||LA(1)==172)) {
					{
					switch ( LA(1)) {
					case 171:
					{
						AST tmp463_AST = null;
						tmp463_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp463_AST);
						match(171);
						break;
					}
					case 172:
					{
						AST tmp464_AST = null;
						tmp464_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp464_AST);
						match(172);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					expr_times();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop298;
				}
				
			} while (true);
			}
			expr_times_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_70);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_times_AST;
	}
	
	public final void expr_power() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_power_AST = null;
		
		try {      // for error handling
			expr_duration();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop302:
			do {
				if (((LA(1) >= FROM && LA(1) <= AFTER))) {
					{
					switch ( LA(1)) {
					case BEFORE:
					{
						AST tmp465_AST = null;
						tmp465_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp465_AST);
						match(BEFORE);
						break;
					}
					case AFTER:
					{
						AST tmp466_AST = null;
						tmp466_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp466_AST);
						match(AFTER);
						break;
					}
					case FROM:
					{
						AST tmp467_AST = null;
						tmp467_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp467_AST);
						match(FROM);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					expr_duration();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop302;
				}
				
			} while (true);
			}
			{
			switch ( LA(1)) {
			case AGO:
			{
				AST tmp468_AST = null;
				tmp468_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp468_AST);
				match(AGO);
				break;
			}
			case EOF:
			case AND:
			case IS:
			case ARE:
			case WAS:
			case WERE:
			case AT:
			case LET:
			case NOW:
			case IF:
			case NOT:
			case OR:
			case THEN:
			case MINIMUM:
			case MIN:
			case MAXIMUM:
			case MAX:
			case LAST:
			case FIRST:
			case EARLIEST:
			case LATEST:
			case WHERE:
			case CONCLUDE:
			case ELSE:
			case ELSEIF:
			case ENDIF:
			case SEMI:
			case ID:
			case LPAREN:
			case RPAREN:
			case 103:
			case 104:
			case COMMA:
			case ARDEN_CURLY_BRACKETS:
			case EQUALS:
			case LITERAL_EQ:
			case LT:
			case LITERAL_LT:
			case GT:
			case LITERAL_GT:
			case LTE:
			case LITERAL_LE:
			case GTE:
			case LITERAL_GE:
			case NE:
			case LITERAL_NE:
			case ACTION_OP:
			case LITERAL_MERGE:
			case LITERAL_SORT:
			case LITERAL_SEQTO:
			case 171:
			case 172:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			expr_power_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_70);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_power_AST;
	}
	
	public final void expr_duration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_duration_AST = null;
		
		try {      // for error handling
			expr_function();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case YEAR:
			case YEARS:
			case DAYS:
			case DAY:
			case MONTH:
			case MONTHS:
			case WEEK:
			case WEEKS:
			case LITERAL_hour:
			case LITERAL_hours:
			case LITERAL_minute:
			case LITERAL_minutes:
			case LITERAL_second:
			case LITERAL_seconds:
			{
				duration_op();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case EOF:
			case AND:
			case IS:
			case ARE:
			case WAS:
			case WERE:
			case FROM:
			case BEFORE:
			case AFTER:
			case AGO:
			case AT:
			case LET:
			case NOW:
			case IF:
			case NOT:
			case OR:
			case THEN:
			case MINIMUM:
			case MIN:
			case MAXIMUM:
			case MAX:
			case LAST:
			case FIRST:
			case EARLIEST:
			case LATEST:
			case WHERE:
			case CONCLUDE:
			case ELSE:
			case ELSEIF:
			case ENDIF:
			case SEMI:
			case ID:
			case LPAREN:
			case RPAREN:
			case 103:
			case 104:
			case COMMA:
			case ARDEN_CURLY_BRACKETS:
			case EQUALS:
			case LITERAL_EQ:
			case LT:
			case LITERAL_LT:
			case GT:
			case LITERAL_GT:
			case LTE:
			case LITERAL_LE:
			case GTE:
			case LITERAL_GE:
			case NE:
			case LITERAL_NE:
			case ACTION_OP:
			case LITERAL_MERGE:
			case LITERAL_SORT:
			case LITERAL_SEQTO:
			case 171:
			case 172:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			expr_duration_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_49);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_duration_AST;
	}
	
	public final void expr_function() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_function_AST = null;
		
		try {      // for error handling
			if ((_tokenSet_56.member(LA(1)))) {
				expr_factor();
				astFactory.addASTChild(currentAST, returnAST);
				expr_function_AST = (AST)currentAST.root;
			}
			else if ((_tokenSet_48.member(LA(1)))) {
				{
				switch ( LA(1)) {
				case THE:
				{
					the();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case COUNT:
				case NOW:
				case MINIMUM:
				case MIN:
				case MAXIMUM:
				case MAX:
				case LAST:
				case FIRST:
				case EARLIEST:
				case LATEST:
				case EXIST:
				case EXISTS:
				case AVG:
				case AVERAGE:
				case SUM:
				case MEDIAN:
				case TRUE:
				case FALSE:
				case OF:
				case TIME:
				case INTLIT:
				case ID:
				case LPAREN:
				case STRING_LITERAL:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				{
				switch ( LA(1)) {
				case NOW:
				case MINIMUM:
				case MIN:
				case MAXIMUM:
				case MAX:
				case LAST:
				case FIRST:
				case EARLIEST:
				case LATEST:
				case TRUE:
				case FALSE:
				case OF:
				case INTLIT:
				case ID:
				case LPAREN:
				case STRING_LITERAL:
				{
					from_of_func_op();
					astFactory.addASTChild(currentAST, returnAST);
					{
					switch ( LA(1)) {
					case OF:
					{
						AST tmp469_AST = null;
						tmp469_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp469_AST);
						match(OF);
						break;
					}
					case NOW:
					case TRUE:
					case FALSE:
					case INTLIT:
					case ID:
					case LPAREN:
					case STRING_LITERAL:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					expr_factor();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case COUNT:
				case EXIST:
				case EXISTS:
				case AVG:
				case AVERAGE:
				case SUM:
				case MEDIAN:
				case TIME:
				{
					of_func_op();
					astFactory.addASTChild(currentAST, returnAST);
					{
					if ((LA(1)==OF)) {
						AST tmp470_AST = null;
						tmp470_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp470_AST);
						match(OF);
					}
					else if ((_tokenSet_48.member(LA(1)))) {
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
					expr_function();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				expr_function_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_71);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_function_AST;
	}
	
	public final void of_func_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST of_func_op_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case COUNT:
			case EXIST:
			case EXISTS:
			case AVG:
			case AVERAGE:
			case SUM:
			case MEDIAN:
			{
				of_read_func_op();
				astFactory.addASTChild(currentAST, returnAST);
				of_func_op_AST = (AST)currentAST.root;
				break;
			}
			case TIME:
			{
				of_noread_func_op();
				astFactory.addASTChild(currentAST, returnAST);
				of_func_op_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_48);
			} else {
			  throw ex;
			}
		}
		returnAST = of_func_op_AST;
	}
	
	public final void expr_factor_atom() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_factor_atom_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ID:
			{
				AST tmp471_AST = null;
				tmp471_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp471_AST);
				match(ID);
				expr_factor_atom_AST = (AST)currentAST.root;
				break;
			}
			case LPAREN:
			{
				match(LPAREN);
				expr();
				astFactory.addASTChild(currentAST, returnAST);
				match(RPAREN);
				expr_factor_atom_AST = (AST)currentAST.root;
				break;
			}
			case TRUE:
			case FALSE:
			{
				boolean_value();
				astFactory.addASTChild(currentAST, returnAST);
				expr_factor_atom_AST = (AST)currentAST.root;
				break;
			}
			case STRING_LITERAL:
			{
				AST tmp474_AST = null;
				tmp474_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp474_AST);
				match(STRING_LITERAL);
				expr_factor_atom_AST = (AST)currentAST.root;
				break;
			}
			default:
				if ((LA(1)==INTLIT)) {
					AST tmp475_AST = null;
					tmp475_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp475_AST);
					match(INTLIT);
					expr_factor_atom_AST = (AST)currentAST.root;
				}
				else if ((LA(1)==NOW||LA(1)==INTLIT)) {
					time_value();
					astFactory.addASTChild(currentAST, returnAST);
					expr_factor_atom_AST = (AST)currentAST.root;
				}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_47);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_factor_atom_AST;
	}
	
	public final void as_func_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST as_func_op_AST = null;
		
		try {      // for error handling
			AST tmp476_AST = null;
			tmp476_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp476_AST);
			match(LITERAL_NUMBER);
			as_func_op_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = as_func_op_AST;
	}
	
	public final void of_noread_func_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST of_noread_func_op_AST = null;
		
		try {      // for error handling
			AST tmp477_AST = null;
			tmp477_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp477_AST);
			match(TIME);
			of_noread_func_op_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_48);
			} else {
			  throw ex;
			}
		}
		returnAST = of_noread_func_op_AST;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"\"and\"",
		"WEIRD_IDENT",
		"\"is\"",
		"\"are\"",
		"\"was\"",
		"\"were\"",
		"\"count\"",
		"\"in\"",
		"\"less\"",
		"\"the\"",
		"\"than\"",
		"\"from\"",
		"\"before\"",
		"\"after\"",
		"\"ago\"",
		"\"write\"",
		"\"at\"",
		"\"let\"",
		"\"now\"",
		"\"be\"",
		"\"year\"",
		"\"years\"",
		"\"if\"",
		"\"it\"",
		"\"they\"",
		"\"not\"",
		"\"or\"",
		"\"then\"",
		"\"read\"",
		"\"minimum\"",
		"\"min\"",
		"\"maximum\"",
		"\"max\"",
		"\"last\"",
		"\"first\"",
		"\"earliest\"",
		"\"lastest\"",
		"\"event\"",
		"\"where\"",
		"\"exist\"",
		"\"exists\"",
		"\"past\"",
		"\"days\"",
		"\"day\"",
		"\"month\"",
		"\"months\"",
		"\"week\"",
		"\"weeks\"",
		"\"avg\"",
		"\"average\"",
		"\"sum\"",
		"\"median\"",
		"\"conclude\"",
		"\"else\"",
		"\"elseif\"",
		"\"endif\"",
		"\"true\"",
		"\"false\"",
		"\"data\"",
		"\"logic\"",
		"\"action\"",
		"\"maintenance\"",
		"\"library\"",
		"\"filename\"",
		"\"mlmname\"",
		"\"of\"",
		"\"time\"",
		"\"within\"",
		"\"end\"",
		"COLON",
		"\"knowledge\"",
		"\"title\"",
		";;",
		"DOT",
		"MINUS",
		"UNDERSCORE",
		"\"arden\"",
		"\"ASTM-E\"",
		"INTLIT",
		"\"version\"",
		"DIGIT",
		"\"institution\"",
		"\"author\"",
		"SEMI",
		"\"specialist\"",
		"\"date\"",
		"\"validation\"",
		"\"production\"",
		"\"research\"",
		"\"testing\"",
		"\"expired\"",
		"TIMES",
		"an identifier",
		"LPAREN",
		"RPAREN",
		"\":\"",
		"\"T\"",
		"\"t\"",
		"\".\"",
		"\"+\"",
		"\"-\"",
		"\"Z\"",
		"\"z\"",
		"\"purpose\"",
		"\"explanation\"",
		"\"keywords\"",
		"\"citations\"",
		"\"SUPPORT\"",
		"\"REFUTE\"",
		"\"links\"",
		"SINGLE_QUOTE",
		"\"type\"",
		"\"data-driven\"",
		"\"data_driven\"",
		"COMMENT",
		"ML_COMMENT",
		"BECOMES",
		"COMMA",
		"\"EVENT\"",
		"\"Event\"",
		"ARDEN_CURLY_BRACKETS",
		"\"PRESENT\"",
		"\"NULL\"",
		"\"BOOLEAN\"",
		"\"NUMBER\"",
		"\"DURATION\"",
		"\"STRING\"",
		"\"LIST\"",
		"\"OBJECT\"",
		"\"GREATER\"",
		"\"EQUAL\"",
		"\"hour\"",
		"\"hours\"",
		"\"minute\"",
		"\"minutes\"",
		"\"second\"",
		"\"seconds\"",
		"\"OCCUR\"",
		"\"Occur\"",
		"\"occur\"",
		"\"OCCURS\"",
		"\"Occurs\"",
		"\"occurs\"",
		"\"OCCURRED\"",
		"\"Occurred\"",
		"\"priority\"",
		"\"evoke\"",
		"\"CALL\"",
		"\"Any\"",
		"EQUALS",
		"\"EQ\"",
		"LT",
		"\"LT\"",
		"GT",
		"\"GT\"",
		"LTE",
		"\"LE\"",
		"GTE",
		"\"GE\"",
		"NE",
		"\"NE\"",
		"ACTION_OP",
		"\"urgency\"",
		"\"MERGE\"",
		"\"SORT\"",
		"\"DATA\"",
		"\"SEQTO\"",
		"\"*\"",
		"\"/\"",
		"STRING_LITERAL",
		"WS",
		"LBRACKET",
		"RBRACKET",
		"DOTDOT",
		"NOT_EQUALS",
		"PLUS",
		"DIV",
		"LCURLY",
		"RCURLY"
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 0L, 4L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 0L, 1024L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 0L, 256L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { -3588805957255216L, 15032647905L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 0L, 24L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 0L, 589824L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 0L, 524288L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { 0L, 2097152L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = { 0L, 4194304L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = { 0L, 16777216L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = { 0L, 33554432L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = { 0L, 67108864L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = { 0L, 17592186044416L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = { 0L, 35184372088832L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = { 0L, 633318697600000L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	private static final long[] mk_tokenSet_16() {
		long[] data = { 0L, 562949953422336L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
	private static final long[] mk_tokenSet_17() {
		long[] data = { 4611686018427387904L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
	private static final long[] mk_tokenSet_18() {
		long[] data = { 1008806316604391424L, 4303355904L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
	private static final long[] mk_tokenSet_19() {
		long[] data = { 0L, 0L, 6291456L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_19 = new BitSet(mk_tokenSet_19());
	private static final long[] mk_tokenSet_20() {
		long[] data = { 0L, 0L, 4194304L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_20 = new BitSet(mk_tokenSet_20());
	private static final long[] mk_tokenSet_21() {
		long[] data = { -9223372036854775808L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_21 = new BitSet(mk_tokenSet_21());
	private static final long[] mk_tokenSet_22() {
		long[] data = { 1080863910642319360L, 4303355904L, 137438953472L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_22 = new BitSet(mk_tokenSet_22());
	private static final long[] mk_tokenSet_23() {
		long[] data = { 0L, 1L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_23 = new BitSet(mk_tokenSet_23());
	private static final long[] mk_tokenSet_24() {
		long[] data = { 0L, 256L, 274877906944L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_24 = new BitSet(mk_tokenSet_24());
	private static final long[] mk_tokenSet_25() {
		long[] data = { 0L, 4096L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_25 = new BitSet(mk_tokenSet_25());
	private static final long[] mk_tokenSet_26() {
		long[] data = { 1085303733815319506L, 1297038372023316480L, 32710437380000L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_26 = new BitSet(mk_tokenSet_26());
	private static final long[] mk_tokenSet_27() {
		long[] data = { -3588805957255214L, 1125932128400097L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_27 = new BitSet(mk_tokenSet_27());
	private static final long[] mk_tokenSet_28() {
		long[] data = { 0L, 262144L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_28 = new BitSet(mk_tokenSet_28());
	private static final long[] mk_tokenSet_29() {
		long[] data = { 0L, 8246337212416L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_29 = new BitSet(mk_tokenSet_29());
	private static final long[] mk_tokenSet_30() {
		long[] data = { -3588805957255216L, 15041041121L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_30 = new BitSet(mk_tokenSet_30());
	private static final long[] mk_tokenSet_31() {
		long[] data = { 0L, 8392704L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_31 = new BitSet(mk_tokenSet_31());
	private static final long[] mk_tokenSet_32() {
		long[] data = { 0L, 8388608L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_32 = new BitSet(mk_tokenSet_32());
	private static final long[] mk_tokenSet_33() {
		long[] data = { 67580382689625088L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_33 = new BitSet(mk_tokenSet_33());
	private static final long[] mk_tokenSet_34() {
		long[] data = { 2190433320960L, 1152921513197043712L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_34 = new BitSet(mk_tokenSet_34());
	private static final long[] mk_tokenSet_35() {
		long[] data = { 3526347089095173120L, 144115200961020000L, 36833639530496L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_35 = new BitSet(mk_tokenSet_35());
	private static final long[] mk_tokenSet_36() {
		long[] data = { 1080863912790851584L, 21483225088L, 137438953472L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_36 = new BitSet(mk_tokenSet_36());
	private static final long[] mk_tokenSet_37() {
		long[] data = { 4607213200904037376L, 144115200969408608L, 36971078483968L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_37 = new BitSet(mk_tokenSet_37());
	private static final long[] mk_tokenSet_38() {
		long[] data = { 3526347086947689472L, 1152921517492011104L, 35184372088832L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_38 = new BitSet(mk_tokenSet_38());
	private static final long[] mk_tokenSet_39() {
		long[] data = { 3458764513824735232L, 1152921517492011040L, 35184372088832L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_39 = new BitSet(mk_tokenSet_39());
	private static final long[] mk_tokenSet_40() {
		long[] data = { 6589016834050L, 1152921530385039488L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_40 = new BitSet(mk_tokenSet_40());
	private static final long[] mk_tokenSet_41() {
		long[] data = { 402653184L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_41 = new BitSet(mk_tokenSet_41());
	private static final long[] mk_tokenSet_42() {
		long[] data = { 0L, 0L, 2088960L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_42 = new BitSet(mk_tokenSet_42());
	private static final long[] mk_tokenSet_43() {
		long[] data = { 537001984L, 128L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_43 = new BitSet(mk_tokenSet_43());
	private static final long[] mk_tokenSet_44() {
		long[] data = { 2190433320962L, 1152921530385039360L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_44 = new BitSet(mk_tokenSet_44());
	private static final long[] mk_tokenSet_45() {
		long[] data = { 0L, 72057594037927936L, 137472507904L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_45 = new BitSet(mk_tokenSet_45());
	private static final long[] mk_tokenSet_46() {
		long[] data = { 2190433320962L, 1152921513196781568L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_46 = new BitSet(mk_tokenSet_46());
	private static final long[] mk_tokenSet_47() {
		long[] data = { 1085303733815319506L, 1297038372023312384L, 32710437380000L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_47 = new BitSet(mk_tokenSet_47());
	private static final long[] mk_tokenSet_48() {
		long[] data = { 3526347086947689472L, 12885164128L, 35184372088832L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_48 = new BitSet(mk_tokenSet_48());
	private static final long[] mk_tokenSet_49() {
		long[] data = { 1080870502881788882L, 1297038372023304192L, 32710437371904L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_49 = new BitSet(mk_tokenSet_49());
	private static final long[] mk_tokenSet_50() {
		long[] data = { 3526382271319770112L, 12885164128L, 35184372088832L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_50 = new BitSet(mk_tokenSet_50());
	private static final long[] mk_tokenSet_51() {
		long[] data = { 1080870502881297362L, 1297036722755862528L, 6322158305280L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_51 = new BitSet(mk_tokenSet_51());
	private static final long[] mk_tokenSet_52() {
		long[] data = { 6144L, 0L, 32L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_52 = new BitSet(mk_tokenSet_52());
	private static final long[] mk_tokenSet_53() {
		long[] data = { 0L, 144115205255729152L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_53 = new BitSet(mk_tokenSet_53());
	private static final long[] mk_tokenSet_54() {
		long[] data = { 1073741824L, 144115205255729152L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_54 = new BitSet(mk_tokenSet_54());
	private static final long[] mk_tokenSet_55() {
		long[] data = { 0L, 17179869184L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_55 = new BitSet(mk_tokenSet_55());
	private static final long[] mk_tokenSet_56() {
		long[] data = { 3458764513824735232L, 12885164032L, 35184372088832L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_56 = new BitSet(mk_tokenSet_56());
	private static final long[] mk_tokenSet_57() {
		long[] data = { 3458766704258056192L, 12885164032L, 35184372088832L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_57 = new BitSet(mk_tokenSet_57());
	private static final long[] mk_tokenSet_58() {
		long[] data = { 1085303733815319506L, 1297038372023304192L, 32710437380000L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_58 = new BitSet(mk_tokenSet_58());
	private static final long[] mk_tokenSet_59() {
		long[] data = { 1080868312447975440L, 144115209559080960L, 6184752906240L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_59 = new BitSet(mk_tokenSet_59());
	private static final long[] mk_tokenSet_60() {
		long[] data = { 3526347086948738048L, 144115218149277792L, 36971078483968L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_60 = new BitSet(mk_tokenSet_60());
	private static final long[] mk_tokenSet_61() {
		long[] data = { 1048576L, 17188257792L, 137438953472L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_61 = new BitSet(mk_tokenSet_61());
	private static final long[] mk_tokenSet_62() {
		long[] data = { 3526347086948738048L, 144115218149277792L, 36833639530496L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_62 = new BitSet(mk_tokenSet_62());
	private static final long[] mk_tokenSet_63() {
		long[] data = { 4607210999734346752L, 144115218149277792L, 36971078483968L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_63 = new BitSet(mk_tokenSet_63());
	private static final long[] mk_tokenSet_64() {
		long[] data = { 1080863912790851584L, 144115209559080960L, 137438953472L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_64 = new BitSet(mk_tokenSet_64());
	private static final long[] mk_tokenSet_65() {
		long[] data = { 1080863912790851584L, 144115209559080960L, 1786706395136L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_65 = new BitSet(mk_tokenSet_65());
	private static final long[] mk_tokenSet_66() {
		long[] data = { 1080868310837362688L, 144115209559080960L, 1786706395136L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_66 = new BitSet(mk_tokenSet_66());
	private static final long[] mk_tokenSet_67() {
		long[] data = { 1080868310837362688L, 144115209559080960L, 6184752906240L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_67 = new BitSet(mk_tokenSet_67());
	private static final long[] mk_tokenSet_68() {
		long[] data = { 1080868311911104512L, 144115209559080960L, 6184752906240L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_68 = new BitSet(mk_tokenSet_68());
	private static final long[] mk_tokenSet_69() {
		long[] data = { 1080868311911104528L, 144115209559080960L, 6184752906240L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_69 = new BitSet(mk_tokenSet_69());
	private static final long[] mk_tokenSet_70() {
		long[] data = { 1080870502881297362L, 1297038372023304192L, 32710437371904L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_70 = new BitSet(mk_tokenSet_70());
	private static final long[] mk_tokenSet_71() {
		long[] data = { 1085303733815313362L, 1297038372023304192L, 32710437379968L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_71 = new BitSet(mk_tokenSet_71());
	
	}
