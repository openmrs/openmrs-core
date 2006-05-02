<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Forms" otherwise="/login.htm" redirect="/admin/forms/formDesign.form" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script src='<%= request.getContextPath() %>/dwr/interface/DWRFormService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/prototype.lite.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.pack.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/openmrsSearch.js"></script>

<style>
	.indent {
		padding-left: 2em;
	}
	.required {
		color: red;
	}
	a.delete {
		background: url(${pageContext.request.contextPath}/images/delete.gif) no-repeat center center;
		text-decoration: none;
	}
	.preview, .preview * {
		color: gray;
		background-color: whitesmoke;
	}
	#concept, #database, #other {
		height: 4em;
	}
	#editForm.preview {
		border-color: grey;
	}
	.preview #formTitle {
		background-color: whitesmoke;
	}
	
	.selected, .selected * {
		background-color: lightgreen;
		color: black;
	}
	
	#HTMLTree {
		width: 330px;
		border: 1px solid black;
		font-size: .9em;
		padding: 2px;
		overflow: auto;
		height: 610px;
	}
	
	#editForm {
		border: 2px solid lightgreen;
		padding: 1px;
	}
	
	#formTitle {
		background-color: lightgreen;
	}
	.searchForm {
		width: 370px;
		position: absolute;
		z-index: 10;
		margin: 5px;
	}
	.searchForm .wrapper {
		padding: 2px;
		background-color: whitesmoke;
		border: 1px solid grey;
		height: 570px;
	}
	.searchResults {
		height: 500px;
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
</style>

<script type="text/javascript">
	
	var editForm = null;
	var selectedLink = null;
	var editButtons = null;
	var formTitle = null;
	var mySearch = null;
	var mySearchStatus = null;
	
	function init() {
		mySearch = new fx.Resize("searchForm", {duration: 100});
		mySearch.hide();
		mySearchStatus = null;
		editForm = $('editForm');
		editButtons = $('editButtons');
		formTitle = $('formTitle');
		clearForm();
		updateParents();
	}
	
	var updateHTMLTree = function(str) {
		$('HTMLTree').innerHTML = str;
	}
	
	var updateOptionTree = function(str) {
		$('ff_parent').innerHTML = str;
	}
	
	function hoverField(id, link) {
		if (selectedLink == null) {
			editForm.className = "preview";
			editButtons.style.display = "none";
			formTitle.innerHTML = "<spring:message code="Form.preview"/>";
			DWRFormService.getFormField(fillForm, id);
		}
		return false;
	}
	
	function unHoverField(link) {
		if (selectedLink == null) {
			editForm.className = "preview";
			clearForm();
		}
	}
	
	function deleteField(id, link) {
		var answer = confirm("Are you sure you want to delete '" + id + "'?");
		if (answer == true) {
			DWRFormService.deleteFormField(updateParents, id);
		}
		return false;
	}
	
	function selectField(id, link) {
		unSelectField();
		link.className = 'selected';
		selectedLink = link;
		formTitle.innerHTML = "Edit:";
		editButtons.style.display = "";
		editForm.className = "";
		$('name').focus();
		DWRFormService.getFormField(fillForm, id);
		disableField();
		enableFormField();
		
		return false;
	}

	var fillForm = function (ff) {
		fillField(ff.field);
			
		$('ff_formFieldId').innerHTML = ff.formFieldId;
		var options = $('ff_parent').options;
		for (var i = 0; i<options.length; i++) {
			if (options[i].value == ff.parent)
				options[i].selected = true;
			else
				options[i].selected = false;
		}
		$('ff_fieldNumber').value = ff.fieldNumber;
		$('ff_fieldPart').value = ff.fieldPart;
		$('ff_pageNumber').value = ff.pageNumber;
		$('ff_minOccurs').value = ff.minOccurs;
		$('ff_maxOccurs').value = ff.maxOccurs;
		if (ff.required == 'yes')
			$('ff_required').checked = true;
		else
			$('ff_required').checked = false;
		$('ff_creator').innerHTML = ff.creator;
		$('ff_changedBy').innerHTML = ff.changedBy;
	}
	
	function fillField(obj) {
		if (obj.conceptId != null) {
			$('fieldId').value = '';
			$('fieldIdDisplay').innerHTML = '';
			$('name').value = obj.name;
			$('conceptName').innerHTML = obj.name + "(" + obj.conceptId + ")";
			$('conceptId').value = obj.conceptId;
			$('creator').innerHTML = '';
			$('selectMultiple').checked = false;
			var options = $('fieldType').options;
			for (var i = 0; i<options.length; i++) {
				if (options[i].value == 1)
					options[i].selected = true;
				else
					options[i].selected = false;
			}
			$('forms').innerHTML = '';
			chooseFieldType(1);
		}
		else {
			$('fieldId').value = obj.fieldId;
			$('fieldIdDisplay').innerHTML = obj.fieldId;
			$('name').value = obj.name;
			$('forms').innerHTML = obj.numForms;
			$('description').innerHTML = obj.description;
			var options = $('fieldType').options;
			for (var i = 0; i<options.length; i++) {
				if (options[i].value == obj.fieldTypeId)
					options[i].selected = true;
				else
					options[i].selected = false;
			}
			chooseFieldType(obj.fieldTypeId);
			if (obj.concept != null) {
				$('conceptName').innerHTML = obj.concept.name + "(" + obj.concept.conceptId + ")";
				$('conceptId').value = obj.concept.conceptId;
			}
			$('tableName').value = obj.table;
			$('attributeName').value = obj.attribute;
		
			if (obj.selectMultiple == 'yes')
				$('selectMultiple').checked = true;
			else
				$('selectMultiple').checked = false;
			if (obj.creator != null)
				$('creator').innerHTML = obj.creator;
			else
				$('creator').innerHTML = '';
		}
	}
	
	function chooseFieldType(fieldTypeId) {
		if (fieldTypeId == 1) { // == 'Concept'
			$('concept').style.display = "";
			$('database').style.display = "none";
			$('other').style.display = "none";
		}
		else if (fieldTypeId == 2) { // -- db element
			$('database').style.display = "";
			$('concept').style.display = "none";
			$('other').style.display = "none";
		}
		else {
			$('other').style.display = "";
			$('concept').style.display = "none";
			$('database').style.display = "none";
		}
	}
	
	function clearForm() {
		editForm.className = "preview";
		editButtons.style.display = "none";

		clearField();
		
		$('ff_formFieldId').innerHTML = '';
		$('ff_fieldNumber').value = '';
		$('ff_fieldPart').value = '';
		$('ff_pageNumber').value = '';
		$('ff_minOccurs').value = '';
		$('ff_maxOccurs').value = '';
		$('ff_required').checked = false;
		$('ff_creator').innerHTML = '';
		$('ff_changedBy').innerHTML = '';	
	}
	
	function clearField() {
		$('fieldId').value = '';
		$('fieldIdDisplay').innerHTML = '';
		$('name').value = '';
		$('forms').innerHTML = '';
		$('description').innerHTML = '';
		var options = $('fieldType').options;
		for (var i = 0; i<options.length; i++) {
			options[i].selected = false;
		}
		$('concept').style.display = "";
		$('conceptName').innerHTML = "";
		$('conceptId').value = "";
		$('database').style.display = "none";
		$('other').style.display = "none"
		$('selectMultiple').checked = false;
		$('creator').innerHTML = "";
	}
	
	function addNewField() {
		clearField();
		enableField();
		$('name').focus();
		selectedLink = "new";
	}
	
	function addNewFormField() {
		cancel();
		editForm.className = "";
		enableFormField();
		enableField();
		formTitle.innerHTML = "Add New: ";
		$('name').focus();
		editButtons.style.display = "";
		selectedLink = "new";
		
		return false;
	}
	
	function refresh() {
		document.location = document.location;
	}
	
	function editForThisForm() {
		enableField();
		if ($('forms').innerHTML != '1' && $('forms').innerHTML != '') {
			$('fieldId').value = '';
			$('fieldIdDisplay').innerHTML = '';
		}
		$('name').focus();
	}
	
	function editForAllForms() {
		document.location = "field.form?fieldId=" + $('fieldId').value;
	}
	
	function disableField() {
		$('name').disabled = true;
		$('description').disabled = true;
		$('tableName').disabled = true;
		$('attributeName').disabled = true;
		$('fieldType').disabled = true;
		$('selectMultiple').disabled = true;
		$('field').className = 'preview';
	}
	
	function enableField() {
		$('name').disabled = false;
		$('description').disabled = false;
		$('tableName').disabled = false;
		$('attributeName').disabled = false;
		$('fieldType').disabled = false;
		$('selectMultiple').disabled = false;
		$('field').className = '';
	}
	
	function disableFormField() {
		$('ff_formFieldId').disabled = true;
		$('ff_fieldNumber').disabled = true;
		$('ff_fieldPart').disabled = true;
		$('ff_pageNumber').disabled = true;
		$('ff_minOccurs').disabled = true;
		$('ff_maxOccurs').disabled = true;
		$('ff_required').disabled = true;
	}
	
	function enableFormField() {
		$('ff_formFieldId').disabled = false;
		$('ff_fieldNumber').disabled = false;
		$('ff_fieldPart').disabled = false;
		$('ff_pageNumber').disabled = false;
		$('ff_minOccurs').disabled = false;
		$('ff_maxOccurs').disabled = false;
		$('ff_required').disabled = false;
	}
	
	function updateParents() {
		DWRFormService.getOptionTree(updateOptionTree, <request:parameter name="formId"/>);
		DWRFormService.getHTMLTree(updateHTMLTree, <request:parameter name="formId"/>);
		cancel();	
	}
	
	function unSelectField(link) {
		editForm.className = 'preview';
		formTitle.innerHTML = '&nbsp;';
		disableField();
		disableFormField();
		if (selectedLink != null && typeof selectedLink != "string")
			selectedLink.className = '';
		selectedLink = null;
	}

	function save() {
		var fieldId = null;
		if ($('fieldId').value != 'undefined')
			fieldId = $('fieldId').value;
		var fieldName = $('name').value;
		var fieldDesc = $('description').value;
		var fieldType = $('fieldType').value;
		var concept = null
		var table = '';
		var attr  = '';
		if ($('concept').style.display != "none")
			concept = $('conceptId').value;
		else {
			table = $('tableName').value;
			attr = $('attributeName').value;
		}
		var multiple = $('selectMultiple').checked;
		
		var formFieldId = $('ff_formFieldId').innerHTML;
		var parent = $('ff_parent').value;
		var number = $('ff_fieldNumber').value;
		if (number.length == 0)
			number = null;
		var part   = $('ff_fieldPart').value;
		if (part.length == 0)
			part = null;
		var page   = $('ff_pageNumber').value;
		if (page.length == 0)
			page = null;
		var min    = $('ff_minOccurs').value;
		if (min.length == 0)
			min = null;
		var max    = $('ff_maxOccurs').value;
		if (max.length == 0)
			max = null;
		var required = $('ff_required').checked;
		
		var formId = <request:parameter name="formId"/>;
		
		DWRFormService.saveFormField(updateParents, fieldId, fieldName, fieldDesc, fieldType, concept, table, attr, 
			multiple, formFieldId, formId, parent, number, part, page, min, max, required);
	}
	
	function cancel() {
		unSelectField();
		disableField();
		disableFormField();
		clearForm();
	}
	
	function showSearchForm(input, event) {
		if (mySearchStatus == null) {
			mySearch.toggle();
			mySearchStatus = "1";
		}
		searchBoxChange('searchBody', input, event, false, 300);
	}
	
	var findObjects = function(txt) {
		DWRFormService.findFieldsAndConcepts(fillTable, txt);
	}
	
	var lastConceptId = "-1";
	var getName = function(obj) {
		if (typeof obj == 'string') return obj;
		var s = "";
		if (obj.conceptId != null) {
			lastConceptId = obj.conceptId;
			s = "CONCEPT." + obj.name + " (" + obj.conceptId + ")";
		}
		else if (obj.fieldId != null) {
			if (obj.concept != null && obj.concept.conceptId == lastConceptId)
				s = " &nbsp; &nbsp; ";
			s += obj.name + " (" + obj.numForms + " forms)";
		}
		
		if ($('verboseListing').checked)
			s += "<div class='description'>" + obj.description + "</div>";

		return s;
	}
	
	var customCellFunctions = [getNumber, getName];
	
	function onSelect(fields) {
		fillField(fields[0]);
		closeBox();
	}
	
	function showVerbose() {
		showSearchForm($('name'), null);
	}
	
	function allowAutoListWithNumber() {
		return true;
	}
	
	function closeBox() {
		mySearch.hide();
		mySearchStatus = null;
		return false;
	}
	
	var oldonload = window.onload;
	if (typeof window.onload != 'function') {
		window.onload = init;
	} else {
		window.onload = function() {
			oldonload();
			init();
		}
	}

</script>

<h2>
	<spring:message code="Form.design.title" /> - 
	${form.name}
</h2>

<c:if test="${form.published == true}">
	<div class="retiredMessage"><div><spring:message code="Form.design.disabled"/></div></div>
</c:if>

<table width="99%">
	<tr>
		<td valign="top" width="625">
			<a href="javascript:refresh()"><spring:message code="general.refresh"/></a>
			<div id="HTMLTree">
			</div>
			
			<br/><br/>
			<a href="formEdit.form?formId=${form.formId}"><spring:message code="Form.editProperties" /></a> |
			<a href="${pageContext.request.contextPath}/formDownload?target=schema&formId=${form.formId}"><spring:message code="Form.downloadSchema" /></a> |
			<a href="${pageContext.request.contextPath}/formDownload?target=template&formId=${form.formId}"><spring:message code="Form.downloadTemplate" /></a> |
			<a href="${pageContext.request.contextPath}/formDownload?target=xsn&formId=${form.formId}"><spring:message code="Form.downloadXSN" /></a>
		</td>
		<td valign="top" style="padding-left: 10px;">
			<a href="#add" onclick="return addNewFormField();"><spring:message code="FormField.add" /></a> <br /><br />
			<div id="editForm">
				<div id="formTitle"></div>
				
				<div id="fieldButtons">
					<c:if test="${form.published == false}">
						<input type="button" onclick="addNewField()" value="<spring:message code="Field.addNewField"/>"/>
						<input type="button" onclick="editForThisForm()" value="<spring:message code="Field.editForThisForm"/>"/>
						<input type="button" onclick="editForAllForms()" value="<spring:message code="Field.editForAllForms"/>"/>
					</c:if>
				</div>
				
				<%@ include file="formFieldEdit.jsp" %>
				
				<div id="editButtons">
					<c:if test="${form.published == false}">
						<input type="button" value="<spring:message code="general.save"/>" onclick="save()" />
						<input type="button" value="<spring:message code="general.cancel"/>" onclick="cancel()" />
					</c:if>
				</div>
			</div>
		</td>
	</tr>
</table>

<%@ include file="/WEB-INF/template/footer.jsp" %>