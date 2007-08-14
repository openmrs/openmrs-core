package org.openmrs.synchronization.engine;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class represents a unique identifier of a <i>SyncItem</i> item based on a simple generic.  
 *
 */
public class SyncItemKey<T> implements Serializable {
    
    public static final long serialVersionUID = 0L;
    public Log log = LogFactory.getLog(this.getClass());

    // Fields
    private T keyValue = null;

    // Constructors
    public SyncItemKey() {}
    public SyncItemKey(T keyValue) {
        assert (keyValue != null);
        this.keyValue = keyValue;
    }

    // Properties
    public T getKeyValue(){
        return keyValue;
    } 
    public void setKeyValue (T keyValue){
        assert (keyValue != null);
        this.keyValue = keyValue;
    }

    // Methods
       
    //equality is determined based on T.equals()
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SyncItemKey)) return false;
        return ((SyncItemKey)o).getKeyValue().equals(getKeyValue());
    }
    
    @Override
    public int hashCode() {
        return getKeyValue().hashCode();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append( " { keyValue: "  );
        sb.append( getKeyValue() );
        sb.append( " } "            );

        return sb.toString();
    }
}
