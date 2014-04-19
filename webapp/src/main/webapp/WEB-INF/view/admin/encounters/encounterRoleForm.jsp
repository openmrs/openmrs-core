<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Encounter Roles" otherwise="/login.htm"
                 redirect="/admin/encounters/encounterRole.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript">

    function confirmPurge() {
        if (confirm("<openmrs:message code="general.confirm.purge"/>")) {
            return true;
        } else {
            return false;
        }
    }

</script>

<script type="text/javascript">
    function forceMaxLength(object, maxLength) {
        if (object.value.length >= maxLength) {
            object.value = object.value.substring(0, maxLength);
        }
    }
</script>

<h2><openmrs:message code="EncounterRole.title"/></h2>

<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.encounterRoleForm.belowTitle" type="html" parameters="encounterRoleId=${encounterRole.encounterRoleId}"/>

<spring:hasBindErrors name="encounterRole">
    <openmrs:message htmlEscape="false" code="fix.error"/>
    <br/>
</spring:hasBindErrors>
<form method="post">
    <fieldset>
        <table>
            <tr>
                <td><openmrs:message code="general.name"/><span class="required">*</span></td>
                <td colspan="5">
                    <spring:bind path="encounterRole.name">
                        <input type="text" name="name" value='<c:out value="${status.value}"/>' size="35"/>
                        <c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
                    </spring:bind>
                </td>
            </tr>
            <tr>
                <td valign="top"><openmrs:message code="general.description"/></td>
                <td valign="top" colspan="5">
                    <spring:bind path="encounterRole.description">
                        <textarea name="description" rows="3" cols="40">${status.value}</textarea>
                        <c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
                    </spring:bind>
                </td>
            </tr>
            <c:if test="${!(encounterRole.creator == null)}">
                <tr>
                    <td><openmrs:message code="general.createdBy"/></td>
                    <td>
                      <c:out value="${encounterRole.creator.personName}"/> - <openmrs:formatDate date="${encounterRole.dateCreated}" type="long"/>
                    </td>
                </tr>
            </c:if>
            <tr>
         		<c:if test="${encounterRole.encounterRoleId != null}">
           			<td><font color="#D0D0D0"><sub><openmrs:message code="general.uuid"/></sub></font></td>
           			<td colspan="${fn:length(locales)}"><font color="#D0D0D0"><sub>
           			<spring:bind path="encounterRole.uuid">
               			<c:out value="${status.value}"></c:out>
           			</spring:bind></sub></font>
           			</td>
           		</c:if>
    	   </tr>
        </table>
        <br/>
<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.encounterRoleForm.inForm" type="html" parameters="encounterRoleId=${encounterRole.encounterRoleId}" />

        <input type="submit" value="<openmrs:message code="EncounterRole.save"/>" name="saveEncounterRole">
    </fieldset>
</form>

<br/>

<c:if test="${not encounterRole.retired && not empty encounterRole.encounterRoleId}">
	<form method="post">
		<fieldset>
			<h4><openmrs:message code="EncounterRole.retireEncounterRole"/></h4>
			
			<b><openmrs:message code="general.reason"/></b>
			<input type="text" value="" size="40" name="retireReason" />
			<spring:hasBindErrors name="encounterRole">
				<c:forEach items="${errors.allErrors}" var="error">
					<c:if test="${error.code == 'retireReason'}"><span class="error"><openmrs:message code="${error.defaultMessage}" text="${error.defaultMessage}"/></span></c:if>
				</c:forEach>
			</spring:hasBindErrors>
			<br/>
			<input type="submit" value='<openmrs:message code="EncounterRole.retireEncounterRole"/>' name="retire"/>
		</fieldset>
	</form>
</c:if>

<br/>

<c:if test="${encounterRole.retired == true && not empty encounterRole.encounterRoleId}">
	<openmrs:hasPrivilege privilege="Manage Encounter Roles">
		<form id="unretire" method="post">
		<fieldset>
		<h4><openmrs:message code="EncounterRole.unretire" /></h4>
		<input type="submit" value='<openmrs:message code="EncounterRole.unretire"/>' name="unretire" /></fieldset>
		</form>
	</openmrs:hasPrivilege>
</c:if>
<br />

<c:if test="${not empty encounterRole.encounterRoleId}">
	<openmrs:hasPrivilege privilege="Purge Encounter Types">
		<form id="purge" method="post" onsubmit="return confirmPurge()">
			<fieldset>
				<h4><openmrs:message code="EncounterRole.purgeEncounterRole"/></h4>
				<input type="submit" value='<openmrs:message code="EncounterRole.purgeEncounterRole"/>' name="purge" />
			</fieldset>
		</form>
	</openmrs:hasPrivilege>
</c:if>

<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.encounterRoleForm.footer" type="html" parameters="encounterRoleId=${encounterRole.encounterRoleId}" />
<%@ include file="/WEB-INF/template/footer.jsp" %>