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
			<!--<td valign="top">${personAttributeType.sortWeight}</td> -->
				<td valign="top">
					<a href="personAttributeType.form?personAttributeTypeId=${personAttributeType.personAttributeTypeId}">
						<c:choose>
							<c:when test="${personAttributeType.retired == true}">
								<del>${personAttributeType.name}</del>
							</c:when>
							<c:otherwise>
								${personAttributeType.name}
							</c:otherwise>
						</c:choose>
					</a>
				</td>
				<td valign="top">${personAttributeType.format}</td>
				<td valign="top"><c:if test="${personAttributeType.searchable == true}"><spring:message code="general.yes"/></c:if></td>
				<td valign="top">${personAttributeType.description}</td>
				<td valign="top">${personAttributeType.editPrivilege}</td>
			</tr>
		</c:forEach>
	</table>
	<input type="hidden" name="action" id="saveAction" value=""/>
	<input type="submit" value='<spring:message code="PersonAttributeType.moveup"/>' onclick="document.getElementById('saveAction').value='moveup'">
	<input type="submit" value='<spring:message code="PersonAttributeType.movedown"/>' onclick="document.getElementById('saveAction').value='movedown'">
</form>

<form method="post" style="display: none;">
	<input id="move_id" type="hidden" name="personAttributeTypeId" value="0"/>
	<input id="move_action" type="hidden" name="action" value=""/>
	<input id="move_submit" type="submit" value=""/>
</form>

<br/>

<b class="boxHeader"><spring:message code="PersonAttributeType.viewingListing.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th><spring:message code="PersonAttributeType.patient.listing" /></th>
			<td><input type="text" size="50" name="patientListingAttributeTypes" value="${patientListingAttributeTypes}"/></td>
			<td><spring:message code="PersonAttributeType.patient.listing.help" /></td>
		</tr>
		<tr>
			<th><spring:message code="PersonAttributeType.patient.viewing" /></th>
			<td><input type="text" size="50" name="patientViewingAttributeTypes" value="${patientViewingAttributeTypes}"/></td>
			<td><spring:message code="PersonAttributeType.patient.viewing.help" /></td>
		</tr>
		<tr>
			<th><spring:message code="PersonAttributeType.patient.header" /></th>
			<td><input type="text" size="50" name="patientHeaderAttributeTypes" value="${patientHeaderAttributeTypes}"/></td>
			<td><spring:message code="PersonAttributeType.patient.header.help" /></td>
		</tr>		
		<tr>
			<th><spring:message code="PersonAttributeType.user.listing" /></th>
			<td><input type="text" size="50" name="userListingAttributeTypes" value="${userListingAttributeTypes}"/></td>
			<td><spring:message code="PersonAttributeType.user.listing.help" /></td>
		</tr>
		<tr>
			<th><spring:message code="PersonAttributeType.user.viewing" /></th>
			<td><input type="text" size="50" name="userViewingAttributeTypes" value="${userViewingAttributeTypes}"/></td>
			<td><spring:message code="PersonAttributeType.user.viewing.help" /></td>
		</tr>
	</table>
	
	<input type="hidden" name="action" value="attrs"/>
	<input type="submit" value='<spring:message code="general.save"/>'>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>