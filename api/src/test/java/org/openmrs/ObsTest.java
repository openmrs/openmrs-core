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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.APIException;
import org.openmrs.test.Verifies;

/**
 * This class tests all methods that are not getter or setters in the Obs java object TODO: finish
 * this test class for Obs
 * 
 * @see Obs
 */
public class ObsTest {
	
	private static final String VERO = "Vero";
	
	private static final String FORM_NAMESPACE_PATH_SEPARATOR = "^";
	
	/**
	 * Tests the addToGroup method in ObsGroup
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldAddandRemoveObsToGroup() throws Exception {
		
		Obs obs = new Obs(1);
		
		Obs obsGroup = new Obs(755);
		
		// These methods should not fail even with null attributes on the obs
		assertFalse(obsGroup.isObsGrouping());
		assertFalse(obsGroup.hasGroupMembers(false));
		assertFalse(obsGroup.hasGroupMembers(true)); // Check both flags for false
		
		// adding an obs when the obs group has no other obs
		// should not throw an error
		obsGroup.addGroupMember(obs);
		assertEquals(1, obsGroup.getGroupMembers().size());
		
		// check duplicate add. should only be one
		obsGroup.addGroupMember(obs);
		assertTrue(obsGroup.hasGroupMembers(false));
		assertEquals("Duplicate add should not increase the grouped obs size", 1, obsGroup.getGroupMembers().size());
		
		Obs obs2 = new Obs(2);
		
		obsGroup.removeGroupMember(obs2);
		assertTrue(obsGroup.hasGroupMembers(false));
		assertEquals("Removing a non existent obs should not decrease the number of grouped obs", 1, obsGroup
		        .getGroupMembers().size());
		
		// testing removing an obs from a group that has a null obs list
		new Obs().removeGroupMember(obs2);
		
		obsGroup.removeGroupMember(obs);
		
		assertEquals(0, obsGroup.getGroupMembers().size());
		
		// try to add an obs group to itself
		try {
			obsGroup.addGroupMember(obsGroup);
			fail("An APIException about adding an obsGroup should have been thrown");
		}
		catch (APIException e) {
			// this exception is expected
		}
	}
	
	/**
	 * tests the getRelatedObservations method:
	 */
	@Test
	public void shouldGetRelatedObservations() throws Exception {
		// create a child Obs
		Obs o = new Obs();
		o.setDateCreated(new Date());
		o.setLocation(new Location(1));
		o.setObsDatetime(new Date());
		o.setPerson(new Patient(2));
		o.setValueText("childObs");
		
		// create its sibling
		Obs oSibling = new Obs();
		oSibling.setDateCreated(new Date());
		oSibling.setLocation(new Location(1));
		oSibling.setObsDatetime(new Date());
		oSibling.setValueText("childObs2");
		oSibling.setPerson(new Patient(2));
		
		// create a parent Obs
		Obs oParent = new Obs();
		oParent.setDateCreated(new Date());
		oParent.setLocation(new Location(1));
		oParent.setObsDatetime(new Date());
		oSibling.setValueText("parentObs");
		oParent.setPerson(new Patient(2));
		
		// create a grandparent obs
		Obs oGrandparent = new Obs();
		oGrandparent.setDateCreated(new Date());
		oGrandparent.setLocation(new Location(1));
		oGrandparent.setObsDatetime(new Date());
		oGrandparent.setPerson(new Patient(2));
		oSibling.setValueText("grandParentObs");
		
		oParent.addGroupMember(o);
		oParent.addGroupMember(oSibling);
		oGrandparent.addGroupMember(oParent);
		
		// create a leaf observation at the grandparent level
		Obs o2 = new Obs();
		o2.setDateCreated(new Date());
		o2.setLocation(new Location(1));
		o2.setObsDatetime(new Date());
		o2.setPerson(new Patient(2));
		o2.setValueText("grandparentLeafObs");
		
		oGrandparent.addGroupMember(o2);
		
		/**
		 * test to make sure that if the original child obs calls getRelatedObservations, it returns
		 * itself and its siblings: original obs is one of two groupMembers, so relatedObservations
		 * should return a size of set 2 then, make sure that if oParent calls
		 * getRelatedObservations, it returns its own children as well as the leaf obs attached to
		 * the grandparentObs oParent has two members, and one leaf ancestor -- so a set of size 3
		 * should be returned.
		 */
		assertEquals(o.getRelatedObservations().size(), 2);
		assertEquals(oParent.getRelatedObservations().size(), 3);
		
		// create a great-grandparent obs
		Obs oGGP = new Obs();
		oGGP.setDateCreated(new Date());
		oGGP.setLocation(new Location(1));
		oGGP.setObsDatetime(new Date());
		oGGP.setPerson(new Patient(2));
		oGGP.setValueText("grandParentObs");
		oGGP.addGroupMember(oGrandparent);
		
		// create a leaf great-grandparent obs
		Obs oGGPleaf = new Obs();
		oGGPleaf.setDateCreated(new Date());
		oGGPleaf.setLocation(new Location(1));
		oGGPleaf.setObsDatetime(new Date());
		oGGPleaf.setPerson(new Patient(2));
		oGGPleaf.setValueText("grandParentObs");
		oGGP.addGroupMember(oGGPleaf);
		
		/**
		 * now run the previous assertions again. this time there are two ancestor leaf obs, so the
		 * first assertion should still return a set of size 2, but the second assertion sould
		 * return a set of size 4.
		 */
		assertEquals(o.getRelatedObservations().size(), 2);
		assertEquals(oParent.getRelatedObservations().size(), 4);
		
		// remove the grandparent leaf observation:
		
		oGrandparent.removeGroupMember(o2);
		
		// now the there is only one ancestor leaf obs:
		assertEquals(o.getRelatedObservations().size(), 2);
		assertEquals(oParent.getRelatedObservations().size(), 3);
		
		/**
		 * finally, test a non-obsGroup and non-member Obs to the function Obs o2 is now not
		 * connected to our heirarchy: an empty set should be returned:
		 */
		
		assertNotNull(o2.getRelatedObservations());
		assertEquals(o2.getRelatedObservations().size(), 0);
		
	}
	
