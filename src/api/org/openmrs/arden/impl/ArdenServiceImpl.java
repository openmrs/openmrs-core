package org.openmrs.arden.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

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
	      parseFile(new FileInputStream(f), f.getName()/*f.getAbsolutePath()*/);
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
	private void parseFile(FileInputStream s, String fn) throws Exception {
		
		 try {
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
		     OutputStream os = new FileOutputStream("src/api/org/openmrs/arden/compiled/" + cfn+".java");
		  //   int fd = 2;
		  //   OutputStream os = new FileOutputStream(FileDescriptor.out);
		     Writer w = new OutputStreamWriter(os);
		     log.info("Writing to file - " + cfn+".java");
	
		 	 w.write("/********************************************************************" + "\n");
		 	 w.write(maintenance);
		 	
		 	 log.debug(t.getNextSibling().toStringTree());   // prints library
		     String library = treeParser.library(t.getNextSibling(), ardObj);
		     w.write(library);
		     w.write("\n********************************************************************/\n");
		     w.write("package org.openmrs.arden.compiled;\n\n");
		     w.write("import java.util.Iterator;\nimport java.util.Locale;\nimport java.util.Set;\n");
		     w.write("import java.util.HashMap;\n");
		     w.write("import org.openmrs.Concept;\nimport org.openmrs.Obs;\nimport org.openmrs.Patient;\n");
		     w.write("import org.openmrs.arden.*;\n\n");
		     
		     String classname = ardObj.getClassName();
		     w.write("public class " + classname + " implements ArdenRule{\n"); // Start of class
		     w.write("private Patient patient;\nprivate Locale locale;\nprivate String firstname;\n");
		     w.write("private ArdenDataSource dataSource;\n");
		     w.write("private HashMap<String, String> userVarMap;\n");
		     w.write("\n\n//Constructor\n");
		     w.write("public " + classname + "(Patient p, ArdenDataSource d){\n");
		     w.write("\n\tlocale = c.getLocale();\n\tpatient = p;\n\tdataSource = d;\n");
		     w.write("\tuserVarMap = new HashMap <String, String>();\n");
		     w.write("\tfirstname = patient.getPatientName().getGivenName();\n");
		     w.write("\tuserVarMap.put(\"firstname\", firstname);\n");
		     w.write("\tinitAction();\n\t");		     
		     w.write("}\n\n\n"); // End of constructor
		     
		     w.write("public ArdenRule getChildren() {\n\tArdenRule rule = null;\n\treturn rule;\n}\n\n"); 
		     w.write("public ArdenRule getInstance() {\n\tArdenRule rule = null;\n\tif (this != null){\n\t\trule = this;\n\t}\n\t\treturn rule;\n}\n\n"); 
		     
		     w.flush();
		     		     
		     log.debug(t.getNextSibling().getNextSibling().toStringTree()); // Print data
		     System.out.println(t.getNextSibling().getNextSibling().toStringTree()); // Print data
		     
		   	 treeParser.data(t.getNextSibling().getNextSibling(),ardObj);
		   	 log.debug(t.getNextSibling().getNextSibling().getNextSibling().toStringTree()); // Print logic
		   	 System.out.println(t.getNextSibling().getNextSibling().getNextSibling().toStringTree()); // Print logic
		     //String logicstr = treeParser.logic(t.getNextSibling().getNextSibling().getNextSibling(), ardObj);
		    
		     ardObj.PrintEvaluateList();   // To Debug
		     ardObj.WriteEvaluate(w);
		     String actionstr = treeParser.action(t.getNextSibling().getNextSibling().getNextSibling().getNextSibling(), ardObj);
		     ardObj.WriteAction(actionstr, w);
			 
		     w.append("}");	// end class
			 w.flush();
			 w.close();
		      
		    }
		    catch (Exception e) {
		      log.error(e.getStackTrace());
		    }
	}
	
	
}