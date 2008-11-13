package org.openmrs.synchronization.engine;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.FilePackage;
import org.openmrs.serialization.IItem;
import org.openmrs.serialization.Item;
import org.openmrs.serialization.Record;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.server.RemoteServer;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/**
 *  Sets up common routines and initialization for all sync tests. Note for all sync tests:
 *  MUST MARK AS NotTransctional so that Tx that is created in runOnChild() menthod is
 *  committed upon exit of that method. 
 *  
 *  Note: org.springframework.transaction.annotation.Propagation.REQUIRES_NEW doesn't help
 *  since on most EDBMS systems it doesn't do what spec says
 *
 */
public abstract class SyncBaseTest extends BaseContextSensitiveTest {

	
	protected final Log log = LogFactory.getLog(getClass());
	public DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
	
	public abstract String getInitialDataset();
	
	@Override
	public void baseSetupWithStandardDataAndAuthentication() throws Exception {
		// Do nothing
	}
	
	protected void setupSyncTestChild() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		executeDataSet(getInitialDataset());
	}
	
	@Transactional
	@Rollback(false)
	protected void runOnChild(SyncTestHelper testMethods) throws Exception {
		log.info("\n************************************* Running On Child *************************************");
		testMethods.runOnChild();		
	}

	@Transactional
	protected void runOnParent(SyncTestHelper testMethods) throws Exception {
        //now run parent
		log.info("\n************************************* Running on Parent *************************************");		
		testMethods.runOnParent();		
	}
	
	/**
	 * Sets up initial data set before set of instructions simulating child changes is executed.
	 * 
	 * @see #runOnChild(SyncTestHelper)
	 * @see #runSyncTest(SyncTestHelper)
	 * 
	 * @throws Exception
	 */
	@Transactional
	@Rollback(false)
	protected void beforeRunOnChild() throws Exception {
		Context.openSession();
		deleteAllData();
		executeDataSet("org/openmrs/synchronization/engine/include/SyncCreateTest.xml");
		authenticate();		
	}
	
	@Transactional
	@Rollback(false)
	protected void applySyncChanges() throws Exception {
		
		//get sync records created by child
		List<SyncRecord> syncRecords = Context.getSynchronizationService().getSyncRecords();
		if (syncRecords == null || syncRecords.size() == 0) {
			assertFalse("No changes found (i.e. sync records size is 0)", true);
		}
		
		//now reload db from scratch
		log.info("\n************************************* Reload Database *************************************");
		deleteAllData();
		executeDataSet("org/openmrs/synchronization/engine/include/SyncCreateTest.xml");
		executeDataSet("org/openmrs/synchronization/engine/include/SyncRemoteChildServer.xml");
		
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
		
		return;
	}

	/**
	 * Executes the sync test workflow:
	 * <br/>1. prepopulate DB
	 * <br/>2. Execute set of instructions simulating sync child
	 * <br/>3. Fetch sync records, re-initialize DB for parent and then apply the sync records
	 * <br/>4. Execute set of instructions  simulating sync parent; typically just asserts to ensure child changes
	 * came accross.
	 * 
	 *<br/>Note: The non-transactional vs. transactional behavior of helper methods: each step must be in its own Tx since sync flushes
	 * its sync record at Tx boundry. Consequently it is required for the overall test to run as non-transactional
	 * and each individual step to be transactional; as stated in class comments, true nested transactions are RDMS fantasy,
	 * it mostly doesn't exist.
	 * 
	 * @param testMethods helper object holding methods for child and parent execution
	 * @throws Exception
	 */
	@NotTransactional
	public void runSyncTest(SyncTestHelper testMethods) throws Exception {

		this.beforeRunOnChild();
		
		this.runOnChild(testMethods);
		
		this.applySyncChanges();
		
		this.runOnParent(testMethods);
	}	
}

	
