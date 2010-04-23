<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Users" otherwise="/login.htm" redirect="/admin/users/user.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<c:set var="errorsFromPreviousSubmit" value="false"/>
<spring:hasBindErrors name="user">
	<c:set var="errorsFromPreviousSubmit" value="true"/>
</spring:hasBindErrors>
<c:choose>
	<c:when test="${errorsFromPreviousSubmit == 'false' && empty param.userId && empty param.person_id && empty createNewPerson}">	
		<script type="text/javascript">
			function personSelectedCallback(relType, person) {
				if (person != null && person.personId != null) {
					document.getElementById('useExistingButton').disabled = false;
				} else {
					hideDiv('useExistingButton').disabled = true;
				}
			}
		</script>
		<h2><spring:message code="User.title.add"/></h2>
		<spring:message code="User.needsAPerson"/>
		<br/>
		<br/>
		<table>
			<tr valign="top">
				<td style="border-right: 1px lightgrey solid; padding-right: 5em">
					<h3><spring:message code="User.createNewPerson"/></h3>
					<form method="get" action="user.form">
						<input type="hidden" name="createNewPerson" value="true"/>
						<input type="submit" value="<spring:message code="general.next"/>"/>
					</form>
				</td>
				<td style="padding-left: 5em">
					<h3><spring:message code="User.useExisting"/></h3>
					<form method="get" action="user.form">
						<spring:message code="User.whichPerson"/> <openmrs_tag:personField formFieldName="person_id" formFieldId="existingPersonId" callback="personSelectedCallback"/>
						<br/>
						<input id="useExistingButton" disabled="true" type="submit" value="<spring:message code="general.next"/>"/>
					</form>
				</td>
			</tr>
		</table>
	</c:when>
	<c:otherwise>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/scripts/validation.js" />

<h2><spring:message code="User.title"/></h2>

<c:if test="${user.retired}">
	<div id="userFormRetired" class="retiredMessage">
		<div><spring:message code="User.retiredMessage"/></div>
	</div>
</c:if>

<c:if test="${user.person.dead}">
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

<form id="thisUserForm" method="post" action="user.form" autocomplete="off">
	<c:if test="${param.userId != null}">
		<input type="hidden" name="userId" value="${param.userId}"/>
	</c:if>
	<c:if test="${createNewPerson}">
		<input type="hidden" name="createNewPerson" value="true"/>
	</c:if>
	<fieldset>
		<legend><spring:message code="User.demographicInfo"/></legend>
		<c:choose>
			<c:when test="${not empty createNewPerson}">
				<table>
					<spring:bind path="user.person">
						<c:if test="${status.errorMessage != ''}">
							<tr>
								<span class="error">${status.errorMessage}</span>
							</tr>
						</c:if>
					</spring:bind>
					<spring:nestedPath path="user.person.names[0]">
						<openmrs:portlet url="nameLayout" id="namePortlet" size="full" parameters="layoutMode=edit|layoutShowTable=false|layoutShowExtended=false" />
					</spring:nestedPath>
					<tr>
						<td><spring:message code="Person.gender"/></td>
						<td><spring:bind path="user.person.gender">
								<openmrs:forEachRecord name="gender">
									<input type="radio" name="${status.expression}" id="${record.key}" value="${record.key}" <c:if test="${record.key == status.value}">checked</c:if> />
										<label for="${record.key}"> <spring:message code="Person.gender.${record.value}"/> </label>
								</openmrs:forEachRecord>
							<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
							</spring:bind>
						</td>
					</tr>
				</table>
			</c:when>
			<c:otherwise>
				<%-- The importJavascriptFile function in openmrs.js isn't working, so import these here --%>
				<openmrs:htmlInclude file="/dwr/engine.js" />
				<openmrs:htmlInclude file="/dwr/util.js" />
				<openmrs:htmlInclude file="/dwr/interface/DWRPersonService.js" />
				<table>
					<tr valign="top">
						<td><spring:message code="User.person"/>&nbsp;&nbsp;</td>
						<td>
							<openmrs_tag:personField searchLabelCode="" formFieldName="person_id" initialValue="${user.person.personId}"/>
						</td>
					</tr>
				</table>
			</c:otherwise>
		</c:choose>
	</fieldset>	

	<fieldset>
		<legend><spring:message code="User.loginInfo"/></legend>
		<table>
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
					<td><input type="password" name="userFormPassword" value="<c:if test="${isNewUser == false}">XXXXXXXXXXXXXXX</c:if>" autocomplete="off"/></td>
		
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
						<input type="checkbox" name="forcePassword" value="true" <c:if test="${changePassword == true}">checked</c:if> />
						<i><spring:message code="User.forceChange.description"/></i>
					</td>
				</tr>
			</c:if>
			
			<tr><td colspan="2">&nbsp;</td></tr>
			
			<tr>
				<td valign="top"><spring:message code="User.roles"/></td>
				<td valign="top">
					<openmrs:listPicker name="roleStrings" allItems="${allRoles}" currentItems="${user.roles}" />
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
						<td><input type="password" autocomplete="off" name="secretAnswer" size="50" value=""/> <i><spring:message code="general.optional"/></i></td>
					</tr>
				</c:if>
			
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
	</fieldset>

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

<br/>

<c:if test="${not empty user.userId}">
	<form method="post" action="user.form">
		<c:if test="${param.userId != null}">
			<input type="hidden" name="userId" value="${param.userId}"/>
		</c:if>
		<fieldset>
			<c:choose>
				<c:when test="${not user.retired}">
					<h4><spring:message code="User.retire"/></h4>
					
					<b><spring:message code="general.reason"/></b>
					<spring:bind path="user.retireReason">
						<input type="text" 
								name="${status.expression}" 
								value="${status.value}" 
								autocomplete="off" />
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</spring:bind>
					<br/>
					<input type="submit" value='<spring:message code="User.retire"/>' name="action"/>			
				</c:when>
				<c:otherwise>
					<input type="submit" value='<spring:message code="User.unRetire"/>' name="action"/>	
				</c:otherwise>
			</c:choose>
		</fieldset>
	</form>
</c:if>

<script type="text/javascript">
 document.forms[0].elements[0].focus();
</script>

	</c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/template/footer.jsp" %>