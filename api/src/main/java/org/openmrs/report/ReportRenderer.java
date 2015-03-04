/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.report;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;

/**
 * Takes a ReportSchema and renders it, often acting as a connector that delegates to a
 * sophisticated package like BIRT or Jasper Reports. Implementations of this class should only use
 * a no-arg constructor, since they will be instantiated by the ReportObjectService via reflection.
 * 
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public interface ReportRenderer {
	
	/**
	 * @return What should this renderer be called, e.g. in a drop-down list where the user picks
	 *         which renderer to use
	 */
	public String getLabel();
	
	/**
	 * Returns the modes in which this report schema could be rendered.
	 * 
	 * @param schema
	 * @return a <code>Collection<RenderingMode></code> of all modes in which the given ReportSchema
	 *         can be rendered
	 */
	public Collection<RenderingMode> getRenderingModes(ReportSchema schema);
	
	/**
	 * The content-type that will be rendered
	 * <p>
	 * Should be null if getLinkUrl() returns a non-null value.
	 * 
	 * @param schema The ReportSchema to render
	 * @param argument Argument from the RenderingMode that the user selected
	 * @return the <code>String</code> representation of the rendered content type
	 */
	public String getRenderedContentType(ReportSchema schema, String argument);
	
	/**
	 * Should be null if getLinkUrl() returns a non-null value.
	 * 
	 * @param schema
	 * @param argument Argument from the RenderingMode that the user selected
	 * @return Suggested filename to save the rendered report as.
	 */
	public String getFilename(ReportSchema schema, String argument);
	
	/**
	 * Render the report's data to a stream
	 * 
	 * @param reportData Data that was calculated by the Reporting API and service
	 * @param argument Argument from the RenderingMode that the user selected
	 * @param out
	 * @throws ReportRenderingException
	 */
	public void render(ReportData reportData, String argument, OutputStream out) throws IOException,
	        ReportRenderingException;
	
	/**
	 * Render the report's data to a stream
	 * 
	 * @param reportData Data that was calculated by the Reporting API and service
	 * @param argument Argument from the RenderingMode that the user selected
	 * @param writer the object to write the output to
	 * @throws ReportRenderingException
	 */
	public void render(ReportData reportData, String argument, Writer writer) throws IOException, ReportRenderingException;
}
