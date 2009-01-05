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
package org.openmrs.web.controller.report;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortDefinition;
import org.openmrs.cohort.CohortUtil;
import org.openmrs.report.CohortDataSetDefinition;
import org.openmrs.report.DataSetDefinition;
import org.openmrs.report.Parameter;
import org.openmrs.report.ReportSchema;
import org.openmrs.report.ReportSchemaXml;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.PatientSearchReportObject;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.simpleframework.xml.Serializer;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

/**
 * This form lets you create or edit a report with a single CohortDataSetDefinition. You should
 * <strong>not</strong> use this form to edit other types of reports.
 */
public class CohortReportFormController extends SimpleFormController implements Validator {
	
	Log log = LogFactory.getLog(getClass());
	
	/**
	 * Creates a command object and tries to fill it with data from the saved report schema with the
	 * id given by the 'reportId' parameter.
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		CommandObject command = new CommandObject();
		
		if (Context.isAuthenticated() && !isFormSubmission(request)) {
			// if this is an existing report, get its data
			String idString = request.getParameter("reportId");
			if (idString != null) {
				Integer id = Integer.valueOf(idString);
				ReportSchemaXml schemaXml = Context.getReportService().getReportSchemaXml(id);
				ReportSchema schema = Context.getReportService().getReportSchema(schemaXml);
				CohortDataSetDefinition cohorts = null;
				if (schema.getDataSetDefinitions() == null)
					schema.setDataSetDefinitions(new ArrayList<DataSetDefinition>());
				if (schema.getDataSetDefinitions().size() == 0)
					schema.getDataSetDefinitions().add(new CohortDataSetDefinition());
				for (DataSetDefinition d : schema.getDataSetDefinitions()) {
					if (d instanceof CohortDataSetDefinition) {
						if (cohorts != null)
							throw new Exception(
							        "You may not edit a report that contains more than one Cohort Dataset Definition");
						cohorts = (CohortDataSetDefinition) d;
					} else {
						throw new Exception(
						        "You may not edit a report that contains datasets besides Cohort Dataset Definition");
					}
				}
				if (cohorts == null)
					throw new Exception("You may only edit a report that has exactly one Cohort Dataset Definition");
				
				command.setReportId(id);
				command.setName(schema.getName());
				command.setDescription(schema.getDescription());
				command.getParameters().addAll(schema.getReportParameters());
				
				// populate command.rows, directly from XML
				Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
				    new InputSource(new StringReader(schemaXml.getXml())));
				// xml looks like <reportSchema>...<dataSets>...<dataSetDefinition class="org.openmrs.report.CohortDataSetDefinition">
				// TODO: do this with xpath
				Node temp = findChild(xml, "reportSchema");
				temp = findChild(temp, "dataSets");
				temp = findChildWithAttribute(temp, "dataSetDefinition", "class",
				    "org.openmrs.report.CohortDataSetDefinition");
				
				Map<String, String> nameToStrategy = new LinkedHashMap<String, String>();
				Node strategies = findChild(temp, "strategies");
				if (strategies != null) {
					NodeList nl = strategies.getChildNodes();
					// each is a <entry><string>name</string><cohort ...><specification>strategy</specification></cohort></entry>
					for (int i = 0; i < nl.getLength(); ++i) {
						Node node = nl.item(i);
						if ("entry".equals(node.getNodeName())) {
							String name = findChild(node, "string").getFirstChild().getNodeValue();
							String strategy = findChild(findChild(node, "cohort"), "specification").getFirstChild()
							        .getNodeValue();
							nameToStrategy.put(name, strategy);
						}
					}
				}
				
				Map<String, String> nameToDescription = new LinkedHashMap<String, String>();
				Node descriptions = findChild(temp, "descriptions");
				if (descriptions != null) {
					NodeList nl = descriptions.getChildNodes();
					// each is a <entry><string>name</string><string>descr</string></entry>
					for (int i = 0; i < nl.getLength(); ++i) {
						Node node = nl.item(i);
						if ("entry".equals(node.getNodeName())) {
							String name = findChild(node, "string").getFirstChild().getNodeValue();
							String descr = findChild(node, "string", 2).getFirstChild().getNodeValue();
							nameToDescription.put(name, descr);
						}
					}
				}
				
				LinkedHashSet<String> names = new LinkedHashSet<String>();
				names.addAll(nameToStrategy.keySet());
				names.addAll(nameToDescription.keySet());
				
				List<CohortReportRow> rows = new ArrayList<CohortReportRow>();
				for (String name : names) {
					String descr = nameToDescription.get(name);
					String strat = nameToStrategy.get(name);
					CohortReportRow row = new CohortReportRow();
					row.setName(name);
					row.setDescription(descr);
					row.setQuery(strat);
					rows.add(row);
				}
				command.setRows(rows);
			}
		}
		return command;
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception {
		Map<String, Object> ret = new HashMap<String, Object>();
		
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(Date.class);
		classes.add(Integer.class);
		classes.add(Double.class);
		classes.add(Location.class);
		ret.put("parameterClasses", classes);
		
		List<AbstractReportObject> searches = Context.getReportObjectService().getReportObjectsByType(
		    OpenmrsConstants.REPORT_OBJECT_TYPE_PATIENTSEARCH);
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (AbstractReportObject o : searches) {
			if (o instanceof PatientSearchReportObject) {
				StringBuilder searchName = new StringBuilder(o.getName());
				List<Parameter> parameters = ((PatientSearchReportObject) o).getPatientSearch().getParameters();
				if (parameters != null && !parameters.isEmpty()) {
					searchName.append("|");
					for (Iterator<Parameter> i = parameters.iterator(); i.hasNext();) {
						Parameter p = i.next();
						searchName.append(p.getName()).append("=${?}");
						if (i.hasNext()) {
							searchName.append(",");
						}
					}
				}
				map.put(searchName.toString(), o.getDescription());
			} else {
				map.put(o.getName(), o.getDescription());
			}
		}
		ret.put("patientSearches", map);
		
		Properties macros = Context.getReportService().getReportXmlMacros();
		map = new LinkedHashMap<String, String>();
		for (Map.Entry<Object, Object> e : macros.entrySet()) {
			if (!e.getKey().toString().equals("macroPrefix") && !e.getKey().toString().equals("macroSuffix"))
				map.put(e.getKey().toString(), e.getValue().toString());
		}
		ret.put("macros", map);
		ret.put("macroPrefix", macros.get("macroPrefix"));
		ret.put("macroSuffix", macros.get("macroSuffix"));
		
		return ret;
	}
	
	/**
	 * Handles parameters and rows, since Spring isn't good with lists
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#onBind(javax.servlet.http.HttpServletRequest,
	 *      java.lang.Object, org.springframework.validation.BindException)
	 */
	@Override
	protected void onBind(HttpServletRequest request, Object commandObj, BindException errors) throws Exception {
		CommandObject command = (CommandObject) commandObj;
		
		// parameters
		String[] paramNames = request.getParameterValues("parameterName");
		String[] paramLabels = request.getParameterValues("parameterLabel");
		String[] paramClasses = request.getParameterValues("parameterClass");
		List<Parameter> params = new ArrayList<Parameter>();
		if (paramNames != null) {
			for (int i = 0; i < paramNames.length; ++i) {
				if (StringUtils.hasText(paramNames[i]) || StringUtils.hasText(paramLabels[i])
				        || StringUtils.hasText(paramClasses[i])) {
					try {
						Class<?> clz = null;
						if (StringUtils.hasText(paramClasses[i]))
							clz = Class.forName(paramClasses[i]);
						Parameter p = new Parameter(paramNames[i], paramLabels[i], clz, null);
						params.add(p);
					}
					catch (Exception ex) {
						errors.rejectValue("parameters", null, "Parameter error: " + ex.toString());
					}
				}
			}
		}
		command.setParameters(params);
		
		// rows
		String[] rowNames = request.getParameterValues("rowName");
		String[] rowDescriptions = request.getParameterValues("rowDescription");
		String[] rowQueries = request.getParameterValues("rowQuery");
		List<CohortReportRow> rows = new ArrayList<CohortReportRow>();
		if (rowNames != null) {
			for (int i = 0; i < rowNames.length; ++i) {
				try {
					CohortReportRow row = new CohortReportRow();
					row.setName(rowNames[i]);
					row.setDescription(rowDescriptions[i]);
					row.setQuery(rowQueries[i]);
					rows.add(row);
				}
				catch (Exception ex) {
					errors.rejectValue("rows", null, "Row error: " + ex.toString());
				}
			}
		}
		command.setRows(rows);
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object commandObj,
	                                BindException errors) throws Exception {
		CommandObject command = (CommandObject) commandObj;
		
		// do simpleframework serialization of everything but 'rows', and add those via handcoded xml, since
		// serializing them is not reversible
		
		ReportSchema rs = new ReportSchema();
		rs.setReportSchemaId(command.getReportId());
		rs.setName(command.getName());
		rs.setDescription(command.getDescription());
		rs.setReportParameters(command.getParameters());
		rs.setDataSetDefinitions(new ArrayList<DataSetDefinition>());
		Serializer serializer = OpenmrsUtil.getSerializer();
		StringWriter sw = new StringWriter();
		serializer.write(rs, sw);
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		
		Document xml = db.parse(new InputSource(new StringReader(
		        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + sw.toString())));
		Node node = findChild(xml, "reportSchema");
		node = findChild(node, "dataSets");
		Element dsd = xml.createElement("dataSetDefinition");
		dsd.setAttribute("name", "cohorts");
		dsd.setAttribute("class", "org.openmrs.report.CohortDataSetDefinition");
		node.appendChild(dsd);
		Element strategies = xml.createElement("strategies");
		strategies.setAttribute("class", "java.util.LinkedHashMap");
		dsd.appendChild(strategies);
		Element descriptions = xml.createElement("descriptions");
		descriptions.setAttribute("class", "java.util.LinkedHashMap");
		dsd.appendChild(descriptions);
		for (CohortReportRow row : command.getRows()) {
			if (StringUtils.hasText(row.getQuery())) {
				Element entry = xml.createElement("entry");
				strategies.appendChild(entry);
				Element nameEl = xml.createElement("string");
				Text val = xml.createTextNode(row.getName());
				val.setNodeValue(row.getName());
				nameEl.appendChild(val);
				entry.appendChild(nameEl);
				Element cohort = xml.createElement("cohort");
				entry.appendChild(cohort);
				cohort.setAttribute("class", "org.openmrs.reporting.PatientSearch");
				Element strategyEl = xml.createElement("specification");
				val = xml.createTextNode(row.getQuery());
				val.setNodeValue(row.getQuery());
				strategyEl.appendChild(val);
				cohort.appendChild(strategyEl);
			}
			if (StringUtils.hasText(row.getDescription())) {
				Element entry = xml.createElement("entry");
				descriptions.appendChild(entry);
				Element el = xml.createElement("string");
				Text val = xml.createTextNode(row.getName());
				val.setNodeValue(row.getName());
				el.appendChild(val);
				entry.appendChild(el);
				el = xml.createElement("string");
				val = xml.createTextNode(row.getDescription());
				val.setNodeValue(row.getDescription());
				el.appendChild(val);
				entry.appendChild(el);
			}
		}
		
		// now turn this into an xml string
		System.setProperty("javax.xml.transform.TransformerFactory",
		    "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		trans.setOutputProperty(OutputKeys.INDENT, "yes");
		trans.setOutputProperty(OutputKeys.METHOD, "xml");
		StringWriter out = new StringWriter();
		StreamResult result = new StreamResult(out);
		DOMSource source = new DOMSource(xml);
		trans.transform(source, result);
		String schemaXml = out.toString();
		
		ReportSchemaXml rsx = new ReportSchemaXml();
		rsx.populateFromReportSchema(rs);
		rsx.setXml(schemaXml);
		rsx.updateXmlFromAttributes();
		
		if (rsx.getReportSchemaId() != null) {
			Context.getReportService().saveReportSchemaXml(rsx);
		} else {
			Context.getReportService().saveReportSchemaXml(rsx);
		}
		
		return new ModelAndView(new RedirectView(getSuccessView() + "?reportId=" + rsx.getReportSchemaId()));
	}
	
	private Node findChild(Node parent, String name) {
		NodeList list = parent.getChildNodes();
		for (int i = 0; i < list.getLength(); ++i) {
			Node node = list.item(i);
			if (node.getNodeName().equals(name))
				return node;
		}
		return null;
	}
	
	/**
	 * finds the ith occurrence of a child with the given name
	 */
	private Node findChild(Node parent, String name, int index) {
		int soFar = 0;
		NodeList list = parent.getChildNodes();
		for (int i = 0; i < list.getLength(); ++i) {
			Node node = list.item(i);
			if (node.getNodeName().equals(name)) {
				++soFar;
				if (soFar == index)
					return node;
			}
		}
		return null;
	}
	
	private Node findChildWithAttribute(Node parent, String name, String attrName, String attrValue) {
		NodeList list = parent.getChildNodes();
		for (int i = 0; i < list.getLength(); ++i) {
			Node node = list.item(i);
			if (node.getNodeName().equals(name)) {
				Node attr = node.getAttributes().getNamedItem(attrName);
				if (attr != null && attr.getNodeValue().equals(attrValue))
					return node;
			}
		}
		return null;
	}
	
	// ***** Validator methods *****
	
	/**
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class clazz) {
		return clazz.equals(CommandObject.class);
	}
	
	/**
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 */
	public void validate(Object commandObj, Errors errors) {
		CommandObject command = (CommandObject) commandObj;
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "error.null");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "error.null");
		
