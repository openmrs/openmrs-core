/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class OpenmrsClassLoaderTest {

	public static class ClassWithStaticField {

		public static Object value;
	}

	@Test
	public void nullStaticFieldsShouldSetStaticFieldsToNull() {
		ClassWithStaticField.value = new Object();

		OpenmrsClassLoader.nullStaticFields(ClassWithStaticField.class);

		assertNull(ClassWithStaticField.value);
	}

	@Test
	public void nullStaticFieldsShouldNotFailForAClassThatCannotBeLinked(@TempDir Path classesDirectory) throws IOException {
		try (URLClassLoader classLoader = compileClassReferringToAMissingType(classesDirectory)) {
			Class<?> unlinkable = assertDoesNotThrow(
			    () -> Class.forName("org.openmrs.test.missing.HolderOfMissingType", false, classLoader));

			// the class loads, but reflecting over it throws an error rather than an exception
			assertThrows(NoClassDefFoundError.class, unlinkable::getDeclaredFields);

			assertDoesNotThrow(() -> OpenmrsClassLoader.nullStaticFields(unlinkable));
		}
	}

	/**
	 * Builds a class that refers to a type which is not on the class path at runtime, the way a class
	 * in a module jar refers to a type that only an optional dependency of that jar would have
	 * provided.
	 *
	 * @return a class loader that can load the class, but not the type it refers to
	 */
	private URLClassLoader compileClassReferringToAMissingType(Path classesDirectory) throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		assumeTrue(compiler != null, "no java compiler available to build the test fixture");

		Path source = classesDirectory.resolve("HolderOfMissingType.java");
		Files.write(source,
		    ("package org.openmrs.test.missing;\n" + "public class HolderOfMissingType {\n"
		            + "	public static MissingType value;\n" + "}\n" + "class MissingType {\n" + "}\n")
		            .getBytes(StandardCharsets.UTF_8));

		assertEquals(0, compiler.run(null, null, null, "-d", classesDirectory.toString(), source.toString()),
		    "failed to compile the test fixture");

		Path missingType = classesDirectory.resolve("org/openmrs/test/missing/MissingType.class");
		assertTrue(Files.deleteIfExists(missingType), "test fixture was not compiled as expected");

		return new URLClassLoader(new URL[] { classesDirectory.toUri().toURL() }, getClass().getClassLoader());
	}
}
