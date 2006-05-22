package org.openmrs.reporting.export;

import java.io.Serializable;

public class ConceptColumn implements ExportColumn, Serializable {
	
	public static final long serialVersionUID = 987654323L;
	
	private String columnType = "concept";
	private String columnName = "";
	private String modifier = "";
	private int modifierNum = 5;
	private String conceptName = "";
	private String[] extras = null;
	
	public ConceptColumn() { }
	
	public ConceptColumn(String columnName, String modifier, String conceptName, String[] extras) {
		this.columnName = columnName;
		this.modifier = modifier;
		this.conceptName = conceptName;
		this.extras = extras;
	}
	
	public String toTemplateString() {
		String s = "";
		
		if (DataExportReportObject.MODIFIER_ANY.equals(modifier)) {
			s += "#set($o = $fn.getLastObs('" + conceptName + "')) ";
			s += "$!{o.getValueAsString($locale)}";
			s += extrasToTemplateString();
		}
		else if (DataExportReportObject.MODIFIER_FIRST.equals(modifier)) {
			s += "#set($o = $fn.getLastObs('" + conceptName + "')) ";
			s += "$!{o.getValueAsString($locale)}";
			s += extrasToTemplateString();
		}
		else if (DataExportReportObject.MODIFIER_LAST.equals(modifier)) {
			s += "#set($o = $fn.getLastObs('" + conceptName + "')) ";
			s += "$!{o.getValueAsString($locale)}";
			s += extrasToTemplateString();
		}
		else if (DataExportReportObject.MODIFIER_LAST_NUM.equals(modifier)) {
			s += "#set($obs = $fn.getLastNObs(" + modifierNum + ", '" + conceptName + "'))";
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
			s += "#foreach($o in [1.." + (modifierNum - 1) +"]) ";
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

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getConceptName() {
		return conceptName;
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
	
}