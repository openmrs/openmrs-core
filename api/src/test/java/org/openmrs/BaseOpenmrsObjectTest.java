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
package org.openmrs;

import static junit.framework.Assert.assertEquals;

import org.hibernate.Hibernate;
import org.junit.Assert;
import org.junit.Test;

public class BaseOpenmrsObjectTest {
	
	private static class BaseOpenmrsObjectMock extends BaseOpenmrsObject {
		
		@Override
		public Integer getId() {
			return null;
		}
		
		@Override
		public void setId(Integer id) {
		}
		
	}
	
	/**
	 * @see BaseOpenmrsObject#equals(Object)
	 * @verifies return false if given obj has null uuid
	 */
	@Test
	public void equals_shouldReturnFalseIfGivenObjHasNullUuid() throws Exception {
		//given
		BaseOpenmrsObject o = new BaseOpenmrsObjectMock();
		BaseOpenmrsObject obj = new BaseOpenmrsObjectMock();
		
		//when
		obj.setUuid(null);
		
		//then
		Assert.assertFalse(o.equals(obj));
	}
	
	/**
	 * @see BaseOpenmrsObject#equals(Object)
	 * @verifies return false if given obj is not instance of BaseOpenmrsObject
	 */
	@Test
	public void equals_shouldReturnFalseIfGivenObjIsNotInstanceOfBaseOpenmrsObject() throws Exception {
		//given
		BaseOpenmrsObject o = new BaseOpenmrsObjectMock();
		
		//when
		Object obj = new Object();
		
		//then
		Assert.assertFalse(o.equals(obj));
	}
	
	/**
	 * @see BaseOpenmrsObject#equals(Object)
	 * @verifies return false if given obj is null
	 */
	@Test
	public void equals_shouldReturnFalseIfGivenObjIsNull() throws Exception {
		//given
		BaseOpenmrsObject o = new BaseOpenmrsObjectMock();
		
		//when
		BaseOpenmrsObject obj = null;
		
		//then
		Assert.assertFalse(o.equals(obj));
	}
	
	/**
	 * @see BaseOpenmrsObject#equals(Object)
	 * @verifies return false if uuid is null
	 */
	@Test
	public void equals_shouldReturnFalseIfUuidIsNull() throws Exception {
		//given
		BaseOpenmrsObject o = new BaseOpenmrsObjectMock();
		BaseOpenmrsObject obj = new BaseOpenmrsObjectMock();
		
		//when
		o.setUuid(null);
		
		//then
		Assert.assertFalse(o.equals(obj));
	}
	
	/**
	 * @see BaseOpenmrsObject#equals(Object)
	 * @verifies return true if objects are the same
	 */
	@Test
	public void equals_shouldReturnTrueIfObjectsAreTheSame() throws Exception {
		//given
		BaseOpenmrsObject o = new BaseOpenmrsObjectMock();
		
		//when
		BaseOpenmrsObject obj = o;
		
		//then
		Assert.assertTrue(o.equals(obj));
	}
	
	/**
	 * @see BaseOpenmrsObject#equals(Object)
	 * @verifies return true if uuids are equal
	 */
	@Test
	public void equals_shouldReturnTrueIfUuidsAreEqual() throws Exception {
		//given
		BaseOpenmrsObject o = new BaseOpenmrsObjectMock();
		BaseOpenmrsObject obj = new BaseOpenmrsObjectMock();
		
		//when
		obj.setUuid(o.getUuid());
		
		//then
		Assert.assertTrue(o.equals(obj));
	}
	
	/**
	 * @see BaseOpenmrsObject#hashCode()
	 * @verifies not fail if uuid is null
	 */
	@Test
	public void hashCode_shouldNotFailIfUuidIsNull() throws Exception {
		//given
		BaseOpenmrsObject o = new BaseOpenmrsObjectMock();
		
		//when
		o.setUuid(null);
		
		//then
		o.hashCode();
	}
	
	@Test
	public void shouldNotBeEqualWhenDifferentClassesAndSameId() throws Exception {
		Encounter encounter = new Encounter(2);
		Order order = new Order(2);
		
		Assert.assertFalse(encounter.equals(order));
	}
	
