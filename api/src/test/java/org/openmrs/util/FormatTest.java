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

import java.util.Date;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

public class FormatTest {

    /**
     * @see Format#format(Date, Locale, Format.FORMAT_TYPE)
     */
    @Test
    public void format_shouldNotFailWhenAllParametersAreNull() {
        Assert.assertEquals("",Format.format(null, null, null));
    }

    /**
     * @see Format#format(Date, Locale, Format.FORMAT_TYPE)
     */
    @Test
    public void format_shouldNotFailWhenDateAndLocaleIsNull() {
        Assert.assertEquals("", Format.format(null, null, Format.FORMAT_TYPE.DATE));
    }

    /**
     * @see Format#format(Date, Locale, Format.FORMAT_TYPE)
     */
    @Test
    public void format_shouldNotFailWhenLocaleAndTypeIsNull() {
        Assert.assertEquals("", Format.format(new Date(1460323142), null, null));
    }

    /**
     * @see Format#format(Date, Locale, Format.FORMAT_TYPE)
     */
    @Test
    public void format_shouldNotFailWhenOnlyLocaleIsNull() {
        Assert.assertEquals("", Format.format(new Date(1460323142), null, Format.FORMAT_TYPE.DATE));
    }

    /**
     * @see Format#format(Date, Locale, Format.FORMAT_TYPE)
     */
    @Test
    public void format_shouldNotFailWhenDateAndTypeIsNull() {
        Assert.assertEquals("", Format.format(null, Locale.US, null));
    }

    /**
     * @see Format#format(Date, Locale, Format.FORMAT_TYPE)
     */
    @Test
    public void format_shouldNotFailWhenOnlyDateIsNull() {
        Assert.assertEquals("", Format.format(null, Locale.US, Format.FORMAT_TYPE.DATE));
    }

    /**
     * @see Format#format(Date, Locale, Format.FORMAT_TYPE)
     */
    @Test
    public void format_shouldNotFailWhenOnlyTypeIsNull() {
        Assert.assertEquals("", Format.format(new Date(1460323539000L), Locale.US, null));
    }

    /**
     * @see Format#format(Date, Locale, Format.FORMAT_TYPE)
     */
    @Test
    public void format_shouldNotFailWhenNoneOfTheParametersAreNull() {
        Assert.assertNotEquals("", Format.format(new Date(1460323539000L), Locale.US, Format.FORMAT_TYPE.DATE));
    }
}
