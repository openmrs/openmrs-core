package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class RoleEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see RoleEditor#setAsText(String)
	 * @verifies set using name
	 */
	@Test
	public void setAsText_shouldSetUsingName() throws Exception {
		RoleEditor editor = new RoleEditor();
		editor.setAsText("Provider");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see RoleEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		RoleEditor editor = new RoleEditor();
		editor.setAsText("3480cb6d-c291-46c8-8d3a-96dc33d199fb");
		Assert.assertNotNull(editor.getValue());
	}
}
