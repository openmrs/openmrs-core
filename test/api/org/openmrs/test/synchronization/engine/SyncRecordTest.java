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
package org.openmrs.test.synchronization.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.openmrs.serialization.FilePackage;
import org.openmrs.serialization.IItem;
import org.openmrs.serialization.Item;
import org.openmrs.serialization.Package;
import org.openmrs.serialization.Record;
import org.openmrs.synchronization.SyncItemState;
import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncItemKey;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.test.annotation.NotTransactional;

/**
 * Tests sync record serialization.
 */
public class SyncRecordTest extends BaseContextSensitiveTest  {

    /**
     * test serialization of syncRecord
     * 
     * @throws Exception
     */
	@Test
    @NotTransactional
    public void shouldSerialization() throws Exception {

        // 'normal' state
        String guid1 = UUID.randomUUID().toString();
        SyncRecord syncRecord1 = new SyncRecord();
        syncRecord1.setTimestamp(new Date());
        syncRecord1.setGuid(guid1);
        SyncItem item11 = new SyncItem();
        item11.setContent("<Person><Name>Some Person</Name></Person>");
        item11.setState(SyncItemState.NEW);
        item11.setKey(new SyncItemKey<String>(UUID.randomUUID().toString(),
                String.class));
        SyncItem item12 = new SyncItem();
        item12
                .setContent("<PersonAddress><Street>Some Street</Street></PersonAddress>");
        item12.setState(SyncItemState.UPDATED);
        item12.setKey(new SyncItemKey<String>(UUID.randomUUID().toString(),
                String.class));
        List<SyncItem> items1 = new ArrayList<SyncItem>();
        items1.add(item11);
        items1.add(item12);
        syncRecord1.setItems(items1);

        // 'weird' end cases start here

        // no timestamp or items
        SyncRecord syncRecord2 = new SyncRecord();
        syncRecord2.setGuid(UUID.randomUUID().toString());

        // dump out the state
        Package pkg = new FilePackage();
        Record record = pkg.createRecordForWrite("SyncRecordTest");
        Item top = record.getRootItem();

        ((IItem) syncRecord1).save(record, top);
        ((IItem) syncRecord2).save(record, top);
        List<SyncRecord> originals = new ArrayList<SyncRecord>();
        originals.add(syncRecord1);
        originals.add(syncRecord2);    

        // now Test deserialize - THIS DOES NOT WORK YET
        String textDes = record.toStringAsDocumentFragement();

        Package pkgDes = new FilePackage();
        Record recordDes = pkgDes.createRecordFromString(textDes);
        Item topDes = recordDes.getRootItem();

        // get items list that holds serialized sync records
        List<Item> itemsDes = recordDes.getItems(topDes);

        assertTrue(itemsDes.size() == originals.size());

        SyncRecord syncRecordDesc = null;
        Iterator<Item> iterator = itemsDes.iterator();
        Iterator<SyncRecord> iteratorOrig = originals.iterator();
        while (iterator.hasNext()) {
            syncRecordDesc = new SyncRecord();
            syncRecordDesc.load(recordDes, iterator.next());
            assertEquals(syncRecordDesc, iteratorOrig.next());
        }

        return;
    }

    /**
     * Auto generated method comment
     * 
     * @throws Exception
     */
    @Test
    @NotTransactional
    public void shouldEquality() throws Exception {
        
        //setup instance 1
        String guid1 = UUID.randomUUID().toString();
        SyncRecord syncRecord1 = new SyncRecord();
        syncRecord1.setTimestamp(new Date());
        syncRecord1.setGuid(guid1);
        SyncItem item11 = new SyncItem();
        item11.setContent("<Person><Name>Some Person</Name></Person>");
        item11.setState(SyncItemState.NEW);
        item11.setKey(new SyncItemKey<String>(UUID.randomUUID().toString(),
                String.class));
        List<SyncItem> items1 = new ArrayList<SyncItem>();
        items1.add(item11);
        syncRecord1.setItems(items1);

        //setup instance 2
        SyncRecord syncRecord2 = new SyncRecord();
        syncRecord2.setTimestamp(syncRecord1.getTimestamp());
        syncRecord2.setGuid(syncRecord1.getGuid());
        SyncItem item21 = new SyncItem();
        item21.setContent("<Person><Name>Some Person</Name></Person>");
        item21.setState(SyncItemState.NEW);
        item21.setKey(item11.getKey());
        List<SyncItem> items2 = new ArrayList<SyncItem>();
        items2.add(item21);
        syncRecord2.setItems(items2);
       
        //assert now
        assertEquals(syncRecord1,syncRecord2);
        
        syncRecord1.setItems(null);
    	assertNotSame(syncRecord1,syncRecord2);
        
        //some variations
        Date date2 =  new Date();
        date2.setTime(syncRecord1.getTimestamp().getTime() + 1);
        syncRecord2.setTimestamp(date2);
        assertTrue(!syncRecord1.equals(syncRecord2));
        
        date2.setTime(syncRecord1.getTimestamp().getTime());
        assertTrue(syncRecord1.equals(syncRecord2));
        
        item21.setContent("<Person><Name>Some Person Name</Name></Person>");
        assertTrue(!syncRecord1.equals(syncRecord2));
    }
}
