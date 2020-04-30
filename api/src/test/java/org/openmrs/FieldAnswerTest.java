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

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;


/**
 * Tests the {@link FieldAnswer} object
 */
public class FieldAnswerTest {

     FieldAnswer fieldAnswer;
     
     @Before
     public void setup() {
          fieldAnswer = new FieldAnswer();
     }
     /**
	   * @see FieldAnswer#setClean()
      * @see FieldAnswer#getDirty()
	   */
     @Test
     public void shouldReturnClean(){
          fieldAnswer.setClean();
          Assert.assertEquals(fieldAnswer.getDirty(), false);
     }
     
     /**
      * @see FieldAnswer#getDirty()
      * @see FieldAnswer#setConcept(Concept)
      * @see FieldAnswer#getConcept()
	   */
     @Test
     public void shouldSetConcept(){
         Concept concept = new Concept();
         fieldAnswer.setConcept(concept);
         Assert.assertTrue(fieldAnswer.getDirty());
         Assert.assertEquals(fieldAnswer.getConcept(), concept);
     }
     
     /**
      * @see FieldAnswer#setConcept(Concept)
      * @see FieldAnswer#getConcept()
      */
     @Test
     public void shouldGetConcept(){
         Concept concept = new Concept();
         fieldAnswer.setConcept(concept);
         Assert.assertEquals(fieldAnswer.getConcept(), concept);
     }
     
     /**
      * @see FieldAnswer#getDirty()
      * @see FieldAnswer#setCreator(User)
      * @see FieldAnswer#getCreator()
      */
     @Test
     public void shouldSetCreator(){
          User newUser = new User();
          fieldAnswer.setCreator(newUser);
          Assert.assertTrue(fieldAnswer.getDirty());
          Assert.assertEquals(fieldAnswer.getCreator(), newUser);
     }
     
     /**
      * @see FieldAnswer#setCreator(User)
      * @see FieldAnswer#getCreator()
      */
     @Test
     public void shouldGetCreator(){
          User newUser = new User();
          fieldAnswer.setCreator(newUser);
          Assert.assertEquals(fieldAnswer.getCreator(), newUser);
     }
      
     /**
      * @see FieldAnswer#setDateCreated(Date)
      * @see FieldAnswer#getDirty()
      * @see FieldAnswer#getDateCreated()
      */
     @Test
     public void shouldSetDate(){
          Date date = new Date();
          fieldAnswer.setDateCreated(date);
          Assert.assertEquals(fieldAnswer.getDirty(), true);
          Assert.assertEquals(fieldAnswer.getDateCreated(), date);
     }
     
     /**
      * @see FieldAnswer#setDateCreated(Date)
      * @see FieldAnswer#getDateCreated()
      */
     @Test
     public void shouldGetDate(){
          Date date = new Date();
          fieldAnswer.setDateCreated(date);
          Assert.assertEquals(fieldAnswer.getDateCreated(), date);
     }
     
     /**
      * @see FieldAnswer#setField(Field)
      * @see FieldAnswer#getDirty()
      * @see FieldAnswer#getField()
      */
     @Test
     public void shouldSetField(){
          Field newField = new Field();
          fieldAnswer.setField(newField);
          Assert.assertTrue(fieldAnswer.getDirty());
          Assert.assertEquals(fieldAnswer.getField(), newField);
     }
     
     /**
      * @see FieldAnswer#setField(Field)
      * @see FieldAnswer#getField()
      */
     @Test
     public void shouldGetField(){
          Field newField = new Field();
          fieldAnswer.setField(newField);
          Assert.assertEquals(fieldAnswer.getField(), newField);
     }
     
}