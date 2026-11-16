/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests methods on the PersonAttribute class
 */
public class PersonAttributeTest extends BaseContextSensitiveTest {

	@TempDir
	Path tempDir;

	/**
	 * @see PersonAttribute#toString()
	 */
	@Test
	public void toString_shouldReturnToStringOfHydratedValue() {
		// type = CIVIL STATUS, concept = MARRIED
		PersonAttributeType type = Context.getPersonService().getPersonAttributeType(8);
		PersonAttribute attr = new PersonAttribute(type, "6");
		assertEquals("MARRIED", attr.toString());
	}

	/**
	 * @see PersonAttribute#equalsContent(PersonAttribute)
	 */
	@Test
	public void equalsContent_shouldReturnTrueIfAttributeTypeValueAndVoidStatusAreTheSame() {
		PersonAttribute pa = new PersonAttribute(2); // a different personAttributeid than below
		pa.setAttributeType(new PersonAttributeType(1));
		pa.setValue("1");
		pa.setVoided(false);
		PersonAttribute other = new PersonAttribute(1); // a different personAttributeid than above
		pa.setAttributeType(new PersonAttributeType(1));
		pa.setValue("1");
		pa.setVoided(false);

		assertTrue(pa.equalsContent(other));
	}

	/**
	 * @see PersonAttribute#getHydratedObject()
	 */
	@Test
	public void getHydratedObject_shouldLoadClassInFormatProperty() {
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat("org.openmrs.Concept");

		PersonAttribute pa = new PersonAttribute(2);
		pa.setAttributeType(type);
		pa.setValue("5089");

		Concept concept = (Concept) pa.getHydratedObject();
		assertEquals(5089, concept.getConceptId().intValue());
	}

	/**
	 * @see PersonAttribute#getHydratedObject()
	 */
	@Test
	public void getHydratedObject_shouldLoadUserClassInFormatProperty() {
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat("org.openmrs.User");

		PersonAttribute pa = new PersonAttribute(2);

		pa.setAttributeType(type);
		pa.setValue("1");

		Object value = pa.getHydratedObject();
		assertTrue((value instanceof User), "should load user class in format property");
	}

	/**
	 * @see PersonAttribute#getHydratedObject()
	 */
	@Test
	public void getHydratedObject_shouldStillLoadClassInFormatPropertyIfNotAttributable() {
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat("java.lang.String");

		PersonAttribute pa = new PersonAttribute(2);

		pa.setAttributeType(type);
		pa.setValue("lalapalooza");

		String value = (String) pa.getHydratedObject();
		assertEquals("lalapalooza", value);
	}

	/**
	 * @see PersonAttribute#voidAttribute(String)
	 */
	@Test
	public void voidAttribute_shouldSetVoidedBitToTrue() {
		PersonAttribute pa = new PersonAttribute(2);
		pa.setVoided(false);
		pa.voidAttribute("Because");
		assertTrue(pa.getVoided());
	}

	/**
	 * @see PersonAttribute#getHydratedObject()
	 */
	@Test
	public void getHydratedObject_shouldLoadValueTypeInFormatPropertyUsingStringConstructor() {
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat("java.lang.Integer");

		PersonAttribute pa = new PersonAttribute(2);
		pa.setAttributeType(type);
		pa.setValue("42");

		assertEquals(42, pa.getHydratedObject());
	}

	/**
	 * @see PersonAttribute#getHydratedObject()
	 */
	@Test
	public void getHydratedObject_shouldReturnRawValueIfAttributableHasNoNoArgConstructor() {
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat(StringOnlyAttributable.class.getName());

		PersonAttribute pa = new PersonAttribute(2);
		pa.setAttributeType(type);
		pa.setValue("raw");

		assertEquals("raw", pa.getHydratedObject());
	}

	/**
	 * @see PersonAttribute#getHydratedObject()
	 */
	@Test
	public void getHydratedObject_shouldNotInstantiateClassThatIsNotPermitted() {
		File file = tempDir.resolve("should-not-exist.txt").toFile();
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat(FileOutputStream.class.getName());

		PersonAttribute pa = new PersonAttribute(2);
		pa.setAttributeType(type);
		pa.setValue(file.getAbsolutePath());

		pa.getHydratedObject();
		assertFalse(file.exists());
	}