	/**
	 * @see {@link Obs#isComplex()}
	 */
	@Test
	@Verifies(value = "should return true if the concept is complex", method = "isComplex()")
	public void isComplex_shouldReturnTrueIfTheConceptIsComplex() throws Exception {
		ConceptDatatype cd = new ConceptDatatype();
		cd.setName("Complex");
		cd.setHl7Abbreviation("ED");
		
		ConceptComplex complexConcept = new ConceptComplex();
		complexConcept.setDatatype(cd);
		
		Obs obs = new Obs();
		obs.setConcept(complexConcept);
		
		Assert.assertTrue(obs.isComplex());
	}
	
	/**
	 * @see {@link Obs#setValueAsString(String)}
	 */
	@Test(expected = RuntimeException.class)
	@Verifies(value = "should fail if the value of the string is empty", method = "setValueAsString(String)")
	public void setValueAsString_shouldFailIfTheValueOfTheStringIsEmpty() throws Exception {
		Obs obs = new Obs();
		obs.setValueAsString("");
	}
	
	/**
	 * @see {@link Obs#setValueAsString(String)}
	 */
	@Test(expected = RuntimeException.class)
	@Verifies(value = "should fail if the value of the string is null", method = "setValueAsString(String)")
	public void setValueAsString_shouldFailIfTheValueOfTheStringIsNull() throws Exception {
		Obs obs = new Obs();
		obs.setValueAsString(null);
	}
	
	/**
	 * @see {@link Obs#getValueAsBoolean()}
	 */
	@Test
	@Verifies(value = "should return false for value_numeric concepts if value is 0", method = "getValueAsBoolean()")
	public void getValueAsBoolean_shouldReturnFalseForValue_numericConceptsIfValueIs0() throws Exception {
		Obs obs = new Obs();
		obs.setValueNumeric(0.0);
		Assert.assertEquals(false, obs.getValueAsBoolean());
	}
	
