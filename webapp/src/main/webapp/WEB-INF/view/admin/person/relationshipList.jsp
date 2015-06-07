<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Relationships" otherwise="/login.htm" redirect="/admin/person/relationshipList.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.PersonSearch");
	
	dojo.addOnLoad( function() {
		
		searchWidget = dojo.widget.manager.getWidgetById("pSearch");			
		
		dojo.event.topic.subscribe("pSearch/select", 
			function(msg) {
				document.location = "relationship.form?personId=" + msg.objs[0].personId;
			}
		);
		
		searchWidget.inputNode.select();
	});
	
</script>

<h2><openmrs:message code="Relationship.title"/></h2>

<a href="${pageContext.request.contextPath}/admin/person/relationship.form"><openmrs:message code="Relationship.add"/></a><br/><br/>

<div id="findRelationship">
	<b class="boxHeader"><openmrs:message code="Person.find"/></b>
	<div class="box">
		<div dojoType="PersonSearch" widgetId="pSearch" inputName="personName" searchLabel='<openmrs:message code="Person.searchBox"/>'></div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
