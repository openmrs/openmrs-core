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
package org.openmrs.report.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.DataSetService;
import org.openmrs.api.ReportService;
import org.openmrs.api.context.Context;
import org.openmrs.report.DataSet;
import org.openmrs.report.DataSetDefinition;
import org.openmrs.report.EvaluationContext;
import org.openmrs.report.RenderingMode;
import org.openmrs.report.ReportData;
import org.openmrs.report.ReportRenderer;
import org.openmrs.report.ReportSchema;
import org.openmrs.report.ReportSchemaXml;
import org.openmrs.report.db.ReportDAO;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.simpleframework.xml.Serializer;

/**
 * Methods specific to objects in the report package. These methods render reports or save them to
 * the database
 * 
 * @see org.openmrs.api.ReportService
 * @see org.openmrs.api.context.Context
 */
public class ReportServiceImpl implements ReportService {
	
	public Log log = LogFactory.getLog(this.getClass());
	
	private ReportDAO dao = null;
	
	/**
	 * Report renderers that have been registered. This is filled via {@link #setRenderers(Map)} and
	 * spring's applicationContext-service.xml object
	 */
	private static Map<Class<? extends ReportRenderer>, ReportRenderer> renderers = null;
	
	/**
	 * Default constructor
	 */
	public ReportServiceImpl() {
	}
	
