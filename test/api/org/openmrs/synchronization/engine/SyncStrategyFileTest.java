package org.openmrs.synchronization.engine;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.api.SynchronizationService;
import org.openmrs.synchronization.engine.SyncStrategyFile;

import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncItemKey;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.engine.SyncRecordState;
import org.openmrs.synchronization.engine.SyncItem.SyncItemState;

public class SyncStrategyFileTest extends SyncBaseTest {

    protected SyncSource source = null;

    protected SynchronizationService service = null;

    protected SyncStrategyFile strategy = null;

    protected List<SyncRecord> changed = null;

    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        authenticate();
    }

    public void testCreateTx() throws Exception {
        assertTrue(Context.isAuthenticated());

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

        // TODO - assert contents of the file

        // Cleanup the journal
        SyncSourceJournalTest.cleanupRecords(service, changed);
        
        return;
    }

}
