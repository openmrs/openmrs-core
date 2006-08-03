<%@ include file="/WEB-INF/template/include.jsp" %>

<script type="text/javascript">
	var djConfig = {debugAtAllCosts: true, isDebug: true};
</script>

<script type="text/javascript" src="/openmrs/scripts/dojo/dojo.js"></script>

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.EncounterSearch");
	dojo.require("dojo.widget.openmrs.ConceptSearch");
	
	dojo.hostenv.writeIncludes();
	
</script>

<style>
 .searchBox {
   height: 300px;
   width: 45%;
   float: left;
   overflow-y:scroll;
   border: 1px solid black;
   padding: 3px;
   margin: 2px;
  }
</style>

<div class="searchBox">
<b>Encounter Search</b><br>

<div dojoType="EncounterSearch" widgetId="eSearch" showIncludeVoided="true"></div>
	
</div>

<div class="searchBox">
<b>Concept Search</b><br>

<div dojoType="ConceptSearch" widgetId="cSearch" showVerboseListing="true" showIncludeRetired="true"></div>

</div>

<br style="clear: both">

