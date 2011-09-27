<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Provider Attribute Types,Purge Provider Attribute Types" otherwise="/login.htm" redirect="/admin/provider/providerAttributeType.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript">

	function confirmPurge() {
		if (confirm("<spring:message code='ProviderAttributeType.purgeConfirmMessage' />")) {
			return true;
		} else {
			return false;
		}
	}
	
</script>

<script type="text/javascript">
   function forceMaxLength(object, maxLength) {
      if( object.value.length >= maxLength) {
         object.value = object.value.substring(0, maxLength); 
      }
   }
</script>

<h2><spring:message code="ProviderAttributeType.title"/></h2>

<openmrs:extensionPoint pointId="org.openmrs.admin.provider.providerForm.belowTitle" type="html" parameters="providerAttributeTypeId=${providerAttributeType.providerAttributeTypeId}" />

<spring:hasBindErrors name="providerAttributeType">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>
<form method="post">
<fieldset>
<table>
	<tr>
		<td><spring:message code="general.name"/></td>
		<td>
			<spring:bind path="providerAttributeType.name">
				<input type="text" name="name" value="${status.value}" size="50" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td valign="top">
			<spring:bind path="providerAttributeType.description">
				<textarea name="description" rows="3" cols="40" onkeypress="return forceMaxLength(this, 1024);" >${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="FormField.minOccurs"/></td>
		<td>
			<spring:bind path="providerAttributeType.minOccurs">
				<input type="text" name="minOccurs" value="${status.value}" size="10" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="FormField.maxOccurs"/></td>
		<td>
			<spring:bind path="providerAttributeType.maxOccurs">
				<input type="text" name="maxOccurs" value="${status.value}" size="10" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="AttributeType.datatype"/></td>
		<td>
			<spring:bind path="providerAttributeType.datatype">
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
			<spring:bind path="providerAttributeType.handlerConfig">
				<textarea name="handlerConfig" rows="3" cols="40" >${status.value}</textarea>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(providerAttributeType.creator == null)}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td><openmrs:format user="${ providerAttributeType.creator }"/></td>
		</tr>
	</c:if>
</table>
<br />

<openmrs:extensionPoint pointId="org.openmrs.admin.provider.providerForm.inForm" type="html" parameters="providerAttributeTypeId=${providerAttributeType.providerAttributeTypeId}" />

<input type="submit" value="<spring:message code="ProviderAttributeType.save"/>" name="save">

</fieldset>
</form>

<br/>

<c:if test="${not providerAttributeType.retired && not empty providerAttributeType.providerAttributeTypeId}">
	<form method="post">
		<fieldset>
			<h4><spring:message code="ProviderAttributeType.retireProviderAttributeType"/></h4>
			
			<b><spring:message code="general.reason"/></b>
			<input type="text" value="" size="40" name="retireReason" />
			<spring:hasBindErrors name="providerAttributeType">
				<c:forEach items="${errors.allErrors}" var="error">
					<c:if test="${error.code == 'retireReason'}"><span class="error"><spring:message code="${error.defaultMessage}" text="${error.defaultMessage}"/></span></c:if>
				</c:forEach>
			</spring:hasBindErrors>
			<br/>
			<input type="submit" value='<spring:message code="ProviderAttributeType.retireProviderAttributeType"/>' name="retire"/>
		</fieldset>
	</form>
</c:if>

<br/>

<c:if
	test="${providerAttributeType.retired == true && not empty providerAttributeType.providerAttributeTypeId}">
	<openmrs:hasPrivilege privilege="Manage Provider Attribute Types">
		<form id="unretire" method="post">
		<fieldset>
		<h4><spring:message
			code="ProviderAttributeType.unretireProviderAttributeType" /></h4>
		<input type="submit"
			value='<spring:message code="ProviderAttributeType.unretireProviderAttributeType"/>'
			name="unretire" /></fieldset>
		</form>
	</openmrs:hasPrivilege>
</c:if>
<br />

<c:if test="${not empty providerAttributeType.providerAttributeTypeId}">
	<openmrs:hasPrivilege privilege="Purge Provider Attribute Types">
		<form id="purge" method="post" onsubmit="return confirmPurge()">
			<fieldset>
				<h4><spring:message code="ProviderAttributeType.purgeProviderAttributeType"/></h4>
				<input type="submit" value='<spring:message code="ProviderAttributeType.purgeProviderAttributeType"/>' name="purge" />
			</fieldset>
		</form>
	</openmrs:hasPrivilege>
</c:if>

<openmrs:extensionPoint pointId="org.openmrs.admin.provider.providerAttributeTypeForm.footer" type="html" parameters="providerAttributeTypeId=${providerAttributeType.providerAttributeTypeId}" />

<script type="text/javascript">
 document.forms[0].elements[0].focus();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>