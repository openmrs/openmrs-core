/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.synchronization.engine;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.serialization.Item;
import org.openmrs.serialization.IItem;
import org.openmrs.serialization.Record;
import org.openmrs.synchronization.SyncException;

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
        if (!(o instanceof SyncItemKey) || o == null) return false;
        if ( ((SyncItemKey)o).genericType.getName() != this.genericType.getName())
            return false;
        else
            return (((SyncItemKey<?>)o).getKeyValue() == null) ?
                (this.getKeyValue() == null) : ((SyncItemKey<?>)o).getKeyValue().equals(this.getKeyValue());
    }
    
    @Override
    public int hashCode() {
        return getKeyValue().hashCode();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append( " { keyType: "  );
        sb.append( this.genericType.getName());
        sb.append( " } "            );
        sb.append( " { keyValue: "  );
        sb.append( getKeyValue() );
        sb.append( " } "            );

        return sb.toString();
    }
    
    public Item save(Record xml, Item parent) throws Exception {
        Item me = xml.createItem(parent, this.getClass().getName());
        
        if (genericType != null) {
            me.setAttribute("type", genericType.getName() );
        }
        else {
            me.setAttribute("type", "");
        }
        
        me.setAttribute("value", getKeyValue().toString() );
        
        return me;
    }

    @SuppressWarnings("unchecked")
    public void load(Record xml, Item me) throws Exception {
        //TODO: is this adequate for type safety?
        if (this.genericType.getName().equals(me.getAttribute("type"))) 
            this.keyValue = (T)me.getAttribute("value");
        else
            throw new SyncException("Failed to deserialize SyncItemKey, type mismatch. Expected type: " + this.genericType.getName() + " found: " + me.getAttribute("type"));
    }
    
}
