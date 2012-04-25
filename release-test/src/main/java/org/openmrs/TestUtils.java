package org.openmrs;

/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p/>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p/>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
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
