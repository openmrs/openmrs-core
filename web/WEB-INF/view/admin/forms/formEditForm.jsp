<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Forms" otherwise="/login.htm" redirect="/admin/forms/formEdit.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Form.edit.title"/></h2>

<spring:hasBindErrors name="form">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message code="${error.code}" text="${error.code}"/><br/><!-- ${error} -->
		</c:forEach>
	</div>
</spring:hasBindErrors>

<c:if test="${form.retired}">
	<div class="retiredMessage"><div><spring:message code="Form.retiredMessage"/></div></div>
</c:if>

<br/>
<a href="formSchemaDesign.form?formId=${form.formId}"><spring:message code="Form.designSchema" /></a> |
<a href="${pageContext.request.contextPath}/formDownload?target=schema&formId=${form.formId}"><spring:message code="Form.downloadSchema" /></a> |
<a href="${pageContext.request.contextPath}/formDownload?target=template&formId=${form.formId}"><spring:message code="Form.downloadTemplate" /></a> |
<a href="${pageContext.request.contextPath}/formDownload?target=xsn&formId=${form.formId}"><spring:message code="Form.downloadXSN" /></a>
<br/>
<br/>

<form method="post" enctype="multipart/form-data">
<table>
	<tr>
		<td><spring:message code="general.name"/></td>
		<td>
			<spring:bind path="form.name">
				<input type="text" name="${status.expression}" value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}"><c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td valign="top">
			<spring:bind path="form.description">
				<textarea name="description" rows="3" cols="40">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Form.version"/></td>
		<td>
			<spring:bind path="form.version">
				<input type="text" name="${status.expression}" value="${status.value}" size="5" />
				<c:if test="${status.errorMessage != ''}"><c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Form.build"/></td>
		<td>
			<spring:bind path="form.build">
				<input type="text" name="${status.expression}" value="${status.value}" size="5" />
				<c:if test="${status.errorMessage != ''}"><c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Form.published"/></td>
		<td>
			<spring:bind path="form.published">
				<input type="hidden" name="_${status.expression}">
				<input type="checkbox" name="${status.expression}" 
					   id="${status.expression}" 
					   <c:if test="${status.value == true}">checked</c:if> 
				/>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Encounter.type"/></td>
		<td>
			<spring:bind path="form.encounterType">
				<select name="encounterType">
					<c:forEach items="${encounterTypes}" var="type">
						<option value="${type.encounterTypeId}" <c:if test="${type.encounterTypeId == status.value}">selected</c:if>>${type.name}</option>
					</c:forEach>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Form.schemaNamespace"/></td>
		<td>
			<spring:bind path="form.schemaNamespace">
				<input type="text" name="${status.expression}" value="${status.value}" size="55" />
				<c:if test="${status.errorMessage != ''}"><c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Form.infoPathSolutionVersion"/></td>
		<td>
			<spring:bind path="form.infoPathSolutionVersion">
				<input type="text" name="${status.expression}" value="${status.value}" size="55" />
				<c:if test="${status.errorMessage != ''}"><c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Form.uri"/></td>
		<td>
			<spring:bind path="form.uri">
				<input type="text" name="${status.expression}" value="${status.value}" size="55" />
				<c:if test="${status.errorMessage != ''}"><c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Form.xslt"/></td>
		<td>
			<spring:bind path="form.xslt">
				<c:if test="${form.xslt != ''}"><a target="_new" href="formViewXslt.form?formId=${form.formId}"><spring:message code="Form.xslt.view"/></a><br/></c:if>
				<spring:message code="Form.xslt.upload"/> <input type="file" name="xslt_file" size="25" />
				<c:if test="${status.errorMessage != ''}"><c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="general.retired"/></td>
		<td>
			<spring:bind path="form.retired">
				<input type="hidden" name="_${status.expression}">
				<input type="checkbox" name="${status.expression}" 
					   id="${status.expression}" 
					   <c:if test="${status.value == true}">checked</c:if> 
				/>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="general.retiredReason"/></td>
		<spring:bind path="form.retiredReason">
			<td>
				<input type="text" name="${status.expression}" id="retiredReason" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<c:if test="${form.retired}" >
		<tr>
			<td><spring:message code="general.retiredBy"/></td>
			<td>
				${form.retiredBy.firstName} ${form.retiredBy.lastName} -
				<openmrs:formatDate date="${form.dateRetired}" type="long" />
			</td>
		</tr>
	</c:if>
	<c:if test="${!(form.creator == null)}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td>
				${form.creator.firstName} ${form.creator.lastName} -
				<openmrs:formatDate date="${form.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
	<c:if test="${!(form.changedBy == null)}">
		<tr>
			<td><spring:message code="general.changedBy" /></td>
			<td>
				${form.changedBy.firstName} ${form.changedBy.lastName} -
				<openmrs:formatDate date="${form.dateChanged}" type="long" />
			</td>
		</tr>
	</c:if>
</table>
<br />
<c:if test="${not empty param.duplicate}">
	<input type="submit" name="action" value="<spring:message code="Form.create.duplicate"/>">
</c:if>
<c:if test="${empty param.duplicate}">
	<input type="submit" name="action" value="<spring:message code="Form.save"/>">
	
	<c:if test="${form.formId != null}">
		<openmrs:hasPrivilege privilege="Delete Forms">
			 &nbsp; &nbsp; &nbsp;
			<input type="submit" name="action" value="<spring:message code="Form.delete"/>" onclick="return confirm('Are you sure you want to delete this entire form AND schema?')"/>
		</openmrs:hasPrivilege>
	</c:if>
</c:if>

</form>



<%@ include file="/WEB-INF/template/footer.jsp" %>