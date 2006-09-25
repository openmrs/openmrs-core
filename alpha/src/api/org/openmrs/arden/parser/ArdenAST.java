package org.openmrs.arden.parser;
import antlr.CommonAST;
import antlr.Token;
import antlr.collections.AST;

public abstract class ArdenAST extends CommonAST {
	public abstract String value();
//	 satisfy abstract methods from BaseAST
	public void initialize(int t, String txt) {
	}
	public void initialize(AST t) {
	}
	public void initialize(Token tok) {
	}
}

