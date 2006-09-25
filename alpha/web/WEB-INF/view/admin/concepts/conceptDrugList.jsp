<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Concepts" otherwise="/login.htm" redirect="/admin/concepts/conceptDrug.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.ConceptSearch");

	<request:existsParameter name="autoJump">
		var autoJump = <request:parameter name="autoJump"/>;
	</request:existsParameter>

	dojo.addOnLoad( function() {
		
		var dSearch = dojo.widget.manager.getWidgetById("dSearch");
		
		dojo.event.topic.subscribe("dSearch/select", 
			function(msg) {
				document.location = "conceptDrug.form?drugId=" + msg.objs[0].drugId + "&phrase=" + dSearch.savedText;
			}
		);
		
		dSearch.doFindObjects = function(txt) {
			DWRConceptService.findDrugs(dSearch.simpleClosure(dSearch, 'doObjectsFound'), txt, dSearch.includeRetired);
		}
		
		dojo.widget.manager.getWidgetById("dSearch").inputNode.select();
	});
</script>

<h2><spring:message code="ConceptDrug.title"/></h2>

<a href="conceptDrug.form"><spring:message code="ConceptDrug.add"/></a><br/><br/>

<div id="findConceptDrug">
	<b class="boxHeader"><spring:message code="ConceptDrug.find"/></b>
	<div class="box">
		<div dojoType="ConceptSearch" widgetId="dSearch" drugId='<request:existsParameter name="conceptDrugId">request.getAttribute("conceptDrugId")</request:existsParameter>' showIncludeRetired="true" searchTitle="<spring:message code="ConceptDrug.search"/>" searchPhrase='<request:existsParameter name="phrase"><request:parameter name="phrase" /></request:existsParameter>'></div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>