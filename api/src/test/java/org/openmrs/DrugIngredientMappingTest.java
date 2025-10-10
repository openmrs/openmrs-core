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

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.hibernate.SessionFactory;

import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;



public class DrugIngredientMappingTest extends BaseContextSensitiveTest {
	  
    @Test
    public void shouldSetAndGetDrug() {
        Drug drug = new Drug();
        DrugIngredient di = new DrugIngredient();
        di.setDrug(drug);
        assertEquals(drug, di.getDrug());
    }

    @Test
    public void shouldSetAndGetIngredient() {
        Concept ingredient = new Concept();
        DrugIngredient di = new DrugIngredient();
        di.setIngredient(ingredient);
        assertEquals(ingredient, di.getIngredient());
    }

    @Test
    public void shouldSetAndGetStrength() {
        Double strength = 10.5;
        DrugIngredient di = new DrugIngredient();
        di.setStrength(strength);
        assertEquals(strength, di.getStrength());
    }

    @Test
    public void shouldSetAndGetUnits() {
        Concept units = new Concept();
        DrugIngredient di = new DrugIngredient();
        di.setUnits(units);
        assertEquals(units, di.getUnits());
    }

    @Test
    public void getId_shouldThrowUnsupportedOperationException() {
        DrugIngredient di = new DrugIngredient();
        assertThrows(UnsupportedOperationException.class, di::getId);
    }

    @Test
    public void setId_shouldThrowUnsupportedOperationException() {
        DrugIngredient di = new DrugIngredient();
        assertThrows(UnsupportedOperationException.class, () -> di.setId(123));
    }
}
