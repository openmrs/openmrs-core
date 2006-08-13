<%@ include file="/WEB-INF/template/include.jsp" %>

<!-- Concept View for FieldGen module -->
<!-- <input type="text" size="6" name="${model.formFieldName}" value="${model.obj.conceptId}" /> -->
<!-- Concept View for FieldGen module -->

<script type="text/javascript">
	var djConfig = {debugAtAllCosts: false, isDebug: false};
</script>

<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.ConceptSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");
	
	dojo.hostenv.writeIncludes();
	
	dojo.addOnLoad( function() {
		var cSearchWidget = dojo.widget.manager.getWidgetById("${model.formFieldName}_cSearch");
		var cSelection = dojo.widget.manager.getWidgetById("${model.formFieldName}_cSelection");
		
		dojo.event.topic.subscribe("${model.formFieldName}_cSearch/select", 
			function (msg) {
				location.href = "concept.form?conceptId=" + msg.objs[0].conceptId;
			} 
		);
		
		dojo.event.topic.subscribe("${model.formFieldName}_cSearch/objectsFound",
			function (msg) {
				if (msg)
					msg.objects.push("<a href='concept.form'>Add New Concept</a>");
			} 
		);
		
		dojo.event.topic.subscribe("${model.formFieldName}_c2Search/select", 
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
   z-index: 5;
  }
</style>

<div dojoType="ConceptSearch" widgetId="${model.formFieldName}_c2Search" showVerboseListing="true"></div>
<div dojoType="OpenmrsPopup" widgetId="${model.formFieldName}_cSelection" searchWidget="${model.formFieldName}_c2Search" searchTitle="Find Concepts"></div>

