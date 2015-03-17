<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Encounter Types" otherwise="/login.htm" redirect="/admin/encounters/encounterType.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript">

	function confirmPurge() {
		if (confirm("Are you sure you want to purge this object? It will be permanently removed from the system.")) {
			return true;
		} else {
			return false;
		}
	}
	
</script>

<h2><openmrs:message code="EncounterType.title"/></h2>

<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.encounterForm.belowTitle" type="html" parameters="encounterTypeId=${encounterType.encounterTypeId}" />

<spring:hasBindErrors name="encounterType">
    <openmrs_tag:errorNotify errors="${errors}" />
</spring:hasBindErrors>
<form method="post">
<fieldset>
<table>
	<tr>
		<td><openmrs:message code="general.name"/><span class="required">*</span></td>
		<td>
			<spring:bind path="encounterType.name">
				<input type="text" name="name" value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}"><c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><openmrs:message code="general.description"/></td>
		<td valign="top">
			<spring:bind path="encounterType.description">
				<textarea name="description" rows="3" cols="40" onkeypress="return forceMaxLength(this, 1024);" >${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="EncounterType.editPrivilege"/></td>
		<td>
			<spring:bind path="encounterType.editPrivilege">
				<select name="editPrivilege">
					<option value=""></option>
					<c:forEach items="${privileges}" var="privilege">
						<option value="${privilege.privilege}" <c:if test="${privilege.privilege == status.value}">selected</c:if>>${privilege.privilege}</option>
					</c:forEach>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
		<td><i><openmrs:message code="EncounterType.editPrivilege.help"/></i></td>
	</tr>
	<tr>
		<td><openmrs:message code="EncounterType.viewPrivilege"/></td>
		<td>
			<spring:bind path="encounterType.viewPrivilege">
				<select name="viewPrivilege">
					<option value=""></option>
					<c:forEach items="${privileges}" var="privilege">
						<option value="${privilege.privilege}" <c:if test="${privilege.privilege == status.value}">selected</c:if>>${privilege.privilege}</option>
					</c:forEach>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
		<td><i><openmrs:message code="EncounterType.viewPrivilege.help"/></i></td>
	</tr>		
	<c:if test="${!(encounterType.creator == null)}">
		<tr>
			<td><openmrs:message code="general.createdBy" /></td>
			<td>
				<c:out value="${encounterType.creator.personName}" /> -
				<openmrs:formatDate date="${encounterType.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
	<tr>
        <c:if test="${encounterType.encounterTypeId != null}">
           	<td><font color="#D0D0D0"><sub><openmrs:message code="general.uuid"/></sub></font></td>
           	<td colspan="${fn:length(locales)}"><font color="#D0D0D0"><sub>
           	<spring:bind path="encounterType.uuid">
            <c:out value="${status.value}"></c:out>
           	</spring:bind></sub></font>
           	</td>
         </c:if>
    </tr>
</table>
<br />

<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.encounterForm.inForm" type="html" parameters="encounterTypeId=${encounterType.encounterTypeId}" />

<openmrs:globalProperty key="EncounterType.encounterTypes.locked" var="encounterTypesLocked"/>

<c:if test="${encounterTypesLocked != 'true'}">
<input type="submit" value="<openmrs:message code="EncounterType.save"/>" name="save">
</c:if>
<c:if test="${encounterTypesLocked == 'true'}">
<input type="submit" value="<openmrs:message code="EncounterType.save"/>" name="save" disabled>
</c:if>
</fieldset>
</form>

<br/>

<c:if test="${not encounterType.retired && not empty encounterType.encounterTypeId}">
	<form method="post">
		<fieldset>
			<h4><openmrs:message code="EncounterType.retireEncounterType"/></h4>
			
			<b><openmrs:message code="general.reason"/></b>
			<input type="text" value="" size="40" name="retireReason" />
			<spring:hasBindErrors name="encounterType">
				<c:forEach items="${errors.allErrors}" var="error">
					<c:if test="${error.code == 'retireReason'}"><span class="error"><openmrs:message code="${error.defaultMessage}" text="${error.defaultMessage}"/></span></c:if>
				</c:forEach>
			</spring:hasBindErrors>
			<br/>
			<c:if test="${encounterTypesLocked != 'true'}">
			<input type="submit" value='<openmrs:message code="EncounterType.retireEncounterType"/>' name="retire"/>
			</c:if>
			<c:if test="${encounterTypesLocked == 'true'}">
			<input type="submit" value='<openmrs:message code="EncounterType.retireEncounterType"/>' name="retire" disabled/>
			</c:if>
		</fieldset>
	</form>
</c:if>

<c:if test="${encounterType.retired && not empty encounterType.encounterTypeId}">
	<form method="post">
		<fieldset>
			<h4><openmrs:message code="EncounterType.unretireEncounterType"/></h4>
			
			<br/>
			<c:if test="${encounterTypesLocked != 'true'}">
			<input type="submit" value='<openmrs:message code="EncounterType.unretireEncounterType"/>' name="unretire"/>
			</c:if>
			<c:if test="${encounterTypesLocked == 'true'}">
			<input type="submit" value='<openmrs:message code="EncounterType.unretireEncounterType"/>' name="unretire" disabled/>
			</c:if>
		</fieldset>
	</form>
</c:if>

<br/>

<c:if test="${not empty encounterType.encounterTypeId}">
	<openmrs:hasPrivilege privilege="Purge Encounter Types">
		<form id="purge" method="post" onsubmit="return confirmPurge()">
			<fieldset>
				<h4><openmrs:message code="EncounterType.purgeEncounterType"/></h4>
				<c:if test="${encounterTypesLocked != 'true'}">
				<input type="submit" value='<openmrs:message code="EncounterType.purgeEncounterType"/>' name="purge" />
				</c:if>
				<c:if test="${encounterTypesLocked == 'true'}">
				<input type="submit" value='<openmrs:message code="EncounterType.purgeEncounterType"/>' name="purge" disabled />
				</c:if>
			</fieldset>
		</form>
	</openmrs:hasPrivilege>
</c:if>

<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.encounterTypeForm.footer" type="html" parameters="encounterTypeId=${encounterType.encounterTypeId}" />

<%@ include file="/WEB-INF/template/footer.jsp" %>