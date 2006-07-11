package org.openmrs.hl7;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;

public class HL7Parser {
	
	private Log log = LogFactory.getLog(this.getClass());

	private Context context;
	
	public HL7Parser(Context context) {
		this.context = context;
	}

	public void parseHL7InQueue(HL7InQueue hl7InQueue) throws HL7Exception {
		String hl7Data = hl7InQueue.getHL7Data();
		parse(IOUtils.toInputStream(hl7Data));
	}
	
	public void parse(InputStream hl7Stream) throws HL7Exception {
		BufferedReader hl7 = new BufferedReader(new InputStreamReader(hl7Stream));

	}


}
