package org.openmrs.dynamicformentry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class DynamicFormEntryController extends SimpleFormController {

    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
        
    protected Map<String, Object> formBackingObject(HttpServletRequest request) throws Exception {
    	HttpSession httpSession = request.getSession();
		Map<String, Object> formInProgress = (Map<String, Object>) httpSession.getAttribute(WebConstants.OPENMRS_DYNAMIC_FORM_IN_PROGRESS_ATTR);
		if (formInProgress == null) {
			formInProgress = new TreeMap<String, Object>();
			httpSession.setAttribute(WebConstants.OPENMRS_DYNAMIC_FORM_IN_PROGRESS_ATTR, formInProgress);
		}
		return formInProgress;
    }
    
    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
    	log.debug("command is " + command);
    	String formId = request.getParameter("formId");
    	if (formId == null || formId.length() == 0) {
    		throw new IllegalArgumentException("formId is a required parameter");
    	}
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
    	Map ret = new HashMap<String, Object>();
    	ret.put("formId", Integer.valueOf(formId));
		if (context != null) {
			Form form = context.getFormService().getForm(Integer.valueOf(formId));
			if (form == null) {
				throw new IllegalArgumentException("Can't find a form with formId " + formId);
			}
			List<FormField> fields = new ArrayList<FormField>(form.getFormFields());
			Collections.sort(fields, new Comparator<FormField>() {
					public int compare(FormField left, FormField right) {
						Integer l = left.getPageNumber();
						if (l == null) {
							l = Integer.MAX_VALUE;
						}
						Integer r = right.getPageNumber();
						if (r == null) {
							r = Integer.MAX_VALUE;
						}
						int temp = l.compareTo(r);
						if (temp == 0) {
							l = left.getFieldNumber();
							if (l == null) {
								l = Integer.MAX_VALUE;
							}
							r = right.getFieldNumber();
							if (r == null) {
								r = Integer.MAX_VALUE;
							}
							temp = l.compareTo(r);
						}
						return temp;
					}
				});
			
			ret.put("form", form);
			ret.put("fields", fields);
		}
    	return ret;
    }
    
    /*
    private Form getForm(HttpServletRequest request) {
    	Integer formId = null;
    	try {
    		formId = new Integer(request.getParameter("formId"));
    	} catch (Exception ex) {
    		throw new IllegalArgumentException("illegal value for formId parameter: " + request.getParameter("formId"), ex);
    	}
    	Form form = null;
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			form = context.getFormService().getForm(formId);
		}
		return form;
    }
    */
    
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
    		Object comm, BindException errors) throws Exception {

    	Map<String, Object> command = (Map<String, Object>) comm;
    	log.debug("command is " + command);
    	// String view = getFormView();
    	
		Context context = (Context) request.getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			return new ModelAndView(new RedirectView("login.htm"));
		}
		FormService fs = context.getFormService();
    	
		for (Enumeration e = request.getParameterNames(); e.hasMoreElements(); ) {
			String paramName = (String) e.nextElement();
    		// "data.[formFieldId].value"
    		if (paramName.startsWith("data.")) {
    			String[] s = paramName.split("\\.");
    			if (s.length != 3) {
    				throw new IllegalArgumentException("parameters should be of the form data.[formFieldId].[valueType]");
    			}
    			Integer formFieldId = Integer.valueOf(s[1]);
    			String valueType = s[2];
    			String[] values = request.getParameterValues(paramName);
    			if (values.length > 1) {
    				throw new IllegalArgumentException("Multiple values for one key not yet implemented (" + paramName + " has " + values.length + " values)");
    			}
    			if (values.length == 1) {
    				String key = valueType;
    				FormField ff = fs.getFormField(formFieldId);
    				do {
    					key = ff.getField().getName() + "." + key;
    					ff = ff.getParent();
    				} while (ff != null);
    				if (values[0] == null || values[0].length() == 0) {
    					command.remove(key);
    				} else { 
    					command.put(key, values[0]);
    					log.debug("set " + key + " -> " + values[0]);
    				}
    			}
    		}
    	}
		String view = "/openmrs/dynamicForm.form?formId=" + request.getParameter("formId");
		log.debug("redirecting to " + view);
    	return new ModelAndView(new RedirectView(view));
    }

    /*
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return null;
		}
		
		Form form = getForm(request);
		Map<String, Object> model = new HashMap<String, Object>();
		StringBuilder sb = new StringBuilder();
		if (form != null) {
			sb.append(FormUtil.getFormStructure(context, form)).append("\n");
			
			List<FormField> fields = new ArrayList<FormField>(form.getFormFields());
			Collections.sort(fields, new Comparator<FormField>() {
					public int compare(FormField left, FormField right) {
						Integer l = left.getPageNumber();
						if (l == null) {
							l = Integer.MAX_VALUE;
						}
						Integer r = right.getPageNumber();
						if (r == null) {
							r = Integer.MAX_VALUE;
						}
						int temp = l.compareTo(r);
						if (temp == 0) {
							l = left.getFieldNumber();
							if (l == null) {
								l = Integer.MAX_VALUE;
							}
							r = right.getFieldNumber();
							if (r == null) {
								r = Integer.MAX_VALUE;
							}
							temp = l.compareTo(r);
						}
						return temp;
					}
				});
			model.put("fields", fields);
			
			for (FormField field : fields) {
				Field f = field.getField();
				sb.append("pg#" + field.getPageNumber() + " field#" + field.getFieldNumber() + ": ");
				sb.append(f.getName());
				if (f.getConcept() != null) {
					sb.append(" concept " + f.getConcept());
				}
				if (f.getTableName() != null && f.getTableName().length() > 0) {
					sb.append(" tableName " + f.getTableName());
				}
				if (field.getParent() != null) {
					sb.append(" [CHILD OF " + field.getParent().getField().getName() + "]");
				}
				sb.append("\n");
			}
			
			// this is all taken from FormDownloadServlet
			String formDir = FormEntryConstants.FORMENTRY_INFOPATH_OUTPUT_DIR;
			String formFilePath = formDir
					+ (formDir.endsWith(File.separator) ? ""
							: File.separator) + form.getUri();

			
			// Get Constants
			String schemaFilename = FormEntryConstants.FORMENTRY_DEFAULT_SCHEMA_NAME;
			String templateFilename = FormEntryConstants.FORMENTRY_DEFAULT_TEMPLATE_NAME;
			String sampleDataFilename = FormEntryConstants.FORMENTRY_DEFAULT_SAMPLEDATA_NAME;
			String url = FormUtil.getFormAbsoluteUrl(form);

			// Expand the xsn
			log.debug("expandXsn(" + formFilePath + ")");
			File tmpXSN = FormEntryUtil.expandXsn(formFilePath);
			log.debug("...returns " + tmpXSN);
			log.debug("..." + tmpXSN.getAbsolutePath());

			// Generate the schema and template.xml
			log.debug("making schema from context and " + form);
			String schema = new FormSchemaBuilder(context, form).getSchema();
			log.debug("...returns " + schema);
			log.debug("making template from form and " + url);
			String template = new FormXmlTemplateBuilder(context, form, url)
					.getXmlTemplate((Patient) null);
			log.debug("...returns " + template);
			template = template.replaceAll("@SESSION@", "");
			sb.append("\ntemplate=\n" + template);

			// Generate and overwrite the schema
			log.debug("finding schema file " + schemaFilename + " in tmpXSN dir " + tmpXSN);
			File schemaFile = FormEntryUtil.findFile(tmpXSN, schemaFilename);
			log.debug("...returns " + schemaFile);
			if (schemaFile == null)
				throw new IOException("Schema: '" + schemaFilename
						+ "' cannot be null");
			FileWriter schemaOutput = new FileWriter(schemaFile, false);
			schemaOutput.write(schema);
			schemaOutput.close();
			sb.append("\nschema=\n" + schema);
			
			// replace template.xml with the generated xml
			File templateFile = FormEntryUtil.findFile(tmpXSN, templateFilename);
			if (templateFile == null)
				throw new IOException("Template: '" + templateFilename
						+ "' cannot be null");
			FileWriter templateOutput = new FileWriter(templateFile, false);
			templateOutput.write(template);
			templateOutput.close();
			sb.append("\ntemplate=\n" + template);

			// replace sampleData.xml with the generated xml
			File sampleDataFile = FormEntryUtil
					.findFile(tmpXSN, sampleDataFilename);
			if (sampleDataFile == null)
				throw new IOException("Template: '" + sampleDataFilename
						+ "' cannot be null");
			FileWriter sampleDataOutput = new FileWriter(sampleDataFile, false);
			sampleDataOutput.write(template);
			sampleDataOutput.close();
			sb.append("\ntemplate=\n" + template);

		}
		
		model.put("debug", sb);
		//return new ModelAndView("/debug", "model", model);
		return new ModelAndView("/formentry/dynamic/enter", "model", model);
	}
	*/
	
}
