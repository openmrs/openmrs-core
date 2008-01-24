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

import java.util.Date;
import java.util.List;

import org.openmrs.BaseContextSensitiveTest;
import org.openmrs.api.SynchronizationService;
import org.openmrs.api.context.Context;

/**
 * TODO: describe test file
 */
public class SyncStrategyFileTest extends BaseContextSensitiveTest {

    protected SyncSource source = null;

    protected SynchronizationService service = null;

    protected SyncStrategyFile strategy = null;

    protected List<SyncRecord> changed = null;

    public void testCreateTx() throws Exception {
    	initializeInMemoryDatabase();
        authenticate();

        // setup instances
        source = new SyncSourceJournal();
        service = Context.getSynchronizationService();
        strategy = new SyncStrategyFile();
        // Create initial set of records - use SyncSourceJournalTest helper
        changed = SyncSourceJournalTest.createRecords(service);
        this.transactionManager.commit(this.transactionStatus);
        //set last local 1 sec before the 1st test record
        Date lastLocal = new Date();
        lastLocal.setTime(changed.iterator().next().getTimestamp().getTime() - 1000);        
        source.setLastSyncLocal(new SyncPoint<Date>(lastLocal));

        // test strategy - create a test transmission
        SyncTransmission tx = strategy.createSyncTransmission(source);
        System.out.println("*** Created sync Transmission file: "
                + tx.getFileName());

        //read it back from file: TODO - fix this
        SyncTransmission txFromFile = new SyncTransmission();
        //txFromFile.ReadFromFile(tx.getFileName());
        
        //assert equality
        assertEquals(tx, txFromFile);

        // Cleanup the journal
        SyncSourceJournalTest.cleanupRecords(service, changed);
        
        return;
    }

}