	/**
	 * @see {@link Obs#getValueAsBoolean()}
	 */
	@Test
	@Verifies(value = "should return null for value_numeric concepts if value is neither 1 nor 0", method = "getValueAsBoolean()")
	public void getValueAsBoolean_shouldReturnNullForValue_numericConceptsIfValueIsNeither1Nor0() throws Exception {
		Obs obs = new Obs();
		obs.setValueNumeric(24.8);
		Assert.assertNull(obs.getValueAsBoolean());
	}
	
	@Test
	@Verifies(value = "should return non precise values for NumericConcepts", method = "getValueAsString(Locale)")
	public void getValueAsString_shouldReturnNonPreciseValuesForNumericConcepts() throws Exception {
		Obs obs = new Obs();
		obs.setValueNumeric(25.125);
		ConceptNumeric cn = new ConceptNumeric();
		ConceptDatatype cdt = new ConceptDatatype();
		cdt.setHl7Abbreviation("NM");
		cn.setDatatype(cdt);
		cn.setPrecise(false);
		obs.setConcept(cn);
		String str = "25";
		Assert.assertEquals(str, obs.getValueAsString(Locale.US));
	}
	
	@Test
	@Verifies(value = "should not return long decimal numbers as scientific notation", method = "getValueAsString(Locale)")
	public void getValueAsString_shouldNotReturnLongDecimalNumbersAsScientificNotation() throws Exception {
		Obs obs = new Obs();
		obs.setValueNumeric(123456789.0);
		String str = "123456789.0";
		Assert.assertEquals(str, obs.getValueAsString(Locale.US));
	}
	
	@Test
	@Verifies(value = "should return date in correct format", method = "getValueAsString()")
	public void getValueAsString_shouldReturnDateInCorrectFormat() throws Exception {
		Obs obs = new Obs();
		obs.setValueDatetime(new Date());
		Concept cn = new Concept();
		ConceptDatatype cdt = new ConceptDatatype();
		cdt.setHl7Abbreviation("DT");
		cn.setDatatype(cdt);
		obs.setConcept(cn);
		
		Date utilDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = dateFormat.format(utilDate);
		Assert.assertEquals(dateString, obs.getValueAsString(Locale.US));
	}
	
	/**
	 * @see {@link Obs#getValueAsBoolean()}
	 */
	@Test
	@Verifies(value = "should return true for value_numeric concepts if value is 1", method = "getValueAsBoolean()")
	public void getValueAsBoolean_shouldReturnTrueForValue_numericConceptsIfValueIs1() throws Exception {
		Obs obs = new Obs();
		obs.setValueNumeric(1.0);
		Assert.assertEquals(true, obs.getValueAsBoolean());
	}
	
	/**
	 * @see Obs#getGroupMembers(boolean)
	 * @verifies Get all group members if passed true, and non-voided if passed false
	 */
	@Test
	public void getGroupMembers_shouldGetAllGroupMembersIfPassedTrueAndNonvoidedIfPassedFalse() throws Exception {
		Obs parent = new Obs(1);
		Set<Obs> members = new HashSet<Obs>();
		members.add(new Obs(101));
		members.add(new Obs(103));
		Obs voided = new Obs(99);
		voided.setVoided(true);
		members.add(voided);
		parent.setGroupMembers(members);
		members = parent.getGroupMembers(true);
		assertEquals("set of all members should have length of 3", 3, members.size());
		members = parent.getGroupMembers(false);
		assertEquals("set of non-voided should have length of 2", 2, members.size());
		members = parent.getGroupMembers(); // should be same as false
		assertEquals("default should return non-voided with length of 2", 2, members.size());
	}
	
	/**
	 * @see Obs#hasGroupMembers(boolean)
	 * @verifies return true if this obs has group members based on parameter
	 */
	@Test
	public void hasGroupMembers_shouldReturnTrueIfThisObsHasGroupMembersBasedOnParameter() throws Exception {
		Obs parent = new Obs(5);
		Obs child = new Obs(33);
		child.setVoided(true);
		parent.addGroupMember(child); // Only contains 1 voided child
		assertTrue("When checking for all members, should return true", parent.hasGroupMembers(true));
		assertFalse("When checking for non-voided, should return false", parent.hasGroupMembers(false));
		assertFalse("Default should check for non-voided", parent.hasGroupMembers());
	}
	
