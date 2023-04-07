package liquibase.ext.sqlgenerator.core;

public class DefaultDatabaseModifier implements DatabaseModifier {
	
	public String getModifyString() {
		return "ALTER COLUMN";
	}
}
