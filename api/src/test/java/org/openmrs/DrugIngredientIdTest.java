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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;



public class DrugIngredientIdTest {

    @Test
    public void shouldSetAndGetDrugId() {
        DrugIngredientId id = new DrugIngredientId();
        id.setDrugId(123);
        assertEquals(123, id.getDrugId());
    }

    @Test
    public void shouldSetAndGetIngredientId() {
        DrugIngredientId id = new DrugIngredientId();
        id.setIngredientId(456);
        assertEquals(456, id.getIngredientId());
    }

    @Test
    public void shouldInitializeFieldsViaConstructor() {
        DrugIngredientId id = new DrugIngredientId(111, 222);
        assertEquals(111, id.getDrugId());
        assertEquals(222, id.getIngredientId());
    }

    @Test
    public void shouldBeEqualIfFieldsAreSame() {
        DrugIngredientId id1 = new DrugIngredientId(1, 2);
        DrugIngredientId id2 = new DrugIngredientId(1, 2);
        assertEquals(id1, id2);
    }

    @Test
    public void shouldNotBeEqualIfFieldsAreDifferent() {
        DrugIngredientId id1 = new DrugIngredientId(1, 2);
        DrugIngredientId id2 = new DrugIngredientId(1, 3);
        assertNotEquals(id1, id2);
    }

    @Test
    public void shouldHaveSameHashCodeIfEqual() {
        DrugIngredientId id1 = new DrugIngredientId(10, 20);
        DrugIngredientId id2 = new DrugIngredientId(10, 20);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    public void shouldNotBeEqualToNullOrDifferentType() {
        DrugIngredientId id = new DrugIngredientId(1, 2);
        assertNotEquals(null, id);
        assertNotEquals("some string", id);
    }
}

