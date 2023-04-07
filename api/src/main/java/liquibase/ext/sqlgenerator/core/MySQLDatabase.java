package liquibase.ext.sqlgenerator.core;

public class MySQLDatabase implements DatabaseModifier {
	
	public String getModifyString() {
		return "MODIFY";
	}
}
