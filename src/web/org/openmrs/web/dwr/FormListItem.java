package org.openmrs.web.dwr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;

public class FormListItem {
	
	protected final Log log = LogFactory.getLog(getClass());

	private Integer formId;
	private String name = "";
	private String description = "";
	private String version = "";
	private Integer build = 0;
	private boolean published = false;
	private String encounterType = "";
	
	public FormListItem() { }
		
	public FormListItem(Form form) {

		if (form != null) {
			formId = form.getFormId();
			name = form.getName();
			description = form.getDescription();
			if (form.getEncounterType() != null)
				encounterType = form.getEncounterType().getName();
			
			version = form.getVersion();
			build = form.getBuild();
			published = form.getPublished();
		}
	}

	public Integer getBuild() {
		return build;
	}

	public void setBuild(Integer build) {
		this.build = build;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEncounterType() {
		return encounterType;
	}

	public void setEncounterType(String encounterType) {
		this.encounterType = encounterType;
	}

	public Integer getFormId() {
		return formId;
	}

	public void setFormId(Integer formId) {
		this.formId = formId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	
	
}
