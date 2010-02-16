<table>
	<tr>
		<td><spring:message code="general.preferred"/></td>
		<td>
			<spring:bind path="preferred">
				<input type="hidden" name="_${status.expression}">
				<input type="checkbox" name="${status.expression}" onclick="if (preferredBoxClick) preferredBoxClick(this)" value="true" alt="patientIdentifier" <c:if test="${status.value == true}">checked</c:if> />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="PatientIdentifier.identifier"/></td>
		<td>
			<spring:bind path="identifier">
				<input type="text" 
						name="${status.expression}" 
						value="${status.value}" 
						onKeyUp="return true; validateIdentifier(this, 'saveButton', '<spring:message code="error.identifier"/>');"
						onChange="return true; validateIdentifier(this, 'saveButton', '<spring:message code="error.identifier"/>');"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
			</td>
	</tr>
	<tr>
		<td><spring:message code="PatientIdentifier.identifierType"/></td>
		<td>
			<spring:bind path="identifierType">
				<select name="${status.expression}" onclick="modifyTab(this, this.options[this.selectedIndex].text, 0)">
					<openmrs:forEachRecord name="patientIdentifierType">
						<option value="${record.patientIdentifierTypeId}" <c:if test="${record.patientIdentifierTypeId == status.value}">selected</c:if>>
							${record.name}
						</option>
					</openmrs:forEachRecord>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="PatientIdentifier.location"/></td>
		<td>
			<spring:bind path="location">
				<select name="${status.expression}">
					<openmrs:forEachRecord name="location">
						<option value="${record.locationId}" <c:if test="${record.locationId == status.value}">selected</c:if>>
							${record.name}
						</option>
					</openmrs:forEachRecord>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<spring:bind path="creator">
		<c:if test="${!(status.value == null)}">
			<tr>
				<td><spring:message code="general.createdBy" /></td>
				<td>
					${status.value.personName} -
					<openmrs:formatDate path="dateCreated" type="long" />
				</td>
			</tr>
		</c:if>
	</spring:bind>
	<c:if test="${identifier.patientIdentifierId != null}">
		<tr>
			<td><spring:message code="general.voided"/></td>
			<td>
				<spring:bind path="voided">
					<input type="hidden" name="_${status.expression}"/>
					<input type="checkbox" name="${status.expression}"
						   <c:if test="${status.value == true}">checked="checked"</c:if> 
						   onClick="toggleLayer('voidReasonRow-${identifier}'); if (voidedBoxClicked) voidedBoxClicked(this); "
					/>
				</spring:bind>
			</td>
		</tr>
	</c:if>
	<tr id="voidReasonRow-${identifier}" <spring:bind path="voided"><c:if test="${status.value == false}">style="display: none"</c:if></spring:bind> >
		<td><spring:message code="general.voidReason"/></td>
		<spring:bind path="voidReason">
			<td>
				<input type="text" name="${status.expression}" value="${status.value}" size="35"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<spring:bind path="voidedBy">
		<c:if test="${status.value != null}">
			<tr>
				<td><spring:message code="general.voidedBy" /></td>
				<td>
					${status.value.personName} -
					<openmrs:formatDate path="dateVoided" type="long" />
				</td>
			</tr>
		</c:if>
	</spring:bind>
</table>