	/**
	 * Method used by Spring injection to set the ReportDAO implementation to use in this service
	 * 
	 * @param dao The ReportDAO to use in this service
	 */
	public void setReportDAO(ReportDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.api.ReportService#deleteReportSchema(org.openmrs.report.ReportSchema)
	 */
	public void deleteReportSchema(ReportSchema reportSchema) {
		throw new APIException("Not Yet Implemented");
	}
	
	/**
	 * @see org.openmrs.api.ReportService#evaluate(org.openmrs.report.ReportSchema,
	 *      org.openmrs.Cohort, org.openmrs.report.EvaluationContext)
	 */
	@SuppressWarnings("unchecked")
	public ReportData evaluate(ReportSchema reportSchema, Cohort inputCohort, EvaluationContext evalContext) {
		ReportData ret = new ReportData();
		Map<String, DataSet> data = new HashMap<String, DataSet>();
		ret.setDataSets(data);
		ret.setReportSchema(reportSchema);
		ret.setEvaluationContext(evalContext);
		DataSetService dss = Context.getDataSetService();
		
		if (reportSchema.getDataSetDefinitions() != null)
			for (DataSetDefinition dataSetDefinition : reportSchema.getDataSetDefinitions()) {
				data.put(dataSetDefinition.getName(), dss.evaluate(dataSetDefinition, inputCohort, evalContext));
			}
		
		return ret;
	}
	
	/**
	 * @see org.openmrs.api.ReportService#getReportRenderer(java.lang.String)
	 */
	public ReportRenderer getReportRenderer(Class<? extends ReportRenderer> clazz) {
		try {
			return renderers.get(clazz);
		}
		catch (Exception ex) {
			log.error("Failed to get report renderer for " + clazz, ex);
			return null;
		}
	}
	
	/**
	 * @see org.openmrs.api.ReportService#getReportRenderer(java.lang.String)
	 */
	public ReportRenderer getReportRenderer(String className) {
		try {
			return renderers.get(OpenmrsClassLoader.getInstance().loadClass(className));
		}
		catch (Exception ex) {
			log.error("Failed to get report renderer for " + className, ex);
			return null;
		}
	}
	
	/**
	 * @see org.openmrs.api.ReportService#getReportRenderers()
	 */
	public Collection<ReportRenderer> getReportRenderers() {
		return getRenderers().values();
	}
	
	/**
	 * @see org.openmrs.api.ReportService#getRenderingModes(org.openmrs.report.ReportSchema)
	 */
	public List<RenderingMode> getRenderingModes(ReportSchema schema) {
		List<RenderingMode> ret = new Vector<RenderingMode>();
		for (ReportRenderer r : getReportRenderers()) {
			Collection<RenderingMode> modes = r.getRenderingModes(schema);
			if (modes != null)
				ret.addAll(modes);
		}
		Collections.sort(ret);
		return ret;
	}
	
	/**
	 * @see org.openmrs.api.ReportService#getReportSchema(java.lang.Integer)
	 */
	public ReportSchema getReportSchema(Integer reportSchemaId) throws APIException {
		ReportSchemaXml xml = getReportSchemaXml(reportSchemaId);
		return getReportSchema(xml);
	}
	
	/**
	 * @see org.openmrs.api.ReportService#getReportSchema(org.openmrs.report.ReportSchemaXml)
	 */
	public ReportSchema getReportSchema(ReportSchemaXml reportSchemaXml) throws APIException {
		ReportSchema reportSchema = null;
		if (reportSchemaXml == null || reportSchemaXml.getXml() == null || reportSchemaXml.getXml().length() == 0) {
			throw new APIException("The current serialized ReportSchema string named 'xml' is null or empty");
		}
		Serializer deserializer = OpenmrsUtil.getSerializer();
		String expandedXml = applyReportXmlMacros(reportSchemaXml.getXml());
		try {
			reportSchema = deserializer.read(ReportSchema.class, expandedXml);
		}
		catch (Exception e) {
			throw new APIException(e);
		}
		return reportSchema;
	}
	
	/**
	 * @see org.openmrs.api.ReportService#getReportSchemas()
	 */
	public List<ReportSchema> getReportSchemas() throws APIException {
		List<ReportSchema> ret = new ArrayList<ReportSchema>();
		for (ReportSchemaXml xml : getReportSchemaXmls()) {
			ret.add(getReportSchema(xml));
		}
		return ret;
	}
	
	/**
	 * ADDs renderers...doesn't replace them.
	 * 
	 * @see org.openmrs.api.ReportService#setRenderers(java.util.Map)
	 */
	public void setRenderers(Map<Class<? extends ReportRenderer>, ReportRenderer> newRenderers) throws APIException {
		for (Map.Entry<Class<? extends ReportRenderer>, ReportRenderer> entry : newRenderers.entrySet()) {
			registerRenderer(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * @see org.openmrs.api.ReportService#getRenderers()
	 */
	public Map<Class<? extends ReportRenderer>, ReportRenderer> getRenderers() throws APIException {
		if (renderers == null)
			renderers = new LinkedHashMap<Class<? extends ReportRenderer>, ReportRenderer>();
		
		return renderers;
	}
	
	/**
	 * @see org.openmrs.api.ReportService#registerRenderer(java.lang.Class,
	 *      org.openmrs.report.ReportRenderer)
	 */
	public void registerRenderer(Class<? extends ReportRenderer> rendererClass, ReportRenderer renderer) throws APIException {
		getRenderers().put(rendererClass, renderer);
	}
	
	/**
	 * @see org.openmrs.api.ReportService#registerRenderer(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public void registerRenderer(String rendererClass) throws APIException {
		try {
			Class loadedClass = OpenmrsClassLoader.getInstance().loadClass(rendererClass);
			registerRenderer(loadedClass, (ReportRenderer) loadedClass.newInstance());
			
		}
		catch (Exception e) {
			throw new APIException("Unable to load and instantiate renderer", e);
		}
	}
	
	/**
	 * @see org.openmrs.api.ReportService#removeReportRenderer(org.openmrs.report.ReportRenderer)
	 */
	public void removeRenderer(Class<? extends ReportRenderer> renderingClass) {
		renderers.remove(renderingClass);
	}
	
	/**
	 * @see org.openmrs.api.ReportService#saveReportSchema(org.openmrs.report.ReportSchema)
	 */
	public void saveReportSchema(ReportSchema reportSchema) {
		throw new APIException("Not Yet Implemented");
	}
	
	/**
	 * @see org.openmrs.api.ReportService#getReportSchemaXml(java.lang.Integer)
	 */
	public ReportSchemaXml getReportSchemaXml(Integer reportSchemaXmlId) {
		return dao.getReportSchemaXml(reportSchemaXmlId);
	}
	
	/**
	 * @see org.openmrs.api.ReportService#saveReportSchemaXml(org.openmrs.report.ReportSchemaXml)
	 */
	public void saveReportSchemaXml(ReportSchemaXml reportSchemaXml) {
		dao.saveReportSchemaXml(reportSchemaXml);
	}
	
	/**
	 * @see org.openmrs.api.ReportService#createReportSchemaXml(org.openmrs.report.ReportSchemaXml)
	 * @deprecated use saveReportSchemaXml(reportSchemaXml)
	 */
	public void createReportSchemaXml(ReportSchemaXml reportSchemaXml) {
		saveReportSchemaXml(reportSchemaXml);
	}
	
	/**
	 * @see org.openmrs.api.ReportService#updateReportSchemaXml(org.openmrs.report.ReportSchemaXml)
	 * @deprecated use saveReportSchemaXml(reportSchemaXml)
	 */
	public void updateReportSchemaXml(ReportSchemaXml reportSchemaXml) {
		saveReportSchemaXml(reportSchemaXml);
	}
	
	/**
	 * @see org.openmrs.api.ReportService#deleteReportSchemaXml(org.openmrs.report.ReportSchemaXml)
	 */
	public void deleteReportSchemaXml(ReportSchemaXml reportSchemaXml) {
		dao.deleteReportSchemaXml(reportSchemaXml);
	}
	
	/**
	 * @see org.openmrs.api.ReportService#getReportSchemaXmls()
	 */
	public List<ReportSchemaXml> getReportSchemaXmls() {
		return dao.getReportSchemaXmls();
	}
	
	/**
	 * @see org.openmrs.api.ReportService#getReportXmlMacros()
	 */
	public Properties getReportXmlMacros() {
		try {
			String macrosAsString = Context.getAdministrationService().getGlobalProperty(
			    OpenmrsConstants.GLOBAL_PROPERTY_REPORT_XML_MACROS);
			Properties macros = new Properties();
			if (macrosAsString != null) {
				macros.load(new ByteArrayInputStream(macrosAsString.getBytes("UTF-8")));
			}
			return macros;
		}
		catch (Exception ex) {
			throw new APIException(ex);
		}
	}
	
	/**
	 * @see org.openmrs.api.ReportService#saveReportXmlMacros(java.util.Properties)
	 */
	public void saveReportXmlMacros(Properties macros) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			macros.store(out, null);
			GlobalProperty prop = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_REPORT_XML_MACROS, out.toString());
			Context.getAdministrationService().saveGlobalProperty(prop);
		}
		catch (Exception ex) {
			throw new APIException(ex);
		}
	}
	
	/**
	 * @see org.openmrs.api.ReportService#applyReportXmlMacros(java.lang.String)
	 */
	public String applyReportXmlMacros(String input) {
		Properties macros = getReportXmlMacros();
		if (macros != null && macros.size() > 0) {
			log.debug("XML Before macros: " + input);
			String prefix = macros.getProperty("macroPrefix", "");
			String suffix = macros.getProperty("macroSuffix", "");
			while (true) {
				String replacement = input;
				for (Map.Entry<Object, Object> e : macros.entrySet()) {
					String key = prefix + e.getKey() + suffix;
					String value = e.getValue() == null ? "" : e.getValue().toString();
					log.debug("Trying to replace " + key + " with " + value);
					replacement = replacement.replace(key, (String) e.getValue());
				}
				if (input.equals(replacement)) {
					log.debug("Macro expansion complete.");
					break;
				}
				input = replacement;
				log.debug("XML Exploded to: " + input);
			}
		}
		return input;
	}
}
