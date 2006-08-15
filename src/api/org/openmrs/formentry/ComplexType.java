package org.openmrs.formentry;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.openmrs.Field;
import org.openmrs.FormField;

public class ComplexType {
	TreeMap<Integer, TreeSet<FormField>> formStructure;
	FormField formField;
	String token;
	boolean rendered = false;

	ComplexType(TreeMap<Integer, TreeSet<FormField>> formStructure,
			FormField formField) {
		this(formStructure, formField, null);
	}

	ComplexType(TreeMap<Integer, TreeSet<FormField>> formStructure,
			FormField formField, String token) {
		this.formStructure = formStructure;
		this.formField = formField;
		this.token = token;
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ComplexType) {
			ComplexType ct = (ComplexType) obj;
			return equivalent(this.formField, ct.formField);
		}
		return false;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setRendered(boolean rendered) {
		this.rendered = rendered;
	}

	public boolean isRendered() {
		return rendered;
	}

	public Field getField() {
		return formField.getField();
	}

	public boolean isRequired() {
		return formField.isRequired();
	}

	public int hashCode() {
		return this.formField.getField().getFieldId();
	}

	// true if form fields are structurally equivalent in schema (can share
	// a type definition)
	public boolean equivalent(FormField a, FormField b) {
		if (a.equals(b))
			return true;
		if (!a.getField().getFieldId().equals(b.getField().getFieldId())
				|| a.isRequired() != b.isRequired())
			return false;
		TreeSet<FormField> aBranch = formStructure.get(a.getFormFieldId());
		TreeSet<FormField> bBranch = formStructure.get(b.getFormFieldId());
		if (aBranch == null || bBranch == null)
			return (aBranch == bBranch);
		Iterator aIter = aBranch.iterator();
		Iterator bIter = bBranch.iterator();
		while (aIter.hasNext()) {
			FormField aFormField = (FormField) aIter.next();
			FormField bFormField = (FormField) bIter.next();
			if (!equivalent(aFormField, bFormField))
				return false;
		}
		return true;
	}

	public static ComplexType getComplexType(
			TreeMap<Integer, TreeSet<FormField>> formStructure,
			Vector<ComplexType> list, FormField formField, String token,
			Vector<String> tagList) {
		ComplexType ct = new ComplexType(formStructure, formField);
		int index = list.indexOf(ct);
		if (index >= 0)
			return list.get(index);
		ct.setToken(FormUtil.getNewTag(token, tagList));
		list.add(ct);
		return ct;
	}
}