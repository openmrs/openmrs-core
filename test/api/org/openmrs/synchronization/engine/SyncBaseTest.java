package org.openmrs.synchronization.engine;


import org.openmrs.BaseContextSensitiveTest;

/**
 * Placeholder to setup common routines and initialization for all sync tests.
 *
 */
public class SyncBaseTest extends BaseContextSensitiveTest {

    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        authenticate();

    }
}
