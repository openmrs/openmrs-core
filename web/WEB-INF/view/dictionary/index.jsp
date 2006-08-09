<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:require privilege="View Concepts" otherwise="/login.htm"
	redirect="/dictionary/index.htm" />

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/dojo/dojo.js"></script>

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.ConceptSearch");
	
	var searchWidget;
	
	dojo.addOnLoad( function() {
		
		searchWidget = dojo.widget.manager.getWidgetById("cSearch");			
		
		dojo.event.topic.subscribe("cSearch/select", 
			function(msg) {
				document.location = "concept.htm?conceptId=" + msg.objs[0].conceptId;
			}
		);
		
		searchWidget.inputNode.select();
	});

</script>

<h2><spring:message code="dictionary.title" /></h2>

<a href="<%= request.getContextPath() %>/downloadDictionary.csv"><spring:message code="dictionary.download.link"/></a> <spring:message code="dictionary.download.description"/><br />
<br />

<div id="findPatient">
	<b class="boxHeader"><spring:message code="Concept.find"/></b>
	<div class="box">
		<div dojoType="ConceptSearch" widgetId="cSearch" searchLabel='<spring:message code="dictionary.searchBox"/>' searchPhrase='<request:parameter name="phrase"/>' showVerboseListing="true" showIncludeRetired="true"></div>
	</div>
</div>

<br/>
<a href="concept.form"><spring:message code="Concept.add"/></a> (Use sparingly)

<%@ include file="/WEB-INF/template/footer.jsp" %>