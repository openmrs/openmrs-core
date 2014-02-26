package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class ConceptEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ConceptEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		ConceptEditor editor = new ConceptEditor();
		editor.setAsText("3");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see ConceptEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		ConceptEditor editor = new ConceptEditor();
		editor.setAsText("0cbe2ed3-cd5f-4f46-9459-26127c9265ab");
		Assert.assertNotNull(editor.getValue());
	}
	
}
