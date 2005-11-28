<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Concepts" otherwise="/login.htm" redirect="/admin/concepts/conceptDatatype.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="ConceptDatatype.title"/></h2>

<form method="post">
<table>
	<tr>
		<td><spring:message code="general.name"/></td>
		<td>
			<spring:bind path="conceptDatatype.name">
				<input type="text" name="name" value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td>
			<spring:bind path="conceptDatatype.description">
				<textarea name="description" rows="3" cols="40">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${conceptDatatype.conceptDatatypeId != null}">
		<tr>
			<td><spring:message code="general.creator"/></td>
			<td>${conceptDatatype.creator}</td>
		</tr>
		<tr>
			<td><spring:message code="general.dateCreated"/></td>
			<td>${conceptDatatype.dateCreated}</td>
		</tr>
	</c:if>
</table>
<input type="hidden" name="conceptDatatypeId:int" value="${conceptDatatype.conceptDatatypeId}">
<br />
<input type="submit" value="<spring:message code="ConceptDatatype.save"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>