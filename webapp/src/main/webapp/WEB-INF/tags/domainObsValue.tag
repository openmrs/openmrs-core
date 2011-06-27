<%@tag import="org.openmrs.web.attribute.handler.FieldGenAttributeHandler"%>
<%@tag import="java.util.Map"%>
<%@tag import="org.openmrs.api.context.Context"%>
<%@tag import="org.openmrs.web.obs.handler.FieldGenObsHandler"%>
<%@tag import="org.openmrs.obs.ComplexObsHandler"%>
<%@tag import="org.openmrs.Concept"%>
<%@tag import="org.openmrs.ConceptComplex"%>
<%@tag import="org.openmrs.api.ConceptService;"%>

<%@ include file="/WEB-INF/template/include.jsp"%>

<%--
You must specify concept and formFieldName. valueComplex is optional
--%>

<%@ attribute name="concept" required="true" type="org.openmrs.Concept"%>
<%@ attribute name="formFieldName" required="true" type="java.lang.String"%>
<%@ attribute name="valueComplex" required="false" type="java.lang.String"%>

<%
	ComplexObsHandler handlerObs = null;
	ConceptService cs = Context.getConceptService();
	ConceptComplex conceptComplex = cs.getConceptComplex(6100);
	String valueString = null;
	
	if(valueComplex != null){
		valueString = valueComplex;
	}
	if (handlerObs != null){
%>

<% if (handlerObs instanceof FieldGenObsHandler) {
	FieldGenObsHandler h = (FieldGenObsHandler) handlerObs;
    String widgetName = h.getWidgetName();
%>
<openmrs:fieldGen formFieldName="${ formFieldName }"
	type="<%= widgetName %>" val="" />

<% } %>
<% } else {%>
<input type="text" name="${ formFieldName }" value="<%= valueString %>" />
<% } %>

