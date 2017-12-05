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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.APIException;
import org.openmrs.obs.ComplexData;
import org.openmrs.util.Reflect;

/**
 * This class tests all methods that are not getter or setters in the Obs java object TODO: finish
 * this test class for Obs
 * 
 * @see Obs
 */
public class ObsTest {
	
	private static final String VERO = "Vero";
	
	private static final String FORM_NAMESPACE_PATH_SEPARATOR = "^";
	
	//ignore these fields, groupMembers and formNamespaceAndPath field are taken care of by other tests
	private static final List<String> IGNORED_FIELDS = Arrays.asList("dirty", "log", "serialVersionUID",
	    "DATE_TIME_PATTERN", "TIME_PATTERN", "DATE_PATTERN", "FORM_NAMESPACE_PATH_SEPARATOR",
	    "FORM_NAMESPACE_PATH_MAX_LENGTH", "obsId", "groupMembers", "uuid", "changedBy", "dateChanged", "voided", "voidedBy",
	    "voidReason", "dateVoided", "formNamespaceAndPath", "$jacocoData");
	
	private void resetObs(Obs obs) throws Exception {
		Field field = Obs.class.getDeclaredField("dirty");
		field.setAccessible(true);
		try {
			field.set(obs, false);
		}
		finally {
			field.setAccessible(false);
		}
		assertFalse(obs.isDirty());
	}
	
	private Obs createObs(Integer id) throws Exception {
		Obs obs = new Obs(id);
		List<Field> fields = Reflect.getAllFields(Obs.class);
		for (Field field : fields) {
			if (IGNORED_FIELDS.contains(field.getName())) {
				continue;
			}
			setFieldValue(obs, field, false);
		}
		assertFalse(obs.isDirty());
		return obs;
	}

	private void setFieldValue(Obs obs, Field field, boolean setAlternateValue) throws Exception {
		final boolean accessible = field.isAccessible();
		if (!accessible) {
			field.setAccessible(true);
		}
		try {
			Object oldFieldValue = field.get(obs);
			Object newFieldValue = generateValue(field, setAlternateValue);
			//sanity check
			if (setAlternateValue) {
				assertNotEquals("The old and new values should be different for field: Obs." + field.getName(),
				    oldFieldValue, newFieldValue);
			}
			
			field.set(obs, newFieldValue);
		}
		finally {
			field.setAccessible(accessible);
		}
	}
	
	private Object generateValue(Field field, boolean setAlternateValue) throws Exception {
		Object fieldValue;
		if (field.getType().equals(Boolean.class)) {
			fieldValue = setAlternateValue;
		} else if (field.getType().equals(Integer.class)) {
			fieldValue = setAlternateValue ? 10 : 17;
		} else if (field.getType().equals(Double.class)) {
			fieldValue = setAlternateValue ? 5.0 : 7.0;
		} else if (field.getType().equals(Date.class)) {
			fieldValue = new Date();
			if (setAlternateValue) {
				Calendar c = Calendar.getInstance();
				c.add(Calendar.MINUTE, 2);
				fieldValue = c.getTime();
			}
		} else if (field.getType().equals(String.class)) {
			fieldValue = setAlternateValue ? "old" : "new";
		} else if (field.getType().equals(Person.class)) {
			//setPerson updates the personId, so we want the personIds to match for the tests to be valid
			fieldValue = new Person(setAlternateValue ? 10 : 17);
		} else if (field.getType().equals(ComplexData.class)) {
			fieldValue = new ComplexData(setAlternateValue ? "some complex data" : "Some other value", new Object());
		} else if (field.getType().equals(Obs.Interpretation.class)) {
			fieldValue = setAlternateValue ? Obs.Interpretation.ABNORMAL : Obs.Interpretation.CRITICALLY_ABNORMAL;
		} else if (field.getType().equals(Obs.Status.class)) {
			fieldValue = setAlternateValue ? Obs.Status.AMENDED : Obs.Status.PRELIMINARY;
		} else {
			fieldValue = field.getType().newInstance();
		}
		assertNotNull("Failed to generate a value for field: Obs." + field.getName());
		
		return fieldValue;
	}
	
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
	 * @see Obs#isComplex()
	 */
	@Test
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
	 * @see Obs#setValueAsString(String)
	 */
	@Test(expected = RuntimeException.class)
	public void setValueAsString_shouldFailIfTheValueOfTheStringIsEmpty() throws Exception {
		Obs obs = new Obs();
		obs.setValueAsString("");
	}
	
