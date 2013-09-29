package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class LocationEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see LocationEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		LocationEditor editor = new LocationEditor();
		editor.setAsText("1");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see LocationEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		LocationEditor editor = new LocationEditor();
		editor.setAsText("8d6c993e-c2cc-11de-8d13-0010c6dffd0f");
		Assert.assertNotNull(editor.getValue());
	}
}
