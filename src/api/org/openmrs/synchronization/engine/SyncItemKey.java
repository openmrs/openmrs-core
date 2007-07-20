package org.openmrs.synchronization.engine;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class represents a unique identifier of a <i>SyncItem</i> item in a repository.
 *
 */
public class SyncItemKey implements Comparable , Serializable {
    
    public static final long serialVersionUID = 0L;
    public Log log = LogFactory.getLog(this.getClass());

    // Fields
    private Object keyValue = null;

    // Constructors
    public SyncItemKey(Object keyValue) {
        assert (keyValue != null);
        this.keyValue = keyValue;
    }

    // Properties
    public Object getKeyValue(){
        return keyValue;
    }
    public void setKeyValue(Object keyValue){
        assert (keyValue != null);
        this.keyValue = keyValue;
    }


    // Methods
    public String getKeyAsString() {
        return keyValue.toString();
    }

    public boolean equals(Object o) {
        if (!(o instanceof SyncItemKey)) return false;
        return ((SyncItemKey)o).getKeyAsString().equals(getKeyAsString());
    }

    public int compareTo(Object o) {

        if (!(o instanceof SyncItemKey)) {
            throw new ClassCastException("A SyncItemKey object expected.");
        }
        SyncItemKey otherKey = (SyncItemKey)o;

        if (keyValue != null && otherKey.getKeyValue() != null) {
            return keyValue.toString().compareTo(otherKey.getKeyValue().toString());
        }
        if (keyValue == null && otherKey.getKeyValue() == null) {
            return 0;
        }

        return -1;
    }
   
    public int hashCode() {
        return getKeyAsString().hashCode();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append( " { keyValue: "  );
        sb.append( getKeyAsString() );
        sb.append( " } "            );

        return sb.toString();
    }
}
