package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class ProviderEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ProviderEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		ProviderEditor editor = new ProviderEditor();
		editor.setAsText("1");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see ProviderEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		ProviderEditor editor = new ProviderEditor();
		editor.setAsText("c2299800-cca9-11e0-9572-0800200c9a66");
		Assert.assertNotNull(editor.getValue());
	}
}
