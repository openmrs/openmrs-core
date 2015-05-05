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

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ReportsTransformer {

    private static File cd = new File(ReportsTransformer.class.getProtectionDomain().getCodeSource().getLocation().getFile())
            .getParentFile().getParentFile();


    public static void main(String args[]) throws TransformerException, FileNotFoundException {
        System.out.println("TRANSFORMING JBEHAVE XML TO JUNIT COMPATIBLE XML.");
        System.out.println("LOOKING FOR XML FILES AT "+cd.getPath()+"/target/jbehave");
        List<File> jbehaveReports = getXMLFiles(new File(cd, "/target/jbehave/"));
        for(File report : jbehaveReports){
            tranformReport(report);
        }

    }

    public static void tranformReport(File xmlPath) throws TransformerFactoryConfigurationError,
            TransformerException, FileNotFoundException {
        Source xml = new StreamSource(xmlPath);
        Source xslt = new StreamSource(new File(cd,
                "src/main/resources/reports/jbehave-3.x-to-junit-1.0.xsl"));
        String xmlName = xmlPath.getName();

        if(!xmlName.contains("AfterStories") && !xmlName.contains("BeforeStories")){
            System.out.println("PROCESSING XML::"+xmlName);
            String resultOutputPath = cd + "/target/jbehave/TEST" + xmlName;
            Result resultOutput = new StreamResult(new FileOutputStream(resultOutputPath));
            DOMResult result = new DOMResult();
            Transformer transformer = TransformerFactory.newInstance().newTransformer(xslt);
            transformer.transform(xml, result);
            transformer.transform(xml, resultOutput);
        }
        
    }

    public static List<File> getXMLFiles(File folder) {
        List<File> xmlFiles = new ArrayList<File>();

        File[] files = folder.listFiles();
        if(files != null){
            System.out.println("FOUND "+files.length+" FILES AT "+folder.getPath());
            for (File file : files) {
                String fileName = file.getName();
                if (file.isFile() && (fileName.endsWith(".xml") || fileName.endsWith(".XML"))) {
                    xmlFiles.add(file);
                }
            }
            System.out.println("FOUND "+xmlFiles.size()+" XMLs at"+folder.getPath());
        }
        return xmlFiles;
    }

}
