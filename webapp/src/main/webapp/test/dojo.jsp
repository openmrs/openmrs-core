<%@ include file="/WEB-INF/template/include.jsp" %>

<link href="<%= request.getContextPath() %>/openmrs.css" type="text/css" rel="stylesheet" />

<script type="text/javascript">
	var djConfig = {debugAtAllCosts: true, isDebug: true};
</script>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.EncounterSearch");
	dojo.require("dojo.widget.openmrs.ConceptSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");
	
	dojo.addOnLoad( function() {
		var eSearchWidget = dojo.widget.manager.getWidgetById("eSearch");
		var cSearchWidget = dojo.widget.manager.getWidgetById("cSearch");
		var cSelection = dojo.widget.manager.getWidgetById("cSelection");
		
		dojo.event.topic.subscribe("eSearch/select", 
			function (msg) {
				location.href = "encounter.form?encounterId=" + msg.objs[0].encounterId;
			} 
		);
		
		dojo.event.topic.subscribe("cSearch/select", 
			function (msg) {
				location.href = "concept.form?conceptId=" + msg.objs[0].conceptId;
			} 
		);
		
		dojo.event.topic.subscribe("cSearch/objectsFound",
			function (msg) {
				if (msg)
					msg.objs.push("<a href='concept.form'>Add New Concept</a>");
			} 
		);
		
		dojo.event.topic.subscribe("c2Search/select", 
			function(msg) {
				if (msg) {
					var concept = msg.objs[0];
					cSelection.displayNode.innerHTML = concept.name;
					cSelection.hiddenInputNode.value = concept.conceptId;
				}
			}
		);
				
	});
	
</script>

<style>
 .searchBox {
   height: 320px;
   width: 45%;
   overflow-y:scroll;
   border: 1px solid black;
   float: left;
   padding: 3px;
   margin: 2px;
  }
</style>

<div class="searchBox">
	<b>Encounter Search</b><br/>
	<div dojoType="EncounterSearch" widgetId="eSearch" showIncludeVoided="true"></div>
</div>

<div class="searchBox">
	<b>Concept Search</b><br/>
	<div dojoType="ConceptSearch" widgetId="cSearch" showVerboseListing="true" showIncludeRetired="true" tableHeight="270"></div>
</div>

<div class="searchBox">
	<b>Concept Selection</b>
	<div dojoType="ConceptSearch" widgetId="c2Search" showVerboseListing="true" excludeClasses="Diagnosis"></div>
	<div dojoType="OpenmrsPopup" widgetId="cSelection" searchWidget="c2Search" searchTitle="Concept Search"></div>
</div>


<br style="clear: both">

