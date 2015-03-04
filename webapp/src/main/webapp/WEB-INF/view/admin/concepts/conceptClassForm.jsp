<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Concept Classes" otherwise="/login.htm" redirect="/admin/concepts/conceptClass.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="ConceptClass.title"/></h2>

<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptClassForm.afterTitle" type="html" parameters="conceptClassId=${conceptClass.conceptClassId}" />
<spring:hasBindErrors name="conceptClass">
    <openmrs_tag:errorNotify errors="${errors}" />
</spring:hasBindErrors>
<form method="post">
<table>
	<tr>
		<td><openmrs:message code="general.name"/><span class="required">*</span></td>
		<td>
			<spring:bind path="conceptClass.name">
				<input type="text" name="name" value="<c:out value="${status.value}" />" size="35" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><openmrs:message code="general.description"/></td>
		<td>
			<spring:bind path="conceptClass.description">
				<textarea name="description" rows="3" cols="40"><c:out value="${status.value}" /></textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(conceptClass.creator == null)}">
		<tr>
			<td><openmrs:message code="general.createdBy" /></td>
			<td>
				<c:out value="${conceptClass.creator.personName}" /> -
				<openmrs:formatDate date="${conceptClass.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
    <tr>
     <c:if test="${conceptClass.conceptClassId != null}">
          <td><font color="#D0D0D0"><sub><openmrs:message code="general.uuid"/></sub></font></td>
          <td colspan="${fn:length(locales)}"><font color="#D0D0D0"><sub>${conceptClass.uuid}</sub></font></td>
     </c:if>
   </tr>
</table>
<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptClassForm.inForm" type="html" parameters="conceptClassId=${conceptClass.conceptClassId}" />
<br />
<input type="submit" value="<openmrs:message code="ConceptClass.save"/>">
</form>

<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptClassForm.footer" type="html" parameters="conceptClassId=${conceptClass.conceptClassId}" />

<%@ include file="/WEB-INF/template/footer.jsp" %>