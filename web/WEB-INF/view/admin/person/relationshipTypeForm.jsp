<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Relationship Types" otherwise="/login.htm" redirect="/admin/person/relationshipType.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="RelationshipType.title"/></h2>

<form method="post">
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
<input type="submit" value="<spring:message code="RelationshipType.save"/>">
</form>

<script type="text/javascript">
 document.forms[0].elements[0].focus();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>