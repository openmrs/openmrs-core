package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class ConceptSourceEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ConceptSourceEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		ConceptSourceEditor editor = new ConceptSourceEditor();
		editor.setAsText("3");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see ConceptSourceEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		ConceptSourceEditor editor = new ConceptSourceEditor();
		editor.setAsText("75f5b378-5065-11de-80cb-001e378eb67e");
		Assert.assertNotNull(editor.getValue());
	}
}
