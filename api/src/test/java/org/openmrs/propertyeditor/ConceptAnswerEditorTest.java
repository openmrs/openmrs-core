package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class ConceptAnswerEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ConceptAnswerEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		ConceptAnswerEditor editor = new ConceptAnswerEditor();
		editor.setAsText("1");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see ConceptAnswerEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		ConceptAnswerEditor editor = new ConceptAnswerEditor();
		editor.setAsText("b1230431-2fe5-49fc-b535-ae42bc849747");
		Assert.assertNotNull(editor.getValue());
	}
}
