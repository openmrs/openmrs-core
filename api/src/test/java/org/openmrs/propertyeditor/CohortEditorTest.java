package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class CohortEditorTest extends BaseContextSensitiveTest {
	
	protected static final String COHORT_XML = "org/openmrs/api/include/CohortServiceTest-cohort.xml";
	
	@Before
	public void prepareData() throws Exception {
		executeDataSet(COHORT_XML);
	}
	
	/**
	 * @see CohortEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		CohortEditor editor = new CohortEditor();
		editor.setAsText("1");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see CohortEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		CohortEditor editor = new CohortEditor();
		editor.setAsText("h9a9m0i6-15e6-467c-9d4b-mbi7teu9lf0f");
		Assert.assertNotNull(editor.getValue());
	}
}
