/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSet;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;

/**
 * This servlet will package all non retired concepts into a comma delimited file. Retired concepts
 * are ignored.
 */
public class DownloadDictionaryServlet extends HttpServlet {
	
	public static final long serialVersionUID = 1231231L;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * Maximum size of query results, when retrieved in batches. ABKTODO: should probably be
	 * configurable somewhere
	 */
	public int batchSize = 1000;
	
	/**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			Locale locale = Context.getLocale();
			
			ConceptService cs = Context.getConceptService();
			String s = new SimpleDateFormat("dMy_Hm").format(new Date());
			
			response.setHeader("Content-Type", "text/csv;charset=UTF-8");
			response.setHeader("Content-Disposition", "attachment; filename=conceptDictionary" + s + ".csv");
			
			String headerLine = "Concept Id,Name,Description,Synonyms,Answers,Set Members,Class,Datatype,Changed By,Creator\n";
			response.getWriter().write(headerLine);
			
			Iterator<Concept> conceptIterator = cs.conceptIterator();
			while (conceptIterator.hasNext()) {
				Concept c = conceptIterator.next();
				if (!c.isRetired()) {
					
					StringBuilder line = new StringBuilder("");
					line.append(c.getConceptId());
					line.append(",");
					String name, description;
					ConceptName cn = c.getName(locale);
					if (cn == null) {
						name = "";
					} else {
						name = cn.getName();
					}
					
					ConceptDescription cd = c.getDescription(locale);
					if (cd == null) {
						description = "";
					} else {
						description = cd.getDescription();
					}
					
					line.append('"');
					line.append(name.replace("\"", "\"\""));
					line.append("\",");
					
					if (description == null) {
						description = "";
					}
					line.append('"');
					line.append(description.replace("\"", "\"\""));
					line.append("\",");
					
					StringBuilder tmp = new StringBuilder("");
					for (ConceptName syn : c.getNames()) {
						tmp.append(syn).append("\n");
					}
					line.append('"');
					line.append(tmp.toString().trim());
					line.append("\",");
					
					tmp = new StringBuilder("");
					for (ConceptAnswer answer : c.getAnswers(false)) {
						if (answer.getAnswerConcept() != null) {
							tmp.append(answer.getAnswerConcept().getName()).append("\n");
						} else if (answer.getAnswerDrug() != null) {
							tmp.append(answer.getAnswerDrug().getFullName(Context.getLocale())).append("\n");
						}
					}
					line.append('"');
					line.append(tmp.toString().trim());
					line.append("\",");
					
					tmp = new StringBuilder("");
					for (ConceptSet set : c.getConceptSets()) {
						if (set.getConcept() != null) {
							name = set.getConcept().getName().toString();
							tmp.append(name.replace("\"", "\"\"")).append("\n");
						}
					}
					line.append('"');
					line.append(tmp.toString().trim());
					line.append("\",");
					
					line.append('"');
					if (c.getConceptClass() != null) {
						line.append(c.getConceptClass().getName());
					}
					line.append("\",");
					
					line.append('"');
					if (c.getDatatype() != null) {
						line.append(c.getDatatype().getName());
					}
					line.append("\",");
					
					line.append('"');
					if (c.getChangedBy() != null) {
						line.append(c.getChangedBy().getPersonName());
					}
					line.append("\",");
					
					line.append('"');
					if (c.getCreator() != null) {
						line.append(c.getCreator().getPersonName());
					}
					line.append("\"\n");
					
					response.getWriter().write(line.toString());
					
				} //end if !c.isRetired()
				
			} //end while(conceptIterator.hasNext())
			
		}
		catch (Exception e) {
			log.error("Error while downloading concepts.", e);
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
