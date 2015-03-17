<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Visit Types" otherwise="/login.htm" redirect="/admin/visits/visitType.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript">

	function confirmPurge() {
		if (confirm("<openmrs:message code='VisitType.purgeConfirmMessage' />")) {
			return true;
		} else {
			return false;
		}
	}
	
</script>

<h2><openmrs:message code="VisitType.title"/></h2>

<openmrs:extensionPoint pointId="org.openmrs.admin.visits.visitForm.belowTitle" type="html" parameters="visitTypeId=${visitType.visitTypeId}" />

<spring:hasBindErrors name="visitType">
	<openmrs:message htmlEscape="false" code="fix.error"/>
	<br />
</spring:hasBindErrors>
<form method="post">
<fieldset>
<table>
	<tr>
		<td><openmrs:message code="general.name"/><span class="required">*</span></td>
		<td>
			<spring:bind path="visitType.name">
				<input type="text" name="name" value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><openmrs:message code="general.description"/></td>
		<td valign="top">
			<spring:bind path="visitType.description">
				<textarea name="description" rows="3" cols="40" onkeypress="return forceMaxLength(this, 1024);" >${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(visitType.creator == null)}">
		<tr>
			<td><openmrs:message code="general.createdBy" /></td>
			<td><openmrs:format user="${ visitType.creator }"/></td>
		</tr>
	</c:if>
	<tr>
        <c:if test="${visitType.visitTypeId != null}">
           	<td><font color="#D0D0D0"><sub><openmrs:message code="general.uuid"/></sub></font></td>
           	<td colspan="${fn:length(locales)}"><font color="#D0D0D0"><sub>
           	<spring:bind path="visitType.uuid">
            <c:out value="${status.value}"></c:out>
           	</spring:bind></sub></font>
           	</td>
         </c:if>
    </tr>
</table>
<br />

<openmrs:extensionPoint pointId="org.openmrs.admin.visits.visitForm.inForm" type="html" parameters="visitTypeId=${visitType.visitTypeId}" />

<input type="submit" value="<openmrs:message code="VisitType.save"/>" name="save">

</fieldset>
</form>

<br/>

<c:if test="${not visitType.retired && not empty visitType.visitTypeId}">
	<form method="post">
		<fieldset>
			<h4><openmrs:message code="VisitType.retireVisitType"/></h4>
			
			<b><openmrs:message code="general.reason"/></b>
			<input type="text" value="" size="40" name="retireReason" />
			<spring:hasBindErrors name="visitType">
				<c:forEach items="${errors.allErrors}" var="error">
					<c:if test="${error.code == 'retireReason'}"><span class="error"><openmrs:message code="${error.defaultMessage}" text="${error.defaultMessage}"/></span></c:if>
				</c:forEach>
			</spring:hasBindErrors>
			<br/>
			<input type="submit" value='<openmrs:message code="VisitType.retireVisitType"/>' name="retire"/>
		</fieldset>
	</form>
</c:if>

<c:if test="${visitType.retired && not empty visitType.visitTypeId}">
	<form method="post">
		<fieldset>
			<h4><openmrs:message code="VisitType.unretireVisitType"/></h4>
			<input type="submit" value='<openmrs:message code="VisitType.unretireVisitType"/>' name="unretire"/>
		</fieldset>
	</form>
</c:if>

<br/>

<c:if test="${not empty visitType.visitTypeId}">
	<openmrs:hasPrivilege privilege="Manage Visit Types">
		<form id="purge" method="post" onsubmit="return confirmPurge()">
			<fieldset>
				<h4><openmrs:message code="VisitType.purgeVisitType"/></h4>
				<input type="submit" value='<openmrs:message code="VisitType.purgeVisitType"/>' name="purge" />
			</fieldset>
		</form>
	</openmrs:hasPrivilege>
</c:if>

<openmrs:extensionPoint pointId="org.openmrs.admin.visits.visitTypeForm.footer" type="html" parameters="visitTypeId=${visitType.visitTypeId}" />

<%@ include file="/WEB-INF/template/footer.jsp" %>