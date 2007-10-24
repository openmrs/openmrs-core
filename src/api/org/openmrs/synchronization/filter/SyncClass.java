/**
 * Auto generated file comment
 */
package org.openmrs.synchronization.filter;

/**
  *
  */
public class SyncClass {
    private Integer syncClassId;
    private String name;
    private SyncClassType type;
    private Boolean defaultTo;
    private Boolean defaultFrom;
    
    public Boolean getDefaultFrom() {
        return defaultFrom;
    }
    public void setDefaultFrom(Boolean defaultFrom) {
        this.defaultFrom = defaultFrom;
    }
    public Boolean getDefaultTo() {
        return defaultTo;
    }
    public void setDefaultTo(Boolean defaultTo) {
        this.defaultTo = defaultTo;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getSyncClassId() {
        return syncClassId;
    }
    public void setSyncClassId(Integer syncClassId) {
        this.syncClassId = syncClassId;
    }
    public SyncClassType getType() {
        return type;
    }
    public void setType(SyncClassType type) {
        this.type = type;
    }
    
    
    
}
