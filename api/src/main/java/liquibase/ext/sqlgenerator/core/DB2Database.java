package liquibase.ext.sqlgenerator.core;

public class DB2Database implements DatabaseModifier {
	
	public String getModifyString() {
		return "ALTER COLUMN";
	}
}
