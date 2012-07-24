package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class ConceptNumericEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ConceptNumericEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		ConceptNumericEditor editor = new ConceptNumericEditor();
		editor.setAsText("5089");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see ConceptNumericEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		ConceptNumericEditor editor = new ConceptNumericEditor();
		editor.setAsText("a09ab2c5-878e-4905-b25d-5784167d0216");
		Assert.assertNotNull(editor.getValue());
	}
}
