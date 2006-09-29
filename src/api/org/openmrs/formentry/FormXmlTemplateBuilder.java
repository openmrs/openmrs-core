package org.openmrs.formentry;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Drug;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;

/**
 * XML template builder for OpenMRS forms.
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class FormXmlTemplateBuilder {

	protected final Log log = LogFactory.getLog(getClass());

	Form form;
	String url;

	/**
	 * Construct an XML template builder for generating patient-based templates
	 * for a given OpenMRS form.
	 * 
	 * @param context
	 *            active OpenMRS context
	 * @param form
	 *            OpenMRS form for which template(s) will be made
	 * @param url
	 *            absolute (full, including "http://" and ending with ".xsn")
	 *            url location of InfoPath form (.xsn file)
	 */
	public FormXmlTemplateBuilder(Form form, String url) {
		this.form = form;
		this.url = url;
	}
	
	public synchronized String getXmlTemplate(Patient patient) {
		try {
			Velocity.init();
		} catch (Exception e) {
			log.error("Error initializing Velocity engine", e);
		}
		VelocityContext velocityContext = new VelocityContext();

		if (patient != null) {
			velocityContext.put("form", form);
			velocityContext.put("url", url);
			User user = Context.getAuthenticatedUser();
			String enterer;
			if (user != null)
				enterer = user.getUserId() + "^" + user.getFirstName() + " "
						+ user.getLastName();
			else
				enterer = "";

			velocityContext.put("enterer", enterer);
			velocityContext.put("patient", patient);
			velocityContext.put("timestamp", new SimpleDateFormat(
					"yyyyMMdd'T'HH:mm:ss.SSSZ"));
			velocityContext.put("date", new SimpleDateFormat("yyyyMMdd"));
			velocityContext.put("time", new SimpleDateFormat("HH:mm:ss"));
		}

		String template = null;
		try {
			StringWriter w = new StringWriter();
			Velocity.evaluate(velocityContext, w, this.getClass().getName(),
					form.getTemplate());
			template = w.toString();
		} catch (Exception e) {
			log.error("Error evaluating default values for form "
					+ form.getName() + "[" + form.getFormId() + "]", e);
		}
		return template;
	}

	/**
	 * Returns the XML template for a form
	 * 
	 * @param includeDefaultScripts
	 *            if true, field defaults are inserted into the template
	 * @return XML template for a form
	 */
	public synchronized String getXmlTemplate(boolean includeDefaultScripts) {
		StringBuffer xml = new StringBuffer();

		xml.append(FormXmlTemplateFragment.header(form, url));
		xml.append(FormXmlTemplateFragment.openForm(form, FormEntryUtil
				.getFormSchemaNamespace(form), includeDefaultScripts));

		TreeMap<Integer, TreeSet<FormField>> formStructure = FormUtil
				.getFormStructure(form);

		renderStructure(xml, formStructure, includeDefaultScripts, 0, 2);

		xml.append(FormXmlTemplateFragment.closeForm());

		return xml.toString();
	}

	public void renderStructure(StringBuffer xml,
			TreeMap<Integer, TreeSet<FormField>> formStructure,
			boolean includeDefaultScripts, Integer sectionId, int indent) {
		if (!formStructure.containsKey(sectionId))
			return;
		Vector<String> tagList = new Vector<String>();
		TreeSet<FormField> section = formStructure.get(sectionId);
		if (section == null || section.size() < 1)
			return;
		for (FormField formField : section) {
			String xmlTag = FormUtil.getNewTag(formField.getField().getName(),
					tagList);
			Integer subSectionId = formField.getFormFieldId();
			char[] indentation = new char[indent];
			for (int i = 0; i < indent; i++)
				indentation[i] = ' ';
			xml.append(indentation);
			xml.append("<" + xmlTag);
			Field field = formField.getField();
			Integer fieldTypeId = field.getFieldType().getFieldTypeId();
			if (fieldTypeId.equals(FormEntryConstants.FIELD_TYPE_DATABASE)) {
				xml.append(" openmrs_table=\"");
				xml.append(formField.getField().getTableName());
				xml.append("\" openmrs_attribute=\"");
				xml.append(formField.getField().getAttributeName());
				if (formStructure.containsKey(formField.getFormFieldId())) {
					xml.append("\">\n");
					renderStructure(xml, formStructure, includeDefaultScripts,
							subSectionId, indent
									+ FormEntryConstants.INDENT_SIZE);
					xml.append(indentation);
				} else {
					if (field.getDefaultValue() != null) {
						xml.append("\">");
						if (includeDefaultScripts)
							xml.append(field.getDefaultValue());
					} else {
						if (!formField.isRequired())
							xml.append("\" xsi:nil=\"true");
						xml.append("\">");
					}
				}
				xml.append("</");
				xml.append(xmlTag);
				xml.append(">\n");
			} else if (fieldTypeId
					.equals(FormEntryConstants.FIELD_TYPE_CONCEPT)) {
				Concept concept = field.getConcept();
				xml.append(" openmrs_concept=\"");
				xml.append(FormUtil.conceptToString(concept, Context
						.getLocale()));
				xml.append("\" openmrs_datatype=\"");
				xml.append(concept.getDatatype().getHl7Abbreviation());
				xml.append("\"");
				if (formStructure.containsKey(formField.getFormFieldId())) {
					xml.append(">\n");
					renderStructure(xml, formStructure, includeDefaultScripts,
							subSectionId, indent
									+ FormEntryConstants.INDENT_SIZE);
					xml.append(indentation);
					xml.append("</");
					xml.append(xmlTag);
					xml.append(">\n");
				} else {
					if (concept.getDatatype().getHl7Abbreviation().equals(
							FormEntryConstants.HL7_CODED)
							|| concept
									.getDatatype()
									.getHl7Abbreviation()
									.equals(
											FormEntryConstants.HL7_CODED_WITH_EXCEPTIONS)) {
						xml.append(" multiple=\"");
						xml.append(field.getSelectMultiple() ? "1" : "0");
						xml.append("\"");
					}
					xml.append(">\n");
					xml.append(indentation);
					xml.append(indentation);
					xml.append("<date xsi:nil=\"true\"></date>\n");
					xml.append(indentation);
					xml.append(indentation);
					xml.append("<time xsi:nil=\"true\"></time>\n");
					if ((concept.getDatatype().getHl7Abbreviation().equals(
							FormEntryConstants.HL7_CODED) || concept
							.getDatatype()
							.getHl7Abbreviation()
							.equals(
									FormEntryConstants.HL7_CODED_WITH_EXCEPTIONS))
							&& field.getSelectMultiple()) {
						for (ConceptAnswer answer : concept
								.getSortedAnswers(Context.getLocale())) {
							xml.append(indentation);
							xml.append(indentation);
							xml.append("<");
							String answerConceptName = answer
									.getAnswerConcept().getName(
											Context.getLocale()).getName();
							Drug answerDrug = answer.getAnswerDrug();
							if (answerDrug == null) {
								String answerTag = FormUtil.getXmlToken(
										answerConceptName);
								xml.append(answerTag);
								xml.append(" openmrs_concept=\"");
								xml.append(FormUtil.conceptToString(answer
										.getAnswerConcept(), Context
										.getLocale()));
								xml.append("\">false</");
								xml.append(answerTag);
								xml.append(">\n");
							} else {
								String answerDrugName = answerDrug.getName();
								String answerTag = FormUtil.getXmlToken(
										answerDrugName);
								xml.append(answerTag);
								xml.append(" openmrs_concept=\"");
								xml.append(FormUtil.conceptToString(answer
										.getAnswerConcept(), Context
										.getLocale()));
								xml.append("^");
								xml.append(FormUtil.drugToString(answerDrug));
								xml.append("\">false</");
								xml.append(answerTag);
								xml.append(">\n");
							}
						}
					} else {
						xml.append(indentation);
						xml.append(indentation);
						xml.append("<value");
						if (concept.getDatatype().getHl7Abbreviation().equals(
								FormEntryConstants.HL7_BOOLEAN))
							xml.append(" infopath_boolean_hack=\"1\"");
						xml.append(" xsi:nil=\"true\"></value>\n");
					}
					xml.append(indentation);
					xml.append("</");
					xml.append(xmlTag);
					xml.append(">\n");
				}
			} else {
				xml.append(">\n");
				renderStructure(xml, formStructure, includeDefaultScripts,
						subSectionId, indent + FormEntryConstants.INDENT_SIZE);
				xml.append(indentation);
				xml.append("</");
				xml.append(xmlTag);
				xml.append(">\n");
			}
		}
	}
}
