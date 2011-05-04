<%@tag import="org.openmrs.web.attribute.handler.FieldGenAttributeHandler"%>
<%@tag import="java.util.Map"%>
<%@tag import="org.openmrs.api.context.Context"%>

<%@ include file="/WEB-INF/template/include.jsp" %>
<%--
You must specify either attributeType or handler.
You must specify formFieldName
--%>
<%@ attribute name="handler" required="false" type="org.openmrs.attribute.handler.AttributeHandler" %>
<%@ attribute name="attributeType" required="false" type="org.openmrs.attribute.AttributeType" %>
<%@ attribute name="value" required="false" type="org.openmrs.attribute.Attribute" %>
<%@ attribute name="formFieldName" required="true" type="java.lang.String" %>
<%
if (attributeType != null) {
	handler = Context.getAttributeService().getHandler(attributeType);
}
%>
<% if (handler instanceof FieldGenAttributeHandler) {
    FieldGenAttributeHandler<?> h = (FieldGenAttributeHandler) handler;
    String widgetName = h.getWidgetName();
    Map<String, Object> widgetConfig = h.getWidgetConfiguration();
%>
    <openmrs:fieldGen
        formFieldName="${ formFieldName }"
        type="<%= widgetName %>"
        parameterMap="<%= widgetConfig %>"
        val="${ value.objectValue }"/>
    
<% } else {
	String valueAsString = "";
	if (value != null)
		valueAsString = value.getSerializedValue();
%>
    <input type="text" name="${ formFieldName }" value="<%= valueAsString %>"/>
<% } %>