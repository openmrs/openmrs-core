package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class EncounterTypeEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see EncounterTypeEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		EncounterTypeEditor editor = new EncounterTypeEditor();
		editor.setAsText("1");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see EncounterTypeEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		EncounterTypeEditor editor = new EncounterTypeEditor();
		editor.setAsText("61ae96f4-6afe-4351-b6f8-cd4fc383cce1");
		Assert.assertNotNull(editor.getValue());
	}
}
