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
package org.openmrs.api.db;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

import org.hibernate.Hibernate;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Form;

/**
 * A FormResource is meant as a way for modules to add arbitrary information to
 * a Form. FormResources are essentially just key-value pairs. The value is
 * stored as a byte array but can be set and retrieved as a String. A Form can
 * have 0-n FormResources but only one FormResource for any given combination of
 * owner and name.
 * 
 * A FormResource's <i>owner</i> must be unique and should refer to a particular
 * service or module claiming ownership of the resource. The <i>name</i> of a
 * resource specifies one of many resources that can be stored under a
 * particular owner, and can be the same name as a resource used by another
 * owner. Only one resource for each <i>owner:name</i> combination will
 * ever be saved.
 * 
 * @since 1.9
 */
public class FormResource extends BaseOpenmrsObject implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer formResourceId;
	
	private Form form;
	
	private String owner;
	
	private String name;
	
	private byte[] value;
	
	/**
	 * returns the id
	 */
	@Override
	public Integer getId() {
		return getFormResourceId();
	}
	
	/**
	 * sets the id
	 */
	@Override
	public void setId(Integer id) {
		setFormResourceId(id);
	}
	
	/**
	 * @return the formResourceId
	 */
	public Integer getFormResourceId() {
		return formResourceId;
	}
	
	/**
	 * @param formResourceId the formResourceId to set
	 */
	public void setFormResourceId(Integer id) {
		formResourceId = id;
	}
	
	/**
	 * @return the form
	 */
	public Form getForm() {
		return form;
	}
	
	/**
	 * @param form
	 *            the form to set
	 */
	public void setForm(Form form) {
		this.form = form;
	}
	
	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}
	
	/**
	 * @param owner
	 *            the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the value
	 */
	public byte[] getValue() {
		return value;
	}
	
	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(byte[] value) {
		this.value = value;
	}
	
	/**
	 * convenience method to set value from a Blob
	 * 
	 * @param blob
	 */
	@SuppressWarnings("unused")
	private void setValueBlob(Blob blob) {
		this.value = toByteArray(blob);
	}
	
	/**
	 * convenience method to get value as a Blob
	 * 
	 * @return value as a Blob
	 */
	@SuppressWarnings("unused")
	private Blob getValueBlob() {
		if (value == null)
			return null;
		return Hibernate.createBlob(value);
	}
	
	/**
	 * converts a Blob to a byte[]
	 * 
	 * @param fromImageBlob
	 * @return converted byte array
	 */
	private byte[] toByteArray(Blob blob) {
		if (blob == null)
			return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			return toByteArrayImpl(blob, baos);
		}
		catch (Exception e) {
			// not sure what could go wrong here
		}
		return null;
	}
	
	/**
	 * converts a byte stream to a byte[]
	 * 
	 * @param blob
	 * @param baos
	 * @return converted byte array
	 * @throws SQLException
	 * @throws IOException
	 */
	private byte[] toByteArrayImpl(Blob blob, ByteArrayOutputStream baos) throws SQLException, IOException {
		byte buf[] = new byte[4000];
		int dataSize;
		InputStream is = blob.getBinaryStream();
		
		try {
			while ((dataSize = is.read(buf)) != -1) {
				baos.write(buf, 0, dataSize);
			}
		}
		finally {
			if (is != null) {
				is.close();
			}
		}
		return baos.toByteArray();
	}
	
}
