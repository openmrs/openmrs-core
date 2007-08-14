package org.openmrs.synchronization.engine;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Atomic unit of the sync process.
 *
 */
@Root
public class SyncItem implements Serializable {

    public static final long serialVersionUID = 0L;
    public Log log = LogFactory.getLog(this.getClass());

    public enum SyncItemState {
        NEW, 
        UPDATED, 
        DELETED, 
        SYNCHRONIZED, 
        UNKNOWN, 
        CONFLICT
    };
 
    // Fields
    private SyncItemKey key = null;
    private SyncItemState state = SyncItemState.UNKNOWN;
    @Element(data=true)
    private String content = null;
    
    // Properties
    public SyncItemKey getKey() {
        return key;
    }
    
    public void setKey(SyncItemKey key) {
        this.key = key;
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
        if (!(o instanceof SyncItem))
            return false;

        return ((SyncItem) o).getKey().equals(key);
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

}
