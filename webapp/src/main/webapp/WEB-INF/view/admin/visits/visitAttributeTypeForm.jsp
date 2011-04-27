<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Visit Attribute Types,Purge Visit Attribute Types" otherwise="/login.htm" redirect="/admin/visits/visitAttributeType.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript">

	function confirmPurge() {
		if (confirm("<spring:message code='VisitAttributeType.purgeConfirmMessage' />")) {
			return true;
		} else {
			return false;
		}
	}
	
	function confirmUnretire() {
		var retVal = confirm('<spring:message code="VisitAttributeType.UnretireVisitAttributeTypeMessage"/>');
		return retVal;
	}
	
</script>

<script type="text/javascript">
   function forceMaxLength(object, maxLength) {
      if( object.value.length >= maxLength) {
         object.value = object.value.substring(0, maxLength); 
      }
   }
</script>

<h2><spring:message code="VisitAttributeType.title"/></h2>

<openmrs:extensionPoint pointId="org.openmrs.admin.visits.visitForm.belowTitle" type="html" parameters="visitAttributeTypeId=${visitAttributeType.visitAttributeTypeId}" />

<spring:hasBindErrors name="visitAttributeType">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>
<form method="post">
<fieldset>
<table>
	<tr>
		<td><spring:message code="general.name"/></td>
		<td>
			<spring:bind path="visitAttributeType.name">
				<input type="text" name="name" value="${status.value}" size="50" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td valign="top">
			<spring:bind path="visitAttributeType.description">
				<textarea name="description" rows="3" cols="40" onkeypress="return forceMaxLength(this, 1024);" >${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="FormField.minOccurs"/></td>
		<td>
			<spring:bind path="visitAttributeType.minOccurs">
				<input type="text" name="minOccurs" value="${status.value}" size="10" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="FormField.maxOccurs"/></td>
		<td>
			<spring:bind path="visitAttributeType.maxOccurs">
				<input type="text" name="maxOccurs" value="${status.value}" size="10" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="AttributeType.logicalType"/></td>
		<td>
			<spring:bind path="visitAttributeType.logicalType">
				<select name="logicalType">
					<option value=""></option>
					<c:forEach items="${logicalTypes}" var="logicalType">
						<option value="${logicalType}" <c:if test="${logicalType == status.value}">selected</c:if>>${logicalType}</option>
					</c:forEach>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="AttributeType.handlerConfig"/></td>
		<td>
			<spring:bind path="visitAttributeType.handlerConfig">
				<textarea name="handlerConfig" rows="3" cols="40" >${status.value}</textarea>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(visitAttributeType.creator == null)}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td><openmrs:format user="${ visitAttributeType.creator }"/></td>
		</tr>
	</c:if>
</table>
<br />

<openmrs:extensionPoint pointId="org.openmrs.admin.visits.visitForm.inForm" type="html" parameters="visitAttributeTypeId=${visitAttributeType.visitAttributeTypeId}" />

<input type="submit" value="<spring:message code="VisitAttributeType.save"/>" name="save">

</fieldset>
</form>

<br/>

<c:if test="${not visitAttributeType.retired && not empty visitAttributeType.visitAttributeTypeId}">
	<form method="post">
		<fieldset>
			<h4><spring:message code="VisitAttributeType.retireVisitAttributeType"/></h4>
			
			<b><spring:message code="general.reason"/></b>
			<input type="text" value="" size="40" name="retireReason" />
			<spring:hasBindErrors name="visitAttributeType">
				<c:forEach items="${errors.allErrors}" var="error">
					<c:if test="${error.code == 'retireReason'}"><span class="error"><spring:message code="${error.defaultMessage}" text="${error.defaultMessage}"/></span></c:if>
				</c:forEach>
			</spring:hasBindErrors>
			<br/>
			<input type="submit" value='<spring:message code="VisitAttributeType.retireVisitAttributeType"/>' name="retire"/>
		</fieldset>
	</form>
</c:if>

<br/>

<c:if
	test="${visitAttributeType.retired == true && not empty visitAttributeType.visitAttributeTypeId}">
	<openmrs:hasPrivilege privilege="Manage Visit Attribute Types">
		<form id="unretire" method="post" onsubmit="return confirmUnretire()">
		<fieldset>
		<h4><spring:message
			code="VisitAttributeType.UnretireVisitAttributeType" /></h4>
		<input type="submit"
			value='<spring:message code="VisitAttributeType.UnretireVisitAttributeType"/>'
			name="unretire" /></fieldset>
		</form>
	</openmrs:hasPrivilege>
</c:if>
<br />

<c:if test="${not empty visitAttributeType.visitAttributeTypeId}">
	<openmrs:hasPrivilege privilege="Purge Visit Attribute Types">
		<form id="purge" method="post" onsubmit="return confirmPurge()">
			<fieldset>
				<h4><spring:message code="VisitAttributeType.purgeVisitAttributeType"/></h4>
				<input type="submit" value='<spring:message code="VisitAttributeType.purgeVisitAttributeType"/>' name="purge" />
			</fieldset>
		</form>
	</openmrs:hasPrivilege>
</c:if>

<openmrs:extensionPoint pointId="org.openmrs.admin.visits.visitAttributeTypeForm.footer" type="html" parameters="visitAttributeTypeId=${visitAttributeType.visitAttributeTypeId}" />

<script type="text/javascript">
 document.forms[0].elements[0].focus();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>