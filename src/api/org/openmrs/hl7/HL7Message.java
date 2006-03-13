package org.openmrs.hl7;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.io.IOUtils;

public class HL7Message {

	String[] segments;
	HL7Segment[] hl7Segments;
	int cursor = 0;
	String fieldSeparator;
	String componentSeparator;
	String repetitionSeparator;
	String escapeCharacter;
	String subcomponentSeparator;
	Date dateTime;
	String messageType;
	String eventType;
	String controlId;
	String processingId;
	String version;
	String profileId;
	
	public HL7Message(String message) throws HL7Exception {
		parse(message);
	}
	
	public HL7Message(InputStream messageStream) throws IOException, HL7Exception {
		this(IOUtils.toString(messageStream));
	}
	
	private void parse(String message) throws HL7Exception {
		segments = message.split("\\r");
		if (segments.length < 1)
			throw new HL7Exception("Invalid message format");
		
		// initialize array for lazy creation of HL7Segments
		hl7Segments = new HL7Segment[segments.length];
		
		// Messages must begin with a message header (MSH) segment
		String msh = segments[0];
		if (!msh.startsWith("MSH"))
			throw new HL7Exception("Invalid message format - must begin with message header (MSH) segment");
		
		// Parse the MSH segment before proceeding
		try {
			fieldSeparator = msh.substring(3,4);
			String[] mshFields = msh.split("\\" + fieldSeparator);
			
			String encodingCharacters = safeArrayElement(mshFields, 1);			
			componentSeparator = safeSubstring(encodingCharacters, 0, 1);
			repetitionSeparator = safeSubstring(encodingCharacters, 1, 2);
			escapeCharacter = safeSubstring(encodingCharacters, 2, 3);
			subcomponentSeparator = safeSubstring(encodingCharacters, 3, 4);
			
			dateTime = HL7Util.parseHL7Timestamp(mshFields[6]);
			String[] typeComponents = safeArrayElement(mshFields,8).split("\\" + componentSeparator);
			messageType = safeArrayElement(typeComponents, 0);
			eventType = safeArrayElement(typeComponents, 1);
			controlId = safeArrayElement(mshFields, 9);
			processingId = safeArrayElement(mshFields, 10);
			version = safeArrayElement(mshFields, 11);
			profileId = safeArrayElement(mshFields, 20);
		} catch (Exception e) {
			throw new HL7Exception("Error parsing MSH segment", e);
		}
		
	}

	/* Return a substring while tolerating invalid index references */
	private String safeSubstring(String s, int beginIndex, int endIndex) {
		if (beginIndex < 0 || beginIndex >= s.length() || endIndex <= beginIndex)
			return "";
		if (endIndex >= s.length())
			return s.substring(beginIndex);
		return s.substring(beginIndex, endIndex);
	}
	
	private String safeArrayElement(String[] array, int index) {
		if (array == null || index < 0 || index >= array.length)
			return "";
		return array[index];
	}

	public String getComponentSeparator() {
		return componentSeparator;
	}

	public String getEscapeCharacter() {
		return escapeCharacter;
	}

	public String getFieldSeparator() {
		return fieldSeparator;
	}

	public String getRepetitionSeparator() {
		return repetitionSeparator;
	}

	public String getSubcomponentSeparator() {
		return subcomponentSeparator;
	}
	
	public Date getDateTime() {
		return dateTime;
	}

	public String getMessageType() {
		return messageType;
	}
	
	public String getEventType() {
		return eventType;
	}

	public String getControlId() {
		return controlId;
	}

	public String getProcessingId() {
		return processingId;
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getProfileId() {
		return profileId;
	}
	
	private HL7Segment getSegment(int index) {
		if (hl7Segments[index] == null)
			hl7Segments[index] = new HL7Segment(this, segments[index]);
		return hl7Segments[index];
	}
	
	public HL7Segment getNextSegment() {
		if (cursor < segments.length-1) {
			cursor++;
			return getSegment(cursor);
		}
		return null;
	}
	
	public HL7Segment getNextSegment(String segmentId) {
		HL7Segment segment;
		do {
			segment = getNextSegment();
		} while (segment != null && !segment.getId().equals(segmentId));
		return segment;
	}
	
	public boolean hasNextSegment(String segmentId) {
		if (cursor < segments.length-1) {
			return getSegment(cursor+1).getId().equals(segmentId);
		}
		return false;
	}
	
	public void resetCursor() {
		cursor = 0;
	}

}
