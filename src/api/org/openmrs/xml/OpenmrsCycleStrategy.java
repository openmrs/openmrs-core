package org.openmrs.xml;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.OpenmrsConstants;
import org.simpleframework.xml.graph.CycleStrategy;
import org.simpleframework.xml.stream.NodeMap;

/**
 * This is a specific serialization strategy developed so that the OpenMRS objects that happen to be
 * proxied by Hibernate will be serialized correctly. Use: <code>
 * 		Serializer serializer = new Persister(new OpenmrsCycleStrategy());
 * 		serializer.write(someObject, outputStream);
 * </code>
 * 
 * @see OpenmrsWriteState that knows about the OpenmrsWriteGraph
 * @see OpenmrsWriteGraph that knows the special hibernate stuff
 * 
 * @deprecated - Use OpenmrsSerializer from Context.getSerializationService.getDefaultSerializer()
 */
@Deprecated
public class OpenmrsCycleStrategy extends CycleStrategy {
	
	private static final Log log = LogFactory.getLog(OpenmrsCycleStrategy.class);
	
	/**
	 * If true, the serialization will be kept to a minimum as decided in the methods marked with @Replace
	 * annotations
	 */
	private boolean shortSerialization = false;
	
	/**
	 * Overriding the default constructor so we can set our label logic onto the strategy. This
	 * label logic will convert hibernate proxy class names to their equivalent pojo class names
	 */
	public OpenmrsCycleStrategy() {
		super();
		setLabelLogic(new OpenmrsLabelLogic());
		setReferenceLogic(new OpenmrsReferenceLogic());
	}
	
	/**
	 * Custom constructor to set whether this serialization will be a short one or not. The
	 * 
	 * @param isShortSerialization
	 */
	public OpenmrsCycleStrategy(boolean isShortSerialization) {
		this();
		this.shortSerialization = isShortSerialization;
	}
	
	/**
	 * @see org.simpleframework.xml.graph.CycleStrategy#setRoot(java.lang.Class, java.lang.Object,
	 *      org.simpleframework.xml.stream.NodeMap, java.util.Map)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean setRoot(Class field, Object value, NodeMap node, Map map) {
		
		// if the constructor was called to mark this as a short serialization,
		// put that property into the session map so that the @Replace methods
		// have access and know about it.
		if (shortSerialization)
			map.put(OpenmrsConstants.SHORT_SERIALIZATION, Boolean.TRUE);
		
		log.debug("Setting root as class: " + field);
		
		// continue as normal
		return super.setRoot(field, value, node, map);
	}
	
}
