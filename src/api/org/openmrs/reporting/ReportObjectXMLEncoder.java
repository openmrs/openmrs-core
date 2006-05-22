package org.openmrs.reporting;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReportObjectXMLEncoder {

	private Log log = LogFactory.getLog(this.getClass());
	
	private Object objectToEncode;
	
	public ReportObjectXMLEncoder( Object objectToEncode ) {
		this.objectToEncode = objectToEncode;
	}
	
	public String toXmlString() {
		ByteArrayOutputStream arr = new ByteArrayOutputStream();
	    XMLEncoder enc = new XMLEncoder(new BufferedOutputStream(arr));
	    for ( Field f : this.objectToEncode.getClass().getDeclaredFields() ) {
    		//System.out.println("\n\n\nABOUT TO CHECK ENUM: " + f.getName() + " is " + f.getType().getName() + "\n\n\n");
	    	if ( f.getType().isEnum() ) {
	    		//System.out.println("\n\n\nABOUT TO WRITE AN ENUM\n\n\n");
	    		try {
	    			enc.setPersistenceDelegate(f.getType(), new EnumDelegate());
	    		} catch( Exception e ) {
	    			//System.out.println("ReportObjectXMLEncoder failed to write enumeration " + f.getName());
	    		}
	    	}
	    }
	    log.error("objectToEncode.type: " + objectToEncode.getClass());
	    enc.writeObject(this.objectToEncode);
	    enc.close();
	    
	    return arr.toString();
	}

	/**
	 * @return Returns the objectToEncode.
	 */
	public Object getObjectToEncode() {
		return objectToEncode;
	}

	/**
	 * @param objectToEncode The objectToEncode to set.
	 */
	public void setObjectToEncode(Object objectToEncode) {
		this.objectToEncode = objectToEncode;
	}

	class EnumDelegate extends DefaultPersistenceDelegate
	{
	   protected Expression instantiate(Object oldInstance, Encoder out)
	   {
	      return new Expression(Enum.class,
	         "valueOf",
	         new Object[] { oldInstance.getClass(), ((Enum) oldInstance).name() });
	   }
	   protected boolean mutatesTo(Object oldInstance, Object newInstance)
	   {
	      return oldInstance == newInstance;
	   }
	}
}
