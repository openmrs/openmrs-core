package org.openmrs.api.db.ibatis;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Field;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.db.APIException;
import org.openmrs.api.db.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.util.Compare;

/**
 * Ibatis-specific implementation of org.openmrs.api.db.FormService
 * 
 * @see org.openmrs.api.db.FormService
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class IbatisFormService implements FormService {

	private final Log log = LogFactory.getLog(getClass());

	private Context context;

	/**
	 * Service must be constructed within a <code>context</code>
	 * @param context
	 * @see org.openmrs.api.context.Context
	 */
	public IbatisFormService(Context context) {
		this.context = context;
	}

	/**
	 * @see org.openmrs.api.db.FormService#createForm(Form)
	 */
	// TODO add fields/field answers ?
	public Form createForm(Form form) throws APIException {
		try {
			form.setCreator(context.getAuthenticatedUser());
			
			SqlMap.instance().insert("createForm", form);
			
			if (form.getFormFields() != null) {
				for(Iterator i = form.getFormFields().iterator(); i.hasNext();) {
					FormField formField = (FormField)i.next();
					formField.setCreator(context.getAuthenticatedUser());
					SqlMap.instance().insert("createFormField", formField);
				}
			}
			
		} catch (SQLException e) {
			throw new APIException(e);
		}
		return form;
	}

	/**
	 * @see org.openmrs.api.db.FormService#getForm(Integer)
	 */
	public Form getForm(Integer formId) throws APIException {
		Form form;
		try {
			form = (Form) SqlMap.instance().queryForObject("getForm", formId);
		} catch (SQLException e) {
			throw new APIException(e);
		}
		return form;
	}

	/**
	 * @see org.openmrs.api.db.FormService#updateForm(Form)
	 */
	public void updateForm(Form form) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();

				if (form.getCreator() == null) {
					this.createForm(form);
				} else {
					form.setChangedBy(context.getAuthenticatedUser());
					SqlMap.instance().update("updateForm", form);
					
					Map map;
					List toAdd;
					List toDel;
					
					//update formFields
					List oldFormFields = SqlMap.instance().queryForList("getFormFieldsByFormId", form.getFormId());
					map = Compare.compareLists(oldFormFields, (List)form.getFormFields());
					toAdd = (List)map.get("toAdd");
					toDel = (List)map.get("toDel");
					for (Iterator i = toAdd.iterator(); i.hasNext();) {
						FormField formField = (FormField)i.next();
						formField.setCreator(context.getAuthenticatedUser());
						SqlMap.instance().insert("createFormField", formField);
					}
					for (Iterator i = toDel.iterator(); i.hasNext();)
						SqlMap.instance().delete("deleteFormField", i.next());
					
				}
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}

	}
	
	/**
	 * @see org.openmrs.api.db.FormService#retireForm(Form, String)
	 */
	public void retireForm(Form form, String reason) throws APIException {
		form.setRetiredBy(context.getAuthenticatedUser());
		form.setRetiredReason(reason);
		try {
			SqlMap.instance().update("retireForm", form);
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.FormService#unretireForm(Form)
	 */
	public void unretireForm(Form form) {
		try {
			SqlMap.instance().update("unretireForm", form);
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.FormService#deleteForm(Form)
	 */
	public void deleteForm(Form form) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();
				SqlMap.instance().delete("deleteForm", form);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}

	public List<FieldType> getFieldTypes() throws APIException {
		
		List<FieldType> fieldTypes;
		
		try {
			try {
				SqlMap.instance().startTransaction();
				fieldTypes = SqlMap.instance().queryForList("getAllFieldTypes", null);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		
		return fieldTypes;
	}

	public FieldType getFieldType(Integer fieldTypeId) throws APIException {

		FieldType fieldType;
		
		try {
			try {
				SqlMap.instance().startTransaction();
				fieldType = (FieldType)SqlMap.instance().queryForObject("getFieldType", fieldTypeId);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		
		return fieldType;
	}

	public void createFormField(FormField formField) throws APIException {
		// TODO Auto-generated method stub
		
	}

	public void deleteField(Field field) throws APIException {
		// TODO Auto-generated method stub
		
	}

	public void deleteFormField(FormField formField) throws APIException {
		// TODO Auto-generated method stub
		
	}

	public Field getField(Integer fieldId) throws APIException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Field> getFields() throws APIException {
		// TODO Auto-generated method stub
		return null;
	}

	public FormField getFormField(Integer formFieldId) throws APIException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Form> getForms() throws APIException {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateField(Field field) throws APIException {
		// TODO Auto-generated method stub
		
	}

	public void updateFormField(FormField formField) throws APIException {
		// TODO Auto-generated method stub
		
	}
	
}
