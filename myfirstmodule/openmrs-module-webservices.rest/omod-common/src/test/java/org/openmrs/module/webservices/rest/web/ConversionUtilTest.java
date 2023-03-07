/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web;

import static org.hamcrest.core.Is.is;
import org.junit.Assert;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Test;
import org.openmrs.api.ConceptNameType;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;

public class ConversionUtilTest extends BaseModuleWebContextSensitiveTest {
	
	/**
	 * @see ConversionUtil#convert(Object,Type)
	 * @verifies String to Date conversion for multiple formatted date/dateTime strings
	 */
	@Test
	public void convert_shouldReturnEqualsDateFromString() throws Exception {
		Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse("2011-05-01T00:00:00.000");
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.setTimeZone(TimeZone.getDefault());
		Date expected = cal.getTime();
		String[] dateFormats = { "2011-05-01", "2011-05-01 00:00:00", "2011-05-01T00:00:00.000", "2011-05-01T00:00:00.000" };
		for (int i = 0; i < dateFormats.length; i++) {
			Date result = (Date) ConversionUtil.convert(dateFormats[i], Date.class);
			Assert.assertEquals(result, expected);
		}
	}
	
	/**
	 * @see ConversionUtil#convert(Object,Type)
	 * @verifies String to Date conversion for multiple formatted date/dateTime strings having
	 *           timezone
	 */
	@Test
	public void convert_shouldReturnCorrectDateWhenParsingStringHavingTimeZone() throws Exception {
		Date expectedDate1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse("2016-01-12T06:00:00+0530");
		//Added to check against more ISO8601 format dates im 'dates2' array
		Date expectedDate2 = (Date) ConversionUtil.convert("2014-02-20T11:00:00.000-0500", Date.class);
		
		String[] dates1 = { "2016-01-12T06:00:00+05:30", "2016-01-12T06:00:00+0530" };
		String[] dates2 = { "2014-02-20T11:00:00.000-05:00", "2014-02-20T11:00:00.000-05" };
		
		for (String date : dates1) {
			Date actualDate = (Date) ConversionUtil.convert(date, Date.class);
			Assert.assertEquals(expectedDate1, actualDate);
		}
		
		for (String date : dates2) {
			Date actualDate = (Date) ConversionUtil.convert(date, Date.class);
			Assert.assertEquals(expectedDate2, actualDate);
		}
	}
	
	/**
	 * @see ConversionUtil#convert(Object,Type)
	 * @verifies String to Date conversion by assert false for date mismatches
	 */
	@Test
	public void convert_shouldReturnFalseOnIncorrectDateFromString() throws Exception {
		Date expected = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse("2011-05-01T00:00:00.000+0530");
		String[] dateFormats = { "2011-05-01T00:00:00.000+0200", "2012-05-01T00:00:00.000" };
		for (int i = 0; i < dateFormats.length; i++) {
			Date result = (Date) ConversionUtil.convert(dateFormats[i], Date.class);
			Assert.assertTrue(result != expected);
		}
	}
	
	/**
	 * @see ConversionUtil#convert(Object,Type)
	 * @verifies String format and its representation are equal
	 */
	@Test
	public void convertToRepresentation_shouldReturnSameStringForToday() throws Exception {
		Date today = new Date();
		String expected = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(today);
		String result = (String) ConversionUtil.convertToRepresentation(today, Representation.REF);
		Assert.assertEquals(result, expected);
	}
	
	/**
	 * @see {@link ConversionUtil#convert(Object,Type)}
	 */
	@Test
	public void convert_shouldSConvertStringsToEnumsValues() throws Exception {
		Object conceptNameType = ConversionUtil.convert("FULLY_SPECIFIED", ConceptNameType.class);
		Assert.assertNotNull(conceptNameType);
		Assert.assertTrue(conceptNameType.getClass().isAssignableFrom(ConceptNameType.class));
	}
	
	/**
	 * @see {@link ConversionUtil#convert(Object,Type)}
	 */
	@Test
	public void convert_shouldConvertStringsToLocales() throws Exception {
		Object locale = ConversionUtil.convert("en", Locale.class);
		Assert.assertNotNull(locale);
		Assert.assertTrue(locale.getClass().isAssignableFrom(Locale.class));
	}
	
