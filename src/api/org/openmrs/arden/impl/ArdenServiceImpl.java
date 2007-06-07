package org.openmrs.arden.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.arden.ArdenBaseLexer;
import org.openmrs.arden.ArdenBaseParser;
import org.openmrs.arden.ArdenBaseTreeParser;
import org.openmrs.arden.ArdenService;
import org.openmrs.arden.MLMObject;

import antlr.BaseAST;

/**
 * Arden-related services
 * 
 * @author Vibha Anand
 *
 * @version 1.0
 */
public class ArdenServiceImpl implements ArdenService {

	private final Log log = LogFactory.getLog(getClass());
	
	public ArdenServiceImpl() {	}
	
	/**
	 * 
	 * @param file - mlm file to be parsed
	 */
	public void compileFile(String file) {
	 try {
	      // if we have at least one command-line argument
	      if (file.length() > 0 ) {
	    	  log.debug("Parsing file" + file);
	    
	    	  // for each directory/file specified on the command line
	          doFile(new File(file)); // parse it  
	    	  
	      }
	    }
	    catch(Exception e) {
	    	log.error(e.getStackTrace());
	    }	
	}
	
	/**
	 * 
	 * @param f - parse a file or a directory
	 */
	private void doFile(File f) throws Exception {
	boolean retVal = true;
	try {
		// If this is a directory, walk each file/dir in that directory
	    if (f.isDirectory()) {
	      String files[] = f.list();
	      for(int i=0; i < files.length; i++) {
	      	doFile(new File(f, files[i]));
	      }
	    }

	    // otherwise, if this is a mlm file, parse it!
	    else if (f.getName().substring(f.getName().length()-4).equals(".mlm")) {
	      log.info("Parsing file name:" + f.getName());
	      retVal = parseFile(new FileInputStream(f), f.getName()/*f.getAbsolutePath()*/);
	      if(!retVal) {
	    	  System.out.println("Please correct the compiler error!");
	       }
	    }
	 }
	 catch(Exception e) {
		 log.error(e.getMessage());
	 }
		
	}
	
	/**
	 * 
	 * @param s
	 * @param fn
	 */
	private boolean parseFile(FileInputStream s, String fn) throws Exception {
		boolean retVal = true;
		 try {
			 	Date Today = new Date(); 
		      //int index  = fn.indexOf(".mlm");
		      String cfn; // = fn.substring(0,index); 
		    
		      MLMObject ardObj = new MLMObject(Context.getLocale(), null);
		      
		      // Create a scanner that reads from the input stream passed to us
		      ArdenBaseLexer lexer = new ArdenBaseLexer(s);

		      // Create a parser that reads from the scanner
		      ArdenBaseParser parser = new ArdenBaseParser(lexer);

		      // start parsing at the compilationUnit rule
		      parser.startRule();
		      BaseAST t = (BaseAST) parser.getAST();
		  //    OutputStream os = new FileOutputStream("src/api/org/openmrs/arden/compiled/" + cfn+".java");
		  //    Writer w = new OutputStreamWriter(os);
		  //    log.info("Writing to file - " + cfn+".java");
		      	      
		      log.debug(t.toStringTree());     // prints maintenance
		     
		      ArdenBaseTreeParser treeParser = new ArdenBaseTreeParser();
		      
		     String maintenance =  treeParser.maintenance(t, ardObj);
	
		     cfn = ardObj.getClassName();
		     OutputStream os = new FileOutputStream("src/api/org/openmrs/logic/rule/" + cfn+".java");
		  //   int fd = 2;
		  //   OutputStream os = new FileOutputStream(FileDescriptor.out);
		     Writer w = new OutputStreamWriter(os);
		     log.info("Writing to file - " + cfn+".java");
	
		 	 w.write("/********************************************************************" + "\n Translated from - " + fn + " on " +  Today.toString()+ "\n\n");
		 	 w.write(maintenance);
		 	
		 	 log.debug(t.getNextSibling().toStringTree());   // prints library
		     String library = treeParser.library(t.getNextSibling(), ardObj);
		     w.write(library);
		     w.write("\n********************************************************************/\n");
		     w.write("package org.openmrs.logic.rule;\n\n");
		     w.write("import java.util.HashMap;\n");
		     w.write("import org.openmrs.Concept;\nimport org.openmrs.Patient;\n");
		     w.write("import org.openmrs.logic.Constraint;\nimport org.openmrs.logic.DateConstraint;\n");
		     w.write("import org.openmrs.logic.Duration;\nimport org.openmrs.logic.LogicDataSource;\n");
		     w.write("import org.openmrs.logic.Result;\nimport org.openmrs.logic.Rule;\n");
		     w.write("import org.openmrs.logic.Aggregation;\n\n\n");
		     
		     String classname = ardObj.getClassName();
		     w.write("public class " + classname + " extends Rule{\n\n"); // Start of class
		     w.write("private Patient patient;\nprivate String firstname;\n");
		     w.write("private LogicDataSource dataSource;\n");
		     w.write("private HashMap<String, String> userVarMap;\n\n\n\n");
		     
		     w.flush();
		     		     
		     log.debug(t.getNextSibling().getNextSibling().toStringTree()); // Print data
		     System.out.println(t.getNextSibling().getNextSibling().toStringTree()); // Print data
		     
		   	 treeParser.data(t.getNextSibling().getNextSibling(),ardObj);
		   	 log.debug(t.getNextSibling().getNextSibling().getNextSibling().toStringTree()); // Print logic
		   	 System.out.println(t.getNextSibling().getNextSibling().getNextSibling().toStringTree()); // Print logic
		     String logicstr = treeParser.logic(t.getNextSibling().getNextSibling().getNextSibling(), ardObj);
		    
		     ardObj.PrintConceptMap();	   // To Debug
		     ardObj.PrintEvaluateList();   // To Debug
		     retVal = ardObj.WriteEvaluate(w, classname);
		     if(retVal) {
			     String actionstr = treeParser.action(t.getNextSibling().getNextSibling().getNextSibling().getNextSibling(), ardObj);
			     ardObj.WriteAction(actionstr, w);
			     w.append("}");	// end class
				 w.flush();
				 w.close();
			 }
		     else {   //delete the compiled file so far
		    	 w.flush();
				 w.close();
		    	 boolean success = (new File("src/api/org/openmrs/logic/rule/" + cfn + ".java")).delete();
		    	    if (!success) {
		    	        System.out.println("Incomplete compiled file " + cfn + ".java cannot be deleted!");
		    	    }
          
		     }
		   		      
		    }
		    catch (Exception e) {
		      log.error(e.getStackTrace());
		    }
		    return retVal;
	}
	
	
}