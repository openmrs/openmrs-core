<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Edit Users" otherwise="/login.htm"
	redirect="/admin/users/users.list" />
<spring:message var="pageTitle" code="User.manage.titlebar" scope="page" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<h2><spring:message code="User.manage.title" /></h2>

<a href="user.form"><spring:message code="User.add" /></a>

<br />
<br />

<form method="get">

<table>
	<tr>
		<td><spring:message code="User.find"/></td>
		<td><input type="text" name="name" value="<c:out value="${param.name}"/>" /></td>
	</tr>
	<tr>
		<td><spring:message code="Role.role"/></td>
		<td>
			<select name="role">
				<option></option>
				<openmrs:forEachRecord name="role">
					<c:if test="${record.role != 'Anonymous' && record.role != 'Authenticated'}">
						<option <c:if test="${param.role == record.role}">selected</c:if>><c:out value="${record.role}"/></option>
					</c:if>
				</openmrs:forEachRecord>
			</select>
		</td>
	</tr>
	<tr>
		<td><spring:message code="SearchResults.includeDisabled"/></td>
		<td>
			<input type="checkbox" name="includeDisabled" <c:if test="${param.includeDisabled == 'on'}">checked=checked</c:if>/>
		</td>
	</tr>
	<tr>
		<td></td>
		<td><input type="submit" name="action" value="<spring:message code="general.search"/>"/></td>
	</tr>
</table>

</form>

<br/>

<c:if test="${fn:length(users) == 0 && (param.name != None || param.role != None || param.includeDisabled != None)}">
	<spring:message code="User.noUsersFound"/>
</c:if>

<c:if test="${fn:length(users) > 0}">
<b class="boxHeader"><spring:message code="User.list.title" /></b>
<div class="box">
<table class="openmrsSearchTable" style="width: 100%;" cellpadding="2" cellspacing="0">
	<thead>
		<tr style="" dojoattachpoint="headerRow">
			<th><spring:message code="User.systemId" javaScriptEscape="true"/></th>
			<th><spring:message code="User.username" javaScriptEscape="true"/></th>
			<th><spring:message code="PersonName.givenName" javaScriptEscape="true"/></th>
			<th><spring:message code="PersonName.familyName" javaScriptEscape="true"/></th>
			<th><spring:message code="User.roles" javaScriptEscape="true"/></th>
			<openmrs:forEachDisplayAttributeType personType="user" displayType="listing" var="attrType">
				<th><spring:message code="PersonAttributeType.${fn:replace(attrType.name, ' ', '')}" javaScriptEscape="true" text="${attrType.name}"/></th>
			</openmrs:forEachDisplayAttributeType>
		</tr>
	</thead>
	<c:forEach var="user" items="${users}" varStatus="rowStatus">
		<tr class='${rowStatus.index % 2 == 0 ? "evenRow" : "oddRow" } ${user.retired ? "retired" : "" }'>
			<td style="white-space: nowrap">
				<a href="user.form?userId=<c:out value="${user.userId}"/>">
					<c:out value="${user.systemId}"/>
				</a>
			</td>
			<td><c:out value="${user.username}"/></td>
			<td><c:out value="${user.givenName}"/></td>
			<td><c:out value="${user.familyName}"/></td>
			<td><c:out value="${user.roles}"/></td>
			<openmrs:forEachDisplayAttributeType personType="user" displayType="listing" var="attrType">
				<td>${user.attributes[attrType.name]}</td>
			</openmrs:forEachDisplayAttributeType>
		</tr>
	</c:forEach>
			
</table>
</div>
</c:if>

<br />
<br />

<%@ include file="/WEB-INF/template/footer.jsp"%>