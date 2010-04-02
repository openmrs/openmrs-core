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

<c:if test="${empty param.duplicate}">
<br/>
<a href="#designSchema"><spring:message code="Form.designSchema" /></a>
<c:if test="${form.formId != 1}">
	<openmrs:extensionPoint pointId="org.openmrs.admin.forms.formHeader" type="html" parameters="formId=${form.formId}">
		<c:forEach items="${extension.links}" var="link">
			| <a href="${pageContext.request.contextPath}/${link.key}"><spring:message code="${link.value}"/></a>
		</c:forEach>
	</openmrs:extensionPoint>
</c:if>
</c:if>

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
				<textarea name="description" rows="3" cols="40" type="_moz">${status.value}</textarea>
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
		<td><spring:message code="Form.published"/></td>
		<td>
			<spring:bind path="form.published">
				<input type="hidden" name="_${status.expression}">
				<input type="checkbox" name="${status.expression}" 
					   id="${status.expression}" 
					   <c:if test="${status.value == true && empty param.duplicate}">checked</c:if> 
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
					   onchange="document.getElementById('retiredReasonRow').style.display = (this.checked == true) ? '' : 'none';"
				/>
			</spring:bind>
		</td>
	</tr>
	<tr id="retiredReasonRow">
		<td><spring:message code="general.retiredReason"/></td>
		<spring:bind path="form.retireReason">
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
				${form.retiredBy.personName} -
				<openmrs:formatDate date="${form.dateRetired}" type="long" />
			</td>
		</tr>
	</c:if>
	<c:if test="${!(form.creator == null)}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td>
				${form.creator.personName} -
				<openmrs:formatDate date="${form.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
	<c:if test="${!(form.changedBy == null)}">
		<tr>
			<td><spring:message code="general.changedBy" /></td>
			<td>
				${form.changedBy.personName} -
				<openmrs:formatDate date="${form.dateChanged}" type="long" />
			</td>
		</tr>
	</c:if>
	<openmrs:extensionPoint pointId="org.openmrs.admin.forms.formRow" type="html" parameters="formId=${form.formId}">
		<c:forEach items="${extension.rows}" var="row">
			<tr>
				<td><spring:message code="${row.key}"/></td>
				<td>${row.value}</td>
			</tr>
		</c:forEach>
	</openmrs:extensionPoint>
</table>
<br />
<c:if test="${not empty param.duplicate}">
	<input type="submit" name="action" value="<spring:message code="Form.create.duplicate"/>">
</c:if>
<c:if test="${empty param.duplicate}">
	<input type="submit" name="action" value="<spring:message code="Form.save"/>">
	
	<c:if test="${form.formId != null && form.formId != 1}">
		<openmrs:hasPrivilege privilege="Delete Forms">
			 &nbsp; &nbsp; &nbsp;
			<input type="submit" name="action" value="<spring:message code="Form.delete"/>" onclick="return confirm('<spring:message code="Form.confirmation"/>')"/>
		</openmrs:hasPrivilege>
	</c:if>
</c:if>

</form>

<script type="text/javascript">
	document.getElementById('retiredReasonRow').style.display = document.getElementById('retired').checked ==true ? '' : 'none';
</script>

<c:if test="${empty param.duplicate}">
<!-- SCHEMA SECTION -->

<a name="designSchema"></a>

<openmrs:htmlInclude file="/dwr/util.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRFormService.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />
<openmrs:htmlInclude file="/admin/forms/formSchemaDesign.js" />

