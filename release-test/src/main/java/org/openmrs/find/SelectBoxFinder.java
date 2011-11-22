package org.openmrs.find;

import org.hamcrest.Factory;
import org.openqa.selenium.lift.find.HtmlTagFinder;

public class SelectBoxFinder extends HtmlTagFinder {
	
	@Override
	protected String tagName() {
		return "select";
	}
	
	@Override
	protected String tagDescription() {
		return "select";
	}
	
	@Factory
	public static HtmlTagFinder selectbox() {
		return new SelectBoxFinder();
	}
	
}
