package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class VisitTypeEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see VisitTypeEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		VisitTypeEditor editor = new VisitTypeEditor();
		editor.setAsText("1");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see VisitTypeEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		VisitTypeEditor editor = new VisitTypeEditor();
		editor.setAsText("c0c579b0-8e59-401d-8a4a-976a0b183519");
		Assert.assertNotNull(editor.getValue());
	}
}
