package org.openmrs.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

/**
 * A variation of Spring's <tt>DelegatingPasswordEncoder</tt> that falls back to the 
 * supplied <tt>fallbackEncoder</tt> without using a prefix.
 * <p/>
 * Spring's <tt>DelegatingPasswordEncoder</tt> winds up prepending the "{<encoder>}"
 * to the password. However, we want to support existing passwords with no formatting,
 * so with this variation, our default fallback generates and validates passwords 
 * without that prefix.
 */
public class OpenmrsDelegatingPasswordEncoder implements PasswordEncoder {
	
	private final PasswordEncoder defaultEncoder;
	
	private final PasswordEncoder fallbackEncoder;
	
	private final String idForEncode;
	
	private final Map<String, PasswordEncoder> idToPasswordEncoder;
	
	public OpenmrsDelegatingPasswordEncoder(String idForEncode, Map<String, PasswordEncoder> idToPasswordEncoder, PasswordEncoder fallbackEncoder) {
		this.fallbackEncoder = fallbackEncoder;
		
		if (idForEncode == null || idForEncode.isEmpty()) {
			this.defaultEncoder = fallbackEncoder;
		} else if (!idToPasswordEncoder.containsKey(idForEncode)) {
			throw new IllegalArgumentException("The encoder named '" + idForEncode + "' is not configured for this instance of OpenmrsDelegatingPasswordEncoder");
		} else {
			defaultEncoder = idToPasswordEncoder.get(idForEncode);
		}
		
		this.idForEncode = idForEncode;
		this.idToPasswordEncoder = idToPasswordEncoder;
	}

	@Override
	public String encode(CharSequence rawPassword) {
		if (defaultEncoder == fallbackEncoder) {
			return defaultEncoder.encode(rawPassword);
		}
		
		return "{" + idForEncode + "}" + defaultEncoder.encode(rawPassword);
	}

	@Override
	public boolean matches(CharSequence rawPassword, String prefixedPassword) {
		if (rawPassword == null && prefixedPassword == null) {
			return true;
		}
		
		String id = extractId(prefixedPassword);
		String encodedPassword = prefixedPassword;
		// if we have an id
		if (id != null && !id.isEmpty()) {
			encodedPassword = encodedPassword.substring(encodedPassword.indexOf("}") + 1);
		}
		
		PasswordEncoder encoder = idToPasswordEncoder.get(id);
		if (encoder == null) {
			return defaultEncoder.matches(rawPassword, encodedPassword);
		}
		
		return encoder.matches(rawPassword, encodedPassword);
	}

	@Override
	public boolean upgradeEncoding(String prefixedPassword) {
		return extractId(prefixedPassword) == null && idForEncode != null && !idForEncode.isEmpty();
	}

	private String extractId(String prefixEncodedPassword) {
		if (prefixEncodedPassword == null) {
			return null;
		}
		
		int start = prefixEncodedPassword.indexOf("{");
		if (start != 0) {
			return null;
		}
		
		int end = prefixEncodedPassword.indexOf("}", start);
		if (end < 0) {
			return null;
		}
		
		return prefixEncodedPassword.substring(start + 1, end);
	}
}
