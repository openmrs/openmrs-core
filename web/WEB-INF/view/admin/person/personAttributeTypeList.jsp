<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Person Attribute Types" otherwise="/login.htm" redirect="/admin/person/personAttributeType.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="PersonAttributeType.manage.title"/></h2>

<a href="personAttributeType.form"><spring:message code="PersonAttributeType.add"/></a>

<br /><br />

<b class="boxHeader"><spring:message code="PersonAttributeType.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> <spring:message code="general.name"/> </th>
			<th> <spring:message code="PersonAttributeType.format"/> </th>
			<th> <spring:message code="PersonAttributeType.searchable"/> </th>
			<th> <spring:message code="general.description"/> </th>
			<th> <spring:message code="PersonAttributeType.editPrivilege"/> </th>
		</tr>
		<c:forEach var="personAttributeType" items="${personAttributeTypeList}">
			<tr>
				<td valign="top"><input type="checkbox" name="personAttributeTypeId" value="${personAttributeType.personAttributeTypeId}"></td>
				<td valign="top"><a href="personAttributeType.form?personAttributeTypeId=${personAttributeType.personAttributeTypeId}">
					   ${personAttributeType.name}
					</a>
				</td>
				<td valign="top">${personAttributeType.format}</td>
				<td valign="top"><c:if test="${personAttributeType.searchable == true}"><spring:message code="general.yes"/></c:if></td>
				<td valign="top">${personAttributeType.description}</td>
				<td valign="top">${personAttributeType.editPrivilege}</td>
			</tr>
		</c:forEach>
	</table>
	<input type="hidden" name="action" value="delete"/>
	<input type="submit" value='<spring:message code="PersonAttributeType.delete"/>'>
</form>

<br/>

<b class="boxHeader"><spring:message code="PersonAttributeType.viewingListing.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th><spring:message code="PersonAttributeType.patient.listing" /></th>
			<td><input type="text" size="50" name="patient.listingAttributeTypes" value="${patientListingAttributeTypes}"/></td>
			<td><spring:message code="PersonAttributeType.patient.listing.help" /></td>
		</tr>
		<tr>
			<th><spring:message code="PersonAttributeType.patient.viewing" /></th>
			<td><input type="text" size="50" name="patient.viewingAttributeTypes" value="${patientViewingAttributeTypes}"/></td>
			<td><spring:message code="PersonAttributeType.patient.viewing.help" /></td>
		</tr>
		<tr>
			<th><spring:message code="PersonAttributeType.patient.header" /></th>
			<td><input type="text" size="50" name="patient.headerAttributeTypes" value="${patientHeaderAttributeTypes}"/></td>
			<td><spring:message code="PersonAttributeType.patient.header.help" /></td>
		</tr>		
		<tr>
			<th><spring:message code="PersonAttributeType.user.listing" /></th>
			<td><input type="text" size="50" name="user.listingAttributeTypes" value="${userListingAttributeTypes}"/></td>
			<td><spring:message code="PersonAttributeType.user.listing.help" /></td>
		</tr>
		<tr>
			<th><spring:message code="PersonAttributeType.user.viewing" /></th>
			<td><input type="text" size="50" name="user.viewingAttributeTypes" value="${userViewingAttributeTypes}"/></td>
			<td><spring:message code="PersonAttributeType.user.viewing.help" /></td>
		</tr>
	</table>
	
	<input type="hidden" name="action" value="attrs"/>
	<input type="submit" value='<spring:message code="general.save"/>'>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>