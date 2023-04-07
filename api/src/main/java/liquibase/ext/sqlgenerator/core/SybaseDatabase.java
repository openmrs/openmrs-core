package liquibase.ext.sqlgenerator.core;

public class SybaseDatabase implements DatabaseModifier {
	
	public String getModifyString() {
		return "MODIFY";
	}
}