	/**
	 * @see Obs#isObsGrouping()
	 * @verifies ignore voided Obs
	 */
	@Test
	public void isObsGrouping_shouldIncludeVoidedObs() throws Exception {
		Obs parent = new Obs(5);
		Obs child = new Obs(33);
		child.setVoided(true);
		parent.addGroupMember(child);
		assertTrue("When checking for Obs grouping, should include voided Obs", parent.isObsGrouping());
	}
	
	/**
	 * @see Obs#getValueAsString(Locale)
	 * @verifies use commas or decimal places depending on locale
	 */
	@Test
	public void getValueAsString_shouldUseCommasOrDecimalPlacesDependingOnLocale() throws Exception {
		Obs obs = new Obs();
		obs.setValueNumeric(123456789.3);
		String str = "123456789,3";
		Assert.assertEquals(str, obs.getValueAsString(Locale.GERMAN));
	}
	
	/**
	 * @see Obs#getValueAsString(Locale)
	 * @verifies not use thousand separator
	 */
	@Test
	public void getValueAsString_shouldNotUseThousandSeparator() throws Exception {
		Obs obs = new Obs();
		obs.setValueNumeric(123456789.0);
		String str = "123456789.0";
		Assert.assertEquals(str, obs.getValueAsString(Locale.ENGLISH));
	}
	
	/**
	 * @see Obs#getValueAsString(Locale)
	 * @verifies return regular number for size of zero to or greater than ten digits
	 */
	@Test
	public void getValueAsString_shouldReturnRegularNumberForSizeOfZeroToOrGreaterThanTenDigits() throws Exception {
		Obs obs = new Obs();
		obs.setValueNumeric(1234567890.0);
		String str = "1234567890.0";
		Assert.assertEquals(str, obs.getValueAsString(Locale.ENGLISH));
	}
	
	/**
	 * @see Obs#getValueAsString(Locale)
	 * @verifies return regular number if decimal places are as high as six
	 */
	@Test
	public void getValueAsString_shouldReturnRegularNumberIfDecimalPlacesAreAsHighAsSix() throws Exception {
		Obs obs = new Obs();
		obs.setValueNumeric(123456789.012345);
		String str = "123456789.012345";
		Assert.assertEquals(str, obs.getValueAsString(Locale.ENGLISH));
	}
	
	@Test
	@Verifies(value = "should return localized name of the value coded concept", method = "getValueAsString(Locale)")
	public void getValueAsString_shouldReturnLocalizedCodedConcept() throws Exception {
		ConceptDatatype cdt = new ConceptDatatype();
		cdt.setHl7Abbreviation("CWE");
		
		Concept cn = new Concept();
		cn.setDatatype(cdt);
		cn.addName(new ConceptName(VERO, Locale.ITALIAN));
		
		Obs obs = new Obs();
		obs.setValueCoded(cn);
		obs.setConcept(cn);
		obs.setValueCodedName(new ConceptName("True", Locale.US));
		
		Assert.assertEquals(VERO, obs.getValueAsString(Locale.ITALIAN));
	}
	
	/**
	 * @see {@link Obs#setFormField(String,String)}
	 */
	@Test
	@Verifies(value = "should set the underlying formNamespaceAndPath in the correct pattern", method = "setFormField(String,String)")
	public void setFormField_shouldSetTheUnderlyingFormNamespaceAndPathInTheCorrectPattern() throws Exception {
		final String ns = "my ns";
		final String path = "my path";
		Obs obs = new Obs();
		obs.setFormField(ns, path);
		java.lang.reflect.Field formNamespaceAndPathProperty = Obs.class.getDeclaredField("formNamespaceAndPath");
		formNamespaceAndPathProperty.setAccessible(true);
		Assert.assertEquals(ns + FORM_NAMESPACE_PATH_SEPARATOR + path, formNamespaceAndPathProperty.get(obs));
	}
	
	/**
	 * @see {@link Obs#getFormFieldNamespace()}
	 */
	@Test
	@Verifies(value = "should return null if the namespace is not specified", method = "getFormFieldNamespace()")
	public void getFormFieldNamespace_shouldReturnNullIfTheNamespaceIsNotSpecified() throws Exception {
		Obs obs = new Obs();
		obs.setFormField("", "my path");
		Assert.assertNull(obs.getFormFieldNamespace());
	}
	
