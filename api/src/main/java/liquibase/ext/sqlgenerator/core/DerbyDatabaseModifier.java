package liquibase.ext.sqlgenerator.core;

public class DerbyDatabaseModifier implements DatabaseModifier {
	
	public String getModifyString() {
		return "ALTER COLUMN";
	}
}
