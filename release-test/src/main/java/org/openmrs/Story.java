/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.openmrs.steps.UniversalSteps;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class Story extends JUnitStory {

	protected WebDriver driver;

	public Story() {
		super();
	}

    /**
     * Override this in your story to provide steps files.
     */
    abstract public List<Steps> includeSteps();

    @Override
    public Embedder configuredEmbedder() {
        Embedder embedder = super.configuredEmbedder();
        embedder.useEmbedderControls(new EmbedderControls().doIgnoreFailureInStories(true).useStoryTimeoutInSecs(1200));
        return embedder;
    }

    @Override
    public List<CandidateSteps> candidateSteps() {
        File outputDirectory = createStoryReporter().outputDirectory();
        List<Steps> steps = new ArrayList<Steps>(includeSteps());
        steps.add(new UniversalSteps(outputDirectory, driver));

        return new InstanceStepsFactory(configuration(), toArray(steps)).createCandidateSteps();
    }

    private Object[] toArray(List<Steps> steps) {
        return steps.toArray(new Steps[0]);
    }

    @Override
	public Configuration configuration() {

        return new MostUsefulConfiguration().useStoryLoader(
                new LoadFromClasspath(this.getClass().getClassLoader()))
				.useStoryReporterBuilder(createStoryReporter());
	}

    private StoryReporterBuilder createStoryReporter() {
        return new StoryReporterBuilder().withDefaultFormats().withFormats(Format.CONSOLE, Format.XML);
    }

    @Override
	public void run() throws Throwable {
		createDriver();
		super.run();
	}
	
	private void createDriver() {
		this.driver = new FirefoxDriver();
        this.driver.manage().window().setSize(new Dimension(1920, 1200));
		// Use this driver if using Firefox 5.  There is currently a bug in selenium with ff5
		//this.driver = new ChromeDriver();
	}
}