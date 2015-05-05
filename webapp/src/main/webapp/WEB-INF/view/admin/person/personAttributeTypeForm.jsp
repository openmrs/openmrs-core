<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Person Attribute Types" otherwise="/login.htm" redirect="/admin/person/personAttributeType.form" />

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
	
	function confirmUnretire() {
		var retVal = confirm('<openmrs:message code="PersonAttributeType.UnretirePersonAtrributeMessage"/>');
		return retVal;
	}
	
</script>

<h2><openmrs:message code="PersonAttributeType.title"/></h2>

<form method="post">
<fieldset>
<table>
	<tr>
		<td><openmrs:message code="general.name"/><span class="required">*</span></td>
		<td>
			<spring:bind path="personAttributeType.name">
				<input type="text" name="name" value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="PersonAttributeType.format"/><span class="required">*</span></td>
		<td>
			<spring:bind path="personAttributeType.format">
				<%-- This logic is here because java.util.Date should not be allowed, but existing entries may have that value, and it's impossible to fix that automatically. --%>
				<c:set var="isJavaUtilDate" value='${status.value == "java.util.Date"}'/>
				<select name="format">
                    <option value=""></option>
                    <c:forEach items="${formats}" var="format">
                        <option value="${format}" <c:if test="${format == status.value}">selected</c:if>>${format}</option>
                    </c:forEach>
                    <c:if test="${isJavaUtilDate}">
						<option value="java.util.Date" selected="true">java.util.Date</option>
					</c:if>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				<c:if test="${isJavaUtilDate != ''}">
					<br/>
					<span class="error"><openmrs:message htmlEscape="false" code="PersonAttributeType.java.util.Date.warning"/></span>
				</c:if>
			</spring:bind>
		</td>
		<td><i><openmrs:message code="PersonAttributeType.format.help"/></i></td>
	</tr>
	<tr>
		<td><openmrs:message code="PersonAttributeType.foreignKey"/></td>
		<td>
			<spring:bind path="personAttributeType.foreignKey">
				<input type="text" name="foreignKey" value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
		<td><i><openmrs:message code="PersonAttributeType.foreignKey.help"/></i></td>
	</tr>
	<tr>
		<td><openmrs:message code="PersonAttributeType.searchable"/></td>
		<td>
			<spring:bind path="personAttributeType.searchable">
				<input type="hidden" name="_${status.expression}">
				<input type="checkbox" name="${status.expression}" 
					   id="${status.expression}" 
					   <c:if test="${status.value == true}">checked</c:if> 
				/>
			</spring:bind>
		</td>
		<td><i><openmrs:message code="PersonAttributeType.searchable.help"/></i></td>
	</tr>
	<tr>
		<td valign="top"><openmrs:message code="general.description"/></td>
		<td valign="top">
			<spring:bind path="personAttributeType.description">
				<textarea name="description" rows="3" cols="40">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="PersonAttributeType.editPrivilege"/></td>
		<td>
			<spring:bind path="personAttributeType.editPrivilege">
				<select name="editPrivilege">
					<option value=""></option>
					<c:forEach items="${privileges}" var="privilege">
						<option value="${privilege.privilege}" <c:if test="${privilege.privilege == status.value}">selected</c:if>>${privilege.privilege}</option>
					</c:forEach>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
		<td><i><openmrs:message code="PersonAttributeType.editPrivilege.help"/></i></td>
	</tr>	
	<c:if test="${personAttributeType.creator != null}">
		<tr>
			<td><openmrs:message code="general.createdBy" /></td>
			<td>
				<c:out value="${personAttributeType.creator.personName}" /> -
				<openmrs:formatDate date="${personAttributeType.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
	<c:if test="${personAttributeType.changedBy != null}">
		<tr>
			<td><openmrs:message code="general.changedBy" /></td>
			<td>
				<c:out value="${personAttributeType.changedBy.personName}" /> -
				<openmrs:formatDate date="${personAttributeType.dateChanged}" type="long" />
			</td>
		</tr>
	</c:if>
	<tr>
     <c:if test="${personAttributeType.personAttributeTypeId != null}">
       <td><font color="#D0D0D0"><sub><openmrs:message code="general.uuid"/></sub></font></td>
       <td colspan="${fn:length(locales)}"><font color="#D0D0D0"><sub>${personAttributeType.uuid}</sub></font></td>
     </c:if>
   </tr>
</table>
<input type="hidden" name="personAttributeTypeId:int" value="${personAttributeType.personAttributeTypeId}">
<br />
<openmrs:globalProperty key="personAttributeTypes.locked" var="PersonAttributeTypesLocked"/>
<input type="submit" value="<openmrs:message code="PersonAttributeType.save"/>" name="save" <c:if test="${PersonAttributeTypesLocked == 'true'}"> disabled</c:if>>
</fieldset>
</form>

<br/>

<c:if test="${not personAttributeType.retired && not empty personAttributeType.personAttributeTypeId}">
	<form method="post">
		<fieldset>
			<h4><openmrs:message code="PersonAttributeType.retirePersonAttributeType"/></h4>
			
			<b><openmrs:message code="general.reason"/></b>
			<input type="text" value="" size="40" name="retireReason" />
			<spring:hasBindErrors name="personAttributeType">
				<c:forEach items="${errors.allErrors}" var="error">
					<c:if test="${error.code == 'retireReason'}"><span class="error"><openmrs:message code="${error.defaultMessage}" text="${error.defaultMessage}"/></span></c:if>
				</c:forEach>
			</spring:hasBindErrors>
			<br/>
			<input type="submit" value='<openmrs:message code="PersonAttributeType.retirePersonAttributeType"/>' name="retire" <c:if test="${PersonAttributeTypesLocked == 'true'}"> disabled</c:if> />
		</fieldset>
	</form>
</c:if>

<br/>
<c:if
	test="${personAttributeType.retired == true && not empty personAttributeType.personAttributeTypeId}">
	<openmrs:hasPrivilege privilege="Manage Person Attribute Types">
		<form id="unretire" method="post" onsubmit="return confirmUnretire()">
		<fieldset>
		<h4><openmrs:message
			code="PersonAttributeType.UnretirePersonAttributeType" /></h4>
		<input type="submit"
			value='<openmrs:message code="PersonAttributeType.UnretirePersonAttributeType"/>'
			name="unretire" <c:if test="${PersonAttributeTypesLocked == 'true'}"> disabled</c:if> /></fieldset>
		</form>
	</openmrs:hasPrivilege>
</c:if>
<br />

<c:if test="${not empty personAttributeType.personAttributeTypeId}">
	<openmrs:hasPrivilege privilege="Purge Person Attribute Types">
		<form id="purge" method="post" onsubmit="return confirmPurge()">
			<fieldset>
				<h4><openmrs:message code="PersonAttributeType.purgePersonAttributeType"/></h4>
				<input type="submit" value='<openmrs:message code="PersonAttributeType.purgePersonAttributeType"/>' name="purge" <c:if test="${PersonAttributeTypesLocked == 'true'}"> disabled</c:if> />
			</fieldset>
		</form>
	</openmrs:hasPrivilege>
</c:if>

<script type="text/javascript">
 document.forms[0].elements[0].focus();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>