package org.openmrs;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.StoryReporterBuilder.Format;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public abstract class Story extends JUnitStory {

	protected WebDriver driver;

	public Story() {
		super();
	}

	@Override
	public Configuration configuration() {
		return new MostUsefulConfiguration().useStoryLoader(
				new LoadFromClasspath(this.getClass().getClassLoader()))
				.useStoryReporterBuilder(
						new StoryReporterBuilder().withDefaultFormats()
								.withFormats(Format.TXT, Format.CONSOLE));
	}

	@Override
	public void run() throws Throwable {
		createDriver();
		super.run();
		closeDriver();
	}
	
	private void createDriver() {
	    this.driver = new FirefoxDriver();
	}

	private void closeDriver() {
	    this.driver.close();
	}

}