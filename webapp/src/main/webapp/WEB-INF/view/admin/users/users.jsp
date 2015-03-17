<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Edit Users" otherwise="/login.htm"
	redirect="/admin/users/users.list" />
<openmrs:message var="pageTitle" code="User.manage.titlebar" scope="page" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<style type="text/css">
.bold_text{
	font-weight: bold;
}
</style>

<h2><openmrs:message code="User.manage.title" /></h2>

<a href="user.form"><openmrs:message code="User.add" /></a>

<br />
<br />

<form method="get">

<table>
	<tr>
		<td><openmrs:message code="User.find"/></td>
		<td><input type="text" name="name" value="<c:out value="${param.name}"/>" /></td>
	</tr>
	<tr>
		<td><openmrs:message code="Role.role"/></td>
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
		<td><openmrs:message code="SearchResults.includeDisabled"/></td>
		<td>
			<input type="checkbox" name="includeDisabled" <c:if test="${param.includeDisabled == 'on'}">checked=checked</c:if>/>
		</td>
	</tr>
	<tr>
		<td></td>
		<td><input type="submit" name="action" value="<openmrs:message code="general.search"/>"/></td>
	</tr>
</table>

</form>

<br/>

<c:if test="${fn:length(users) == 0 && (param.name != None || param.role != None || param.includeDisabled != None)}">
	<openmrs:message code="User.noUsersFound"/>
</c:if>

<c:if test="${fn:length(users) > 0}">
<b class="boxHeader"><openmrs:message code="User.list.title" /></b>
<div class="box">
<table class="openmrsSearchTable" style="width: 100%;" cellpadding="2" cellspacing="0">
	<thead>
		<tr style="" dojoattachpoint="headerRow">
			<th><openmrs:message code="User.systemId" javaScriptEscape="true"/></th>
			<th><openmrs:message code="User.username" javaScriptEscape="true"/></th>
			<th><openmrs:message code="PersonName.givenName" javaScriptEscape="true"/></th>
			<th><openmrs:message code="PersonName.familyName" javaScriptEscape="true"/></th>
			<th>
				<openmrs:message code="User.roles" javaScriptEscape="true"/>
			</th>
			<openmrs:forEachDisplayAttributeType personType="user" displayType="listing" var="attrType">
				<th><openmrs:message code="PersonAttributeType.${fn:replace(attrType.name, ' ', '')}" javaScriptEscape="true" text="${attrType.name}"/></th>
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
			<td>
				<c:if test="${fn:length(userRolesMap[user]) > 3}">
				<span title="${userRolesMap[user]}">
				</c:if>
				<c:forEach var="r" items="${userRolesMap[user]}" varStatus="varStatus" end="2">
					<c:choose>
						<c:when test="${varStatus.index == 0}">
							<c:choose>
								<c:when test="${r == role}">
									<span class='bold_text'>${r} </span>
								</c:when>
								<c:when test="${r != role && role != null}">
									<span class='bold_text'>
										<c:forEach var="inheritedRole" items="${userInheritanceLineMap[user]}" varStatus="inheritanceStatus">
										${inheritedRole} <c:if test="${inheritanceStatus.index ne fn:length(userInheritanceLineMap[user]) - 1}"> -> </c:if>
										</c:forEach>
									</span>
								</c:when>
								<c:otherwise>${r}</c:otherwise>
							</c:choose>
						</c:when>
						<c:otherwise>, ${r}</c:otherwise>
					</c:choose>
				</c:forEach>
				<c:if test="${fn:length(userRolesMap[user]) > 3}">
				, ....</span>
				</c:if>
			</td>
			<openmrs:forEachDisplayAttributeType personType="user" displayType="listing" var="attrType">
				<td><c:if test="${user.person != null}">${user.person.attributeMap[attrType.name]}</c:if></td>
			</openmrs:forEachDisplayAttributeType>
		</tr>
	</c:forEach>
			
</table>
</div>
</c:if>

<br />
<br />

<script type="text/javascript">
  document.forms[0].elements[0].focus();
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>