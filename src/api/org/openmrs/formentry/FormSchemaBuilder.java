package org.openmrs.formentry;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptNumeric;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.context.Context;

/**
 * Schema generator for OpenMRS forms.
 * 
 * Usage:
 * 
 * <pre>
 * Context context = ContextFactory.getContext();
 * context.authenticate(username, password);
 * Form myForm = context.getFormService().getForm(myFormId);
 * String schema = new FormSchemaBuilder(context, myForm).getSchema();
 * </pre>
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class FormSchemaBuilder {

	Context context;
	Form form;
	TreeMap<Integer, TreeSet<FormField>> formStructure;
	String schema = null;
	Vector<String> tagList;
	Hashtable<ComplexType, String> complexTypes;

	/**
	 * Internal convenience class for collecting complex type definitions. We
	 * implement the equals() and hashCode() methods so these objects can be
	 * used as a Hashtable key without creating duplicates.
	 */
	private class ComplexType {
		Field field;
		boolean required;

		ComplexType(Field field, boolean required) {
			this.field = field;
			this.required = required;
		}

		public boolean equals(Object obj) {
			if (obj != null && obj instanceof ComplexType) {
				ComplexType ct = (ComplexType) obj;
				return (ct.field.equals(this.field) && ct.required == this.required);
			}
			return false;
		}

		public int hashCode() {
			return this.field.getFieldId();
		}
	}

	/**
	 * Construct a schema builder for a given form within a given context
	 * 
	 * @param context
	 * @param form
	 */
	public FormSchemaBuilder(Context context, Form form) {
		this.context = context;
		this.form = form;
		complexTypes = new Hashtable<ComplexType, String>();
	}

	/**
	 * Thread-safe schema generation. Once the schema is generated, subsequent
	 * calls simply return the previously generated schema.
	 * 
	 * @return schema for form
	 */
	public synchronized String getSchema() {
		if (schema != null)
			return schema;

		StringBuffer s = new StringBuffer();
		s.append(FormSchemaFragment.header(form.getSchemaNamespace()));
		s.append(FormSchemaFragment.startForm());

		formStructure = FormUtil.getFormStructure(context, form);
		for (FormField section : formStructure.get(0)) {
			String sectionName = FormUtil.getXmlToken(section.getField()
					.getName());
			String sectionTag = FormUtil.getNewTag(sectionName, tagList);
			String sectionTypeTag = FormUtil.getNewTag(sectionName + "_type",
					tagList);
			s.append("      <xs:element name=\"" + sectionTag + "\" type=\""
					+ sectionTypeTag + "\" />\n");
		}

		s.append("      <xs:element name=\"other\" type=\"_other_type\" />\n");
		s.append(FormSchemaFragment.closeForm(form.getFormId(), form.getName(),
				form.getVersion()));
		s.append(FormSchemaFragment.predefinedTypes());

		TreeSet<FormField> section = formStructure.get(0);
		while (section != null) {
			section = renderSection(section, s);
		}

		for (Enumeration<ComplexType> i = complexTypes.keys(); i
				.hasMoreElements();) {
			ComplexType complexType = i.nextElement();
			String token = complexTypes.get(complexType);
			Field field = complexType.field;
			boolean required = complexType.required;
			if (field.getFieldType().getFieldTypeId().equals(
					FormEntryConstants.FIELD_TYPE_CONCEPT)) {
				Concept concept = field.getConcept();
				ConceptDatatype datatype = concept.getDatatype();
				if (FormEntryConstants.simpleDatatypes.containsKey(datatype
						.getHl7Abbreviation()))
					s.append(FormSchemaFragment.simpleConcept(token, concept,
							FormEntryConstants.simpleDatatypes.get(datatype
									.getHl7Abbreviation()), required, context
									.getLocale()));
				else if (datatype.getHl7Abbreviation().equals(
						FormEntryConstants.HL7_NUMERIC)) {
					ConceptNumeric conceptNumeric = context.getConceptService()
							.getConceptNumeric(concept.getConceptId());
					s.append(FormSchemaFragment.numericConcept(token, concept,
							required, conceptNumeric.getLowAbsolute(),
							conceptNumeric.getHiAbsolute(), conceptNumeric
									.getPrecise(), context.getLocale()));
				} else if (datatype.getHl7Abbreviation().equals(
						FormEntryConstants.HL7_CODED)
						|| datatype.getHl7Abbreviation().equals(
								FormEntryConstants.HL7_CODED_WITH_EXCEPTIONS)) {
					Collection<ConceptAnswer> answers = field.getConcept()
							.getAnswers();
					if (field.getSelectMultiple())
						s.append(FormSchemaFragment.selectMultiple(token,
								concept, answers, context.getLocale()));
					else
						s.append(FormSchemaFragment
								.selectSingle(token, concept, answers,
										required, context.getLocale()));
				}
			}
		}

		s.append(FormSchemaFragment.footer());

		schema = s.toString();

		return schema;
	}

	/**
	 * Render a section of the schema
	 * 
	 * @param section
	 *            a sequence of <code>FormField</code>s
	 * @param s
	 *            the java.util.StringBuffer used for generating output
	 * @return a list of any elements encountered within the current section
	 *         that have children of their own (and, therefore, need subsequent
	 *         processing); otherwise, <code>null</code>.
	 */
	private TreeSet<FormField> renderSection(TreeSet<FormField> section,
			StringBuffer s) {
		TreeSet<FormField> subSectionList = null;
		for (FormField sectionFormField : section) {
			String sectionName = FormUtil.getXmlToken(sectionFormField
					.getField().getName());
			if (sectionName.equals("other"))
				continue;
			TreeSet<FormField> sectionFormFieldList = formStructure
					.get(sectionFormField.getFormFieldId());
			s.append("<xs:complexType name=\"" + sectionName + "_type\">\n");
			s.append("  <xs:sequence>\n");
			if (sectionFormFieldList != null) {
				for (FormField subSectionFormField : sectionFormFieldList) {
					String elemTag = FormUtil.getNewTag(FormUtil
							.getXmlToken(subSectionFormField.getField()
									.getName()), tagList);
					String elemTypeTag = getNewTypeTag(subSectionFormField);
					if (formStructure.containsKey(subSectionFormField
							.getFormFieldId())) {
						if (subSectionList == null)
							subSectionList = new TreeSet<FormField>();
						subSectionList.add(subSectionFormField);
					}
					if (subSectionFormField.getField().getFieldType()
							.getFieldTypeId().equals(
									FormEntryConstants.FIELD_TYPE_DATABASE)) {
						s.append("    <xs:element name=\"" + elemTag + "\" ");
						if (subSectionFormField.getMinOccurs() != null)
							s.append("minOccurs=\""
									+ subSectionFormField.getMinOccurs()
									+ "\" ");
						if (subSectionFormField.getMaxOccurs() != null)
							s.append("maxOccurs=\""
									+ subSectionFormField.getMaxOccurs()
									+ "\" ");
						s
								.append("nillable=\""
										+ (subSectionFormField.isRequired() ? "0"
												: "1") + "\">\n");
						s.append("      <xs:complexType>\n");
						s.append("        <xs:simpleContent>\n");
						s.append("          <xs:extension base=\""
								+ elemTypeTag + "\">\n");
						s
								.append("            <xs:attribute name=\"openmrs_table\" type=\"xs:string\" use=\"required\" fixed=\""
										+ subSectionFormField.getField()
												.getTableName() + "\" />\n");
						s
								.append("            <xs:attribute name=\"openmrs_attribute\" type=\"xs:string\" use=\"required\" fixed=\""
										+ subSectionFormField.getField()
												.getAttributeName() + "\" />\n");
						s.append("          </xs:extension>\n");
						s.append("        </xs:simpleContent>\n");
						s.append("      </xs:complexType>\n");
						s.append("    </xs:element>\n");
					} else {
						s.append("    <xs:element name=\"" + elemTag
								+ "\" type=\"" + elemTypeTag + "\" ");
						if (subSectionFormField.getMinOccurs() != null)
							s.append("minOccurs=\""
									+ subSectionFormField.getMinOccurs()
									+ "\" ");
						if (subSectionFormField.getMaxOccurs() != null)
							s.append("maxOccurs=\""
									+ maxOccursValue(subSectionFormField
											.getMaxOccurs()) + "\" ");
						s
								.append("nillable=\""
										+ (subSectionFormField.isRequired() ? "0"
												: "1") + "\" />\n");
					}
				}
			}
			s.append("  </xs:sequence>\n");
			if (sectionFormField.getField().getFieldType().getFieldTypeId()
					.equals(FormEntryConstants.FIELD_TYPE_CONCEPT)) {
				Concept concept = sectionFormField.getField().getConcept();

				s
						.append("  <xs:attribute name=\"openmrs_concept\" type=\"xs:string\" use=\"required\" fixed=\""
								+ FormUtil.conceptToString(concept, context
										.getLocale()) + "\" />\n");
				s
						.append("  <xs:attribute name=\"openmrs_datatype\" type=\"xs:string\" use=\"required\" fixed=\""
								+ concept.getDatatype().getHl7Abbreviation()
								+ "\" />\n");
			}
			s.append("</xs:complexType>\n\n");
		}
		return subSectionList;
	}

	/**
	 * Returns an appropriate representation of a maximum occurence for an XML
	 * schema.
	 * 
	 * @param n
	 *            defined value for maximum occurence of a particular element
	 * @return maximum occurence or "unbounded" if maximum is not defined or is
	 *         defined as -1.
	 */
	private String maxOccursValue(Integer n) {
		if (n == null || n.equals(-1))
			return "unbounded";
		return n.toString();
	}

	/**
	 * Returns a unique tag name for type definitions (simply adds "_type" to
	 * the end and ensures that the tag name is unique and valid)
	 * 
	 * @param f
	 *            <code>FormField</code> from which to derive type
	 * @return unique tag name for datatype of given <code>FormField</code>
	 */
	private String getNewTypeTag(FormField f) {
		if (f.getField().getFieldType().getFieldTypeId().equals(
				FormEntryConstants.FIELD_TYPE_CONCEPT)) {
			String typeTag = FormUtil.getNewTag(FormUtil.getXmlToken(f
					.getField().getName()
					+ "_type"), tagList);
			ComplexType ct = new ComplexType(f.getField(), f.isRequired());
			if (!complexTypes.contains(ct))
				complexTypes.put(ct, typeTag);
			return typeTag;
		} else if (f.getField().getFieldType().getFieldTypeId().equals(
				FormEntryConstants.FIELD_TYPE_DATABASE)) {
			// TODO: currently defaulting to string for all database fields
			// this will need to change!
			if (f.isRequired())
				return "_requiredString";
			return "xs:string";
		}
		return null;
	}

}
