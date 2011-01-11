<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:htmlInclude file="/scripts/flot/jquery.flot.js" />
<openmrs:htmlInclude file="/scripts/flot/jquery.flot.multiple.threshold.js"/> 

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
							<div id="conceptBox-${conceptId}" style="text-align: center;">
								<h1>
									<span class="conceptGraphTitle"></span>
									<spring:message code="patientDashboard.graphs.title"/>
								</h1>
								<a class="graphToggleLink" href="#conceptBox-${conceptId}"><spring:message code="patientDashboard.graphs.hide"/></a>
								<div class="conceptBox-body">
									<div style="margin: 10px auto; height: 300px; width: 600px; align: center;" align="center" id="conceptGraphBox-${conceptId}">
										<spring:message code="general.loading" />
									</div>
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
							</div>
							<a id="concept-${conceptId}"></a>
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
	<spring:message code="patientDashboard.graphs.hide" var="hideLabel" scope="page"/>
	<spring:message code="patientDashboard.graphs.show" var="showLabel" scope="page"/>
	
	<script type="text/javascript">
		function findMaxAndMin(dataset) {
			if(undefined == dataset)return undefined;
				var arr = [];
				for( var i=0;i<dataset.length;i++){
				   arr[i] = dataset[i][1];
			}
			arr.sort(function(p1,p2){return p1-p2});
			return { min:arr[0],max:arr[arr.length-1]};
		}
		  
		function showToolTip(x, y, contents){
		    $j('<div id="tooltip">' + contents + '</div>').css( {
		           position: 'absolute',
		           display: 'none',
		           top: y + 5,
		           left: x + 5,
		           border: '1px solid #fdd',
		           padding: '2px',
		           'background-color': '#fee',
		           opacity: 0.80
		       }).appendTo("body").fadeIn(200);
		}
		
		function formatDate(dateToFormat) {
			return "" + dateToFormat.getDate() + "/" + (dateToFormat.getMonth() + 1) + "/" + dateToFormat.getFullYear();
		}
		
		function toggleGraphVisibility(evt) {
			var tgt, gBox; 
			
			tgt = $j(evt.target);
			
			if (tgt.hasClass("graphToggleLink")) {
				gBox = $j(tgt.next());
				
				gBox.toggle(0, function () {
					// change the text in the link
					if (gBox.css("display") == "none") {
						tgt.text("${showLabel}");
					} else {
						tgt.text("${hideLabel}");
					}
				});		// toggle the display of the graph and table
			}
		}
		
		function loadGraphs() {
			<c:forEach items="${fn:split(graphConceptString, '-')}" var="conceptId">
			<c:if test="${conceptId != ''}">
				<openmrs:globalProperty var="colorAbsolute" key="graph.color.absolute"/>
				<openmrs:globalProperty var="colorNormal" key="graph.color.normal"/>
				<openmrs:globalProperty var="colorCritical" key="graph.color.critical"/>			
		$j.getJSON("patientGraphJson.form?patientId=${patient.patientId}&conceptId=${conceptId}", function(json){
			  $j("#conceptBox-${conceptId} .conceptGraphTitle").html(json.name);
			
			  var plot = $j.plot($j('#conceptGraphBox-${conceptId}'),
			  [{
			  	data: json.data, 
			  	lines:{show:true}, 
			  	points: { show: true }, 
			  	color:"rgb(0,0,0)",
			  	constraints: [{
                  	    threshold: {above:json.normal.high},
                  	    color: "${colorNormal}"
                 	}, {
                     	threshold: {below:json.normal.low},
                  	    color: "${colorNormal}"
                 	}, {
                  	    threshold: {above:json.absolute.high},
                  	    color: "${colorAbsolute}"
                 	}, {
                  	    threshold: {below:json.absolute.low},
                  	    color: "${colorAbsolute}"
                 	}, {
                  	    threshold: {above:json.critical.high},
                  	    color: "${colorCritical}"
                 	}, {
                  	    threshold: {below:json.critical.low},
                  	    color: "${colorCritical}"
                 	}]
                 }], {
                     xaxis: {mode: "time", timeformat: "%b %y", minTickSize: [1, "month"]},
				  yaxis: {min: findMaxAndMin(json.data).min-10, max: findMaxAndMin(json.data).max+10, tickFormatter: function (v, axis) { return v.toFixed(axis.tickDecimals) + " " + json.units }},
			  	  grid: { hoverable: true, clickable: true }
				});							  
			
			  $j("#conceptBox-${conceptId}").bind("plothover", function (event, pos, item) {
				 $j("#tooltip").remove();
				 plot.unhighlight();
				 if (item) {
				  	showToolTip(item.pageX, item.pageY, "" + formatDate(new Date(item.datapoint[0])) + ": " + item.datapoint[1] + " " + json.units);
				  	plot.highlight(item.series, item.datapoint);
				 }
			  });
		});
			</c:if>
	    </c:forEach>
	
		}

		$j(document).ready(function () {

			

			// ensure Hide Graph links are hooked
			$j(".box${model.patientVariation}").bind("click", toggleGraphVisibility);

			window.setTimeout(loadGraphs, 1000);
		});
		
	</script>
	