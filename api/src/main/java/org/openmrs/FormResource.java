/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.envers.Audited;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.customdatatype.CustomValueDescriptor;
import org.openmrs.customdatatype.NotYetPersistedException;
import org.openmrs.customdatatype.SingleCustomValue;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;
import javax.persistence.Lob;

/**
 * A FormResource is meant as a way for modules to add arbitrary information to
 * a Form. FormResources are essentially just key-value pairs. The value is
 * stored as a custom datatype. A Form can have 0-n FormResources but only one
 * FormResource per name per Form.
 *
 * The <i>name</i> of a resource specifies one of many resources that can be
 * stored for a particular owner. Only one resource for each name will
 * ever be saved.
 *
 * @since 1.9
 */
@Entity
@Table(name = "form_resource")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@AttributeOverride(name = "uuid", column = @Column(name = "uuid", unique = true, nullable = false, length = 38))
@Audited
public class FormResource extends BaseOpenmrsObject implements CustomValueDescriptor, SingleCustomValue<FormResource> {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "form_resource_form_resource_id_seq")
	@GenericGenerator(
		name = "form_resource_form_resource_id_seq",
		strategy = "native",
		parameters = @Parameter(name = "sequence", value = "form_resource_form_resource_id_seq")
	)
	@Column(name = "form_resource_id")
	private Integer formResourceId;

	@ManyToOne
	@JoinColumn(name = "form_id", nullable = false)
	private Form form;

	@Column(name = "name", length = 255, nullable = true)
	private String name;

	@Lob
	@Column(name = "value_reference", length = 65535, nullable = true)
	private String valueReference;

	@Column(name = "datatype", length = 255)
	private String datatypeClassname;

	@Lob
	@Column(name = "datatype_config", length = 65535)
	private String datatypeConfig;

	@Column(name = "preferred_handler")
	private String preferredHandlerClassname;

	@Lob
	@Column(name = "handler_config")
	private String handlerConfig;

	@Transient
	private transient boolean dirty = false;

	@Transient
	private transient Object typedValue;

	@ManyToOne
	@JoinColumn(name = "changed_by")
	private User changedBy;
	
	@Column(name = "date_changed", length = 19)
	private Date dateChanged;
	
	public FormResource() {
		// generic constructor
	}
	
	/**
	 * Create a copy of a provided FormResource, ignoring the uuid and id of the original
	 *
	 * @param old the original FormResource to be copied
	 */
	public FormResource(FormResource old) {
		this.form = old.getForm();
		this.name = old.getName();
		this.valueReference = old.getValueReference();
		this.datatypeClassname = old.getDatatypeClassname();
		this.datatypeConfig = old.getDatatypeConfig();
		this.preferredHandlerClassname = old.getPreferredHandlerClassname();
		this.handlerConfig = old.getHandlerConfig();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getFormResourceId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setFormResourceId(id);
	}
	
	/**
	 * gets the form
	 *
	 * @return the form
	 */
	public Form getForm() {
		return form;
	}
	
	/**
	 * sets the form
	 *
	 * @param form the form
	 */
	public void setForm(Form form) {
		this.form = form;
	}
	
	/**
	 * gets the form resource id
	 *
	 * @return the form resource's id
	 */
	public Integer getFormResourceId() {
		return formResourceId;
	}
	
	/**
	 * sets the form resource id
	 *
	 * @param formResourceId the form resource's id
	 */
	public void setFormResourceId(Integer formResourceId) {
		this.formResourceId = formResourceId;
	}
	
	/**
	 * gets the name of the resource
	 *
	 * @return the name of the resource
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * sets the name of the resource
	 *
	 * @param name the name of the resource
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomValueDescriptor#getDatatypeClassname()
	 */
	@Override
	public String getDatatypeClassname() {
		return datatypeClassname;
	}
	
	/**
	 * @param datatypeClassname the datatypeClassname to set
	 */
	public void setDatatypeClassname(String datatypeClassname) {
		this.datatypeClassname = datatypeClassname;
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomValueDescriptor#getDatatypeConfig()
	 */
	@Override
	public String getDatatypeConfig() {
		return datatypeConfig;
	}
	
	/**
	 * @param datatypeConfig the datatypeConfig to set
	 */
	public void setDatatypeConfig(String datatypeConfig) {
		this.datatypeConfig = datatypeConfig;
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomValueDescriptor#getPreferredHandlerClassname()
	 */
	@Override
	public String getPreferredHandlerClassname() {
		return preferredHandlerClassname;
	}
	
	/**
	 * @param preferredHandlerClassname the preferredHandlerClassname to set
	 */
	public void setPreferredHandlerClassname(String preferredHandlerClassname) {
		this.preferredHandlerClassname = preferredHandlerClassname;
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomValueDescriptor#getHandlerConfig()
	 */
	@Override
	public String getHandlerConfig() {
		return handlerConfig;
	}
	
	/**
	 * @param handlerConfig the handlerConfig to set
	 */
	public void setHandlerConfig(String handlerConfig) {
		this.handlerConfig = handlerConfig;
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#getDescriptor()
	 */
	@Override
	public FormResource getDescriptor() {
		return this;
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#getValueReference()
	 */
	@Override
	public String getValueReference() {
		if (valueReference == null) {
			throw new NotYetPersistedException();
		} else {
			return valueReference;
		}
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#getValue()
	 */
	@Override
	public Object getValue(){
		if (typedValue == null) {
			typedValue = CustomDatatypeUtil.getDatatype(this).fromReferenceString(getValueReference());
		}
		return typedValue;
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#setValue(java.lang.Object)
	 */
	@Override
	public <T> void setValue(T typedValue)  {
		this.typedValue = typedValue;
		dirty = true;
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#setValueReferenceInternal(java.lang.String)
	 */
	@Override
	public void setValueReferenceInternal(String valueToPersist)  {
		this.valueReference = valueToPersist;
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#isDirty()
	 *
	 * @deprecated as of 2.0, use {@link #getDirty()}
	 */
	@Deprecated
	@JsonIgnore
	@Override
	public boolean isDirty() {
		return getDirty();
	}
	
	public boolean getDirty() {
		return dirty;
	}
	
	/**
	 * @return Returns the changedBy.
	 */
	public User getChangedBy() {
		return changedBy;
	}
	
	/**
	 * @param changedBy The user that changed this object
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	/**
	 * @return Returns the date this object was changed
	 */
	public Date getDateChanged() {
		return dateChanged;
	}
	
	/**
	 * @param dateChanged The date this object was changed
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
}
