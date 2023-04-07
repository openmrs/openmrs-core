package liquibase.ext.sqlgenerator.core;

public class SybaseASADatabaseModifier implements DatabaseModifier {
	
	public String getModifyString() {
		return "MODIFY";
	}
}
