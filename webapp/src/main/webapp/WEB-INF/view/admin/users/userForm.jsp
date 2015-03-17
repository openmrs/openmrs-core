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
					document.getElementById('useExistingButton').disabled = true;
				}
			}
		</script>
		<h2><openmrs:message code="User.title.add"/></h2>
		<openmrs:message code="User.needsAPerson"/>
		<br/>
		<br/>
		<table>
			<tr valign="top">
				<td style="border-right: 1px lightgrey solid; padding-right: 5em">
					<h3><openmrs:message code="User.createNewPerson"/></h3>
					<form method="get" action="user.form">
						<input type="hidden" name="createNewPerson" value="true"/>
						<input id="createNewPersonButton" type="submit" value="<openmrs:message code="general.next"/>"/>
					</form>
				</td>
				<td style="padding-left: 5em">
					<h3><openmrs:message code="User.useExisting"/></h3>
					<form method="get" action="user.form">
						<openmrs:message code="User.whichPerson"/> <openmrs_tag:personField formFieldName="person_id" formFieldId="existingPersonId" callback="personSelectedCallback"/>
						<br/>
						<input id="useExistingButton" disabled="true" type="submit" value="<openmrs:message code="general.next"/>"/>
					</form>
				</td>
			</tr>
		</table>
	</c:when>
	<c:otherwise>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/scripts/validation.js" />

<h2><openmrs:message code="User.title"/></h2>

<c:if test="${user.retired}">
	<div id="userFormRetired" class="retiredMessage">
		<div><openmrs:message code="User.retiredMessage"/></div>
	</div>
</c:if>

<c:if test="${user.person.dead}">
	<div id="userFormDeceased" class="retiredMessage">
		<div><openmrs:message code="User.userDeceased"/></div>
	</div>
</c:if>

<spring:hasBindErrors name="user">
    <openmrs_tag:errorNotify errors="${errors}" />
</spring:hasBindErrors>

