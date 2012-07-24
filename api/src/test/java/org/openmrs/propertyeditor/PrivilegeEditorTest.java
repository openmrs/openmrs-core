package org.openmrs.propertyeditor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class PrivilegeEditorTest extends BaseContextSensitiveTest {
	
	protected static final String XML_FILENAME = "org/openmrs/api/include/UserServiceTest.xml";
	
	@Before
	public void prepareData() throws Exception {
		executeDataSet(XML_FILENAME);
	}
	
	/**
	 * @see PrivilegeEditor#setAsText(String)
	 * @verifies set using name
	 */
	@Test
	public void setAsText_shouldSetUsingName() throws Exception {
		PrivilegeEditor editor = new PrivilegeEditor();
		editor.setAsText("Some Privilege");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see PrivilegeEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		PrivilegeEditor editor = new PrivilegeEditor();
		editor.setAsText("d979d066-15e6-467c-9d4b-cb575ef97f0f");
		Assert.assertNotNull(editor.getValue());
	}
}
