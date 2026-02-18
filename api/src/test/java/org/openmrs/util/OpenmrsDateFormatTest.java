package org.openmrs.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.junit.jupiter.api.Test;

public class OpenmrsDateFormatTest {

	@Test
	public void parse_shouldThrowNullPointerExceptionWhenTextIsNull() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		OpenmrsDateFormat format = new OpenmrsDateFormat(sdf, Locale.ENGLISH);

		assertThrows(NullPointerException.class, () -> format.parse(null));
	}

	@Test
	public void parse_shouldRejectTwoDigitYearWhenPatternEndsWithYYYY() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		OpenmrsDateFormat format = new OpenmrsDateFormat(sdf, Locale.ENGLISH);

		assertThrows(ParseException.class, () -> format.parse("09-02-26"));
	}

	@Test
	public void parse_shouldAcceptFourDigitYearWhenPatternEndsWithYYYY() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		OpenmrsDateFormat format = new OpenmrsDateFormat(sdf, Locale.ENGLISH);

		assertDoesNotThrow(() -> format.parse("09-02-2026"));
	}

	@Test
	public void parse_shouldRejectInputWithDifferentLengthWhenPatternDoesNotEndWithYYYY() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		OpenmrsDateFormat format = new OpenmrsDateFormat(sdf, Locale.ENGLISH);

		// pattern length = 8, input length = 7
		assertThrows(ParseException.class, () -> format.parse("2026029"));
	}
}
