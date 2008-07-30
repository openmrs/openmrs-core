package org.openmrs.test.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.datasource.LogicDataSource;
import org.openmrs.logic.result.Result;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Tests the ObsDataSource functionality
 */
public class ObsDataSourceTest extends BaseContextSensitiveTest {

    private Log log = LogFactory.getLog(this.getClass());

    @Before
    public void runBeforeEachTest() throws Exception {
    	initializeInMemoryDatabase();
    	executeDataSet("org/openmrs/test/logic/include/ObsDataSourceTest.xml");
        authenticate();
    }

    /**
     * TODO change to use the in memory database
     */
    @Test
	public void shouldObsDataSource() {
        LogicDataSource lds = Context.getLogicService().getLogicDataSource("obs");
        Cohort patients = new Cohort();

        patients.addMember(2);
        patients.addMember(3);
        
        assertEquals(2, patients.getSize());
        LogicContext context = new LogicContext(patients);
        Map<Integer, Result> result = lds.read(context, patients, new LogicCriteria(
                "CD4 COUNT"));
        context = null;
        assertNotNull(result);
        assertEquals(2, result.size());

        for (Integer id : result.keySet()) {
            for (Result r : result.get(id)) {
                log.error("PatientID: " + id + ", CD4 COUNT: " + r.toNumber());
            }
        }
    }
}
