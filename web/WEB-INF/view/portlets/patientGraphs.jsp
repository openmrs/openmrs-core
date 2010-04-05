<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:htmlInclude file="/scripts/jquery/jquery-1.3.2.min.js" />
<openmrs:htmlInclude file="/scripts/flot/jquery.flot.js" />
<openmrs:htmlInclude file="/scripts/flot/jquery.flot.multiple.threshold.js"/> 

<script type="text/javascript">
	var $j = jQuery.noConflict();
</script>
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
						<div  style="margin: 0pt auto; height: 300px; width: 600px; align: center" align="center" id="conceptBox-${conceptId}"><spring:message
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
					$j.getJSON("patientGraphJson.form?patientId=${patient.patientId}&conceptId=${conceptId}", function(json){
						  $j.plot($j('#conceptBox-${conceptId}'),
						  [
						  {
						  	data:json.data,lines:{show:true},color:"rgb(0,0,0)",
						  	constraints:
						  	[
						  	{
                           	    threshold: {above:json.normal.high},
                           	    color: "rgb(255,126,0)"
                          	},
                          	{
                           	    threshold: {below:json.normal.low},
                           	    color: "rgb(255,126,0)"
                          	},
                          	{
                           	    threshold: {above:json.absolute.high},
                           	    color: "rgb(20,20,20)"
                          	},
                          	{
                           	    threshold: {below:json.absolute.low},
                           	    color: "rgb(20,20,20)"
                          	},
                          	{
                           	    threshold: {above:json.critical.high},
                           	    color: "rgb(200,0,0)"
                          	},
                          	{
                           	    threshold: {below:json.critical.low},
                           	    color: "rgb(200,0,0)"
                          	}	                        
                          	]
                          	}],
                          	{ 
								xaxis: { 
										mode: "time",minTickSize: [1, "month"]
										},
								yaxis: {
										min: findMaxAndMin(json.data).min-10, max: findMaxAndMin(json.data).max+10
						  				}
							}
                          );
						  
							function findMaxAndMin(dataset) {
								if(undefined == dataset)return undefined;
								var arr = [];
								for( var i=0;i<dataset.length;i++){
								   arr[i] = dataset[i][1];
								}
								arr.sort(function(p1,p2){return p1-p2});
								return { min:arr[0],max:arr[arr.length-1]};
							}
					}
					);
				</c:if>
			</c:forEach>
		}
		window.setTimeout(loadGraphs, 1000);		
	</script>
	