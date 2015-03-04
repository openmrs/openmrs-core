<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Concept Sources" otherwise="/login.htm" redirect="/admin/concepts/conceptSource.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="ConceptSource.title"/></h2>

<c:if test="${isImplementationId}">
<br/><openmrs:message code="ConceptSource.isImplementationId"/><br/><br/>
</c:if>

<c:if test="${conceptSource.conceptSourceId == null}">
	<form method="post">
</c:if>

<table>
	<tr>
		<td><openmrs:message code="general.name"/></td>
		<td>
			<spring:bind path="conceptSource.name">
				<input type="text" name="name" value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
		<td class="description"><openmrs:message code="ConceptSource.name.help"/></td>
	</tr>
	<tr>
		<td><openmrs:message code="ConceptSource.hl7Code"/></td>
		<td>
			<spring:bind path="conceptSource.hl7Code">
				<input type="text" name="hl7Code" value="${status.value}" size="35" maxlength="20" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
		<td class="description"><openmrs:message code="ConceptSource.hl7Code.help"/></td>
	</tr>
	<tr>
		<td valign="top"><openmrs:message code="general.description"/></td>
		<td>
			<spring:bind path="conceptSource.description">
				<textarea name="description" rows="3" cols="40">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
		<td class="description"><openmrs:message code="ConceptSource.description.help"/></td>
	</tr>
	<c:if test="${conceptSource.creator != null}">
		<tr>
			<td><openmrs:message code="general.createdBy" /></td>
			<td>
				${conceptSource.creator.personName} -
				<openmrs:formatDate date="${conceptSource.dateCreated}" type="long" />
			</td>
			<td class="description"></td>
		</tr>
	</c:if>
</table>
<br />
<c:choose>
	<c:when test="${conceptSource.conceptSourceId == null}">
		<input type="submit" value='<openmrs:message code="ConceptSource.save"/>'>
		</form>
	</c:when>
	<c:otherwise>
		<openmrs:message code="ConceptSource.cannotBeEdited"/>
	</c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/template/footer.jsp" %>