package liquibase.ext.sqlgenerator.core;

public class HsqlDatabaseModifier implements DatabaseModifier {
	
	public String getModifyString() {
		return "ALTER COLUMN";
	}
}
