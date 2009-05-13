<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Users" otherwise="/login.htm" redirect="/admin/users/user.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/scripts/validation.js" />

<h2><spring:message code="User.title"/></h2>

<c:if test="${user.voided}">
	<div id="userFormVoided" class="retiredMessage">
		<div><spring:message code="User.voidedMessage"/></div>
	</div>
</c:if>

<c:if test="${user.dead}">
	<div id="userFormDeceased" class="retiredMessage">
		<div><spring:message code="User.userDeceased"/></div>
	</div>
</c:if>

<spring:hasBindErrors name="user">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message code="${error.code}" text="${error.code}"/><br/><!-- ${error} -->
		</c:forEach>
	</div>
	<br />
</spring:hasBindErrors>

<form id="thisUserForm" method="post" autocomplete="off">
	<table>
		
		<spring:nestedPath path="user.names[0]">
			<openmrs:portlet url="nameLayout" id="namePortlet" size="full" parameters="layoutMode=edit|layoutShowTable=false|layoutShowExtended=false" />
		</spring:nestedPath>
		<tr>
			<td><spring:message code="Person.gender"/></td>
			<td><spring:bind path="user.gender">
					<openmrs:forEachRecord name="gender">
						<input type="radio" name="gender" id="${record.key}" value="${record.key}" <c:if test="${record.key == status.value}">checked</c:if> />
							<label for="${record.key}"> <spring:message code="Person.gender.${record.value}"/> </label>
					</openmrs:forEachRecord>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		
		<tr>
			<td><br/></td><td></td>
		</tr>
		<tr>
			<td><spring:message code="User.systemId"/></td>
			<td>
				<c:choose>
					<c:when test="${user.systemId != null && user.systemId != ''}">
						${user.systemId}
					</c:when>
					<c:otherwise>
						(<spring:message code="User.systemId.willBeGenerated"/>)
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		<tr>
			<td><spring:message code="User.username"/></td>
			<td>
				<spring:bind path="user.username">
					<input type="text" 
							name="${status.expression}" 
							value="${status.value}" 
							autocomplete="off" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
				<i><spring:message code="User.login.manner" /></i>
			</td>
		</tr>
		<c:if test="${modifyPasswords == true}">
			<tr>
				<td><spring:message code="User.usersPassword" /></td>
				<td><input type="password" name="userFormPassword" value="<c:if test="${isNewUser == false}">XXXXXXXXXXXXXXX</c:if>" autocomplete="off"/> <i><spring:message code="User.password.description"/></i></td>
	
			</tr>
			<tr>
				<td><spring:message code="User.confirm" /></td>
				<td>
					<input type="password" name="confirm" value="<c:if test="${isNewUser == false}">XXXXXXXXXXXXXXX</c:if>" autocomplete="off" />
					<i><spring:message code="User.confirm.description" /></i>
				</td>
			</tr>
			<tr>
				<td><spring:message code="User.forceChange" /></td>
				<td>
					<input type="checkbox" name="${changePasswordName}" value="true" <c:if test="${changePassword == true}">checked</c:if> />
					<i><spring:message code="User.forceChange.description"/></i>
				</td>
			</tr>
		</c:if>
		
		<tr><td colspan="2">&nbsp;</td></tr>
		
		<tr>
			<td valign="top"><spring:message code="User.roles"/></td>
			<td valign="top">
				<openmrs:listPicker name="roleStrings" allItems="${roles}" currentItems="${user.roles}" />
			</td>
		</tr>
		
		<tr><td colspan="2">&nbsp;</td></tr>
		
		<tr>
			<td colspan="2"><a href="#Show Advanced" onclick="return toggleLayer('advancedOptions', this, '<spring:message code="User.showAdvancedOptions"/>', '<spring:message code="User.hideAdvancedOptions"/>')"><spring:message code="User.showAdvancedOptions"/></a></td>
		</tr>
		<tbody id="advancedOptions" style="display: none">
			<c:if test="${modifyPasswords == true}">
				<tr>
					<td><spring:message code="User.secretQuestion" /></td>
					<td><input type="text" name="secretQuestion" size="50" value="${user.secretQuestion}" /> <i><spring:message code="general.optional"/></i></td>
				</tr>
				<tr>
					<td><spring:message code="User.secretAnswer" /></td>
					<td><input type="password" autocomplete="off" name="secretAnswer" size="50" value="<c:if test="${isNewUser == false}">XXXXXXXXXXXXXXX</c:if>"/> <i><spring:message code="general.optional"/></i></td>
				</tr>
			</c:if>
			<c:set var="INCLUDE_PERSON_GENDER" value="false"/>
			<spring:nestedPath path="user">
				<%@ include file="../person/include/editPersonInfo.jsp" %>
			</spring:nestedPath>
		
		<c:if test="${fn:length(user.userProperties) > 0}" >
			<tr>
				<td valign="top" colspan="2"><spring:message code="User.userProperties" /></td>
			</tr>
	
			<tr>
				<td></td>
				<td>
					<table cellpadding="1" cellspacing="0">
							<thead>
								<tr>
									<td><spring:message code="general.name" /></td>
									<td><spring:message code="general.value" /></td>
								</tr>
							</thead>
							<tbody id="userPropsList">
							<c:forEach var="userProp" items="${user.userProperties}" varStatus="status">
								<tr class="<c:choose><c:when test="${status.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>">
									<td valign="top">
										<input type="hidden" name="property"
											value="${userProp.key}" maxlength="250" />
										${userProp.key}:
									</td>
									<td valign="top">
										<c:choose>
											<c:when test="${fn:length(userProp.value) > 20}">
												<textarea name="value" rows="1" cols="60"
													wrap="off">${userProp.value}</textarea>
											</c:when>
											<c:otherwise>
												<input type="text" name="value" value="${userProp.value}"
													size="30" maxlength="4000" />
											</c:otherwise>
										</c:choose>
									</td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</td>
			</tr>
		</c:if>
	</tbody>
</table>

<br />
	
	<br/>
	
	<input type="submit" id="saveButton" name="action" value="<spring:message code="User.save"/>" />
	
	<c:if test="${user.userId != null}">
		<openmrs:hasPrivilege privilege="Become User (Actually you need to be a superuser)">
			&nbsp;&nbsp;&nbsp;&nbsp;
			<input type="submit" name="action" value="<spring:message code="User.assumeIdentity" />" onClick="return confirm('<spring:message code="User.assumeIdentity.confirm"/>');" />
		</openmrs:hasPrivilege>
		
		<openmrs:hasPrivilege privilege="Delete User">
			&nbsp;&nbsp;&nbsp;&nbsp;
			<input type="submit" name="action" value="<spring:message code="User.delete" />" onClick="return confirm('<spring:message code="User.delete.confirm"/>');" />
		</openmrs:hasPrivilege>
	</c:if>
</form>

<script type="text/javascript">
 document.forms[0].elements[0].focus();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>