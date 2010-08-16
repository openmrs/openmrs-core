<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Implementation Id" otherwise="/login.htm" redirect="/admin/maintenance/implementationid.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="localHeader.jsp" %>

<h2><spring:message code="ImplementationId.setup" /></h2>

<br/>

<form action="" method="post">
	<table>
		<tr>
			<spring:bind path="implId.name" >
				<td style="white-space: nowrap"><spring:message code="ImplementationId.name"/></td>
				<td><input type="text" name="${status.expression}" value="${status.value}" size="40" /></td>
				<td class="description"><spring:message code="ImplementationId.name.help"/></td>
			</spring:bind>
		</tr>
		<tr>
			<spring:bind path="implId.implementationId">
				<td style="white-space: nowrap"><spring:message code="ImplementationId.implementationId"/></td>
				<td><input type="text" value="${status.value}" name="${status.expression}" maxlength="20" size="8"/></td>
				<td class="description"><spring:message code="ImplementationId.sourceId.help"/></td>
			</spring:bind>
		</tr>
		<tr>
			<spring:bind path="implId.passphrase">
				<td><spring:message code="ImplementationId.passphrase"/></td>
				<td><input type="text" value="${status.value}" name="${status.expression}" maxlength="255" size="40"/></td>
				<td class="description"><spring:message code="ImplementationId.passphrase.help"/></td>
			</spring:bind>
		</tr>
		<tr>
			<spring:bind path="implId.description">
				<td valign="top"><spring:message code="general.description"/></td>
				<td><textarea name="${status.expression}" rows="3" cols="43">${status.value}</textarea></td>
				<td class="description" valign="top"><spring:message code="ImplementationId.description.help"/></td>
			</spring:bind>
		</tr>
		<tr>
			<td></td>
			<td colspan="2"><br/><input type="submit" value='<spring:message code="ImplementationId.save"/>'/></td>
		</tr>
	</table>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>