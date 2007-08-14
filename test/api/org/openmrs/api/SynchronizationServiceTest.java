package org.openmrs.api;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.openmrs.BaseTest;
import org.openmrs.api.context.Context;
import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncItemKey;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.engine.SyncRecordState;
import org.openmrs.synchronization.engine.SyncItem.SyncItemState;

public class SynchronizationServiceTest extends BaseTest {

    protected SynchronizationService syncService = Context.getSynchronizationService();

    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        authenticate();
    }

    public void testSynchronizationService() throws Exception {
        assertTrue(Context.isAuthenticated());

        int numberOfSyncRecords = syncService.getSyncRecords().size();

        Calendar calendar = new GregorianCalendar();
        calendar.clear();
        calendar.set(2007, Calendar.JANUARY, 1, 12, 0, 0);
        String guid = UUID.randomUUID().toString();

        SyncRecord syncRecord = new SyncRecord();
        syncRecord.setTimestamp(calendar.getTime());
        syncRecord.setGuid(guid);

        // Create a couple of SyncItems
        SyncItem item1 = new SyncItem();
        item1.setContent("<Person><Name>Some Person</Name></Person>");
        item1.setState(SyncItemState.NEW);
        item1.setKey(new SyncItemKey<String>(UUID.randomUUID().toString()));

        SyncItem item2 = new SyncItem();
        item2.setContent("<PersonAddress><Street>Some Street</Street></PersonAddress>");
        item2.setState(SyncItemState.UPDATED);
        item2.setKey(new SyncItemKey<String>(UUID.randomUUID().toString()));

        // Add them to the list
        List<SyncItem> items = new ArrayList<SyncItem>();
        items.add(item1);
        items.add(item2);

        // Add them to the record
        syncRecord.setItems(items);

        // Store the record
        syncService.createSyncRecord(syncRecord);

        // Make sure the lists size increased:
        assertEquals(
                "The number of SyncRecords in the list did not increase after create",
                syncService.getSyncRecords().size(), (numberOfSyncRecords + 1));

        // Retrieve it back
        SyncRecord retrievedSyncRecord = syncService.getSyncRecord(guid);

        assertEquals("GUID does not match", syncRecord.getGuid(),
                retrievedSyncRecord.getGuid());
        assertEquals("RetryCount does not match", syncRecord.getRetryCount(),
                retrievedSyncRecord.getRetryCount());
        assertEquals("State does not match", syncRecord.getState(),
                retrievedSyncRecord.getState());
        assertEquals("Timestamp does not match", syncRecord.getTimestamp(),
                retrievedSyncRecord.getTimestamp());
        assertEquals("Number of SyncItems in the list does not match",
                syncRecord.getItems().size(), retrievedSyncRecord.getItems()
                        .size());

        // Update it
        calendar.clear();
        calendar.set(2007, Calendar.JANUARY, 1, 12, 0, 1);
        retrievedSyncRecord.setState(SyncRecordState.ABORTED);
        retrievedSyncRecord.setRetryCount(1);
        retrievedSyncRecord.setTimestamp(calendar.getTime());

        syncService.updateSyncRecord(retrievedSyncRecord);

        // Retrieve it back
        SyncRecord updatedSyncRecord = syncService.getSyncRecord(guid);

        assertEquals("GUID of updated SyncRecord does not match",
                retrievedSyncRecord.getGuid(), updatedSyncRecord.getGuid());
        assertEquals("RetryCount of updated SyncRecord does not match",
                retrievedSyncRecord.getRetryCount(), updatedSyncRecord
                        .getRetryCount());
        assertEquals("State of updated SyncRecord does not match",
                retrievedSyncRecord.getState(), updatedSyncRecord.getState());
        assertEquals("Timestamp of updated SyncRecord does not match",
                retrievedSyncRecord.getTimestamp(), updatedSyncRecord
                        .getTimestamp());
        assertEquals("Number of SyncItems in the list does not match",
                retrievedSyncRecord.getItems().size(), updatedSyncRecord
                        .getItems().size());

        // Retrieve all
        List<SyncRecord> records = syncService.getSyncRecords();
        assertEquals(records.size(), (numberOfSyncRecords + 1));

        // Retrieve all in a certain state
        records = syncService.getSyncRecords(SyncRecordState.ABORTED);
        assertNotNull(
                "No records returned from service, null, should be at least one.",
                records);
        Iterator<SyncRecord> iterator = records.iterator();
        while (iterator.hasNext()) {
            assertEquals(
                    "SyncRecordState did not match the expected state Aborted",
                    iterator.next().getState(), SyncRecordState.ABORTED);
        }

        // Clean up
        // FIXME: delete does NOT work correctly in the unit tests, but seems to
        // work in real use. Why?
        syncService.deleteSyncRecord(syncService.getSyncRecord(guid));

        // Check that it's gone
        assertNull(syncService.getSyncRecord(guid));
    }

    public void testSynchronizationGetFirstAndBetween() throws Exception {
        assertTrue(Context.isAuthenticated());

        Calendar calendar = new GregorianCalendar();

        // Create records
        calendar.clear();
        calendar.set(2007, Calendar.JANUARY, 1, 13, 0, 0);
        SyncRecord record0 = new SyncRecord();
        record0.setGuid(UUID.randomUUID().toString());
        record0.setRetryCount(0);
        record0.setState(SyncRecordState.SENT);
        record0.setTimestamp(calendar.getTime());

        calendar.clear();
        calendar.set(2007, Calendar.JANUARY, 1, 13, 0, 1);
        SyncRecord record1 = new SyncRecord();
        record1.setGuid(UUID.randomUUID().toString());
        record1.setRetryCount(1);
        record1.setState(SyncRecordState.NEW);
        record1.setTimestamp(calendar.getTime());

        calendar.clear();
        calendar.set(2007, Calendar.JANUARY, 1, 13, 0, 2);
        SyncRecord record2 = new SyncRecord();
        record2.setGuid(UUID.randomUUID().toString());
        record2.setRetryCount(2);
        record2.setState(SyncRecordState.PENDING_SEND);
        record2.setTimestamp(calendar.getTime());

        calendar.clear();
        calendar.set(2007, Calendar.JANUARY, 1, 13, 0, 3);
        SyncRecord record3 = new SyncRecord();
        record3.setGuid(UUID.randomUUID().toString());
        record3.setRetryCount(3);
        record3.setState(SyncRecordState.NEW);
        record3.setTimestamp(calendar.getTime());

        calendar.clear();
        calendar.set(2007, Calendar.JANUARY, 1, 13, 0, 4);
        SyncRecord record4 = new SyncRecord();
        record4.setGuid(UUID.randomUUID().toString());
        record4.setRetryCount(4);
        record4.setState(SyncRecordState.PENDING_SEND);
        record4.setTimestamp(calendar.getTime());

        calendar.clear();
        calendar.set(2007, Calendar.JANUARY, 1, 13, 0, 5);
        SyncRecord record5 = new SyncRecord();
        record5.setGuid(UUID.randomUUID().toString());
        record5.setRetryCount(5);
        record5.setState(SyncRecordState.COMMITTED);
        record5.setTimestamp(calendar.getTime());

        // Persist records
        syncService.createSyncRecord(record0);
        syncService.createSyncRecord(record1);
        syncService.createSyncRecord(record2);
        syncService.createSyncRecord(record3);
        syncService.createSyncRecord(record4);
        syncService.createSyncRecord(record5);

        // Retrieve records after record1.timestamp
        List<SyncRecord> recordsSinceTimestamp = syncService
                .getSyncRecordsSince(record1.getTimestamp());
        assertEquals("Number of records after timestamp didn't match",
                recordsSinceTimestamp.size(), 4);

        Iterator<SyncRecord> iterator = recordsSinceTimestamp.iterator();
        while (iterator.hasNext()) {
            assertTrue("Timestamp in list was not after specified timestamp",
                    iterator.next().getTimestamp()
                            .after(record1.getTimestamp()));
        }

        // Retrieve record between record1 and 4 (including 4)
        List<SyncRecord> recordsBetweenTimestamps = syncService
                .getSyncRecordsBetween(record1.getTimestamp(), record4
                        .getTimestamp());
        assertEquals("Number of records between timestamps didn't match",
                recordsBetweenTimestamps.size(), 3);

        iterator = recordsBetweenTimestamps.iterator();
        while (iterator.hasNext()) {
            SyncRecord r = iterator.next();
            assertTrue("Timestamp in list was not after specified timestamp", r
                    .getTimestamp().after(record1.getTimestamp()));
            assertTrue(
                    "Timestamp in list was not before or equal to specified timestamp",
                    (r.getTimestamp().before(record4.getTimestamp()))
                            || r.getTimestamp().equals(record4.getTimestamp()));
        }

        // Retrieve first record in queue - record1 is the earliest with state
        // new etc.
        SyncRecord firstRecord = syncService.getFirstSyncRecordInQueue();
        assertEquals(record1.getGuid(), firstRecord.getGuid());
        assertEquals(record1.getRetryCount(), firstRecord.getRetryCount());
        assertEquals(record1.getState(), firstRecord.getState());
        assertEquals(record1.getTimestamp(), firstRecord.getTimestamp());

        // Update record1's status to make getFirstSyncRecordInQueue to retrieve
        // the next:
        firstRecord.setState(SyncRecordState.SENT);
        syncService.updateSyncRecord(firstRecord);

        // Retrieve first record in queue again:
        firstRecord = syncService.getFirstSyncRecordInQueue();
        assertEquals(record2.getGuid(), firstRecord.getGuid());
        assertEquals(record2.getRetryCount(), firstRecord.getRetryCount());
        assertEquals(record2.getState(), firstRecord.getState());
        assertEquals(record2.getTimestamp(), firstRecord.getTimestamp());

        // Cleanup
        syncService.deleteSyncRecord(record0);
        syncService.deleteSyncRecord(record1);
        syncService.deleteSyncRecord(record2);
        syncService.deleteSyncRecord(record3);
        syncService.deleteSyncRecord(record4);
        syncService.deleteSyncRecord(record5);
    }
}
