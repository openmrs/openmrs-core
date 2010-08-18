<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Concept Classes" otherwise="/login.htm" redirect="/admin/concepts/conceptClass.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="ConceptClass.title"/></h2>

<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptClassForm.afterTitle" type="html" parameters="conceptClassId=${conceptClass.conceptClassId}" />

<form method="post">
<table>
	<tr>
		<td><spring:message code="general.name"/></td>
		<td>
			<spring:bind path="conceptClass.name">
				<input type="text" name="name" value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td>
			<spring:bind path="conceptClass.description">
				<textarea name="description" rows="3" cols="40">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(conceptClass.creator == null)}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td>
				${conceptClass.creator.personName} -
				<openmrs:formatDate date="${conceptClass.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
</table>
<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptClassForm.inForm" type="html" parameters="conceptClassId=${conceptClass.conceptClassId}" />
<br />
<input type="submit" value="<spring:message code="ConceptClass.save"/>">
</form>

<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptClassForm.footer" type="html" parameters="conceptClassId=${conceptClass.conceptClassId}" />

<%@ include file="/WEB-INF/template/footer.jsp" %>