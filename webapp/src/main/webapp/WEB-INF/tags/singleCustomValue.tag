<%@tag import="java.util.Random"%>
<%@tag import="org.openmrs.customdatatype.CustomDatatype"%>
<%@tag import="org.openmrs.customdatatype.CustomDatatypeHandler"%>
<%@tag import="org.openmrs.customdatatype.CustomDatatypeUtil"%>
<%@tag import="org.openmrs.web.attribute.handler.FieldGenDatatypeHandler"%>
<%@tag import="org.openmrs.web.attribute.handler.WebDatatypeHandler"%>
<%@tag import="java.util.Map"%>
<%@tag import="org.openmrs.api.context.Context"%>

<%@ include file="/WEB-INF/template/include.jsp" %>
<%--
You must specify either customValueDescriptor or value.
You must specify formFieldName
--%>
<%@ attribute name="customValueDescriptor" required="false" type="org.openmrs.customdatatype.CustomValueDescriptor" %>
<%@ attribute name="value" required="false" type="org.openmrs.customdatatype.SingleCustomValue" %>
<%@ attribute name="formFieldName" required="true" type="java.lang.String" %>
<%
if (value != null) {
	customValueDescriptor = value.getDescriptor();
}
CustomDatatypeHandler handler = CustomDatatypeUtil.getHandler(customValueDescriptor);
Object initialValue = value == null ? null : value.getValue();

if (handler instanceof FieldGenDatatypeHandler) {
    FieldGenDatatypeHandler h = (FieldGenDatatypeHandler) handler;
    String widgetName = h.getWidgetName();
    Map<String, Object> widgetConfig = h.getWidgetConfiguration();
%>
    <openmrs:fieldGen
        formFieldName="${ formFieldName }"
        type="<%= widgetName %>"
        parameterMap="<%= widgetConfig %>"
        val="<%= initialValue %>"/>
<% } else if (handler instanceof WebDatatypeHandler) {
	String widgetId = "customValue" + new Random().nextInt();
	WebDatatypeHandler h = (WebDatatypeHandler) handler;
	CustomDatatype dt = CustomDatatypeUtil.getDatatype(customValueDescriptor);
%>

	<%= h.getWidgetHtml(dt, formFieldName, widgetId, initialValue) %>

<% } else {
	String valueAsString = "";
	if (value != null)
		valueAsString = value.getValueReference();
%>
    <input type="text" name="${ formFieldName }" value="<%= valueAsString %>"/>
<% } %>