		for (Parameter p : command.getParameters()) {
			if (!StringUtils.hasText(p.getName()) || !StringUtils.hasText(p.getLabel()) || p.getClazz() == null)
				errors.rejectValue("parameters", null, "All parameters must have a name, a label, and a datatype");
		}
		
		for (CohortReportRow row : command.getRows()) {
			if (!StringUtils.hasText(row.getName()))
				errors.rejectValue("rows", null, "Each row must have a name");
			
			try {
				String query = row.getQuery();
				query = Context.getReportService().applyReportXmlMacros(query);
				CohortDefinition def = CohortUtil.parse(query);
				if (def == null)
					throw new Exception();
			}
			catch (Exception ex) {
				errors.rejectValue("rows", null, "Failed to parse: " + row.getQuery() + " (" + ex.getMessage() + ")");
			}
		}
	}
	
	// ***** Command object *****
	
	public class CommandObject {
		
		private Integer reportId;
		
		private String name;
		
		private String description;
		
		private List<Parameter> parameters;
		
		private List<CohortReportRow> rows;
		
		public CommandObject() {
			parameters = new ArrayList<Parameter>();
			rows = new ArrayList<CohortReportRow>();
		}
		
		public Integer getReportId() {
			return reportId;
		}
		
		public void setReportId(Integer reportId) {
			this.reportId = reportId;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getDescription() {
			return description;
		}
		
		public void setDescription(String description) {
			this.description = description;
		}
		
		public List<Parameter> getParameters() {
			return parameters;
		}
		
		public void setParameters(List<Parameter> parameters) {
			this.parameters = parameters;
		}
		
		public List<CohortReportRow> getRows() {
			return rows;
		}
		
		public void setRows(List<CohortReportRow> rows) {
			this.rows = rows;
		}
	}
	
	public class CohortReportRow {
		
		private String name;
		
		private String description;
		
		private String query;
		
		public CohortReportRow() {
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getDescription() {
			return description;
		}
		
		public void setDescription(String description) {
			this.description = description;
		}
		
		public String getQuery() {
			return query;
		}
		
		public void setQuery(String query) {
			this.query = query;
		}
	}
	
}
