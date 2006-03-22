<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Users" otherwise="/login.htm" redirect="/admin/users/user.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script src="<%= request.getContextPath() %>/scripts/validation.js"></script>

<h2><spring:message code="User.title"/></h2>

<spring:hasBindErrors name="user">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message code="${error.code}" text="${error.code}"/><br/><!-- ${error} -->
		</c:forEach>
	</div>
	<br />
</spring:hasBindErrors>

<form method="post">
	<table>
		<tr>
			<td><spring:message code="User.systemId"/></td>
			<td>
				<spring:bind path="user.systemId">
					${status.value}
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td><spring:message code="User.username"/></td>
			<td>
				<spring:bind path="user.username">
					<input type="text" 
							name="${status.expression}" 
							id="username"
							value="${status.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<c:if test="${modifyPasswords == true}">
			<tr>
				<td><spring:message code="User.password" /></td>
				<td><input type="password" name="password" /></td>
	
			</tr>
			<tr>
				<td><spring:message code="User.confirm" /></td>
				<td><input type="password" name="confirm" /></td>
			</tr>
			<tr>
				<td><spring:message code="User.secretQuestion" /></td>
				<td><input type="text" name="secretQuestion" size="50" /></td>
	
			</tr>
			<tr>
				<td><spring:message code="User.secretAnswer" /></td>
				<td><input type="password" name="secretAnswer" size="50" /></td>
			</tr>
			<tr>
				<td><spring:message code="User.forceChange" /></td>
				<td><input type="checkbox" name="${changePasswordName}" value="true" <c:if test="${changePassword == true}">checked</c:if> /></td>
			</tr>
		</c:if>
		<tr>
			<td><spring:message code="User.firstName" /></td>
			<td>
				<spring:bind path="user.firstName">
					<input type="text" name="${status.expression}" value="${status.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>

		</tr>
		<tr>
			<td><spring:message code="User.middleName"/></td>
			<td>
				<spring:bind path="user.middleName">
					<input type="text" name="${status.expression}" value="${status.value}"/>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>

		</tr>
		<tr>
			<td><spring:message code="User.lastName"/></td>
			<td>
				<spring:bind path="user.lastName">
					<input type="text" name="${status.expression}" value="${status.value}"/>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>

		</tr>
		<tr>
			<td valign="top"><spring:message code="User.roles"/></td>
			<td valign="top">
				<openmrs:listPicker name="roles" allItems="${roles}" currentItems="${user.roles}" contextPath="${pageContext.request.contextPath}" />
			</td>
		</tr>
		<c:if test="${!(user.creator == null)}">
			<tr>
				<td><spring:message code="general.createdBy" /></td>
				<td>
					${user.creator.firstName} ${user.creator.lastName} -
					<openmrs:formatDate date="${user.dateCreated}" type="long" />
				</td>
			</tr>
		</c:if>
		<c:if test="${!(user.changedBy == null)}">
			<tr>
				<td><spring:message code="general.changedBy" /></td>
				<td>
					${user.changedBy.firstName} ${user.changedBy.lastName} -
					<openmrs:formatDate date="${user.dateChanged}" type="long" />
				</td>
			</tr>
		</c:if>
		<tr>
			<td><spring:message code="general.voided"/></td>
			<td>
				<spring:bind path="user.voided">
					<input type="hidden" name="_${status.expression}">
					<input type="checkbox" name="${status.expression}" 
						   id="${status.expression}" 
						   <c:if test="${status.value == true}">checked</c:if> 
						   onClick="voidedBoxClick(this)"
					/>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td><spring:message code="general.voidReason"/></td>
			<spring:bind path="user.voidReason">
				<td>
					<input type="text" name="${status.expression}" id="voidReason" value="${status.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</td>
			</spring:bind>
		</tr>
		<c:if test="${user.voided}" >
			<tr>
				<td><spring:message code="general.voidedBy"/></td>
				<td>
					${user.voidedBy.firstName} ${user.voidedBy.lastName} -
					<openmrs:formatDate date="${user.dateVoided}" type="long" />
				</td>
			</tr>
		</c:if>
	</table>
	<input type="submit" id="saveButton" value="<spring:message code="User.save"/>" />
</form>

<script type="text/javascript">
 document.forms[0].elements[0].focus();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>