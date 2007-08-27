package org.openmrs.synchronization.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.serial.FilePackage;
import org.openmrs.serial.Item;
import org.openmrs.serial.IItem;
import org.openmrs.serial.Record;
import org.openmrs.serial.TimestampNormalizer;

/**
 * SyncTransmission a collection of sync records to be sent to the parent.
 */
public class SyncTransmission implements IItem {

    // consts

    // fields
    private final Log log = LogFactory.getLog(getClass());
    
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_S"); //used to format file names 
    private String fileName = null;
    private List<SyncRecord> syncRecords = null;
    private String guid = null;

    // constructor(s)
    SyncTransmission() {
    }

    /* 
     * Take passed in records and create a new sync_tx file
     */
    SyncTransmission(List<SyncRecord> valRecords) {

        guid = UUID.randomUUID().toString();        
        fileName = "sync_tx_" + sdf.format(new Date());
        this.syncRecords = valRecords;
    }

    // methods
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
    public List<SyncRecord> getSyncRecords() {
        return syncRecords;
    }
    public void setSyncRecords(List<SyncRecord> value) {
        this.syncRecords = value;
    }

    /** Create a new transmission from records: use org.openmrs.serial to make a file
     * 
     */
    public void CreateFile() {

        try {            
            FilePackage pkg = new FilePackage();
            Record xml = pkg.createRecordForWrite("SyncTransmission");
            Item root = xml.getRootItem();

            //serialize
            this.save(xml,root);

            //now dump to file
            pkg.savePackage(org.openmrs.util.OpenmrsUtil
                    .getApplicationDataDirectory()
                    + "/journal/" + fileName);

        } catch (Exception e) {
            log.error("Cannot create sync transmission.");
            throw new SyncException("Cannot create sync transmission", e);
        }
        return;

    }

    /** IItem.save() implementation
     * 
     */
    public Item save(Record xml, Item parent) throws Exception {
        Item me = xml.createItem(parent, this.getClass().getName());
        
        //serialize primitives
        if (guid != null) xml.setAttribute(me, "guid", guid);
        if (fileName != null) xml.setAttribute(me, "fileName", fileName);
        
        //serialize Records list
        Item itemsCollection = xml.createItem(me, "records");
        
        if (syncRecords != null) {
            me.setAttribute("itemCount", Integer.toString(syncRecords.size()));
            Iterator<SyncRecord> iterator = syncRecords.iterator();
            while (iterator.hasNext()) {
                iterator.next().save(xml, itemsCollection);
            }
        };

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
            this.syncRecords = null;
        } else {
            this.syncRecords = new ArrayList<SyncRecord>();
            List<Item> serItems = xml.getItems(itemsCollection);
            for (int i = 0; i < serItems.size(); i++) {
                Item serItem = serItems.get(i);
                SyncRecord syncRecord = new SyncRecord();
                syncRecord.load(xml, serItem);
                syncRecords.add(syncRecord);
            }
        }

    }
}