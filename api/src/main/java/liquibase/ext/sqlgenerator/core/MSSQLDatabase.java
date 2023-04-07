package liquibase.ext.sqlgenerator.core;

public class MSSQLDatabase implements DatabaseModifier {
	
	public String getModifyString() {
		return "ALTER COLUMN";
	}
}
