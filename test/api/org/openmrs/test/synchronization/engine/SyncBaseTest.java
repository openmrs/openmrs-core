package org.openmrs.test.synchronization.engine;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.FilePackage;
import org.openmrs.serialization.IItem;
import org.openmrs.serialization.Item;
import org.openmrs.serialization.Record;

import org.openmrs.synchronization.engine.*;
import org.openmrs.synchronization.server.RemoteServer;

/**
 *  to setup common routines and initialization for all sync tests.
 *
 */
public abstract class SyncBaseTest extends BaseContextSensitiveTest {

	
	protected final Log log = LogFactory.getLog(getClass());
	public DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
	
	public abstract String getInitialDataset();
	
	public String getParentSetupDataset() {
		return "org/openmrs/test/synchronization/engine/include/SyncRemoteChildServer.xml";
	}

	protected void setupSyncTestChild() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		executeDataSet(getInitialDataset());
	}
	
	protected void setupSyncTestParent() throws Exception {
		deleteAllData();
		initializeInMemoryDatabase();
		executeDataSet(getInitialDataset());
		executeDataSet(getParentSetupDataset());
	}
		
	public void runSyncTest(SyncTestHelper testMethods) throws Exception {
		deleteAllData();
		Context.openSession();
		executeDataSet("org/openmrs/test/synchronization/engine/include/SyncCreateTest.xml");
		authenticate();

		log.info("\n************************************* Running On Child *************************************");
		testMethods.runOnChild();
		
		this.transactionManager.commit(this.transactionStatus);
		Context.closeSession();
		Context.openSession();

		List<SyncRecord> syncRecords = Context.getSynchronizationService().getSyncRecords();
		if (syncRecords == null || syncRecords.size() == 0)
			assertFalse("No changes found (i.e. sync records size is 0)", true);

		log.info("\n************************************* Deleting Data *************************************");
		deleteAllData();
		executeDataSet("org/openmrs/test/synchronization/engine/include/SyncCreateTest.xml");
		executeDataSet("org/openmrs/test/synchronization/engine/include/SyncRemoteChildServer.xml");

		log.info("\n************************************* Sync Record(s) to Process *************************************");
        FilePackage pkg = new FilePackage();
        Record record = pkg.createRecordForWrite("SyncTest");
        Item top = record.getRootItem();
		for (SyncRecord syncRecord : syncRecords) {			
            ((IItem) syncRecord).save(record, top);
		}
        try {
            log.info("Sync record:\n" + record.toString());
        } catch (Exception e) {
            e.printStackTrace(System.out);
            fail("Serialization failed with an exception: " + e.getMessage());
        }		

		log.info("\n************************************* Processing Sync Record(s) *************************************");
		RemoteServer origin = Context.getSynchronizationService().getRemoteServer(1);
		for (SyncRecord syncRecord : syncRecords) {			
			Context.getSynchronizationIngestService().processSyncRecord(syncRecord, origin);
		}
		
        log.info("\n************************************* Running on Parent *************************************");
		testMethods.runOnParent();
		Context.closeSession();
	}
}

	
