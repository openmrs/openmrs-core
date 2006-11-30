<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/formentry/taskpane/conceptAnswer.htm" />

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	<c:if test="${empty param.nodePath}">alert("Error 3947: A parameter named 'nodePath' must be defined");</c:if>
	<c:if test="${empty param.conceptId}">alert("Error 3948: A parameter named 'conceptId' must be defined");</c:if>
	
	dojo.require("dojo.widget.openmrs.ConceptSearch");
	
	function miniObject(c) {
		this.key = c.conceptId;
		this.value = c.name;
	}
	
	var searchWidget;
	
	dojo.addOnLoad( function() {
		
		searchWidget = dojo.widget.manager.getWidgetById("cSearch");			
		
		dojo.event.topic.subscribe("cSearch/select", 
			function(msg) {
				for (i=0; i<msg.objs.length; i++) {
					pickConcept('${param.nodePath}', new miniObject(msg.objs[i]));
				}
			}
		);
		
		var label = searchWidget.verboseListing.previousSibling;
		label.parentNode.insertBefore(document.createElement('br'), label);
		
		searchWidget.inputNode.focus();
		searchWidget.inputNode.select();

	});

</script>

<c:choose>
	<c:when test="${empty param.title}">
		<h3><spring:message code="conceptAnswer.title"/></h3>
	</c:when>
	<c:otherwise>
		<h3><spring:message code="${param.title}"/></h3>
	</c:otherwise>
</c:choose>

<div id="conceptAnswerConceptName"><openmrs_tag:concept conceptId="${param.conceptId}"/></div>

<div id="searchForm">
	<div dojoType="ConceptSearch" widgetId="cSearch" inputWidth="9em" showAnswers="${param.conceptId}" performInitialSearch="true" useOnKeyDown="true" allowConceptEdit="false"></div>
	<br />
	<small>
		<em>
			<spring:message code="general.search.hint" />
		</em>
	</small>
</div>

<br />

<%@ include file="/WEB-INF/template/footer.jsp"%>