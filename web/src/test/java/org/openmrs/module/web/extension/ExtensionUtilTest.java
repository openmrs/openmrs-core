package org.openmrs.module.web.extension;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Form;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 * Tests {@link ExtensionUtil}.
 */
public class ExtensionUtilTest extends BaseModuleContextSensitiveTest {
	
	/**
	 * @see ExtensionUtil#getFormsModulesCanAddEncounterToVisit()
	 * @verifies return empty set if no implementations found
	 */
	@Test
	public void getFormsModulesCanAddEncounterToVisit_shouldReturnEmptySetIfNoImplementationsFound() throws Exception {
		ExtensionUtil extensionUtil = new ExtensionUtil();
		Set<Form> forms = extensionUtil.getFormsModulesCanAddEncounterToVisit();
		Assert.assertNotNull(forms);
		Assert.assertEquals(0, forms.size());
	}
}