	/**
	 * @see PersonAttribute#getHydratedObject()
	 */
	@Test
	public void getHydratedObject_shouldNotCallNoArgConstructorOfClassThatIsNotPermitted() {
		NoArgConstructorSpy.constructed = false;
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat(NoArgConstructorSpy.class.getName());

		PersonAttribute pa = new PersonAttribute(type, "value");

		assertEquals("value", pa.getHydratedObject());
		assertFalse(NoArgConstructorSpy.constructed);
	}

	/**
	 * @see PersonAttribute#getHydratedObject()
	 */
	@Test
	public void getHydratedObject_shouldReturnRawValueIfFormatIsNotPermitted() {
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat(FileOutputStream.class.getName());

		PersonAttribute pa = new PersonAttribute(2);
		pa.setAttributeType(type);
		pa.setValue("some value");

		assertEquals("some value", pa.getHydratedObject());
	}

	/**
	 * @see PersonAttribute#toString()
	 */
	@Test
	public void toString_shouldNotInstantiateClassThatIsNotPermitted() {
		File file = tempDir.resolve("should-not-exist.txt").toFile();
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat(FileOutputStream.class.getName());

		PersonAttribute pa = new PersonAttribute(2);
		pa.setAttributeType(type);
		pa.setValue(file.getAbsolutePath());

		pa.toString();
		assertFalse(file.exists());
	}

	/**
	 * @see PersonAttribute#isPermittedClass(Class)
	 */
	@Test
	public void isPermittedClass_shouldReturnTrueForAttributableClass() {
		assertTrue(PersonAttribute.isPermittedClass(Location.class));
	}

	/**
	 * @see PersonAttribute#isPermittedClass(Class)
	 */
	@Test
	public void isPermittedClass_shouldReturnTrueForPermittedValueType() {
		assertTrue(PersonAttribute.isPermittedClass(Boolean.class));
	}

	/**
	 * @see PersonAttribute#getHydratedObject()
	 */
	@Test
	public void getHydratedObject_shouldReturnRawValueForOpenmrsObjectFormat() {
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat("org.openmrs.Patient");

		PersonAttribute pa = new PersonAttribute(2);
		pa.setAttributeType(type);
		pa.setValue("2");

		assertEquals("2", pa.getHydratedObject());
	}

	/**
	 * @see PersonAttribute#getHydratedObject()
	 */
	@Test
	public void getHydratedObject_shouldReturnRawValueForCharacterFormat() {
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat("java.lang.Character");

		PersonAttribute pa = new PersonAttribute(2);
		pa.setAttributeType(type);
		pa.setValue("M");

		assertEquals("M", pa.getHydratedObject());
	}

	/**
	 * @see PersonAttribute#isPermittedClass(Class)
	 */
	@Test
	public void isPermittedClass_shouldReturnTrueForOpenmrsObjectClass() {
		assertTrue(PersonAttribute.isPermittedClass(Patient.class));
	}

	/**
	 * @see PersonAttribute#isPermittedClass(Class)
	 */
	@Test
	public void isPermittedClass_shouldReturnTrueForCharacter() {
		assertTrue(PersonAttribute.isPermittedClass(Character.class));
	}

	/**
	 * @see PersonAttribute#isPermittedClass(Class)
	 */
	@Test
	public void isPermittedClass_shouldReturnFalseForClassOutsidePermittedSet() {
		assertFalse(PersonAttribute.isPermittedClass(FileOutputStream.class));
	}

	public static class NoArgConstructorSpy {

		static boolean constructed = false;

		public NoArgConstructorSpy() {
			constructed = true;
		}
	}

	public static class StringOnlyAttributable implements Attributable<StringOnlyAttributable> {

		private final String value;

		public StringOnlyAttributable(String value) {
			this.value = value;
		}

		@Override
		public StringOnlyAttributable hydrate(String s) {
			return new StringOnlyAttributable("hydrated:" + s);
		}

		@Override
		public String serialize() {
			return value;
		}

		@Override
		public List<StringOnlyAttributable> getPossibleValues() {
			return Collections.emptyList();
		}

		@Override
		public List<StringOnlyAttributable> findPossibleValues(String searchText) {
			return Collections.emptyList();
		}

		@Override
		public String getDisplayString() {
			return value;
		}

		@Override
		public String toString() {
			return value;
		}
	}

}
