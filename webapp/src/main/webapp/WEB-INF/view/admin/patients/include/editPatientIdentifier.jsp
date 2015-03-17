<table>
	<tr>
		<td><openmrs:message code="general.preferred"/></td>
		<td>
			<spring:bind path="preferred">
				<input type="hidden" name="_${status.expression}">
				<input type="checkbox" name="${status.expression}" onclick="if (preferredBoxClick) preferredBoxClick(this)" value="true" alt="patientIdentifier" <c:if test="${status.value == true}">checked</c:if> />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="PatientIdentifier.identifier"/><span class="required">*</span></td>
		<td>
			<spring:bind path="identifier">
				<input type="text"
						name="${status.expression}"
						value="${status.value}"
						onKeyUp="return true; validateIdentifier(this, 'saveButton', '<openmrs:message code="error.identifier"/>');"
						onChange="return true; validateIdentifier(this, 'saveButton', '<openmrs:message code="error.identifier"/>');"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
			</td>
	</tr>
	<tr>
		<td><openmrs:message code="PatientIdentifier.identifierType"/><span class="required">*</span></td>
		<td class="patientIdentifierTypeColumn">
			<spring:bind path="identifierType">
				<c:set var="hideLocation" value="${status.actualValue.locationBehavior == 'NOT_USED'}"/>
				<select id="identifierTypeBox${varStatus.count == null ? 0 : varStatus.count}" name="${status.expression}" onfocus="storeSelectedIdentifierType(this.options[this.selectedIndex].text)" onclick="modifyTab(this, this.options[this.selectedIndex].text, 0);" onChange="toggleLocationBoxAndIndentifierTypeWarning(this.options[this.selectedIndex].value,this.id);">
					<option value=""></option>
					<openmrs:forEachRecord name="patientIdentifierType">
						<option value="${record.patientIdentifierTypeId}" <c:if test="${record.patientIdentifierTypeId == status.value}">selected</c:if>>
                            <c:out value="${record.name}" />
						</option>
					</openmrs:forEachRecord>
				</select>
				<div class="identifierTypeWarningWrapper" style="display:inline-block;">
				    <div style="display: none; background-color: #ffff88" class="identifierTypeWarningDiv" id="identifierTypeWarning${varStatus.count == null ? 0 : varStatus.count}">
					    <openmrs:message code="Patient.warning.duplicateIdentifierTypes"/>
				    </div>
				</div>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>
			<c:if test="${identifierLocationUsed}">
				<openmrs:message code="PatientIdentifier.location"/>
				<span id="locationRequired${varStatus.count == null ? 0 : varStatus.count}" class="required" style="${hideLocation || varStatus.count == null ? 'display: none;' : ''}">*</span>
			</c:if>
		</td>
		<td>
			<spring:bind path="location">
				<select id="locationBox${varStatus.count == null ? 0 : varStatus.count}" name="${status.expression}" style="${hideLocation || varStatus.count == null ? 'display: none;' : ''}">
					<option value=""></option>
					<openmrs:forEachRecord name="location">
						<option value="${record.locationId}" <c:if test="${record.locationId == status.value}">selected</c:if>>
                            <c:out value="${record.name}" />
						</option>
					</openmrs:forEachRecord>
				</select>
				<span id="locationNABox${varStatus.count == null ? 0 : varStatus.count}" style="${hideLocation && varStatus.count != null ? '' : 'display: none;'}">
					<c:if test="${identifierLocationUsed}">
						<openmrs:message code="PatientIdentifier.location.notApplicable"/>
					</c:if>
				</span>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<spring:bind path="creator">
		<c:if test="${!(status.value == null)}">
			<tr>
				<td><openmrs:message code="general.createdBy" /></td>
				<td>
					<c:out value="${status.value.personName}" /> -
					<openmrs:formatDate path="dateCreated" type="long" />
				</td>
			</tr>
		</c:if>
	</spring:bind>
	
	<spring:bind path="changedBy">
	<c:if test="${status.value != null}">
		<tr>
			<td><openmrs:message code="general.changedBy" /></td>
			<td>
				<c:out value="${status.value.personName}" /> -
				<openmrs:formatDate path="dateChanged" type="long" />
			</td>
		</tr>
	</c:if>
</spring:bind>
	<c:if test="${identifier.patientIdentifierId != null}">
		<tr>
			<td><openmrs:message code="general.voided"/></td>
			<td>
				<spring:bind path="voided">
					<input type="hidden" name="_${status.expression}"/>
					<input type="checkbox" name="${status.expression}"
						   <c:if test="${status.value == true}">checked="checked"</c:if> 
						   onClick="toggleLayer('voidReasonIdentifierRow-<c:out value="${identifier}" />'); if (voidedBoxClicked) voidedBoxClicked(this); "
					/>
				</spring:bind>
			</td>
		</tr>
	<tr id="voidReasonIdentifierRow-<c:out value="${identifier.patientIdentifierId}" />" <spring:bind path="voided"><c:if test="${status.value == false}">style="display: none"</c:if></spring:bind> >
		<td><openmrs:message code="general.voidReason"/></td>
		<spring:bind path="voidReason">
			<td>
				<input type="text" name="${status.expression}" value="${status.value}" size="35"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
    </c:if>
	<spring:bind path="voidedBy">
		<c:if test="${status.value != null}">
			<tr>
				<td><openmrs:message code="general.voidedBy" /></td>
				<td>
					<c:out value="${status.value.personName}" /> -
					<openmrs:formatDate path="dateVoided" type="long" />
				</td>
			</tr>
		</c:if>
	</spring:bind>
</table>