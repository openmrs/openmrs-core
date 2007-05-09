package org.openmrs.reporting;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

public class ReportObjectXMLDecoder {

	//private Log log = LogFactory.getLog(this.getClass());
	
	private String xmlToDecode;
	
	public ReportObjectXMLDecoder ( String xmlToDecode ) {
		this.xmlToDecode = xmlToDecode;
	}
	
	public AbstractReportObject toAbstractReportObject() {
		ExceptionListener exListener = new ReportObjectWrapperExceptionListener();
		XMLDecoder dec = new XMLDecoder(new BufferedInputStream(new ByteArrayInputStream(xmlToDecode.getBytes())), null, exListener);
		AbstractReportObject o = (AbstractReportObject)dec.readObject();
	    dec.close();
	    /* TODO: handle things like this, which doesn't include name:
	 <void property="question"> 
      <object id="ConceptNumeric0" class="org.openmrs.ConceptNumeric"> 
       <void property="conceptClass"> 
        <object class="org.openmrs.ConceptClass$$EnhancerByCGLIB$$f8e7bddb"/> 
       </void> 
       <void property="conceptId"> 
        <int>5497</int> 
       </void> 
       <void property="creator"> 
        <object class="org.openmrs.User$$EnhancerByCGLIB$$4e1e4f76"> 
         <void property="userId"> 
          <int>2</int> 
         </void> 
        </object> 
       </void> 
       <void property="datatype"> 
        <object class="org.openmrs.ConceptDatatype$$EnhancerByCGLIB$$7e1176f"/> 
       </void> 
       <void property="units"> 
        <string>cells/mm3</string> 
       </void> 
       <void property="version"> 
        <string></string> 
       </void> 
      </object> 
     </void> 
	     */
	    return o;
	}

	/**
	 * @return Returns the xmlToDecode.
	 */
	public String getXmlToDecode() {
		return xmlToDecode;
	}
	/**
	 * @param xmlToDecode The xmlToDecode to set.
	 */
	public void setXmlToDecode(String xmlToDecode) {
		this.xmlToDecode = xmlToDecode;
	}

}
