/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.arden.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.arden.ArdenBaseLexer;
import org.openmrs.arden.ArdenBaseParser;
import org.openmrs.arden.ArdenBaseTreeParser;
import org.openmrs.arden.ArdenService;
import org.openmrs.arden.MLMObject;
import org.springframework.transaction.annotation.Transactional;

import antlr.BaseAST;

/**
 * Arden-related services
 *
 * @author Vibha Anand
 * @version 1.0
 */
@Transactional
public class ArdenServiceImpl implements ArdenService {
	
	private final Log log = LogFactory.getLog(getClass());
	
	public ArdenServiceImpl() {
	}
	
	/**
	 * @param file - mlm file to be parsed
	 */
	public void compileFile(String file, String outFolder) {
		try {
			// if we have at least one command-line argument
			if (file.length() > 0) {
				log.debug("Parsing file" + file);
				
				// for each directory/file specified on the command line
				doFile(new File(file), outFolder); // parse it
				
			}
		}
		catch (Exception e) {
			log.error(e.getStackTrace());
		}
	}
	
	/**
	 * @param f - parse a file or a directory
	 */
	private void doFile(File f, String outFolder) throws Exception {
		boolean retVal = true;
		try {
			// If this is a directory, walk each file/dir in that directory
			if (f.isDirectory()) {
				String files[] = f.list();
				for (int i = 0; i < files.length; i++) {
					doFile(new File(f, files[i]), outFolder);
				}
			}

			// otherwise, if this is a mlm file, parse it!
			else if (f.getName().substring(f.getName().length() - 4).equals(".mlm")) {
				log.info("Parsing file name:" + f.getName());
				retVal = parseFile(new FileInputStream(f), f.getName(), outFolder);
				if (!retVal) {
					System.out.println("Please correct the compiler error!");
				}
			}
		}
		catch (Exception e) {
			log.error(e.getMessage());
		}
		
	}
	
