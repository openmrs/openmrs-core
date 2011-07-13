<%@tag import="org.apache.commons.lang.StringUtils"%>
<%@tag import="org.openmrs.attribute.Attribute"%>
<%@tag import="java.util.ArrayList" %>
<%@tag import="java.util.List" %>

<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="attributeType" required="true" type="org.openmrs.attribute.AttributeType" %>
<%@ attribute name="customizable" required="true" type="org.openmrs.attribute.Customizable" %>
<%
	List<Attribute> existing = customizable.getActiveAttributes(attributeType);
	int howManyToShow = attributeType.getMaxOccurs() == null ? 1 : attributeType.getMaxOccurs();
	howManyToShow = Math.min(howManyToShow, existing.size());

	List<Object> values = new ArrayList<Object>();
	for (int i = 0; i < howManyToShow; ++i) {
		Attribute val = existing.get(i);
		if (val.getObjectValue() != null)
			values.add(val.getObjectValue());
	}
	String displayedValue = StringUtils.join(values, ", ");
%>
<%=displayedValue%>
