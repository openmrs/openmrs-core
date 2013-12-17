package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class OrderTypeEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see OrderTypeEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		OrderTypeEditor editor = new OrderTypeEditor();
		editor.setAsText("1");
		
		//TODO commented out because of this commit:
		//https://github.com/openmrs/openmrs-core/commit/6360b0e78ee98f75eef10bf37d7cbda2e67d5ce9
		//Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see OrderTypeEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		OrderTypeEditor editor = new OrderTypeEditor();
		editor.setAsText("84ce45a8-5e7c-48f7-a581-ca1d17d63a62");
		
		//TODO commented out because of this commit:
		//https://github.com/openmrs/openmrs-core/commit/6360b0e78ee98f75eef10bf37d7cbda2e67d5ce9
		//Assert.assertNotNull(editor.getValue());
	}
}
