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
		editor.setAsText("dc5c1fcc-0459-4201-bf70-0b90535ba362");
		Assert.assertNotNull(editor.getValue());
	}
}