<style>
	.required {
		color: red;
	}
	a.delete {
		background: url(${pageContext.request.contextPath}/images/delete.gif) no-repeat center center;
		text-decoration: none;
		padding-left: 2px;
		cursor: pointer;
	}
	.disabled, .disabled * {
		color: gray;
		background-color: whitesmoke;
		}
		.disabled #formFieldTitle {
			background-color: whitesmoke;
		}
	div.dojoTree {
		overflow: hidden;
	}
	#editFormField {
		position: absolute;
		left: -1000px;
		top: 0px;
		background-color: white;
		z-index: 20;
		width: 500px;
		border: 2px solid lightgreen;
		padding: 1px;
		}
		#editFormField.disabled {
			border-color: gray;
		}
	
	#formFieldTitle {
		background-color: lightgreen;
		width: 100%;
	}
	#fieldWarning, #fieldWarningIframe {
		position: absolute;
		margin-left: 5%;
		margin-top: 7%;
		width: 90%;
		padding: 3px;
		}
		#fieldWarning {
			color: firebrick;
			border: 2px solid firebrick;
			text-align: center;
			z-index: 999;
			background-color: white;
		}
		#fieldWarningIframe {
			padding: 2px;
			z-index: 998;
			height: 50px;
		}
	span.fieldConceptHit {
		color: gray;
	}
	span.treeNodeRow div.dojoTree div.dojoTreeNode {
		display: inline;
	}
	.openmrsSearchTable tr td div {
		overflow: hidden;
	}
	#fieldSearchDiv {
		xposition: fixed;
		z-index: 10;
		background-color: white;
	}
	
</style>

<script type="text/javascript">
	var formPublished = ${form.published};
	var formId = <request:parameter name="formId"/>;
	<c:if test="${form.formId == 1}"> formPublished = true; </c:if>
</script>

<br/>
<h2>
	<spring:message code="Form.design.title" /><!-- - 
	${form.name} -->
</h2>

<br/>

<c:if test="${form.published == true}">
	<div class="retiredMessage"><div><spring:message code="Form.design.disabled"/></div></div>
</c:if>

<div dojoType="TreeBasicController" widgetId="treeController" DNDController="create"></div>
<div dojoType="TreeSelector" widgetId="searchTreeSelector"></div>
<div dojoType="TreeSelector" widgetId="treeSelector"></div>
<div dojoType="TreeContextMenu" toggle="explode" contextMenuForWindow="false" widgetId="treeContextMenu">
	<div dojoType="TreeMenuItem" treeActions="edit" caption="Edit Field" widgetId="treeContextMenuEdit"></div>
	<div dojoType="TreeMenuItem" treeActions="addChild" caption="Create" widgetId="treeContextMenuCreate"></div>
	<div dojoType="TreeMenuItem" treeActions="remove" caption="Remove" widgetId="treeContextMenuRemove"></div>
</div>

<div id="loadingTreeMessage">
	<br/>
	&nbsp;
	<spring:message code="general.loading" />
</div>

<table width="99%">
	<tr>
		<td valign="top">
			<div dojoType="Tree" menu="treeContextMenu" strictFolders="false" DNDMode="between" toggler="fade" widgetId="tree" DNDAcceptTypes="<c:if test="${form.published != true}">*</c:if>" controller="treeController" selector="treeSelector">
			</div>
		</td>
		<td valign="top" style="padding-left: 5px;" id="fieldSearch" width="40%">
			<div id="fieldSearchDiv">
				<c:if test="${form.published != true && form.formId != 1}">
					<div dojoType="FieldSearch" widgetId="fieldSearch" searchLabel='<spring:message code="Field.find" />' showHeaderRow="false" alsoSearchConcepts="true"></div>
				</c:if>
			</div>
		</td>
	</tr>
</table>

<div id="editFormField">
	<div id="formFieldTitle"><spring:message code="FormField.edit"/>:</div>
	
	<form xonsubmit="save(selectedNode)" style="padding: 0px; margin: 0px; overflow: auto">
		<%@ include file="include/formFieldEdit.jsp" %>
	
		<c:if test="${form.published != true && form.formId != 1}">
			<input type="submit" id="saveFormField" onclick="return save(selectedNode);" value="<spring:message code="general.save"/>" />
		</c:if>
		<input type="button" id="cancelFormField" onclick="cancelClicked()" value="<spring:message code="general.cancel"/>" />
	</form>
</div>
<!-- Spacer for schema editing -->
<div style="min-height: 300px"></div>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp" %>