	/**
	 * @see Obs#setValueAsString(String)
	 */
	@Test(expected = RuntimeException.class)
	public void setValueAsString_shouldFailIfTheValueOfTheStringIsNull() throws Exception {
		Obs obs = new Obs();
		obs.setValueAsString(null);
	}
	
	/**
	 * @see Obs#getValueAsBoolean()
	 */
	@Test
	public void getValueAsBoolean_shouldReturnFalseForValue_numericConceptsIfValueIs0() throws Exception {
		Obs obs = new Obs();
		obs.setValueNumeric(0.0);
		Assert.assertEquals(false, obs.getValueAsBoolean());
	}
	
	/**
	 * @see Obs#getValueAsBoolean()
	 */
	@Test
	public void getValueAsBoolean_shouldReturnNullForValue_numericConceptsIfValueIsNeither1Nor0() throws Exception {
		Obs obs = new Obs();
		obs.setValueNumeric(24.8);
		Assert.assertNull(obs.getValueAsBoolean());
	}
	
	@Test
	public void getValueAsString_shouldReturnNonPreciseValuesForNumericConcepts() throws Exception {
		Obs obs = new Obs();
		obs.setValueNumeric(25.125);
		ConceptNumeric cn = new ConceptNumeric();
		ConceptDatatype cdt = new ConceptDatatype();
		cdt.setHl7Abbreviation("NM");
		cn.setDatatype(cdt);
		cn.setAllowDecimal(false);
		obs.setConcept(cn);
		String str = "25";
		Assert.assertEquals(str, obs.getValueAsString(Locale.US));
	}
	
	@Test
	public void getValueAsString_shouldNotReturnLongDecimalNumbersAsScientificNotation() throws Exception {
		Obs obs = new Obs();
		obs.setValueNumeric(123456789.0);
		String str = "123456789.0";
		Assert.assertEquals(str, obs.getValueAsString(Locale.US));
	}
	
	@Test
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
	 * @see Obs#getValueAsBoolean()
	 */
	@Test
	public void getValueAsBoolean_shouldReturnTrueForValue_numericConceptsIfValueIs1() throws Exception {
		Obs obs = new Obs();
		obs.setValueNumeric(1.0);
		Assert.assertEquals(true, obs.getValueAsBoolean());
	}
	
