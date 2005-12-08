<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Concepts" otherwise="/login.htm" redirect="/admin/concepts/conceptSetDerived.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="ConceptSetDerived.title"/></h2>

<form method="post">
	<spring:message code="ConceptSetDerived.instructions"/>
	<br/><br/>
	Concept Id: <input type="text" name="conceptId"> (Optional)<br/><br/>
	<input type="submit" value="<spring:message code="ConceptSetDerived.manage"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>