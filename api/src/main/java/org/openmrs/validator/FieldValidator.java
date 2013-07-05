package org.openmrs.validator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
/****/
public class RelationshipTypeValidator implements Validator {
	/** Log for this class and subclasses */
protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Determines if the command object being submitted is a valid type
 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		return c.equals(RelationshipType.class);
	}
	
	/**
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail validation if aIsToB is null or empty
	 * @should fail validation if bIsToA is null or empty
	 * @should fail validation if non-retired record with same combination exists
	 * @should fail validation if non-retired record with reverse combination exists
	 * @should pass validation if retired record with same combination exists
	 */
	public void validate(Object obj, Errors errors) {
		RelationshipType relationshipType = (RelationshipType) obj;
		if (relationshipType == null) {
			errors.rejectValue("relationshipType", "error.general");
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "aIsToB", "error.name");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "bIsToA", "error.name");

			if (!errors.hasFieldErrors("aIsToB") && !errors.hasFieldErrors("bIsToA"))
			{
				//check duplicate name
				List<RelationshipType> rts = Context.getPersonService().getAllRelationshipTypes();
				
				String aIsToB = relationshipType.getaIsToB().toUpperCase();
				String bIsToA = relationshipType.getbIsToA().toUpperCase();

				if (!relationshipType.isRetired() && rts != null) {
					for (RelationshipType dupobj : rts) {
						if (!dupobj.getId().equals(relationshipType.getId()) && !dupobj.isRetired()) {
							if (dupobj.getaIsToB().toUpperCase().equals(aIsToB)
							        && dupobj.getbIsToA().toUpperCase().equals(bIsToA)) {
								errors.rejectValue("aIsToB", "general.error.nameAlreadyInUse");
							errors.rejectValue("bIsToA", "general.error.nameAlreadyInUse");
								break;
							} else if (dupobj.getaIsToB().toUpperCase().equals(bIsToA)
							        && dupobj.getbIsToA().toUpperCase().equals(aIsToB)) {
								errors.rejectValue("aIsToB", "general.error.nameAlreadyInUse");
								errors.rejectValue("bIsToA", "general.error.nameAlreadyInUse");
								break;
							}
						}
					}
				}	
			}
		}
	}
}