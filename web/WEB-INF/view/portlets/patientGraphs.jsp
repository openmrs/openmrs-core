<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:htmlInclude file="/scripts/dojoConfig.js"/>
<openmrs:htmlInclude file="/scripts/dojo/dojo.js"/>

<script type="text/javascript">	
	dojo.require("dojo.widget.openmrs.ConceptSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");
	
	dojo.addOnLoad( function() {
		
		searchWidget = dojo.widget.manager.getWidgetById("cSearch");			
		
		dojo.event.topic.subscribe("cSearch/select", 
			function(msg) {
				document.location="?patientId=${patient.patientId}&patientGraphConcept=" + msg.objs[0].conceptId;
			}
		);
	});
</script>

<style>
table#labTestTable {
	border: 2px solid black;
	border-spacing: 0px;
	border-collapse: collapse;
	margin: 2px;
}

table#labTestTable td {
	border: 1px solid black;
	padding: 3px;
}

table#labTestTable th {
	border: 1px solid black;
	padding: 3px;
}
</style>

<openmrs:userProperty key="patientGraphConcepts" defaultValue="" var="userConcepts" />

<%
	if (request.getParameter("patientGraphConcept") != null) {
		String userConcepts = (String)pageContext.getAttribute("userConcepts");
		if (request.getParameter("patientGraphConceptRemove") != null) {
			String[] conceptList = userConcepts.split("-");
			userConcepts = "";
			for (String s : conceptList)
				if (!request.getParameter("patientGraphConcept").equals(s) && !"".equals(s))
					userConcepts += "-" + s;
		}
		else {
			if (!userConcepts.contains(request.getParameter("patientGraphConcept")))
				userConcepts += "-" + request.getParameter("patientGraphConcept");
		}
		pageContext.setAttribute("userConcepts", userConcepts);
		org.openmrs.api.context.Context.getAuthenticatedUser().setUserProperty("patientGraphConcepts", userConcepts); // set this for the currently cached logged in user
		org.openmrs.User user = org.openmrs.api.context.Context.getUserService().getUser(org.openmrs.api.context.Context.getAuthenticatedUser().getUserId());
		org.openmrs.api.context.Context.getUserService().setUserProperty(user, "patientGraphConcepts", userConcepts); // save it to the db for this user so it reappears when they log in again
	}
%>

<c:set var="graphConceptString" value="${userConcepts}" />
	
	<div class="boxHeader${model.patientVariation}"><spring:message code="patientDashboard.graphs"/></div>
	<div class="box${model.patientVariation}">
		<table width="100%">
			<tr>
				<td align="center">
					<c:if test="${fn:length(userConcepts) > 0}">
						<div style="width: 750px; overflow: auto; border: 1px solid black;"> 
							<openmrs:obsTable
								observations="${model.patientObs}"
								concepts="${fn:replace(userConcepts, '-', '|')}"
								conceptLink="admin/observations/personObs.form?personId=${model.patientId}&"
								id="labTestTable"
								showEmptyConcepts="false"
								showConceptHeader="true"
								showDateHeader="true"
								orientation="horizontal"
								sort="asc"
								combineEqualResults="true"
								limit="-1"
							/>
						</div>
					</c:if>
				</td>
			</tr>
			<c:forEach items="${fn:split(graphConceptString, '-')}" var="conceptId" varStatus="varStatus">
				<c:if test="${conceptId != ''}">
					<tr>
						<td>
						<div>
						<div align="center" id="conceptBox-${conceptId}"><spring:message
							code="general.loading" /></div>
						<div align="center">
						<div style="width: 750px; overflow: auto; border: 1px solid black;">
		
						<openmrs:obsTable observations="${model.patientObs}"
							concepts="${conceptId}"
							conceptLink="admin/observations/personObs.form?personId=${model.patientId}&"
							id="labTestTable" showEmptyConcepts="false"
							showConceptHeader="true" showDateHeader="true"
							orientation="horizontal" sort="asc" combineEqualResults="true"
							limit="-1" />
						</div>
						<div align="center" valign="top" style="font-size: .9em"><a
							href="?patientId=${patient.patientId}&patientGraphConceptRemove=true&patientGraphConcept=${conceptId}"><spring:message
							code="general.remove" /></a> <br />
						<br />
						</div>
		
						</div>
		
		
						</div>
						</td>
					</tr>
					<tr>
						<td><br />
						<hr />
						<br />
						</td>
					</tr>
				</c:if>
			</c:forEach>
			<tr>
				<td>
					<form>
						<spring:message code="patientGraphs.addNewGraph" />
						<span dojoType="ConceptSearch" widgetId="cSearch" includeDatatypes="Numeric"></span><br/>
						<span dojoType="OpenmrsPopup" searchWidget="cSearch" searchTitle='<spring:message code="Concept.find"/>' changeButtonValue='<spring:message code="general.choose"/>'></span> 
					</form>
				</td>
			</tr>
		</table>
	</div>
	
	<script type="text/javascript">
		function loadGraphs() {
			<c:forEach items="${fn:split(graphConceptString, '-')}" var="conceptId">
				<c:if test="${conceptId != ''}">
					<openmrs:concept conceptId="${conceptId}" var="concept" nameVar="name" numericVar="num">
						document.getElementById('conceptBox-${conceptId}').innerHTML = '<img src="${pageContext.request.contextPath}/showGraphServlet?patientId=${patient.patientId}&conceptId=${conceptId}&width=600&height=300&minRange=<c:out value="${num.lowAbsolute}" default="0.0"/>&maxRange=<c:out value="${num.hiAbsolute}" default="200.0"/>" />';
					</openmrs:concept>
				</c:if>
			</c:forEach>
		}
		window.setTimeout(loadGraphs, 1000);		
	</script>
	