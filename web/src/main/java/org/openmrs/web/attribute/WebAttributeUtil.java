/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.attribute;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.attribute.Attribute;
import org.openmrs.attribute.AttributeType;
import org.openmrs.attribute.BaseAttribute;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatypeHandler;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.customdatatype.CustomValueDescriptor;
import org.openmrs.customdatatype.Customizable;
import org.openmrs.web.attribute.handler.FieldGenDatatypeHandler;
import org.openmrs.web.attribute.handler.WebDatatypeHandler;
import org.springframework.validation.BindingResult;

/**
 * Web-layer utility methods related to customizable {@link Attribute}s
 * @since 1.9
 */
public class WebAttributeUtil {
	
	@SuppressWarnings( { "unchecked", "rawtypes" })
	public static Object getValue(HttpServletRequest request, CustomValueDescriptor descriptor, String paramName) {
		CustomDatatype<?> datatype = CustomDatatypeUtil.getDatatype(descriptor);
		CustomDatatypeHandler handler = CustomDatatypeUtil.getHandler(descriptor);
		return getValue(request, datatype, handler, paramName);
	}
	
	/**
	 * Gets the value of an attribute out of an HTTP request, treating it according to the appropriate handler type.
	 *
	 * @param request
	 * @param handler
	 * @param paramName
	 * @return
	 */
	public static <T> T getValue(HttpServletRequest request, CustomDatatype<T> dt,
	        CustomDatatypeHandler<CustomDatatype<T>, T> handler, String paramName) {
		if (handler != null) {
			if (handler instanceof FieldGenDatatypeHandler) {
				return ((FieldGenDatatypeHandler<CustomDatatype<T>, T>) handler).getValue(dt, request, paramName);
			} else if (handler instanceof WebDatatypeHandler) {
				return ((WebDatatypeHandler<CustomDatatype<T>, T>) handler).getValue(dt, request, paramName);
			}
		}
		
		String submittedValue = request.getParameter(paramName);
		if (StringUtils.isNotEmpty(submittedValue)) { // check empty instead of blank, because " " is meaningful
			return dt.fromReferenceString(submittedValue);
		} else {
			return null;
		}
	}
	
	/**
	 * Reusable regex pattern for {@link #getFromSquareBrackets(String)}
	 */
	private static Pattern betweenSquareBrackets = Pattern.compile("\\[(\\d*)\\]");
	
	/**
	 * something[3] -> 3
	 *
	 * @param input
	 * @return
	 */
	private static Integer getFromSquareBrackets(String input) {
		Matcher matcher = betweenSquareBrackets.matcher(input);
		matcher.find();
		return Integer.valueOf(matcher.group(1));
	}
	
	/**
	 * Finds an existing attribute in a Customizable parent with the given id
	 *
	 * @param owner
	 * @param existingAttributeId
	 * @return
	 */
	private static <T extends Attribute<?, ?>> T findAttributeById(Customizable<T> owner, Integer existingAttributeId) {
		for (T candidate : owner.getActiveAttributes()) {
			if (candidate.getId().equals(existingAttributeId)) {
				return candidate;
			}
		}
		return null;
	}
	
	/**
	 * Helper method to void an attribute
	 *
	 * @param existing
	 */
	private static void voidAttribute(Attribute<?, ?> existing) {
		existing.setVoided(true);
		existing.setVoidedBy(Context.getAuthenticatedUser());
		existing.setDateVoided(new Date());
	}
	
	/**
	 * Handles attributes submitted on a form that uses the "attributesForType" tag
	 *
	 * @param owner the object that the attributes will be applied to
	 * @param errors Spring binding object for owner
	 * @param attributeClass the actual class of the attribute we need to instantiate, e.g. LocationAttribute
	 * @param request the user's submission
	 * @param attributeTypes all available attribute types for owner's class
	 */
	public static <AttributeClass extends BaseAttribute, CustomizableClass extends Customizable<AttributeClass>, AttributeTypeClass extends AttributeType<CustomizableClass>> void handleSubmittedAttributesForType(
	        CustomizableClass owner, BindingResult errors, Class<AttributeClass> attributeClass, HttpServletRequest request,
	        List<AttributeTypeClass> attributeTypes) {
		// TODO figure out if this toVoid thing is still relevant
		List<AttributeClass> toVoid = new ArrayList<AttributeClass>(); // a bit of a hack to avoid voiding things if there are errors
		for (AttributeType<?> attrType : attributeTypes) {
			CustomDatatype dt = CustomDatatypeUtil.getDatatype(attrType);
			CustomDatatypeHandler handler = CustomDatatypeUtil.getHandler(attrType);
			// Look for parameters starting with "attribute.${ attrType.id }". They may be either of: 
			// * attribute.${ attrType.id }.new[${ meaningless int }]
			// * attribute.${ attrType.id }.existing[${ existingAttribute.id }]
			for (@SuppressWarnings("unchecked")
			Enumeration<String> iter = request.getParameterNames(); iter.hasMoreElements();) {
				String paramName = iter.nextElement();
				if (paramName.startsWith("attribute." + attrType.getId())) {
					String afterPrefix = paramName.substring(("attribute." + attrType.getId()).length());
					Object valueAsObject;
					try {
						valueAsObject = getValue(request, dt, handler, paramName);
					}
					catch (Exception ex) {
						errors.rejectValue("activeAttributes", "attribute.error.invalid",
						    new Object[] { attrType.getName() }, "Illegal value for " + attrType.getName());
						continue;
					}
					if (afterPrefix.startsWith(".new[")) {
						// if not empty, we create a new one
						if (valueAsObject != null && !"".equals(valueAsObject)) {
							AttributeClass attr;
							try {
								attr = attributeClass.newInstance();
							}
							catch (Exception ex) {
								throw new RuntimeException(ex);
							}
							attr.setAttributeType(attrType);
							attr.setValue(valueAsObject);
							owner.addAttribute(attr);
						}
						
					} else if (afterPrefix.startsWith(".existing[")) {
						// if it has changed, we edit the existing one
						Integer existingAttributeId = getFromSquareBrackets(afterPrefix);
						AttributeClass existing = findAttributeById(owner, existingAttributeId);
						if (existing == null) {
							throw new RuntimeException("Visit was modified between page load and submit. Try again.");
						}
						if (valueAsObject == null) {
							// they changed an existing value to "", so we void that attribute
							toVoid.add(existing);
						} else if (!existing.getValue().equals(valueAsObject)) {
							// they changed an existing value to a new value
							toVoid.add(existing);
							AttributeClass newVal;
							try {
								newVal = attributeClass.newInstance();
							}
							catch (Exception ex) {
								throw new RuntimeException(ex);
							}
							newVal.setAttributeType(attrType);
							newVal.setValue(valueAsObject);
							owner.addAttribute(newVal);
						}
					}
				}
			}
		}
		
		for (Attribute<?, ?> attr : toVoid) {
			voidAttribute(attr);
		}
	}
	
}
