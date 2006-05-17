package org.openmrs.reporting.export;

import java.io.Serializable;

public class ConceptColumn implements ExportColumn, Serializable {
	
	public static final long serialVersionUID = 987654323L;
	
	private String columnType = "concept";
	private String columnName = "";
	private String modifier = "";
	private int modifierNum = 5;
	private String conceptName = "";
	
	public ConceptColumn() { }
	
	public ConceptColumn(String columnName, String modifier, String conceptName) {
		this.columnName = columnName;
		this.modifier = modifier;
		this.conceptName = conceptName;
	}
	
	public String toTemplateString() {
		String s = "";
		
		if (DataExportReportObject.MODIFIER_ANY.equals(modifier)) {
			s += "$!{fn.getLastObs('" + conceptName + "').getValueAsString($locale)}";
			s += "$!{fn.getSeparator()}";
			s += "$!{fn.formatDate('short', $fn.getLastObs('" + conceptName + "').getObsDatetime())}";
		}
		else if (DataExportReportObject.MODIFIER_FIRST.equals(modifier)) {
			s += "$!{fn.getFirstObs('" + conceptName + "').getValueAsString($locale)}";
			s += "$!{fn.getSeparator()}";
			s += "$!{fn.formatDate('short', $fn.getFirstObs('" + conceptName + "').getObsDatetime())}";
		}
		else if (DataExportReportObject.MODIFIER_LAST.equals(modifier)) {
			s += "$!{fn.getLastObs('" + conceptName + "').getValueAsString($locale)}";
			s += "$!{fn.getSeparator()}";
			s += "$!{fn.formatDate('short', $fn.getLastObs('" + conceptName + "').getObsDatetime())}";
		}
		else if (DataExportReportObject.MODIFIER_LAST_NUM.equals(modifier)) {
			s += "#set($obs = $fn.getLastNObs(" + modifierNum + ", '" + conceptName + "')) ";
			s += "#foreach($o in $obs) ";
			s += "  #if($velocityCount > 1)";
			s += "    $!{fn.getSeparator()}";
			s += "  #end";
			s += "  $!{o.getValueAsString($locale)}";
			s += "  $!{fn.getSeparator()}";
			s += "  $!{fn.formatDate('short', $o.getObsDatetime())}";
			s += "#end\n";
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
		s += "$!{fn.getSeparator()}";
		s += columnName + " Datetime";
		
		if (DataExportReportObject.MODIFIER_LAST_NUM.equals(modifier)) {
			s += "#foreach($o in [1.." + (modifierNum - 1) +"]) ";
			s += "$!{fn.getSeparator()}";
			s += columnName + " ($velocityCount)";
			s += "$!{fn.getSeparator()}";
			s += columnName + " Datetime ($velocityCount)";
			s += "#end\n";
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
	
}