package org.openmrs.web.dwr;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptWord;
import org.openmrs.web.Util;

import uk.ltd.getahead.dwr.ExecutionContext;
import uk.ltd.getahead.dwr.util.JavascriptUtil;

public class ConceptListItem {
	
	protected final Log log = LogFactory.getLog(getClass());

	private Integer conceptId;
	private String name;
	private String shortName;
	private String description;
	private String synonym; 
	private Boolean retired;


	public ConceptListItem() { }
	
	public ConceptListItem(ConceptWord word) {

		if (word != null) {

			Concept concept = word.getConcept();
			conceptId = concept.getConceptId();
			JavascriptUtil util = new JavascriptUtil();
			ConceptName cn = concept.getName(new Locale(word.getLocale()));
			name = "";
			shortName = "";
			if (cn != null) {
				name = util.escapeJavaScript(cn.getName());
				shortName = util.escapeJavaScript(cn.getShortName());
				description = util.escapeJavaScript(cn.getDescription());
			}
			synonym = word.getSynonym();
			retired = concept.isRetired();
		}
	}
	
	public ConceptListItem(Concept concept) {

		if (concept != null) {

			conceptId = concept.getConceptId();
			JavascriptUtil util = new JavascriptUtil();
			Locale locale = Util.getLocale(ExecutionContext.get().getHttpServletRequest());
			ConceptName cn = concept.getName(locale);
			name = "";
			shortName = "";
			if (cn != null) {
				name = util.escapeJavaScript(cn.getName());
				shortName = util.escapeJavaScript(cn.getShortName());
				description = util.escapeJavaScript(cn.getDescription());
			}
			synonym = "";
			retired = concept.isRetired();
		}
	}

	public Integer getConceptId() {
		return conceptId;
	}

	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**
	 * @return Returns the synonym.
	 */
	public String getSynonym() {
		return synonym;
	}

	/**
	 * @param synonym The synonym to set.
	 */
	public void setSynonym(String synonym) {
		this.synonym = synonym;
	}

	public Boolean getRetired() {
		return retired;
	}

	public void setRetired(Boolean retired) {
		this.retired = retired;
	}
	

}
