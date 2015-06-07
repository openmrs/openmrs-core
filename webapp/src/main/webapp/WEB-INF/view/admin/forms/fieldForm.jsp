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
				popup.hiddenCodedDatatype.value = msg.objs[0].isCodedDatatype;
				popup.displayNode.innerHTML = msg.objs[0].name;
				setSelectMultiple(jQuery('#hiddenCodedDatatype').val());
			}
		);
		
		chooseFieldType(jQuery('#fieldType').val());
	});


	function chooseFieldType(fieldTypeId) {
		if (fieldTypeId == 1) { // == 'Concept'
			jQuery('#concept').css('display', "");
			jQuery('#database').css('display', "none");
			setSelectMultiple(jQuery('#hiddenCodedDatatype').val());
		}
		else if (fieldTypeId == 2) { // -- db element
			jQuery('#database').css('display', "");
			jQuery('#concept').css('display', "none");
			jQuery('#selectMultiple').attr("disabled", "disabled");
			jQuery('#selectMultiple').attr("checked", false);
		}
		else {
			jQuery('#concept').css('display', "none");
			jQuery('#database').css('display', "none");
			jQuery('#selectMultiple').attr("disabled", "disabled");
			jQuery('#selectMultiple').attr("checked", false);
		}
	}
	
	function setSelectMultiple(isCodedDatatype) {
		if (isCodedDatatype == "true") {
			jQuery('#selectMultiple').removeAttr("disabled");
		}
		else{
			jQuery('#selectMultiple').attr("checked", false);
			jQuery('#selectMultiple').attr("disabled", "disabled");
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

<br/>
<br/>
<c:if test="${field.fieldId!=null}">
<div class="boxHeader">
	<b><openmrs:message code="Field.formTableTitle" /></b>
</div>
<div class="box">
	<table cellpadding="2" cellspacing="0" id="formTable" width="98%">
		<tr>
			<th> <openmrs:message code="general.name" /> </th>
			<th> <openmrs:message code="Form.version" /> </th>
			<th> <openmrs:message code="Form.build" /> </th>
			<th> <openmrs:message code="general.description" /> </th>
			<th> <openmrs:message code="Form.published" /> </th>

		</tr>
		<c:forEach var="form" items="${formList}" varStatus="status">
			<tr class='${status.index % 2 == 0 ? "evenRow" : "oddRow"} ${form.retired ? "voided" : ""}'>
				<td valign="top" style="white-space: nowrap"><a href="formEdit.form?formId=${form.formId}"><c:out value="${form.name}"/></a></td>
				<td valign="top">${form.version}</td>
				<td valign="top">${form.build}</td>
				<td valign="top"><c:out value="${form.description}"/></td>
				<td valign="top"><c:if test="${form.published == true}"><openmrs:message code="general.yes"/></c:if></td>
			</tr>
		</c:forEach>
	</table>
</div>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>
