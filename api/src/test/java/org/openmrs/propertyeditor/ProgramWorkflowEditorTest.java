package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class ProgramWorkflowEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ProgramWorkflowEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		ProgramWorkflowEditor editor = new ProgramWorkflowEditor();
		editor.setAsText("1");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see ProgramWorkflowEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		ProgramWorkflowEditor editor = new ProgramWorkflowEditor();
		editor.setAsText("84f0effa-dd73-46cb-b931-7cd6be6c5f81");
		Assert.assertNotNull(editor.getValue());
	}
}
