/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.arden.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.arden.ArdenBaseLexer;
import org.openmrs.arden.ArdenBaseParser;
import org.openmrs.arden.ArdenBaseTreeParser;
import org.openmrs.arden.ArdenService;
import org.openmrs.arden.MLMObject;
import org.openmrs.logic.result.Result.Datatype;
import org.openmrs.logic.rule.RuleParameterInfo;

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
	public void compileFile(String file, String outFolder) {
	 try {
	      // if we have at least one command-line argument
	      if (file.length() > 0 ) {
	    	  log.debug("Parsing file" + file);
	    
	    	  // for each directory/file specified on the command line
	          doFile(new File(file), outFolder); // parse it  
	    	  
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
	private void doFile(File f, String outFolder) throws Exception {
	boolean retVal = true;
	try {
		// If this is a directory, walk each file/dir in that directory
	    if (f.isDirectory()) {
	      String files[] = f.list();
	      for(int i=0; i < files.length; i++) {
	      	doFile(new File(f, files[i]), outFolder);
	      }
	    }

	    // otherwise, if this is a mlm file, parse it!
	    else if (f.getName().substring(f.getName().length()-4).equals(".mlm")) {
	      log.info("Parsing file name:" + f.getName());
	      retVal = parseFile(new FileInputStream(f), f.getName()/*f.getAbsolutePath()*/, outFolder);
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
	private boolean parseFile(FileInputStream s, String fn, String outFolder) throws Exception {
		boolean retVal = true;
		 try {
			  Date Today = new Date(); 
		      String cfn;  
		      
		      AdministrationService adminService = Context.getAdministrationService();
			  String packagePrefix = adminService.getGlobalProperty("dss.rulePackagePrefix");
		    
		      MLMObject ardObj = new MLMObject(Context.getLocale(), null);
		      
		      // Create a scanner that reads from the input stream passed to us
		      ArdenBaseLexer lexer = new ArdenBaseLexer(s);

		      // Create a parser that reads from the scanner
		      ArdenBaseParser parser = new ArdenBaseParser(lexer);
		      
		      // start parsing at the compilationUnit rule
		      parser.startRule();
		      BaseAST t = (BaseAST) parser.getAST();
	      	      
		      log.debug(t.toStringTree());     // prints maintenance
		     
		      ArdenBaseTreeParser treeParser = new ArdenBaseTreeParser();
		      
		      
		     String maintenance =  treeParser.maintenance(t, ardObj);
	
		     cfn = ardObj.getClassName();
		     OutputStream os = new FileOutputStream(outFolder + cfn+".java");
				
		     Writer w = new OutputStreamWriter(os);
		     log.info("Writing to file - " + cfn+".java");
	
		 	 w.write("/********************************************************************" + "\n Translated from - " + fn + " on " +  Today.toString()+ "\n\n");
		 	 w.write(maintenance);
		 	
		 	 t = (BaseAST)t.getNextSibling(); // Move to library
		 	 log.debug(t.toStringTree());   // prints library
		     String library = treeParser.library(t, ardObj);
		     w.write(library);
		     w.write("\n********************************************************************/\n");
		     if(0 == packagePrefix.length())
		     {
		    	 w.write("package org.openmrs.module.dss.rule;\n\n");
		     }
		     else
		     {	 
		    	 w.write("package " + packagePrefix + ";\n\n");
		     }
		     w.write("import java.util.ArrayList;\n");
		     w.write("import java.util.HashMap;\n");
		     w.write("import java.util.List;\n");
		     w.write("import java.util.Map;\n");
		     w.write("import java.util.Set;\n");
			    
		     w.write("import org.apache.commons.logging.Log;\n");
		     w.write("import org.apache.commons.logging.LogFactory;\n");
		     w.write("import org.openmrs.Patient;\n");
             w.write("import org.openmrs.api.context.Context;\n");
             w.write("import org.openmrs.logic.LogicContext;\n");
             w.write("import org.openmrs.logic.LogicCriteria;\n");
             w.write("import org.openmrs.logic.LogicException;\n");
             w.write("import org.openmrs.logic.LogicService;\n");
             w.write("import org.openmrs.logic.Rule;\n");
             w.write("import org.openmrs.logic.result.Result;\n");
             w.write("import org.openmrs.logic.result.Result.Datatype;\n");
             w.write("import org.openmrs.logic.rule.RuleParameterInfo;\n");
             w.write("import org.openmrs.module.dss.DssRule;\n");
     
		     
		     String classname = ardObj.getClassName();
		     w.write("public class " + classname + " implements Rule, DssRule{\n\n"); // Start of class
		     w.write("private Patient patient;\nprivate String firstname;\n");
		     w.write("private HashMap<String, String> userVarMap;\n\n\n\n");
		     
		     w.write("private Log log = LogFactory.getLog(this.getClass());\n");
		     w.write("private LogicService logicService = Context.getLogicService();\n\n\n");
		     
		     w.write("public void activate(String token) throws LogicException " + "{\n\ttry\n\t{\n\t" +
		    	    "\tlogicService.addRule(token, this);\n" + 
		    	"\t} catch (Exception e)\n\t{\n\t\tlogicService.updateRule(token, this);\n\t}\n}\n");
		     w.flush();
		     
		     
		     /**************************************************************************************
		      * Implement the other interface methods
		      */
		     
		     String str = "";
		     w.write("/*** @see org.openmrs.logic.rule.Rule#getDuration()*/\n\t" +
		     "public int getDuration() {\n\t\treturn 60*30;   // 30 minutes\n}\n\n");

		     w.write("/*** @see org.openmrs.logic.rule.Rule#getDatatype(String)*/\n\t" +
		     "public Datatype getDatatype(String token) {\n\t// TODO add per-token datatype retrieval\n\t" +
		         "return Datatype.TEXT;\n}\n\n");
		     
		     w.write("/*** @see org.openmrs.logic.rule.Rule#getParameterList()*/\n\t" +
		     "public Set<RuleParameterInfo> getParameterList() {\n\treturn null;\n\t}\n\n");

		     w.write("/*** @see org.openmrs.logic.rule.Rule#getDependencies()*/\n\t" +
		     "public String[] getDependencies() {\n\treturn new String[] { };\n}\n\n");
		     
		     w.write("/*** @see org.openmrs.logic.rule.Rule#getTTL()*/\n\t" + 
		     "public int getTTL() {\n\t\treturn 0; //60 * 30; // 30 minutes\n}\n\n");

		     w.write("/*** @see org.openmrs.logic.rule.Rule#getDatatype(String)*/\n\t" +
		     "public Datatype getDefaultDatatype() {\n\treturn Datatype.CODED;\n}\n\n");
             
		     str = ardObj.getAuthor();
		     w.write("/*(non-Javadoc)\n" +
				      "* @see org.openmrs.module.dssmodule.ATDRule#getAuthor()\n" +
				      "*/" + 
				     "public String getAuthor()\n" +
				     "{\n" +
				     "	// TODO Auto-generated method stub\n\t" +
				     	"return \"" + str + "\";\n" +
				     "}\n\n");
		     
		     str = ardObj.getCitations();
		     w.write("/*(non-Javadoc)\n" +
				      "* @see org.openmrs.module.dssmodule.ATDRule#getCitations()\n" +
				      "*/" + 
				     "public String getCitations()\n" +
				     "{\n" +
				     "	// TODO Auto-generated method stub\n\t" +
				     "return \"" + str + "\";\n" +
				     "}\n\n");
		     
		    
		     str = ardObj.getDate();
		     w.write("/*(non-Javadoc)\n" +
				      "* @see org.openmrs.module.dssmodule.ATDRule#getDate()\n" +
				      "*/" + 
				     "public String getDate()\n" +
				     "{\n" +
				     "	// TODO Auto-generated method stub\n\t" +
				     "return \"" + str + "\";\n" +
				     "}\n\n");
		     str = ardObj.getExplanation();
		     w.write("/*(non-Javadoc)\n" +
				      "* @see org.openmrs.module.dssmodule.ATDRule#getExplanation()\n" +
				      "*/" + 
				     "public String getExplanation()\n" +
				     "{\n" +
				     "	// TODO Auto-generated method stub\n\t" +
				     "return \"" + str + "\";\n" +
				     "}\n\n");
		    
		     str = ardObj.getInstitution();
		     w.write("/*(non-Javadoc)\n" +
				      "* @see org.openmrs.module.dssmodule.ATDRule#getInstitution()\n" +
				      "*/" + 
				     "public String getInstitution()\n" +
				     "{\n" +
				     "	// TODO Auto-generated method stub\n\t" +
				     	"return \"" + str + "\";\n" +
				     "}\n\n");
		     str = ardObj.getKeywords();		     
		     w.write("/*(non-Javadoc)\n" +
				      "* @see org.openmrs.module.dssmodule.ATDRule#getKeywords()\n" +
				      "*/" + 
				     "public String getKeywords()\n" +
				     "{\n" +
				     "	// TODO Auto-generated method stub\n\t" +
				     "return \"" + str + "\";\n" +
				     "}\n\n");
		     str = ardObj.getLinks();
		     w.write("/*(non-Javadoc)\n" +
				      "* @see org.openmrs.module.dssmodule.ATDRule#getLinks()\n" +
				      "*/" + 
				     "public String getLinks()\n" +
				     "{\n" +
				     "	// TODO Auto-generated method stub\n\t" +
				     "return \"" + str + "\";\n" +
				     "}\n\n");
		    
		     str = ardObj.getPurpose();
		     w.write("/*(non-Javadoc)\n" +
				      "* @see org.openmrs.module.dssmodule.ATDRule#getPurpose()\n" +
				      "*/" + 
				     "public String getPurpose()\n" +
				     "{\n" +
				     "	// TODO Auto-generated method stub\n\t" +
				     "return \"" + str + "\";\n" +
				     "}\n\n");
		     str = ardObj.getSpecialist();
		     w.write("/*(non-Javadoc)\n" +
				      "* @see org.openmrs.module.dssmodule.ATDRule#getSpecialist()\n" +
				      "*/" + 
				     "public String getSpecialist()\n" +
				     "{\n" +
				     "	// TODO Auto-generated method stub\n\t" +
				     "return \"" + str + "\";\n" +
				     "}\n\n");
		    
		     str = ardObj.getTitle();
		     w.write("/*(non-Javadoc)\n" +
				      "* @see org.openmrs.module.dssmodule.ATDRule#getTitle()\n" +
				      "*/" + 
				     "public String getTitle()\n" +
				     "{\n" +
				     "	// TODO Auto-generated method stub\n\t" +
				     	"return \"" + str + "\";\n" +
				     "}\n\n");
		     double d = ardObj.getVersion();
		     w.write("/*(non-Javadoc)\n" +
				      "* @see org.openmrs.module.dssmodule.ATDRule#getVersion()\n" +
				      "*/" + 
				     "public Double getVersion()\n" +
				     "{\n" +
				     "	// TODO Auto-generated method stub\n\t" +
				     "return " + d + ";\n" +
				     "}\n\n");
		     str = ardObj.getType();
		     if(str == null){
		    	    w.write("/* (non-Javadoc)\n"+
		                     "* @see org.openmrs.module.dssmodule.ATDRule#getType()\n"+
		                     "*/\n"+
		                     "public String getType()\n"+
		                     "{\n"+
		                     "return null;\n" +
		                     "}\n\n");
				 	 
		     }
		     else {	 
			     w.write("/* (non-Javadoc)\n"+
	                     "* @see org.openmrs.module.dssmodule.ATDRule#getType()\n"+
	                     "*/\n"+
	                     "public String getType()\n"+
	                     "{\n"+
	                     "return \"" + str + "\";\n" +
	                     "}\n\n");
			 }
		     w.write("/* (non-Javadoc)\n"
			        + "* @see org.openmrs.module.dssmodule.ATDRule#postprocessing(org.openmrs.logic.result.Result)\n"
			        + "*/\n"
			        + "public String postprocessing(Result ruleResult)\n"
			        + "{\n" + "if(ruleResult != null)\n" + "{\n"
			        + "return ruleResult.toString();\n" + "}\n"
			        + "return \"\";\n" + "}\n\n");
		    		     
		     /**************************************************************************************/	
		     
		     t = (BaseAST)t.getNextSibling();  // Move to Knowledge
		     log.debug(t.toStringTree());   // prints knowledge
		     String knowledge_text = treeParser.knowledge_text(t, ardObj);
		     
		     
		     /**************************************************Write Knowledge dependent section**********************************************/
		     int p = ardObj.getPriority();
		     w.write("/*(non-Javadoc)\n" +
		      "* @see org.openmrs.module.dssmodule.ATDRule#getPriority()\n" +
		      "*/" + 
		     "public Integer getPriority()\n" +
		     "{\n" +
		     "	// TODO Auto-generated method stub\n\t" +
		          "return " + p + ";\n" +
		     "}\n\n");

		     str = ardObj.getData();
		     w.write("/*(non-Javadoc)\n" +
				      "* @see org.openmrs.module.dssmodule.ATDRule#getData()\n" +
				      "*/" + 
				     "public String getData()\n" +
				     "{\n" +
				     "	// TODO Auto-generated method stub\n\t" +
				     "return \"" + str + "\";\n" +
				     "}\n\n");
		     
		     str = ardObj.getLogic();
		     w.write("/*(non-Javadoc)\n" +
				      "* @see org.openmrs.module.dssmodule.ATDRule#getLogic()\n" +
				      "*/" + 
				     "public String getLogic()\n" +
				     "{\n" +
				     "	// TODO Auto-generated method stub\n\t" +
				     "return \"" + str + "\";\n" +
				     "}\n\n");
		     
		     str = ardObj.getAction();
		     w.write("/*(non-Javadoc)\n" +
				      "* @see org.openmrs.module.dssmodule.ATDRule#getAction()\n" +
				      "*/" + 
				     "public String getAction()\n" +
				     "{\n" +
				     "	// TODO Auto-generated method stub\n\t" +
				     "return \"" + str + "\";\n" +
				     "}\n\n");
		     
		    /*************************************************************************************************************************************/
		     
	         // Move back to knowledge tree to actually start converting data logic action
		     t = (BaseAST) parser.getAST();
		     t = (BaseAST)t.getNextSibling().getNextSibling();  // Move to Knowledge
		     log.debug(t.toStringTree());   // prints knowledge
		     String knowledge = treeParser.knowledge(t, ardObj);
		     
		     /**************************************************************************************/
		   /*  log.debug(t.getNextSibling().getNextSibling().toStringTree()); // Print data
		     System.out.println(t.getNextSibling().getNextSibling().toStringTree()); // Print data
		     
		   	 treeParser.data(t.getNextSibling().getNextSibling(),ardObj);
		   	 log.debug(t.getNextSibling().getNextSibling().getNextSibling().toStringTree()); // Print logic
		   	 System.out.println(t.getNextSibling().getNextSibling().getNextSibling().toStringTree()); // Print logic
		     String logicstr = treeParser.logic(t.getNextSibling().getNextSibling().getNextSibling(), ardObj);
		    */
		     ardObj.PrintConceptMap();	   // To Debug
		     ardObj.PrintEvaluateList();   // To Debug
		     retVal = ardObj.WriteEvaluate(w, classname);
		     if(retVal) {
			 //    String actionstr = treeParser.action(t.getNextSibling().getNextSibling().getNextSibling().getNextSibling(), ardObj);
			     ardObj.WriteAction(ardObj.getActionStr(), w);
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
		    s.close();
		    return retVal;
	}
	
	
}