package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class OrderEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see OrderEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		OrderEditor editor = new OrderEditor();
		editor.setAsText("2");
		
		//TODO commented out because of this commit:
		//https://github.com/openmrs/openmrs-core/commit/6360b0e78ee98f75eef10bf37d7cbda2e67d5ce9
		//Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see OrderEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		OrderEditor editor = new OrderEditor();
		editor.setAsText("dfca4077-493c-496b-8312-856ee5d1cc26");
		
		//TODO commented out because of this commit:
		//https://github.com/openmrs/openmrs-core/commit/6360b0e78ee98f75eef10bf37d7cbda2e67d5ce9
		//Assert.assertNotNull(editor.getValue());
	}
}
