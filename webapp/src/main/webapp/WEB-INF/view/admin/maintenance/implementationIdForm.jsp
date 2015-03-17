<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Implementation Id" otherwise="/login.htm" redirect="/admin/maintenance/implementationid.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="ImplementationId.setup" /></h2>

<br/>

<form action="" method="post">
	<table>
		<tr>
			<spring:bind path="implId.name" >
				<td style="white-space: nowrap"><openmrs:message code="ImplementationId.name"/><span class="required">*</span></td>
				<td style="white-space: nowrap">
				    <input type="text" name="${status.expression}" value="${status.value}" size="40" />
				    <c:if test="${status.errorMessage != ''}">
                        <span class="error">${status.errorMessage}</span>
                    </c:if>
				</td>
				<td class="description"><openmrs:message code="ImplementationId.name.help"/></td>
			</spring:bind>
		</tr>
		<tr>
			<spring:bind path="implId.implementationId">
				<td style="white-space: nowrap"><openmrs:message code="ImplementationId.implementationId"/><span class="required">*</span></td>
				<td style="white-space: nowrap">
					<input type="text" value="${status.value}" name="${status.expression}" maxlength="20" size="8"/>
					<c:if test="${status.errorMessage != ''}">
						<span class="error">${status.errorMessage}</span>
					</c:if>
				</td>
				<td class="description"><openmrs:message code="ImplementationId.sourceId.help"/></td>
			</spring:bind>
		</tr>
		<tr>
			<spring:bind path="implId.passphrase">
				<td><openmrs:message code="ImplementationId.passphrase"/><span class="required">*</span></td>
				<td style="white-space: nowrap">
					<input type="text" value="${status.value}" name="${status.expression}" maxlength="255" size="40"/>
					<c:if test="${status.errorMessage != ''}">
						<span class="error">${status.errorMessage}</span>
					</c:if>
				</td>
				<td class="description"><openmrs:message code="ImplementationId.passphrase.help"/></td>
			</spring:bind>
		</tr>
		<tr>
			<spring:bind path="implId.description">
				<td valign="top"><openmrs:message code="general.description"/><span class="required">*</span></td>
				<td style="white-space: nowrap">
					<textarea name="${status.expression}" rows="3" cols="43">${status.value}</textarea>
					<c:if test="${status.errorMessage != ''}">
						<span class="error">${status.errorMessage}</span>
					</c:if>
				</td>
				<td class="description" valign="top"><openmrs:message code="ImplementationId.description.help"/></td>
			</spring:bind>
		</tr>
		<tr>
			<td></td>
			<td colspan="2"><br/><input type="submit" value='<openmrs:message code="ImplementationId.save"/>'/></td>
		</tr>
	</table>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>