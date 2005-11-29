
<!-- Assumes spring:nestedPath already set on PatientName object -->
<table>
	<tr>
		<td><spring:message code="PatientName.preferred"/></td>
		<td>
			<spring:bind path="preferred">
				<input type="hidden" name="_${status.expression}">
				<input type="checkbox" name="${status.expression}" <c:if test="${status.value == true}">checked</c:if> />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="PatientName.givenName"/></td>
		<td>
			<spring:bind path="givenName">
				<input type="text" name="${status.expression}" value="${status.value}" onKeyUp="modifyTab(this, this.value, 0);" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="PatientName.middleName"/></td>
		<td>
			<spring:bind path="middleName">
				<input type="text" name="${status.expression}" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="PatientName.familyName"/></td>
		<td>
			<spring:bind path="familyNamePrefix">
				<input type="text" name="${status.expression}" size="10" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
			<spring:bind path="familyName">
				<input type="text" name="${status.expression}" value="${status.value}" onKeyUp="modifyTab(this, this.value, 2);" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
			<spring:bind path="familyName2">
				<input type="text" name="${status.expression}" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
			<spring:bind path="familyNameSuffix">
				<input type="text" name="${status.expression}" size="10" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>	
	<tr>
		<td><spring:message code="PatientName.degree"/></td>
		<td>
			<spring:bind path="degree">
				<input type="text" name="${status.expression}" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<spring:bind path="creator">
		<c:if test="${!(status.value == null)}">
			<tr>
				<td><spring:message code="general.createdBy" /></td>
				<td>
					${status.value.firstName} ${status.value.lastName} -
					<spring:bind path="dateCreated">
						${status.value}
					</spring:bind>
				</td>
			</tr>
		</c:if>
	</spring:bind>
</table>
