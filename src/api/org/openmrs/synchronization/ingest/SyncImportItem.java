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
package org.openmrs.synchronization.ingest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.serialization.Item;
import org.openmrs.serialization.Record;
import org.openmrs.synchronization.SyncException;
import org.openmrs.synchronization.SyncItemState;
import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncItemKey;

/**
 *
 */
public class SyncImportItem {

    private Log log = LogFactory.getLog(this.getClass());

    // Fields
    private SyncItemKey<?> key = null;
    private SyncItemState state = SyncItemState.UNKNOWN;
    private String content = null;
    private String errorMessage = "";
    private String errorMessageArgs = null;
    
    public String getErrorMessage() {
    	return errorMessage;
    }

	public void setErrorMessage(String errorMessage) {
    	this.errorMessage = errorMessage;
    }

	// Properties
    public SyncItemKey<?> getKey() {
        return key;
    }
    
    public void setKey(SyncItemKey<?> key) {
        this.key = key;
    }

    public String getErrorMessageArgs() {
    	return errorMessageArgs;
    }

	public void setErrorMessageArgs(String errorMessageArgs) {
    	this.errorMessageArgs = errorMessageArgs;
    }

	public SyncItemState getState() {
        return state;
    }
    
    public void setState(SyncItemState state) {
        this.state = state;
    }
 
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }

    // Methods
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SyncItem) || o == null)
            return false;

        SyncItem oSync = (SyncItem) o;
        boolean same = ((oSync.getKey() == null) ? (this.getKey() == null) : oSync.getKey().equals(this.getKey()))
                && ((oSync.getContent() == null) ? (this.getContent() == null) : oSync.getContent().equals(this.getContent()))
                && ((oSync.getState() == null) ? (this.getState() == null) : oSync.getState().equals(this.getState()));
        
        return same;
    }

    @Override
    public int hashCode() {
        //FIXME: Key might be null, though it shouldn't..
        // Should these key-objects implement some interface - causes problems with serialization.
        if (getKey() != null) {
            return getKey().hashCode();
        } else {
            return super.hashCode();
        }
    }

    public Item save(Record xml, Item parent) throws Exception {
        Item me = xml.createItem(parent, this.getClass().getName());

        //serialize primitives
        xml.setAttribute(me, "state", state.toString());
        xml.setAttribute(me, "errorMessage", errorMessage.toString());
        
        Item itemKey = xml.createItem(me, "key");
        if (key != null) {
            xml.setAttribute(itemKey,"key-type",key.getKeyValue().getClass().getName());
            key.save(xml, itemKey);
        }
        
        Item itemContent = xml.createItem(me, "content");
        if (content != null) {
            xml.createTextAsCDATA(itemContent, content);
        }
        
        return me;
    }
    
    public void load(Record xml, Item me) throws Exception {
        state = SyncItemState.valueOf(me.getAttribute("state"));
        errorMessage = me.getAttribute("errorMessage");
        Item itemKey = xml.getItem(me, "key");
        
        if (itemKey.isEmpty()) {
            key = null;
        } else {
            String keyType = itemKey.getAttribute("key-type");
            if (keyType.equals("java.lang.String")) {
                key = new SyncItemKey<String>(String.class);
                key.load(xml, xml.getFirstItem(itemKey));
            } else {
                throw new SyncException("Failed to deserialize SyncItem, could not create sync key of type: " + keyType);
            }
        }
        
        Item itemContent = xml.getItem(me, "content");
        if (itemContent.isEmpty()) {
            content = null;
        } else {
            content = itemContent.getText();
        }
    }

}
