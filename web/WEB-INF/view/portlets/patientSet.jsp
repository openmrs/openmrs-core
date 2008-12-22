<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:htmlInclude file="/scripts/easyAjax.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRPatientSetService.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<c:choose>
	<c:when test="${model.bodyId != null}">
		<c:set var="tableBodyId" value="${model.bodyId}"/>
	</c:when>
	<c:otherwise>
		<c:set var="tableBodyId" value="patientSetTableBody"/>
	</c:otherwise>
</c:choose>

<script type="text/javascript">
	var localList = null;

	if (${model.patientSet != null}) {
		var tmp = '${model.patientSet.commaSeparatedPatientIds}';
		if (tmp != '')
			localList = tmp.split(",");
		else
			localList = new Array();
		<c:if test="${model.varToSet != null}">
			<%-- TODO: refresh this variable on changes to localList --%>
			${model.varToSet} = "${model.patientSet.commaSeparatedPatientIds}";
		</c:if>
	}
	
	<c:if test="${model.droppable && model.patientSetSize == 0}">
		if (localList == null) {
			hideLayer('${model.id}');
		}
	</c:if>
	

	var PS_fromIndex = 0;
	var PS_pageSize = 15;
	<c:if test="${model.pageSize != null}">PS_pageSize = ${model.pageSize}</c:if>
	var PS_totalPatients = 0;

	function remove(patientId) {
		if (localList != null) {
			for (var i = 0; i < localList.length; ++i) {
				if (localList[i] == patientId) {
					localList.splice(i, 1);
					--i;
				}
			}
			refreshList();
		} else {
			DWRPatientSetService.removeFromMyPatientSet(patientId, function() { refreshList() });
		}
	}
	
	function clearThis() {
		if (localList != null) {
			localList = new Array();
			refreshList();
			hideLayer('${model.id}');
		} else {
			DWRPatientSetService.clearMyPatientSet(<c:if test="${model.id != null}">function() { refreshList(); hideLayer('${model.id}')}</c:if>);
		}
	}
	
	function goToStart() {
		PS_fromIndex = 0;
		refreshList();
	}
	
	function pageBackwards() {
		PS_fromIndex -= PS_pageSize;
		if (PS_fromIndex < 0) {
			PS_fromIndex = 0;
		}
		refreshList();
	}

	function pageForwards() {
		if (PS_totalPatients > 0 && (PS_fromIndex + PS_pageSize) >= PS_totalPatients)
			return;
		PS_fromIndex += PS_pageSize;
		if (localList != null && PS_fromIndex >= PS_totalPatients) {
			PS_fromIndex -= PS_pageSize;
			return;
		}
		refreshList();
	}
	
	function goToEnd() {
		PS_fromIndex = PS_totalPatients - PS_pageSize;
		if (PS_fromIndex < 0) {
			PS_fromIndex = 0;
		}
		refreshList();
	}
	
	function refreshList() {
		if (localList != null) {
			PS_totalPatients = localList.length;
			document.getElementById("PS_totalNumber").innerHTML = localList.length;
		} else {
			DWRPatientSetService.getMyPatientSetSize(function(sz) { PS_totalPatients = sz; document.getElementById("PS_totalNumber").innerHTML = sz; });
		}
		<%-- TODO: this is commented out because it breaks the page in IE. Find the right place to put it.
		dwr.util.useLoadingMessage();
		--%>
		if (localList != null) {
			var ptIds = new Array();
			var n = PS_fromIndex + PS_pageSize;
			if (n > localList.length) {
				n = localList.length;
			}
			for (var i = PS_fromIndex; i < n; ++i) {
				ptIds.push(localList[i]);
			}
			var commaSeparated = ptIds.join();
			DWRPatientSetService.getPatients(commaSeparated, handleRefreshList);
		} else {
			DWRPatientSetService.getFromMyPatientSet(PS_fromIndex, PS_pageSize, handleRefreshList);
		}
	}
	
	function handleRefreshList(patients) {
		<c:if test="${model.id != null}">
			if (patients.length == 0) {
				<c:if test="${model.droppable}">
					hideLayer('${model.id}');
				</c:if>
				return;
			} else {
				showLayer('${model.id}');
			}
		</c:if>
		dwr.util.removeAllRows("${tableBodyId}");
		if (PS_fromIndex > 0) {
			var row = document.getElementById("${tableBodyId}").insertRow(0);
			var cell = row.insertCell(0);
			cell.colSpan = 5;
			cell.innerHTML = "...";
		}
		var count = PS_fromIndex + 1;
		var cellFuncs = [
			function(patient) { return count++ + "."; },
			function(patient) {
					var isSel = <c:choose><c:when test="${empty OPENMRS_VIEWING_PATIENT_ID}">false</c:when><c:otherwise>patient.patientId == ${OPENMRS_VIEWING_PATIENT_ID}</c:otherwise></c:choose> ;
			<c:choose>
				<c:when test="${model.linkUrl == null}">
					return (isSel ? "<b>" : "") + patient.givenName + " " + patient.middleName + " " + patient.familyName + (isSel ? "</b>" : "");
				</c:when>
				<c:otherwise>
					if (isSel) {
						return '<b>' + patient.givenName + " " + patient.middleName + " " + patient.familyName + '</b>';
					} else {
						return '<a href="<%= request.getContextPath() %>/${model.linkUrl}?patientId=' + patient.patientId + '">' + patient.givenName + ' ' + patient.middleName + ' ' + patient.familyName + '</a>';
					}
				</c:otherwise>
			</c:choose>
			},
			function(patient) { return patient.gender; },
			function(patient) { return patient.age; },
			function(patient) { return '<c:if test="${model.mutable}"><a href="javascript:remove(' + patient.patientId + ')" style="color: red">[x]<a></c:if>'; }
		];
		dwr.util.addRows("${tableBodyId}", patients, cellFuncs, { escapeHtml:false });
		if (PS_fromIndex + PS_pageSize < PS_totalPatients) {
			var tbl = document.getElementById("${tableBodyId}");
			var row = tbl.insertRow(tbl.rows.length);
			var cell = row.insertCell(0);
			cell.colSpan = 5;
			cell.innerHTML = "...";
		}
	}
