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

import com.mysql.management.MysqldResource;
import com.mysql.management.MysqldResourceI;
import com.mysql.management.util.QueryUtil;
import org.openmrs.util.OpenmrsUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

import static org.openmrs.TestUtils.prepareJdbcConnectionUrl;

public class MysqlFactory {
    public static final String DRIVER = "com.mysql.jdbc.Driver";

    public static void main(String[] args) throws Exception {
        File databaseDir = new File(args[0]);
        File dataDir = new File(args[1]);
        String port = System.getProperty("mysql_port", "3336");
        String userName = System.getProperty("mysql_username", "root");
        String password = System.getProperty("mysql_password", "password");
        String databaseName = System.getProperty("database_name", "openmrsReleaseTest");
        if(System.getProperty("smoke-test") != null || System.getProperty("start-test-server")!=null){
            System.out.println("Starting Database server ");
            startDatabaseServer(databaseDir,dataDir, port, userName, password);
        }else{
        deleteOldRunTimePropertiesFile();
        startDatabaseServer(databaseDir, dataDir, port, userName, password);
        dropDatabaseIfExists(port, userName, password, databaseName);
        }
    }

    private static void dropDatabaseIfExists(String port, String userName, String password, String databaseName) throws Exception {
        Class.forName(DRIVER);
        Connection connection = null;
        try {
            String jdbcUrl = prepareJdbcConnectionUrl(port, databaseName);
            connection = DriverManager.getConnection(jdbcUrl, userName, password);
            String sql = "DROP DATABASE IF EXISTS " + databaseName+"";
            int status = new QueryUtil(connection).executeUpdate(sql);

            System.out.println("------------------------");
            System.out.println(sql);
            System.out.println("------------------------");
            System.out.println("status of drop database "+status);
            System.out.println("------------------------");
            System.out.flush();
            Thread.sleep(100); // wait for System.out to finish flush
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                throw e;
            }
        }
    }

    private static void deleteOldRunTimePropertiesFile() {
        String fileNameInTestMode = OpenmrsUtil.getApplicationDataDirectory() + OpenmrsUtil.getRuntimePropertiesFileNameInTestMode();
        System.out.println("Deleting the file " + fileNameInTestMode);
        if (fileNameInTestMode != null) {
            File runtimePropertiesFile = new File(fileNameInTestMode);
            runtimePropertiesFile.delete();
        }
    }


    public static MysqldResource startDatabaseServer(File databaseDir, File dataDir, String port,
                                                     String userName, String password) throws InterruptedException {
        Thread.sleep(1000);
        MysqldResource mysqldResource = new MysqldResource(databaseDir);

        Map database_options = new HashMap();
        database_options.put(MysqldResourceI.PORT, Integer.toString(Integer.parseInt(port)));
        database_options.put(MysqldResourceI.INITIALIZE_USER, "true");
        database_options.put(MysqldResourceI.INITIALIZE_USER_NAME, userName);
        database_options.put(MysqldResourceI.INITIALIZE_PASSWORD, password);

        stopDatabaseServer(mysqldResource);
        mysqldResource.start("openmrs-release-test", database_options);

        if (!mysqldResource.isRunning()) {
            throw new RuntimeException("MySQL did not start.");
        }

        System.out.println("MySQL is running.");

        return mysqldResource;
    }

    private static void stopDatabaseServer(MysqldResource mysqldResource) throws InterruptedException {
        if(mysqldResource.isRunning()){
            System.out.println("STOPPING EMBEDDED MYSQL!!");
            mysqldResource.shutdown();
            Thread.sleep(1000);
            System.out.println("SHUTTING DOWN MYSQL COMPLETED SUCCESFULLY!!");
        }
    }
}
