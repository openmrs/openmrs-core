<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View Forms" otherwise="/login.htm" redirect="/admin/forms/field.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.FieldSearch");

	dojo.addOnLoad( function() {
		dojo.event.topic.subscribe("fSearch/select", 
			function(msg) {
				var search = dojo.widget.manager.getWidgetById("fSearch");
				document.location = "field.form?fieldId=" + msg.objs[0].fieldId + "&phrase=" + search.savedText;
			}
		);
		
		dojo.widget.manager.getWidgetById("fSearch").inputNode.select();
	});
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
		<div dojoType="FieldSearch" widgetId="fSearch" searchLabel='<spring:message code="Field.search" />' fieldId='<request:parameter name="fieldId" />'></div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>
