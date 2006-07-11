<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View Forms" otherwise="/login.htm" redirect="/admin/forms/field.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/prototype.lite.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.pack.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/openmrsSearch.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/conceptSearch.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRConceptService.js'></script>

<script type="text/javascript">

var myConceptSearch = null;
var changeButton = null;

var init = function() {
	myConceptSearch = new fx.Resize("searchForm", {duration: 100});
	myConceptSearch.hide();
	chooseFieldType($('fieldType').value);
};

var onSelect = function(objs) {
	var obj = objs[0];
	$("conceptId").value = obj.conceptId;
	$("conceptName").innerHTML = obj.name;
	myConceptSearch.hide();
	changeButton.focus();
	return false;
}

function showConceptSearch(btn) {
	setPosition(btn, $("searchForm"), 515, 500);
	resetForm();
	DWRUtil.removeAllRows("conceptSearchBody");
	myConceptSearch.toggle();
	$("searchText").value = '';
	$("searchText").select();
	changeButton = btn;
}

function closeBox() {
	myConceptSearch.hide();
	return false;
}

function chooseFieldType(fieldTypeId) {
	if (fieldTypeId == 1) { // == 'Concept'
		$('concept').style.display = "";
		$('database').style.display = "none";
	}
	else if (fieldTypeId == 2) { // -- db element
		$('database').style.display = "";
		$('concept').style.display = "none";
	}
	else {
		$('concept').style.display = "none";
		$('database').style.display = "none";
	}
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

<style type="text/css">
	.searchForm {
		width: 500px;
		position: absolute;
		z-index: 10;
		margin: 5px;
		left: -1000px;
	}
	.searchForm .wrapper {
		padding: 2px;
		background-color: whitesmoke;
		border: 1px solid grey;
		height: 475px;
	}
	.searchResults {
		height: 420px;
		overflow: auto;
	}
</style>

<h2>
	<spring:message code="Field.title" />
</h2>

<spring:hasBindErrors name="field">
	<spring:message code="fix.error" />
	<br />
	<!-- ${errors} -->
</spring:hasBindErrors>
<form method="post" action="">

	<%@ include file="include/fieldEdit.jsp" %>

	<br />
	<input type="hidden" name="phrase" value='<request:parameter name="phrase" />' />
	<input type="submit" value='<spring:message code="general.save"/>'>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
