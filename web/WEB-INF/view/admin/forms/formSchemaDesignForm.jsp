<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Forms" otherwise="/login.htm" redirect="/admin/forms/formSchemaDesign.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript">
	var djConfig = {debugAtAllCosts: false, isDebug: false };
</script>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/interface/DWRConceptService.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/interface/DWRFormService.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/engine.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/util.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/prototype.lite.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.pack.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/openmrsSearch.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/conceptSearch.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/admin/forms/formSchemaDesign.js"></script>

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
	
	#editFormField {
		position: absolute;
		left: -1000px;
		top: 0px;
		background-color: white;
		z-index: 20;
		width: 500px;
		border: 2px solid lightgreen;
		padding: 1px;
		overflow: auto;
		}
		#editFormField.disabled {
			border-color: grey;
		}
	
	#formFieldTitle {
		background-color: lightgreen;
		width: 100%;
	}

	.searchForm {
		width: 400px;
		position: absolute;
		z-index: 10;
		margin: 5px;
		left: -1000px;
		z-index: 1001;
		overflow: auto;
	}
	.searchForm .wrapper {
		padding: 2px;
		background-color: whitesmoke;
		border: 1px solid grey;
		height: 370px;
		z-index: 1001;
	}
	.searchResults {
		height: 400px;
		overflow: auto;
		width: 390px;
	}
	#closeButton {
		border: 1px solid gray;
		background-color: lightpink;
		font-size: .6em;
		color: black;
		margin: 2px;
		padding: 1px;
		cursor: pointer;
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
	#fieldResults tr td div {
		overflow: hidden;
	}
	
</style>

<script type="text/javascript">
	var formPublished = ${form.published};
	var formId = <request:parameter name="formId"/>;
	<c:if test="${form.formId == 1}"> formPublished = true; </c:if>
</script>

<h2>
	<spring:message code="Form.design.title" /> - 
	${form.name}
</h2>

<br/>
<a href="formEdit.form?formId=${form.formId}"><spring:message code="Form.editProperties" /></a>
<c:if test="${form.formId != 1}"> |
	<a href="${pageContext.request.contextPath}/formDownload?target=schema&formId=${form.formId}"><spring:message code="Form.downloadSchema" /></a> |
	<a href="${pageContext.request.contextPath}/formDownload?target=template&formId=${form.formId}"><spring:message code="Form.downloadTemplate" /></a> |
	<a href="${pageContext.request.contextPath}/formDownload?target=xsn&formId=${form.formId}"><spring:message code="Form.downloadXSN" /></a>
	<openmrs:hasPrivilege privilege="Upload XSN">
		| <a href="${pageContext.request.contextPath}/admin/formentry/xsnUpload.form"><spring:message code="FormEntry.xsn.manage"/></a>		
	</openmrs:hasPrivilege>
</c:if>

<br/><br/>

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

<table width="99%">
	<tr>
		<td valign="top">
			<div dojoType="Tree" menu="treeContextMenu" strictFolders="false" DNDMode="between" toggler="fade" widgetId="tree" DNDAcceptTypes="<c:if test="${form.published != true}">tree;miniTree</c:if>" controller="treeController" selector="treeSelector">
			</div>
		</td>
		<td valign="top" style="padding-left: 5px;" id="fieldSearch" width="40%">
			<c:if test="${form.published != true && form.formId != 1}">
				<spring:message code="Field.find" /> <br/>
				<input type="text" id="searchField" size="25" onFocus="searchType='field'" onKeyUp="searchBoxChange(fieldResults, this, event, false, 400)"/>
				<table cellspacing="0" cellpadding="2" width="100%">
					<tbody id="fieldResults">
					</tbody>
				</table>
			</c:if>
		</td>
	</tr>
</table>

<!--
<c:if test="${form.published == false}">
	<br/>
	<form method="post">
		<input type="submit" name="action" value='<spring:message code="Form.updateSortOrder" />' />
	</form>
</c:if>
-->

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

<div id="searchForm" class="searchForm">
	<div class="wrapper">
		<input type="button" onclick="return closeBox();" class="closeButton" value="X" />
		<form method="get" onsubmit="return searchBoxChange('searchBody', searchText, null, false, 0); return false;" action="">
			<h3>
				<spring:message code="Concept.find" />
			</h3>
			<input type="text" id="searchText" size="35" onkeyup="return searchBoxChange('searchBody', this, event, false, 400);">
			<input type="checkbox" id="verboseListing" value="true" <c:if test="${defaultVerbose == true}">checked</c:if> onclick="searchBoxChange('searchBody', searchText, event, false, 0); searchText.focus();"><label for="verboseListing"><spring:message code="dictionary.verboseListing"/></label>
		</form>
		<div id="searchResults" class="searchResults">
			<table cellspacing="0" cellpadding="2">
				<tbody id="searchBody">
				</tbody>
			</table>
		</div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>