	/**
	 * @see {@link Obs#getFormFieldNamespace()}
	 */
	@Test
	@Verifies(value = "should return the correct namespace for a form field with a path", method = "getFormFieldNamespace()")
	public void getFormFieldNamespace_shouldReturnTheCorrectNamespaceForAFormFieldWithAPath() throws Exception {
		final String ns = "my ns";
		final String path = "my path";
		Obs obs = new Obs();
		obs.setFormField(ns, path);
		Assert.assertEquals(ns, obs.getFormFieldNamespace());
	}
	
	/**
	 * @see {@link Obs#getFormFieldNamespace()}
	 */
	@Test
	@Verifies(value = "should return the namespace for a form field that has no path", method = "getFormFieldNamespace()")
	public void getFormFieldNamespace_shouldReturnTheNamespaceForAFormFieldThatHasNoPath() throws Exception {
		final String ns = "my ns";
		Obs obs = new Obs();
		obs.setFormField(ns, null);
		Assert.assertEquals(ns, obs.getFormFieldNamespace());
	}
	
	/**
	 * @see {@link Obs#getFormFieldPath()}
	 */
	@Test
	@Verifies(value = "should return null if the path is not specified", method = "getFormFieldPath()")
	public void getFormFieldPath_shouldReturnNullIfThePathIsNotSpecified() throws Exception {
		Obs obs = new Obs();
		obs.setFormField("my ns", "");
		Assert.assertNull(obs.getFormFieldPath());
	}
	
	/**
	 * @see {@link Obs#getFormFieldPath()}
	 */
	@Test
	@Verifies(value = "should return the correct path for a form field with a namespace", method = "getFormFieldPath()")
	public void getFormFieldPath_shouldReturnTheCorrectPathForAFormFieldWithANamespace() throws Exception {
		final String ns = "my ns";
		final String path = "my path";
		Obs obs = new Obs();
		obs.setFormField(ns, path);
		Assert.assertEquals(path, obs.getFormFieldPath());
	}
	
	/**
	 * @see {@link Obs#getFormFieldPath()}
	 */
	@Test
	@Verifies(value = "should return the path for a form field that has no namespace", method = "getFormFieldPath()")
	public void getFormFieldPath_shouldReturnThePathForAFormFieldThatHasNoNamespace() throws Exception {
		final String path = "my path";
		Obs obs = new Obs();
		obs.setFormField("", path);
		Assert.assertEquals(path, obs.getFormFieldPath());
	}
	
	/**
	 * @see {@link Obs#setFormField(String,String)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should reject a namepace and path combination longer than the max length", method = "setFormField(String,String)")
	public void setFormField_shouldRejectANamepaceAndPathCombinationLongerThanTheMaxLength() throws Exception {
		StringBuffer nsBuffer = new StringBuffer(125);
		for (int i = 0; i < 125; i++) {
			nsBuffer.append("n");
		}
		StringBuffer pathBuffer = new StringBuffer(130);
		for (int i = 0; i < 130; i++) {
			nsBuffer.append("p");
		}
		
		final String ns = nsBuffer.toString();
		final String path = pathBuffer.toString();
		Obs obs = new Obs();
		obs.setFormField(ns, path);
	}
	
	/**
	 * @see {@link Obs#setFormField(String,String)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should reject a namepace containing the separator", method = "setFormField(String,String)")
	public void setFormField_shouldRejectANamepaceContainingTheSeparator() throws Exception {
		final String ns = "my ns" + FORM_NAMESPACE_PATH_SEPARATOR;
		Obs obs = new Obs();
		obs.setFormField(ns, "");
	}
	
	/**
	 * @see {@link Obs#setFormField(String,String)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should reject a path containing the separator", method = "setFormField(String,String)")
	public void setFormField_shouldRejectAPathContainingTheSeparator() throws Exception {
		final String path = FORM_NAMESPACE_PATH_SEPARATOR + "my path";
		Obs obs = new Obs();
		obs.setFormField("", path);
	}
}
