<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Update HL7 Source" otherwise="/login.htm" redirect="/admin/hl7/hl7Source.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>
<script type="text/javascript">

	function confirmPurge() {
		if (confirm("<spring:message code="general.confirm.purge"/>")) {
			return true;
		} else {
			return false;
		}
	}
	
</script>


<h2><spring:message code="Hl7Source.title"/></h2>

<openmrs:extensionPoint pointId="org.openmrs.admin.hl7.Hl7SourceForm.belowTitle" type="html" parameters="id=${hl7Source.id}" />

<spring:hasBindErrors name="hl7Source">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>
<form method="post">
<fieldset>
<table>
	<tr>
		<td><spring:message code="general.name"/></td>
		<td>
			<spring:bind path="hl7Source.name">
				<input type="text" name="name" value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}"><c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td valign="top">
			<spring:bind path="hl7Source.description">
				<textarea name="description" rows="3" cols="40" onkeypress="return maxLength(this, 1024);" >${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if></c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(hl7Source.creator == null)}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td>
				${hl7Source.creator.personName} -
				<openmrs:formatDate date="${hl7Source.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
</table>
<br />

<openmrs:extensionPoint pointId="org.openmrs.admin.hl7.ehl7SourceForm.inForm" type="html" parameters="id=${hl7Source.id}" />

<input type="submit" value="<spring:message code="HL7Source.save"/>" name="save">

</fieldset>
</form>

<br/>



<br/>

<c:if test="${not empty hl7Source.id}">
	<openmrs:hasPrivilege privilege="Purge HL7 Source">
		<form id="purge" method="post" onsubmit="return confirmPurge()">
			<fieldset>
				<h4><spring:message code="HL7Source.purgeHL7Source"/></h4>
				<input type="submit" value='<spring:message code="HL7Source.purgeHL7Source"/>' name="purge" />
			</fieldset>
		</form>
	</openmrs:hasPrivilege>
</c:if>

<openmrs:extensionPoint pointId="org.openmrs.admin.hl7.hl7SourceForm.footer" type="html" parameters="id=${hl7Source.id}" />

<%@ include file="/WEB-INF/template/footer.jsp" %>
