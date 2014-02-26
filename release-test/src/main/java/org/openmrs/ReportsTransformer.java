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
