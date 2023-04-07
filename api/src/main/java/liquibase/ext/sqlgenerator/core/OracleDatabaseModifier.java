package liquibase.ext.sqlgenerator.core;

public class OracleDatabaseModifier implements DatabaseModifier {
	
	public String getModifyString() {
		return "MODIFY (";
	}
}
