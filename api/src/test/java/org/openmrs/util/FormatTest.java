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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Date;
import java.util.Locale;

import org.junit.jupiter.api.Test;

public class FormatTest {

    /**
     * @see Format#format(Date, Locale, Format.FORMAT_TYPE)
     */
    @Test
    public void format_shouldNotFailWhenAllParametersAreNull() {
        assertEquals("",Format.format(null, null, null));
    }

    /**
     * @see Format#format(Date, Locale, Format.FORMAT_TYPE)
     */
    @Test
    public void format_shouldNotFailWhenDateAndLocaleIsNull() {
        assertEquals("", Format.format(null, null, Format.FORMAT_TYPE.DATE));
    }

    /**
     * @see Format#format(Date, Locale, Format.FORMAT_TYPE)
     */
    @Test
    public void format_shouldNotFailWhenLocaleAndTypeIsNull() {
        assertEquals("", Format.format(new Date(1460323142), null, null));
    }

    /**
     * @see Format#format(Date, Locale, Format.FORMAT_TYPE)
     */
    @Test
    public void format_shouldNotFailWhenOnlyLocaleIsNull() {
        assertEquals("", Format.format(new Date(1460323142), null, Format.FORMAT_TYPE.DATE));
    }

    /**
     * @see Format#format(Date, Locale, Format.FORMAT_TYPE)
     */
    @Test
    public void format_shouldNotFailWhenDateAndTypeIsNull() {
        assertEquals("", Format.format(null, Locale.US, null));
    }

    /**
     * @see Format#format(Date, Locale, Format.FORMAT_TYPE)
     */
    @Test
    public void format_shouldNotFailWhenOnlyDateIsNull() {
        assertEquals("", Format.format(null, Locale.US, Format.FORMAT_TYPE.DATE));
    }

    /**
     * @see Format#format(Date, Locale, Format.FORMAT_TYPE)
     */
    @Test
    public void format_shouldNotFailWhenOnlyTypeIsNull() {
        assertEquals("", Format.format(new Date(1460323539000L), Locale.US, null));
    }

    /**
     * @see Format#format(Date, Locale, Format.FORMAT_TYPE)
     */
    @Test
    public void format_shouldNotFailWhenNoneOfTheParametersAreNull() {
        assertNotEquals("", Format.format(new Date(1460323539000L), Locale.US, Format.FORMAT_TYPE.DATE));
    }
}
