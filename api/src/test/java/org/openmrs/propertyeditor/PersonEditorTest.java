package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class PersonEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see PersonEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		PersonEditor editor = new PersonEditor();
		editor.setAsText("2");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see PersonEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		PersonEditor editor = new PersonEditor();
		editor.setAsText("da7f524f-27ce-4bb2-86d6-6d1d05312bd5");
		Assert.assertNotNull(editor.getValue());
	}
}
