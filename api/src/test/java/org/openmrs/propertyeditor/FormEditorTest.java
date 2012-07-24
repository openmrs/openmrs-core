package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class FormEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see FormEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		FormEditor editor = new FormEditor();
		editor.setAsText("1");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see FormEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		FormEditor editor = new FormEditor();
		editor.setAsText("d9218f76-6c39-45f4-8efa-4c5c6c199f50");
		Assert.assertNotNull(editor.getValue());
	}
}
