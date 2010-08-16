<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Relationship Types" otherwise="/login.htm" redirect="/admin/person/relationshipType.form" />

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

<h2><spring:message code="RelationshipType.title"/></h2>

<form method="post">
<fieldset>
<table>
	<tr>
		<td><spring:message code="RelationshipType.aIsToB"/></td>
		<td>
			<spring:bind path="relationshipType.aIsToB">
				<input type="text" name="${status.expression}" value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="RelationshipType.bIsToA"/></td>
		<td>
			<spring:bind path="relationshipType.bIsToA">
				<input type="text" name="${status.expression}" value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td valign="top">
			<spring:bind path="relationshipType.description">
				<textarea name="description" rows="3" cols="40">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${relationshipType.creator != null}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td>
				${relationshipType.creator.personName} -
				<openmrs:formatDate date="${relationshipType.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
</table>
<br />
<input type="submit" value="<spring:message code="RelationshipType.save"/>" name="save">
</fieldset>
</form>

<br/>

<c:if test="${not relationshipType.retired && not empty relationshipType.relationshipTypeId}">
	<form method="post">
		<fieldset>
			<h4><spring:message code="RelationshipType.retireRelationshipType"/></h4>
			
			<b><spring:message code="general.reason"/></b>
			<input type="text" value="" size="40" name="retireReason" />
			<spring:hasBindErrors name="relationshipType">
				<c:forEach items="${errors.allErrors}" var="error">
					<c:if test="${error.code == 'retireReason'}"><span class="error"><spring:message code="${error.defaultMessage}" text="${error.defaultMessage}"/></span></c:if>
				</c:forEach>
			</spring:hasBindErrors>
			<br/>
			<input type="submit" value='<spring:message code="RelationshipType.retireRelationshipType"/>' name="retire"/>
		</fieldset>
	</form>
</c:if>

<br/>

<c:if test="${not empty relationshipType.relationshipTypeId}">
	<openmrs:hasPrivilege privilege="Purge Relationship Types">
		<form id="purge" method="post" onsubmit="return confirmPurge()">
			<fieldset>
				<h4><spring:message code="RelationshipType.purgeRelationshipType"/></h4>
				<input type="submit" value='<spring:message code="RelationshipType.purgeRelationshipType"/>' name="purge" />
			</fieldset>
		</form>
	</openmrs:hasPrivilege>
</c:if>

<script type="text/javascript">
 document.forms[0].elements[0].focus();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>