package org.openmrs.synchronization.engine;


import org.openmrs.BaseTest;
import org.openmrs.api.context.Context;
import org.openmrs.api.SynchronizationService;


public class SyncBaseTest extends BaseTest {

    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        authenticate();
        
    }

    //uncomment and put in your test user credentials here
    @Override
    public synchronized String[] getUsernameAndPassword(String message) {
        return (new String[] {"admin","Nos1212"});
    }
    
}
