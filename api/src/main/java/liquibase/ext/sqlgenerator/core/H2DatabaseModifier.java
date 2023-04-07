package liquibase.ext.sqlgenerator.core;

public class H2DatabaseModifier implements DatabaseModifier {
	
	public String getModifyString() {
		return "ALTER COLUMN";
	}
}