	/**
	 * @param s
	 * @param fn
	 */
	private boolean parseFile(InputStream s, String fn, String outFolder) throws Exception {
		boolean retVal = true;
		try {
			Date Today = new Date();
			String cfn;
			
			String packagePrefix = Context.getAdministrationService().getGlobalProperty("logic.default.packageName");
			if (StringUtils.isEmpty(packagePrefix)) {
				packagePrefix = "org.openmrs.logic.rule";
			}
			
			MLMObject ardObj = new MLMObject(Context.getLocale(), null);
			
			// Create a scanner that reads from the input stream passed to us
			ArdenBaseLexer lexer = new ArdenBaseLexer(s);
			
			// Create a parser that reads from the scanner
			ArdenBaseParser parser = new ArdenBaseParser(lexer);
			
			// start parsing at the compilation unit rule
			parser.startRule();
			BaseAST t = (BaseAST) parser.getAST();
			
			if (log.isDebugEnabled()) {
				log.debug(t.toStringTree()); // prints maintenance
			}
			
			ArdenBaseTreeParser treeParser = new ArdenBaseTreeParser();
			
			String maintenance = treeParser.maintenance(t, ardObj);
			
			cfn = ardObj.getClassName();
			
			String packageFolderName = packagePrefix.replace('.', File.separatorChar);
			File packageFolder = new File(outFolder, packageFolderName);
			if (!packageFolder.exists()) {
				packageFolder.mkdirs();
			}
			
			// make sure that the file is stored in the correct folder based on the package
			OutputStream os = new FileOutputStream(new File(packageFolder, cfn + ".java"));
			
			Writer w = new OutputStreamWriter(os);
			log.info("Writing to file - " + cfn + ".java");
			
			w.write("/********************************************************************" + "\n Translated from - " + fn
			        + " on " + Today.toString() + "\n\n");
			w.write(maintenance);
			
			t = (BaseAST) t.getNextSibling(); // Move to library
			
			if (log.isDebugEnabled()) {
				log.debug(t.toStringTree()); // prints library
			}
			String library = treeParser.library(t, ardObj);
			w.write(library);
			w.write("\n********************************************************************/\n");
			w.write("package " + packagePrefix + ";\n\n");
			w.write("import java.util.ArrayList;\n");
			w.write("import java.util.HashMap;\n");
			w.write("import java.util.List;\n");
			w.write("import java.util.Map;\n");
			w.write("import java.util.Set;\n");
			w.write("import java.util.Collection;\n");
			w.write("import java.util.Collections;\n");
			
			w.write("import org.apache.commons.logging.Log;\n");
			w.write("import org.apache.commons.logging.LogFactory;\n");
			w.write("import org.openmrs.Patient;\n");
			w.write("import org.openmrs.api.PatientService;\n");
			w.write("import org.openmrs.api.context.Context;\n");
			w.write("import org.openmrs.arden.MlmRule;\n");
			w.write("import org.openmrs.logic.LogicContext;\n");
			w.write("import org.openmrs.logic.impl.LogicCriteriaImpl;\n");
			w.write("import org.openmrs.logic.LogicException;\n");
			w.write("import org.openmrs.logic.Rule;\n");
			w.write("import org.openmrs.logic.result.Result;\n");
			w.write("import org.openmrs.logic.result.Result.Datatype;\n");
			w.write("import org.openmrs.logic.rule.RuleParameterInfo;\n");
			w.write("import org.openmrs.logic.rule.provider.RuleProvider;\n");
			w.write("import org.openmrs.logic.Duration;\n");
			w.write("import java.util.StringTokenizer;\n\n");
			w.write("import org.openmrs.api.ConceptService;\n");
			w.write("import java.text.SimpleDateFormat;\n");
			w.write("import org.openmrs.Concept;\n");
			w.write("import org.openmrs.ConceptName;\n");
			
			String classname = ardObj.getClassName();
			w.write("public class " + classname + " implements MlmRule{\n\n"); // Start of class
			
			w.write("\tprivate Log log = LogFactory.getLog(this.getClass());\n");
			
			w.flush();
			
			/**************************************************************************************
			 * Implement the other interface methods
			 */
			
			String str = "";
			w.write("\t/*** @see org.openmrs.logic.rule.Rule#getDuration()*/\n\t"
			        + "public int getDuration() {\n\t\treturn 60*30;   // 30 minutes\n\t}\n\n");
			
			w.write("\t/*** @see org.openmrs.logic.rule.Rule#getDatatype(String)*/\n\t"
			        + "public Datatype getDatatype(String token) {\n\t" + "\treturn Datatype.TEXT;\n\t}\n\n");
			
			w.write("\t/*** @see org.openmrs.logic.rule.Rule#getParameterList()*/\n\t"
			        + "public Set<RuleParameterInfo> getParameterList() {\n\t\treturn null;\n\t}\n\n");
			
			w.write("\t/*** @see org.openmrs.logic.rule.Rule#getDependencies()*/\n\t"
			        + "public String[] getDependencies() {\n\t\treturn new String[] { };\n\t}\n\n");
			
			w.write("\t/*** @see org.openmrs.logic.rule.Rule#getTTL()*/\n\t"
			        + "public int getTTL() {\n\t\treturn 0; //60 * 30; // 30 minutes\n\t}\n\n");
			
			w.write("\t/*** @see org.openmrs.logic.rule.Rule#getDatatype(String)*/\n\t"
			        + "public Datatype getDefaultDatatype() {\n\t\treturn Datatype.CODED;\n\t}\n\n");
			
			str = ardObj.getAuthor();
			if (str != null && str.length() == 0) {
				str = null;
			}
			if (str != null) {
				str = "\"" + str + "\"";
			}
			w.write("\t/*** @see org.openmrs.arden.MlmRule#getAuthor()*/\n" + "\tpublic String getAuthor(){\n"
			        + "\t\treturn " + str + ";\n" + "\t}\n\n");
			
			str = ardObj.getCitations();
			if (str != null && str.length() == 0) {
				str = null;
			}
			if (str != null) {
				str = "\"" + str + "\"";
			}
			w.write("\t/*** @see org.openmrs.arden.MlmRule#getCitations()*/\n" + "\tpublic String getCitations(){\n"
			        + "\t\treturn " + str + ";\n" + "\t}\n\n");
			
			str = ardObj.getDate();
			if (str != null && str.length() == 0) {
				str = null;
			}
			if (str != null) {
				str = "\"" + str + "\"";
			}
			w.write("\t/*** @see org.openmrs.arden.MlmRule#getDate()*/\n" + "\tpublic String getDate(){\n" + "\t\treturn "
			        + str + ";\n" + "\t}\n\n");
			str = ardObj.getExplanation();
			if (str != null && str.length() == 0) {
				str = null;
			}
			if (str != null) {
				str = "\"" + str + "\"";
			}
			w.write("\t/*** @see org.openmrs.arden.MlmRule#getExplanation()*/\n" + "\tpublic String getExplanation(){\n"
			        + "\t\treturn " + str + ";\n" + "\t}\n\n");
			
			str = ardObj.getInstitution();
			if (str != null && str.length() == 0) {
				str = null;
			}
			if (str != null) {
				str = "\"" + str + "\"";
			}
			w.write("\t/*** @see org.openmrs.arden.MlmRule#getInstitution()*/\n" + "\tpublic String getInstitution(){\n"
			        + "\t\treturn " + str + ";\n" + "\t}\n\n");
			str = ardObj.getKeywords();
			if (str != null && str.length() == 0) {
				str = null;
			}
			if (str != null) {
				str = "\"" + str + "\"";
			}
			w.write("\t/*** @see org.openmrs.arden.MlmRule#getKeywords()*/\n" + "\tpublic String getKeywords(){\n"
			        + "\t\treturn " + str + ";\n" + "\t}\n\n");
			str = ardObj.getLinks();
			if (str != null && str.length() == 0) {
				str = null;
			}
			if (str != null) {
				str = "\"" + str + "\"";
			}
			w.write("\t/*** @see org.openmrs.arden.MlmRule#getLinks()*/\n" + "\tpublic String getLinks(){\n" + "\t\treturn "
			        + str + ";\n" + "\t}\n\n");
			
			str = ardObj.getPurpose();
			if (str != null && str.length() == 0) {
				str = null;
			}
			if (str != null) {
				str = "\"" + str + "\"";
			}
			w.write("\t/*** @see org.openmrs.arden.MlmRule#getPurpose()*/\n" + "\tpublic String getPurpose(){\n"
			        + "\t\treturn " + str + ";\n" + "\t}\n\n");
			str = ardObj.getSpecialist();
			if (str != null && str.length() == 0) {
				str = null;
			}
			if (str != null) {
				str = "\"" + str + "\"";
			}
			w.write("\t/*** @see org.openmrs.arden.MlmRule#getSpecialist()*/\n" + "\tpublic String getSpecialist(){\n"
			        + "\t\treturn " + str + ";\n" + "\t}\n\n");
			
			str = ardObj.getTitle();
			if (str != null && str.length() == 0) {
				str = null;
			}
			if (str != null) {
				str = "\"" + str + "\"";
			}
			w.write("\t/*** @see org.openmrs.arden.MlmRule#getTitle()*/\n" + "\tpublic String getTitle(){\n" + "\t\treturn "
			        + str + ";\n" + "\t}\n\n");
			double d = ardObj.getVersion();
			w.write("\t/*** @see org.openmrs.arden.MlmRule#getVersion()*/\n" + "\tpublic Double getVersion(){\n"
			        + "\t\treturn " + d + ";\n" + "\t}\n\n");
			str = ardObj.getType();
			if (str != null && str.length() == 0) {
				str = null;
			}
			if (str != null) {
				str = "\"" + str + "\"";
			}
			w.write("\t/*** @see org.openmrs.arden.MlmRule#getType()*/\n" + "\tpublic String getType(){\n" + "\t\treturn "
			        + str + ";\n" + "\t}\n\n");
			
			/**************************************************************************************/
			
			t = (BaseAST) t.getNextSibling(); // Move to Knowledge
			log.debug(t.toStringTree()); // prints knowledge
			
			/**************************************************Write Knowledge dependent section**********************************************/
			Integer p = ardObj.getPriority();
			w.write("\t/*** @see org.openmrs.arden.MlmRule#getPriority()*/\n" + "\tpublic Integer getPriority(){\n"
			        + "\t\treturn " + p + ";\n" + "\t}\n\n");
			
			str = ardObj.getData();
			if (str != null && str.length() == 0) {
				str = null;
			}
			if (str != null) {
				str = "\"" + str + "\"";
			}
			w.write("\t/*** @see org.openmrs.arden.MlmRule#getData()*/\n" + "\tpublic String getData(){\n" + "\t\treturn "
			        + str + ";\n" + "\t}\n\n");
			
			str = ardObj.getLogic();
			if (str != null && str.length() == 0) {
				str = null;
			}
			if (str != null) {
				str = "\"" + str + "\"";
			}
			w.write("\t/*** @see org.openmrs.arden.MlmRule#getLogic()*/\n" + "\tpublic String getLogic(){\n" + "\t\treturn "
			        + str + ";\n" + "\t}\n\n");
			
			str = ardObj.getAction();
			if (str != null && str.length() == 0) {
				str = null;
			}
			if (str != null) {
				str = "\"" + str + "\"";
			}
			w.write("\t/*** @see org.openmrs.arden.MlmRule#getAction()*/\n" + "\tpublic String getAction(){\n"
			        + "\t\treturn " + str + ";\n" + "\t}\n\n");
			
			Integer ageMin = ardObj.getAgeMin();
			w.write("\t/*** @see org.openmrs.arden.MlmRule#getAgeMin()*/\n" + "\tpublic Integer getAgeMin(){\n"
			        + "\t\treturn " + ageMin + ";\n" + "\t}\n\n");
			
			str = ardObj.getAgeMinUnits();
			if (str != null && str.length() == 0) {
				str = null;
			}
			if (str != null) {
				str = "\"" + str + "\"";
			}
			w.write("\t/*** @see org.openmrs.arden.MlmRule#getAgeMinUnits()*/\n" + "\tpublic String getAgeMinUnits(){\n"
			        + "\t\treturn " + str + ";\n" + "\t}\n\n");
			
			Integer ageMax = ardObj.getAgeMax();
			w.write("\t/*** @see org.openmrs.arden.MlmRule#getAgeMax()*/\n" + "\tpublic Integer getAgeMax(){\n"
			        + "\t\treturn " + ageMax + ";\n" + "\t}\n\n");
			
			str = ardObj.getAgeMaxUnits();
			if (str != null && str.length() == 0) {
				str = null;
			}
			if (str != null) {
				str = "\"" + str + "\"";
			}
			w.write("\t/*** @see org.openmrs.arden.MlmRule#getAgeMaxUnits()*/\n" + "\tpublic String getAgeMaxUnits(){\n"
			        + "\t\treturn " + str + ";\n" + "\t}\n\n");
			
			w.write("\tprivate static boolean containsIgnoreCase(Result key,List<Result> lst){\n");
			w.write("\t\tif(key == null){\n");
			w.write("\t\t\treturn false;\n");
			w.write("\t\t}\n");
			w.write("\t\tString keyString = \"\";\n");
			w.write("\t\tif(key.getDatatype() == Result.Datatype.CODED) {\n");
			w.write("\t\t\tConcept keyConcept = key.toConcept();\n");
			w.write("\t\t\tif(keyConcept != null) {\n");
			w.write("\t\t\t\tkeyString = ((ConceptName) keyConcept.getNames().toArray()[0]).getName();\n");
			w.write("\t\t\t}\n");
			w.write("\t\t} else {\n");
			w.write("\t\t\tkeyString = key.toString();\n");
			w.write("\t\t}\n");
			w.write("\t\tfor(Result element:lst){\n");
			w.write("\t\t\tConcept concept = element.toConcept();\n");
			w.write("\t\t\tif(concept == null){\n");
			w.write("\t\t\t\tcontinue;\n");
			w.write("\t\t\t}\n");
			w.write("\t\t\tCollection<ConceptName> cns = concept.getNames();\n");
			w.write("\t\t\tfor(ConceptName cn:cns) {\n");
			w.write("\t\t\t\tString elementString = cn.getName();\n");
			w.write("\t\t\t\tif(keyString.equalsIgnoreCase(elementString)){\n");
			w.write("\t\t\t\t\treturn true;\n");
			w.write("\t\t\t\t}\n");
			w.write("\t\t}\n");
			w.write("\t\t}\n");
			w.write("\t\treturn false;\n");
			w.write("\t}\n");
			
			w.write("\tprivate static String toProperCase(String str){\n\n");
			
			w.write("\t\tif(str == null || str.length()<1){\n");
			w.write("\t\t\treturn str;\n");
			w.write("\t\t}\n\n");
			
			w.write("\t\tStringBuffer resultString = new StringBuffer();\n");
			w.write("\t\tString delimiter = \" \";\n");
			
			w.write("\t\tStringTokenizer tokenizer = new StringTokenizer(str,delimiter,true);\n");
			
			w.write("\t\tString currToken = null;\n\n");
			
			w.write("\t\twhile(tokenizer.hasMoreTokens()){\n");
			w.write("\t\t\tcurrToken = tokenizer.nextToken();\n");
			
			w.write("\t\t\tif(!currToken.equals(delimiter)){\n");
			w.write("\t\t\t\tif(currToken.length()>0){\n");
			w.write("\t\t\t\t\tcurrToken = currToken.substring(0, 1).toUpperCase()\n");
			w.write("\t\t\t\t\t+ currToken.substring(1).toLowerCase();\n");
			w.write("\t\t\t\t}\n");
			w.write("\t\t\t}\n");
			
			w.write("\t\t\tresultString.append(currToken);\n");
			w.write("\t\t}\n");
			
			w.write("\t\treturn resultString.toString();\n");
			w.write("\t}\n");
			
			/**
			 * *************************************************************************************
			 * *********************************************
			 */
			
			// Move back to knowledge tree to actually start converting data
			// logic action
			t = (BaseAST) parser.getAST();
			t = (BaseAST) t.getNextSibling().getNextSibling(); // Move to
			// Knowledge
			log.debug(t.toStringTree()); // prints knowledge
			
			/** *********************************************************************************** */
			
			ardObj.PrintEvaluateList("data"); // To Debug
			retVal = ardObj.WriteEvaluate(w, classname);
			if (retVal) {
				ardObj.WriteAction(w);
				w.append("}"); // end class
				w.flush();
				w.close();
			} else { //delete the compiled file so far
				w.flush();
				w.close();
				boolean success = (new File("src/api/org/openmrs/logic/rule/" + cfn + ".java")).delete();
				if (!success) {
					System.out.println("Incomplete compiled file " + cfn + ".java cannot be deleted!");
				}
				
			}
			
		}
		catch (Exception e) {
			log.error(e);
		}
		finally {
			s.close();
		}
		return retVal;
	}
	
	/**
	 * @see org.openmrs.arden.ArdenService#compile(java.lang.String, java.lang.String)
	 */
	public void compile(String ardenRuleDefinition, String outputDir) throws APIException {
		try {
			InputStream inputStream = new ByteArrayInputStream(ardenRuleDefinition.getBytes());
			parseFile(inputStream, "ConceptDerived", outputDir);
		}
		catch (Exception e) {
			throw new APIException("arden.unable.compile", (Object[]) null);
		}
	}
}
