package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class VisitEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see VisitEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		VisitEditor editor = new VisitEditor();
		editor.setAsText("1");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see VisitEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		VisitEditor editor = new VisitEditor();
		editor.setAsText("1e5d5d48-6b78-11e0-93c3-18a905e044dc");
		Assert.assertNotNull(editor.getValue());
	}
}
