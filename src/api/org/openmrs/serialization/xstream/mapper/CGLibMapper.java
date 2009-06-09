package org.openmrs.serialization.xstream.mapper;

import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * Mapper that removes the annoying CGLib signature which generates an unsuable XML (the classes
 * mentioned in there do not exist). This mapper takes care only of the writing to the XML
 * (deflating) not the other way around (inflating) because there is no need.
 * 
 * @author Costin Leau
 */
public class CGLibMapper extends MapperWrapper {
	
	public static final String marker = new String("EnhancerByCGLIB");
	
	public CGLibMapper(Mapper wrapped) {
		super(wrapped);
	}
	
	/**
	 * @see com.thoughtworks.xstream.mapper.Mapper#serializedClass(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public String serializedClass(Class type) {
		String classNameWithoutEnhanced = removeSignature(super.serializedClass(type));
		Class actualClass = null;
		try {
			//here re-get the actual class in order to put the alias name of the acutal class(which is delegated by cglib proxy) into xml string
			actualClass = Class.forName(classNameWithoutEnhanced);
		}
		catch (ClassNotFoundException e) {
			return classNameWithoutEnhanced;
		}
		//here assure xstream can get the alias name of the actual class which is proxied by cglib
		return super.serializedClass(actualClass);
	}
	
	/**
	 * @see com.thoughtworks.xstream.mapper.Mapper#serializedMember(java.lang.Class,
	 *      java.lang.String)
	 */
	public String serializedMember(Class type, String memberName) {
		return removeSignature(super.serializedMember(type, memberName));
	}
	
	/**
	 * Convenience method to remove "EnhancerByCGLIB" string.
	 * 
	 * @param name the class name that has "EnhancerByCGLIB" in it.
	 * @return the class name with the marker (EnhancerByCGLIB) in it
	 */
	private String removeSignature(String name) {
		int count = name.indexOf(marker);
		if (count >= 0) {
			count -= 2;
			return name.substring(0, count);
		}
		return name;
	}
}
