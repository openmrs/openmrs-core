/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.util;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ExceptionUtilTest {
	
	/**
	 * @see ExceptionUtil#rethrowIfCause(Throwable,Class)
	 * @verifies allow an intermediate exception to be rethrown
	 */
	@Test
	public void rethrowIfCause_shouldAllowAnIntermediateExceptionToBeRethrown() throws Exception {
		try {
			@SuppressWarnings("unchecked")
			List<Class<? extends RuntimeException>> chain = Arrays.asList(NullPointerException.class,
			    IllegalArgumentException.class, IllegalStateException.class);
			throwExceptionChain(chain);
			
		}
		catch (Exception ex) {
			int numFound = 0;
			
			// Should be able to find the innermost NPE
			Exception innermost = null;
			try {
				ExceptionUtil.rethrowIfCause(ex, NullPointerException.class);
			}
			catch (Exception cause) {
				Assert.assertNull(cause.getCause());
				innermost = cause;
				++numFound;
			}
			
			// Should be able to find the middle IllegalArgumentException
			try {
				ExceptionUtil.rethrowIfCause(ex, IllegalArgumentException.class);
			}
			catch (Exception middle) {
				Assert.assertEquals(innermost, middle.getCause());
				++numFound;
			}
			
			// Should be able to find the outermost IllegalStateException
			try {
				ExceptionUtil.rethrowIfCause(ex, IllegalStateException.class);
			}
			catch (Exception outer) {
				Assert.assertEquals(ex, outer);
				++numFound;
			}
			
			Assert.assertEquals(3, numFound);
		}
	}
	
	/**
	 * Recursively builds up an exception chain with the requested exception classes in it. 
	 * 
	 * @param classesInChain the start of the list is the root cause, and the end of the list is the outermost wrapped exception
	 */
	private void throwExceptionChain(List<Class<? extends RuntimeException>> classesInChain) throws Exception {
		if (classesInChain.size() > 1) {
			try {
				throwExceptionChain(classesInChain.subList(0, classesInChain.size() - 1));
			}
			catch (Exception ex) {
				Class<? extends Exception> outer = classesInChain.get(classesInChain.size() - 1);
				Constructor<? extends Exception> constructor = outer.getConstructor(Throwable.class);
				throw constructor.newInstance(ex);
			}
		} else {
			// length should be 1
			Class<? extends Exception> classToThrow = classesInChain.get(0);
			throw classToThrow.newInstance();
		}
	}
}
