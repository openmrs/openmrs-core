package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class UserEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see UserEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		UserEditor editor = new UserEditor();
		editor.setAsText("501");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see UserEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		UserEditor editor = new UserEditor();
		editor.setAsText("c1d8f5c2-e131-11de-babe-001e378eb67e");
		Assert.assertNotNull(editor.getValue());
	}
}
