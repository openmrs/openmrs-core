package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class ConceptClassEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ConceptClassEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		ConceptClassEditor editor = new ConceptClassEditor();
		editor.setAsText("1");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see ConceptClassEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		ConceptClassEditor editor = new ConceptClassEditor();
		editor.setAsText("97097dd9-b092-4b68-a2dc-e5e5be961d42");
		Assert.assertNotNull(editor.getValue());
	}
}
