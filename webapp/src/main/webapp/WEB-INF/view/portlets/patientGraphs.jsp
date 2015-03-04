<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:htmlInclude file="/scripts/flot/jquery.flot.js" />
<openmrs:htmlInclude file="/scripts/flot/jquery.flot.multiple.threshold.js"/> 

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
	
	<div class="boxHeader${model.patientVariation}"><openmrs:message code="patientDashboard.graphs"/></div>
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
			<c:forEach items="${fn:split(graphConceptString, '-')}" var="conceptIds" varStatus="varStatus">
				<c:if test="${conceptIds != ''}">
					<tr>
						<td>
							<div id="conceptBox-${conceptIds}" style="text-align: center;">
								<h1>
									<span class="conceptGraphTitle"></span>
									<openmrs:message code="patientDashboard.graphs.title"/>
								</h1>
								<a class="graphToggleLink" href="#conceptBox-${conceptIds}"><openmrs:message code="patientDashboard.graphs.hide"/></a>
								<div class="conceptBox-body">
									<div style="margin: 10px auto; height: 300px; width: 600px; align: center;" align="center" id="conceptGraphBox-${conceptIds}">
										<openmrs:message code="general.loading" />
									</div>
									<div align="center">
										<div style="width: 750px; overflow: auto; border: 1px solid black;">
											<openmrs:obsTable observations="${model.patientObs}"
												concepts="${conceptIds}"
												conceptLink="admin/observations/personObs.form?personId=${model.patientId}&"
												id="labTestTable" showEmptyConcepts="false"
												showConceptHeader="true" showDateHeader="true"
												orientation="horizontal" sort="asc" combineEqualResults="true"
												limit="-1" />
											</div>
											<div align="center" valign="top" style="font-size: .9em"><a
												href="?patientId=<c:out value="${patient.patientId}" />&patientGraphConceptRemove=true&patientGraphConcept=${conceptIds}"><openmrs:message
												code="general.remove" /></a> <br />
											<br />
										</div>
									</div>
								</div>
							</div>
							<a id="concept-${conceptIds}"></a>
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
					<openmrs:message code="patientGraphs.addNewGraph" />:<br />
					<openmrs:message code="Concept.find"/>
					<openmrs_tag:conceptField formFieldName="concept" formFieldId="conceptId" excludeDatatypes="N/A" includeDatatypes="Numeric" onSelectFunction="onConceptSelect" />
						<script type="text/javascript">
						function onConceptSelect(concept) {
							document.location="?patientId=<c:out value="${patient.patientId}" />&patientGraphConcept=" + concept.conceptId;
						}
						</script>
				</td>
			</tr>
		</table>
	</div>
	<openmrs:message code="patientDashboard.graphs.hide" var="hideLabel" scope="page"/>
	<openmrs:message code="patientDashboard.graphs.show" var="showLabel" scope="page"/>
	
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
		
		function formatDateForGraph(dateToFormat) {
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
			<c:forEach items="${fn:split(graphConceptString, '-')}" var="conceptIds">
			<c:if test="${conceptIds != ''}">
				<openmrs:globalProperty var="colorAbsolute" key="graph.color.absolute"/>
				<openmrs:globalProperty var="colorNormal" key="graph.color.normal"/>
				<openmrs:globalProperty var="colorCritical" key="graph.color.critical"/>
		$j.getJSON("patientGraphJson.form?patientId=<c:out value="${patient.patientId}" />&conceptId=${conceptIds}", function(json){
			json = json.graph.graph;
			  $j("#conceptBox-${conceptIds} .conceptGraphTitle").html(json.name);
			
			  var plot = $j.plot($j('#conceptGraphBox-${conceptIds}'),
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
			
			  $j("#conceptBox-${conceptIds}").bind("plothover", function (event, pos, item) {
				 $j("#tooltip").remove();
				 plot.unhighlight();
				 if (item) {
				  	showToolTip(item.pageX, item.pageY, "" + formatDateForGraph(new Date(item.datapoint[0])) + ": " + item.datapoint[1] + " " + json.units);
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
