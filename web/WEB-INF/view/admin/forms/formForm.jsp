<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Forms" otherwise="/login.htm" redirect="/admin/forms/form.form" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script src='<%= request.getContextPath() %>/dwr/interface/DWRFormService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>

<style>
	.indent {
		padding-left: 2em;
	}
	.required {
		color: red;
	}
	x.delete {
		background: url(/@WEBAPP.NAME@/images/delete.gif) no-repeat center center;
	}
	.disabled * {
		color: gray;
		background-color: whitesmoke;
	}
	.selected {
		background-color: lightgrey;
	}
	
	#previewForm {
		width: 250px;
		border: 1px solid darkgray;
	}
	
	#formTree {
		height: 800px;
		overflow: auto;
		width: 400px;
	}
	
	#editForm {
		width: 250px;
		border: 1px solid black;
	}
</style>

<script type="text/javascript">
	
	var editForm = null;
	var previewForm = null;
	var selectedLink = null;
	
	function init() {
		editForm = $('editForm');
		previewForm = $('previewForm');
	}
	
	function hoverField(id, link) {
		previewForm.className = "";
		DWRFormService.getFormField(previewFormField, id);
		return false;
	}
	
	function unHoverField(link) {
		previewForm.className = "disabled";
		clearFormFieldPreview();
	}
	
	function deleteField(id, link) {
		//TODO: finish delete function
		return false;
	}
	
	var previewFormField = function(ff) {
		$('p_formFieldId').innerHTML = ff.formFieldId;
		$('p_parent').innerHTML = ff.parent.formFieldId;
		$('p_field').innerHTML = '#' + ff.field.fieldId + " " + ff.field.name;
		$('p_fieldNumber').innerHTML = ff.fieldNumber;
		$('p_fieldPart').innerHTML = ff.fieldPart;
		$('p_pageNumber').innerHTML = ff.pageNumber;
		$('p_minOccurs').innerHTML = ff.minOccurs;
		$('p_maxOccurs').innerHTML = ff.maxOccurs;
		$('p_required').innerHTML = ff.required == true ? 'yes' : 'no';
		$('p_createdBy').innerHTML = ff.createdBy.firstName + " " + ff.createdBy.lastName;
		$('p_changedBy').innerHTML = ff.changedBy.firstName + " " + ff.changedBy.lastName;
	}
	
	function clearFormFieldPreview() {
		$('p_formFieldId').innerHTML = '';
		$('p_parent').innerHTML = '';
		$('p_field').innerHTML = '';
		$('p_fieldNumber').innerHTML = '';
		$('p_fieldPart').innerHTML = '';
		$('p_pageNumber').innerHTML = '';
		$('p_minOccurs').innerHTML = '';
		$('p_maxOccurs').innerHTML = '';
		$('p_required').innerHTML = '';
		$('p_createdBy').innerHTML = '';
		$('p_changedBy').innerHTML = '';
	}
	
	function selectField(id, link) {
		link.className = 'selected';
		selectedLink = link;
		editForm.style.visibility = 'show';
		DWRFormService.getFormField(editFormField, id);
	}
	
	var editFormField = function (ff) {
		
	}
	
	function unSelectField() {
		selectedLink.className = '';
		editForm.style.visibility = 'hide';
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

<table>
	<tr>
		<td valign="top">
			<div id="formTree">
				${tree}
			</div>
		</td>
		<td valign="top">
			<div id="previewForm" class="disabled">
				<form>
					<%@ include file="formFieldPreview.jsp" %>
				</form>
			</div>
		</td>
		<td valign="top">
			<div id="editForm">
				<form>
					<%@ include file="formFieldEdit.jsp" %>
					<input type="submit" value="<spring:message code="general.save"/>"/>
				</form>
			</div>
		</td>
	</tr>
</table>

<%@ include file="/WEB-INF/template/footer.jsp" %>