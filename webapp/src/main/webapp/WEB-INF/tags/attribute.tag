<%@tag import="org.openmrs.customdatatype.CustomDatatypeHandler"%>
<%@tag import="org.openmrs.customdatatype.CustomDatatype"%>
<%@tag import="org.openmrs.customdatatype.CustomDatatypeUtil"%>
<%@tag import="org.openmrs.web.attribute.handler.FieldGenDatatypeHandler"%>
<%@tag import="java.util.Map"%>
<%@tag import="org.openmrs.api.context.Context"%>

<%@ include file="/WEB-INF/template/include.jsp" %>
<%--
You must specify either attributeType or handler.
You must specify formFieldName
--%>
<%@ attribute name="attributeType" required="false" type="org.openmrs.attribute.AttributeType" %>
<%@ attribute name="value" required="false" type="org.openmrs.attribute.Attribute" %>
<%@ attribute name="formFieldName" required="true" type="java.lang.String" %>
<%
//CustomDatatype dt = CustomDatatypeUtil.getDatatype(attributeType);
CustomDatatypeHandler handler = CustomDatatypeUtil.getHandler(attributeType);

if (handler instanceof FieldGenDatatypeHandler) {
    FieldGenDatatypeHandler h = (FieldGenDatatypeHandler) handler;
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
		valueAsString = value.getValueReference();
%>
    <input type="text" name="${ formFieldName }" value="<%= valueAsString %>"/>
<% } %>