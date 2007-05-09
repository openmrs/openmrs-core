package org.openmrs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;

import org.openmrs.api.context.Context;
import org.openmrs.arden.ArdenBaseLexer;
import org.openmrs.arden.ArdenBaseParser;
import org.openmrs.arden.ArdenBaseTreeParser;
import org.openmrs.arden.MLMObject;

import antlr.BaseAST;


public class ArdenTest extends BaseTest {

	public static void RunTest(String file, MLMObject ardObj) {
	    // Use a try/catch block for parser exceptions
	    try {
	      // if we have at least one command-line argument
	      if (file.length() > 0 ) {
	        System.err.println("Parsing...");

	        // for each directory/file specified on the command line
	          doFile(new File(file), ardObj); // parse it
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
	  public static void doFile(File f, MLMObject ardObj) throws Exception {
	    // If this is a directory, walk each file/dir in that directory
	    if (f.isDirectory()) {
	      String files[] = f.list();
	   //   System.out.println("------------Total files = " + files.length);
	      for(int i=0; i < files.length; i++)
	      {
	      	doFile(new File(f, files[i]), ardObj);
	      }
	    }

	    // otherwise, if this is a mlm file, parse it!
	    else if (f.getName().substring(f.getName().length()-4).equals(".mlm")) {
	      System.err.println("-------------------------------------------");
	      System.err.println("--------------File name--" + f.getName());
	      System.err.println(f.getAbsolutePath());
	      parseFile(new FileInputStream(f), f.getAbsolutePath(), ardObj);
	    }
	  }

	  // Here's where we do the real work...
	  public static void parseFile(InputStream s, String fn, MLMObject ardObj) throws Exception {
	  	//new ArdenReadNode();
	    try {
	      int index  = fn.indexOf(".mlm");
	      String cfn = fn.substring(0,index); 
	    	  
	      // Create a scanner that reads from the input stream passed to us
	      ArdenBaseLexer lexer = new ArdenBaseLexer(s);

	      // Create a parser that reads from the scanner
	      ArdenBaseParser parser = new ArdenBaseParser(lexer);

	      // start parsing at the compilationUnit rule
	      parser.startRule();
	      BaseAST t = (BaseAST) parser.getAST();
	      OutputStream os = new FileOutputStream(cfn+".java");
	      Writer w = new OutputStreamWriter(os);
	      //t.xmlSerialize(w);
	      //w.write("\n");
	      //w.flush();
	      System.err.println("Wrote to file - " + cfn+".java");
	      //DumpASTVisitor visitor = new DumpASTVisitor ();
	      //visitor.visit(t);
	      
	      
	   //   String tree = parser.getAST().toStringList();
	      
	     System.err.println(t.toStringTree());     // prints maintenance
	     
	      ArdenBaseTreeParser treeParser = new ArdenBaseTreeParser();
	      
	      //String datastr = treeParser.data(t);
	      
	     String maintenance =  treeParser.maintenance(t, ardObj);
	 	 w.write("/********************************************************************" + "\n");
	 	 w.write(maintenance);
	 	
	 	 System.err.println(t.getNextSibling().toStringTree());   // prints library
	     String library = treeParser.library(t.getNextSibling(), ardObj);
	     w.write(library);
	     w.write("\n********************************************************************/\n");
	     w.write("package org.openmrs.arden;\n\n");
	     w.write("import java.util.Iterator;\nimport java.util.Locale;\nimport java.util.Set;\n");
	     w.write("import java.util.HashMap;\n");
	     w.write("import org.openmrs.Concept;\nimport org.openmrs.Obs;\nimport org.openmrs.Patient;\n");
	     
	     String classname = ardObj.getClassName();
	     w.write("public class " + classname + "{\n"); // Start of class
	     w.write("private Patient patient;\nprivate Locale locale;\nprivate String firstname;\n");
	     w.write("private HashMap<String, String> userVarMap;\n");
	     w.write("\n\n//Constructor\n");
	     w.write("public " + classname + "(Context c, Integer pid, Locale l){\n");
	     w.write("\n\tlocale = l;\n\tpatient = c.getPatientService().getPatient(pid);\n");
	     w.write("\tuserVarMap = new HashMap <String, String>();\n");
	     w.write("\tfirstname = patient.getPersonName().getGivenName();\n}\n\n\n");
	     w.write("public Obs getObsForConceptForPatient(Concept concept, Locale locale, Patient patient) {\n");
	     w.write("\tSet <Obs> MyObs;\n");
	     w.write("\tObs obs = new Obs();\n\t{");
	     w.write("\t\tMyObs = Context.getObsService().getObservations(patient, concept);\n");
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
	     w.write("\taction();\n");
	     w.write("\t\tString str = userVarMap.get(\"ActionStr\");\n");
	     w.write("\t\tSystem.out.println(str);\n");
	     w.write("\t}\n");
	     w.write("\treturn retVal;\n");
	     w.write("}\n\n");	// End of this function
	     w.flush();
	     
	     
	     
	     
	     System.err.println(t.getNextSibling().getNextSibling().toStringTree()); // Print data
	  	 
	       

	       
	    
	      
	   	  //treeParser.data(t,ardObj);
	      treeParser.data(t.getNextSibling().getNextSibling(),ardObj);
	   	  System.err.println("---------------------------------AFter data ----------------------------------");
	   	  	ardObj.PrintConceptMap();
	   	  System.err.println("-------------------------------------------------------------------");
	   	  
	   	  System.err.println(t.getNextSibling().getNextSibling().getNextSibling().toStringTree()); // Print logic
	      String logicstr = treeParser.logic(t.getNextSibling().getNextSibling().getNextSibling(), ardObj);
	    
	      System.err.println("---------------------------------AFter Logic ----------------------------------");
	          ardObj.PrintConceptMap();
	      System.err.println("-------------------------------------------------------------------");	      	  
	      ardObj.PrintEvaluateList();
	   //   if(ardObj.Evaluate())
	    //  {
	     //     System.err.println(t.getNextSibling().getNextSibling().getNextSibling().getNextSibling().toStringTree()); // Print action
		     
	    //	  System.err.println(t.getNextSibling().getNextSibling().toStringTree());
	    //	  System.err.println("---------------------------------CONCLUDED TRUE ----------------------------------");
	    	  
	    //	  String actionstr = treeParser.action(t.getNextSibling().getNextSibling().getNextSibling().getNextSibling(), ardObj);
	    //	  System.err.println(actionstr);
	    //  }
	      
	      ardObj.WriteEvaluate(w, "");
	      String actionstr = treeParser.action(t.getNextSibling().getNextSibling().getNextSibling().getNextSibling(), ardObj);
	      ardObj.WriteAction(actionstr, w);
		     
		     
		     w.append("}");	// end class
		     w.flush();
		     w.close();
	      
	    }
	    catch (Exception e) {
	      System.err.println("parser exception: "+e);
	      e.printStackTrace();   // so we can get stack trace		
	    }
	  }
	
	
	public void testClass() throws Exception {
		
		startup();
		
		Context.authenticate("vibha", "chicachica");
		Locale locale = Context.getLocale();
		
	//	HiRiskLeadScreen mlm = new HiRiskLeadScreen(context,1,locale);
	//	mlm.run();
	
	/*    Patient patient = new Patient();
		patient.setPatientId(1);
		PersonName pn = new PersonName("Jenny", "M", "Patient");
		patient.addName(pn);
		
		MLMObject ardObj = new MLMObject(context, locale, patient);
		//RunTest("test/arden test/HiRiskLeadScreen.mlm", ardObj ); //populates ardObj
		RunTest("test/arden test/4.mlm", ardObj ); //populates ardObj
	*/	
	//	ConceptService cs = Context.getConceptService();
	//	ObsService os = Context.getObsService();
		
	
	//	Set <Obs> MyObs ;
		
	//	String cv, temp_cn, cn;
	//	char separator = ' ';
	//	int index = 0;
	//	Iterator conceptIter;
		
	//	List <ConceptWord>  conceptsWords;
		
	//	conceptIter = ardObj.iterator();
	//	for (int i=0; i < size; i++) {
	//	while(conceptIter.hasNext()) {
	//		cv = ardObj.GetNextConceptVar();
	//		cv = (String) conceptIter.next();
	//		temp_cn = ardObj.GetConceptName(cv);
	//		index = temp_cn.indexOf("from");	// First substring
	//		cn = temp_cn.substring(1,index); 
			
			// Todo: Need a better method to find a concept
	//		conceptsWords = cs.findConcepts(/*"BLOOD LEAD LEVEL"*/cn, locale, false);  
	//		if (!conceptsWords.isEmpty()) {
	//			    ConceptWord conceptWord = conceptsWords.get(0);
	//			    Concept c = conceptWord.getConcept(); 
	//				MyObs = os.getObservations(patient, c);
	//				Iterator iter = MyObs.iterator();
	//				while(iter.hasNext())
	//			{
	//				Obs o =(Obs) iter.next();
	//				
	//			    System.out.println(o.getValueAsString(locale));
	//			 //   iter.remove();
	//			}
	//			System.out.println("Total Obs for " + cn + " = "  + MyObs.size());
	//		}
	//	}
		
		shutdown();
	}
	
	
}