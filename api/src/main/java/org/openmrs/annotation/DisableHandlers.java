package org.openmrs.annotation;

import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.handler.RequiredDataHandler;

import java.lang.annotation.*;

/**
 *    In RequiredDataAdvice, by default, RequiredDataHandlers are called on all child collections of the OpenmrsObject being handled.
 *    By annotating a Collection with a @DisableHandlers annotation, you specific that RequiredDataAdvice should NOT apply the specified
 *    handler(s) to a child collection.  For example:
 *
 *      private class ClassWithDisableHandlersAnnotation extends BaseOpenmrsData {
 *          @DisableHandlers(handlerTypes = {VoidHandler.class, SaveHandler.class})
 *          private List<Person> persons;
 *      }
 *
 **/

@Target( { ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface DisableHandlers {
	
	/**
	 * The set of handlers to be be disabled
	 */
	public Class<? extends RequiredDataHandler>[] handlerTypes() default {};
	
}
