/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.steps;

import org.apache.commons.io.FileUtils;
import org.jbehave.core.annotations.AfterStories;
import org.openmrs.Steps;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

// Universal steps that should be included in all stories
public class UniversalSteps extends Steps {

	private File outputDirectory;

	public UniversalSteps(File outputDirectory, WebDriver driver) {
		super(driver);
		this.outputDirectory = outputDirectory;
	}

	@AfterStories
	public void closeDriver() {
		try {
			getWebDriver().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterStories
	public void copyFilesOver() throws URISyntaxException, IOException {
		try {
			String style = "reports/style";
			File viewDirectory = createDirectoryIfDoesNotExist("view");
			URL styleResource = getClass().getClassLoader().getResource(style);

			if (styleResource != null) {
				FileUtils.copyDirectoryToDirectory(new File(styleResource.toURI()), viewDirectory);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public File createDirectoryIfDoesNotExist(String name) {
		File directory = new File(outputDirectory, name);
		if (! directory.exists()) {
			directory.mkdirs();
		}
		return directory;
	}

}
