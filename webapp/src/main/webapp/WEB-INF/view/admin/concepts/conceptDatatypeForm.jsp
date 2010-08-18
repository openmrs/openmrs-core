<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Concept Datatypes" otherwise="/login.htm" redirect="/admin/concepts/conceptDatatype.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="ConceptDatatype.title"/></h2>

<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptDatatypeForm.afterTitle" type="html" parameters="conceptDatatypeId=${conceptDatatype.conceptDatatypeId}" />

<%--  <form method="post"> --%>
<table>
	<tr>
		<td><spring:message code="general.name"/></td>
		<td>
			<spring:bind path="conceptDatatype.name">
				<input type="text" name="name" value="${status.value}" size="35" readonly="1"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td>
			<spring:bind path="conceptDatatype.description">
				<textarea name="description" rows="3" cols="40" type="_moz" readonly="1">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="ConceptDatatype.hl7Abbreviation"/></td>
		<td>
			<spring:bind path="conceptDatatype.hl7Abbreviation">
				<input type="text" name="hl7Abbreviation" value="${status.value}" size="5" readonly="1"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(conceptDatatype.creator == null)}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td>
				${conceptDatatype.creator.personName} -
				<openmrs:formatDate date="${conceptDatatype.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
</table>
<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptDatatypeForm.inForm" type="html" parameters="conceptDatatypeId=${conceptDatatype.conceptDatatypeId}" />
<br />
<%-- <input type="submit" value="<spring:message code="ConceptDatatype.save"/>">
</form> --%>

<div id="conceptDatatypeFormReadOnly">(<spring:message code="general.readonly"/>)</div>

<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptDatatypeForm.footer" type="html" parameters="conceptDatatypeId=${conceptDatatype.conceptDatatypeId}" />

<%@ include file="/WEB-INF/template/footer.jsp" %>