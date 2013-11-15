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
		
		chooseFieldType(jQuery('#fieldType').val());
		
	});


	function chooseFieldType(fieldTypeId) {
		if (fieldTypeId == 1) { // == 'Concept'
			jQuery('#concept').css('display', "");
			jQuery('#database').css('display', "none");
		}
		else if (fieldTypeId == 2) { // -- db element
			jQuery('#database').css('display', "");
			jQuery('#concept').css('display', "none");
		}
		else {
			jQuery('#concept').css('display', "none");
			jQuery('#database').css('display', "none");
		}
	}

</script>

<h2>
	<openmrs:message code="Field.title" />
</h2>

<spring:hasBindErrors name="field">
	<openmrs:message htmlEscape="false" code="fix.error" />
	<br />
	<!-- ${errors} -->
</spring:hasBindErrors>
<form method="post" action="">

	<%@ include file="include/fieldEdit.jsp" %>

	<br />
	<input type="hidden" name="phrase" value='<request:parameter name="phrase" />' />
	
	<input type="submit" value='<openmrs:message code="general.save"/>' name="action">
	<c:if test="${field.fieldId != null && empty param.duplicate}">
		&nbsp; &nbsp; <input type="submit" onclick="return confirm('<openmrs:message code="Field.deleteWarning"/>')" value='<openmrs:message code="general.delete"/>' name="action">
	</c:if>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
