package org.openmrs.arden;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOContext;

import antlr.BaseAST;

/**
 * Arden-related services
 * 
 * @author Vibha Anand
 *
 * @version 1.0
 */
public class ArdenService {

	private final Log log = LogFactory.getLog(getClass());

	private Context context;

	private DAOContext daoContext;

	public ArdenService(Context c, DAOContext d) {
		this.context = c;
		this.daoContext = d;
	}
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
		      int index  = fn.indexOf(".mlm");
		      String cfn = fn.substring(0,index); 
		    
		      MLMObject ardObj = new MLMObject(context, context.getLocale(), null);
		      
		      // Create a scanner that reads from the input stream passed to us
		      ArdenBaseLexer lexer = new ArdenBaseLexer(s);

		      // Create a parser that reads from the scanner
		      ArdenBaseParser parser = new ArdenBaseParser(lexer);

		      // start parsing at the compilationUnit rule
		      parser.startRule();
		      BaseAST t = (BaseAST) parser.getAST();
		      OutputStream os = new FileOutputStream("src/api/org/openmrs/arden/compiled/" + cfn+".java");
		      Writer w = new OutputStreamWriter(os);
		      log.info("Writing to file - " + cfn+".java");
		      	      
		      log.debug(t.toStringTree());     // prints maintenance
		     
		      ArdenBaseTreeParser treeParser = new ArdenBaseTreeParser();
		      
		     String maintenance =  treeParser.maintenance(t, ardObj);
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
		     w.write("import org.openmrs.api.context.Context;\n\n");
		     
		     String classname = ardObj.getClassName();
		     w.write("public class " + classname + "{\n"); // Start of class
		     w.write("private Context context;\nprivate Patient patient;\nprivate Locale locale;\nprivate String firstname;\n");
		     w.write("private HashMap<String, String> userVarMap;\n");
		     w.write("\n\n//Constructor\n");
		     w.write("public " + classname + "(Context c, Integer pid, Locale l){\n");
		     w.write("\tcontext = c;\n\tlocale = l;\n\tpatient = c.getPatientService().getPatient(pid);\n");
		     w.write("\tuserVarMap = new HashMap <String, String>();\n");
		     w.write("\tfirstname = patient.getPatientName().getGivenName();\n");
		     w.write("\tuserVarMap.put(\"firstname\", firstname);\n");
		     w.write("\tinitAction();");		     
		     w.write("}\n\n\n"); // End of constructor
		     
		     w.write("public Obs getObsForConceptForPatient(Concept concept, Locale locale, Patient patient) {\n");
		     w.write("\tSet <Obs> MyObs;\n");
		     w.write("\tObs obs = new Obs();\n\t{");
		     w.write("\t\tMyObs = context.getObsService().getObservations(patient, concept);\n");
		     w.write("\t\tIterator iter = MyObs.iterator();\n");
		     w.write("\t\tif(iter.hasNext()) {\n");
		     w.write("\t\t\twhile(iter.hasNext())	{\n");
		     w.write("\t\t\t\tobs = (Obs) iter.next();\n");
		     w.write("\t\t\t\tSystem.out.println(obs.getValueAsString(locale));\n");
		     w.write("\t\t\t}\n");
		     w.write("\t\t\t\treturn obs;\n");
		     w.write("\t\t}\n");
		     w.write("\t\telse {\n");
		     w.write("\t\t\treturn null;\n");
		     w.write("\t\t}\n");
		     w.write("\t}\n");
		     w.write("}\n\n");  // End of this function
		     
		     w.write("public boolean run() {\n");
		     w.write("\tboolean retVal = false;\n");
		     w.write("\tif(evaluate()) {\n");
		     w.write("\t\tString str = action();\n");
		     w.write("\t\tSystem.out.println(str);\n");
		     w.write("\t}\n");
		     w.write("\treturn retVal;\n");
		     w.write("}\n\n");	// End of this function
		     w.flush();
		     		     
		     log.debug(t.getNextSibling().getNextSibling().toStringTree()); // Print data
		  	 		      
		   	 treeParser.data(t.getNextSibling().getNextSibling(),ardObj);
		   	 log.debug(t.getNextSibling().getNextSibling().getNextSibling().toStringTree()); // Print logic
		   	 System.out.println(t.getNextSibling().getNextSibling().getNextSibling().toStringTree()); // Print logic
		     String logicstr = treeParser.logic(t.getNextSibling().getNextSibling().getNextSibling(), ardObj);
		    
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