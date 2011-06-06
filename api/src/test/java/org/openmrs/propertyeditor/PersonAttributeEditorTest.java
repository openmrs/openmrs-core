package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class PersonAttributeEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see PersonAttributeEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		PersonAttributeEditor editor = new PersonAttributeEditor();
		editor.setAsText("1");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see PersonAttributeEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		PersonAttributeEditor editor = new PersonAttributeEditor();
		editor.setAsText("0768f3da-b692-44b7-a33f-abf2c450474e");
		Assert.assertNotNull(editor.getValue());
	}
}
