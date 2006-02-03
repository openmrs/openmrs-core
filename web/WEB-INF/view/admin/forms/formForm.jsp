<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Forms" otherwise="/login.htm" redirect="/admin/forms/form.form" />
	
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
		background: url(/@WEBAPP.NAME@/images/delete.gif) no-repeat center center;
		text-decoration: none;
	}
	.preview, .preview * {
		color: gray;
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
		width: 600px;
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
		width: 400px;
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
			formTitle.innerHTML = "Preview: (click to edit)";
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
		//TODO: finish delete function
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
		$('ff_creator').innerHTML = ff.creator;
		$('ff_changedBy').innerHTML = ff.changedBy;
	}
	
	function fillField(field) {
		$('fieldId').value = field.fieldId;
		$('name').value = field.name;
		$('forms').innerHTML = field.numForms;
		$('description').innerHTML = field.description;
		var options = $('fieldType').options;
		for (var i = 0; i<options.length; i++) {
			if (options[i].value == field.fieldTypeId)
				options[i].selected = true;
			else
				options[i].selected = false;
		}
		chooseFieldType(field.fieldTypeId);
		if (field.concept != null)
			$('conceptName').innerHTML = field.concept.name;
		$('tableName').value = field.table;
		$('attributeName').value = field.attribute;
		
		if (field.selectMultiple == 'yes')
			$('selectMultiple').checked = true;
		$('creator').innerHTML = field.creator;
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
		$('name').value = '';
		$('forms').innerHTML = '';
		$('description').innerHTML = '';
		var options = $('fieldType').options;
		for (var i = 0; i<options.length; i++) {
			options[i].selected = false;
		}
		$('concept').style.display = "";
		$('conceptName').innerHTML = "";
		$('database').style.display = "none";
		$('other').style.display = "none"
		$('selectMultiple').checked = false;
		$('creator').innerHTML = "";
	}
	
	function addNewField() {
		clearField();
		enableField();
		$('name').focus();
	}
	
	function editForThisForm() {
		enableField();
		if ($('forms').innerHTML != '1' && $('forms').innerHTML != '')
			$('fieldId').value = '';
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
	}
	
	function enableField() {
		$('name').disabled = false;
		$('description').disabled = false;
		$('tableName').disabled = false;
		$('attributeName').disabled = false;
		$('fieldType').disabled = false;
		$('selectMultiple').disabled = false;
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
		DWRFormService.getHTMLTree(updateHTMLTree, <request:parameter name="formId"/>);
		DWRFormService.getOptionTree(updateOptionTree, <request:parameter name="formId"/>);
	}
	
	function unSelectField(link) {
		if (selectedLink != null)
			selectedLink.className = '';
		editForm.className = 'preview';
		formTitle.innerHTML = '&nbsp;';
		selectedLink = null;
		disableField();
		disableFormField();
	}

	function save() {
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
		DWRFormService.findFields(fillTable, txt);
	}
	
	var lastConceptId = "-1";
	var getName = function(obj) {
		if (typeof obj == 'string') return obj;
		var s = "";
		if (obj.conceptId != null) {
			lastConceptId = obj.conceptId;
			s = "CONCEPT." + obj.name;
		}
		else if (obj.fieldId != null) {
			if (obj.concept != null && obj.concept.conceptId == lastConceptId)
				s = " &nbsp; &nbsp; ";
			s += obj.name;
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

<h2><spring:message code="Form.manage" /></h2>	

<a href="formField.edit"><spring:message code="FormField.add" /></a> <br />

<br />

<table width="99%">
	<tr>
		<td valign="top" width="625">
			<div id="HTMLTree">
			</div>
		</td>
		<td valign="top" style="padding-left: 10px;">
			<div id="editForm">
				<div id="formTitle"></div>
				
				<input type="button" onclick="addNewField()" value="<spring:message code="Field.addNewField"/>"/>
				<input type="button" onclick="editForThisForm()" value="<spring:message code="Field.editForThisForm"/>"/>
				<input type="button" onclick="editForAllForms()" value="<spring:message code="Field.editForAllForms"/>"/>
				
				<%@ include file="formFieldEdit.jsp" %>
				<div id="editButtons">
					<input type="button" value="<spring:message code="general.save"/>" onclick="save()" />
					<input type="button" value="<spring:message code="general.cancel"/>" onclick="cancel()" />
				</div>
			</div>
		</td>
	</tr>
</table>

<%@ include file="/WEB-INF/template/footer.jsp" %>