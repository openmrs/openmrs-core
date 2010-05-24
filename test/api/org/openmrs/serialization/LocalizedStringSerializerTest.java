package org.openmrs.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;
import org.openmrs.LocalizedString;
import org.openmrs.test.Verifies;

public class LocalizedStringSerializerTest {
	
	/**
	 * @see {@link LocalizedStringSerializer#serialize(Object)}
	 */
	@Test
	@Verifies(value = "should return null if given object is null", method = "serialize(Object)")
	public void serialize_shouldReturnNullIfGivenObjectIsNull() throws Exception {
		OpenmrsSerializer serializer = new LocalizedStringSerializer();
		assertNull(serializer.serialize(null));
	}
	
	/**
	 * @see {@link LocalizedStringSerializer#serialize(Object)}
	 */
	@Test
	@Verifies(value = "should not fail if given object hasnt variants", method = "serialize(Object)")
	public void serialize_shouldNotFailIfGivenObjectHasntVariants() throws Exception {
		LocalizedString ls = new LocalizedString();
		ls.setUnlocalizedValue("Favorite Color");
		assertNull(ls.getVariants());
		String expected = "Favorite Color";
		OpenmrsSerializer serializer = new LocalizedStringSerializer();
		String actual = serializer.serialize(ls);
		assertEquals(expected, actual);
	}
	
	/**
	 * @see {@link LocalizedStringSerializer#serialize(Object)}
	 */
	@Test
	@Verifies(value = "should serialize correctly if given object has variants", method = "serialize(Object)")
	public void serialize_shouldSerializeCorrectlyIfGivenObjectHasVariants() throws Exception {
		LocalizedString ls = new LocalizedString();
		ls.setUnlocalizedValue("Favorite Color");
		Map<Locale, String> variants = new LinkedHashMap<Locale, String>();
		variants.put(new Locale("en", "UK"), "Favourite Colour");
		variants.put(new Locale("fr"), "Couleur pr¨¦f¨¦r¨¦e");
		ls.setVariants(variants);
		String expected = "Favorite Color^v1^en_UK:Favourite Colour;fr:Couleur pr¨¦f¨¦r¨¦e";
		OpenmrsSerializer serializer = new LocalizedStringSerializer();
		String actual = serializer.serialize(ls);
		assertEquals(expected, actual);
	}
	
	/**
	 * @see {@link LocalizedStringSerializer#serialize(Object)}
	 */
	@Test(expected = SerializationException.class)
	@Verifies(value = "should throw a SerializationException if given object doesnt belong to LocalizedString", method = "serialize(Object)")
	public void serialize_shouldThrowASerializationExceptionIfGivenObjectDoesntBelongToLocalizedString() throws Exception {
		OpenmrsSerializer serializer = new LocalizedStringSerializer();
		serializer.serialize(new String("should fail"));
	}
	
	/**
	 * @see {@link LocalizedStringSerializer#deserialize(String,Class<+QT;>)}
	 */
	@Test
	@Verifies(value = "should deserialize correctly if given serializedObject contains variants", method = "deserialize(String,Class<+QT;>)")
	public void deserialize_shouldDeserializeCorrectlyIfGivenSerializedObjectContainsVariants() throws Exception {
		String s = "Favorite Color^v1^en_UK:Favourite Colour;fr:Couleur pr¨¦f¨¦r¨¦e";
		LocalizedString expected = new LocalizedString();
		expected.setUnlocalizedValue("Favorite Color");
		Map<Locale, String> variants = new HashMap<Locale, String>();
		variants.put(new Locale("en", "UK"), "Favourite Colour");
		variants.put(new Locale("fr"), "Couleur pr¨¦f¨¦r¨¦e");
		expected.setVariants(variants);
		OpenmrsSerializer serializer = new LocalizedStringSerializer();
		LocalizedString actual = serializer.deserialize(s, LocalizedString.class);
		assertEquals(expected, actual);
	}
	
	/**
	 * @see {@link LocalizedStringSerializer#deserialize(String,Class<+QT;>)}
	 */
	@Test
	@Verifies(value = "should not fail if given serializedObject doesnt contains variants", method = "deserialize(String,Class<+QT;>)")
	public void deserialize_shouldNotFailIfGivenSerializedObjectDoesntContainsVariants() throws Exception {
		String s = "Favorite Color";
		OpenmrsSerializer serializer = new LocalizedStringSerializer();
		LocalizedString actual = serializer.deserialize(s, LocalizedString.class);
		assertNull(actual.getVariants());
		LocalizedString expected = new LocalizedString();
		expected.setUnlocalizedValue("Favorite Color");
		assertEquals(expected, actual);
	}
	
	/**
	 * @see {@link LocalizedStringSerializer#deserialize(String,Class<+QT;>)}
	 */
	@Test
	@Verifies(value = "should return null if given serializedObject is empty", method = "deserialize(String,Class<+QT;>)")
	public void deserialize_shouldReturnNullIfGivenSerializedObjectIsEmpty() throws Exception {
		String s = "";
		OpenmrsSerializer serializer = new LocalizedStringSerializer();
		assertNull(serializer.deserialize(s, LocalizedString.class));
	}
	
	/**
	 * @see {@link LocalizedStringSerializer#deserialize(String,Class<+QT;>)}
	 */
	@Test
	@Verifies(value = "should return null if given serializedObject is null", method = "deserialize(String,Class<+QT;>)")
	public void deserialize_shouldReturnNullIfGivenSerializedObjectIsNull() throws Exception {
		String s = null;
		OpenmrsSerializer serializer = new LocalizedStringSerializer();
		assertNull(serializer.deserialize(s, LocalizedString.class));
	}
	
	/**
	 * @see {@link LocalizedStringSerializer#deserialize(String,Class<+QT;>)}
	 */
	@Test(expected = SerializationException.class)
	@Verifies(value = "should throw a SerializationException if given clazz doesnt equal with LocalizedString class", method = "deserialize(String,Class<+QT;>)")
	public void deserialize_shouldThrowASerializationExceptionIfGivenClazzDoesntEqualWithLocalizedStringClass()
	                                                                                                           throws Exception {
		OpenmrsSerializer serializer = new LocalizedStringSerializer();
		serializer.deserialize("Favorite Color", String.class);
	}
}
