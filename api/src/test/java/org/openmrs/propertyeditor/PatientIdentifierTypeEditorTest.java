package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class PatientIdentifierTypeEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see PatientIdentifierTypeEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		PatientIdentifierTypeEditor editor = new PatientIdentifierTypeEditor();
		editor.setAsText("1");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see PatientIdentifierTypeEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		PatientIdentifierTypeEditor editor = new PatientIdentifierTypeEditor();
		editor.setAsText("1a339fe9-38bc-4ab3-b180-320988c0b968");
		Assert.assertNotNull(editor.getValue());
	}
}
