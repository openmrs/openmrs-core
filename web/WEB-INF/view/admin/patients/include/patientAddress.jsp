<table>
	<tr>
		<td><spring:message code="PatientAddress.address1"/></td>
		<td>
			<spring:bind path="address1">
				<input type="text" name="${status.expression}" value="${status.value}"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="PatientAddress.address2"/></td>
		<td>
			<spring:bind path="address2">
				<input type="text" name="${status.expression}" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
			
	</tr>
	<tr>
		<td><spring:message code="PatientAddress.cityVillage"/></td>
			<spring:bind path="cityVillage">
				<td>
					<input type="text" name="${status.expression}" value="${status.value}" onKeyUp="modifyTab(this, this.value, 0);"/>
				</td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		<td><spring:message code="PatientAddress.stateProvince"/></td>
			<spring:bind path="stateProvince">
				<td>
					<input type="text" name="${status.expression}" size="10" value="${status.value}" />
				</td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		<td><spring:message code="PatientAddress.country"/></td>
			<spring:bind path="country">
				<td>
					<input type="text" name="${status.expression}" size="15" value="${status.value}" />
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
					<input type="text" name="${status.expression}" value="${status.value}" />
				</td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		<td>Longitude</td>
			<spring:bind path="longitude">
				<td>
					<input type="text" name="${status.expression}" value="${status.value}" />
				</td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
	</tr>
	<c:if test="${!(address.creator == null)}" >
		<tr>
			<td><spring:message code="general.creator"/></td>
			<td>
				<spring:bind path="creator">
					${address.creator.username}
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td><spring:message code="general.dateCreated"/></td>
			<td>
				<spring:bind path="dateCreated">
					<openmrs:formatDate date="${address.dateCreated}" type="long"/>
				</spring:bind>
			</td>
		</tr>
	</c:if>
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
			<td>
				<input type="text" name="${status.expression}" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<c:if test="${!(address.voidedBy == null)}" >
		<tr>
			<td><spring:message code="general.voidedBy"/></td>
			<td>
				<spring:bind path="voidedBy">
					${address.voidedBy.username}
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td><spring:message code="general.dateVoided"/></td>
			<td>
				<spring:bind path="dateVoided">
					<openmrs:formatDate date="${address.dateVoided}" type="long"/>
				</spring:bind>
			</td>
		</tr>
	</c:if>
	
</table>