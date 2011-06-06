package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class ConceptDatatypeEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ConceptDatatypeEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		ConceptDatatypeEditor editor = new ConceptDatatypeEditor();
		editor.setAsText("1");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see ConceptDatatypeEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		ConceptDatatypeEditor editor = new ConceptDatatypeEditor();
		editor.setAsText("8d4a4488-c2cc-11de-8d13-0010c6dffd0f");
		Assert.assertNotNull(editor.getValue());
	}
}
