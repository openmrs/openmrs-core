package org.openmrs.synchronization.engine;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncItemKey;


public class SyncItemImpl implements Serializable, SyncItem {
    
    public static final long serialVersionUID = 0L;
    public Log log = LogFactory.getLog(this.getClass());
    

    // Fields
    protected SyncItemKey key = null;
    protected SyncItemState state = SyncItemState.UNKNOWN;
    protected String content = null;
    protected byte[] byteContent = null;

    // Constructor(s)
    protected SyncItemImpl() {}
    
    // Properties
    public SyncItemKey getKey() {
        return this.key;
    }
    public void setKey(SyncItemKey newKey) {
        this.key = newKey;
    }
    public SyncItemState getState(){
        return state;
    }
    public void setState(SyncItemState newState){
        this.state = newState;
    }
    public String getContent(){
        return content;
    }
    public void setContent(String newContent){
        this.content = newContent;
    }
    public byte[] getByteContent(){
        return byteContent;
    }
    public void setByteContent(byte[] newContent){
        this.byteContent = newContent;
    }

  
   public boolean equals(Object o) {
       if (!(o instanceof SyncItem)) return false;

       return ((SyncItem)o).getKey().equals(key);
   }


   public int hashCode() {
       return getKey().hashCode();
   }

}
