<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/formentry/taskpane/tribe.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<c:choose>
	<c:when test="${not empty param.nodePath}">
		<c:set var="nodePath" value="${param.nodePath}"/>
	</c:when>
	<c:otherwise>
		<c:set var="nodePath" value="//tribe.tribe_id"/>
	</c:otherwise>
</c:choose>

<h3><spring:message code="Tribe.title"/></h3>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.OpenmrsSearch");
	dojo.require("dojo.widget.openmrs.PatientSearch");
	
	function miniObject(o) {
		this.key = o.tribeId;
		this.value = o.name;
	}
	
	var searchWidget;
	
	dojo.addOnLoad( function() {
		
		searchWidget = dojo.widget.manager.getWidgetById("tSearch");			
		
		dojo.event.topic.subscribe("tSearch/select", 
			function(msg) {
				setObj('${nodePath}', new miniObject(msg.objs[0]));
			}
		);
		
		dojo.event.topic.subscribe("tSearch/objectsFound", 
			function(msg) {
				if (msg.objs.length == 1 && typeof msg.objs[0] == 'string')
					msg.objs.push('<p class="no_hit"><spring:message code="Tribe.missing" /></p>');
			}
		);
		
		searchWidget.doFindObjects = function(phrase) {
			DWRPatientService.findTribes(searchWidget.simpleClosure(searchWidget, "doObjectsFound"), phrase);
			return false;
		};
		
		searchWidget.getCellContent = function(loc) {
			if (typeof loc == 'string') return loc;
				return loc.name;
		}
		
		searchWidget.showHeaderRow = false;
		
		searchWidget.inputNode.focus();
		searchWidget.inputNode.select();
		
		// prefill tribes on page load
		DWRPatientService.getTribes(searchWidget.simpleClosure(searchWidget, "doObjectsFound"));
		
	});

</script>

<div dojoType="OpenmrsSearch" widgetId="tSearch" inputWidth="10em" useOnKeyDown="true"></div>
<br />
<small><em><spring:message code="general.search.hint"/></em></small>

<br/><br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>