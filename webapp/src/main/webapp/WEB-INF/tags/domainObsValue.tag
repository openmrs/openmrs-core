<%@tag
	import="org.openmrs.web.attribute.handler.FieldGenAttributeHandler"%>
<%@tag import="java.util.Map"%>
<%@tag import="org.openmrs.api.context.Context"%>
<%@tag import="org.openmrs.web.obs.handler.FieldGenObsHandler"%>
<%@tag import="org.openmrs.obs.ComplexObsHandler"%>

<%@ include file="/WEB-INF/template/include.jsp"%>

<%--
You must specify both concept and formFieldName
--%>

<%@ attribute name="concept" required="true" type="org.openmrs.Concept"%>
<%@ attribute name="formFieldName" required="true"
	type="java.lang.String"%>

<%
	ComplexObsHandler handler = Context.getObsService().getHandler("PatientHandler");
%>
<% if (handler instanceof FieldGenObsHandler) {
	FieldGenObsHandler h = (FieldGenObsHandler) handler;
    String widgetName = h.getWidgetName();
%>
<openmrs:fieldGen formFieldName="${ formFieldName }"
	type="<%= widgetName %>" val="" />

<% } else {
%>

<% } %>


