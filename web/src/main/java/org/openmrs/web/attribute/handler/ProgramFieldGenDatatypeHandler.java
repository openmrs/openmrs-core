/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.attribute.handler;

import org.openmrs.Program;
import org.openmrs.customdatatype.datatype.ProgramDatatype;

import java.util.Map;

/**
 * Handler for the Program custom datatype
 *
 * @Component
 * @since 1.12
 */
public class ProgramFieldGenDatatypeHandler extends BaseMetadataFieldGenDatatypeHandler<ProgramDatatype, Program> {

    /**
     * @see org.openmrs.customdatatype.CustomDatatypeHandler#setHandlerConfiguration(java.lang.String)
     */
    @Override
    public void setHandlerConfiguration(String handlerConfig) {
        // not used
    }

    /**
     * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getWidgetName()
     */
    @Override
    public String getWidgetName() {
        return "org.openmrs.Program";
    }

    /**
     * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getWidgetConfiguration()
     */
    @Override
    public Map<String, Object> getWidgetConfiguration() {
        return null;
    }
}
