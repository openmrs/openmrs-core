package org.openmrs.arden.parser;

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;
import java.io.*;

public class ArdenReadNode extends StatementNode{
	
//	public ArdenReadNode(Token tok){
//		tok=tok;
//	}
	/** Compute value of subtree;
     * this is heterogeneous part :)
     */
    public String value() {
        return "Assign " + left().value() + "By calculating " + right().value();
    }

    public String toString() {
        return " +";
    }
    public void xmlSerializeRootOpen(Writer out) throws IOException {
		out.write("<READ>");
	}

	public void xmlSerializeRootClose(Writer out) throws IOException {
		out.write("</READ>");
	}

//	 satisfy abstract methods from BaseAST
    public void initialize(int t, String txt) {
    }
    public void initialize(AST t) {
    }
    public void initialize(Token tok) {
    }

}
