<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View Fields" otherwise="/login.htm" redirect="/admin/forms/field.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<script src='<%= request.getContextPath() %>/dwr/interface/DWRFormService.js' type="text/javascript"></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js' type="text/javascript"></script>
<script src='<%= request.getContextPath() %>/dwr/util.js' type="text/javascript"></script>
<script src='<%= request.getContextPath() %>/scripts/openmrsSearch.js' type="text/javascript"></script>

<script type="text/javascript">
	var savedText = "";
	function showSearch() {
		fieldListing.style.display = "none";
		searchBox.focus();
	}
	
	function onSelect(arr) {
		document.location = "field.form?fieldId=" + arr[0].fieldId + "&phrase=" + savedText;
	}
	
	function findObjects(text) {
		savedText = text;
		DWRFormService.findFields(fillTable, text);
		fieldListing.style.display = "";
		return false;
	}
	
	var getName = function(f) {
		if (typeof f == 'string') return f;
		return f.name;
	}
	
	var getType = function(f) {
		if (typeof f == 'string') return '';
		return f.fieldTypeName;
	}
	
	var getDesc = function(f) {
		if (typeof f == 'string') return '';
		return f.description;
	}
		
	var customCellFunctions = [getNumber, getName, getType, getDesc];
	
	function search(event, delay) {
		searchBoxChange("fieldTableBody", $('searchBox'), event, delay);
		return false;
	}
	
</script>

<h2>
	<spring:message code="Field.title" />
</h2>

<a href="field.form">
	<spring:message code="Field.add" />
</a>
<br />
<br />

<div id="findField">
	<b class="boxHeader">
		<spring:message code="Field.find" />
	</b>
	<div class="box">
		<form id="findFieldForm" onsubmit="return search(event, 0);" action="">
			<table>
				<tr>
					<td>
						<spring:message code="Field.search" />
					</td>
					<td>
						<input type="text" id="searchBox" onkeyup="search(event, 400)">
					</td>
				</tr>
			</table>
		</form>
		<div id="fieldListing">
			<table id="fieldTable" cellspacing="0" cellpadding="1" width="100%">
				<thead>
					<tr>
						<th>
						</th>
						<th>
							<spring:message code="general.name" />
						</th>
						<th>
							<spring:message code="Field.type" />
						</th>
						<th>
							<spring:message code="general.description" />
						</th>
					</tr>
				</thead>
				<tbody id="fieldTableBody">
				</tbody>
			</table>
		</div>
	</div>
</div>

<script type="text/javascript">

	var fieldListing	= document.getElementById("fieldListing");
	var searchBox		= document.getElementById("searchBox");
	
	showSearch();
	
	<request:existsParameter name="fieldId">
		var fields = new Array();
		var fields[0] = new Object();
		fields[0].fieldId = request.getAttribute("fieldId");
		onSelect(fields);
	</request:existsParameter>
	
	<request:existsParameter name="phrase">
		searchBox.value = '<request:parameter name="phrase" />';
	</request:existsParameter>
	
	// creates back button functionality
	if (searchBox.value != "")
		searchBoxChange("fieldTableBody", searchBox, null, 0, 0);
	
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>