<form id="thisUserForm" method="post" action="user.form" autocomplete="off">
	<c:if test="${param.userId != null}">
		<input type="hidden" name="userId" value="${param.userId}"/>
	</c:if>
	<c:if test="${user.person.personId != null}">
		<input type="hidden" name="person_id" value="${user.person.personId}"/>
	</c:if>
	<c:if test="${createNewPerson}">
		<input type="hidden" name="createNewPerson" value="true"/>
	</c:if>
	<fieldset>
		<legend><openmrs:message code="User.demographicInfo"/></legend>
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
				<td><openmrs:message code="Person.gender"/><span class="required">*</span></td>
				<td><spring:bind path="user.person.gender">
						<openmrs:forEachRecord name="gender">
							<input type="radio" name="${status.expression}" id="${record.key}" value="${record.key}" <c:if test="${record.key == status.value}">checked</c:if> />
								<label for="${record.key}"> <openmrs:message code="Person.gender.${record.value}"/> </label>
						</openmrs:forEachRecord>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</spring:bind>
				</td>
			</tr>
		</table>
	</fieldset>	
	
	<br/>
	
	<fieldset>
		<legend><openmrs:message code="User.loginInfo"/></legend>
		<table>
			<tr>
				<td><openmrs:message code="User.systemId"/></td>
				<td>
					<c:choose>
						<c:when test="${user.systemId != null && user.systemId != ''}">
							${user.systemId}
						</c:when>
						<c:otherwise>
							(<openmrs:message code="User.systemId.willBeGenerated"/>)
						</c:otherwise>
					</c:choose>
				</td>
			</tr>
			<tr>
				<td><openmrs:message code="User.username"/></td>
				<td>
					<spring:bind path="user.username">
						<input type="text" 
								name="${status.expression}" 
								value="${status.value}" 
								autocomplete="off" />
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</spring:bind>
					<i><openmrs:message code="User.login.manner" /></i>
				</td>
			</tr>
			<c:if test="${modifyPasswords == true}">
				<tr>
				<td><openmrs:message code="User.usersPassword" /><span class="required">*</span></td>
					<td><input type="password" name="userFormPassword" value="<c:if test="${isNewUser == false}">XXXXXXXXXXXXXXX</c:if>" autocomplete="off"/>
                    
                    <openmrs:globalProperty key="security.passwordMinimumLength" var="passwordMinimumLength"/>
                    <openmrs:globalProperty key="security.passwordRequiresDigit" var="passwordRequiresDigit"/>
                    <openmrs:globalProperty key="security.passwordRequiresNonDigit" var="passwordRequiresNonDigit"/>
                    <openmrs:globalProperty key="security.passwordRequiresUpperAndLowerCase" var="passwordRequiresUpperAndLowerCase"/>
                    
                    <i><openmrs:message code="general.passwordLength" arguments="${passwordMinimumLength}" />                    
					
					<% boolean prevCondition=false; %>
                    
                    <c:if test="${passwordRequiresUpperAndLowerCase == true || passwordRequiresDigit == true || passwordRequiresNonDigit == true}"> <openmrs:message code="general.shouldHave" /></c:if>
                    
                    <c:if test="${passwordRequiresUpperAndLowerCase == true}" > <openmrs:message code="changePassword.hint.password.bothCasesRequired" /><% prevCondition=true; %></c:if>
                    
                    <c:if test="${passwordRequiresDigit == true}" ><% if(prevCondition==true) out.print(","); %> <openmrs:message code="changePassword.hint.password.digitRequired" /><% prevCondition=true; %></c:if>
                    
                    <c:if test="${passwordRequiresNonDigit == true}" ><% if(prevCondition==true) out.print(","); %> <openmrs:message code="changePassword.hint.password.nonDigitRequired" /></c:if>
                    
                    </i> 
                    </td>
				</tr>
				<tr>
					<td><openmrs:message code="User.confirm" /><span class="required">*</span></td>
					<td>
						<input type="password" name="confirm" value="<c:if test="${isNewUser == false}">XXXXXXXXXXXXXXX</c:if>" autocomplete="off" />
						<i><openmrs:message code="User.confirm.description" /></i>
					</td>
				</tr>
				<tr>
					<td><openmrs:message code="User.forceChange" /></td>
					<td>
						<input type="checkbox" name="forcePassword" value="true" <c:if test="${changePassword == true}">checked</c:if> />
						<i><openmrs:message code="User.forceChange.description"/></i>
					</td>
				</tr>
			</c:if>
			<tr><td colspan="2">&nbsp;</td></tr>
			
			<tr>
				<td valign="top"><openmrs:message code="User.roles"/></td>
				<td valign="top">
					<openmrs:listPicker name="roleStrings" allItems="${allRoles}" currentItems="${user.roles}" />
				</td>
			</tr>
			
			<tr><td colspan="2">&nbsp;</td></tr>
			
			<tr>
				<td colspan="2"><a href="#Show Advanced" onclick="return toggleLayer('advancedOptions', this, '<openmrs:message code="User.showAdvancedOptions"/>', '<openmrs:message code="User.hideAdvancedOptions"/>')"><openmrs:message code="User.showAdvancedOptions"/></a></td>
			</tr>
			<tbody id="advancedOptions" style="display: none">
				<c:if test="${modifyPasswords == true}">
					<tr>
						<td><openmrs:message code="User.secretQuestion" /></td>
						<td><input type="text" name="secretQuestion" size="50" value="${user.secretQuestion}" /> <i><openmrs:message code="general.optional"/></i></td>
					</tr>
					<tr>
						<td><openmrs:message code="User.secretAnswer" /></td>
						<td><input type="password" autocomplete="off" name="secretAnswer" size="50" value=""/> <i><openmrs:message code="general.optional"/></i></td>
					</tr>
				</c:if>
			<tr>
         	   <c:if test="${user.userId != null}">
           		<td><font color="#D0D0D0"><sub><openmrs:message code="general.uuid" /></sub></font></td>
           		<td colspan="${fn:length(locales)}"><font color="#D0D0D0"><sub>${user.uuid}</sub></font></td>
      		   </c:if>
 	        </tr> 
			<c:if test="${fn:length(user.userProperties) > 0}" >
				<tr>
					<td valign="top" colspan="2"><openmrs:message code="User.userProperties" /></td>
				</tr>
		
				<tr>
					<td></td>
					<td>
						<table cellpadding="1" cellspacing="0">
								<thead>
									<tr>
										<td><openmrs:message code="general.name" /></td>
										<td><openmrs:message code="general.value" /></td>
									</tr>
								</thead>
								<tbody id="userPropsList">
								<c:forEach var="userProp" items="${user.userProperties}" varStatus="status">
									<tr class='${status.index % 2 == 0 ? "evenRow" : "oddRow"}'>
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
	<c:if test="${isNewUser == false}" >
		<fieldset><legend><openmrs:message code="User.creationInfo" /></legend>
		<table>
			<tr>
				<td><openmrs:message code="general.createdBy" /></td>
				<td><c:if test="${user.creator == null}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
				<openmrs:format user="${user.creator}" />
				</td>
			</tr>

			<tr>
				<td><openmrs:message code="general.dateCreated" /></td>	
				<td><openmrs:formatDate date="${user.dateCreated}" type="long"/></td>
			</tr>
		</table>
		</fieldset>
	</c:if>
	<c:if test="${user.retired == true}">
		<br/>
		<fieldset><legend><openmrs:message code="User.retiredInfo" /></legend>
		<table>
			<tr>
				<td><openmrs:message code="User.retiredBy" /></td>
			<td><c:if test="${user.retiredBy == null}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
			<openmrs:format user="${user.retiredBy}" />
			</td>
			</tr>
			<tr>
				<td><openmrs:message code="User.dateRetired" /></td>
				<td><openmrs:formatDate date="${user.dateRetired}" type="long"/></td>
			</tr>
		</table>
		</fieldset>
 	</c:if>

	<br/>
	
	<input type="submit" id="saveButton" name="action" value="<openmrs:message code="User.save"/>" />
	
	<c:if test="${user.userId != null}">
		<c:if test="${!user.retired}">
		<openmrs:hasPrivilege privilege="Become User (Actually you need to be a superuser)">
			&nbsp;&nbsp;&nbsp;&nbsp;
			<input type="submit" name="action" value="<openmrs:message code="User.assumeIdentity" />" onClick="return confirm('<openmrs:message code="User.assumeIdentity.confirm"/>');" />
		</openmrs:hasPrivilege>
		</c:if>
		<openmrs:hasPrivilege privilege="Delete User">
			&nbsp;&nbsp;&nbsp;&nbsp;
			<input type="submit" name="action" value="<openmrs:message code="User.delete" />" onClick="return confirm('<openmrs:message code="User.delete.confirm"/>');" />
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
					<h4><openmrs:message code="User.retire"/></h4>
					
					<b><openmrs:message code="general.reason"/></b>
					<spring:bind path="user.retireReason">
						<input type="text" 
								name="${status.expression}" 
								value="${status.value}" 
								autocomplete="off" />
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</spring:bind>
					<br/>
					<input type="submit" value='<openmrs:message code="User.retire"/>' name="action"/>			
				</c:when>
				<c:otherwise>
					<input type="submit" value='<openmrs:message code="User.unRetire"/>' name="action"/>	
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