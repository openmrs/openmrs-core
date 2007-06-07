package org.openmrs.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to describe service layer authorization attributes.
 * 
 * <p>For example:
 * <pre>
 *     &#64;Authorized ({"View Users"})
 *     public void getUsersByName(String name);
 * </pre> 
 *
 * @author Justin Miranda
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Authorized {

    /**
     * Returns the list of privileges needed to access a method. (i.e. "View Users")
     * 
     * @return String[] The secure method attributes 
     */
    public String[] value();
}
