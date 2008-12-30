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
			
			String line = "Concept Id,Name,Description,Synonyms,Answers,Set Members,Class,Datatype,Changed By,Creator\n";
			response.getWriter().write(line);
			
			int listIndex = 0;
			Iterator<Concept> conceptIterator = cs.conceptIterator();
			while (conceptIterator.hasNext()) {
				Concept c = conceptIterator.next();
				if (c.isRetired() == false) {
					
					line = c.getConceptId() + ",";
					String name, description;
					ConceptName cn = c.getName(locale);
					if (cn == null)
						name = "";
					else
						name = cn.getName();
					
					ConceptDescription cd = c.getDescription(locale);
					if (cd == null)
						description = "";
					else
						description = cd.getDescription();
					
					line += '"' + name.replace("\"", "\"\"") + "\",";
					
					if (description == null)
						description = "";
					line = line + '"' + description.replace("\"", "\"\"") + "\",";
					
					String tmp = "";
					for (ConceptName syn : c.getNames()) {
						tmp += syn + "\n";
					}
					line += '"' + tmp.trim() + "\",";
					
					tmp = "";
					for (ConceptAnswer answer : c.getAnswers()) {
						if (answer.getAnswerConcept() != null)
							tmp += answer.getAnswerConcept().getName() + "\n";
						else if (answer.getAnswerDrug() != null)
							tmp += answer.getAnswerDrug().getFullName(Context.getLocale()) + "\n";
					}
					line += '"' + tmp.trim() + "\",";
					
					tmp = "";
					for (ConceptSet set : c.getConceptSets()) {
						if (set.getConcept() != null) {
							name = set.getConcept().getName().toString();
							tmp += name.replace("\"", "\"\"") + "\n";
						}
					}
					line += '"' + tmp.trim() + "\",";
					
					line += '"';
					if (c.getConceptClass() != null)
						line += c.getConceptClass().getName();
					line += "\",";
					
					line += '"';
					if (c.getDatatype() != null)
						line += c.getDatatype().getName();
					line += "\",";
					
					line += '"';
					if (c.getChangedBy() != null)
						line += c.getChangedBy().getPersonName();
					line += "\",";
					
					line += '"';
					if (c.getCreator() != null)
						line += c.getCreator().getPersonName();
					line += "\"\n";
					
					response.getWriter().write(line);
				}
				
			}
		}
		catch (Throwable t) {
			log.error("Error while downloading concepts.", t);
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
