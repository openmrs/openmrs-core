package org.openmrs.synchronization.ingest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.serial.FilePackage;
import org.openmrs.serial.IItem;
import org.openmrs.serial.Item;
import org.openmrs.serial.Record;
import org.openmrs.synchronization.engine.SyncException;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.engine.SyncTransmission;

/**
 * SyncTransmission a collection of sync records to be sent to the parent.
 */
public class SyncTransmissionResponse implements IItem {

    // consts

    // fields
    private final Log log = LogFactory.getLog(getClass());
    
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_S"); //used to format file names 
    private String fileName = null;
    private List<SyncImportRecord> syncImportRecords = null;
    private String guid = null;
    private String fileOutput = "";

	// constructor(s)
    public SyncTransmissionResponse() {
    }

    /* 
     * Take passed in records and create a new sync_tx file
     */
    
    public SyncTransmissionResponse(SyncTransmission transmission) {
    	this.guid = transmission.getGuid();
    	fileName = transmission.getFileName();
    	int idx = fileName.lastIndexOf(".");
    	if ( idx > -1 ) fileName = fileName.substring(0, idx) + "_response" + fileName.substring(idx);
    	else fileName = fileName + "_response";
    }

    public List<SyncImportRecord> getSyncImportRecords() {
    	return syncImportRecords;
    }

	public void setSyncImportRecords(List<SyncImportRecord> syncImportRecords) {
    	this.syncImportRecords = syncImportRecords;
    }

	// methods
    public String getFileOutput() {
    	return fileOutput;
    }

    public String getFileName() {
        return fileName;
    }
    public void setFileName(String value) {
        fileName = value;
    }
    public String getGuid() {
        return guid;
    }
    public void setGuid(String value) {
        guid = value;
    }
    
    /** Create a new transmission from records: use org.openmrs.serial to make a file
     * 
     */
    public void CreateFile() {

        try {            
            FilePackage pkg = new FilePackage();
            Record xml = pkg.createRecordForWrite(this.getClass().getName());
            Item root = xml.getRootItem();

            //serialize
            this.save(xml,root);

            //now dump to file
            fileOutput = pkg.savePackage(org.openmrs.util.OpenmrsUtil
                    .getApplicationDataDirectory()
                    + "/import/" + fileName, true);

        } catch (Exception e) {
            log.error("Cannot create sync transmission.");
            throw new SyncException("Cannot create sync transmission", e);
        }
        return;

    }

    /** IItem.save() implementation
     * 
     */
    public Item save(Record xml, Item me) throws Exception {
        //Item me = xml.createItem(parent, this.getClass().getName());
        
        //serialize primitives
        if (guid != null) xml.setAttribute(me, "guid", guid);
        if (fileName != null) xml.setAttribute(me, "fileName", fileName);
        
        //serialize Records list
        Item itemsCollection = xml.createItem(me, "records");
        
        if (syncImportRecords != null) {
            me.setAttribute("itemCount", Integer.toString(syncImportRecords.size()));
            for ( SyncImportRecord importRecord : syncImportRecords ) {
            	importRecord.save(xml, itemsCollection);
            }
            /*
             * replaced by 5.0 shorthand
            Iterator<SyncImportRecord> iterator = syncImportRecords.iterator();
            while (iterator.hasNext()) {
                iterator.next().save(xml, itemsCollection);
            }
            */
        }

        return me;
    }

    /** IItem.load() implementation
     * 
     */
    public void load(Record xml, Item me) throws Exception {
        
        this.guid = me.getAttribute("guid");
        this.fileName = me.getAttribute("fileName");
        
        //now get items
        Item itemsCollection = xml.getItem(me, "records");
        
        if (itemsCollection.isEmpty()) {
            this.syncImportRecords = null;
        } else {
            this.syncImportRecords = new ArrayList<SyncImportRecord>();
            List<Item> serItems = xml.getItems(itemsCollection);
            for (int i = 0; i < serItems.size(); i++) {
                Item serItem = serItems.get(i);
                SyncImportRecord syncImportRecord = new SyncImportRecord();
                syncImportRecord.load(xml, serItem);
                this.syncImportRecords.add(syncImportRecord);
            }
        }

    }
}