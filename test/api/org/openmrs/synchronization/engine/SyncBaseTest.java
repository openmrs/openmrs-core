package org.openmrs.synchronization.engine;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.openmrs.BaseContextSensitiveTest;
import org.openmrs.api.context.Context;
import org.openmrs.synchronization.server.RemoteServer;

/**
 * Placeholder to setup common routines and initialization for all sync tests.
 *
 */
public abstract class SyncBaseTest extends BaseContextSensitiveTest {

	public DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
	
	public abstract String getInitialDataset();
	
	public String getParentSetupDataset() {
		return "org/openmrs/synchronization/engine/include/SyncRemoteChildServer.xml";
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
		Context.openSession();
		
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/synchronization/engine/include/SyncCreateTest.xml");
		authenticate();

		testMethods.runOnChild();
		
		this.transactionManager.commit(this.transactionStatus);
		Context.closeSession();
		Context.openSession();

		List<SyncRecord> syncRecords = Context.getSynchronizationService().getSyncRecords();
		if (syncRecords == null || syncRecords.size() == 0)
			assertFalse("No changes found (i.e. sync records size is 0)", true);
		
		deleteAllData();
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/synchronization/engine/include/SyncCreateTest.xml");
		executeDataSet("org/openmrs/synchronization/engine/include/SyncRemoteChildServer.xml");
		RemoteServer origin = Context.getSynchronizationService().getRemoteServer(1);
		for (SyncRecord syncRecord : syncRecords) {
			Context.getSynchronizationIngestService().processSyncRecord(syncRecord, origin);
		}
		
		testMethods.runOnParent();
		Context.closeSession();
	}

}
