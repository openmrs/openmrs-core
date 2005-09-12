package org.openmrs.api.ibatis;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.FormService;
import org.openmrs.context.Context;

/**
 * Ibatis-specific implementation of org.openmrs.api.FormService
 * 
 * @see org.openmrs.api.FormService
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
	 * @see org.openmrs.context.Context
	 */
	public IbatisFormService(Context context) {
		this.context = context;
	}

	/**
	 * @see org.openmrs.api.FormService#createForm(Form)
	 */
	public Form createForm(Form form) throws APIException {
		try {
			SqlMap.instance().insert("createForm", form);
		} catch (SQLException e) {
			throw new APIException(e);
		}
		return form;
	}

	/**
	 * @see org.openmrs.api.FormService#getForm(Integer)
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
	 * @see org.openmrs.api.FormService#updateForm(Form)
	 */
	public void updateForm(Form form) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();

				User authenticatedUser = context.getAuthenticatedUser();

				if (form.getCreator().getUserId() == null) {
					form.setCreator(authenticatedUser);
					SqlMap.instance().insert("createForm", form);
				} else {
					form.setChangedBy(authenticatedUser);
					SqlMap.instance().update("updateForm", form);
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
	 * @see org.openmrs.api.FormService#retireForm(Form, String)
	 */
	public void retireForm(Form form, String reason) throws APIException {
		// TODO add "void_reason" to form table
		//form.setVoided(true);
		form.setChangedBy(context.getAuthenticatedUser());
		// form.setVoidReason(reason);
		try {
			SqlMap.instance().update("retireForm", form);
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.FormService#unRetireForm(Form)
	 */
	public void unRetireForm(Form form) {
		form.setChangedBy(context.getAuthenticatedUser());
		try {
			SqlMap.instance().update("unRetireForm", form);
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.FormService#deleteForm(Form)
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

}
