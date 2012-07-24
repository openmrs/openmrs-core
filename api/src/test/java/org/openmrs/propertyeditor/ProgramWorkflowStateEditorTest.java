package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class ProgramWorkflowStateEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ProgramWorkflowStateEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		ProgramWorkflowStateEditor editor = new ProgramWorkflowStateEditor();
		editor.setAsText("1");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see ProgramWorkflowStateEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		ProgramWorkflowStateEditor editor = new ProgramWorkflowStateEditor();
		editor.setAsText("92584cdc-6a20-4c84-a659-e035e45d36b0");
		Assert.assertNotNull(editor.getValue());
	}
}