	/**
	 * @see {@link ConversionUtil#convert(Object,Type)}
	 * @verifies convert to an array
	 */
	@Test
	public void convert_shouldConvertToAnArray() throws Exception {
		List<String> input = Arrays.asList("en", "fr");
		Locale[] converted = (Locale[]) ConversionUtil.convert(input, Locale[].class);
		assertThat(converted.length, is(2));
		assertThat(converted[0], is(Locale.ENGLISH));
		assertThat(converted[1], is(Locale.FRENCH));
	}
	
	/**
	 * @see {@link ConversionUtil#convert(Object,Type)}
	 * @verifies convert to a class
	 */
	@Test
	public void convert_shouldConvertToAClass() throws Exception {
		String input = "java.lang.String";
		Class converted = (Class) ConversionUtil.convert(input, Class.class);
		Assert.assertTrue(converted.isAssignableFrom(String.class));
	}
	
	public void convert_shouldConvertIntToDouble() throws Exception {
		assertThat((Double) ConversionUtil.convert(5, Double.class), is(5d));
	}
	
	public void convert_shouldConvertDoubleToInt() throws Exception {
		assertThat((Integer) ConversionUtil.convert(5d, Integer.class), is(5));
	}
	
	/**
	 * @verifies resolve TypeVariables to actual type
	 * @see ConversionUtil#convert(Object, java.lang.reflect.Type)
	 */
	@Test
	public void convert_shouldResolveTypeVariablesToActualType() throws Exception {
		ChildGenericType_Int i = new ChildGenericType_Int();
		Method setter = PropertyUtils.getPropertyDescriptor(i, "value").getWriteMethod();
		
		Object result = ConversionUtil.convert("25", setter.getGenericParameterTypes()[0], i);
		
		Assert.assertNotNull(result);
		Assert.assertEquals(25, result);
	}
	
	/**
	 * @verifies return the actual type if defined on the parent class
	 * @see ConversionUtil#getTypeVariableClass(Class, java.lang.reflect.TypeVariable)
	 */
	@Test
	public void getTypeVariableClass_shouldReturnTheActualTypeIfDefinedOnTheParentClass() throws Exception {
		ChildGenericType_Int i = new ChildGenericType_Int();
		ChildGenericType_String s = new ChildGenericType_String();
		ChildGenericType_Temp t = new ChildGenericType_Temp();
		
		Method setter = PropertyUtils.getPropertyDescriptor(i, "value").getWriteMethod();
		Type type = ConversionUtil.getTypeVariableClass(ChildGenericType_Int.class,
		    (TypeVariable<?>) setter.getGenericParameterTypes()[0]);
		
		Assert.assertNotNull(type);
		Assert.assertEquals(Integer.class, type);
		
		setter = PropertyUtils.getPropertyDescriptor(s, "value").getWriteMethod();
		type = ConversionUtil.getTypeVariableClass(ChildGenericType_String.class,
		    (TypeVariable<?>) setter.getGenericParameterTypes()[0]);
		
		Assert.assertNotNull(type);
		Assert.assertEquals(String.class, type);
		
		setter = PropertyUtils.getPropertyDescriptor(t, "value").getWriteMethod();
		type = ConversionUtil.getTypeVariableClass(ChildGenericType_Temp.class,
		    (TypeVariable<?>) setter.getGenericParameterTypes()[0]);
		
		Assert.assertNotNull(type);
		Assert.assertEquals(Temp.class, type);
	}
	
	/**
	 * @verifies return the actual type if defined on the grand-parent class
	 * @see ConversionUtil#getTypeVariableClass(Class, java.lang.reflect.TypeVariable)
	 */
	@Test
	public void getTypeVariableClass_shouldReturnTheActualTypeIfDefinedOnTheGrandparentClass() throws Exception {
		GrandchildGenericType_Int i = new GrandchildGenericType_Int();
		GreatGrandchildGenericType_Int i2 = new GreatGrandchildGenericType_Int();
		
		Method setter = PropertyUtils.getPropertyDescriptor(i, "value").getWriteMethod();
		Type type = ConversionUtil.getTypeVariableClass(GrandchildGenericType_Int.class,
		    (TypeVariable<?>) setter.getGenericParameterTypes()[0]);
		
		Assert.assertNotNull(type);
		Assert.assertEquals(Integer.class, type);
		
		setter = PropertyUtils.getPropertyDescriptor(i2, "value").getWriteMethod();
		type = ConversionUtil.getTypeVariableClass(GreatGrandchildGenericType_Int.class,
		    (TypeVariable<?>) setter.getGenericParameterTypes()[0]);
		
		Assert.assertNotNull(type);
		Assert.assertEquals(Integer.class, type);
	}
	
