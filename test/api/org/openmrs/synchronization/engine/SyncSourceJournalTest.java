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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.openmrs.BaseContextSensitiveTest;
import org.openmrs.api.SynchronizationService;
import org.openmrs.api.context.Context;
import org.openmrs.synchronization.SyncItemState;

/**
 * TODO: describe file
 */
public class SyncSourceJournalTest extends BaseContextSensitiveTest {

    /**
     * Auto generated method comment
     * 
     * @throws Exception
     */
    public void testGlobalProperties() throws Exception {
    	initializeInMemoryDatabase();
        authenticate();
        
        SyncSource source = null;
        SyncPoint<Date> p = null;
        Date d = new Date();
        
        //init
        source = new SyncSourceJournal();
        
        //test read/write null local
        p = new SyncPoint<Date>(null);
        source.setLastSyncLocal(p);
        assertNull(source.getLastSyncLocal().getValue());

        //test read/write null remote
        p = new SyncPoint<Date>(null);
        source.setLastSyncRemote(p);
        assertNull(source.getLastSyncRemote().getValue());
        
        //test read/write now local
        p = new SyncPoint<Date>(new Date());
        source.setLastSyncLocal(p);
        assertEquals(source.getLastSyncLocal().getValue(),p.getValue());

        //test read/write now remote
        p = new SyncPoint<Date>(new Date());
        source.setLastSyncRemote(p);
        assertEquals(source.getLastSyncRemote().getValue(),p.getValue());

    }
    
    /**
     * Auto generated method comment
     * 
     * @throws Exception
     */
    public void testGetChanged() throws Exception {
    	initializeInMemoryDatabase();
        authenticate();

        //get service
        SynchronizationService service = Context.getSynchronizationService();

        //Create initial set of records
        List<SyncRecord> changed =SyncSourceJournalTest.createRecords(service);
        
        //make it write to DB
        this.transactionManager.commit(this.transactionStatus);
        
        //test sync source
        SyncSource source = new SyncSourceJournal();
        
        SyncRecord first = service.getFirstSyncRecordInQueue();
        
        SyncPoint<Date> from = new SyncPoint<Date>();
        SyncPoint<Date> to = new SyncPoint<Date>(new Date());
        
        from.setValue(new Date(first.getTimestamp().getTime() - 60000));
        source.getChanged(from, to);
        
        //Cleanup the journal
        SyncSourceJournalTest.cleanupRecords(service, changed);
         
        return;
    }
    
    /**
     * helper method - can be reused by other sync tests
     */
    public static List<SyncRecord> createRecords(SynchronizationService syncService) {

        
        //create some test records
        String guid1 = UUID.randomUUID().toString();
        SyncRecord syncRecord1 = new SyncRecord();
        syncRecord1.setTimestamp(new Date());
        syncRecord1.setGuid(guid1);

        // Create a couple of SyncItems
        SyncItem item11 = new SyncItem();
        item11.setContent("<Person><Name>Some Person</Name></Person>");
        item11.setState(SyncItemState.NEW);
        item11.setKey(new SyncItemKey<String>(UUID.randomUUID().toString(),String.class));

        SyncItem item12 = new SyncItem();
        item12.setContent("<PersonAddress><Street>Some Street</Street></PersonAddress>");
        item12.setState(SyncItemState.UPDATED);
        item12.setKey(new SyncItemKey<String>(UUID.randomUUID().toString(),String.class));

        // Add them to the list
        List<SyncItem> items1 = new ArrayList<SyncItem>();
        items1.add(item11);
        items1.add(item12);

        // Add them to the record 1 & store
        syncRecord1.setItems(items1);
        syncService.createSyncRecord(syncRecord1);

        //setup record 2
        SyncRecord syncRecord2 = new SyncRecord();
        String guid2 = UUID.randomUUID().toString();
        syncRecord2.setTimestamp(new Date());
        syncRecord2.setGuid(guid2);
        SyncItem item21 = new SyncItem();
        item21.setContent("<Person><Name>Some Person record 2</Name></Person>");
        item21.setState(SyncItemState.UPDATED);
        item21.setKey(item11.getKey());
        
        // Store record2
        List<SyncItem> items2 = new ArrayList<SyncItem>();
        items2.add(item21);   
        syncRecord2.setItems(items2);
        syncService.createSyncRecord(syncRecord2);
        
        List<SyncRecord> records = new ArrayList<SyncRecord>();
        records.add(syncRecord1);
        records.add(syncRecord2);
        
        return records;
    }
    
    /**
     * Auto generated method comment
     * 
     * @param syncService
     * @param records
     */
    public static void cleanupRecords(SynchronizationService syncService,List<SyncRecord> records) {
        Iterator<SyncRecord> iterator = records.iterator();
        while (iterator.hasNext()) {
            syncService.deleteSyncRecord(iterator.next());
        };
        return;   
    }
}
