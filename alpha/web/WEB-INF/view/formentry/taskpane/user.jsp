<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/formentry/taskpane/user.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<h3><spring:message code="User.title"/></h3>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.UserSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsSearch");
	
	function miniObject(o) {
		this.key = o.userId;
		this.value = searchWidget.getCellContent(o);
	}
	
	var searchWidget;
	
	dojo.addOnLoad( function() {
		
		searchWidget = dojo.widget.manager.getWidgetById("uSearch");			
		
		dojo.event.topic.subscribe("uSearch/select", 
			function(msg) {
				setObj('//encounter.provider_id', new miniObject(msg.objs[0]));
			}
		);
		
		dojo.event.topic.subscribe("uSearch/objectsFound", 
			function(msg) {
				if (msg.objs.length == 1 && typeof msg.objs[0] == 'string')
					msg.objs.push('<p class="no_hit"><spring:message code="provider.missing" /></p>');
			}
		);
		
		searchWidget.getCellContent = function(o) {
			if (typeof o == 'string') return o;
			str = ''
			str += o.firstName + " ";
			str += o.lastName;
			str += " (" + o.systemId + ")";
			return str;
		}
		
		searchWidget.inputNode.focus();
		searchWidget.inputNode.select();
		
		// prefill users on page load
		searchWidget.showAll();
	});

</script>

<div dojoType="UserSearch" widgetId="uSearch" inputWidth="10em" useOnKeyDown="true" roles='<request:existsParameter name="role"><request:parameters id="r" name="role"><request:parameterValues id="names"><jsp:getProperty name="names" property="value"/>;</request:parameterValues></request:parameters></request:existsParameter>'></div>
<br />
<small><em><spring:message code="general.search.hint"/></em></small>

<br/><br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>