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
package org.openmrs.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openmrs.Cohort;
import org.openmrs.report.EvaluationContext;
import org.openmrs.report.RenderingMode;
import org.openmrs.report.ReportData;
import org.openmrs.report.ReportRenderer;
import org.openmrs.report.ReportSchema;
import org.openmrs.report.ReportSchemaXml;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 */
@Transactional
public interface ReportService {

	/**
	 * 
	 * Auto generated method comment
	 * 
	 * @return
	 */
	public ReportData evaluate(ReportSchema reportSchema, Cohort inputCohort,
	        EvaluationContext context);


	/**
	 * 
	 * Auto generated method comment
	 * 
	 * @return
	 */
	@Transactional(readOnly=true)
	public List<ReportSchema> getReportSchemas();

	/**
	 * 
	 * Auto generated method comment
	 * 
	 * @return
	 */
	@Transactional(readOnly=true)
	public ReportSchema getReportSchema(Integer reportSchemaId);
	
	/**
	 * Returns a ReportSchema object from a ReportSchemaXml definition
	 * De-serialized the xml definition, applies macro definitions, and 
	 * returns an expanded report schema object
	 * @param reportSchemaXml - the ReportSchemaXml to use to return a ReportSchema instance
	 * @return ReportSchema
	 * @throws Exception if conversion fails
	 */
	public ReportSchema getReportSchema(ReportSchemaXml reportSchemaXml);

	/**
	 * Auto generated method comment
	 * 
	 * @param reportSchema
	 */
	public void saveReportSchema(ReportSchema reportSchema);

	/**
	 * Auto generated method comment
	 * 
	 * @param reportSchema
	 */
	public void deleteReportSchema(ReportSchema reportSchema);

	/**
	 * @return All registered report renderers
	 */
	@Transactional(readOnly=true)
	public Collection<ReportRenderer> getReportRenderers();

	/**
	 * @return all rendering modes for the given schema, in their preferred order
	 */
	@Transactional(readOnly=true)
	public List<RenderingMode> getRenderingModes(ReportSchema schema);

	/**
	 * Auto generated method comment
	 * 
	 * @param key
	 * @return
	 */
	@Transactional(readOnly=true)
	public ReportRenderer getReportRenderer(Class<? extends ReportRenderer> clazz);
	
	/**
	 * Get the report renderer 
	 * 
	 * @param key
	 * @return
	 */
	@Transactional(readOnly=true)
	public ReportRenderer getReportRenderer(String className);

	/**
	 * Add the given map to this service's renderers
	 * 
	 * This map is set via spring, see the applicationContext-service.xml file
	 * 
	 * @param renderers Map of class to renderer object
	 */
	public void setRenderers(Map<Class<? extends ReportRenderer>, ReportRenderer> renderers) throws APIException;
	
	/**
	 * Gets the renderers map registered to this report service
	 * 
	 * @return
	 * @throws APIException
	 */
	public Map<Class<? extends ReportRenderer>, ReportRenderer> getRenderers() throws APIException;
	
	/**
	 * Registers the given renderer with the service
	 * 
	 * @param rendererClass
	 * @param renderer
	 * @throws APIException
	 */
	public void registerRenderer(Class<? extends ReportRenderer> rendererClass, ReportRenderer renderer) throws APIException;
	
	/**
	 * Convenience method for {@link #registerRenderer(Class, ReportRenderer)}
	 * 
	 * @param rendererClass
	 * @throws APIException
	 */
	public void registerRenderer(String rendererClass) throws APIException;
	
	/**
	 * Remove the renderer associated with <code>rendererClass</code> from the
	 * list of available renderers
	 * 
	 * @param rendererClass
	 */
	public void removeRenderer(Class<? extends ReportRenderer> rendererClass) throws APIException;

	/**
	 * Get the xmlified ReportSchema object that was saved previously
	 * 
	 * @return ReportSchemaXml object that is associated with the given id
	 */
	@Transactional(readOnly=true)
	public ReportSchemaXml getReportSchemaXml(Integer reportSchemaXmlId);

	/**
	 * Create a new ReportSchemaXml object in the database.
	 * 
	 * @param reportSchemaXml xml to save
	 */
	public void createReportSchemaXml(ReportSchemaXml reportSchemaXml);
	
	/**
	 * Update the given ReportSchemaXml object in the database.
	 * 
	 * @param reportSchemaXml xml to save
	 */
	public void updateReportSchemaXml(ReportSchemaXml reportSchemaXml);
	

	/**
	 * Delete the given ReportSchemaXml class from the db
	 */
	public void deleteReportSchemaXml(ReportSchemaXml reportSchemaXml);
	
	/**
	 * Get all saved ReportSchemaXml objects in the db
	 * 
	 * @return List of ReportSchemaXml objects 
	 */
	public List<ReportSchemaXml> getReportSchemaXmls();
	

	/**
	 * Gets the macros that will be used when deserializing ReportSchemaXML 
	 * 
	 * @return macros
	 */
	public Properties getReportXmlMacros();
	
	/**
	 * Saves the macros that will be used when deserializing ReportSchemaXML
	 * 
	 * @param macros the macros to set
	 */
	public void saveReportXmlMacros(Properties macros);
	
	/**
	 * Applies the report xml macros to the input, and returns it.
	 * 
	 * @param input The text (presumably a report schema xml definition) that you want to apply macros to 
	 * @return the result of applying macro substitutions to input 
	 */
	public String applyReportXmlMacros(String input);

}