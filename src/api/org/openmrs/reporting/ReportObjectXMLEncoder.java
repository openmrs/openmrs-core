package org.openmrs.reporting;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.PatientSetService;

public class ReportObjectXMLEncoder {

	private Log log = LogFactory.getLog(this.getClass());
	
	private Object objectToEncode;
	
	public ReportObjectXMLEncoder( Object objectToEncode ) {
		this.objectToEncode = objectToEncode;
	}
	
	public String toXmlString() {
		ByteArrayOutputStream arr = new ByteArrayOutputStream();
		EnumDelegate enumDelegate = new EnumDelegate();
	    XMLEncoder enc = new XMLEncoder(new BufferedOutputStream(arr));
	    Set<Class> alreadyAdded = new HashSet<Class>();
	    {
	    	List<Class> enumClasses = new ArrayList<Class>();
	    	enumClasses.add(PatientSetService.Modifier.class);
	    	enumClasses.add(PatientSetService.TimeModifier.class);
	    	enumClasses.add(PatientSetService.BooleanOperator.class);
	    	enumClasses.add(PatientSetService.GroupMethod.class);
	    	for (Class clz : enumClasses) {
		    	enc.setPersistenceDelegate(clz, enumDelegate);
		    	alreadyAdded.add(clz);
	    	}
	    }
	    // This original implementation won't handle enums that aren't direct properties of the bean, but I'm leaving it here anyway.
	    for ( Field f : this.objectToEncode.getClass().getDeclaredFields() ) {
	    	Class clz = f.getType();
	    	if ( clz.isEnum() && !alreadyAdded.contains(clz) ) {
	    		try {
	    			enc.setPersistenceDelegate(clz, enumDelegate);
	    			alreadyAdded.add(clz);
	    		} catch( Exception e ) {
	    			log.error("ReportObjectXMLEncoder failed to write enumeration " + f.getName(), e);
	    		}
	    	}
	    }
	    log.debug("objectToEncode.type: " + objectToEncode.getClass());
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
