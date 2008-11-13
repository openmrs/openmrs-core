<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Encounters" otherwise="/login.htm" redirect="/admin/encounters/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.EncounterSearch");

	var eSearch;
	
	dojo.addOnLoad( function() {
		
		eSearch = dojo.widget.manager.getWidgetById('eSearch');
		
		dojo.event.topic.subscribe("eSearch/select", 
			function(msg) {
				document.location = "encounter.form?encounterId=" + msg.objs[0].encounterId + "&phrase=" + eSearch.getPhraseSearched();
			}
		);
		
		<request:existsParameter name="phrase">
			searchBox.value = '<request:parameter name="phrase" />';
		</request:existsParameter>
	
		eSearch.inputNode.focus();
		eSearch.inputNode.select();
	});
		
</script>

<h2><spring:message code="Encounter.title"/></h2>

<a href="encounter.form"><spring:message code="Encounter.add"/></a>

<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.index.afterAdd" type="html" />

<br/><br/>

<div id="findEncounter">
	<b class="boxHeader"><spring:message code="Encounter.find"/></b>
	<div class="box">
		<div dojoType="EncounterSearch" widgetId="eSearch" showIncludeVoided="true" <request:existsParameter name="autoJump">allowAutoJump='true'</request:existsParameter> encounterId='<request:parameter name="encounterId" />'></div>
	</div>
</div>

<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.index.footer" type="html" />

<%@ include file="/WEB-INF/template/footer.jsp" %>