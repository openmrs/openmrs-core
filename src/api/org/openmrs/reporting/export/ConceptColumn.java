package org.openmrs.reporting.export;

import java.io.Serializable;

public class ConceptColumn implements ExportColumn, Serializable {
	
	public static final long serialVersionUID = 987654323L;
	
	private String columnType = "concept";
	private String columnName = "";
	private String modifier = "";
	private Integer modifierNum = null;
	private Integer conceptId = null;
	private String conceptName = "";
	private String[] extras = null;
	
	public ConceptColumn() { }
	
	public ConceptColumn(String columnName, String modifier, Integer modifierNum, String conceptId, String[] extras) {
		this.columnName = columnName;
		this.modifier = modifier;
		this.modifierNum = modifierNum;
		try {
			this.conceptId = Integer.valueOf(conceptId);
		}
		catch (NumberFormatException e) {
			this.conceptName = conceptId; // for backwards compatibility to pre 1.0.43
		}
		this.extras = extras;
	}
	
	public String toTemplateString() {
		String s = "";
		
		if (DataExportReportObject.MODIFIER_ANY.equals(modifier)) {
			s += "#set($o = $fn.getLastObs('" + getConceptIdOrName() + "')) ";
			s += "$!{o.getValueAsString($locale)}";
			s += extrasToTemplateString();
		}
		else if (DataExportReportObject.MODIFIER_FIRST.equals(modifier)) {
			s += "#set($o = $fn.getLastObs('" + getConceptIdOrName() + "')) ";
			s += "$!{o.getValueAsString($locale)}";
			s += extrasToTemplateString();
		}
		else if (DataExportReportObject.MODIFIER_LAST.equals(modifier)) {
			s += "#set($o = $fn.getLastObs('" + getConceptIdOrName() + "')) ";
			s += "$!{o.getValueAsString($locale)}";
			s += extrasToTemplateString();
		}
		else if (DataExportReportObject.MODIFIER_LAST_NUM.equals(modifier)) {
			Integer num = modifierNum == null ? 1 : modifierNum;
			s += "#set($obs = $fn.getLastNObs(" + num + ", '" + getConceptIdOrName() + "'))";
			s += "#foreach($o in $obs)";
			s += "#if($velocityCount > 1)";
			s += "$!{fn.getSeparator()}";
			s += "#end";
			s += "$!{o.getValueAsString($locale)}";
			s += extrasToTemplateString();
			s += "#end\n";
		}
		
		return s;
	}
	
	private String extrasToTemplateString() {
		String s = "";
		if (extras != null)  {
			for (String ext : extras) {
				s += "$!{fn.getSeparator()}";
				if ("obsDatetime".equals(ext))
					s += "$!{fn.formatDate('short', $o.getObsDatetime())}";
				else if("location".equals(ext))
					s += "$!{o.getLocation().getName()}";
				else if ("comment".equals(ext))
					s += "$!{o.getComment()}";
				else if ("provider".equals(ext))
					s += "$!{o.getEncounter().getProvider().getFirstName()} $!{o.getEncounter().getProvider().getLastName()}";
				else if ("encounterType".equals(ext))
					s += "$!{o.getEncounter().getEncounterType().getName()}";
			}
		}
		
		return s;
	}

	public String getColumnType() {
		return columnType;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}

	public String getColumnName() {
		return columnName;
	}
	
	public String getTemplateColumnName() {
		String s = columnName;
		s += getExtrasTemplateColumnNames(false);
		
		if (DataExportReportObject.MODIFIER_LAST_NUM.equals(modifier)) {
			Integer num = modifierNum == null ? 1 : modifierNum;
			s += "#foreach($o in [1.." + (num - 1) +"]) ";
			s += "$!{fn.getSeparator()}";
			s += columnName + "_($velocityCount)";
			s += getExtrasTemplateColumnNames(true);
			s += "#end\n";
		}
		
		return s;
	}
	
	private String getExtrasTemplateColumnNames(boolean appendCount) {
		String s = "";
		if (extras != null) {
			for (String ext : extras) {
				s += "$!{fn.getSeparator()}";
				s += columnName + "_" + ext;
				if (appendCount)
					s += "_($velocityCount)";
			}
		}
		return s;
	}

	//// left for backwards compatibility to pre 1.0.43
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getConceptName() {
		return conceptName;
	}
	///////

	public Integer getConceptId() {
		return conceptId;
	}

	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
	}

	public void setConceptName(String conceptName) {
		this.conceptName = conceptName;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public String[] getExtras() {
		return extras;
	}

	public void setExtras(String[] extras) {
		this.extras = extras;
	}

	public Integer getModifierNum() {
		return modifierNum;
	}

	public void setModifierNum(Integer modifierNum) {
		this.modifierNum = modifierNum;
	}
	
	// returns conceptId if not null, conceptName otherwise
	// convenience method for backwards compatibility to pre 1.0.43
	public String getConceptIdOrName() {
		if (conceptId != null)
			return conceptId.toString();
		else
			return conceptName;
	}
	
}