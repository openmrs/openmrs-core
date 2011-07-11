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
		var retVal = confirm('<spring:message code="PersonAttributeType.UnretirePersonAtrributeMessage"/>');
		return retVal;
	}
	
	
	   function forceMaxLength(object, maxLength) {
	      if( object.value.length >= maxLength) {
	         object.value = object.value.substring(0, maxLength); 
	      }
	   }

	
</script>

<h2><spring:message code="PersonAttributeType.title"/></h2>

<openmrs:extensionPoint pointId="org.openmrs.admin.persons.personForm.belowTitle" type="html" parameters="personAttributeTypeId=${personAttributeType.personAttributeTypeId}"/>
<form method="post">
<fieldset>
<table>
	<tr>
		<td><spring:message code="general.name"/></td>
		<td>
			<spring:bind path="personAttributeType.name">
				<input type="text" name="name" value="${status.value}" size="50" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td valign="top">
			<spring:bind path="personAttributeType.description">
				<textarea name="description" rows="3" cols="40" onkeypress="return forceMaxLength(this, 1024);" >${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="FormField.minOccurs"/></td>
		<td>
			<spring:bind path="personAttributeType.minOccurs">
				<input type="text" name="minOccurs" value="${status.value}" size="10" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="FormField.maxOccurs"/></td>
		<td>
			<spring:bind path="personAttributeType.maxOccurs">
				<input type="text" name="maxOccurs" value="${status.value}" size="10" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="AttributeType.datatype"/></td>
		<td>
			<spring:bind path="personAttributeType.datatype">
				<select name="datatype">
					<option value=""></option>
					<c:forEach items="${datatypes}" var="datatype">
						<option value="${datatype}" <c:if test="${datatype == status.value}">selected</c:if>>${datatype}</option>
					</c:forEach>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="AttributeType.handlerConfig"/></td>
		<td>
			<spring:bind path="personAttributeType.handlerConfig">
				<textarea name="handlerConfig" rows="3" cols="40" >${status.value}</textarea>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(personAttributeType.creator == null)}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td><openmrs:format user="${ personAttributeType.creator }"/></td>
		</tr>
	</c:if>
</table>
<br />

<input type="submit" value="<spring:message code="PersonAttributeType.save"/>" name="save">

</fieldset>
</form>

<br/>

<c:if test="${not personAttributeType.retired && not empty personAttributeType.personAttributeTypeId}">
	<form method="post">
		<fieldset>
			<h4><spring:message code="PersonAttributeType.retirePersonAttributeType"/></h4>
			
			<b><spring:message code="general.reason"/></b>
			<input type="text" value="" size="40" name="retireReason" />
			<spring:hasBindErrors name="personAttributeType">
				<c:forEach items="${errors.allErrors}" var="error">
					<c:if test="${error.code == 'retireReason'}"><span class="error"><spring:message code="${error.defaultMessage}" text="${error.defaultMessage}"/></span></c:if>
				</c:forEach>
			</spring:hasBindErrors>
			<br/>
			<input type="submit" value='<spring:message code="PersonAttributeType.retirePersonAttributeType"/>' name="retire"/>
		</fieldset>
	</form>
</c:if>

<br/>

<c:if
	test="${personAttributeType.retired == true && not empty personAttributeType.personAttributeTypeId}">
	<openmrs:hasPrivilege privilege="Manage Person Attribute Types">
		<form id="unretire" method="post">
		<fieldset>
		<h4><spring:message
			code="PersonAttributeType.unretirePersonAttributeType" /></h4>
		<input type="submit"
			value='<spring:message code="PersonAttributeType.unretirePersonAttributeType"/>'
			name="unretire" /></fieldset>
		</form>
	</openmrs:hasPrivilege>
</c:if>
<br />

<c:if test="${not empty visitAttributeType.visitAttributeTypeId}">
	<openmrs:hasPrivilege privilege="Purge Person Attribute Types">
		<form id="purge" method="post" onsubmit="return confirmPurge()">
			<fieldset>
				<h4><spring:message code="PersonAttributeType.purgePersonAttributeType"/></h4>
				<input type="submit" value='<spring:message code="PersonAttributeType.purgePersonAttributeType"/>' name="purge" />
			</fieldset>
		</form>
	</openmrs:hasPrivilege>
</c:if>

<openmrs:extensionPoint pointId="org.openmrs.admin.person.personAttributeTypeForm.footer" type="html" parameters="personAttributeTypeId=${visitAttributeType.personAttributeTypeId}" />

<script type="text/javascript">
 document.forms[0].elements[0].focus();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>