	/**
	 * @see Obs#getGroupMembers(boolean)
	 */
	@Test
	public void getGroupMembers_shouldGetAllGroupMembersIfPassedTrueAndNonvoidedIfPassedFalse() throws Exception {
		Obs parent = new Obs(1);
		Set<Obs> members = new HashSet<>();
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
	 */
	@Test
	public void getValueAsString_shouldReturnRegularNumberIfDecimalPlacesAreAsHighAsSix() throws Exception {
		Obs obs = new Obs();
		obs.setValueNumeric(123456789.012345);
		String str = "123456789.012345";
		Assert.assertEquals(str, obs.getValueAsString(Locale.ENGLISH));
	}
	
	@Test
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
	 * @see Obs#setFormField(String,String)
	 */
	@Test
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
	 * @see Obs#getFormFieldNamespace()
	 */
	@Test
	public void getFormFieldNamespace_shouldReturnNullIfTheNamespaceIsNotSpecified() throws Exception {
		Obs obs = new Obs();
		obs.setFormField("", "my path");
		Assert.assertNull(obs.getFormFieldNamespace());
	}
	
	/**
	 * @see Obs#getFormFieldNamespace()
	 */
	@Test
	public void getFormFieldNamespace_shouldReturnTheCorrectNamespaceForAFormFieldWithAPath() throws Exception {
		final String ns = "my ns";
		final String path = "my path";
		Obs obs = new Obs();
		obs.setFormField(ns, path);
		Assert.assertEquals(ns, obs.getFormFieldNamespace());
	}
	
	/**
	 * @see Obs#getFormFieldNamespace()
	 */
	@Test
	public void getFormFieldNamespace_shouldReturnTheNamespaceForAFormFieldThatHasNoPath() throws Exception {
		final String ns = "my ns";
		Obs obs = new Obs();
		obs.setFormField(ns, null);
		Assert.assertEquals(ns, obs.getFormFieldNamespace());
	}
	
	/**
	 * @see Obs#getFormFieldPath()
	 */
	@Test
	public void getFormFieldPath_shouldReturnNullIfThePathIsNotSpecified() throws Exception {
		Obs obs = new Obs();
		obs.setFormField("my ns", "");
		Assert.assertNull(obs.getFormFieldPath());
	}
	
	/**
	 * @see Obs#getFormFieldPath()
	 */
	@Test
	public void getFormFieldPath_shouldReturnTheCorrectPathForAFormFieldWithANamespace() throws Exception {
		final String ns = "my ns";
		final String path = "my path";
		Obs obs = new Obs();
		obs.setFormField(ns, path);
		Assert.assertEquals(path, obs.getFormFieldPath());
	}
	
	/**
	 * @see Obs#getFormFieldPath()
	 */
	@Test
	public void getFormFieldPath_shouldReturnThePathForAFormFieldThatHasNoNamespace() throws Exception {
		final String path = "my path";
		Obs obs = new Obs();
		obs.setFormField("", path);
		Assert.assertEquals(path, obs.getFormFieldPath());
	}
	
	/**
	 * @see Obs#setFormField(String,String)
	 */
	@Test(expected = APIException.class)
	public void setFormField_shouldRejectANamepaceAndPathCombinationLongerThanTheMaxLength() throws Exception {
		StringBuilder nsBuffer = new StringBuilder(125);
		for (int i = 0; i < 125; i++) {
			nsBuffer.append("n");
		}
		for (int i = 0; i < 130; i++) {
			nsBuffer.append("p");
		}
		
		final String ns = nsBuffer.toString();
		final String path = "";
		Obs obs = new Obs();
		obs.setFormField(ns, path);
	}
	
	/**
	 * @see Obs#setFormField(String,String)
	 */
	@Test(expected = APIException.class)
	public void setFormField_shouldRejectANamepaceContainingTheSeparator() throws Exception {
		final String ns = "my ns" + FORM_NAMESPACE_PATH_SEPARATOR;
		Obs obs = new Obs();
		obs.setFormField(ns, "");
	}
	
	/**
	 * @see Obs#setFormField(String,String)
	 */
	@Test(expected = APIException.class)
	public void setFormField_shouldRejectAPathContainingTheSeparator() throws Exception {
		final String path = FORM_NAMESPACE_PATH_SEPARATOR + "my path";
		Obs obs = new Obs();
		obs.setFormField("", path);
	}
	
	/**
	 * @see Obs#isDirty()
	 */
	@Test
	public void isDirty_shouldReturnFalseWhenNoChangeHasBeenMade() throws Exception {
		assertFalse(new Obs().isDirty());
		
		//Should also work if setters are called with same values as the original
		Obs obs = createObs(2);
		obs.setGroupMembers(new LinkedHashSet<>());
		obs.getConcept().setDatatype(new ConceptDatatype());
		assertFalse(obs.isDirty());
		BeanUtils.copyProperties(obs, BeanUtils.cloneBean(obs));
		assertFalse(obs.isDirty());

		obs = createObs(null);
		obs.setGroupMembers(new LinkedHashSet<>());
		obs.getConcept().setDatatype(new ConceptDatatype());
		assertFalse(obs.isDirty());
		BeanUtils.copyProperties(obs, BeanUtils.cloneBean(obs));
		assertFalse(obs.isDirty());
	}
	
	/**
	 * @see Obs#isDirty()
	 */
	@Test
	public void isDirty_shouldReturnTrueWhenAnyImmutableFieldHasBeenChangedForEditedObs() throws Exception {
		Obs obs = createObs(2);
		assertFalse(obs.isDirty());
		updateImmutableFieldsAndAssert(obs, true);
	}

	/**
	 * @see Obs#isDirty()
	 */
	@Test
	public void isDirty_shouldReturnFalseWhenAnyImmutableFieldHasBeenChangedForNewObs() throws Exception {
		Obs obs = createObs(null);
		assertFalse(obs.isDirty());
		updateImmutableFieldsAndAssert(obs, false);
	}

	private void updateImmutableFieldsAndAssert(Obs obs, boolean assertion) throws Exception {
		//Set all fields to some random values via reflection
		List<Field> fields = Reflect.getAllFields(Obs.class);

		final Integer originalPersonId = obs.getPersonId();
		//call each setter and check that dirty has been set to true for each
		for (Field field : fields) {
			String fieldName = field.getName();
			if (IGNORED_FIELDS.contains(fieldName)) {
				continue;
			}

			if ("personId".equals(fieldName)) {
				//call setPersonId because it is protected so BeanUtils.setProperty won't work
				obs.setPersonId((Integer) generateValue(field, true));
			} else {
				BeanUtils.setProperty(obs, fieldName, generateValue(field, true));
			}
			assertEquals("Obs was not marked as dirty after changing: " + fieldName, obs.isDirty(), assertion);
			if ("person".equals(fieldName)) {
				//Because setPerson updates the personId we need to reset personId to its original value 
				//that matches that of person otherwise the test will fail for the personId field
				obs.setPersonId(originalPersonId);
			}
			
			//reset for next field
			resetObs(obs);
		}
	}
	
	/**
	 * @see Obs#isDirty()
	 */
	@Test
	public void isDirty_shouldReturnFalseWhenOnlyMutableFieldsAreChanged() throws Exception {
		Obs obs = new Obs();
		obs.setVoided(true);
		obs.setVoidedBy(new User(1000));
		obs.setVoidReason("some other reason");
		obs.setDateVoided(new Date());
		assertFalse(obs.isDirty());

		Obs obsEdited = new Obs(5);
		obsEdited.setVoided(true);
		obsEdited.setVoidedBy(new User(1000));
		obsEdited.setVoidReason("some other reason");
		obsEdited.setDateVoided(new Date());
		assertFalse(obsEdited.isDirty());
	}


	/**
	 * @see Obs#isDirty()
	 */
	@Test
	public void isDirty_shouldReturnTrueWhenAnImmutableFieldIsChangedFromANonNullToANullValueForEditedObs() throws Exception {
		Obs obs = createObs(2);
		assertNotNull(obs.getComment());
		obs.setComment(null);
		assertTrue(obs.isDirty());
	}

	/**
	 * @see Obs#isDirty()
	 */
	@Test
	public void isDirty_shouldReturnFalsWhenAnImmutableFieldIsChangedFromANonNullToANullValueForNewObs() throws Exception {
		Obs obs = createObs(null);
		assertNotNull(obs.getComment());
		obs.setComment(null);
		assertFalse(obs.isDirty());
	}

	/**
	 * @see Obs#isDirty()
	 */
	@Test
	public void isDirty_shouldReturnTrueWhenAnImmutableFieldIsChangedFromANullToANonNullValueInExistingObs() throws Exception {
		Obs obs = new Obs(5);
		assertNull(obs.getComment());
		obs.setComment("some non null value");
		assertTrue(obs.isDirty());
	}

	/**
	 * @see Obs#isDirty()
	 */
	@Test
	public void isDirty_shouldReturnFalseWhenAnImmutableFieldIsChangedFromANullToANonNullValueInNewObs() throws Exception {
		Obs obs = new Obs();
		assertNull(obs.getComment());
		obs.setComment("some non null value");
		assertFalse(obs.isDirty());
	}
	
	/**
	 * @see Obs#setFormField(String,String)
	 */
	@Test
	public void setFormField_shouldNotMarkTheObsAsDirtyWhenTheValueHasNotBeenChanged() throws Exception {
		Obs obs = createObs(3);
		obs.setFormField(obs.getFormFieldNamespace(), obs.getFormFieldPath());
		assertFalse(obs.isDirty());
	}
	
	/**
	 * @see Obs#setFormField(String,String)
	 */
	@Test
	public void setFormField_shouldMarkTheObsAsDirtyWhenTheValueHasBeenChanged() throws Exception {
		Obs obs = createObs(5);
		final String newNameSpace = "someNameSpace";
		final String newPath = "somePath";
		assertNotEquals(newPath, obs.getFormFieldNamespace());
		assertNotEquals(newNameSpace, obs.getFormFieldPath());
		obs.setFormField(newNameSpace, newPath);
		assertTrue(obs.isDirty());
	}
	
	/**
	 * @see Obs#setFormField(String,String)
	 */
	@Test
	public void setFormField_shouldMarkTheObsAsDirtyWhenTheValueIsChangedFromANonNullToANullValue() throws Exception {
		Obs obs = new Obs(2);
		obs.setFormField("someNameSpace", "somePath");
		resetObs(obs);
		assertFalse(obs.isDirty());
		assertNotNull(obs.getFormFieldNamespace());
		assertNotNull(obs.getFormFieldPath());
		obs.setFormField(null, null);
		assertTrue(obs.isDirty());
	}

	/**
	 * @see Obs#setFormField(String,String)
	 */
	@Test
	public void setFormField_shouldMarkTheObsAsDirtyWhenTheValueIsChangedFromANullToANonNullValue() throws Exception {
		Obs obs = new Obs(5);
		assertNull(obs.getFormFieldNamespace());
		assertNull(obs.getFormFieldPath());
		obs.setFormField("someNameSpace", "somePath");
		assertTrue(obs.isDirty());
	}
	
	/**
	 * @see Obs#addGroupMember(Obs)
	 */
	@Test
	public void addGroupMember_shouldReturnFalseWhenADuplicateObsIsAddedAsAMember() throws Exception {
		Obs obs = new Obs(2);
		Obs member = new Obs();
		obs.addGroupMember(member);
		assertFalse(obs.isDirty());
		resetObs(obs);
		obs.addGroupMember(member);
		assertFalse(obs.isDirty());
	}

	/**
	 * @see Obs#addGroupMember(Obs)
	 */
	@Test
	public void addGroupMember_shouldReturnFalseWhenADuplicateObsIsAddedAsAMemberToNewObs() throws Exception {
		Obs obs = new Obs();
		Obs member = new Obs();
		obs.addGroupMember(member);
		assertFalse(obs.isDirty());
		resetObs(obs);
		obs.addGroupMember(member);
		assertFalse(obs.isDirty());
	}
	
	/**
	 * @see Obs#addGroupMember(Obs)
	 */
	@Test
	public void addGroupMember_shouldReturnFalseWhenANewObsIsAddedAsAMember() throws Exception {
		Obs obs = new Obs(2);
		Obs member1 = new Obs();
		obs.addGroupMember(member1);
		assertFalse(obs.isDirty());
		resetObs(obs);
		Obs member2 = new Obs();
		obs.addGroupMember(member2);
		assertFalse(obs.isDirty());
	}
	
	/**
	 * @see Obs#removeGroupMember(Obs)
	 */
	@Test
	public void removeGroupMember_shouldReturnFalseWhenANonExistentObsIsRemoved() throws Exception {
		Obs obs = new Obs();
		obs.removeGroupMember(new Obs());
		assertFalse(obs.isDirty());
	}
	
	/**
	 * @see Obs#removeGroupMember(Obs)
	 */
	@Test
	public void removeGroupMember_shouldReturnDirtyFalseWhenAnObsIsRemoved() throws Exception {
		Obs obs = new Obs(2);
		Obs member = new Obs();
		obs.addGroupMember(member);
		assertFalse(obs.isDirty());
		resetObs(obs);
		obs.removeGroupMember(member);
		assertFalse(obs.isDirty());
	}

	/**
	 * @see Obs#removeGroupMember(Obs)
	 */
	@Test
	public void removeGroupMember_shouldReturnFalseForDirtyFlagWhenAnObsIsRemovedFromGroup() throws Exception {
		Obs obs = new Obs();
		Obs member = new Obs();
		obs.addGroupMember(member);
		assertFalse(obs.isDirty());
		resetObs(obs);
		obs.removeGroupMember(member);
		assertFalse(obs.isDirty());
	}
	
	/**
	 * @see Obs#setGroupMembers(Set)
	 */
	@Test
	public void setGroupMembers_shouldNotMarkTheExistingObsAsDirtyWhenTheSetIsChangedFromNullToANonEmptyOne() throws Exception {
		Obs obs = new Obs(5);
		assertNull(Obs.class.getDeclaredField("groupMembers").get(obs));
		Set<Obs> members = new HashSet<>();
		members.add(new Obs());
		obs.setGroupMembers(members);
		assertFalse(obs.isDirty());
	}

	/**
	 * @see Obs#setGroupMembers(Set)
	 */
	@Test
	public void setGroupMembers_shouldNotMarkNewObsAsDirtyWhenTheSetIsChangedFromNullToANonEmptyOne() throws Exception {
		Obs obs = new Obs();
		assertNull(Obs.class.getDeclaredField("groupMembers").get(obs));
		Set<Obs> members = new HashSet<>();
		members.add(new Obs());
		obs.setGroupMembers(members);
		assertFalse(obs.isDirty());
	}

	/**
	 * @see Obs#setGroupMembers(Set)
	 */
	@Test
	public void setGroupMembers_shouldNotMarkTheExistingObsAsDirtyWhenTheSetIsReplacedWithAnotherWithDifferentMembers()
	    throws Exception {
		Obs obs = new Obs(2);
		Set<Obs> members1 = new HashSet<>();
		members1.add(new Obs());
		obs.setGroupMembers(members1);
		resetObs(obs);
		Set<Obs> members2 = new HashSet<>();
		members2.add(new Obs());
		obs.setGroupMembers(members2);
		assertFalse(obs.isDirty());
	}

	/**
	 * @see Obs#setGroupMembers(Set)
	 */
	@Test
	public void setGroupMembers_shouldNotMarkTheNewObsAsDirtyWhenTheSetIsReplacedWithAnotherWithDifferentMembers()
			throws Exception {
		Obs obs = new Obs();
		Set<Obs> members1 = new HashSet<>();
		members1.add(new Obs());
		obs.setGroupMembers(members1);
		assertFalse(obs.isDirty());
		Set<Obs> members2 = new HashSet<>();
		members2.add(new Obs());
		obs.setGroupMembers(members2);
		assertFalse(obs.isDirty());
	}
	
	/**
	 * @see Obs#setGroupMembers(Set)
	 */
	@Test
	public void setGroupMembers_shouldNotMarkTheObsAsDirtyWhenTheSetIsChangedFromNullToAnEmptyOne() throws Exception {
		Obs obs = new Obs();
		assertNull(Obs.class.getDeclaredField("groupMembers").get(obs));
		obs.setGroupMembers(new HashSet<>());
		assertFalse(obs.isDirty());
	}
	
	/**
	 * @see Obs#setGroupMembers(Set)
	 */
	@Test
	public void setGroupMembers_shouldNotMarkTheObsAsDirtyWhenTheSetIsReplacedWithAnotherWithSameMembers() throws Exception {
		Obs obs = new Obs();
		Obs o = new Obs();
		Set<Obs> members1 = new HashSet<>();
		members1.add(o);
		obs.setGroupMembers(members1);
		resetObs(obs);
		Set<Obs> members2 = new HashSet<>();
		members2.add(o);
		obs.setGroupMembers(members2);
		assertFalse(obs.isDirty());
	}

	/**
	 * @see Obs#setObsDatetime(Date)
	 */
	@Test
	public void setObsDateTime_shouldNotMarkTheObsAsDirtyWhenDateIsNotChangedAndExistingValueIsOfTimeStampType(){
		Obs obs = new Obs();
		Date date = new Date();
		Timestamp timestamp = new Timestamp(date.getTime());
		obs.setObsDatetime(timestamp);
		obs.setId(1);
		assertFalse(obs.isDirty());

		obs.setObsDatetime(date);

		assertFalse(obs.isDirty());
	}
	
	@Test
	public void shouldSetFinalStatusOnNewObsByDefault() throws Exception {
		Obs obs = new Obs();
		assertThat(obs.getStatus(), is(Obs.Status.FINAL));
	}
	
	@Test
	public void newInstance_shouldCopyMostFields() throws Exception {
		Obs obs = new Obs();
		obs.setStatus(Obs.Status.PRELIMINARY);
		obs.setInterpretation(Obs.Interpretation.LOW);
		obs.setConcept(new Concept());
		obs.setValueNumeric(1.2);
		
		Obs copy = Obs.newInstance(obs);
		
		// these fields are not copied
		assertThat(copy.getObsId(), nullValue());
		assertThat(copy.getUuid(), not(obs.getUuid()));
		
		// other fields are copied
		assertThat(copy.getConcept(), is(obs.getConcept()));
		assertThat(copy.getValueNumeric(), is(obs.getValueNumeric()));
		assertThat(copy.getStatus(), is(obs.getStatus()));
		assertThat(copy.getInterpretation(), is(obs.getInterpretation()));
		// TODO test that the rest of the fields are set
	}
	
	@Test
	public void shouldSupportInterpretationProperty() throws Exception {
		Obs obs = new Obs();
		assertThat(obs.getInterpretation(), nullValue());

		obs.setInterpretation(Obs.Interpretation.NORMAL);
		assertThat(obs.getInterpretation(), is(Obs.Interpretation.NORMAL));
	}
	
	@Test
	public void setValueBoolean_shouldNotSetValueForNonBooleanConcept() throws Exception {
		Obs obs = createObs(2);
		ConceptDatatype dataType = new ConceptDatatype();
		dataType.setUuid(ConceptDatatype.CODED_UUID);
		obs.getConcept().setDatatype(dataType);
		assertNotNull(obs.getValueCoded());
		obs.setValueBoolean(null);
		assertNotNull(obs.getValueCoded());
	}
}
