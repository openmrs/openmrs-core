<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Concept Datatypes" otherwise="/login.htm" redirect="/admin/concepts/conceptDatatype.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="ConceptDatatype.title"/></h2>

<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptDatatypeForm.afterTitle" type="html" parameters="conceptDatatypeId=${conceptDatatype.conceptDatatypeId}" />

<table>
	<tr>
		<td><spring:message code="general.name"/></td>
		<td><c:out value="${conceptDatatype.name}"/></td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td><spring:message code="${conceptDatatype.description}"/></td>
	</tr>
	<tr>
		<td><spring:message code="ConceptDatatype.hl7Abbreviation"/></td>
		<td><c:out value="${conceptDatatype.hl7Abbreviation}"/></td>
	</tr>
	<tr>
		<td><spring:message code="general.uuid"/></td>
		<td><c:out value="${conceptDatatype.uuid}"/></td>
	</tr>

	<c:if test="${!(conceptDatatype.creator == null)}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td>
				<c:out value="${conceptDatatype.creator.personName}"/> -
				<openmrs:formatDate date="${conceptDatatype.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
</table>
<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptDatatypeForm.inForm" type="html" parameters="conceptDatatypeId=${conceptDatatype.conceptDatatypeId}" />
<br />

<div id="conceptDatatypeFormReadOnly">(<spring:message code="general.readonly"/>)</div>

<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptDatatypeForm.footer" type="html" parameters="conceptDatatypeId=${conceptDatatype.conceptDatatypeId}" />

<%@ include file="/WEB-INF/template/footer.jsp" %>