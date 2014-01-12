<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Concept Datatypes"
	otherwise="/login.htm" redirect="/admin/concepts/conceptDatatype.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<h2>
	<openmrs:message code="ConceptDatatype.title" />
</h2>

<openmrs:extensionPoint
	pointId="org.openmrs.admin.concepts.conceptDatatypeForm.afterTitle"
	type="html"
	parameters="conceptDatatypeId=${conceptDatatype.conceptDatatypeId}" />

<table class="left-aligned-th">
	<tr>
		<th><openmrs:message code="general.name" /></th>
		<td><c:out value="${conceptDatatype.name}" /></td>
	</tr>
	<tr>
		<th valign="top"><openmrs:message code="general.description" /></th>
		<td><openmrs:message code="${conceptDatatype.description}" /></td>
	</tr>
	<tr>
		<th><openmrs:message code="ConceptDatatype.hl7Abbreviation" /></th>
		<td><c:out value="${conceptDatatype.hl7Abbreviation}" /></td>
	</tr>
	<c:if test="${!(conceptDatatype.creator == null)}">
		<tr>
			<th><openmrs:message code="general.createdBy" /></th>
			<td><c:out value="${conceptDatatype.creator.personName}" /> - <openmrs:formatDate
					date="${conceptDatatype.dateCreated}" type="long" /></td>
		</tr>
	</c:if>
	<tr>
		<c:if test="${conceptDatatype.conceptDatatypeId != null}">
			<th><font color="#D0D0D0"><sub><openmrs:message
							code="general.uuid" /></sub></font></th>
			<td colspan="${fn:length(locales)}"><font color="#D0D0D0"><sub>${conceptDatatype.uuid}</sub></font></td>
		</c:if>
	</tr>
</table>
<openmrs:extensionPoint
	pointId="org.openmrs.admin.concepts.conceptDatatypeForm.inForm"
	type="html"
	parameters="conceptDatatypeId=${conceptDatatype.conceptDatatypeId}" />
<br />

<div id="conceptDatatypeFormReadOnly">
	(
	<openmrs:message code="general.readonly" />
	)
</div>

<openmrs:extensionPoint
	pointId="org.openmrs.admin.concepts.conceptDatatypeForm.footer"
	type="html"
	parameters="conceptDatatypeId=${conceptDatatype.conceptDatatypeId}" />

<%@ include file="/WEB-INF/template/footer.jsp"%>