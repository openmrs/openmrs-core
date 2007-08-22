package org.openmrs.synchronization.engine;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.serial.Item;
import org.openmrs.serial.IItem;
import org.openmrs.serial.Record;

/**
 * This class represents a unique identifier of a <i>SyncItem</i> item based on a simple generic.  
 *
 */
public class SyncItemKey<T> implements Serializable, IItem {
    
    public static final long serialVersionUID = 0L;
    public Log log = LogFactory.getLog(this.getClass());

    // Fields
    private T keyValue = null;
    private Class<T> genericType = null; 

    // Constructors
    public SyncItemKey() {} //REMOVE
    public SyncItemKey(T keyValue) { //REMOVE
        assert (keyValue != null);
        this.keyValue = keyValue;
    }

    public SyncItemKey(Class<T> genericTypeValue) {
        genericType = genericTypeValue;
    }
    public SyncItemKey(T keyValue,Class<T> genericTypeValue) {
        assert (keyValue != null);
        this.keyValue = keyValue;        
        genericType = genericTypeValue;
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
        return ((SyncItemKey<?>)o).getKeyValue().equals(getKeyValue());
    }
    
    @Override
    public int hashCode() {
        return getKeyValue().hashCode();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append( " { keyType: "  );
        sb.append( this.getClass().getTypeParameters()[0].getName());
        sb.append( " } "            );
        sb.append( " { keyValue: "  );
        sb.append( getKeyValue() );
        sb.append( " } "            );

        return sb.toString();
    }
    
    public Item save(Record xml, Item parent) throws Exception {
        Item me = xml.createItem(parent, this.getClass().getName());
        if (genericType != null)
            me.setAttribute("type",this.genericType.getName() );
        else
            me.setAttribute("type","" );
        me.setAttribute("value", getKeyValue().toString() );
        
        return me;
    }

    public void load(Record xml, Item me) throws Exception {
        // TODO
    }
    
}
