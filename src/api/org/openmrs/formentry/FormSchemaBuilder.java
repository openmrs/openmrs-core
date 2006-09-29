package org.openmrs.formentry;

import java.util.Collection;
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
 * Context.authenticate(username, password);
 * Form myForm = Context.getFormService().getForm(myFormId);
 * String schema = new FormSchemaBuilder(Context, myForm).getSchema();
 * </pre>
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class FormSchemaBuilder {

	Form form;
	TreeMap<Integer, TreeSet<FormField>> formStructure;
	Vector<String> tagList;
	Vector<ComplexType> schemaSections;
	Vector<ComplexType> complexTypes;
	StringBuffer schema;

	/**
	 * Construct a schema builder for a given form within a given Context
	 * 
	 * @param Context
	 * @param form
	 */
	public FormSchemaBuilder(Form form) {
		this.form = form;
	}

	/**
	 * Generates an XML schema from an OpenMRS form definition.
	 * 
	 * @return schema for form
	 */
	public synchronized String getSchema() {

		init(); // initialize variables

		// Start with form schema header
		schema.append(FormSchemaFragment.header(form));

		// define main form section (top level)
		schema.append(FormSchemaFragment.startForm());
		formStructure = FormUtil.getFormStructure(form);
		for (FormField section : formStructure.get(0)) {
			String sectionName = FormUtil.getXmlToken(section.getField()
					.getName());
			String sectionTag = FormUtil.getNewTag(sectionName, tagList);
			ComplexType ct = ComplexType.getComplexType(formStructure,
					schemaSections, section, sectionTag + "_section", tagList);
			String sectionTypeTag = ct.getToken();
			schema.append("      <xs:element name=\"" + sectionTag
					+ "\" type=\"" + sectionTypeTag + "\" />\n");
		}
		schema
				.append("      <xs:element name=\"other\" type=\"_other_section\" />\n");
		schema.append(FormSchemaFragment.closeForm(form));
		schema.append(FormSchemaFragment.predefinedTypes());

		// render sections
		TreeSet<FormField> section = formStructure.get(0);
		while (section != null) {
			section = renderSection(section);
		}

		// render element definitions (types)
		for (ComplexType complexType : complexTypes) {
			String token = complexType.getToken();
			Field field = complexType.getField();
			boolean required = complexType.isRequired();
			if (field.getFieldType().getFieldTypeId().equals(
					FormEntryConstants.FIELD_TYPE_CONCEPT)) {
				Concept concept = field.getConcept();
				ConceptDatatype datatype = concept.getDatatype();
				if (FormEntryConstants.simpleDatatypes.containsKey(datatype
						.getHl7Abbreviation()))
					schema.append(FormSchemaFragment.simpleConcept(token,
							concept, FormEntryConstants.simpleDatatypes
									.get(datatype.getHl7Abbreviation()),
							required, Context.getLocale()));
				else if (datatype.getHl7Abbreviation().equals(
						FormEntryConstants.HL7_NUMERIC)) {
					ConceptNumeric conceptNumeric = Context.getConceptService()
							.getConceptNumeric(concept.getConceptId());
					schema.append(FormSchemaFragment.numericConcept(token,
							conceptNumeric, required, Context.getLocale()));
				} else if (datatype.getHl7Abbreviation().equals(
						FormEntryConstants.HL7_CODED)
						|| datatype.getHl7Abbreviation().equals(
								FormEntryConstants.HL7_CODED_WITH_EXCEPTIONS)) {
					Collection<ConceptAnswer> answers = field.getConcept()
							.getAnswers();
					if (field.getSelectMultiple())
						schema.append(FormSchemaFragment.selectMultiple(token,
								concept, answers, Context.getLocale()));
					else
						schema.append(FormSchemaFragment
								.selectSingle(token, concept, answers,
										required, Context.getLocale()));
				}
			}
		}

		// add footer to complete the schema
		schema.append(FormSchemaFragment.footer());

		return schema.toString();
	}

	/**
	 * Initialize global variables
	 */
	private void init() {
		tagList = new Vector<String>();
		schemaSections = new Vector<ComplexType>();
		complexTypes = new Vector<ComplexType>();
		schema = new StringBuffer(); // build schema using StringBuffer for
		// speed
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
	private TreeSet<FormField> renderSection(TreeSet<FormField> section) {
		TreeSet<FormField> subSectionList = null;
		for (FormField sectionFormField : section) {
			ComplexType sectionType = ComplexType.getComplexType(formStructure,
					schemaSections, sectionFormField, sectionFormField
							.getField().getName()
							+ "_section", tagList);
			if (sectionType.isRendered())
				continue;
			sectionType.setRendered(true);
			TreeSet<FormField> sectionFormFieldList = formStructure
					.get(sectionFormField.getFormFieldId());
			schema.append("<xs:complexType name=\"" + sectionType.getToken()
					+ "\">\n");
			schema.append("  <xs:sequence>\n");
			if (sectionFormFieldList != null) {
				Vector<String> subSectionTagList = new Vector<String>();
				for (FormField subSectionFormField : sectionFormFieldList) {
					String elemTag = FormUtil.getNewTag(subSectionFormField
							.getField().getName(), subSectionTagList);
					String elemTypeTag;
					if (formStructure.containsKey(subSectionFormField
							.getFormFieldId())) {
						if (subSectionList == null)
							subSectionList = new TreeSet<FormField>();
						subSectionList.add(subSectionFormField);
						ComplexType ct = ComplexType.getComplexType(
								formStructure, schemaSections,
								subSectionFormField, elemTag + "_section",
								tagList);
						elemTypeTag = ct.getToken();
					} else
						elemTypeTag = getNewTypeTag(subSectionFormField);
					if (subSectionFormField.getField().getFieldType()
							.getFieldTypeId().equals(
									FormEntryConstants.FIELD_TYPE_DATABASE)) {
						schema.append("    <xs:element name=\"" + elemTag
								+ "\" ");
						if (subSectionFormField.getMinOccurs() != null)
							schema.append("minOccurs=\""
									+ subSectionFormField.getMinOccurs()
									+ "\" ");
						if (subSectionFormField.getMaxOccurs() != null)
							schema.append("maxOccurs=\""
									+ subSectionFormField.getMaxOccurs()
									+ "\" ");
						schema
								.append("nillable=\""
										+ (subSectionFormField.isRequired() ? "0"
												: "1") + "\">\n");
						schema.append("      <xs:complexType>\n");
						schema.append("        <xs:simpleContent>\n");
						schema.append("          <xs:extension base=\""
								+ elemTypeTag + "\">\n");
						schema
								.append("            <xs:attribute name=\"openmrs_table\" type=\"xs:string\" use=\"required\" fixed=\""
										+ subSectionFormField.getField()
												.getTableName() + "\" />\n");
						schema
								.append("            <xs:attribute name=\"openmrs_attribute\" type=\"xs:string\" use=\"required\" fixed=\""
										+ subSectionFormField.getField()
												.getAttributeName() + "\" />\n");
						schema.append("          </xs:extension>\n");
						schema.append("        </xs:simpleContent>\n");
						schema.append("      </xs:complexType>\n");
						schema.append("    </xs:element>\n");
					} else {
						schema.append("    <xs:element name=\"" + elemTag
								+ "\" type=\"" + elemTypeTag + "\" ");
						if (subSectionFormField.getMinOccurs() != null)
							schema.append("minOccurs=\""
									+ subSectionFormField.getMinOccurs()
									+ "\" ");
						if (subSectionFormField.getMaxOccurs() != null)
							schema.append("maxOccurs=\""
									+ maxOccursValue(subSectionFormField
											.getMaxOccurs()) + "\" ");
						schema
								.append("nillable=\""
										+ (subSectionFormField.isRequired() ? "0"
												: "1") + "\" />\n");
					}
				}
			}
			schema.append("  </xs:sequence>\n");
			if (sectionFormField.getField().getFieldType().getFieldTypeId()
					.equals(FormEntryConstants.FIELD_TYPE_CONCEPT)) {
				Concept concept = sectionFormField.getField().getConcept();

				schema
						.append("  <xs:attribute name=\"openmrs_concept\" type=\"xs:string\" use=\"required\" fixed=\""
								+ FormUtil.conceptToString(concept, Context
										.getLocale()) + "\" />\n");
				schema
						.append("  <xs:attribute name=\"openmrs_datatype\" type=\"xs:string\" use=\"required\" fixed=\""
								+ concept.getDatatype().getHl7Abbreviation()
								+ "\" />\n");
			}
			schema.append("</xs:complexType>\n\n");
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
	 * Returns a unique tag name for field type definitions (simply adds "_type"
	 * to the end of the tag name and ensures that the tag name is unique and
	 * valid). Field is added to an array so that these complex types
	 * definitions can be rendered later.
	 * 
	 * @param f
	 *            <code>FormField</code> from which to derive type
	 * @return unique tag name for datatype of given <code>FormField</code>
	 */
	private String getNewTypeTag(FormField f) {
		if (f.getField().getFieldType().getFieldTypeId().equals(
				FormEntryConstants.FIELD_TYPE_CONCEPT)) {
			ComplexType ct = new ComplexType(formStructure, f);
			String typeTag;
			int i = complexTypes.indexOf(ct);
			if (i >= 0)
				typeTag = complexTypes.get(i).token;
			else {
				typeTag = FormUtil.getNewTag(FormUtil.getXmlToken(f.getField()
						.getName()
						+ "_type"), tagList);
				ct.token = typeTag;
				complexTypes.add(ct);
			}
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
