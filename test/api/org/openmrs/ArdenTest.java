package org.openmrs;

import junit.framework.TestCase;
import java.io.*;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextFactory;
import org.openmrs.api.db.hibernate.HibernateUtil;
import java.util.*;
import java.util.Locale;
import java.util.Iterator;

import antlr.CommonAST;
import antlr.collections.AST;
import antlr.*;
import org.openmrs.arden.ArdenBaseLexer;
import org.openmrs.arden.ArdenBaseParser;
import org.openmrs.arden.ArdenBaseTreeParser;
import org.openmrs.arden.MLMObject;
import org.openmrs.api.*;


public class ArdenTest extends TestCase {

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
	      parseFile(new FileInputStream(f), ardObj);
	    }
	  }

	  // Here's where we do the real work...
	  public static void parseFile(InputStream s, MLMObject ardObj) throws Exception {
	  	//new ArdenReadNode();
	    try {
	      // Create a scanner that reads from the input stream passed to us
	      ArdenBaseLexer lexer = new ArdenBaseLexer(s);

	      // Create a parser that reads from the scanner
	      ArdenBaseParser parser = new ArdenBaseParser(lexer);

	      // start parsing at the compilationUnit rule
	      parser.startRule();
	      BaseAST t = (BaseAST) parser.getAST();
	      OutputStream os = new FileOutputStream("Testfile");
	      Writer w = new OutputStreamWriter(os);
	      t.xmlSerialize(w);
	      w.write("\n");
	      w.flush();
	      System.err.println("Wrote to file - " + "Testfile");
	      DumpASTVisitor visitor = new DumpASTVisitor ();
	      visitor.visit(t);
	      
	      
	   //   String tree = parser.getAST().toStringList();
	      
	     System.err.println(t.toStringTree());
	      
	      ArdenBaseTreeParser treeParser = new ArdenBaseTreeParser();
	      //String datastr = treeParser.data(t);
	   	  treeParser.data(t,ardObj);
	      
	      System.err.println(t.getNextSibling().toStringTree());
	      
	      String logicstr = treeParser.logic(t.getNextSibling(), ardObj);
	      String actionstr = treeParser.action(t.getNextSibling().getNextSibling());
	      
	      
	      System.err.println(actionstr);
	      System.err.println(logicstr);
	     // System.err.println(datastr);
	      ardObj.PrintConceptMap();
	      ardObj.PrintEvaluateList();
	      
	    }
	    catch (Exception e) {
	      System.err.println("parser exception: "+e);
	      e.printStackTrace();   // so we can get stack trace		
	    }
	  }
	
	
	public void testClass() throws Exception {
		
		HibernateUtil.startup();
		Context context = ContextFactory.getContext();
		context.authenticate("vibha", "chicachica");
		Locale locale = context.getLocale();
		Patient patient = new Patient();
		patient.setPatientId(1);
		
		MLMObject ardObj = new MLMObject(context, locale, patient);
		RunTest("test/arden test/HiRiskLeadScreen.mlm", ardObj ); //populates ardObj
		
		
	//	ConceptService cs = context.getConceptService();
	//	ObsService os = context.getObsService();
		
	
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
		
		HibernateUtil.shutdown();
	}
	
	
}