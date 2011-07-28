<%@tag
	import="org.openmrs.web.attribute.handler.FieldGenAttributeHandler"%>
<%@tag import="java.util.Map"%>
<%@tag import="org.openmrs.api.context.Context"%>
<%@tag import="org.openmrs.web.obs.handler.FieldGenObsHandler"%>
<%@tag import="org.openmrs.obs.ComplexObsHandler"%>
<%@tag import="org.openmrs.Concept"%>
<%@tag import="org.openmrs.Obs"%>
<%@tag import="org.openmrs.ConceptComplex"%>
<%@tag import="org.openmrs.api.ConceptService;"%>

<%@ include file="/WEB-INF/template/include.jsp"%>

<%--
You must specify concept and formFieldName. valueComplex and Obs are optional
--%>

<%@ attribute name="concept" required="true" type="org.openmrs.Concept"%>
<%@ attribute name="obs" required="false" type="org.openmrs.Obs"%>
<%@ attribute name="valueComplex" required="false"
	type="java.lang.String"%>

<%
	ComplexObsHandler handlerObs = null;
	ConceptService cs = Context.getConceptService();
	Object object = null;
	
	if( valueComplex != null){
	if (concept != null) {
	ConceptComplex conceptComplex = cs.getConceptComplex(concept.getConceptId());
	handlerObs = Context.getObsService().getHandler(conceptComplex.getHandler());	
	}
	}else if(valueComplex == null){
		if (concept != null) {
			ConceptComplex conceptComplex = null;
			conceptComplex = cs.getConceptComplex(concept.getConceptId());
			if(conceptComplex != null){
			conceptComplex.getHandler();
			 handlerObs = Context.getObsService().getHandler(conceptComplex.getHandler());  
			}
		}
	}
	
	String formFieldName = "valueComplex";
	String valueString = "";
	
	if (valueComplex != null) {
		valueString = valueComplex;
	}
	if (handlerObs != null) {
%>

<% if (handlerObs instanceof FieldGenObsHandler) {
	FieldGenObsHandler h = (FieldGenObsHandler) handlerObs;
    String widgetName = h.getWidgetName();
    if(obs != null && obs instanceof Obs){
     object = h.getValue((Obs)obs);
    }
%>

<%if ((obs != null ) && (h != null)) { %>
<openmrs:fieldGen formFieldName="<%=formFieldName %>"
	type="<%= widgetName %>" val="<%= object %>" />
<%}  %>

<% } %>
<% } else {%>
<input type="text" name="valueComplex" value="" />
<% } %>


