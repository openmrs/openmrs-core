package org.openmrs.web.dwr;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import uk.ltd.getahead.dwr.util.JavascriptUtil;

public class ConceptListItem {
	
	protected final Log log = LogFactory.getLog(getClass());

	private Integer conceptId;
	private String name;
	private String shortName;
	private String description;
	private Boolean retired;


	public ConceptListItem() { }
	
	public ConceptListItem(Concept concept) {

		if (concept != null) {

			conceptId = concept.getConceptId();
			JavascriptUtil util = new JavascriptUtil();
			ConceptName cn = concept.getName(new Locale("en"));
			name = "";
			shortName = "";
			if (cn != null) {
				name = util.escapeJavaScript(cn.getName());
				shortName = util.escapeJavaScript(cn.getShortName());
			}
			description = util.escapeJavaScript(concept.getDescription());
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

	public Boolean getRetired() {
		return retired;
	}

	public void setRetired(Boolean retired) {
		this.retired = retired;
	}
	

}
