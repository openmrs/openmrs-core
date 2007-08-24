package org.openmrs.synchronization.engine;

import java.util.Properties;

import org.openmrs.BaseTest;
import org.openmrs.api.context.Context;
import org.openmrs.api.SynchronizationService;

public class SyncBaseTest extends BaseTest {

    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        authenticate();

    }

    /** Pull credentials from runtime file, if available, if not default to prompt.
     * 
     * @see org.openmrs.BaseTest#getUsernameAndPassword(java.lang.String)
     */
    @Override
    public synchronized String[] getUsernameAndPassword(String message) {

        Properties props = null;
        String username = null;
        String userpwd = null;
        
        try {            
        props = this.getRuntimeProperties();
        username = (!props.containsKey("junit.username")) ?  null : props.getProperty("junit.username");
        userpwd = (!props.containsKey("junit.userpwd")) ?  null : props.getProperty("junit.userpwd");
        }
        catch(Exception e) {
            // anything happens, default to calling supper
        }
        
        if (username == null || userpwd == null)
            return super.getUsernameAndPassword(message);
        else
            return (new String[] {username,userpwd});
    }
}
