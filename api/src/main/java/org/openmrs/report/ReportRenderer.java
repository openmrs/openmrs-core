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
