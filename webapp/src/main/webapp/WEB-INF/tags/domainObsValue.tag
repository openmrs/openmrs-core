<%@tag import="org.openmrs.web.attribute.handler.FieldGenAttributeHandler"%>
<%@tag import="java.util.Map"%>
<%@tag import="org.openmrs.api.context.Context"%>

<%@ include file="/WEB-INF/template/include.jsp" %>

<%--
You must specify both concept and formFieldName
--%>

<%@ attribute name="concept" required="true" type="org.openmrs.Concept" %>
<%@ attribute name="formFieldName" required="true" type="java.lang.String" %>

<% 
    String widgetName = "org.openmrs.Patient";
    String formFieldName ="valueComplex";  
%>
    <openmrs:fieldGen
        formFieldName="${ formFieldName }"
        type="<%= widgetName %>"
        val="" />
    

