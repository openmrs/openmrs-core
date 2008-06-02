<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View Forms" otherwise="/login.htm" redirect="/admin/forms/field.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.ConceptSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");

	dojo.addOnLoad( function() {
		
		dojo.event.topic.subscribe("cSearch/select", 
			function(msg) {
				var popup = dojo.widget.manager.getWidgetById("conceptSelection");
				popup.hiddenInputNode.value = msg.objs[0].conceptId;
				popup.displayNode.innerHTML = msg.objs[0].name;
			}
		);
		
		chooseFieldType($('fieldType').value);
		
	});


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

</script>

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
