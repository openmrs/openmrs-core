package org.openmrs.arden.parser;

public abstract class StatementNode extends ArdenAST {
	/** Make me look like a heterogeneous tree */
    public ArdenAST left() {
        return (ArdenAST)getFirstChild();
    }

    public ArdenAST right() {
        ArdenAST t = left();
        if ( t==null ) return null;
        return (ArdenAST)t.getNextSibling();
    }
}
