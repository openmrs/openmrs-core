package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class ConceptNameEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ConceptNameEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		ConceptNameEditor editor = new ConceptNameEditor();
		editor.setAsText("1439");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see ConceptNameEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		ConceptNameEditor editor = new ConceptNameEditor();
		editor.setAsText("9bc5693a-f558-40c9-8177-145a4b119ca7");
		Assert.assertNotNull(editor.getValue());
	}
}
