package org.openmrs.arden.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import antlr.Parser;
import antlr.RecognitionException;

/**
 * An error handler that counts parsing errors and warnings.
 */
public class ErrorCounter implements ParseErrorHandler {
//	private Log log = LogFactory.getLog( "arden.parser" );

	private Parser parser = null;
	private List errorList = new ArrayList();
	private List warningList = new ArrayList();


	public ErrorCounter(Parser parser) {
		this.parser = parser;
	}

	public void reportError(RecognitionException e) {
		reportError( e.toString() );
	//	log.error( e, e );
	}

	public void reportError(String s) {
		String message = ( getFilename() == null ) ? "*** ERROR: " + s : getFilename() + ": *** ERROR: " + s;
	//	log.error( message );
		errorList.add( message );
	}

	public int getErrorCount() {
		return errorList.size();
	}

	public void reportWarning(String s) {
		String message = ( getFilename() == null ) ? "*** WARNING: " + s : getFilename() + ": *** WARNING: " + s;
	//	log.warn( message );
		warningList.add( message );
	}

	public int getWarningCount() {
		return warningList.size();
	}

	public String[] getErrorMessages() {
		return ( String[] ) errorList.toArray( new String[errorList.size()] );
	}

	public String getErrorString() {
		StringBuffer buf = new StringBuffer();
		for ( Iterator iterator = errorList.iterator(); iterator.hasNext(); ) {
			buf.append( ( String ) iterator.next() );
			if ( iterator.hasNext() ) buf.append( "\n" );

		}
		return buf.toString();
	}

	private String getFilename() {
		return ( parser == null ) ? null : parser.getFilename();
	}

}

