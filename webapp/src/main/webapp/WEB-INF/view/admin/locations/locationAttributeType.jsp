<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Location Attribute Types" otherwise="/login.htm" redirect="/admin/locations/locationAttributeTypes.list" />

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

	function forceMaxLength(object, maxLength) {
		if ( object.value.length >= maxLength) {
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
		<c:if test="${ not empty attributeType.datatypeClassname }">
			$j('#datatypeDescription').load(openmrsContextPath + '/q/message.form', { key: '${ attributeType.datatypeClassname }.description' });
		</c:if>
		<c:if test="${ not empty attributeType.preferredHandlerClassname }">
			$j('#handlerDescription').load(openmrsContextPath + '/q/message.form', { key: '${ attributeType.preferredHandlerClassname }.description' });
		</c:if>
	});
</script>

<h2>
	<c:choose>
		<c:when test="${ empty attributeType.id }">
			<openmrs:message code="LocationAttributeType.add.title"/>
		</c:when>
		<c:otherwise>
			<openmrs:message code="LocationAttributeType.edit.title"/>
		</c:otherwise>
	</c:choose>
</h2>

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationAttributeType.belowTitle" type="html" parameters="id=${ attributeType.id }" />

<spring:hasBindErrors name="attributeType">
	<openmrs:message htmlEscape="false" code="fix.error"/>
	<br />
</spring:hasBindErrors>
<form method="post">
<fieldset>
<table>
	<tr>
		<td><openmrs:message code="general.name"/><span class="required">*</span></td>
		<td>
			<spring:bind path="attributeType.name">
				<input type="text" name="name" value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><openmrs:message code="general.description"/></td>
		<td valign="top">
			<spring:bind path="attributeType.description">
				<textarea name="description" rows="3" cols="40" onkeypress="return forceMaxLength(this, 1024);" >${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="AttributeType.minOccurs"/></td>
		<td>
			<spring:bind path="attributeType.minOccurs">
				<input type="text" name="minOccurs" value="${status.value}" size="10" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="AttributeType.maxOccurs"/></td>
		<td>
			<spring:bind path="attributeType.maxOccurs">
				<input type="text" name="maxOccurs" value="${status.value}" size="10" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="AttributeType.datatypeClassname"/><span class="required">*</span></td>
		<td>
			<spring:bind path="attributeType.datatypeClassname">
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
			<spring:bind path="attributeType.datatypeConfig">
				<textarea name="datatypeConfig" rows="3" cols="40" >${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="AttributeType.preferredHandlerClassname"/></td>
		<td>
			<spring:bind path="attributeType.preferredHandlerClassname">
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
			<spring:bind path="attributeType.handlerConfig">
				<textarea name="handlerConfig" rows="3" cols="40" >${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${ not empty attributeType.creator }">
		<tr>
			<td><openmrs:message code="general.createdBy" /></td>
			<td>
				<c:out value="${attributeType.creator.personName}" /> -
				<openmrs:formatDate date="${attributeType.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
	<tr>
    <td><font color="#D0D0D0"><sub><openmrs:message code="general.uuid"/></sub></font></td>
     <td colspan="${fn:length(locales)}">
       <font color="#D0D0D0"><sub>
       <c:if test="${attributeType.locationAttributeTypeId != null}">
       <spring:bind path="attributeType.uuid">
           <c:out value="${status.value}"></c:out>
       </spring:bind></c:if>
       </sub></font>
     </td>
   </tr>
</table>
<br />

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationAttributeType.inForm" type="html" parameters="id=${ attributeType.id }" />

<input type="submit" value="<openmrs:message code="LocationAttributeType.save"/>" name="save">

</fieldset>
</form>

<br/>

<c:if test="${ not attributeType.retired && not empty attributeType.id }">
	<form method="post">
		<fieldset>
			<h4><openmrs:message code="LocationAttributeType.retire"/></h4>
			
			<b><openmrs:message code="general.reason"/></b>
			<input type="text" value="" size="40" name="retireReason" />
			<spring:hasBindErrors name="attributeType">
				<c:forEach items="${ errors.allErrors }" var="error">
					<c:if test="${ error.code == 'retireReason' }"><span class="error"><openmrs:message code="${ error.defaultMessage }" text="${ error.defaultMessage }"/></span></c:if>
				</c:forEach>
			</spring:hasBindErrors>
			<br/>
			<input type="submit" value='<openmrs:message code="LocationAttributeType.retire"/>' name="retire"/>
		</fieldset>
	</form>
</c:if>

<br/>

<c:if test="${ not empty attributeType.id }">
	<openmrs:hasPrivilege privilege="Purge Location Attribute Types">
		<form id="purge" method="post" onsubmit="return confirmPurge()">
			<fieldset>
				<h4><openmrs:message code="LocationAttributeType.purge"/></h4>
				<input type="submit" value='<openmrs:message code="LocationAttributeType.purge"/>' name="purge" />
			</fieldset>
		</form>
	</openmrs:hasPrivilege>
</c:if>

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationAttributeTypeForm.footer" type="html" parameters="id=${ attributeType.id }" />

<%@ include file="/WEB-INF/template/footer.jsp" %>