package org.openmrs.form;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Map.Entry;

import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptNumeric;
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
	Vector<ComplexType> complexTypes;
	private static Hashtable<Integer, String> hl7Datatypes = null;

	// TODO: these constants should be read from a configuration file

	public static final Integer FIELD_TYPE_CONCEPT = 1;
	public static final Integer FIELD_TYPE_DATABASE = 2;
	public static final Integer FIELD_TYPE_TERM_SET = 3;
	public static final Integer FIELD_TYPE_MISC_SET = 4;
	public static final Integer FIELD_TYPE_SECTION = 5;

	public static final Integer DATATYPE_NUMERIC = 1;
	public static final Integer DATATYPE_CODED = 2;
	public static final Integer DATATYPE_TEXT = 3;
	public static final Integer DATATYPE_NA = 4;
	public static final Integer DATATYPE_DOCUMENT = 5;
	public static final Integer DATATYPE_DATE = 6;
	public static final Integer DATATYPE_TIME = 7;
	public static final Integer DATATYPE_DATETIME = 8;
	public static final Integer DATATYPE_BOOLEAN = 10;
	public static final Integer DATATYPE_STRUCTURED_NUMERIC = 12;

	public static final Integer CLASS_DRUG = 3;

	// List of datatypes that do not require complex definitions
	public static final Hashtable<Integer, String> simpleDatatypes = new Hashtable<Integer, String>();
	static {
		simpleDatatypes.put(DATATYPE_TEXT, "xs:string");
		simpleDatatypes.put(DATATYPE_DOCUMENT, "xs:string");
		simpleDatatypes.put(DATATYPE_DATE, "xs:date");
		simpleDatatypes.put(DATATYPE_TIME, "xs:time");
		simpleDatatypes.put(DATATYPE_DATETIME, "xs:dateTime");

		// We make a special boolean type with an extra attribute
		// to get InfoPath to treat booleans properly
		simpleDatatypes.put(DATATYPE_BOOLEAN, "_infopath_boolean");
	}

	/**
	 * Internal class for representing complex types
	 */
	private class ComplexType {
		String token;
		FormField formField;

		ComplexType(String token, FormField formField) {
			this.token = token;
			this.formField = formField;
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
		if (hl7Datatypes == null)
			initHl7Datatypes(context);
		tagList = new Vector<String>();
		complexTypes = new Vector<ComplexType>();
	}

	/**
	 * Used to lazy initialize HL7 abbreviations (only called once). We cannot
	 * read these data until we've gotten a context.
	 * 
	 * @param context
	 */
	private void initHl7Datatypes(Context context) {
		hl7Datatypes = new Hashtable<Integer, String>();
		for (ConceptDatatype datatype : context.getConceptService()
				.getConceptDatatypes()) {
			hl7Datatypes.put(datatype.getConceptDatatypeId(), datatype
					.getHl7Abbreviation());
		}
	}

	/**
	 * Convenience method for fetching HL7 abbreviations by datatype id
	 * 
	 * @param datatypeId
	 * @return
	 */
	public static String getHl7Datatype(Integer datatypeId) {
		return hl7Datatypes.get(datatypeId);
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

		formStructure = getFormStructure(context, form);
		for (FormField section : formStructure.get(0)) {
			String sectionName = getToken(section.getField().getName());
			String sectionTag = newTag(sectionName);
			String sectionTypeTag = newTag(sectionName + "_type");
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

		for (ComplexType complexType : complexTypes) {
			String token = complexType.token;
			FormField formField = complexType.formField;
			if (formField.getField().getFieldType().getFieldTypeId().equals(
					FIELD_TYPE_CONCEPT)) {
				Concept concept = formField.getField().getConcept();
				ConceptDatatype datatype = concept.getDatatype();
				if (simpleDatatypes
						.containsKey(datatype.getConceptDatatypeId()))
					s.append(FormSchemaFragment.simpleConcept(token, concept,
							simpleDatatypes
									.get(datatype.getConceptDatatypeId()),
							formField.isRequired()));
				else if (datatype.getConceptDatatypeId().equals(
						DATATYPE_NUMERIC)) {
					System.out.println("getting numeric concept "
							+ concept.getConceptId());
					ConceptNumeric conceptNumeric = context.getConceptService()
							.getConceptNumeric(concept.getConceptId());
					s.append(FormSchemaFragment.numericConcept(token, concept,
							formField.isRequired(), conceptNumeric
									.getLowAbsolute(), conceptNumeric
									.getHiAbsolute(), conceptNumeric
									.getPrecise()));
				} else if (datatype.getConceptDatatypeId().equals(
						DATATYPE_CODED)) {
					Collection<ConceptAnswer> answers = formField.getField()
							.getConcept().getAnswers();
					if (formField.getField().getSelectMultiple())
						s.append(FormSchemaFragment.selectMultiple(token,
								concept, answers, context.getLocale()));
					else
						s.append(FormSchemaFragment.selectSingle(token,
								concept, answers, formField.isRequired(),
								context.getLocale()));
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
			String sectionName = getToken(sectionFormField.getField().getName());
			if (sectionName.equals("other"))
				continue;
			TreeSet<FormField> sectionFormFields = formStructure
					.get(sectionFormField.getFormFieldId());
			s.append("<xs:complexType name=\"" + sectionName + "_type\">\n");
			s.append("  <xs:sequence>\n");
			for (FormField subSectionFormField : sectionFormFields) {
				String elemTag = newTag(getToken(subSectionFormField.getField()
						.getName()));
				String elemTypeTag = newTypeTag(subSectionFormField);
				if (formStructure.containsKey(subSectionFormField
						.getFormFieldId())) {
					if (subSectionList == null)
						subSectionList = new TreeSet<FormField>();
					subSectionList.add(subSectionFormField);
				}
				if (subSectionFormField.getField().getFieldType()
						.getFieldTypeId().equals(FIELD_TYPE_DATABASE)) {
					s.append("    <xs:element name=\"" + elemTag + "\" ");
					if (subSectionFormField.getMinOccurs() != null)
						s.append("minOccurs=\""
								+ subSectionFormField.getMinOccurs() + "\" ");
					if (subSectionFormField.getMaxOccurs() != null)
						s.append("maxOccurs=\""
								+ subSectionFormField.getMaxOccurs() + "\" ");
					s.append("nillable=\""
							+ (subSectionFormField.isRequired() ? "0" : "1")
							+ "\">\n");
					s.append("      <xs:complexType>\n");
					s.append("        <xs:simpleContent>\n");
					s.append("          <xs:extension base=\"" + elemTypeTag
							+ "\">\n");
					s
							.append("            <xs:attribute name=\"amrs_table\" type=\"xs:string\" use=\"required\" fixed=\""
									+ subSectionFormField.getField()
											.getTableName() + "\" />\n");
					s
							.append("            <xs:attribute name=\"amrs_attribute\" type=\"xs:string\" use=\"required\" fixed=\""
									+ subSectionFormField.getField()
											.getAttributeName() + "\" />\n");
					s.append("          </xs:extension>\n");
					s.append("        </xs:simpleContent>\n");
					s.append("      </xs:complexType>\n");
					s.append("    </xs:element>\n");
				} else {
					s.append("    <xs:element name=\"" + elemTag + "\" type=\""
							+ elemTypeTag + "\" ");
					if (subSectionFormField.getMinOccurs() != null)
						s.append("minOccurs=\""
								+ subSectionFormField.getMinOccurs() + "\" ");
					if (subSectionFormField.getMaxOccurs() != null)
						s.append("maxOccurs=\""
								+ maxOccursValue(subSectionFormField
										.getMaxOccurs()) + "\" ");
					s.append("nillable=\""
							+ (subSectionFormField.isRequired() ? "0" : "1")
							+ "\" />\n");
				}
			}
			s.append("  </xs:sequence>\n");
			s.append("</xs:complexType>\n\n");
		}
		return subSectionList;
	}

	/**
	 * Returns a sorted map of <code>FormField</code>s for the given form.
	 * The root sections of the schema are stored under a key of zero. All other
	 * entries represent sequences of children -- either children of the
	 * top-level sections OR grandchildren, etc.
	 * 
	 * @param context
	 *            current authenticated context
	 * @param form
	 *            form for which structure is requested
	 * @return sorted map of <code>FormField</code>s, where the top-level
	 *         fields are under the key zero and all other leaves are stored
	 *         under their parent <code>FormField</code>'s id.
	 */
	private static TreeMap<Integer, TreeSet<FormField>> getFormStructure(
			Context context, Form form) {
		TreeMap<Integer, TreeSet<FormField>> formStructure = new TreeMap<Integer, TreeSet<FormField>>();
		Integer base = 0;
		formStructure.put(base, new TreeSet<FormField>());

		for (FormField formField : form.getFormFields()) {
			FormField parent = formField.getParent();
			if (parent != null && parent.getFieldNumber() != null
					&& parent.getFieldNumber() > 0) {
				if (!formStructure.containsKey(parent.getFormFieldId()))
					formStructure.put(parent.getFormFieldId(),
							new TreeSet<FormField>());
				formStructure.get(parent.getFormFieldId()).add(formField);
			} else {
				formStructure.get(base).add(formField);
			}
		}

		return formStructure;
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

	/*
	 * Generates a new, unique tag name for any given string @param s string
	 * from which to create a new, unique tag name @return new, unique tag name
	 * based on given string
	 */
	private String newTag(String s) {
		if (tagList.contains(s)) {
			int i = 1;
			while (tagList.contains(s + "_" + i))
				i++;
			String tagName = s + "_" + i;
			tagList.add(tagName);
			return tagName;
		} else {
			tagList.add(s);
			return s;
		}
	}

	/**
	 * Returns a unique tag name for type definitions (simply adds "_type" to
	 * the end and ensures that the tag name is unique and valid)
	 * 
	 * @param f
	 *            <code>FormField</code> from which to derive type
	 * @return unique tag name for datatype of given <code>FormField</code>
	 */
	private String newTypeTag(FormField f) {
		if (f.getField().getFieldType().getFieldTypeId().equals(
				FIELD_TYPE_CONCEPT)) {
			String typeTag = newTag(getToken(f.getField().getName() + "_type"));
			complexTypes.add(new ComplexType(typeTag, f));
			return typeTag;
		} else if (f.getField().getFieldType().getFieldTypeId().equals(
				FIELD_TYPE_DATABASE)) {
			if (f.isRequired())
				return "_requiredString";
			return "xs:string";
		}
		return null;
	}

	/**
	 * Converts a string into a valid XML token (tag name)
	 * 
	 * @param s
	 *            string to convert into XML token
	 * @return valid XML token based on s
	 */
	public static String getToken(String s) {
		// Converts a string into a valid XML token (tag name)
		// No spaces, start with a letter or underscore, not 'xml*'

		// if len(s) < 1, return '_blank'
		if (s == null || s.length() < 1)
			return "_blank";

		// xml tokens must start with a letter
		String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_";

		// after the leading letter, xml tokens may have
		// digits, period, or hyphen
		String nameChars = letters + "0123456789.-";

		// special characters that should be replaced with valid text
		// all other invalid characters will be removed
		Hashtable<String, String> swapChars = new Hashtable<String, String>();
		swapChars.put("!", "bang");
		swapChars.put("#", "pound");
		swapChars.put("\\*", "star");
		swapChars.put("'", "apos");
		swapChars.put("\"", "quote");
		swapChars.put("%", "percent");
		swapChars.put("<", "lt");
		swapChars.put(">", "gt");
		swapChars.put("=", "eq");
		swapChars.put("/", "slash");
		swapChars.put("\\\\", "backslash");

		// start by cleaning whitespace and converting to lowercase
		s = s.replaceAll("^\\s+", "").replaceAll("\\s+$", "").replaceAll(
				"\\s+", "_").toLowerCase();

		// swap characters
		Set<Entry<String, String>> swaps = swapChars.entrySet();
		for (Entry<String, String> entry : swaps) {
			if (entry.getValue() != null)
				s = s.replaceAll(entry.getKey(), "_" + entry.getValue() + "_");
			else
				s = s.replaceAll(String.valueOf(entry.getKey()), "");
		}

		// ensure that invalid characters and consecutive underscores are
		// removed
		String token = "";
		boolean underscoreFlag = false;
		for (int i = 0; i < s.length(); i++) {
			if (nameChars.indexOf(s.charAt(i)) != -1) {
				if (s.charAt(i) != '_' || !underscoreFlag) {
					token += s.charAt(i);
					underscoreFlag = (s.charAt(i) == '_');
				}
			}
		}

		// remove extraneous underscores before returning token
		token = token.replaceAll("_+", "_");
		token = token.replaceAll("_+$", "");

		// make sure token starts with valid letter
		if (letters.indexOf(token.charAt(0)) == -1 || token.startsWith("xml"))
			token = "_" + token;

		// return token
		return token;
	}

	// TODO: For temporary testing purposes. Feel free to delete this method! -Burke
	/*
	public static void main(String[] args) {
		String filename = "c:/documents and settings/burke/desktop/java_schema.xsd";
		Context context = ContextFactory.getContext();
		try {
			context.authenticate("admin", "test");
		} catch (ContextAuthenticationException e) {
			e.printStackTrace();
		}
		Form form = context.getFormService().getForm(15);
		File file = new File(filename);
		try {
			FileWriter out = new FileWriter(file, false);
			out.write(new FormSchemaBuilder(context, form).getSchema());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	*/

}
