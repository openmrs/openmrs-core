/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.layout;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 1.12
 */
public abstract class LayoutSupport<T extends LayoutTemplate> {

	private static final Logger log = LoggerFactory.getLogger(LayoutSupport.class);

	/**
	 * Bumped whenever the tokens change, so that templates can detect that their memoized tokenization
	 * is stale with a single field read rather than by re-resolving this support.
	 */
	private final AtomicInteger configurationVersion = new AtomicInteger(0);

	protected String defaultLayoutFormat;

	protected List<T> layoutTemplates;

	protected List<String> specialTokens;

	/**
	 * @return Returns the layoutTemplates.
	 */
	public List<T> getLayoutTemplates() {
		return layoutTemplates;
	}

	/**
	 * @param layoutTemplates The layoutTemplates to set.
	 */
	public void setLayoutTemplates(List<T> layoutTemplates) {
		this.layoutTemplates = layoutTemplates;
	}

	/**
	 * @return Returns the defaultLayoutTemplate.
	 */
	public T getDefaultLayoutTemplate() {
		return getLayoutTemplateByName(getDefaultLayoutFormat());
	}

	public T getLayoutTemplateByName(String templateName) {
		log.debug("looking for template name: " + templateName);

		if (this.layoutTemplates != null && templateName != null) {
			T ret = null;

			for (T at : this.layoutTemplates) {
				if (at != null && (templateName.equalsIgnoreCase(at.getDisplayName())
				        || templateName.equalsIgnoreCase(at.getCodeName())
				        || templateName.equalsIgnoreCase(at.getCountry()))) {
					ret = at;
					log.debug("Found Layout Template named " + at.getDisplayName());
				}
			}

			return ret;
		} else {
			log.debug("No Layout Templates defined");
			return null;
		}
	}

	public T getLayoutTemplateByCodeName(String templateName) {
		return findLayoutTemplate(templateName, LayoutTemplate::getCodeName);
	}

	public T getLayoutTemplateByCountry(String templateName) {
		return findLayoutTemplate(templateName, LayoutTemplate::getCountry);
	}

	public T getLayoutTemplateByDisplayName(String templateName) {
		return findLayoutTemplate(templateName, LayoutTemplate::getDisplayName);
	}

	private T findLayoutTemplate(String templateName, Function<T, String> valueExtractor) {
		if (this.layoutTemplates != null && templateName != null) {
			T ret = null;

			for (T at : this.layoutTemplates) {
				if (at != null && templateName.equalsIgnoreCase(valueExtractor.apply(at))) {
					ret = at;
					log.debug("Found Layout Template named " + at.getDisplayName());
				}
			}

			return ret;
		} else {
			log.debug("No Layout Templates defined");
			return null;
		}
	}

	/**
	 * @return Returns the specialTokens.
	 */
	public List<String> getSpecialTokens() {
		return specialTokens;
	}

	/**
	 * @param specialTokens The specialTokens to set.
	 */
	public void setSpecialTokens(List<String> specialTokens) {
		this.specialTokens = specialTokens;
		configurationVersion.incrementAndGet();
	}

	/**
	 * @return Returns the defaultLayoutFormat.
	 */
	public abstract String getDefaultLayoutFormat();

	/**
	 * @param defaultLayoutFormat The defaultLayoutFormat to set.
	 */
	public void setDefaultLayoutFormat(String defaultLayoutFormat) {
		this.defaultLayoutFormat = defaultLayoutFormat;
	}

	/**
	 * @return the current special token configuration version, for use by
	 *         {@link LayoutTemplate#getLines()} in deciding whether its cache is still valid
	 * @since 2.8.10
	 */
	int getConfigurationVersion() {
		return configurationVersion.get();
	}
}
