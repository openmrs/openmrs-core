package org.openmrs.synchronization.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;

import org.openmrs.BaseTest;
import org.openmrs.api.context.Context;
import org.openmrs.api.SynchronizationService;
import org.openmrs.serial.FilePackage;
import org.openmrs.serial.Item;
import org.openmrs.serial.IItem;
import org.openmrs.serial.Package;
import org.openmrs.serial.Record;

import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncItemKey;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.engine.SyncRecordState;
import org.openmrs.synchronization.engine.SyncItem.SyncItemState;

public class SyncRecordTest extends SyncBaseTest {

    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        authenticate();
        
    }
    
    /*
     * test serialization of syncRecord
     */
    public void testSerialization() throws Exception {
        assertTrue(Context.isAuthenticated());
                                
        //'normal' state
        String guid1 = UUID.randomUUID().toString();
        SyncRecord syncRecord1 = new SyncRecord();
        syncRecord1.setTimestamp(new Date());
        syncRecord1.setGuid(guid1);
        SyncItem item11 = new SyncItem();
        item11.setContent("<Person><Name>Some Person</Name></Person>");
        item11.setState(SyncItemState.NEW);
        item11.setKey(new SyncItemKey<String>(UUID.randomUUID().toString(),String.class));
        SyncItem item12 = new SyncItem();
        item12.setContent("<PersonAddress><Street>Some Street</Street></PersonAddress>");
        item12.setState(SyncItemState.UPDATED);
        item12.setKey(new SyncItemKey<String>(UUID.randomUUID().toString(),String.class));
        List<SyncItem> items1 = new ArrayList<SyncItem>();
        items1.add(item11);
        items1.add(item12);
        syncRecord1.setItems(items1);
        
        // 'weird' end cases
        
        //no timestamp or items
        SyncRecord syncRecord2 = new SyncRecord();
        syncRecord2.setGuid(UUID.randomUUID().toString());

        //dump out the state
        Package pkg = new FilePackage();
        Record record = pkg.createRecordForWrite("SyncRecordTest");
        Item top = record.getRootItem();
        
        ((IItem)syncRecord1).save(record, top);
        ((IItem)syncRecord2).save(record, top);
        
        System.out.println("*** serialized state ***");
        try {
            System.out.println(record.toStringAsDocumentFragement());
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
            fail("Serialization failed with an exception: " + e.getMessage());
        }        
         
        //now Test deserialize - THIS DOES NOT WORK YET
        String textDes = record.toStringAsDocumentFragement();
        
        Package pkgDes = new FilePackage();
        Record recordDes = pkgDes.createRecordFromString(textDes);
        Item topDes = recordDes.getRootItem();
        
        //get items list that holds serialized sync records
        List<Item> itemsDes = recordDes.getItems(topDes);
        
        SyncRecord syncRecordDesc = null;
        Iterator<Item> iterator = itemsDes.iterator();
        while (iterator.hasNext()) {
            syncRecordDesc = new SyncRecord();
            syncRecordDesc.load(recordDes, iterator.next());
            
            //TODO assert we got the same two records
        }        
                        
        return;
    }        
}