	/**
	 * @verifies return null when actual type cannot be found
	 * @see ConversionUtil#getTypeVariableClass(Class, java.lang.reflect.TypeVariable)
	 */
	@Test
	public void getTypeVariableClass_shouldReturnNullWhenActualTypeCannotBeFound() throws Exception {
		GrandchildGenericType_Int i = new GrandchildGenericType_Int();
		
		Method setter = PropertyUtils.getPropertyDescriptor(i, "value").getWriteMethod();
		Type type = ConversionUtil.getTypeVariableClass(Temp.class, (TypeVariable<?>) setter.getGenericParameterTypes()[0]);
		
		Assert.assertNull(type);
	}
	
	/**
	 * @verifies return the correct actual type if there are multiple generic types
	 * @see ConversionUtil#getTypeVariableClass(Class, java.lang.reflect.TypeVariable)
	 */
	@Test
	public void getTypeVariableClass_shouldReturnTheCorrectActualTypeIfThereAreMultipleGenericTypes() throws Exception {
		ChildMultiGenericType i = new ChildMultiGenericType();
		
		Method setter = PropertyUtils.getPropertyDescriptor(i, "first").getWriteMethod();
		Type type = ConversionUtil.getTypeVariableClass(ChildMultiGenericType.class,
		    (TypeVariable<?>) setter.getGenericParameterTypes()[0]);
		
		Assert.assertNotNull(type);
		Assert.assertEquals(Integer.class, type);
		
		setter = PropertyUtils.getPropertyDescriptor(i, "second").getWriteMethod();
		type = ConversionUtil.getTypeVariableClass(ChildMultiGenericType.class,
		    (TypeVariable<?>) setter.getGenericParameterTypes()[0]);
		
		Assert.assertNotNull(type);
		Assert.assertEquals(String.class, type);
		
		setter = PropertyUtils.getPropertyDescriptor(i, "third").getWriteMethod();
		type = ConversionUtil.getTypeVariableClass(ChildMultiGenericType.class,
		    (TypeVariable<?>) setter.getGenericParameterTypes()[0]);
		
		Assert.assertNotNull(type);
		Assert.assertEquals(Temp.class, type);
	}
	
	/**
	 * @verifies throw IllegalArgumentException when instance class is null
	 * @see ConversionUtil#getTypeVariableClass(Class, java.lang.reflect.TypeVariable)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getTypeVariableClass_shouldThrowIllegalArgumentExceptionWhenInstanceClassIsNull() throws Exception {
		GrandchildGenericType_Int i = new GrandchildGenericType_Int();
		
		Method setter = PropertyUtils.getPropertyDescriptor(i, "value").getWriteMethod();
		Type type = ConversionUtil.getTypeVariableClass(null, (TypeVariable<?>) setter.getGenericParameterTypes()[0]);
	}
	
	/**
	 * @verifies throw IllegalArgumentException when typeVariable is null
	 * @see ConversionUtil#getTypeVariableClass(Class, java.lang.reflect.TypeVariable)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getTypeVariableClass_shouldThrowIllegalArgumentExceptionWhenTypeVariableIsNull() throws Exception {
		ConversionUtil.getTypeVariableClass(Temp.class, null);
	}
	
	public abstract class BaseGenericType<T> {
		
		private T value;
		
		public T getValue() {
			return value;
		}
		
		public void setValue(T value) {
			this.value = value;
		}
	}
	
	public abstract class BaseMultiGenericType<F, S, T> {
		
		private F first;
		
		private S second;
		
		private T third;
		
		public F getFirst() {
			return first;
		}
		
		public void setFirst(F first) {
			this.first = first;
		}
		
		public S getSecond() {
			return second;
		}
		
		public void setSecond(S second) {
			this.second = second;
		}
		
		public T getThird() {
			return third;
		}
		
		public void setThird(T third) {
			this.third = third;
		}
	}
	
	public class Temp {}
	
	public class ChildGenericType_Int extends BaseGenericType<Integer> {}
	
	public class ChildGenericType_String extends BaseGenericType<String> {}
	
	public class ChildGenericType_Temp extends BaseGenericType<Temp> {}
	
	public class GrandchildGenericType_Int extends ChildGenericType_Int {}
	
	public class GreatGrandchildGenericType_Int extends GrandchildGenericType_Int {}
	
	public class ChildMultiGenericType extends BaseMultiGenericType<Integer, String, Temp> {}
}
