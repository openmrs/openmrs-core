<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/formentry/taskpane/tribe.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<h3><spring:message code="Tribe.title"/></h3>

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/dojo/dojo.js"></script>

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
				setObj('//tribe.tribe_id', new miniObject(msg.objs[0]));
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