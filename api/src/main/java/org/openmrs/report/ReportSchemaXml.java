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

import java.io.StringWriter;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.util.OpenmrsUtil;
import org.simpleframework.xml.Serializer;

/**
 * This represents a very simplified saving technique for ReportSchemas. The "xml" attribute is
 * assumed to be a serialized ReportSchema object.
 * 
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class ReportSchemaXml extends BaseOpenmrsObject {
	
	private static final long serialVersionUID = 9330457450L;
	
	private Integer reportSchemaId;
	
	private String name;
	
	private String description;
	
	private String xml;
	
	/**
	 * Default constructor
	 */
	public ReportSchemaXml() {
	}
	
	/**
	 * Convenience constructor taking in a primary key report schema id
	 * 
	 * @param reportschemaId
	 */
	public ReportSchemaXml(Integer reportschemaId) {
		this.reportSchemaId = reportschemaId;
	}
	
	/**
	 * Convenience constructor to allow a user to create this shlub of a class from a full
	 * ReportSchema object
	 * 
	 * @param schema the ReportSchema to serialize and store with this tiny class
	 * @throws Exception if serialization fails
	 */
	public ReportSchemaXml(ReportSchema schema) throws Exception {
		Serializer serializer = OpenmrsUtil.getSerializer();
		StringWriter writer = new StringWriter();
		serializer.write(schema, writer);
		
		populateFromReportSchema(schema);
		setXml(writer.toString());
	}
	
	/**
	 * Set the Report Schema Id
	 * 
	 * @param reportSchemaId
	 */
	public void setReportSchemaId(Integer reportSchemaId) {
		this.reportSchemaId = reportSchemaId;
	}
	
	/**
	 * Returns the ReportSchema Id
	 * 
	 * @return the Integer Report Schema Id
	 */
	public Integer getReportSchemaId() {
		return this.reportSchemaId;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Set the xml content for the ReportSchemaXml
	 * 
	 * @param xml serialized content
	 */
	public void setXml(String xml) {
		this.xml = xml;
	}
	
	/**
	 * Returns the xml of the ReportSchemaXml
	 * 
	 * @return current xml serialization
	 */
	public String getXml() {
		return this.xml;
	}
	
	/**
	 * Convenience helper method to set the attributes on this ReportSchemaXML object with what is
	 * in the report schema object
	 * 
	 * @param reportSchema ReportSchema from which to pull values
	 */
	public void populateFromReportSchema(ReportSchema reportSchema) {
		setReportSchemaId(reportSchema.getReportSchemaId());
		setName(reportSchema.getName());
		setDescription(reportSchema.getDescription());
	}
	
	/**
	 * Convenience helper method that will deserialize the "xml" string to a ReportSchema, update
	 * that report schema with the id/name/desc attributes that are on this ReportSchemaXml object,
	 * and then re serialize that ReportSchema object into the "xml" string
	 */
	public void updateXmlFromAttributes() throws Exception {
		if (getXml() != null && getReportSchemaId() != null) {
			String newXml;
			if (xml.contains("reportSchemaId="))
				newXml = xml.replaceFirst("reportSchemaId=[\"'][^ ]*[\"']", "reportSchemaId=\"" + reportSchemaId + "\"");
			else
				newXml = xml
				        .replaceFirst("<reportSchema([ >])", "<reportSchema reportSchemaId=\"" + reportSchemaId + "\"$1");
			
			setXml(newXml);
		}
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getReportSchemaId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setReportSchemaId(id);
	}
	
}
