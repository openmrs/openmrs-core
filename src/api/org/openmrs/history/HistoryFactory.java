package org.openmrs.history;

import java.util.HashMap;
import java.util.Iterator;
import java.io.*;
import java.lang.reflect.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * History factory - construct a history class (extended) from the target
 * object and populate it from the target's getter/setters
 *
 * @author Julie Melbin
 * @version 1.0
*/
public class HistoryFactory
{
    private static final String _tableName = "_history";
    private static final String _className = "History";

    // swapping for cmdline test
	protected static final Log log = LogFactory.getLog(HistoryFactory.class);
    //static aLog log = new aLog();
        
    /** Create an object which represents the history
     * of the target object provided
     */
    public static IHistory createHistory(Object theObject)
    {
        Class theClass, theHistoryClass;
        String theName, theHistoryName;
        IHistory theHistory = null;

        // history objects follow a well known convention
        // which mirrors the db table naming
        theName = theObject.getClass().getName();
        theHistoryName = theName + _className;

        try {
            theClass = Class.forName(theName);
            theHistoryClass = Class.forName(theHistoryName);

            // we expect an empty constructor
            theHistory = (IHistory)theHistoryClass.newInstance();

            // for all the members of theObject, copy the values
            // into the IHistory object (which is supposed
            // to be a superset of theObject)
            Field[] fields = theClass.getDeclaredFields();
            for (int i = 0;  i < fields.length; i++)
            {
                // this works correctly ONLY when all the fields needed
                // from theObject to properly create an IHistory object have get/set methods;
                // local computed variables that require a other than get/set to be 
                // populate will FAIL this 'clone' attempt
                copyProperty(fields[i].getName(), (Object)theObject, (Object)theHistory);
            }
        } 
        catch (ClassNotFoundException ex) {
            log.error("Request failed for " + theName + ": no history object");
            ex.printStackTrace();
        }
        catch (Exception e) {
            log.error("Request failed for " + theName + ": exception creating class");
            e.printStackTrace();
        }

        return theHistory;
    }
        
    /** Use reflection to copy the value from the src object
     * into the same named field in the IHistory object
     */
    private static void copyProperty(String name, Object src, Object dest) 
    {
        // construct getter method for property in the standard bean flavor
        String prop = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        String mname = "get" + prop;
        Class[] types = new Class[] {};

        // read from source object - no getter? we can't clone it
        Method method;
        try {
            method = src.getClass().getMethod(mname, types);
            Object result = method.invoke(src, new Object[0]);
            
            // the setter method using the getter's return type (for multiple getter
            // methods, we're just using whatever comes back first and ignoring all 
            // others - not expecting that to be a common occurrence)
            mname = "set" + prop;
            types = new Class[1]; 
            types[0] = method.getReturnType(); // use return type from getter
            
            // write to dest object
            try {
                method = dest.getClass().getMethod(mname, types);
                method.invoke(dest, result);

                log.debug("Copied property " + name);
            }
            // no setter is more disturbing than no getter
            catch (Exception e){
                log.error("No method '" + mname + "' in " + dest.getClass().getName());
            }
        }
        // no 'get' might mean this field is private/computed?
        catch (Exception e) {
            log.warn("No method '" + mname  + "' in " + src.getClass().getName());
        }
    }
}

// just used for command line testing
class aLog
{
    public void warn(String s) {out(s);}
    public void error(String s) {out(s);}
    public void debug(String s) {out(s);}
    public void info(String s) {out(s);}
    
    private void out(String s)
    {
        System.out.println(s);
    }
}
