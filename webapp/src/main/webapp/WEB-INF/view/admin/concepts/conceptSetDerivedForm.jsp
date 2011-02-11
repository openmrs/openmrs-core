<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Concepts" otherwise="/login.htm" redirect="/admin/concepts/conceptSetDerived.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="ConceptSetDerived.title"/></h2>

<form method="post">
	<spring:message code="ConceptSetDerived.instructions"/>
	<br/><br/>
	<spring:message code="ConceptSetDerived.conceptId"/> <input type="text" name="conceptId"> <spring:message code="ConceptSetDerived.conceptId.optional"/><br/><br/>
	<input type="submit" value="<spring:message code="ConceptSetDerived.manage"/>">
</form>

<script type="text/javascript">
	document.forms[0].elements[0].focus();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>