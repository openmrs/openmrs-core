<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:require privilege="View Concepts" otherwise="/login.htm"
	redirect="/dictionary/index.htm" />

<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

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
		
		searchWidget.inputNode.focus();
		searchWidget.inputNode.select();
	});

</script>

<h2><spring:message code="dictionary.title" /></h2>

<a href="<%= request.getContextPath() %>/downloadDictionary.csv"><spring:message code="dictionary.download.link"/></a> <spring:message code="dictionary.download.description"/><br />
<br />

<div id="findConcept">
	<b class="boxHeader"><spring:message code="Concept.find"/></b>
	<div class="box">
		<div dojoType="ConceptSearch" widgetId="cSearch" searchLabel='<spring:message code="dictionary.searchBox"/>' searchPhrase='<request:parameter name="phrase"/>' showVerboseListing="true" showIncludeRetired="true"></div>
	</div>
</div>

<br/>

<openmrs:globalProperty key="concepts.locked" var="conceptsLocked"/>
<c:choose>
	<c:when test="${conceptsLocked != 'true'}"> 
		<a href="concept.form"><spring:message code="Concept.add"/></a> (Use sparingly)
	</c:when>
	<c:otherwise>
		(<spring:message code="Concept.concepts.locked" />)
	</c:otherwise>
</c:choose>		

<openmrs:extensionPoint pointId="org.openmrs.dictionary.index" type="html" />

<%@ include file="/WEB-INF/template/footer.jsp" %>