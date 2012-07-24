package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class PersonAttributeTypeEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see PersonAttributeTypeEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		PersonAttributeTypeEditor editor = new PersonAttributeTypeEditor();
		editor.setAsText("1");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see PersonAttributeTypeEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		PersonAttributeTypeEditor editor = new PersonAttributeTypeEditor();
		editor.setAsText("b3b6d540-a32e-44c7-91b3-292d97667518");
		Assert.assertNotNull(editor.getValue());
	}
}