	@Test
	public void shouldNotBeEqualWhenFirstIsNull() throws Exception {
		Encounter encounter = new Encounter(2);
		Assert.assertFalse(encounter.equals(null));
	}
	
	/**
	 * @see BaseOpenmrsObject#toString()
	 * @verifies include uuid if not null
	 */
	@Test
	public void toString_shouldIncludeUuidIfNotNull() throws Exception {
		BaseOpenmrsObject o = new BaseOpenmrsObjectMock();
		
		assertEquals("BaseOpenmrsObjectTest.BaseOpenmrsObjectMock[hashCode=" + Integer.toHexString(o.hashCode()) + ",uuid="
		        + o.getUuid() + "]", o.toString());
	}
	
	/**
	 * @see BaseOpenmrsObject#toString()
	 * @verifies include hashCode if uuid is null
	 */
	@Test
	public void toString_shouldIncludeHashCodeIfUuidIsNull() throws Exception {
		//given
		BaseOpenmrsObject o = new BaseOpenmrsObjectMock();
		o.setUuid(null);
		
		//when
		//then
		assertEquals("BaseOpenmrsObjectTest.BaseOpenmrsObjectMock[hashCode=" + Integer.toHexString(o.hashCode())
		        + ",uuid=<null>]", o.toString());
	}
	
	@Test
	public void equals_shouldReturnTrueIfGivenObjectIsInstanceOfSuperOrExtendingClass() throws Exception {
		Concept concept = new Concept(5);
		Concept numeric = new ConceptNumeric();
		numeric.setUuid(concept.getUuid());
		Assert.assertTrue(numeric.equals(concept));
		Assert.assertTrue(concept.equals(numeric));
	}
	
	@Test
	public void equals_shouldReturnFalseIfGivenObjIsNotInstanceOfSuperOrExtendingClass() throws Exception {
		Encounter encounter = new Encounter();
		Concept concept = new Concept(5);
		concept.setUuid(encounter.getUuid());
		Assert.assertFalse(encounter.equals(concept));
		Assert.assertFalse(concept.equals(encounter));
	}
	
	@Test
	public void equals_shouldReturnTrueIfGivenObjectIsSubclassOfSuperOrExtendingClass() throws Exception {
		Order order = new Order(21);
		DrugOrder type = new DrugOrder(21);
		type.setUuid(order.getUuid());
		Assert.assertTrue(type.equals(order));
		Assert.assertTrue(order.equals(type));
	}
	
	@Test
	public void equals_shouldReturnFalseIfGivenObjectIsNotSubclassOfSuperOrExtendingClass() throws Exception {
		Order order = new Order(21);
		OrderFrequency type = new OrderFrequency();
		type.setUuid(order.getUuid());
		Assert.assertFalse(type.equals(order));
		Assert.assertFalse(order.equals(type));
	}
	
	@Test
	public void equals_shouldReturnfalseIfHibernateProxyOfOneThingIsComparedtoHibernateProxyofSomething() throws Exception {
		Object obj = new  Concept();
		String uuid=((BaseOpenmrsObject) obj).getUuid();
		Class<?> thisClass = Hibernate.getClass(obj);
		Object obj1 = new  Order();
		((BaseOpenmrsObject) obj1).setUuid(uuid);		
		Class<?> objClass = Hibernate.getClass(obj1);
		Assert.assertNotEquals(thisClass, objClass);
	}
	
	@Test
	public void equals_shouldReturnFalseIfHibernateProxyOfOneThingIsComparedtoNonHibernateProxyofSomething()
	        throws Exception {	
		Object obj = new  Concept();
		String uuid=((BaseOpenmrsObject) obj).getUuid();
		Class<?> thisClass = Hibernate.getClass(obj);		
		ConceptSet obj1 = new  ConceptSet();
		((BaseOpenmrsObject) obj1).setUuid(uuid);	
		Assert.assertNotEquals(thisClass, obj1);
	}
}
