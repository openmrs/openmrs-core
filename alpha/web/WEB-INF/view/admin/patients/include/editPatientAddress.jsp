<table>
	<tr>
		<td><spring:message code="general.preferred"/></td>
		<td>
			<spring:bind path="preferred">
				<input type="hidden" name="_${status.expression}">
				<input type="checkbox" name="${status.expression}" onclick="preferredBoxClick(this)" alt="patientAddress" <c:if test="${status.value == true}">checked</c:if> />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="PatientAddress.address1"/></td>
		<td colspan="4">
			<spring:bind path="address1">
				<input type="text" name="${status.expression}" value="${status.value}" size="43" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="PatientAddress.address2"/></td>
		<td colspan="4">
			<spring:bind path="address2">
				<input type="text" name="${status.expression}" value="${status.value}" size="43" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="PatientAddress.cityVillage"/></td>
			<spring:bind path="cityVillage">
				<td>
					<input type="text" name="${status.expression}" value="${status.value}" onKeyUp="modifyTab(this, this.value, 0);" size="12" />
				</td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		<td><spring:message code="PatientAddress.stateProvince"/></td>
			<spring:bind path="stateProvince">
				<td>
					<input type="text" name="${status.expression}" size="12" value="${status.value}" />
				</td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		<td><spring:message code="PatientAddress.country"/></td>
			<spring:bind path="country">
				<td>
					<input type="text" name="${status.expression}" size="11" value="${status.value}" />
				</td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		<td><spring:message code="PatientAddress.postalCode"/></td>
			<spring:bind path="postalCode">
				<td>
					<input type="text" name="${status.expression}" size="5" value="${status.value}" />
				</td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
	</tr>
	<tr>
		<td><spring:message code="PatientAddress.latitude"/></td>
			<spring:bind path="latitude">
				<td>
					<input type="text" name="${status.expression}" value="${status.value}" size="12"/>
				</td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		<td>Longitude</td>
			<spring:bind path="longitude">
				<td>
					<input type="text" name="${status.expression}" value="${status.value}" size="12"/>
				</td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
	</tr>
	<spring:bind path="creator">
		<c:if test="${!(status.value == null)}">
			<tr>
				<td><spring:message code="general.createdBy" /></td>
				<td colspan="4">
					${status.value.firstName} ${status.value.lastName} -
					<spring:bind path="dateCreated">
						${status.value}
					</spring:bind>
				</td>
			</tr>
		</c:if>
	</spring:bind>
	<tr>
		<td><spring:message code="general.voided"/></td>
		<td>
			<spring:bind path="voided">
				<input type="hidden" name="_${status.expression}"/>
				<input type="checkbox" name="${status.expression}" 
					   <c:if test="${status.value == true}">checked="checked"</c:if> 
					   onClick="voidedBoxClick(this)"
				/>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="general.voidReason"/></td>
		<spring:bind path="voidReason">
			<td colspan="4">
				<input type="text" name="add${status.expression}" value="${status.value}" size="43" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<spring:bind path="voidedBy">
		<c:if test="${!(status.value == null)}">
			<tr>
				<td><spring:message code="general.createdBy" /></td>
				<td colspan="4">
					${status.value.firstName} ${status.value.lastName} -
					<spring:bind path="dateVoided">
						${status.value}
					</spring:bind>
				</td>
			</tr>
		</c:if>
	</spring:bind>
	
</table>