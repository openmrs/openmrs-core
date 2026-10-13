/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate.type;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.hibernate.HibernateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.Obs;
import org.openmrs.util.OpenmrsClassLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StringEnumTypeTest {

	private static final String MODULE_ENUM = "org.openmrs.module.stringenumtypetest.ProcessorMode";

	@TempDir
	Path tempDir;

	@Test
	public void setParameterValues_shouldResolveCoreEnumClass() {
		StringEnumType type = newType(Obs.Status.class.getName());

		assertEquals(Obs.Status.class, type.returnedClass());
	}

	@Test
	public void setParameterValues_shouldResolveEnumClassOnlyVisibleToOpenmrsClassLoader() throws Exception {
		OpenmrsClassLoader original = OpenmrsClassLoader.getInstance();
		try (URLClassLoader moduleLoader = compileModuleEnum()) {
			assertThrows(ClassNotFoundException.class, () -> Class.forName(MODULE_ENUM));
			// the constructor registers the new loader as the OpenmrsClassLoader instance
			new OpenmrsClassLoader(moduleLoader);

			StringEnumType type = newType(MODULE_ENUM);

			assertEquals(moduleLoader.loadClass(MODULE_ENUM), type.returnedClass());
			assertEquals("BACKGROUND", type.fromStringValue("BACKGROUND").name());
		} finally {
			restoreOpenmrsClassLoader(original);
		}
	}

	@Test
	public void setParameterValues_shouldFailForUnknownEnumClass() {
		assertThrows(HibernateException.class, () -> newType("org.openmrs.DoesNotExist"));
	}

	private StringEnumType newType(String enumClassName) {
		Properties parameters = new Properties();
		parameters.setProperty("enumClass", enumClassName);
		StringEnumType type = new StringEnumType();
		type.setParameterValues(parameters);
		return type;
	}

	private static void restoreOpenmrsClassLoader(OpenmrsClassLoader original) throws Exception {
		Class<?> holder = Class.forName(OpenmrsClassLoader.class.getName() + "$OpenmrsClassLoaderHolder");
		Field instance = holder.getDeclaredField("INSTANCE");
		instance.setAccessible(true);
		instance.set(null, original);
	}

	private URLClassLoader compileModuleEnum() throws Exception {
		Path source = tempDir.resolve("src/org/openmrs/module/stringenumtypetest/ProcessorMode.java");
		Files.createDirectories(source.getParent());
		Files.writeString(source,
		    "package org.openmrs.module.stringenumtypetest; public enum ProcessorMode { AUTOMATIC, BACKGROUND }");
		Path classes = Files.createDirectories(tempDir.resolve("classes"));
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		assertEquals(0, compiler.run(null, null, null, "-d", classes.toString(), source.toString()));
		return new URLClassLoader(new URL[] { classes.toUri().toURL() }, getClass().getClassLoader());
	}
}
