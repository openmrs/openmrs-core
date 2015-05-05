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

public class TestUtils {
    public static String prepareJdbcConnectionUrl(String port, String databaseName) {
        return "jdbc:mysql://localhost:" + port + "/"+databaseName+"?"
                + "autoReconnect=true"
                + "&sessionVariables=storage_engine=InnoDB"
                + "&useUnicode=true&characterEncoding=UTF-8"
                + "&server.basedir=target/database&server.datadir=target/database/data"
                + "&server.collation-server=utf8_general_ci&server.character-set-server=utf8";
    }
}
