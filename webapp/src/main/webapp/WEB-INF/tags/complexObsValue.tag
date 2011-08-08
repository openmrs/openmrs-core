<%@tag
	import="org.openmrs.web.attribute.handler.FieldGenAttributeHandler"%>
<%@tag import="java.util.Map"%>
<%@tag import="org.openmrs.api.context.Context"%>
<%@tag import="org.openmrs.web.obs.handler.FieldGenObsHandler"%>
<%@tag import="org.openmrs.obs.ComplexObsHandler"%>
<%@tag import="org.openmrs.Concept"%>
<%@tag import="org.openmrs.ConceptComplex"%>
<%@tag import="org.openmrs.api.ConceptService;"%>

<%@ include file="/WEB-INF/template/include.jsp"%>

<%--
You must specify concept and Obs.
--%>

<%@ attribute name="concept" required="true" type="org.openmrs.Concept"%>
<%@ attribute name="obs" required="false" type="org.openmrs.Obs"%>

<%
	ComplexObsHandler handlerObs = null;
	String formFieldName = "valueComplex";
	ConceptService cs = Context.getConceptService();
	Object object = null;
	
	if( obs.isComplex()){	
	ConceptComplex conceptComplex = cs.getConceptComplex(concept.getConceptId());
	handlerObs = Context.getObsService().getHandler(conceptComplex.getHandler());	
	object = handlerObs.getValue(obs);
	}

	if (handlerObs != null) {
%>

<% if (handlerObs instanceof FieldGenObsHandler) {
	FieldGenObsHandler h = (FieldGenObsHandler) handlerObs;
    String widgetName = h.getWidgetName();
%>

<%if (obs != null) { %>
<openmrs:fieldGen formFieldName="<%=formFieldName %>"
	type="<%= widgetName %>" val="<%= object %>" />
<%}  %>

<% } else {
	String valueComplex = null;
	if(obs != null){
		valueComplex = obs.getValueComplex();
	}
	if(valueComplex == null){
		valueComplex = "";
	}
%>
<input type="text" name="<%=formFieldName %>"
	value="<%= valueComplex %>" />

<% } %>
<% } %>

