<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Concepts" otherwise="/login.htm" redirect="/admin/concepts/conceptWord.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="ConceptWord.title"/></h2>

<form method="post">
	<spring:message code="ConceptWord.instructions"/>
	<br/><br/>
	Concept Id: <input type="text" name="conceptId"/> (Optional)<br/><br/>
	<input type="submit" value="<spring:message code="ConceptWord.manage"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>