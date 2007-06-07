package org.openmrs.web.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSynonym;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;

public class DownloadDictionaryServlet extends HttpServlet {

	public static final long serialVersionUID = 1231231L;

	/**
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		Locale locale = Context.getLocale();
		
		ConceptService cs = Context.getConceptService();
		String s = new SimpleDateFormat("dMy_Hm").format(new Date());

		response.setHeader("Content-Type", "text/csv");
		response.setHeader("Content-Disposition", "attachment; filename=conceptDictionary" + s + ".csv");
		
		String line = "Concept Id,Name,Description,Synonyms,Answers,Class,Datatype,Creator,Changed By";
		response.getOutputStream().println(line);
		
		for (Concept c : cs.getConcepts("conceptId", "asc")){
			
			if (c.isRetired() == false) {
			
				line = c.getConceptId()+ ",";
				String name, description;
				ConceptName cn = c.getName(locale);
				if (cn == null)	
					name = description = "";
				else {
					name = cn.getName();
					description = cn.getDescription();
				}
				line += '"' + name.replace("\"", "\"\"") + "\",";
				
				if (description == null) description = "";
				line = line + '"' + description.replace("\"", "\"\"") + "\",";
				
				String tmp = "";
				for (ConceptSynonym syn : c.getSynonyms()) {
					tmp += syn + "\n";
				}
				line += '"' + tmp.trim() + "\",";
				
				tmp = "";
				for (ConceptAnswer answer : c.getAnswers()) {
					if (answer.getAnswerConcept() != null)
						tmp += answer.getAnswerConcept().toString() + "\n";
					else if (answer.getAnswerDrug() != null)
						tmp += answer.getAnswerDrug().toString() + "\n";
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
				line += "\"";
			
				response.getOutputStream().println(line);
				
			}
		}
	}
	
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}