package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class ProgramEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ProgramEditor#setAsText(String)
	 * @verifies set using concept id
	 */
	@Test
	public void setAsText_shouldSetUsingConceptId() throws Exception {
		ProgramEditor editor = new ProgramEditor();
		editor.setAsText("concept.9");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see ProgramEditor#setAsText(String)
	 * @verifies set using concept uuid
	 */
	@Test
	public void setAsText_shouldSetUsingConceptUuid() throws Exception {
		ProgramEditor editor = new ProgramEditor();
		editor.setAsText("concept.0a9afe04-088b-44ca-9291-0a8c3b5c96fa");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see ProgramEditor#setAsText(String)
	 * @verifies set using program id
	 */
	@Test
	public void setAsText_shouldSetUsingProgramId() throws Exception {
		ProgramEditor editor = new ProgramEditor();
		editor.setAsText("1");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see ProgramEditor#setAsText(String)
	 * @verifies set using program uuid
	 */
	@Test
	public void setAsText_shouldSetUsingProgramUuid() throws Exception {
		ProgramEditor editor = new ProgramEditor();
		editor.setAsText("da4a0391-ba62-4fad-ad66-1e3722d16380");
		Assert.assertNotNull(editor.getValue());
	}
}
