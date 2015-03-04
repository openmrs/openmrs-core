<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Concepts" otherwise="/login.htm" redirect="/admin/concepts/conceptIndex.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="ConceptWord.title"/></h2>

<form method="post">
	<openmrs:message code="ConceptWord.instructions"/>
	<br/><br/>
	<openmrs:message code="ConceptWord.conceptId"/> <input type="text" name="conceptId"/> <openmrs:message code="ConceptWord.conceptId.optional"/><br/><br/>
	<input type="submit" value="<openmrs:message code="ConceptWord.manage"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>