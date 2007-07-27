package org.openmrs.synchronization.engine;

/**
 * Atomic unit of the sync process.
 *
 */
public interface SyncItem {

    public enum SyncItemState {NEW, UPDATED, DELETED, SYNCHRONIZED, UNKNOWN, CONFLICT};
 
    public SyncItemKey getKey();
    public void setKey(SyncItemKey key);

    public SyncItemState getState();
    public void setState(SyncItemState state);
 
    public String getContent();
    public void setContent(String content);

    //temp - get rid of this when we have real serializer
    public byte[] getByteContent();
    public void setByteContent(byte[] content);

}
