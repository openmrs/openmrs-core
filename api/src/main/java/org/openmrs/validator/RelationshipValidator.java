package org.openmrs.validator;
 
 import java.util.Date;
 import org.openmrs.Relationship;
 import org.springframework.validation.Errors;
 import org.springframework.validation.Validator;
 
 public class RelationshipValidator implements Validator {
 	
 	@Override
   public boolean supports(Class c)
 	{
 		return Relationship.class.isAssignableFrom(c);
 	}
 	
 	@Override
 	public void validate(Object target, Errors errors)
 	{
 		Relationship reldates = (Relationship) target;
 		
 		if (reldates != null)
 		{
 			Date startDate = reldates.getStartDate();
 			Date endDate = reldates.getEndDate();
 			if (startDate != null && endDate != null)
 			{
 				if (startDate.after(endDate))
 				{
 					errors.reject("Relationship.InvalidDate.error");
 					
 				}
 			}
 	    }
 		
 	 }
 
 }