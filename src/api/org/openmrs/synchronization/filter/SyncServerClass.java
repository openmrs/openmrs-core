/**
 * Auto generated file comment
 */
package org.openmrs.synchronization.filter;

import org.openmrs.synchronization.server.RemoteServer;

/**
 *
 */
public class SyncServerClass {
    private Integer serverClassId;
    private RemoteServer syncServer;
    private SyncClass syncClass;
    private Boolean sendTo;
    private Boolean receiveFrom;
    
    public SyncServerClass() {}
    
    /**
     * @param server
     * @param syncClass2
     */
    public SyncServerClass(RemoteServer server, SyncClass syncClass) {
        this.syncServer = server;
        this.syncClass = syncClass;
        this.sendTo = syncClass.getDefaultTo();
        this.receiveFrom = syncClass.getDefaultFrom();
    }
    public Boolean getReceiveFrom() {
        return receiveFrom;
    }
    public void setReceiveFrom(Boolean receiveFrom) {
        this.receiveFrom = receiveFrom;
    }
    public Boolean getSendTo() {
        return sendTo;
    }
    public void setSendTo(Boolean sendTo) {
        this.sendTo = sendTo;
    }
    public Integer getServerClassId() {
        return serverClassId;
    }
    public void setServerClassId(Integer serverClassId) {
        this.serverClassId = serverClassId;
    }
    public SyncClass getSyncClass() {
        return syncClass;
    }
    public void setSyncClass(SyncClass syncClass) {
        this.syncClass = syncClass;
    }
    public RemoteServer getSyncServer() {
        return syncServer;
    }
    public void setSyncServer(RemoteServer syncServer) {
        this.syncServer = syncServer;
    }
    
    
}
