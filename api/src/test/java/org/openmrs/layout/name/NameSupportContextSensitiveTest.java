package org.openmrs.layout.name;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NameSupportContextSensitiveTest extends BaseContextSensitiveTest {

	@Test
	public void getDefaultLayoutFormat_shouldReadFormatAlreadyInDatabaseOnInit() throws Exception {
		
		executeDataSet("org/openmrs/api/include/NameSupportTest-format.xml");
		
		NameSupport nameSupport = NameSupport.getInstance();
		nameSupport.setDefaultLayoutFormat("short"); // mirrors legacyui's wiring

		Field initializedField = NameSupport.class.getDeclaredField("initialized");
		initializedField.setAccessible(true);
		Field layoutFormatField = NameSupport.class.getDeclaredField("layoutFormat");
		layoutFormatField.setAccessible(true);

		boolean previousInitialized = initializedField.getBoolean(nameSupport);
		Object previousLayoutFormat = layoutFormatField.get(nameSupport);

		try {
			// Simulate a fresh boot: clear the cache/init flag on the existing singleton 
			initializedField.setBoolean(nameSupport, false);
			layoutFormatField.set(nameSupport, null);

			String format = NameSupport.getInstance().getDefaultLayoutFormat();

			assertEquals("long", format);
		} finally {
			initializedField.setBoolean(nameSupport, previousInitialized);
			layoutFormatField.set(nameSupport, previousLayoutFormat);
		}
	}
}