</script>

<c:if test="${model.size != 'full'}">
	<div id="patientSetTray" onmouseover="javascript:showLayer('patientSetBox')">
		<div id="patientSetBox">
			<div style="text-align: right">
				<a href="javascript:hideLayer('patientSetBox')">[<spring:message code="general.hide"/>]</a>
			</div>
</c:if>
			<table id="${model.tableId}">
				<thead id="${model.headId}">
					<tr>
						<th colspan="5">
							<a href="javascript:goToStart()">|&lt;</a>
								&nbsp;&nbsp;&nbsp;
							<a href="javascript:pageBackwards()">&lt;</a>
								&nbsp;&nbsp;&nbsp;
							<spring:message code="PatientSet.setOfN" arguments='<span id="PS_totalNumber">?</span>'/>
								&nbsp;&nbsp;&nbsp;
							<a href="javascript:pageForwards()">&gt;</a>
								&nbsp;&nbsp;&nbsp;
							<a href="javascript:goToEnd()">&gt;|</a>
						</th>
					</tr>
					<tr>
						<th>#</th>
						<th><spring:message code="Person.name"/></th>
						<th><spring:message code="Person.gender"/></th>
						<th><spring:message code="Person.age"/></th>
						<th></th>
					</tr>
				</thead>
				<tbody id="${tableBodyId}">
				</tbody>
			</table>

			<c:if test="${model.allowActions == 'true'}">
				<div id="actionLinks">
					<p/>
					<b>
						<spring:message code="general.action"/>:
					</b>
					<span id="summariesLink">
						<a href="<%= request.getContextPath() %>/patientSummaries.form?source=myPatientSet">
							<spring:message code="summary.generateSummaries"/>
						</a>
					</span>
					<c:if test="${model.allowBatchEntry == 'true' && fn:length(model.batchEntryForms) > 0}">
						<span id="batchEntryLink">
							|
							<a href="javascript:showLayer('patientSetBatchEntryBox'); hideLayer('batchEntryLink')">
								<spring:message code="BatchEntry.title"/>
							</a>
						</span>
		
						<div id="patientSetBatchEntryBox" style="display: none">
							<p/>
							<b><spring:message code="BatchEntry.title"/>:</b>
							<ul>
								<c:forEach var="form" items="${model.batchEntryForms}">
									<li><a href="<%= request.getContextPath() %>/module/formentry/batchForm.form?formId=${form.formId}">${form.name}</a></li>
								</c:forEach>
							</ul>
						</div>
					</c:if>
				</div>
			</c:if>
<c:if test="${model.size != 'full'}">
		</div>

<script type="text/javascript">
	<request:equalsParameter name="showPatientSet" match="true" ignoreCase="true" value="false">
		hideLayer('patientSetBox');
	</request:equalsParameter>
</script>

		<div class="popupTrayTitle">
			<spring:message code="PatientSet.yours" />
			<c:if test="${model.droppable}">
				&nbsp;<br/>
				<a href="javascript:clearThis()" style="color: red">
					[<spring:message code="PatientSet.drop" />]
				</a>
			</c:if>
		</div>
	</div>
</c:if>

<script type="text/javascript">
	addEvent(window, "load", refreshList);
</script>