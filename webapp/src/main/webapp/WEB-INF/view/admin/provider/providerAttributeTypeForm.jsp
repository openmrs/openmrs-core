<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Provider Attribute Types,Purge Provider Attribute Types" otherwise="/login.htm" redirect="/admin/provider/providerAttributeType.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript">

	function confirmPurge() {
		if (confirm("<openmrs:message code='ProviderAttributeType.purgeConfirmMessage' />")) {
			return true;
		} else {
			return false;
		}
	}

	function forceMaxLength(object, maxLength) {
		if (object.value.length >= maxLength) {
			object.value = object.value.substring(0, maxLength); 
		}
	}
	
	$j(function() {
		$j('select[name="datatypeClassname"]').change(function() {
			$j('#datatypeDescription').load(openmrsContextPath + '/q/message.form', { key: $j(this).val() + '.description' });
		});
		$j('select[name="preferredHandlerClassname"]').change(function() {
			$j('#handlerDescription').load(openmrsContextPath + '/q/message.form', { key: $j(this).val() + '.description' });  
		});
		<c:if test="${ not empty providerAttributeType.datatypeClassname }">
			$j('#datatypeDescription').load(openmrsContextPath + '/q/message.form', { key: '${ providerAttributeType.datatypeClassname }.description' });
		</c:if>
		<c:if test="${ not empty providerAttributeType.preferredHandlerClassname }">
			$j('#handlerDescription').load(openmrsContextPath + '/q/message.form', { key: '${ providerAttributeType.preferredHandlerClassname }.description' });
		</c:if>
	});
</script>

<h2><openmrs:message code="ProviderAttributeType.title"/></h2>

<openmrs:extensionPoint pointId="org.openmrs.admin.provider.providerForm.belowTitle" type="html" parameters="providerAttributeTypeId=${providerAttributeType.providerAttributeTypeId}" />

<spring:hasBindErrors name="providerAttributeType">
	<openmrs:message htmlEscape="false" code="fix.error"/>
	<br />
</spring:hasBindErrors>
<form method="post">
<fieldset>
<table>
	<tr>
		<td><openmrs:message code="general.name"/><span class="required">*</span></td>
		<td>
			<spring:bind path="providerAttributeType.name">
				<input type="text" name="name" value="${status.value}" size="50" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><openmrs:message code="general.description"/></td>
		<td valign="top">
			<spring:bind path="providerAttributeType.description">
				<textarea name="description" rows="3" cols="40" onkeypress="return forceMaxLength(this, 1024);" >${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="FormField.minOccurs"/></td>
		<td>
			<spring:bind path="providerAttributeType.minOccurs">
				<input type="text" name="minOccurs" value="${status.value}" size="10" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="FormField.maxOccurs"/></td>
		<td>
			<spring:bind path="providerAttributeType.maxOccurs">
				<input type="text" name="maxOccurs" value="${status.value}" size="10" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="AttributeType.datatypeClassname"/><span class="required">*</span></td>
		<td>
			<spring:bind path="providerAttributeType.datatypeClassname">
				<select name="datatypeClassname">
					<option value=""></option>
					<c:forEach items="${datatypes}" var="datatype">
						<option value="${datatype}" <c:if test="${datatype == status.value}">selected</c:if>><openmrs:message code="${datatype}.name"/></option>
					</c:forEach>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				<br/>
				<span id="datatypeDescription"></span>
			</spring:bind>
		</td>
	</tr>	
	<tr>
		<td><openmrs:message code="AttributeType.datatypeConfig"/></td>
		<td>
			<spring:bind path="providerAttributeType.datatypeConfig">
				<textarea name="datatypeConfig" rows="3" cols="40" >${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="AttributeType.preferredHandlerClassname"/></td>
		<td>
			<spring:bind path="providerAttributeType.preferredHandlerClassname">
				<select name="preferredHandlerClassname">
					<option value=""><openmrs:message code="general.default"/></option>
					<c:forEach items="${handlers}" var="handler">
						<option value="${handler}" <c:if test="${handler == status.value}">selected</c:if>><openmrs:message code="${handler}.name"/></option>
					</c:forEach>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				<br/>
				<span id="handlerDescription"></span>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="AttributeType.handlerConfig"/></td>
		<td>
			<spring:bind path="providerAttributeType.handlerConfig">
				<textarea name="handlerConfig" rows="3" cols="40" >${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(providerAttributeType.creator == null)}">
		<tr>
			<td><openmrs:message code="general.createdBy" /></td>
			<td><openmrs:format user="${ providerAttributeType.creator }"/></td>
		</tr>
	</c:if>
	<tr>
         <c:if test="${providerAttributeType.providerAttributeTypeId != null}">
           <td><font color="#D0D0D0"><sub><openmrs:message code="general.uuid"/></sub></font></td>
           <td colspan="${fn:length(locales)}"><font color="#D0D0D0"><sub>${providerAttributeType.uuid}</sub></font></td>
         </c:if>
   </tr>
</table>
<br />

<openmrs:extensionPoint pointId="org.openmrs.admin.provider.providerForm.inForm" type="html" parameters="providerAttributeTypeId=${providerAttributeType.providerAttributeTypeId}" />

<input type="submit" value="<openmrs:message code="ProviderAttributeType.save"/>" name="save">

</fieldset>
</form>

<br/>

<c:if test="${not providerAttributeType.retired && not empty providerAttributeType.providerAttributeTypeId}">
	<form method="post">
		<fieldset>
			<h4><openmrs:message code="ProviderAttributeType.retireProviderAttributeType"/></h4>
			
			<b><openmrs:message code="general.reason"/></b>
			<input type="text" value="" size="40" name="retireReason" />
			<spring:hasBindErrors name="providerAttributeType">
				<c:forEach items="${errors.allErrors}" var="error">
					<c:if test="${error.code == 'retireReason'}"><span class="error"><openmrs:message code="${error.defaultMessage}" text="${error.defaultMessage}"/></span></c:if>
				</c:forEach>
			</spring:hasBindErrors>
			<br/>
			<input type="submit" value='<openmrs:message code="ProviderAttributeType.retireProviderAttributeType"/>' name="retire"/>
		</fieldset>
	</form>
</c:if>

<br/>

<c:if
	test="${providerAttributeType.retired == true && not empty providerAttributeType.providerAttributeTypeId}">
	<openmrs:hasPrivilege privilege="Manage Provider Attribute Types">
		<form id="unretire" method="post">
		<fieldset>
		<h4><openmrs:message
			code="ProviderAttributeType.unretireProviderAttributeType" /></h4>
		<input type="submit"
			value='<openmrs:message code="ProviderAttributeType.unretireProviderAttributeType"/>'
			name="unretire" /></fieldset>
		</form>
	</openmrs:hasPrivilege>
</c:if>
<br />

<c:if test="${not empty providerAttributeType.providerAttributeTypeId}">
	<openmrs:hasPrivilege privilege="Purge Provider Attribute Types">
		<form id="purge" method="post" onsubmit="return confirmPurge()">
			<fieldset>
				<h4><openmrs:message code="ProviderAttributeType.purgeProviderAttributeType"/></h4>
				<input type="submit" value='<openmrs:message code="ProviderAttributeType.purgeProviderAttributeType"/>' name="purge" />
			</fieldset>
		</form>
	</openmrs:hasPrivilege>
</c:if>

<openmrs:extensionPoint pointId="org.openmrs.admin.provider.providerAttributeTypeForm.footer" type="html" parameters="providerAttributeTypeId=${providerAttributeType.providerAttributeTypeId}" />

<script type="text/javascript">
 document.forms[0].elements[0].focus();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>